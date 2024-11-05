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
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
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

            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            Table infoTable = new Table(new float[]{1, 1});
            infoTable.setWidth(UnitValue.createPercentValue(100));

            Cell sellerCell = new Cell().add(new Paragraph("Seller:\n" + request.getSeller() + "\n" +
                            request.getSellerAddress() + "\nGSTIN: " + request.getSellerGstin()))
                    .setFont(font)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setPadding(10);

            Cell buyerCell = new Cell().add(new Paragraph("Buyer:\n" + request.getBuyer() + "\n" +
                            request.getBuyerAddress() + "\nGSTIN: " + request.getBuyerGstin()))
                    .setFont(font)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setPadding(10);

            infoTable.addCell(sellerCell);
            infoTable.addCell(buyerCell);
            document.add(infoTable);

            // Add Item Table Header
            Table itemTable = new Table(new float[]{4, 2, 2, 2});
            itemTable.setWidth(UnitValue.createPercentValue(100));

            itemTable.addHeaderCell(new Cell().add(new Paragraph("Item")).setFont(boldFont).setTextAlignment(TextAlignment.CENTER));
            itemTable.addHeaderCell(new Cell().add(new Paragraph("Quantity")).setFont(boldFont).setTextAlignment(TextAlignment.CENTER));
            itemTable.addHeaderCell(new Cell().add(new Paragraph("Rate")).setFont(boldFont).setTextAlignment(TextAlignment.CENTER));
            itemTable.addHeaderCell(new Cell().add(new Paragraph("Amount")).setFont(boldFont).setTextAlignment(TextAlignment.CENTER));

            // Add Items to the Table
            for (Item item : request.getItems()) {
                itemTable.addCell(new Cell().add(new Paragraph(item.getName())).setFont(font).setTextAlignment(TextAlignment.CENTER));
                itemTable.addCell(new Cell().add(new Paragraph(item.getQuantity())).setFont(font).setTextAlignment(TextAlignment.CENTER));
                itemTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.getRate()))).setFont(font).setTextAlignment(TextAlignment.CENTER));
                itemTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.getAmount()))).setFont(font).setTextAlignment(TextAlignment.CENTER));
            }

            document.add(itemTable);

        }
    }
}
