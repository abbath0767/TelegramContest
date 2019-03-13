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

import com.ng.telegramcontest.R;
import com.ng.telegramcontest.data.ChartData;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BigGraph extends View {

    private final static String FOLLOWERS = "Followers";
    private final static int POINT_COUNT = 7;
    private final static SimpleDateFormat format = new SimpleDateFormat("MMM dd");

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
    private Paint mDatePaint;
    private ChartData mChartData;
    private String[] preparedDateFormats;
    private boolean[] mSelectedCharts;
    private int diffX;
    private int previousDiffX = 0;
    private float previousEnd = 0f;
    private int from;
    private int to;
    private float[] drawDataCord;
    private ValueAnimator diffAnimator;
    private ChangeBorderType mChangeBorderType;
    private long currentMax;
    private long currentMin;
    private boolean dataIsInit = false;
    private boolean firstBorderPush = true;
    private int fixedFromTo = 0;
    private int fixedStepForDateArrIndex = 0;

    private float density = getResources().getDisplayMetrics().density;

    //todo test
    private Paint testPaint;

    private void initPaints() {
        mTextPaint = new Paint();
        mTextPaint.setColor(getContext().getResources().getColor(R.color.colorPrimary));
        mTextPaint.setStyle(Paint.Style.FILL);
        float scaledSizeInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics());
        mTextPaint.setTextSize(scaledSizeInPixels);

        mDatePaint = new Paint();
        mDatePaint.setColor(getContext().getResources().getColor(R.color.colorDateDay));
        mDatePaint.setStyle(Paint.Style.FILL);
        scaledSizeInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics());
        mDatePaint.setTextSize(scaledSizeInPixels);

        mSeparatorPaint = new Paint();
        mSeparatorPaint.setColor(getContext().getResources().getColor(R.color.colorSeparatorDay));
        mSeparatorPaint.setStrokeWidth(getContext().getResources().getDimension(R.dimen.common_1));

        testPaint = new Paint();
        testPaint.setColor(Color.RED);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float bottomBorderY = getHeight() - getHeight() / 10.0f;
        float topBorderY = getHeight() - getHeight() * 0.9f;
        float leftBorder = ((getWidth() - ((getWidth() / 1.3f))) / 2) + 16 * density;
        canvas.drawText(FOLLOWERS, leftBorder, topBorderY, mTextPaint);
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
        //todo UPDATE after EXTEND/COLLAPSE
