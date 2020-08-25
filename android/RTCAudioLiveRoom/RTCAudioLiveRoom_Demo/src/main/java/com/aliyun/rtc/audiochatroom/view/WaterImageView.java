package com.aliyun.rtc.audiochatroom.view;


import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.aliyun.rtc.audiochatroom.utils.SizeUtil;

import java.util.ArrayList;
import java.util.List;

public class WaterImageView extends android.support.v7.widget.AppCompatImageView {
    /**
     * 最大扩散距离
     */
    private final int MARGIN_LENGTH = SizeUtil.dip2px(getContext(), 11);
    private int mMinCircleRadius;
    private int centerX, centerY;
    private Paint mPaint;
    private List<ValueAnimator> mValueAnimators;
    private List<Water> mWaters;
    private boolean runningAnimation = false;

    /**
     * 最多同时存在三道水波纹
     */
    private static final int MAX_WATER_COUNT = 3;
    /**
     * 水波纹一圈的时间为5s
     */
    private static final int ANIMATION_DURATION = MAX_WATER_COUNT * 1000;

    public WaterImageView(Context context) {
        this(context, null);
    }

    public WaterImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        widthSize = getSizeBySpecMode(widthMode, widthSize);
        heightSize = getSizeBySpecMode(heightMode, heightSize);
        mMinCircleRadius = Math.max(widthSize, heightSize) / 2;
        centerX = widthSize / 2;
        centerY = heightSize / 2;
        setMeasuredDimension(widthSize, heightSize);
    }

    private int getSizeBySpecMode(int mode, int size) {
        if (mode == MeasureSpec.AT_MOST) {
            size = ((ViewGroup) getParent()).getMeasuredWidth();
        } else if (mode == MeasureSpec.UNSPECIFIED) {
            size = ((ViewGroup) getParent()).getMeasuredWidth();
        }
        return size;
    }

    @Override
    public void draw(Canvas canvas) {
        if (runningAnimation) {
            for (Water water : mWaters) {
                mPaint.setAlpha(water.alpha);
                canvas.drawCircle(centerX, centerY, water.waterRadius, mPaint);
            }
        }
        super.draw(canvas);
    }

    public void startWaterAnimation() {
        if (!runningAnimation) {
            synchronized (WaterImageView.class) {
                if (!runningAnimation) {
                    runningAnimation = true;
                    createWaterAnimation();
                }
            }
        }
    }

    private void createWaterAnimation() {
        if (mValueAnimators == null) {
            mValueAnimators = new ArrayList<>();
        }
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1.0F);
        valueAnimator.setDuration(ANIMATION_DURATION);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            Water water = null;
            int position = -1;
            boolean createNewWater = false;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                if (water == null) {
                    water = new Water();
                }
                water.waterRadius = mMinCircleRadius + MARGIN_LENGTH * value;
                water.alpha = (int) (150 * (1 - value));
                if (position == -1 && mWaters != null) {
                    position = mWaters.size();
                } else if (mWaters == null) {
                    position = 0;
                }
                addWater(water, position);
                postInvalidate();
                if (!createNewWater && mWaters.size() < MAX_WATER_COUNT && value >= 1.0F / MAX_WATER_COUNT) {
                    createNewWater = true;
                    createWaterAnimation();
                }
            }
        });
        valueAnimator.start();
        mValueAnimators.add(valueAnimator);
    }

    private void addWater(Water water, int position) {
        if (mWaters == null) {
            mWaters = new ArrayList<>();
        }

        if (mWaters.size() == 0) {
            mWaters.add(water);
        } else if (mWaters.size() > position) {
            mWaters.set(position, water);
        } else {
            mWaters.add(water);
        }

    }

    public boolean isRunningAnimation() {
        return runningAnimation;
    }

    public void stopWaterAnimation() {
        synchronized (WaterImageView.class) {
            if (mValueAnimators == null || !runningAnimation) {
                return;
            }
            runningAnimation = false;
            for (ValueAnimator valueAnimator : mValueAnimators) {
                boolean running = valueAnimator.isRunning();
                valueAnimator.removeAllUpdateListeners();
                if (running) {
                    valueAnimator.end();
                    valueAnimator.cancel();
                }

            }
            mValueAnimators.clear();
            mWaters.clear();
        }
    }

    private static class Water {
        float waterRadius;
        int alpha;
    }
}
