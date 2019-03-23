package com.ng.telegramcontest.ui.view;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.ng.telegramcontest.R;
import com.ng.telegramcontest.data.ChartData;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class BigGraph extends View {

    private final static String FOLLOWERS = "Followers";
    private final static int PONT_COUNT = 7;
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
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
    private float from;
    private float to;
    private float[] drawDataCord;
    private ValueAnimator diffAnimator;
    private ValueAnimator alphaAnimator;
    private long currentMax;
    private long currentMin;
    private boolean dataIsInit = false;
    private boolean firstBorderPush = true;
    private float[][] points;
    private float bottomBorderY = 0f;
    private float topBorderY = 0f;
    private float borderedHeight = 0f;

    private float[] xSmallCoord;
    private float[] xTimeCoord;
    private int tmpCount = 0;
    private int tmpType = -1;
    private int leftIndex = 0;
    private int rightIndex = 0;

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

        for (int i = 0; i < xTimeCoord.length; i++) {
            canvas.drawCircle(xTimeCoord[i], bottomBorderY, 10, testPaint);
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

    public void pushBorderChange(final float fromX, final float toX, final int type) {
        if (mChartData == null)
            return;

        from = fromX;
        to = toX;

        if (firstBorderPush) {
            leftIndex = 0;
            rightIndex = 0;
            for (int i = 0; i < xSmallCoord.length; i++) {
                if (xSmallCoord[i] <= from) {
                    leftIndex = i;
                }

                if (xSmallCoord[i] <= to) {
                    if (i == xSmallCoord.length - 1) {
                        rightIndex = xSmallCoord.length - 1;
                    } else {
                        rightIndex = i + 1;
                    }
                }
            }
            boolean inited = false;
            for (int chartIndex = 0; chartIndex < mChartData.getDataSets().length; chartIndex++) {
                long[] y = mChartData.getDataSets()[chartIndex].getValues();
                for (int i = leftIndex; i <= rightIndex; i++) {
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

        initPoints(type);
    }

    //call only after change select border
    private void initPoints(final int type) {
        initPoints(false, -1, -1, type);
    }

    //call only after change select and select border
    private void initPoints(boolean customExtremum, long min, long max, final int type) {
        points = new float[mChartData.getDataSets().length + 1][];
        float width = getWidth();
        leftIndex = 0;
        rightIndex = 0;
        for (int i = 0; i < xSmallCoord.length; i++) {
            if (xSmallCoord[i] <= from) {
                leftIndex = i;
            }

            if (xSmallCoord[i] <= to) {
                if (i == xSmallCoord.length - 1) {
                    rightIndex = xSmallCoord.length - 1;
                } else {
                    rightIndex = i + 1;
                }
            }
        }

        int countOfPoint = rightIndex - leftIndex + 1;
        if (tmpType != type && type == 0) {
            tmpCount = countOfPoint;
        }
        if (tmpCount != countOfPoint && type == 0) {
            countOfPoint = Math.max(countOfPoint, tmpCount);
            tmpCount = countOfPoint;
            leftIndex = rightIndex - countOfPoint + 1;
            if (leftIndex < 0) {
                leftIndex = 0;
            }
        }
        tmpType = type;

        points[0] = new float[countOfPoint];
        float window = to - from;
        for (int i = 0; i < countOfPoint; i++) {
            points[0][i] = width * (xSmallCoord[i + leftIndex] - from) / window;
        }

        xTimeCoord = new float[PONT_COUNT];
        int step = countOfPoint / PONT_COUNT;
        int leftIndexTime = 0;
        int rightIndexTime = 0;
//        Log.d("TAG", "count: " + countOfPoint + " step: " + step + " length: " + xTimeCoord.length);
//        for (int i = 0; i < xSmallCoord.length; i++) {
//            if (i % (step + 1) == 0) {
//                Log.d("TAG", "potential point: " + xSmallCoord[i] + " i: " + i);
//                if (xSmallCoord[i] <= from) {
//                    leftIndexTime = i;
//                }
//                if (xSmallCoord[i] <= to) {
//                    if (i == xSmallCoord.length - 1) {
//                        rightIndexTime = xSmallCoord.length - 1;
//                    } else {
//                        rightIndexTime = i + 1;
//                    }
//                }
//            }
//        }
//        Log.d("TAG", "from: " + from + " to: " + to);
//        Log.d("TAG", "left index of time: " + leftIndexTime + " right index time: " + rightIndexTime);
//        Log.d("TAG", "valueLeft: " + xSmallCoord[leftIndexTime] + " value right: " + xSmallCoord[rightIndexTime]);
        boolean needSmall = false;
        for (int i = 0; i < xSmallCoord.length / (step + 1); i++) {
            int index = xSmallCoord.length - 1 - (i * (step + 1));
            Log.d("TAG", "Stepped xCoord value: " + xSmallCoord[index] + " index: " + index + " i: " + i);
            if (xSmallCoord[index] >= from) {
                leftIndexTime = index - (step + 1);
                if (leftIndexTime < 0) {
                    needSmall = true;
                    leftIndexTime = step + 1;
                }
            }
            if (xSmallCoord[index] >= to) {
                rightIndexTime = index;
            }
        }

        if (needSmall)
            xTimeCoord = new float[xTimeCoord.length - 1];

        Log.d("TAG", "NEW from: " + from + " to: " + to);
        Log.d("TAG", "left index of time: " + leftIndexTime + " right index time: " + rightIndexTime);
        Log.d("TAG", "valueLeft: " + xSmallCoord[leftIndexTime] + " value right: " + xSmallCoord[rightIndexTime]);
        Log.d("TAG", "len:" + xTimeCoord.length);
        for (int i = xTimeCoord.length - 1; i >= 0; i--) {
            Log.d("TAG", "i: " + i + " xSmall index: " + (i * (step + 1) + leftIndexTime) + " value: " + xSmallCoord[i * (step + 1) + leftIndexTime]);
            xTimeCoord[i] = width * (xSmallCoord[i * (step + 1) + leftIndexTime] - from) / window;
        }
//        for (int i = 0; i < xTimeCoord.length; i++) {
//            Log.d("TAG", "i: " + i + " xSmall index: " + (i * (step + 1) + leftIndexTime) + " value: " + xSmallCoord[i * (step + 1) + leftIndexTime]);
//            xTimeCoord[i] = width * (xSmallCoord[i * (step + 1) + leftIndexTime] - from) / window;
//        }
//        for (int i = leftIndexTime; i < rightIndexTime; i++) {
//            if (i % (step + 1) == 0) {
//                Log.d("TAG", "i: " + i + " step + 1 " + (step + 1));
//                Log.d("TAG", "point in border for time: " + xSmallCoord[i] + " for i: " + i + " counter: " + counter);
//                xTimeCoord[counter] = width * (xSmallCoord[i] - from) / window;
//                counter++;
//            }
//        }
        Log.d("TAG", "Time coord: " + Arrays.toString(xTimeCoord));

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
                for (int i = leftIndex; i <= rightIndex; i++) {
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

        long delta = currentYMax - currentYMin;
        for (int chartIndex = 0; chartIndex < mChartData.getDataSets().length; chartIndex++) {
            points[chartIndex + 1] = new float[countOfPoint];
            long[] y = mChartData.getDataSets()[chartIndex].getValues();
            for (int i = leftIndex; i <= rightIndex; i++) {
                points[chartIndex + 1][i - leftIndex] = (y[i] - currentYMin) * borderedHeight / delta;
            }
        }

        postInvalidateOnAnimation();
    }

    public void initData(ChartData chartData, boolean[] selectedCharts) {
        mChartData = chartData;
        mSelectedCharts = selectedCharts.clone();
        preparedDateFormats = new String[mChartData.getX().getValues().length];
        Date date = new Date();
        final long[] x = mChartData.getX().getValues();
        for (int i = 0; i < preparedDateFormats.length; i++) {
            date.setTime(x[i]);
            preparedDateFormats[i] = format.format(date);
        }

        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                removeOnLayoutChangeListener(this);
                float step = (float) getWidth() / (float) (x.length - 1);
                xSmallCoord = new float[x.length];
                for (int i = 0; i < x.length; i++) {
                    xSmallCoord[i] = i * step;
                }
            }
        });

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
        leftIndex = 0;
        rightIndex = 0;
        for (int i = 0; i < xSmallCoord.length; i++) {
            if (xSmallCoord[i] <= from) {
                leftIndex = i;
            }

            if (xSmallCoord[i] <= to) {
                if (i == xSmallCoord.length - 1) {
                    rightIndex = xSmallCoord.length - 1;
                } else {
                    rightIndex = i + 1;
                }
            }
        }
        for (int chartIndex = 0; chartIndex < mChartData.getDataSets().length; chartIndex++) {
            if (!mSelectedCharts[chartIndex]) {
                continue;
            }
            long[] y = mChartData.getDataSets()[chartIndex].getValues();
            for (int i = leftIndex; i <= rightIndex; i++) {
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
                    initPoints(true, ((Float) animation.getAnimatedValue()).longValue(), value, 0);
                }
            });

            max.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    long value = ((Float) min.getAnimatedValue()).longValue();
                    if (value == 0)
                        value = currentMin;
                    initPoints(true, value, ((Float) animation.getAnimatedValue()).longValue(), 0);
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
