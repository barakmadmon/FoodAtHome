package com.example.foodathome;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.genai.Client;

/**
 * Helper class for interacting with the Gemini AI model.
 */
public class GeminiHelper {
    Client client;
    boolean succesfulResponse;

    /**
     * Constructor for GeminiHelper.
     * @param context The application context, used to retrieve the API key.
     */
    public GeminiHelper(Context context) {
        String apiKey = context.getString(R.string.gemini_api_key);
        client = Client.builder().apiKey(apiKey).build();
    }

    /**
     * Checks if the last response from the AI was successful.
     * @return True if the response was successful, false otherwise.
     */
    public boolean ResponseStatus() {
        return this.succesfulResponse;
    }

    /**
     * Asynchronously asks the Gemini model a question and returns the response via a callback.
     * @param text The question to ask the AI.
     * @param callback The callback to be executed when the response is received.
     */
    public void askGemini(String text, Callback callback) {
        new Thread(() -> {
            String result = "";
            try {
                int tries = 4;
                do
                {
                    result = getResponse(text);
                    tries--;

                    // other errors usually means asking again would not yield responses
                    succesfulResponse= result != null && !result.isEmpty() && !result.startsWith("error:com.google.genai.errors.ServerException: 503");
                    if (!succesfulResponse) {
                        Thread.sleep(1000);
                    }
                } while (tries > 0 && !succesfulResponse);

                this.succesfulResponse = result != null && !result.isEmpty() && !result.contains("error:com.google.genai.errors");
                Log.i("myComments", "got response");
                Log.i("myComments", result);

            } catch (Throwable t) {
                Log.i("myComments", t.toString());
            }

            String finalResult = result;
            callback.onDone(finalResult);
        }).start();
    }

    /**
     * Sends a request to the Gemini model and returns the response.
     * @param text The question to ask the AI.
     * @return The response from the AI.
     */
    private String getResponse(String text) {
        String response = "";
        try {
            response = client.models.generateContent(
                    "gemini-2.5-flash",
                    text,
                    null).text();
        } catch (Exception e) {
            Log.e("GeminiHelper", "Error generating content", e);
            response = "error:" + e.toString();
        }
        return response;
    }
}