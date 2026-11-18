package com.vignesh.ragbackend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class RagService {

    private final PdfService pdfService;

    public RagService(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    public String askQuestion(String question) {
        return pdfService.askQuestion(question);
    }

    public String askQuestion(MultipartFile file, String question) throws IOException {
        pdfService.readPdf(file);
        return pdfService.askQuestion(question);
    }
}
