package com.vignesh.ragbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagService {

    @Autowired
    private RetrieverService retrieverService;

    @Autowired
    private GeminiService geminiService;

    public String askQuestion(String question) {

        List<String> relevantChunks = retrieverService.retrieveRelevantChunks(question);

        String context = String.join("\n\n", relevantChunks);

        return geminiService.askGemini(context, question);
    }
}