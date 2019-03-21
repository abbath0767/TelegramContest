package com.ng.telegramcontest.ui.view;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

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

    private int lastAdded = -1;
    private int lastRemoved = -1;
    private Paint mTextPaint;
    private Paint mSeparatorPaint;
    private Paint mDatePaint;
    private Paint mLinePaint;
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
    private ValueAnimator alphaAnimator;
    private long currentMax;
    private long currentMin;
    private boolean dataIsInit = false;
    private boolean firstBorderPush = true;
    float[][] points;
    private float bottomBorderY = 0f;
    private float topBorderY = 0f;
    private float borderedHeight = 0f;
    private int xStart = 0;
    private int xEnd = 0;
    private int len = 0;

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

        mLinePaint = new Paint();
        mLinePaint.setStrokeWidth(getContext().getResources().getDimension(R.dimen.common_2));
        mLinePaint.setColor(Color.RED);
        mLinePaint.setAntiAlias(true);
        setLayerType(LAYER_TYPE_HARDWARE, mLinePaint);

        mSeparatorPaint = new Paint();
        mSeparatorPaint.setColor(getContext().getResources().getColor(R.color.colorSeparatorDay));
        mSeparatorPaint.setStrokeWidth(getContext().getResources().getDimension(R.dimen.common_1));

        testPaint = new Paint();
        testPaint.setStrokeWidth(getContext().getResources().getDimension(R.dimen.common_2));
        testPaint.setColor(Color.RED);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float topBorderY = getHeight() - getHeight() * 0.9f;
        float leftBorder = 16 * density;
        canvas.drawText(FOLLOWERS, leftBorder, topBorderY, mTextPaint);
        canvas.drawLine(0, bottomBorderY, getWidth(), bottomBorderY, mSeparatorPaint);

        if (!dataIsInit) {
            super.onDraw(canvas);
            return;
        }

        if (points != null) {
            drawLines(canvas);
        }

        super.onDraw(canvas);
    }

    private void drawLines(Canvas canvas) {
        for (int chartIndex = 0; chartIndex < mChartData.getDataSets().length; chartIndex++) {
            mLinePaint.setColor(Color.parseColor(mChartData.getDataSets()[chartIndex].getColor()));
            mLinePaint.setAlpha(getAlphaFor(chartIndex));
            float prevX = 0f;
            float prevY = 0f;

            for (int i = 0; i < points[0].length; i++) {
                if (i == 0) {
                    prevX = points[0][i];
                    prevY = bottomBorderY - points[chartIndex + 1][i];
                } else {
                    canvas.drawLine(prevX, prevY, points[0][i], bottomBorderY - points[chartIndex + 1][i], mLinePaint);
                    prevX = points[0][i];
                    prevY = bottomBorderY - points[chartIndex + 1][i];
                }
            }
        }
    }

    private int getAlphaFor(int chartIndex) {
        if (chartIndex == lastAdded) {
            return (int) alphaAnimator.getAnimatedValue();
        } else if (chartIndex == lastRemoved) {
            return (int) alphaAnimator.getAnimatedValue();
        } else if (mSelectedCharts[chartIndex])
            return 255;
        else
            return 0;
    }

    public void pushBorderChange(final int fromX, final int toX, final int type) {
        if (mChartData == null)
            return;

        from = fromX;
        to = toX;

        if (firstBorderPush) {
            int len = mChartData.getX().getValues().length;
            int xStart = Math.round(fromX * len / getWidth());
            int xEnd = Math.round(toX * len / getWidth());
            boolean inited = false;
            for (int chartIndex = 0; chartIndex < mChartData.getDataSets().length; chartIndex++) {
                long[] y = mChartData.getDataSets()[chartIndex].getValues();
                for (int i = xStart; i <= xEnd; i++) {
                    if (!inited) {
                        inited = true;
                        currentMin = y[i];
                        currentMax = y[i];
                        continue;
                    }
                    if (currentMin > y[i]) {
                        currentMin = y[i];
                    }
                    if (currentMax < y[i]) {
                        currentMax = y[i];
                    }
                }
            }
            firstBorderPush = false;
        }

        initPoints();
    }

    private void initPoints() {
        initPoints(false, -1, -1);
    }

    //todo rework
    private void initPoints(boolean customExtremum, long min, long max) {
        points = new float[mChartData.getDataSets().length + 1][];
        int width = getWidth();
        xStart = Math.round((float) (from * len) / (float) width);
        xEnd = Math.round((float) (to * len) / (float) width);
        int countBetween = xEnd - xStart;

        float testStart = (float) (from * len) / (float) getWidth();
        float testTo = (float) (to * len) / (float) getWidth();
        float testDiff = testTo - testStart;

        if (countBetween != (int) testDiff) {
            countBetween = (int) testDiff;
            xEnd = xStart + countBetween;
        }

        if (xEnd >= len)
            xEnd = len - 1;
        if (xStart < 0)
            xStart = 0;

        points[0] = new float[xEnd - xStart + 1];
        long delta = xEnd - xStart;
        float step = (float) width / (float) delta;
        for (int i = 0; i <= delta; i++) {
            points[0][i] = step * i;
        }

        boolean inited = false;
        long currentYMin = 0;
        long currentYMax = 0;
        if (customExtremum) {
            currentYMin = min;
            currentYMax = max;
        } else {
            for (int chartIndex = 0; chartIndex < mChartData.getDataSets().length; chartIndex++) {
                if (!mSelectedCharts[chartIndex]) {
                    continue;
                }
                long[] y = mChartData.getDataSets()[chartIndex].getValues();
                for (int i = xStart; i <= xEnd; i++) {
                    if (!inited) {
                        inited = true;
                        currentYMin = y[i];
                        currentYMax = y[i];
                        continue;
                    }
                    if (currentYMin > y[i]) {
                        currentYMin = y[i];
                    }
                    if (currentYMax < y[i]) {
                        currentYMax = y[i];
                    }
                }
            }
        }

        delta = currentYMax - currentYMin;
        for (int chartIndex = 0; chartIndex < mChartData.getDataSets().length; chartIndex++) {
            points[chartIndex + 1] = new float[xEnd - xStart + 1];
            long[] y = mChartData.getDataSets()[chartIndex].getValues();
            for (int i = xStart; i <= xEnd; i++) {
                points[chartIndex + 1][i - xStart] = (y[i] - currentYMin) * borderedHeight / delta;
            }
        }

        postInvalidateOnAnimation();
    }

    public void initData(ChartData chartData, boolean[] selectedCharts) {
        mChartData = chartData;
        mSelectedCharts = selectedCharts.clone();
        preparedDateFormats = new String[mChartData.getX().getValues().length];
        Date date = new Date();
        long[] x = mChartData.getX().getValues();
        for (int i = 0; i < preparedDateFormats.length; i++) {
            date.setTime(x[i]);
            preparedDateFormats[i] = format.format(date);
        }
        len = mChartData.getX().getValues().length;

        dataIsInit = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float height = MeasureSpec.getSize(heightMeasureSpec);
        bottomBorderY = height * 0.9f;
        topBorderY = height * 0.1f;
        borderedHeight = bottomBorderY - topBorderY;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void changeSelect(boolean[] selectedCharts) {
        int oldCount = 0;
        int newCount = 0;
        for (boolean show : mSelectedCharts) {
            if (show)
                oldCount++;
        }
        for (boolean show : selectedCharts) {
            if (show)
                newCount++;
        }


        boolean added = newCount > oldCount;
        if (added) {
            lastRemoved = -1;
            for (int i = 0; i < selectedCharts.length; i++) {
                if (selectedCharts[i] && !mSelectedCharts[i]) {
                    lastAdded = i;
                    break;
                }
            }
        } else {
            lastAdded = -1;
            for (int i = 0; i < selectedCharts.length; i++) {
                if (!selectedCharts[i] && mSelectedCharts[i]) {
                    lastRemoved = i;
                    break;
                }
            }
        }

        mSelectedCharts = selectedCharts.clone();

        long newMax = -1;
        long newMin = -1;
        boolean inited = false;
        int len = mChartData.getX().getValues().length;
        int xStart = Math.round(from * len / getWidth());
        int xEnd = Math.round(to * len / getWidth());
        for (int chartIndex = 0; chartIndex < mChartData.getDataSets().length; chartIndex++) {
            if (!mSelectedCharts[chartIndex]) {
                continue;
            }
            long[] y = mChartData.getDataSets()[chartIndex].getValues();
            for (int i = xStart; i <= xEnd; i++) {
                if (!inited) {
                    inited = true;
                    newMin = y[i];
                    newMax = y[i];
                    continue;
                }
                if (newMin > y[i]) {
                    newMin = y[i];
                }
                if (newMax < y[i]) {
                    newMax = y[i];
                }
            }
        }

        if ((currentMax == -1 || currentMin == -1) && !added) {
            clearChart();
        } else if (currentMax == newMax && currentMin == newMin) {
            int alphaFrom = 255;
            int alphaTo = 0;
            if (added) {
                alphaFrom = 0;
                alphaTo = 255;
            }

            alphaAnimator = ValueAnimator.ofInt(alphaFrom, alphaTo);
            alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    postInvalidateOnAnimation();
                }
            });
            alphaAnimator.start();
        } else {
            int alphaFrom = 255;
            int alphaTo = 0;

            if (added) {
                alphaFrom = 0;
                alphaTo = 255;
            }

            final ValueAnimator min = ValueAnimator.ofFloat(currentMin, newMin);
            final ValueAnimator max = ValueAnimator.ofFloat(currentMax, newMax);
            alphaAnimator = ValueAnimator.ofInt(alphaFrom, alphaTo);
            min.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    long value = ((Float) max.getAnimatedValue()).longValue();
                    if (value == 0)
                        value = currentMax;
                    initPoints(true, ((Float) animation.getAnimatedValue()).longValue(), value);
                }
            });

            max.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    long value = ((Float) min.getAnimatedValue()).longValue();
                    if (value == 0)
                        value = currentMin;
                    initPoints(true, value, ((Float) animation.getAnimatedValue()).longValue());
                }
            });
            AnimatorSet set = new AnimatorSet();
            set.playTogether(min, max, alphaAnimator);
            set.setInterpolator(new DecelerateInterpolator());
            set.start();

            currentMax = newMax;
            currentMin = newMin;
        }
    }

    private void clearChart() {
        points = null;
        invalidate();
    }
}
