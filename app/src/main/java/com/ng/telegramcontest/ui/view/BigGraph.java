package com.ng.telegramcontest.ui.view;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.ng.telegramcontest.R;
import com.ng.telegramcontest.data.ChartData;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BigGraph extends View {

    private final static String FOLLOWERS = "Followers";
    private final static int PONT_COUNT = 7;
    private final static SimpleDateFormat format = new SimpleDateFormat("MMM dd");
    private final static SimpleDateFormat formatDetail = new SimpleDateFormat("EEE, MMM dd");

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
    private Paint mTextPaintDate;
    private Paint mTextPaintCount;
    private Paint mTextPaintName;
    private Paint mSeparatorPaint;
    private Paint mDatePaint;
    private Paint mLinePaint;
    private Paint mPointInnerPaint;
    private Paint mDetailPaint;
    private Paint mShadowPaint;
    private ChartData mChartData;
    private String[] preparedDateFormats;
    private int[] preparedDateFormatsIndexes;
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
    private long drawMex;
    private long drawMin;
    private boolean dataIsInit = false;
    private boolean firstBorderPush = true;
    private float[][] points;
    private float[] detailsPoints;
    private float bottomBorderY = 0f;
    private float bottomDateY = 0f;
    private float topBorderY = 0f;
    private float altTopBorderY = 0f;
    private float borderedHeight = 0f;

    private float[] xSmallCoord;
    private float[] xTimeCoord;
    private int tmpCount = 0;
    private int tmpType = -1;
    private int leftIndex = 0;
    private int rightIndex = 0;
    private float valueStep = 0f;
    private float topValue = 0f;

    private float selectedValue = -1f;
    private int selectedIndex = -1;
    private boolean deatilIsShow = false;

    private float marginHorizontal = 0f;
    private float marginVertical = 0f;

    //todo test
    private Paint testPaint;

    private float density = getResources().getDisplayMetrics().density;

    private void initPaints() {
        mTextPaint = new Paint();
        mTextPaint.setColor(getContext().getResources().getColor(R.color.colorPrimary));
        mTextPaint.setStyle(Paint.Style.FILL);
        float scaledSizeInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics());
        mTextPaint.setTextSize(scaledSizeInPixels);
        Typeface bold = Typeface.create("sans-serif-medium", Typeface.NORMAL);
        mTextPaint.setTypeface(bold);

        mTextPaintDate = new Paint();
        mTextPaintDate.setColor(Color.BLACK);
        mTextPaintDate.setStyle(Paint.Style.FILL);
        scaledSizeInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics());
        mTextPaintDate.setTextSize(scaledSizeInPixels);
        mTextPaint.setAntiAlias(true);

        mTextPaintCount = new Paint();
        mTextPaintCount.setStyle(Paint.Style.FILL);
        scaledSizeInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics());
        mTextPaintCount.setTextSize(scaledSizeInPixels);
        mTextPaintCount.setAntiAlias(true);

        mTextPaintName = new Paint();
        mTextPaintName.setStyle(Paint.Style.FILL);
        scaledSizeInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics());
        mTextPaintName.setTextSize(scaledSizeInPixels);
        mTextPaintName.setAntiAlias(true);

        mDatePaint = new Paint();
        mDatePaint.setColor(getContext().getResources().getColor(R.color.colorDateDay));
        mDatePaint.setStyle(Paint.Style.FILL);
        scaledSizeInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics());
        mDatePaint.setTextSize(scaledSizeInPixels);

        mLinePaint = new Paint();
        mLinePaint.setStrokeWidth(getContext().getResources().getDimension(R.dimen.common_2));
        mLinePaint.setColor(Color.RED);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);
        setLayerType(LAYER_TYPE_HARDWARE, mLinePaint);

        mDetailPaint = new Paint();
        mDetailPaint.setColor(Color.parseColor("#fafafa"));
        mDetailPaint.setAntiAlias(true);
        setLayerType(LAYER_TYPE_HARDWARE, mDetailPaint);

        mShadowPaint = new Paint();
        mShadowPaint.setColor(getContext().getResources().getColor(R.color.colorSeparatorDay));
        mShadowPaint.setAntiAlias(true);
        setLayerType(LAYER_TYPE_HARDWARE, mShadowPaint);

        mPointInnerPaint = new Paint();
        mPointInnerPaint.setColor(Color.WHITE);
        mPointInnerPaint.setAntiAlias(true);
        mPointInnerPaint.setStyle(Paint.Style.FILL);
        setLayerType(LAYER_TYPE_HARDWARE, mPointInnerPaint);

        mSeparatorPaint = new Paint();
        mSeparatorPaint.setColor(getContext().getResources().getColor(R.color.colorSeparatorDay));
        mSeparatorPaint.setStrokeWidth(getContext().getResources().getDimension(R.dimen.common_1));

        testPaint = new Paint();
        testPaint.setStrokeWidth(getContext().getResources().getDimension(R.dimen.common_2));
        testPaint.setColor(Color.BLUE);

        xTimeCoord = new float[PONT_COUNT];
        preparedDateFormatsIndexes = new int[PONT_COUNT];

        marginHorizontal = 8 * density;
        marginVertical = 6 * density;

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                detailsPoints = new float[mChartData.getDataSets().length];

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        if (!deatilIsShow) {
                            deatilIsShow = true;
                        } else {
                            selectedIndex = -1;
                            selectedValue = -1f;
                            deatilIsShow = false;
                            postInvalidateOnAnimation();
                            return true;
                        }

                        int leftIndex = -1;
                        for (int i = 0; i < points[0].length; i++) {
                            if (points[0][i] <= x) {
                                leftIndex = i;
                            }
                        }
                        int rightIndex = leftIndex + 1;
                        if (rightIndex >= points[0].length)
                            rightIndex = leftIndex;
                        float leftValue = points[0][leftIndex];
                        float rightValue = points[0][rightIndex];
                        float deltaLeft = x - leftValue;
                        float deltaRight = rightValue - x;
                        if (deltaLeft < deltaRight) {
                            selectedIndex = leftIndex;
                        } else {
                            selectedIndex = rightIndex;
                        }

                        selectedValue = points[0][selectedIndex];
                        for (int i = 0; i < mChartData.getDataSets().length; i++) {
                            detailsPoints[i] = bottomBorderY - points[i + 1][selectedIndex];
                        }

                        postInvalidateOnAnimation();
                        return true;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        if (deatilIsShow) {
                            if (x > getWidth())
                                x = getWidth();

                            int leftIndex = -1;
                            for (int i = 0; i < points[0].length; i++) {
                                if (points[0][i] <= x) {
                                    leftIndex = i;
                                }
                            }
                            int rightIndex = leftIndex + 1;
                            if (rightIndex >= points[0].length)
                                rightIndex = leftIndex;
                            if (leftIndex < 0)
                                leftIndex = 0;
                            float leftValue = points[0][leftIndex];
                            float rightValue = points[0][rightIndex];
                            float deltaLeft = x - leftValue;
                            float deltaRight = rightValue - x;
                            if (deltaLeft < deltaRight) {
                                selectedIndex = leftIndex;
                            } else {
                                selectedIndex = rightIndex;
                            }

                            selectedValue = points[0][selectedIndex];
                            for (int i = 0; i < mChartData.getDataSets().length; i++) {
                                detailsPoints[i] = bottomBorderY - points[i + 1][selectedIndex];
                            }

                            postInvalidateOnAnimation();
                        }

                        return true;
                    }
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {


                        postInvalidateOnAnimation();
                        return true;
                    }
                }

                return false;
            }
        });
    }

    public void setIsNightMode(boolean isNightTheme) {
        if (isNightTheme) {
            mTextPaint.setColor(getContext().getResources().getColor(R.color.colorFollowNight));
            mPointInnerPaint.setColor(getResources().getColor(R.color.colorPrimaryNight));
            mTextPaintDate.setColor(Color.WHITE);
            mDetailPaint.setColor(getResources().getColor(R.color.colorPrimaryNight));
            mSeparatorPaint.setColor(getContext().getResources().getColor(R.color.colorPrimaryDarkNight));
            mShadowPaint.setColor(getContext().getResources().getColor(R.color.colorPrimaryDarkNight));
            mDatePaint.setColor(getContext().getResources().getColor(R.color.colorDateNight));
        } else {
            mTextPaint.setColor(getContext().getResources().getColor(R.color.colorPrimary));
            mPointInnerPaint.setColor(Color.WHITE);
            mTextPaintDate.setColor(Color.BLACK);
            mDetailPaint.setColor(getResources().getColor(R.color.defaultBackColor));
            mSeparatorPaint.setColor(getContext().getResources().getColor(R.color.colorSeparatorDay));
            mShadowPaint.setColor(getContext().getResources().getColor(R.color.colorSeparatorDay));
            mDatePaint.setColor(getContext().getResources().getColor(R.color.colorDateDay));
        }

        postInvalidateOnAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawText(FOLLOWERS, 0, topBorderY, mTextPaint);

        if (!dataIsInit) {
            super.onDraw(canvas);
            return;
        }

        if (points != null) {
            drawLines(canvas);
        }

        if (deatilIsShow) {
            drawDetail(canvas);
        }

        super.onDraw(canvas);
    }

    private void drawDetail(Canvas canvas) {
        canvas.drawLine(selectedValue, bottomBorderY, selectedValue, topBorderY, mSeparatorPaint);
        for (int chartIndex = 1; chartIndex < points.length; chartIndex++) {
            if (!mSelectedCharts[chartIndex - 1]) {
                continue;
            }
            float y = bottomBorderY - points[chartIndex][selectedIndex];
            mLinePaint.setColor(Color.parseColor(mChartData.getDataSets()[chartIndex - 1].getColor()));
            canvas.drawCircle(selectedValue, y, 13, mLinePaint);
            canvas.drawCircle(selectedValue, y, 10, mPointInnerPaint);
        }

        String dateText = formatDetail.format(new Date(mChartData.getX().getValues()[selectedIndex + leftIndex]));
        Rect textRectDate = new Rect();
        mTextPaintDate.getTextBounds(dateText, 0, dateText.length(), textRectDate);

        float addHorizontal = 0f;
        float addVertical = 0f;

        float widthDate = textRectDate.width() + marginHorizontal * 2;
        float heightDate = textRectDate.height() + marginVertical * 2;

        float horizontalDeltaPercent = selectedValue / getWidth();

        int counter = 0;
        float lineWidth = 0f;
        float tmpWidth = 0f;
        float maxLineWidth = 0f;
        float tmpHeight = 0f;
        for (int i = 0; i < mSelectedCharts.length; i++) {
            if (mSelectedCharts[i]) {
                counter++;
                long value = mChartData.getDataSets()[i].getValues()[selectedIndex + leftIndex];
                String text = String.valueOf(value);
                String title = mChartData.getDataSets()[i].getEntityName();
                Rect countRect = new Rect();
                Rect titleRect = new Rect();
                mTextPaintCount.getTextBounds(text, 0, text.length(), countRect);
                mTextPaintName.getTextBounds(title, 0, title.length(), titleRect);
                if (counter % 2 != 0) {
                    lineWidth += Math.max(marginHorizontal * 2f + countRect.width(), marginHorizontal * 2f + titleRect.width());
                    tmpHeight += countRect.height() + marginVertical * 4f + titleRect.height();
                } else {
                    lineWidth += Math.max(marginHorizontal * 2f + countRect.width(), marginVertical * 2f + titleRect.width());
                    maxLineWidth = Math.max(tmpWidth, lineWidth);
                    tmpWidth = lineWidth;
                    lineWidth = 0f;
                }
            }
        }

        //todo test
        addHorizontal = Math.max(widthDate, maxLineWidth);
        addVertical = heightDate + tmpHeight;

        float left = points[0][selectedIndex] - addHorizontal * horizontalDeltaPercent;
        float top = topBorderY;
        float right = points[0][selectedIndex] + addHorizontal - addHorizontal * horizontalDeltaPercent;
        float bottom = topBorderY + addVertical;

        RectF rectS = new RectF(left - 3, top - 3, right + 3, bottom + 7);
        RectF rect = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(rectS, 15, 15, mShadowPaint);
        canvas.drawRoundRect(rect, 10, 10, mDetailPaint);
        canvas.drawText(dateText, left + marginHorizontal, top + marginVertical + textRectDate.height(), mTextPaintDate);
        counter = 0;
        float tmpLeft = 0f;
        tmpHeight = 0f;
        for (int i = 0; i < mSelectedCharts.length; i++) {
            if (mSelectedCharts[i]) {
                mTextPaintCount.setColor(Color.parseColor(mChartData.getDataSets()[i].getColor()));
                mTextPaintName.setColor(Color.parseColor(mChartData.getDataSets()[i].getColor()));
                long value = mChartData.getDataSets()[i].getValues()[selectedIndex + leftIndex];
                String text = String.valueOf(value);
                String title = mChartData.getDataSets()[i].getEntityName();
                Rect countRect = new Rect();
                Rect titleRect = new Rect();
                mTextPaintCount.getTextBounds(text, 0, text.length(), countRect);
                mTextPaintName.getTextBounds(title, 0, title.length(), titleRect);
                if (counter % 2 == 0) {
                    canvas.drawText(text, left + marginHorizontal, top + marginVertical * 3f + textRectDate.height() + countRect.height() + tmpHeight, mTextPaintCount);
                    canvas.drawText(title, left + marginHorizontal, top + marginVertical * 4f + textRectDate.height() + countRect.height() + tmpHeight + titleRect.height(), mTextPaintName);
                    tmpLeft = marginHorizontal * 2f + Math.max(countRect.width(), titleRect.width());
                } else {
                    canvas.drawText(text, left + marginHorizontal + tmpLeft, top + marginVertical * 3f + textRectDate.height() + countRect.height() + tmpHeight, mTextPaintCount);
                    canvas.drawText(title, left + marginHorizontal + tmpLeft, top + marginVertical * 4f + textRectDate.height() + countRect.height() + tmpHeight + titleRect.height(), mTextPaintName);
                    tmpLeft = 0f;
                    tmpHeight += marginVertical * 4f + countRect.height() + titleRect.height();
                }
                counter++;
            }
        }
    }

    private void drawLines(Canvas canvas) {
        float textWidth = mDatePaint.measureText(preparedDateFormats[0]);
        for (int i = 0; i < xTimeCoord.length; i++) {
            canvas.drawText(preparedDateFormats[preparedDateFormatsIndexes[i]], xTimeCoord[i] - textWidth / 2f, bottomDateY, mDatePaint);
        }

        for (int i = 0; i < 6; i++) {
            canvas.drawLine(0, bottomBorderY - valueStep * i, getWidth(), bottomBorderY - valueStep * i, mSeparatorPaint);
        }

        CornerPathEffect cornerPathEffect = new CornerPathEffect(0.5f);
        mLinePaint.setPathEffect(cornerPathEffect);
        for (int chartIndex = 0; chartIndex < mChartData.getDataSets().length; chartIndex++) {
            mLinePaint.setColor(Color.parseColor(mChartData.getDataSets()[chartIndex].getColor()));
            mLinePaint.setAlpha(getAlphaFor(chartIndex));
            Path p = new Path();
            p.moveTo(points[0][0], bottomBorderY - points[chartIndex + 1][0]);
            for (int i = 1; i < points[0].length; i++) {
                p.lineTo(points[0][i], bottomBorderY - points[chartIndex + 1][i]);
            }
            canvas.drawPath(p, mLinePaint);
        }

        for (int i = 0; i < 6; i++) {
            String text = String.valueOf(Math.round(((valueStep * i) * (drawMex - drawMin) / topValue) + drawMin));
            if (text.equals("-1"))
                continue;
            canvas.drawText(text, 0f, (bottomBorderY - valueStep * i) - 10, mDatePaint);
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

        if (deatilIsShow) {
            selectedIndex = -1;
            selectedValue = -1f;
            deatilIsShow = false;
        }

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

        int step = Math.round(countOfPoint / (float) (PONT_COUNT - 2));
        int leftIndexTime = 0;
        int rightIndexTime = 0;
        for (int i = 0; i < xSmallCoord.length / step; i++) {
            int index = xSmallCoord.length - i * step - 1;
            if (xSmallCoord[index] >= from) {
                leftIndexTime = index - step;
                if (leftIndexTime < 0) {
                    leftIndexTime = 0;
                }
            }
            if (xSmallCoord[index] >= to) {
                rightIndexTime = index;
            }
        }

        for (int i = xTimeCoord.length; i > 0; i--) {
            int index = rightIndexTime - (xTimeCoord.length - i) * step;
            preparedDateFormatsIndexes[i - 1] = index;
            if (index < 0) {
                xTimeCoord[i - 1] = xTimeCoord[i] - (xTimeCoord[i + 1] - xTimeCoord[i]);
                preparedDateFormatsIndexes[i - 1] = 0;
                continue;
            }
            xTimeCoord[i - 1] = width * (xSmallCoord[index] - from) / window;
        }

        boolean inited = false;
        long currentYMin = 0;
        long currentYMax = 0;
        if (customExtremum) {
            currentYMin = min;
            currentYMax = max;
            drawMin = min;
            drawMex = max;
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

            drawMin = currentYMin;
            drawMex = currentYMax;
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
        bottomDateY = height - (height - bottomBorderY) / 2f;
        topBorderY = height * 0.1f;
        altTopBorderY = topBorderY * 2f;
        borderedHeight = bottomBorderY - topBorderY;
        valueStep = (bottomBorderY - altTopBorderY) / 5;
        topValue = height - topBorderY - (height - bottomBorderY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void changeSelect(boolean[] selectedCharts) {
        if (deatilIsShow) {
            selectedIndex = -1;
            selectedValue = -1f;
            deatilIsShow = false;
        }

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
