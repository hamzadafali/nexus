package hz.labs.nexus.application;

/**
 * Levee quand la reponse du modele n'est pas un JSON valide pour AskResponse
 * (JSON illisible OU contraintes Bean Validation non respectees).
 * Le message decrit l'erreur : il est renvoye tel quel au modele pour qu'il se corrige.
 */
public class InvalidAiResponseException extends RuntimeException {

    public InvalidAiResponseException(String message) {
        super(message);
    }
}
