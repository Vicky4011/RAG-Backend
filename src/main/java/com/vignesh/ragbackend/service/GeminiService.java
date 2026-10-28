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

        long totalStartTime = System.currentTimeMillis();

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
                        + model
                        + ":generateContent";

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {

            long requestStartTime = System.currentTimeMillis();

            System.out.println(
                    "Sending Gemini request. Attempt "
                            + attempt + "/" + MAX_ATTEMPTS
            );

            try {
                ResponseEntity<Map> response = restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        request,
                        Map.class
                );

                long requestDuration =
                        System.currentTimeMillis() - requestStartTime;

                System.out.println(
                        "Gemini response received in "
                                + requestDuration + " ms"
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

                for (Object item : parts) {
                    if (item instanceof Map part
                            && part.get("text") instanceof String text
                            && !text.isBlank()) {

                        System.out.println(
                                "Gemini request completed successfully in "
                                        + (System.currentTimeMillis()
                                        - totalStartTime)
                                        + " ms total."
                        );

                        return text;
                    }
                }

                throw new IllegalStateException(
                        "Gemini returned no text in its response."
                );

            } catch (HttpStatusCodeException e) {

                int status = e.getStatusCode().value();

                boolean retryable =
                        status == 429
                                || status == 500
                                || status == 502
                                || status == 503
                                || status == 504;

                System.err.println(
                        "Gemini returned HTTP "
                                + status
                                + " on attempt "
                                + attempt
                                + "."
                );

                if (!retryable || attempt == MAX_ATTEMPTS) {
                    System.err.println(
                            "Gemini request failed. Total elapsed time: "
                                    + (System.currentTimeMillis()
                                    - totalStartTime)
                                    + " ms."
                    );

                    throw new IllegalStateException(
                            "Gemini API request failed with HTTP "
                                    + status
                                    + ". Please try again shortly.",
                            e
                    );
                }

                long delay =
                        INITIAL_DELAY_MS * (1L << (attempt - 1));

                System.err.println(
                        "Retrying Gemini in " + delay + " ms."
                );

                sleepBeforeRetry(delay);

            } catch (org.springframework.web.client.ResourceAccessException e) {

                System.err.println(
                        "Gemini connection or response timed out on attempt "
                                + attempt + "."
                );

                if (attempt == MAX_ATTEMPTS) {
                    throw new IllegalStateException(
                            "Gemini did not respond in time. Please try again.",
                            e
                    );
                }

                long delay =
                        INITIAL_DELAY_MS * (1L << (attempt - 1));

                sleepBeforeRetry(delay);
            }
        }

        throw new IllegalStateException(
                "Unable to get a response from Gemini."
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
