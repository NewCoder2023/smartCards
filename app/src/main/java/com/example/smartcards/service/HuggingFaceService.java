package com.example.smartcards.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.*;
import android.os.ParcelFileDescriptor;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import java.io.FileInputStream;

public class HuggingFaceService {
    private static final String OCR_API_URL = "https://api-inference.huggingface.co/models/microsoft/trocr-base-handwritten";
    private static final String QG_API_URL = "https://api-inference.huggingface.co/models/facebook/bart-large-mnli";
    private static final String API_KEY = "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"; // Replace with your actual API key

    private final Context context;
    private final OkHttpClient client;

    public HuggingFaceService(Context context) {
        this.context = context;
        this.client = new OkHttpClient.Builder()
                .build();
    }

    public List<FlashcardPair> processImage(Uri imageUri) throws IOException {
        // Convert image to base64
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
        String base64Image = bitmapToBase64(bitmap);

        // First, use OCR to extract text
        String extractedText = performOCR(base64Image);

        // Then generate questions from the extracted text
        return generateQuestions(extractedText);
    }

    private String performOCR(String base64Image) throws IOException {
        // Prepare request body for TrOCR
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("inputs", base64Image);
        } catch (Exception e) {
            throw new IOException("Error creating OCR request body", e);
        }

        // Make OCR request
        Request request = new Request.Builder()
                .url(OCR_API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        requestBody.toString(),
                        MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("OCR failed: " + response.code());
            }

            String responseBody = response.body().string();
            // TrOCR returns array with single text result
            JSONArray jsonArray = new JSONArray(responseBody);
            return jsonArray.getJSONObject(0).getString("generated_text");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private List<FlashcardPair> generateQuestions(String text) throws IOException {
        // Prepare text for BART model
        // We'll create a prompt that helps generate Q&A pairs
        String prompt = String.format(
                "Generate questions and answers from this text: %s\n\n" +
                        "Format: Question: [question] Answer: [answer]", text);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("inputs", prompt);
            requestBody.put("parameters", new JSONObject()
                    .put("max_length", 512)
                    .put("num_return_sequences", 5) // Generate 5 Q&A pairs
                    .put("temperature", 0.7) // Add some randomness but keep it focused
                    .put("top_p", 0.95)); // Nucleus sampling for better quality
        } catch (Exception e) {
            throw new IOException("Error creating question generation request body", e);
        }

        Request request = new Request.Builder()
                .url(QG_API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        requestBody.toString(),
                        MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Question generation failed: " + response.code());
            }

            return parseQuestionAnswerPairs(response.body().string());
        }
    }

    private List<FlashcardPair> parseQuestionAnswerPairs(String responseData) throws IOException {
        List<FlashcardPair> flashcards = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(responseData);
            String generatedText = jsonArray.getJSONObject(0).getString("generated_text");

            // Split the generated text into lines
            String[] lines = generatedText.split("\n");
            String currentQuestion = null;

            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("Question:")) {
                    currentQuestion = line.substring("Question:".length()).trim();
                } else if (line.startsWith("Answer:") && currentQuestion != null) {
                    String answer = line.substring("Answer:".length()).trim();
                    flashcards.add(new FlashcardPair(currentQuestion, answer));
                    currentQuestion = null;
                }
            }
        } catch (Exception e) {
            throw new IOException("Error parsing generated questions and answers", e);
        }
        return flashcards;
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public List<FlashcardPair> processPDF(Uri pdfUri) throws IOException {
        // Extract text from PDF
        String extractedText = extractTextFromPDF(pdfUri);

        // Use the existing question generation logic
        return generateQuestions(extractedText);
    }

    private String extractTextFromPDF(Uri pdfUri) throws IOException {
        StringBuilder textBuilder = new StringBuilder();

        try (ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(pdfUri, "r")) {
            if (fileDescriptor != null) {
                // Create an input stream from the file descriptor
                try (FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor())) {
                    PdfReader reader = new PdfReader(inputStream);
                    int pages = reader.getNumberOfPages();

                    for (int i = 1; i <= pages; i++) {
                        textBuilder.append(PdfTextExtractor.getTextFromPage(reader, i));
                        textBuilder.append("\n");
                    }
                    reader.close();
                }
            }
        } catch (IOException e) {
            throw new IOException("Error reading PDF file: " + e.getMessage(), e);
        }

        return textBuilder.toString();
    }

    public static class FlashcardPair {
        public final String question;
        public final String answer;

        public FlashcardPair(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
    }
}
