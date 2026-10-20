package hz.labs.nexus.application;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class AiUsageRecorder {

    private static final Logger log = LoggerFactory.getLogger(AiUsageRecorder.class);
    private final MeterRegistry registry;
    private final AiPricingProperties pricing;

    public void record(ChatResponse response, String promptVersion, Duration latency) {
        Usage usage = response.getMetadata().getUsage();
        String model = response.getMetadata().getModel();
        if (model == null || model.isBlank()) {
            model = "unknown";
        }
        // L'usage et les compteurs de tokens peuvent être absents selon le provider
        long in  = usage != null && usage.getPromptTokens() != null ? usage.getPromptTokens() : 0;
        long out = usage != null && usage.getCompletionTokens() != null ? usage.getCompletionTokens() : 0;
        BigDecimal cost = pricing.costOf(model, in, out);

        // Métrique : agrégée, pour les dashboards
        Counter.builder("nexus.ai.cost")
                .baseUnit("usd")
                .tag("model", model)
                .tag("prompt.version", promptVersion)
                .register(registry)
                .increment(cost.doubleValue());

        // Log structuré : une ligne par appel, pour l'investigation
        log.atInfo()
                .addKeyValue("model", model)
                .addKeyValue("promptVersion", promptVersion)
                .addKeyValue("tokensIn", in)
                .addKeyValue("tokensOut", out)
                .addKeyValue("latencyMs", latency.toMillis())
                .addKeyValue("costUsd", cost)
                .log("ai.call");
    }
}
