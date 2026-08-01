package com.vignesh.ragbackend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Double> generateEmbedding(String text) {

        String url = "http://localhost:11434/api/embeddings";

        Map<String, Object> body = Map.of(
                "model", "nomic-embed-text",
                "prompt", text
        );

        Map response = restTemplate.postForObject(
                url,
                body,
                Map.class
        );

        return (List<Double>) response.get("embedding");
    }
}