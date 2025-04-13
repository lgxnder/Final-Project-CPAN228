package com.humber.sleepPlanRepeat.services;
import com.humber.sleepPlanRepeat.models.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    //injecting api key
    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl; // Add this property

    @Autowired
    private final WebClient geminiWebClient;

    public GeminiService(WebClient geminiWebClient) {
        this.geminiWebClient = geminiWebClient;
    }
    //User sends question to gemini in order for a response
    public String getPrompt(String userQuestion) {
        Map<String, Object> requestBody = Map.of("contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", userQuestion)
                        })
                }
        );


        //Sends request to Gemini with post method
        String response = geminiWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/models/gemini-1.5-flash:generateContent")
                        .queryParam("key", apiKey)
                        .build())
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return response;
    }



    public String getPersonalizedMessage(String username, List<Event> events) {
        StringBuilder eventSummary = new StringBuilder();

        if (events == null || events.isEmpty()) {
            eventSummary.append("You have no upcoming events.");
        } else {
            eventSummary.append("Here are the user's upcoming events:\n");
            for (Event event : events) {
                eventSummary.append("- ")
                        .append(event.getTitle())
                        .append(" on ")
                        .append(event.getStartTime().toLocalDate())
                        .append(" at ")
                        .append(event.getStartTime().toLocalTime())
                        .append("\n");
            }
        }

        String question = "Give a motivational and friendly message for user '" + username +
                "'. " + eventSummary.toString() +
                " Encourage them to stay focused and organized.";

        return getPrompt(question);
    }
}
