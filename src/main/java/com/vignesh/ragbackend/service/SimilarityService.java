package com.vignesh.ragbackend.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimilarityService {

    public double cosineSimilarity(List<Double> vector1,
                                   List<Double> vector2) {

        if (vector1 == null || vector2 == null
                || vector1.isEmpty() || vector2.isEmpty()
                || vector1.size() != vector2.size()) {
            System.out.println("WARNING: Skipping similarity check - mismatched or empty vectors ("
                    + (vector1 == null ? "null" : vector1.size()) + " vs "
                    + (vector2 == null ? "null" : vector2.size()) + ")");
            return -1.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.size(); i++) {

            dotProduct += vector1.get(i) * vector2.get(i);

            norm1 += Math.pow(vector1.get(i), 2);

            norm2 += Math.pow(vector2.get(i), 2);
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}