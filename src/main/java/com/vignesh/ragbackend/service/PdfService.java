package com.vignesh.ragbackend.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Service
public class PdfService {

    private String pdfText = "";

    private List<String> pdfChunks = new ArrayList<>();

    @Autowired
    private GeminiService geminiService;
    @Autowired
    private TextChunkService textChunkService;

    public String readPdf(MultipartFile file) throws IOException {

        PDDocument document = Loader.loadPDF(file.getBytes());

        PDFTextStripper stripper = new PDFTextStripper();

        pdfText = stripper.getText(document);

        pdfChunks = textChunkService.splitIntoChunks(pdfText);

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