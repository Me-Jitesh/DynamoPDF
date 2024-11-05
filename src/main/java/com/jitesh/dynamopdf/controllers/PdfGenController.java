package com.jitesh.dynamopdf.controllers;

import com.jitesh.dynamopdf.models.InvoiceRequest;
import com.jitesh.dynamopdf.services.PdfGenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/api/v1/pdf")
public class PdfGenController {

    @Autowired
    private PdfGenService pdfService;

    @PostMapping("/generate")
    public ResponseEntity<String> generatePdf(@RequestBody InvoiceRequest request) {
        try {
            String filePath = pdfService.generateOrRetrievePdf(request);
            return ResponseEntity.ok("PDF Generated Successfully : " + filePath);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate PDF : " + e.getMessage());
        }
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadPdf(@RequestBody InvoiceRequest request) {
        try {
            String filePath = pdfService.generateOrRetrievePdf(request); // Will also generate if not exist
            File pdfFile = new File(filePath);

            if (!pdfFile.exists()) {
                System.out.println("PDF Doesn't Exists !");
                return ResponseEntity.notFound().build(); // Fallback for Something went wrong with data
            }

            Resource resource = new FileSystemResource(pdfFile);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + pdfFile.getName());

            System.out.println("PDF Downloaded Successfully !");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}