//        float step = fixedFromTo / POINT_COUNT;

        //TODO MOVE TO (1)
        for (int i = 0; i < drawDataCord.length; i++) {
            int value = Math.round(fixedStepForDateArrIndex * i + fixedStepForDateArrIndex / 2f);
//            int value = from + (i * fixedStepForDateArrIndex) - fixedStepForDateArrIndex / 2;
//            int test = from + i * fixedStepForDateArrIndex - fixedStepForDateArrIndex / 2;

//            Log.d("TAG", "FOR: " + i + " test= " + test);

            String text = preparedDateFormats[value];
//
            canvas.drawText(text, drawDataCord[i], y, mDatePaint);
        }
    }

    public void pushBorderChange(final SelectWindowView.Border border) {
        if (mChartData == null)
            return;

        int tmpFrom = from;
        int tmpTo = to;
        from = border.fromX;
        to = border.toX;

        if (!firstBorderPush) {
            int diffFrom = tmpFrom - from;
            int diffTo = tmpTo - to;

            if (diffFrom == diffTo) {
                diffX = tmpFrom - from;
                mChangeBorderType = ChangeBorderType.MOVE;

                if (diffX == 0) {
                    return;
                }
                fixedStepForDateArrIndex = (to - from) / POINT_COUNT;
                borderMove(diffX);
            } else {
                mChangeBorderType = ChangeBorderType.EXTEND;
            }
        } else {
            drawDataCord = new float[POINT_COUNT];
            int width = getWidth();
            float stepCoord = width / (float) POINT_COUNT;
            for (int i = 0; i < drawDataCord.length; i++) {
                //calculate draw pos
                drawDataCord[i] = stepCoord * i + stepCoord / 2f;
            }

            fixedStepForDateArrIndex = (to - from) / POINT_COUNT;
            firstBorderPush = false;
        }

        postInvalidateOnAnimation();
    }

    private void borderMove(final int diff) {
        final boolean toRight = diff > 0;
        final int len = to - from;

        if (diffAnimator == null) {
            previousDiffX = diff;
            diffAnimator = ValueAnimator.ofFloat(0f, diff);
//            diffAnimator.setInterpolator(new LinearInterpolator());
        } else {
            previousEnd += (float) diffAnimator.getAnimatedValue();
            diffAnimator.removeAllUpdateListeners();
            diffAnimator.cancel();
            diffAnimator = ValueAnimator.ofFloat(0f, diff + previousDiffX - previousEnd);
//            diffAnimator.setInterpolator(new LinearInterpolator());
            previousDiffX = diff + previousDiffX;
        }

        diffAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            float tmpValue = 0;

            //TODO (1)
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedDiff = (float) animation.getAnimatedValue() - tmpValue;
                tmpValue = (float) animation.getAnimatedValue();
                for (int i = 0; i < drawDataCord.length; i++) {
                    drawDataCord[i] = drawDataCord[i] + (animatedDiff * getWidth() / len);
                }

                if (toRight) {
                    if (drawDataCord[drawDataCord.length - 1] > getWidth()) {
                        for (int i = drawDataCord.length - 1; i > 0; i--) {
                            drawDataCord[i] = drawDataCord[i - 1];
                        }
                        drawDataCord[0] = drawDataCord[1] - getWidth() / (float) POINT_COUNT;
                    }
                } else {
                    if (drawDataCord[0] < 0) {
                        for (int i = 0; i < drawDataCord.length - 1; i++) {
                            drawDataCord[i] = drawDataCord[i + 1];
                        }
                        drawDataCord[drawDataCord.length - 1] = drawDataCord[drawDataCord.length - 2] + getWidth() / (float) POINT_COUNT;
                    }
                }

                postInvalidateOnAnimation();
            }
        });
        diffAnimator.start();
    }

    public void initData(ChartData chartData, boolean[] selectedCharts) {
        Log.d("TAG", "BIG GRAPH. Init data");
        mChartData = chartData;
        mSelectedCharts = selectedCharts;
        preparedDateFormats = new String[mChartData.getX().getValues().length];
        Date date = new Date();
        long[] x = mChartData.getX().getValues();
        for (int i = 0; i < preparedDateFormats.length; i++) {
            date.setTime(x[i]);
            preparedDateFormats[i] = format.format(date);
        }

        currentMax = mChartData.getMaxY();
        currentMin = mChartData.getMinY();
        dataIsInit = true;

//        testSwipeLeft();
//        testSwipeRight();
    }

    private void testSwipeLeft() {
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                pushBorderChange(new SelectWindowView.Border(from - 1, to - 1));
            }
        }, 3000);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                pushBorderChange(new SelectWindowView.Border(from - 2, to - 2));
            }
        }, 3030);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                pushBorderChange(new SelectWindowView.Border(from - 3, to - 3));
            }
        }, 3050);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                pushBorderChange(new SelectWindowView.Border(from - 4, to - 4));
            }
        }, 3080);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                pushBorderChange(new SelectWindowView.Border(from - 5, to - 5));
            }
        }, 3140);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                pushBorderChange(new SelectWindowView.Border(from - 5, to - 5));
//                testSwipeRight();
            }
        }, 3500);
    }

    private void testSwipeRight() {
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                pushBorderChange(new SelectWindowView.Border(from + 1, to + 1));
            }
        }, 3000);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                pushBorderChange(new SelectWindowView.Border(from + 1, to + 1));
            }
        }, 3030);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                pushBorderChange(new SelectWindowView.Border(from + 2, to + 2));
            }
        }, 3050);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                pushBorderChange(new SelectWindowView.Border(from + 1, to + 1));
            }
        }, 3150);
    }

    public void changeSelect(boolean[] selectedCharts) {
        Log.d("TAG", "BIG GRAPH. change select");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) (MeasureSpec.getSize(widthMeasureSpec) * 1.3), MeasureSpec.getSize(heightMeasureSpec));
    }

    enum ChangeBorderType {
        MOVE, EXTEND
    }
}
