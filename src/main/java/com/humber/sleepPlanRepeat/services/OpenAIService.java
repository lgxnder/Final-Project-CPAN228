package com.humber.sleepPlanRepeat.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.humber.sleepPlanRepeat.models.Event;
import com.humber.sleepPlanRepeat.models.User;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Use OpenAI to generate calendar events from text descriptions.
@Service
public class OpenAIService {

    // Use OpenAI model to process requests.
    private final OpenAiChatModel chatModel;

    private final ObjectMapper objectMapper;
    // Utilize ObjectMapper to serialize and deserialize JSON to and from an object.
    // https://fasterxml.github.io/jackson-databind/javadoc/2.7/com/fasterxml/jackson/databind/ObjectMapper.html
    // https://www.baeldung.com/jackson-object-mapper-tutorial

    // Specify to OpenAI the format the response should come in.
    private final String eventJsonSchema = """
        {
            "type": "object",
            "properties": {
                "title": { "type": "string" },
                "startTime": { "type": "string", "format": "date-time" },
                "endTime": { "type": "string", "format": "date-time" },
                "description": { "type": "string" }
            },
            "required": ["title", "startTime", "endTime", "description"]
        }
        """;

    // Constructor injection.
    public OpenAIService(OpenAiChatModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        // Initialize ObjectMapper.
        // Using JavaTimeModule, it gives support for LocalDateTime.
        // This is needed for working with Event objects.
    }

    // Generate Event based on User description.
    public Event generateEvent(String textDescription, User user) {
        try {

            // Specify instructions for the AI to follow.
            String instructions =
                    "Create a calendar event based on this description: \"" + textDescription + "\".\n\n" +
                            "Return a JSON object with these fields:\n" +
                            "- title: A clear, concise title for the event\n" +
                            "- startTime: When the event starts (in YYYY-MM-DDTHH:MM:SS format)\n" +
                            "- endTime: When the event ends (in YYYY-MM-DDTHH:MM:SS format)\n" +
                            "- description: A helpful description of the event\n\n" +
                            "Make the timing realistic - most events last between 30 minutes and 2 hours.\n" +
                            "If no specific date/time is mentioned, use the next logical time.";

            // Initialize prompt with instructions and response format.
            Prompt prompt = new Prompt(
                    instructions,
                    OpenAiChatOptions.builder()
                            .responseFormat(new ResponseFormat(ResponseFormat.Type.JSON_SCHEMA, eventJsonSchema))
                            .build()
            );

            // Send prompt to OpenAI and listen for response.
            ChatResponse response = this.chatModel.call(prompt);

            // Receive and parse JSON response.
            String jsonContent = response.getResult().toString();
            JsonNode jsonNode = objectMapper.readTree(jsonContent);

            // Initialize Event object.
            Event event = new Event();
            event.setTitle(jsonNode.get("title").asText());
            event.setDescription(jsonNode.get("description").asText());

            // Parse date.
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

            String startTimeStr = jsonNode.get("startTime").asText();
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, formatter);
            event.setStartTime(startTime);

            String endTimeStr = jsonNode.get("endTime").asText();
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, formatter);
            event.setEndTime(endTime);

            // Set event specific to user and return event.
            event.setUser(user);
            return event;

        } catch (Exception e){
            System.err.println("Error generating event: " + e.getMessage());
            throw new RuntimeException("Could not generate event from your description", e);
        }
    }
}
