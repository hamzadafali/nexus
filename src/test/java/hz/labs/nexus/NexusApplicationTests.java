package hz.labs.nexus;

import hz.labs.nexus.api.LlmResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.ResourceAccessException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NexusApplicationTests {

    private static final String ASK_BODY = "{\"ask\":\"Pourquoi ce code a-t-il une race condition ?\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatClient chatClient;

    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec responseSpec;

    @BeforeEach
    void mockModelResponse() {
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        responseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.entity(LlmResponse.class))
                .thenReturn(new LlmResponse("Java Concurrency", "Le problème vient d'un accès concurrent.", 3));
    }

    @Test
    void askReturnsStructuredResponseFromMockedModel() throws Exception {
        mockMvc.perform(post("/ask")
                        .contentType("application/json")
                        .content(ASK_BODY))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "skill": "Java Concurrency",
                          "explanation": "Le problème vient d'un accès concurrent.",
                          "level": 3
                        }
                        """));
    }

    @Test
    void askReturnsCleanServiceUnavailableWhenProviderFails() throws Exception {
        when(requestSpec.call()).thenThrow(new TransientAiException("503 - upstream overloaded"));

        mockMvc.perform(post("/ask")
                        .contentType("application/json")
                        .content(ASK_BODY))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "5"))
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.title").value("AI provider unavailable"))
                .andExpect(jsonPath("$.detail").exists())
                // le detail technique du provider ne doit jamais fuiter vers le client
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("upstream overloaded"))));
    }

    @Test
    void askReturnsServiceUnavailableWhenProviderCallTimesOut() throws Exception {
        when(responseSpec.entity(LlmResponse.class))
                .thenThrow(new ResourceAccessException("Read timed out"));

        mockMvc.perform(post("/ask")
                        .contentType("application/json")
                        .content(ASK_BODY))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("AI provider unavailable"));
    }

}
