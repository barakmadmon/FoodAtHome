package com.example.foodathome;

import android.content.Context;

/* METHODS:
    + init- initialise client object
        input: current context
*/

public class AiHandler {
    public static GeminiHelper AIClient;

    public static void init(Context context) {
        if (AIClient == null) {
            AIClient = new GeminiHelper(context.getApplicationContext());
        }
    }
}
