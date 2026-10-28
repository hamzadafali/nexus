package hz.labs.nexus.application;

import hz.labs.nexus.api.AskResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    // Convertit le texte du modele en AskResponse et fournit les instructions de format JSON (schema).
    private final BeanOutputConverter<AskResponse> converter = new BeanOutputConverter<>(AskResponse.class);

    private final ChatClient chatClient;
    private final ChatClient fallbackClient;
    private final Resource systemPrompt;
    private final String promptVersion;
    private final AiUsageRecorder usageRecorder;
    private final Validator validator;
    private final boolean fallbackEnabled;

    public AiService(ChatClient chatClient,
                     @Qualifier("fallbackModel") ChatModel fallbackModel,
                     @Value("${nexus.ai.fallback-enabled}") boolean fallbackEnabled,
                     @Value("${nexus.ai.prompts.ask-system}") Resource systemPrompt,
                     @Value("${nexus.ai.prompts.ask-version}") String promptVersion,
                     AiUsageRecorder usageRecorder,
                     Validator validator) {
        this.chatClient = chatClient;
        // Client dedie au modele de secours : meme usage que chatClient, autre provider.
        this.fallbackClient = ChatClient.create(fallbackModel);
        this.fallbackEnabled = fallbackEnabled;
        this.systemPrompt = systemPrompt;
        this.promptVersion = promptVersion;
        this.usageRecorder = usageRecorder;
        this.validator = validator;
    }


    /**
     * Interroge le modele IA pour analyser le texte fourni.
     * Si la reponse est invalide, on refait UN seul appel en montrant au modele son erreur.
     * Un refus du modele n'est pas une erreur : il est renvoye avec le statut REFUSED, sans retry.
     * Si le provider principal est en panne, l'appel bascule sur le modele de secours (si active).
     *
     * @param text le texte a analyser, jamais vide (verifie en amont par le controller)
     * @return le resultat structure retourne par le modele, valide via Bean Validation
     * @throws InvalidAiResponseException si la reponse reste invalide apres le retry
     */
    public AskResponse analyse(String text) {
        // Conversation envoyee au modele : le texte utilisateur + le schema JSON attendu.
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(text + "\n\n" + converter.getFormat()));

        // 1er essai.
        Generation first = call(messages);
        String refusal = refusalOf(first);
        if (refusal != null) {
            return AskResponse.refused(refusal);
        }

        String firstAnswer = first.getOutput().getText();
        try {
            return parseAndValidate(firstAnswer);
        } catch (InvalidAiResponseException error) {
            // Retry unique : on ajoute la reponse fautive + l'erreur, et on demande une correction.
            messages.add(new AssistantMessage(firstAnswer));
            messages.add(new UserMessage("""
                    Ta reponse precedente est invalide : %s
                    Corrige-la et renvoie uniquement le JSON, sans texte autour."""
                    .formatted(error.getMessage())));

            // 2e et dernier essai : s'il echoue, l'exception remonte a l'appelant.
            Generation second = call(messages);
            String secondRefusal = refusalOf(second);
            if (secondRefusal != null) {
                return AskResponse.refused(secondRefusal);
            }
            return parseAndValidate(second.getOutput().getText());
        }
    }

    /**
     * Envoie la conversation au modele principal ; en cas d'erreur du provider (apres les retries Spring AI),
     * rejoue la meme conversation sur le modele de secours.
     */
    private Generation call(List<Message> messages) {
        try {
            return call(chatClient, messages);
        } catch (RuntimeException primaryError) {
            if (!fallbackEnabled) {
                throw primaryError;
            }
            log.atWarn()
                    .addKeyValue("error", primaryError.toString())
                    .log("ai.fallback");
            try {
                return call(fallbackClient, messages);
            } catch (RuntimeException fallbackError) {
                // Les deux providers sont KO : on garde la trace de l'erreur initiale.
                fallbackError.addSuppressed(primaryError);
                throw fallbackError;
            }
        }
    }

    /** Envoie la conversation via le client donne, enregistre l'usage (tokens, cout, latence) et retourne la generation. */
    private Generation call(ChatClient client, List<Message> messages) {
        long start = System.nanoTime();

        ChatResponse response = client.prompt()
                .system(systemPrompt)
                .messages(messages)
                .call()
                .chatResponse();

        Duration latency = Duration.ofNanos(System.nanoTime() - start);
        usageRecorder.record(response, promptVersion, latency);

        return response.getResult();
    }

    /**
     * Detecte un refus signale par le provider (le modele lui-meme peut aussi repondre status=REFUSED dans le JSON).
     *
     * @return le motif du refus, ou null si le modele a repondu normalement
     */
    private String refusalOf(Generation generation) {
        // Champ "refusal" d'OpenAI : rempli quand le modele refuse au lieu de produire le JSON demande.
        Object refusal = generation.getOutput().getMetadata().get("refusal");
        if (refusal instanceof String reason && !reason.isBlank()) {
            return reason;
        }
        String finishReason = generation.getMetadata().getFinishReason();
        // Reponse coupee par le filtre de contenu (OpenAI).
        if ("CONTENT_FILTER".equalsIgnoreCase(finishReason)) {
            return "Reponse bloquee par le filtre de contenu du provider.";
        }
        // Anthropic signale un refus via stop_reason=refusal.
        if ("REFUSAL".equalsIgnoreCase(finishReason)) {
            return "Le modele a refuse de repondre.";
        }
        return null;
    }

    /** Transforme le texte en AskResponse puis verifie les contraintes (@NotBlank, @Min, @Max). */
    private AskResponse parseAndValidate(String answer) {
        // Etape 1 : le texte doit etre un JSON lisible.
        AskResponse parsed;
        try {
            parsed = converter.convert(answer);
        } catch (JacksonException e) {
            throw new InvalidAiResponseException("JSON illisible (" + e.getOriginalMessage() + ")");
        }

        // Etape 2 : les valeurs doivent respecter les contraintes de AskResponse.
        Set<ConstraintViolation<AskResponse>> violations = validator.validate(parsed);
        if (!violations.isEmpty()) {
            String details = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining(", "));
            throw new InvalidAiResponseException("contraintes non respectees (" + details + ")");
        }

        return parsed;
    }
}
