package hz.labs.nexus.application;

import hz.labs.nexus.api.LlmResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
public class AiService {


    private static final String SYSTEM_PROMPT = """
           You are NEXUS, an AI assistant for software engineering teams. Answer questions about software architecture,
            development, operations and incidents clearly and concisely. You do not have access to the organization's 
            internal documentation or systems yet: if a question depends on internal knowledge (a specific service, 
            incident or team), say so explicitly instead of guessing.
            """;

    private final ChatClient chatClient;

    public AiService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }


    /**
     * Interroge le modele IA pour analyser le texte fourni.
     *
     * @param text le texte a analyser, jamais vide (verifie en amont par le controller)
     * @return le resultat structure retourne par le modele
     * @throws AiProviderUnavailableException si le provider est injoignable ou repond en erreur
     */
    public LlmResponse analyse(String text) {
        // Etape 2 : on envoie le prompt systeme (consignes d'analyse) et le texte utilisateur au modele
        // Etape 3 : .entity(...) demande a Spring AI de generer le schema JSON attendu et de deserialiser
        // automatiquement la reponse du modele en instance de AnalyseResult
        try {
            return chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(text)
                    .call()
                    .entity(LlmResponse.class);
        }
        // TransientAiException : 429 et 5xx du provider, une fois les retries epuises.
        // NonTransientAiException : 4xx (cle invalide, requete refusee).
        // RestClientException : couche transport (timeout de connexion ou de lecture, DNS).
        catch (TransientAiException | NonTransientAiException | RestClientException exception) {
            throw new AiProviderUnavailableException(exception);
        }
    }
}
