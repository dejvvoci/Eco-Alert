package com.programimmobile.ecoalert.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;

import androidx.core.content.ContextCompat;

import com.programimmobile.ecoalert.R;

public class MarkerBitmapHelper {

    public static BitmapDrawable createMarker(Context context,
                                              String category,
                                              int confirmations) {
        // Madhësia bazë
        int size = Math.min(72, 48 + Math.min(confirmations, 6) * 4);
        int w    = size + 16;
        int h    = (int)(size * 1.8f);

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        float cx = w / 2f;
        float r  = size * 0.42f;
        float cy = r + 4f;

        int fillColor = getCategoryColor(context, category);

        // Paint-et
        Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.argb(80, 0, 0, 0));
        shadowPaint.setStyle(Paint.Style.FILL);

        Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(fillColor);
        fillPaint.setStyle(Paint.Style.FILL);

        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(size * 0.38f);

        // Hija
        canvas.drawCircle(cx + 2f, cy + 3f, r + 1f, shadowPaint);

        // Bishti
        Path tail = new Path();
        tail.moveTo(cx - r * 0.35f, cy + r * 0.75f);
        tail.lineTo(cx + r * 0.35f, cy + r * 0.75f);
        tail.lineTo(cx,             cy + r * 1.6f);
        tail.close();
        canvas.drawPath(tail, fillPaint);

        // Rreth
        canvas.drawCircle(cx, cy, r, fillPaint);
        canvas.drawCircle(cx, cy, r, borderPaint);

        // Shkronja
        String letter = getCategoryLetter(category);
        float textY   = cy - (textPaint.descent() + textPaint.ascent()) / 2f;
        canvas.drawText(letter, cx, textY, textPaint);

        return new BitmapDrawable(context.getResources(), bitmap);
    }

    private static String getCategoryLetter(String category) {
        if (category == null) return "?";
        switch (category) {
            case "Mbetje Urbane": return "M";
            case "Zhurmë":        return "Z";
            case "Ndotje Ajri":   return "A";
            case "Ndotje Uji":    return "U";
            default:              return "T";
        }
    }

    private static int getCategoryColor(Context context, String category) {
        int colorRes;
        if (category == null) {
            colorRes = R.color.category_other;
        } else {
            switch (category) {
                case "Mbetje Urbane": colorRes = R.color.category_waste; break;
                case "Zhurmë":        colorRes = R.color.category_noise; break;
                case "Ndotje Ajri":   colorRes = R.color.category_air;   break;
                case "Ndotje Uji":    colorRes = R.color.category_water; break;
                default:              colorRes = R.color.category_other; break;
            }
        }
        return ContextCompat.getColor(context, colorRes);
    }
}