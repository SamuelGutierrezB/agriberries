package com.agriberriesmx.agriberries.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatSeekBar;

public class CustomSeekBarLevelAdd extends AppCompatSeekBar {
    private final int levels = 5;
    private final int barHeight = 80;
    private final int[] levelColors = {Color.parseColor("#029443"), Color.parseColor("#8DC741"), Color.parseColor("#FFDE16"), Color.parseColor("#F46523"), Color.parseColor("#ED1B24")};
    private int progress = 0;

    public CustomSeekBarLevelAdd(Context context) {
        super(context);
    }

    public CustomSeekBarLevelAdd(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        int segmentWidth = width / levels;

        for (int i = 0; i < levels; i++) {
            int left = i * segmentWidth;
            int right = (i + 1) * segmentWidth;
            int top = (height - barHeight) / 2;
            int bottom = (height + barHeight) / 2;

            Paint paint = new Paint();
            paint.setColor(levelColors[i]);

            canvas.drawRect(left, top, right, bottom, paint);
        }

        int indicatorRadius = barHeight / 2;
        int indicatorCenterY = height / 2;
        int indicatorCenterX;
        if (progress >= levels - 1) indicatorCenterX = (levels - 1) * segmentWidth + segmentWidth / 2;
        else indicatorCenterX = progress * segmentWidth + segmentWidth / 2;

        Paint indicatorPaint = new Paint();
        indicatorPaint.setColor(Color.BLACK);

        canvas.drawCircle(indicatorCenterX, indicatorCenterY, indicatorRadius, indicatorPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();

        progress = (int) (x / (getWidth() / levels));
        invalidate();

        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = MeasureSpec.getSize(widthMeasureSpec);

        setMeasuredDimension(desiredWidth, barHeight);
    }

}

