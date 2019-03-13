package com.ng.telegramcontest.ui.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.ng.telegramcontest.R;
import com.ng.telegramcontest.data.ChartData;

import java.util.Arrays;

public class BigGraph extends View {

    private final static String FOLLOWERS = "Followers";

    public BigGraph(Context context) {
        this(context, null);
    }

    public BigGraph(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BigGraph(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BigGraph(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initPaints();
    }

    private Paint mTextPaint;
    private Paint mSeparatorPaint;
    private ChartData mChartData;
    private boolean[] mSelectedCharts;
    private int diff;
    private int from;
    private int to;
//    private int[] drawDatePos;
    private float[] drawDataCord;
    private ValueAnimator diffAnimator;
    private ChangeBorderType mChangeBorderType;
    private long currentMax;
    private long currentMin;
    private boolean dataIsInit = false;
    private boolean firstBorderPush = true;

    //todo test
    private Paint testPaint;

    private void initPaints() {
        mTextPaint = new Paint();
        mTextPaint.setColor(getContext().getResources().getColor(R.color.colorPrimary));
        mTextPaint.setStyle(Paint.Style.FILL);
        float scaledSizeInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics());
        mTextPaint.setTextSize(scaledSizeInPixels);

        mSeparatorPaint = new Paint();
        mSeparatorPaint.setColor(getContext().getResources().getColor(R.color.colorSeparatorDay));
        mSeparatorPaint.setStrokeWidth(getContext().getResources().getDimension(R.dimen.common_1));

        testPaint = new Paint();
        testPaint.setColor(Color.RED);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d("TAG", "ON DRAW");
        float bottomBorderY = getHeight() - getHeight() / 10.0f;
        float topBorderY = getHeight() - getHeight() * 0.9f;
        canvas.drawText(FOLLOWERS, 0, topBorderY, mTextPaint);
        canvas.drawLine(0, bottomBorderY, getWidth(), bottomBorderY, mSeparatorPaint);

        if (!dataIsInit) {
            super.onDraw(canvas);
            return;
        }

        drawBottomDate(canvas, bottomBorderY);

        super.onDraw(canvas);
    }

    private void drawBottomDate(Canvas canvas, float bottomBorderY) {
        float y = bottomBorderY + (getHeight() - bottomBorderY) / 2f;
        for (int i = 0; i < drawDataCord.length; i++) {
            //todo draw text
            canvas.drawCircle(drawDataCord[i], y, 10, testPaint);
        }
    }

    //todo refactor from/to from % to xPos
    public void pushBorderChange(final SelectWindowView.Border border) {
        if (mChartData == null)
            return;

        Log.d("TAG", "BIG GRAPH. Push border change");
        int tmpFrom = from;
        int tmpTo = to;
        from = border.fromX;
        to = border.toX;

        if (!firstBorderPush) {
            int diffFrom = tmpFrom - from;
            int diffTo = tmpTo - to;

            if (diffFrom == diffTo) {
                diff = tmpFrom - from;
                Log.d("TAG", "MOVE. from:" + from + ", to: " + to + ", diff: " + diff);
                mChangeBorderType = ChangeBorderType.MOVE;

                borderMove(diff);
            } else {
                Log.d("TAG", "EXTEND");
                mChangeBorderType = ChangeBorderType.EXTEND;
            }
        } else {
//            drawDatePos = new int[5];
            drawDataCord = new float[5];
            int step = (to - from) / 5;
            int width = getWidth();
            float stepCoord = width / 5f;
            for (int i = 0; i < 5; i++) {
//                drawDatePos[i] = from + step * (i + 1);
                drawDataCord[i] = stepCoord * (i + 1) - stepCoord / 2f;
            }

//            Log.d("TAG", "draw data pos: " + Arrays.toString(drawDatePos));
            Log.d("TAG", "draw data coord: " + Arrays.toString(drawDataCord));

            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pushBorderChange(new SelectWindowView.Border(border.fromX - 5, border.toX - 5));
                }
            }, 3000);
        }

        firstBorderPush = false;

        postInvalidateOnAnimation();
    }

    private void borderMove(int diff) {
        final int len = to - from;
        diffAnimator = ValueAnimator.ofFloat(0f, diff);
        diffAnimator.setInterpolator(new LinearInterpolator());
        diffAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            float tmpValue = 0;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedDiff = (float) animation.getAnimatedValue() - tmpValue;
                for (int i = 0; i < drawDataCord.length; i++) {
//                    drawDatePos[i] = drawDatePos[i] + animatedDiff;
                    drawDataCord[i] = drawDataCord[i] + (animatedDiff * getWidth() / len);
                }
                tmpValue = animatedDiff;
                postInvalidateOnAnimation();
            }
        });
        diffAnimator.start();
    }

    public void initData(ChartData chartData, boolean[] selectedCharts) {
        Log.d("TAG", "BIG GRAPH. Init data");
        mChartData = chartData;
        mSelectedCharts = selectedCharts;

        currentMax = mChartData.getMaxY();
        currentMin = mChartData.getMinY();
        dataIsInit = true;
    }

    public void changeSelect(boolean[] selectedCharts) {
        Log.d("TAG", "BIG GRAPH. change select");
    }

    enum ChangeBorderType {
        MOVE, EXTEND;
    }
}
