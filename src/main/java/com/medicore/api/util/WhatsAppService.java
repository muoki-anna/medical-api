package com.medicore.api.util;

import org.springframework.stereotype.Service;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class WhatsAppService {

    private static final String API_URL = "https://whatsapp.pacewisp.co.ke/send/primary";
    private static final String API_KEY = "3f9d3160-4769-44b5-a0e9-7d4e36512aec";

    public void sendMessage(String phone, String text) {
        if (phone == null || phone.isEmpty()) return;

        // Normalize phone: remove non-digits
        String formattedPhone = phone.replaceAll("[^0-9]", "");
        
        // Convert local Kenyan format (07...) to international (2547...)
        if (formattedPhone.startsWith("0")) {
            formattedPhone = "254" + formattedPhone.substring(1);
        }

        final String finalPhone = formattedPhone;
        new Thread(() -> {
            try {
                System.out.println("Attempting to send WhatsApp to: " + finalPhone);
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("x-api-key", API_KEY);
                conn.setDoOutput(true);

                // Manual JSON building to avoid external dependencies, with basic escaping
                String escapedText = text.replace("\"", "\\\"").replace("\n", "\\n");
                String jsonInputString = "{\"phone\": \"" + finalPhone + "\", \"text\": \"" + escapedText + "\"}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int code = conn.getResponseCode();
                System.out.println("WhatsApp API Response Code: " + code);
                
                if (code >= 400) {
                    try (java.util.Scanner s = new java.util.Scanner(conn.getErrorStream())) {
                        String error = s.useDelimiter("\\A").hasNext() ? s.next() : "";
                        System.err.println("WhatsApp API Error Detail: " + error);
                    }
                }
                
                conn.disconnect();
            } catch (Exception e) {
                System.err.println("WhatsApp Dispatch Failed: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}
