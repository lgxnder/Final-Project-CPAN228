package com.humber.sleepPlanRepeat.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Autowired
    private final WebClient geminiWebClient;

    public GeminiService(WebClient geminiWebClient) {
        this.geminiWebClient = geminiWebClient;
    }

    public String getPrompt(String userQuestion) {
        Map<String, Object> requestBody = Map.of("contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", userQuestion)
                        })
                }

        );
        String response = geminiWebClient.post()
                .uri(apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return "";
    }
}
