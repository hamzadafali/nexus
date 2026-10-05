package hz.labs.nexus.application;

import hz.labs.nexus.api.AskRequest;
import hz.labs.nexus.api.LlmResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiService {


    private static final String SYSTEM_PROMPT = """
            Tu es un assistant expert qui analyse un texte technique (question, extrait de code
            ou description d'un probleme) et en extrait :
            - skill : la competence ou le domaine technique concerne (ex: "Java Concurrency", "Java Streams")
            - explanation : une explication concise du probleme et/ou de sa resolution
            - level : le niveau de difficulte estime, sur une echelle de 1 (debutant) a 5 (expert)
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
     */
    public LlmResponse analyse(String text) {
        // Etape 2 : on envoie le prompt systeme (consignes d'analyse) et le texte utilisateur au modele
        // Etape 3 : .entity(...) demande a Spring AI de generer le schema JSON attendu et de deserialiser
        // automatiquement la reponse du modele en instance de AnalyseResult
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(text)
                .call()
                .entity(LlmResponse.class);
    }
}
