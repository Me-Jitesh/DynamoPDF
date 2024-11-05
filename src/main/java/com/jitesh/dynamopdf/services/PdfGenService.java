package com.jitesh.dynamopdf.services;

import com.jitesh.dynamopdf.models.InvoiceRequest;

public interface PdfGenService {
    String generateOrRetrievePdf(InvoiceRequest request) throws Exception;
}
