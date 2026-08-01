package com.vignesh.ragbackend.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunkService {

    private static final int MAX_CHUNK_SIZE = 1000;

    public List<String> splitIntoChunks(String text) {

        List<String> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        String[] paragraphs = text.split("\\n\\s*\\n");

        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {

            if (currentChunk.length() + paragraph.length() < MAX_CHUNK_SIZE) {

                currentChunk.append(paragraph).append("\n\n");

            } else {

                chunks.add(currentChunk.toString().trim());

                currentChunk = new StringBuilder();

                currentChunk.append(paragraph).append("\n\n");
            }
        }

        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }
}