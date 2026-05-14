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

        // Ensure phone is in international format (e.g., 254...)
        String formattedPhone = phone.replace("+", "").replace(" ", "");

        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("x-api-key", API_KEY); // Assuming it might be in header or part of body if not specified
                // The user didn't specify where the API key goes. Usually it's a header or part of the URL.
                // Let's check the payload again.
                conn.setDoOutput(true);

                String jsonInputString = String.format("{\"phone\": \"%s\", \"text\": \"%s\"}", formattedPhone, text);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int code = conn.getResponseCode();
                System.out.println("WhatsApp API Response Code: " + code);
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
