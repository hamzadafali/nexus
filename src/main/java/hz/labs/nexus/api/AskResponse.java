package hz.labs.nexus.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Reponse de NEXUS, exploitable par le front.
 *
 * @param status     ANSWERED pour une reponse normale, REFUSED si le modele refuse de repondre
 * @param answer     la reponse en Markdown, ou le motif du refus
 * @param confidence confiance du modele entre 0 et 1 (basse si la question depend de connaissances internes)
 * @param followUps  2 a 3 questions de relance suggerees a l'utilisateur
 */
public record AskResponse(
        @NotNull AskStatus status,
        @NotBlank String answer,
        @DecimalMin("0.0") @DecimalMax("1.0") Double confidence,
        @Size(max = 3) List<@NotBlank String> followUps) {

    /** Reponse de refus : seul le motif est renseigne, pas de relances. */
    public static AskResponse refused(String reason) {
        return new AskResponse(AskStatus.REFUSED, reason, null, List.of());
    }

    /** confidence et 2 a 3 followUps ne sont obligatoires que pour une vraie reponse. */
    @JsonIgnore
    @AssertTrue(message = "confidence et 2 a 3 followUps sont obligatoires quand status=ANSWERED")
    public boolean isCompleteWhenAnswered() {
        return status != AskStatus.ANSWERED
                || (confidence != null && followUps != null && followUps.size() >= 2);
    }
}
