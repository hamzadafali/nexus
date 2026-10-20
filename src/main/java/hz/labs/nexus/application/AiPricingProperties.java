package hz.labs.nexus.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Map;

/**
 * Tarifs des modèles IA, en USD par million de tokens.
 */
@ConfigurationProperties("nexus.ai.pricing")
public record AiPricingProperties(Map<String, ModelPrice> models) {

    private static final BigDecimal ONE_MILLION = BigDecimal.valueOf(1_000_000);

    public record ModelPrice(BigDecimal inputPerMillion, BigDecimal outputPerMillion) {}

    public AiPricingProperties {
        models = models == null ? Map.of() : Map.copyOf(models);
    }

    /**
     * Calcule le coût d'un appel. OpenAI renvoie un nom daté ("gpt-4o-mini-2024-07-18"),
     * on retient donc la clé la plus longue qui est un préfixe du modèle.
     *
     * @return le coût en USD, ou zéro si le modèle n'est pas tarifé
     */
    public BigDecimal costOf(String model, long tokensIn, long tokensOut) {
        return models.entrySet().stream()
                .filter(e -> model.startsWith(e.getKey()))
                .max(Comparator.comparingInt(e -> e.getKey().length()))
                .map(e -> e.getValue().inputPerMillion().multiply(BigDecimal.valueOf(tokensIn))
                        .add(e.getValue().outputPerMillion().multiply(BigDecimal.valueOf(tokensOut)))
                        .divide(ONE_MILLION, 8, RoundingMode.HALF_UP))
                .orElse(BigDecimal.ZERO);
    }
}
