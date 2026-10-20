package hz.labs.nexus.api;


import hz.labs.nexus.application.AiService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class AskController {

    final private AiService aiService;

    public AskController(AiService aiService) {
        this.aiService = aiService;
    }


    @PostMapping("/ask")
    public AskResponse responseLlm(@RequestBody AskRequest ask) {

        Objects.requireNonNull(ask, "ask must not be null");

        return aiService.analyse(ask.ask());
    }

}
