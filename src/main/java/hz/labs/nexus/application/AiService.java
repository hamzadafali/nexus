package hz.labs.nexus.application;

import hz.labs.nexus.api.AskResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AiService {


    private final ChatClient chatClient;
    private final Resource systemPrompt;
    private final String promptVersion;
    private final AiUsageRecorder usageRecorder;

    public AiService(ChatClient chatClient,
                     @Value("${nexus.ai.prompts.ask-system}") Resource systemPrompt,
                     @Value("${nexus.ai.prompts.ask-version}") String promptVersion,
                     AiUsageRecorder usageRecorder) {
        this.chatClient = chatClient;
        this.systemPrompt = systemPrompt;
        this.promptVersion = promptVersion;
        this.usageRecorder = usageRecorder;
    }


    /**
     * Interroge le modele IA pour analyser le texte fourni.
     *
     * @param text le texte a analyser, jamais vide (verifie en amont par le controller)
     * @return le resultat structure retourne par le modele
     */
    public AskResponse analyse(String text) {
        long start = System.nanoTime();

        // On envoie le prompt système (instructions) et le texte utilisateur (données) au modèle.
        // responseEntity(...) génère le schéma JSON, désérialise la réponse en AskResponse
        // ET conserve le ChatResponse brut (usage tokens + modèle) pour les métriques.
        ResponseEntity<ChatResponse, AskResponse> result = chatClient.prompt()
                .system(systemPrompt)
                .user(text)
                .call()
                .responseEntity(AskResponse.class);

        Duration latency = Duration.ofNanos(System.nanoTime() - start);
        usageRecorder.record(result.response(), promptVersion, latency);

        return result.entity();
    }
}
