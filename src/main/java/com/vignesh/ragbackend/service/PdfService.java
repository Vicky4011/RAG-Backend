package com.vignesh.ragbackend.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfService {

    private String pdfText = "";
    private List<String> pdfChunks = new ArrayList<>();
    private List<List<Double>> embeddings = new ArrayList<>();

    @Autowired
    private GeminiService geminiService;
    @Autowired
    private TextChunkService textChunkService;
    @Autowired
    private EmbeddingService embeddingService;

    public String readPdf(MultipartFile file) throws IOException {

        PDDocument document = Loader.loadPDF(file.getBytes());

        PDFTextStripper stripper = new PDFTextStripper();

        pdfText = stripper.getText(document);

        List<String> rawChunks = textChunkService.splitIntoChunks(pdfText);

        pdfChunks = new ArrayList<>();
        embeddings = new ArrayList<>();

        for (String chunk : rawChunks) {

            if (chunk == null || chunk.isBlank()) {
                System.out.println("Skipping blank chunk.");
                continue;
            }

            List<Double> embedding = embeddingService.generateEmbedding(chunk);

            System.out.println("Chunk Embedding Size = " + embedding.size());

            if (embedding.isEmpty()) {
                System.out.println("ERROR: Empty embedding generated! Skipping this chunk.");
                continue;
            }

            pdfChunks.add(chunk);
            embeddings.add(embedding);
        }

        System.out.println("==================================");
        System.out.println("TOTAL CHUNKS : " + pdfChunks.size());
        System.out.println("==================================");

        for (int i = 0; i < pdfChunks.size(); i++) {

            System.out.println("Chunk " + (i + 1));

            System.out.println(pdfChunks.get(i));

            System.out.println("--------------------------------");
        }

        document.close();

        return pdfText;
    }

    public List<String> getPdfChunks() {
        return pdfChunks;
    }

    public List<List<Double>> getEmbeddings() {
        return embeddings;
    }

    public String askQuestion(String question) {

        if (pdfText == null || pdfText.isBlank()) {
            return "No PDF uploaded.";
        }

        try {
            return geminiService.askGemini(pdfText, question);
        }
        catch (Exception e) {
            e.printStackTrace();
            return "Error while communicating with Gemini.";
        }
    }

}