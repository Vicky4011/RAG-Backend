package com.vignesh.ragbackend.controller;

import com.vignesh.ragbackend.service.PdfService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class UploadController {

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            PdfService pdfService=new PdfService();

            String text = pdfService.readPdf(file);
            System.out.println(text);

            return ResponseEntity.ok("PDF Stored Successfully");

        } catch (IOException e) {

            return ResponseEntity.badRequest().body("Error Reading PDF");

        }
    }
    }