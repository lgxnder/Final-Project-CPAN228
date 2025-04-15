package com.humber.sleepPlanRepeat.services;
import com.humber.sleepPlanRepeat.models.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FollowUpService {

    @Autowired
    private final GeminiService geminiService;

    // Constructor for GeminiService
    public FollowUpService(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    // Method to generate a follow-up message
    public String generateFollowUpMessage(Event event, String outcome) {
        String prompt = "Generate a follow-up message for the event titled '" + event.getTitle() +
                "' with the following outcome: " + outcome;
        return geminiService.getPrompt(prompt);
    }
}