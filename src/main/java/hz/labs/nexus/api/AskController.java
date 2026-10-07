package hz.labs.nexus.api;


import hz.labs.nexus.application.AiService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AskController {

    final private AiService aiService;

    public AskController(AiService aiService) {
        this.aiService = aiService;
    }


    @PostMapping("/ask")
    public LlmResponse responseLlm(@RequestBody AskRequest ask) {
        return aiService.analyse(ask.ask());
    }

}
