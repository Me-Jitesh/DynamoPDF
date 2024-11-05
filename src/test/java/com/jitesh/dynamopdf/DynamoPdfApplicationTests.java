package com.jitesh.dynamopdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jitesh.dynamopdf.controllers.PdfGenController;
import com.jitesh.dynamopdf.models.InvoiceRequest;
import com.jitesh.dynamopdf.models.Item;
import com.jitesh.dynamopdf.services.PdfGenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PdfGenController.class)
class DynamoPdfApplicationTests {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PdfGenService pdfService;
    private InvoiceRequest sampleRequest;

    @BeforeEach
    public void setUp() {
        sampleRequest = new InvoiceRequest();
        sampleRequest.setSeller("XYZ Pvt. Ltd.");
        sampleRequest.setSellerGstin("29AABBCCDD121ZD");
        sampleRequest.setSellerAddress("New Delhi, India");
        sampleRequest.setBuyer("Vedant Computers");
        sampleRequest.setBuyerGstin("29AABBCCDD131ZD");
        sampleRequest.setBuyerAddress("New Delhi, India");

        Item item = new Item("Product 1", "12 Nos", 123.00, 1476.00);
        List<Item> items = new java.util.ArrayList<>();
        items.add(item);
        sampleRequest.setItems(items);
    }

    @Test
    public void testGenSuccess() throws Exception {
        String filePath = "docs/sample.pdf";
        when(pdfService.generateOrRetrievePdf(sampleRequest)).thenReturn(filePath);

        mockMvc.perform(post("/api/v1/pdf/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("PDF Generated Successfully : " + filePath));
    }

    @Test
    public void testDownloadSuccess() throws Exception {
        String filePath = "docs/sample.pdf";
        File file = new File(filePath);
        file.createNewFile();
        when(pdfService.generateOrRetrievePdf(sampleRequest)).thenReturn(filePath);

        mockMvc.perform(get("/api/v1/pdf/download")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=" + file.getName()))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        // Clean up created file
        file.delete();
    }

    @Test
    public void testPdfNotFound() throws Exception {
        when(pdfService.generateOrRetrievePdf(sampleRequest)).thenThrow(new RuntimeException("File not found"));

        mockMvc.perform(get("/api/v1/pdf/download")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isInternalServerError());
    }
}
