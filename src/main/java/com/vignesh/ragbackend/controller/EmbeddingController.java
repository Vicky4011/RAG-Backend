package com.vignesh.ragbackend.controller;

import com.vignesh.ragbackend.service.EmbeddingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    public EmbeddingController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @GetMapping("/embedding")
    public List<Double> embedding() {

        return embeddingService.generateEmbedding(
                "Java is a programming language."
        );
    }
}