package com.vignesh.ragbackend.controller;

import com.vignesh.ragbackend.service.PdfService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AskController {

    private final PdfService pdfService;

    public AskController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping("/ask")
    public ResponseEntity<String> askQuestion(
            @RequestParam String question) {

        String answer = pdfService.askQuestion(question);

        return ResponseEntity.ok(answer);

    }
}