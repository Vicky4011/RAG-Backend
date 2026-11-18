package com.vignesh.ragbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.fallback-model:gemini-3.1-flash-lite}")
    private String fallbackModel;

    @Value("${gemini.model:gemini-flash-latest}")
    private String model;

    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_DELAY_MS = 1000;

    private final RestTemplate restTemplate = createRestTemplate();

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        // Maximum time to establish a connection.
        factory.setConnectTimeout(10_000);

        // Maximum time to wait for response data.
        factory.setReadTimeout(30_000);

        return new RestTemplate(factory);
    }

    public String askGemini(String context, String question) {

    String prompt = """
        You are a helpful AI assistant that answers questions
        about an uploaded PDF.

        RULES:
        1. Answer using only the supplied PDF context.
        2. Read all the supplied context before answering.
        3. Combine relevant information from different sections.
        4. Understand spelling and grammar mistakes.
        5. If information is partially available, explain what is missing.
        6. If the answer cannot be found, reply:
           "Answer not found in the PDF."
        7. Never invent facts.

        PDF CONTEXT:
        %s

        USER QUESTION:
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

    List<String> models = List.of(model, fallbackModel);

    for (String selectedModel : models) {

        String url =
                "https://generativelanguage.googleapis.com/v1beta/models/"
                        + selectedModel
                        + ":generateContent";

        System.out.println("Trying Gemini model: " + selectedModel);

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
                        "Gemini returned no answer."
                );
            }

            Map candidate = (Map) candidates.get(0);
            Map content = (Map) candidate.get("content");
            List parts = (List) content.get("parts");

            if (parts != null) {
                for (Object item : parts) {

                    if (item instanceof Map part
                            && part.get("text") instanceof String text
                            && !text.isBlank()) {

                        System.out.println(
                                "Answer generated using: "
                                        + selectedModel
                        );

                        return text;
                    }
                }
            }

            throw new IllegalStateException(
                    "Gemini returned no text."
            );

        } catch (HttpStatusCodeException e) {

            int status = e.getStatusCode().value();

            System.err.println(
                    selectedModel + " failed with HTTP " + status
            );

            if (status == 503 || status == 502 || status == 504) {

                System.err.println(
                        "Switching to backup Gemini model."
                );

                continue;
            }

            throw new IllegalStateException(
                    "Gemini API failed with HTTP "
                            + status + ": " + e.getResponseBodyAsString(),
                    e
            );

        } catch (org.springframework.web.client.ResourceAccessException e) {

            System.err.println(
                    selectedModel + " timed out. Trying next model."
            );
        }
    }

    throw new IllegalStateException(
            "Both Gemini models are currently unavailable. "
                    + "Please try again in a few minutes."
    );
}

    private void sleepBeforeRetry(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Gemini retry was interrupted.",
                    e
            );
        }
    }
}
