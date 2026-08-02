package com.vignesh.ragbackend.service;
import com.vignesh.ragbackend.model.ChunkScore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RetrieverService {

    @Autowired
    private PdfService pdfService;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private SimilarityService similarityService;

    public List<String> retrieveRelevantChunks(String question) {

        List<Double> questionEmbedding =
                embeddingService.generateEmbedding(question);

        System.out.println("Question" + questionEmbedding.size());

        List<String> chunks = pdfService.getPdfChunks();

        List<List<Double>> embeddings =
                pdfService.getEmbeddings();

        List<ChunkScore> scores = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {

            double similarity =
                    similarityService.cosineSimilarity(
                            questionEmbedding,
                            embeddings.get(i)
                    );

            scores.add(new ChunkScore(
                    chunks.get(i),
                    similarity
            ));
        }

        scores.sort(
                Comparator.comparingDouble(ChunkScore::getScore)
                        .reversed()
        );

        List<String> topChunks = new ArrayList<>();

        int limit = Math.min(3, scores.size());

        for (int i = 0; i < limit; i++) {

            topChunks.add(scores.get(i).getChunk());

            System.out.println(
                    "Similarity : " + scores.get(i).getScore()
            );
        }
        System.out.println("Chunks: " + chunks.size());
        System.out.println("Embeddings: " + embeddings.size());
        System.out.println("Question Embedding: " + questionEmbedding.size());

        return topChunks;
    }
}