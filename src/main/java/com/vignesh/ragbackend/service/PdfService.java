package com.vignesh.ragbackend.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Service
public class PdfService {

    private String pdfText = "";

    @Autowired
    private GeminiService geminiService;

    public String readPdf(MultipartFile file) throws IOException {

        PDDocument document = Loader.loadPDF(file.getBytes());

        PDFTextStripper stripper = new PDFTextStripper();

        pdfText = stripper.getText(document);

        document.close();

        return pdfText;
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