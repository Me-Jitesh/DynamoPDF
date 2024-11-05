package com.jitesh.dynamopdf.services.impl;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.jitesh.dynamopdf.models.InvoiceRequest;
import com.jitesh.dynamopdf.models.Item;
import com.jitesh.dynamopdf.services.PdfGenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class PdfGenServiceImpl implements PdfGenService {

    @Value("${pdf.storage.dir}")
    private String STORAGE_DIR;

    public String generateOrRetrievePdf(InvoiceRequest request) throws Exception {
        String hash = generateHash(request);
        String filePath = STORAGE_DIR + hash + ".pdf";

        if (Files.exists(Paths.get(filePath))) {
            System.out.println("Already Generated At : " + filePath);
            return filePath;
        }

        createPdf(request, filePath);
        System.out.println("PDF Generated At : " + filePath);
        return filePath;
    }

    private String generateHash(InvoiceRequest request) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(request.toString().getBytes());
        return Base64.getEncoder().encodeToString(md.digest());
    }

    private void createPdf(InvoiceRequest request, String filePath) throws Exception {
        Files.createDirectories(Paths.get(STORAGE_DIR));

        try (PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            document.add(new Paragraph("Invoice").setFont(font).setFontSize(18));

            document.add(new Paragraph("Seller: " + request.getSeller())
                    .setFont(font).setFontSize(12));
            document.add(new Paragraph("Seller GSTIN: " + request.getSellerGstin())
                    .setFont(font).setFontSize(12));
            document.add(new Paragraph("Seller Address: " + request.getSellerAddress())
                    .setFont(font).setFontSize(12));
            document.add(new Paragraph("Buyer: " + request.getBuyer())
                    .setFont(font).setFontSize(12));
            document.add(new Paragraph("Buyer GSTIN: " + request.getBuyerGstin())
                    .setFont(font).setFontSize(12));
            document.add(new Paragraph("Buyer Address: " + request.getBuyerAddress())
                    .setFont(font).setFontSize(12));

            document.add(new Paragraph("\nItems:").setFont(font).setFontSize(14));

            Table table = new Table(new float[]{4, 2, 2, 2});
            table.addHeaderCell(new Cell().add(new Paragraph("Name").setFont(font)));
            table.addHeaderCell(new Cell().add(new Paragraph("Quantity").setFont(font)));
            table.addHeaderCell(new Cell().add(new Paragraph("Rate").setFont(font)));
            table.addHeaderCell(new Cell().add(new Paragraph("Amount").setFont(font)));

            for (Item item : request.getItems()) {
                table.addCell(new Cell().add(new Paragraph(item.getName()).setFont(font)));
                table.addCell(new Cell().add(new Paragraph(item.getQuantity()).setFont(font)));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(item.getRate())).setFont(font)));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(item.getAmount())).setFont(font)));
            }
            document.add(table);
        }
    }
}
