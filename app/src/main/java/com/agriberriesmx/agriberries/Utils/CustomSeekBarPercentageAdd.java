package com.agriberriesmx.agriberries.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatSeekBar;

public class CustomSeekBarPercentageAdd extends AppCompatSeekBar {
    private Paint rectPaint;
    private Paint indicatorPaint;
    private final int barHeight = 80;
    private final int[] levelColors = {Color.parseColor("#ED1B24"), Color.parseColor("#F46523"), Color.parseColor("#FFDE16"), Color.parseColor("#8DC741"), Color.parseColor("#029443")};
    private int progress = 0;

    public CustomSeekBarPercentageAdd(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        rectPaint = new Paint();
        indicatorPaint = new Paint();
        indicatorPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int levels = 5;
        int segmentWidth = width / levels;

        for (int i = 0; i < levels; i++) {
            int left = i * segmentWidth;
            int right = (i + 1) * segmentWidth;
            int top = (height - barHeight) / 2;
            int bottom = (height + barHeight) / 2;

            rectPaint.setColor(levelColors[i]);
            canvas.drawRect(left, top, right, bottom, rectPaint);
        }

        // Get position based on the percentage
        int indicatorRadius = barHeight / 2;
        int indicatorCenterX = (int) (width * (progress / 100f));
        int indicatorCenterY = height / 2;

        canvas.drawCircle(indicatorCenterX, indicatorCenterY, indicatorRadius, indicatorPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        int width = getWidth();

        progress = (int) (100 * (x / width));
        progress = Math.max(0, Math.min(progress, 100));
        invalidate();

        if (event.getAction() == MotionEvent.ACTION_UP) performClick();
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = MeasureSpec.getSize(widthMeasureSpec);

        setMeasuredDimension(desiredWidth, barHeight);
    }

}

