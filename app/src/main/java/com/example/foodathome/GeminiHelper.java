package com.example.foodathome;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.genai.Client;

public class GeminiHelper {
    Client client;
    boolean succesfulResponse;

    public GeminiHelper(Context context) {
        // Accessing the key from resources (resValue in build.gradle)
        String apiKey = context.getString(R.string.gemini_api_key);
        client = Client.builder().apiKey(apiKey).build();
    }

    public boolean ResponseStatus() {
        return this.succesfulResponse;
    }


    public void askGemini(String text, Callback callback) {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            String result = "";
            try {
                result = getResponse(text);
                this.succesfulResponse = result != null && !result.isEmpty() && !result.contains("com.google.genai.errors.ClientException");
                Log.i("myComments", "got response");

            } catch (Exception e) {
                Log.i("myComments", e.toString());
            }

            // callback to main handler (calling thread)
            String finalResult = result;
            mainHandler.post(() -> {
                callback.onDone(finalResult);
            });

        }).start();
    }

    public String getResponse(String text) {
        String response = "";
        try {
            response = client.models.generateContent(
                    "gemini-2.0-flash",
                    text,
                    null).text();
        } catch (Exception e) {
            Log.e("GeminiHelper", "Error generating content", e);
            response = "error:" + e.toString();
        }
        return response;
    }
}
