package com.algonest.AlgoNest_Backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Value("${groq.api.key}")   // ✅ Must match your application.properties
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/coach")
    public ResponseEntity<String> getCoaching(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        if (prompt == null || prompt.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"Missing prompt\"}");
        }

        String trimmedKey = apiKey.trim();
        if (trimmedKey.isEmpty() || trimmedKey.length() < 10) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Groq API key is not configured\"}");
        }

        System.out.println("🔑 Groq Key (first 10 chars): " + trimmedKey.substring(0, Math.min(10, trimmedKey.length())));

        String url = "https://api.groq.com/openai/v1/chat/completions";

        // Escape the prompt for JSON
        String escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", " ");

        String payload = String.format("""
            {
                "model": "openai/gpt-oss-120b",
                "messages": [
                    {
                        "role": "user",
                        "content": "%s"
                    }
                ],
                "temperature": 0.7,
                "max_completion_tokens": 2048,
                "top_p": 1,
                "reasoning_effort": "medium",
                "stream": false,
                "stop": null
            }
            """, escapedPrompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(trimmedKey);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            System.out.println("✅ Groq Response Status: " + response.getStatusCode());
            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            System.err.println("❌ Groq Error: " + errorBody);
            // Return a 500 error with the actual error message, not 401
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + errorBody.replace("\"", "'") + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}