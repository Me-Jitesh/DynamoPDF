package com.jitesh.dynamopdf.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/pdf/web")
public class UIController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${pdf-gen.api.url}")
    String API_URL;

    @GetMapping("/ui")
    private String exposeUI() {
        return "UI";
    }

    @PostMapping("/submit")
    private String submittedData(@RequestParam String data, Model model) {

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Prepare the request
        HttpEntity<String> payload = new HttpEntity<>(data, headers);

        callAPI(payload, model);

        return "UI";
    }

    private void callAPI(HttpEntity<String> payload, Model model) {
        try {

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(API_URL, payload, String.class);
            model.addAttribute("msg", "API Response: " + responseEntity.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("msg", "Error calling API: " + e.getMessage());
        }
    }
}
