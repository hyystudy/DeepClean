package com.abclauncher.deepclean;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by sks on 2016/12/9.
 */
public class RecyclerViewDecoration extends RecyclerView.ItemDecoration {
    private int mOrientation;
    private int mSize;
    private int mColor;
    private Drawable mDivider;

    public RecyclerViewDecoration(Resources resources, @ColorRes int color, @DimenRes int size, int orientation){
        mOrientation = orientation;
        mSize = resources.getDimensionPixelSize(size);
        mColor = resources.getColor(color);
        mDivider = new ColorDrawable(mColor);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left;
        int right;
        int top ;
        int bottom;
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
                top = child.getBottom() + layoutParams.bottomMargin;
                bottom = top + mSize;
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }else {
            top = parent.getPaddingTop();
            bottom = parent.getHeight() - parent.getPaddingBottom();
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
                left = child.getRight() + layoutParams.rightMargin;
                right = left + mSize;
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
       if (mOrientation == LinearLayoutManager.VERTICAL){
            outRect.set(0, 0, 0, mSize);
       }else {
            outRect.set(0, 0, mSize, 0);
       }
    }
}
