package com.example.foodathome;

import android.content.Context;

/**
 * Singleton handler for initializing and providing access to the AI client.
 * This ensures that only one instance of the GeminiHelper is created for the application.
 */
public class AiHandler {
    public static GeminiHelper AIClient;

    /**
     * Initializes the singleton AIClient instance.
     * If the client has not been created yet, it instantiates a new GeminiHelper.
     * This method is safe to call multiple times.
     *
     * @param context The application context, used for accessing API keys and other resources.
     */
    public static void init(Context context) {
        if (AIClient == null) {
            AIClient = new GeminiHelper(context.getApplicationContext());
        }
    }
}
