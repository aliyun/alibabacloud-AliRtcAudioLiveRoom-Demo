package com.aliyun.rtc.audiochatroom.adapter;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.aliyun.rtc.audiochatroom.utils.SizeUtil;


public class AutoItemDecoration extends RecyclerView.ItemDecoration {


    private int mChildCount;

    public AutoItemDecoration(int childCount) {
        mChildCount = childCount;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        int marginRight = 0;
        if (layoutManager instanceof LinearLayoutManager && ((LinearLayoutManager) layoutManager).getOrientation() == OrientationHelper.HORIZONTAL && mChildCount > 0) {
            int measuredWidth = parent.getMeasuredWidth();
            int childViewWidth = view.getMeasuredWidth();
            if (childViewWidth == 0) {
                view.measure(0, 0);
            }
            childViewWidth = view.getMeasuredWidth();
            if (mChildCount * childViewWidth < measuredWidth && mChildCount > 1 && parent.getChildCount() < mChildCount) {
                marginRight = (measuredWidth - (mChildCount * childViewWidth)) / (mChildCount - 1);
            }
            outRect.right = marginRight;
        }
    }
}
