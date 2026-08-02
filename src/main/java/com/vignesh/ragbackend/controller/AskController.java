package com.vignesh.ragbackend.controller;

import com.vignesh.ragbackend.service.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AskController {

    @Autowired
    private RagService ragService;

    @GetMapping("/ask")
    public String askQuestion(@RequestParam String question) {

        return ragService.askQuestion(question);
    }
}