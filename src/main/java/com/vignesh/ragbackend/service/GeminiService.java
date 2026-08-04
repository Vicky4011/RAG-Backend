package com.vignesh.ragbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String askGemini(String context, String question) {

        String prompt = """
                You are a helpful AI assistant.

                Answer the user's question ONLY using the context below.

                If the answer is not present in the context, reply:
                "Answer not found in the PDF."

                Context:
                %s

                Question:
                %s
                """.formatted(context, question);

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        String url =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key="
                        + apiKey;

        ResponseEntity<Map> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        request,
                        Map.class
                );

        Map candidate =
                (Map) ((List) response.getBody().get("candidates")).get(0);

        Map content =
                (Map) candidate.get("content");

        Map part =
                (Map) ((List) content.get("parts")).get(0);

        return part.get("text").toString();
    }
}