package hz.labs.nexus.configuration;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("Tu es un assistant utile.")
                .build();
    }

    // Les deux modeles sont auto-configures par les starters (cle, modele : voir application.yaml).
    // @Primary : c'est lui qu'utilise le ChatClient.Builder auto-configure.
    @Bean("primaryModel")
    @Primary
    ChatModel primaryModel(OpenAiChatModel openAiChatModel) {
        return openAiChatModel;
    }

    @Bean("fallbackModel")
    ChatModel fallbackModel(AnthropicChatModel anthropicChatModel) {
        return anthropicChatModel;
    }
}
