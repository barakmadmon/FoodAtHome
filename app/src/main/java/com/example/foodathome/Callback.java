package com.example.foodathome;

/**
 * A generic callback interface used for handling the results of asynchronous operations.
 * This is widely used for Firebase calls to return a status message upon completion.
 */
public interface Callback {
    /**
     * Called when the asynchronous operation is complete.
     * @param result A string containing the result of the operation, typically "Success" or an error message.
     */
    void onDone(String result);
}
