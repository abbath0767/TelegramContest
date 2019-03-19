package com.ng.telegramcontest.ui.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
    private long currentMax;
    private long currentMin;
    private boolean dataIsInit = false;
    private boolean firstBorderPush = true;
    float[][] points;

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
//        float bottomBorderY = getHeight() - getHeight() / 10.0f;
        float topBorderY = getHeight() - getHeight() * 0.9f;
        float leftBorder = (getWidth() / 2) + 16 * density;
        canvas.drawText(FOLLOWERS, leftBorder, topBorderY, mTextPaint);
//        canvas.drawLine(0, bottomBorderY, getWidth(), bottomBorderY, mSeparatorPaint);

        if (!dataIsInit) {
            super.onDraw(canvas);
            return;
        }

        drawLines(canvas);

        super.onDraw(canvas);
    }

    private void drawLines(Canvas canvas) {
        float prevX = 0f;
        float prevY = 0f;
        for (int i = 0; i < points[0].length; i++) {
            if (i == 0) {
                prevX = points[0][i];
                prevY = getHeight() - points[1][i];
            } else {
                canvas.drawLine(prevX, prevY, points[0][i], getHeight() - points[1][i], mLinePaint);
                prevX = points[0][i];
                prevY = getHeight() - points[1][i];
            }
        }
    }

    public void pushBorderChange(final int fromX, final int toX, final int type) {
        if (mChartData == null)
            return;

        from = fromX;
        to = toX;

        initPoints();
        postInvalidateOnAnimation();
    }

    private void initPoints() {
        points = new float[mChartData.getDataSets().length + 1][];
        int len = mChartData.getX().getValues().length;
        long[] x = mChartData.getX().getValues();
        int xStart = Math.round(from * len / getWidth());
        int xEnd = Math.round(to * len / getWidth());
        int width = getWidth();

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

        int height = getHeight();
        points[1] = new float[xEnd - xStart + 1];
        long[] y = mChartData.getDataSets()[0].getValues();
        long currentYMin = 0;
        long currentYMax = 0;
        for (int i = xStart; i <= xEnd; i++) {
            if (i == xStart) {
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
        delta = currentYMax - currentYMin;
        for (int point = xStart; point <= xEnd; point++) {
            points[1][point - xStart] = (y[point] - currentYMin) * height / delta;
        }
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
    }

    public void changeSelect(boolean[] selectedCharts) {
        Log.d("TAG", "BIG GRAPH. change select");
    }
}
