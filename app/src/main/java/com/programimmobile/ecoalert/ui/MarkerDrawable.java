package com.programimmobile.ecoalert.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.programimmobile.ecoalert.R;

public class MarkerDrawable extends Drawable {

    private final Paint circlePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path  tailPath     = new Path();

    private final String letter;
    private final int    fillColor;
    private final int    size;

    public MarkerDrawable(Context context, String category, int confirmations) {
        this.letter    = getCategoryLetter(category);
        this.fillColor = getCategoryColor(context, category);

        // Madhësia rritet me konfirmimet — min 48, max 72
        this.size = Math.min(72, 48 + confirmations * 4);

        // Shadow
        shadowPaint.setColor(Color.argb(60, 0, 0, 0));
        shadowPaint.setStyle(Paint.Style.FILL);

        // Rreth kryesor
        circlePaint.setColor(fillColor);
        circlePaint.setStyle(Paint.Style.FILL);

        // Border i bardhë
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f);

        // Teksti
        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(size * 0.38f);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        float cx    = bounds.exactCenterX();
        float cy    = bounds.height() * 0.42f;
        float r     = size * 0.42f;

        // Hija
        canvas.drawCircle(cx + 2, cy + 3, r + 1, shadowPaint);

        // Bishti i markerit (trekëndësh poshtë)
        tailPath.reset();
        tailPath.moveTo(cx - r * 0.35f, cy + r * 0.75f);
        tailPath.lineTo(cx + r * 0.35f, cy + r * 0.75f);
        tailPath.lineTo(cx,             cy + r * 1.55f);
        tailPath.close();
        canvas.drawPath(tailPath, circlePaint);

        // Rreth kryesor
        canvas.drawCircle(cx, cy, r, circlePaint);

        // Border
        canvas.drawCircle(cx, cy, r, borderPaint);

        // Shkronja
        float textY = cy - (textPaint.descent() + textPaint.ascent()) / 2f;
        canvas.drawText(letter, cx, textY, textPaint);
    }

    @Override
    public int getIntrinsicWidth()  { return size + 8; }

    @Override
    public int getIntrinsicHeight() { return (int)(size * 1.7f); }

    @Override
    public void setAlpha(int alpha) { circlePaint.setAlpha(alpha); }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {}

    @Override
    public int getOpacity() { return PixelFormat.TRANSLUCENT; }

    private String getCategoryLetter(String category) {
        if (category == null) return "?";
        switch (category) {
            case "Mbetje Urbane": return "M";
            case "Zhurmë":        return "Z";
            case "Ndotje Ajri":   return "A";
            case "Ndotje Uji":    return "U";
            default:              return "T";
        }
    }

    private int getCategoryColor(Context context, String category) {
        int colorRes;
        if (category == null) {
            colorRes = R.color.category_other;
        } else {
            switch (category) {
                case "Mbetje Urbane": colorRes = R.color.category_waste;  break;
                case "Zhurmë":        colorRes = R.color.category_noise;  break;
                case "Ndotje Ajri":   colorRes = R.color.category_air;    break;
                case "Ndotje Uji":    colorRes = R.color.category_water;  break;
                default:              colorRes = R.color.category_other;  break;
            }
        }
        return ContextCompat.getColor(context, colorRes);
    }
}