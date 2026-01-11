package com.example.foodathome;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class GeminiHelper
{
    Client client;
    boolean succesfulResponse;

    public GeminiHelper()
    {
        client = Client.builder().apiKey(BuildConfig.GEMINI_API_KEY).build();
    }

    public boolean ResponseStatus() { return this.succesfulResponse; }

    public void askGemini(String text, Callback callback)
    {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            String result = "";
            try {
                GenerateContentResponse response =
                        client.models.generateContent(
                                "gemini-2.5-flash",
                                text,
                                null);

                result = response.text();
                this.succesfulResponse = result != null && !result.isEmpty();
                Log.i("myComments", "got response");

            } catch (Exception e) {
                Log.i("myComments", e.toString());
            }

            // Post the result back to the main/UI thread
            String finalResult = result;
            mainHandler.post(() -> {
                callback.onDone(finalResult); });

        }).start();
    }
}