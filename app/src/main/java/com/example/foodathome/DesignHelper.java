package com.example.foodathome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class DesignHelper {

    static public BitmapDescriptor DINING_ICON (Context context) {
        return descriptDrawable(context,R.drawable.outline_dining_24);
    }
    static private BitmapDescriptor descriptDrawable(Context context, int vectorResId) {
        // 1. Get the vector drawable from your resources
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        // 2. Set the bounds (size) of the drawable
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        // 3. Create a blank Bitmap with the same dimensions
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);

        // 4. Create a Canvas to "draw" the vector onto the blank Bitmap
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        // 5. Convert that Bitmap into a BitmapDescriptor for the Map
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
