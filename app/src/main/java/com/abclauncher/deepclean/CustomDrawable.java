package com.abclauncher.deepclean;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

/**
 * Created by sks on 2016/12/22.
 */

public class CustomDrawable extends Drawable {
    private float mRadius;
    private int mColor;
    private final Paint mPaint;
    private int x;
    private int y;

    public CustomDrawable(int rippleColor, int x, int y, float radius){
        mColor = rippleColor;
        mRadius = radius;
        this.x = x;
        this.y = y;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(rippleColor);
    }


    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(x, y, mRadius, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }
}
