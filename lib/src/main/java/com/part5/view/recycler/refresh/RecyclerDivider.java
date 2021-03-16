package com.part5.view.recycler.refresh;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerDivider extends RecyclerView.ItemDecoration {
    private final int mOrientation;
    private final Paint mPaint;
    private float dividerHeight, leftMargin, rightMargin;

    public RecyclerDivider(int orientation) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        this.mOrientation = orientation;
    }

    /**
     * 横向的分割线
     *
     * @param height      分割线高
     * @param color       分割线颜色
     * @param leftMargin  分割线距离左边的距离
     * @param rightMargin 分割线距离右边的距离
     */
    public void initVerticalDivider(float height, int color, float leftMargin, float rightMargin) {
        this.dividerHeight = height;
        mPaint.setColor(color);
        this.leftMargin = leftMargin;
        this.rightMargin = rightMargin;
    }

    public void initHorizontalDivider(float height, int color) {
        initVerticalDivider(height, color, 0, 0);
    }

    @Override
    public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            final float left = parent.getPaddingLeft() + leftMargin;
            final float right = parent.getMeasuredWidth() - parent.getPaddingRight() - rightMargin;
            final int childSize = parent.getChildCount();
            for (int i = 0; i < childSize; i++) {
                final View child = parent.getChildAt(i);
                if (parent instanceof VerticalRecyclerView && ((VerticalRecyclerView) parent).isSpecialItemType(child)) {
                    continue;
                }
                RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int top = child.getBottom() + p.bottomMargin;
                final float bottom = top + dividerHeight;
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        } else {
            final int top = parent.getPaddingTop();
            final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
            final int childSize = parent.getChildCount();
            for (int i = 0; i < childSize; i++) {
                final View child = parent.getChildAt(i);
                if (parent instanceof VerticalRecyclerView && ((VerticalRecyclerView) parent).isSpecialItemType(child)) {
                    continue;
                }
                RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int left = child.getRight() + p.rightMargin;
                final float right = left + dividerHeight;
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        boolean isNormalItemType = parent instanceof VerticalRecyclerView && ((VerticalRecyclerView) parent).isSpecialItemType(view);
        int size = isNormalItemType ? 0 : (int) dividerHeight;
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            outRect.set(0, 0, 0, size);
        } else {
            outRect.set(0, 0, size, 0);
        }
    }
}