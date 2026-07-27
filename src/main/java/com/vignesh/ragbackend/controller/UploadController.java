package com.vignesh.ragbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class UploadController {

    @PostMapping("/upload")
    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) {

        System.out.println("Received File : " + file.getOriginalFilename());

        return ResponseEntity.ok("Upload Successful");
    }

}