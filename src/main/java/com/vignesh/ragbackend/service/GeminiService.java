package com.vignesh.ragbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final int MAX_ATTEMPTS = 4;
    private static final long INITIAL_DELAY_MS = 1000;

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
        headers.set("x-goog-api-key", apiKey);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        String url =
                "https://generativelanguage.googleapis.com/v1beta/models/"
                        + model + ":generateContent";

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {

            try {
                ResponseEntity<Map> response = restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        request,
                        Map.class
                );

                Map responseBody = response.getBody();

                if (responseBody == null) {
                    throw new IllegalStateException(
                            "Gemini returned an empty response."
                    );
                }

                List candidates =
                        (List) responseBody.get("candidates");

                if (candidates == null || candidates.isEmpty()) {
                    throw new IllegalStateException(
                            "Gemini returned no answer. Check the API response."
                    );
                }

                Map candidate = (Map) candidates.get(0);
                Map content = (Map) candidate.get("content");

                if (content == null) {
                    throw new IllegalStateException(
                            "Gemini response did not contain content."
                    );
                }

                List parts = (List) content.get("parts");

                if (parts == null || parts.isEmpty()) {
                    throw new IllegalStateException(
                            "Gemini response did not contain text."
                    );
                }

                Map part = (Map) parts.get(0);
                Object text = part.get("text");

                if (text == null) {
                    throw new IllegalStateException(
                            "Gemini response did not contain answer text."
                    );
                }

                return text.toString();

            } catch (HttpStatusCodeException e) {

                int status = e.getStatusCode().value();

                boolean retryable =
                        status == 429 ||
                                status == 500 ||
                                status == 502 ||
                                status == 503 ||
                                status == 504;

                if (!retryable) {
                    throw new IllegalStateException(
                            "Gemini API request failed with HTTP "
                                    + status
                                    + ". Check your API key, model, and request configuration.",
                            e
                    );
                }

                if (attempt == MAX_ATTEMPTS) {
                    throw new IllegalStateException(
                            "Gemini is temporarily unavailable after "
                                    + MAX_ATTEMPTS
                                    + " attempts. Please try again shortly.",
                            e
                    );
                }

                long delay = INITIAL_DELAY_MS * (1L << (attempt - 1));

                System.err.println(
                        "Gemini returned HTTP " + status
                                + ". Retry " + (attempt + 1)
                                + " of " + MAX_ATTEMPTS
                                + " in " + delay + " ms."
                );

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();

                    throw new IllegalStateException(
                            "Gemini retry was interrupted.",
                            interrupted
                    );
                }
            }
        }

        throw new IllegalStateException(
                "Unable to get a response from Gemini."
        );
    }
}
