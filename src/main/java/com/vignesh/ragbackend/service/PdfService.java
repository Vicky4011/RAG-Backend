package com.vignesh.ragbackend.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PdfService {

    private String pdfText = "";

    public String readPdf(MultipartFile file) throws IOException {

        PDDocument document = Loader.loadPDF(file.getBytes());

        PDFTextStripper stripper = new PDFTextStripper();

        pdfText = stripper.getText(document);

        document.close();

        return pdfText;
    }

    public String getPdfText() {
        return pdfText;
    }

}