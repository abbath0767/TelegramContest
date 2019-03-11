package com.ng.telegramcontest.ui;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.ng.telegramcontest.R;
import com.ng.telegramcontest.data.ChartData;
import com.ng.telegramcontest.data.DataSet;

import static com.ng.telegramcontest.util.CalculateHelper.getCordValues;
import static com.ng.telegramcontest.util.CalculateHelper.getPrepared;

public class SmallGraph extends View implements OnTaskExecuted {

    public SmallGraph(Context context) {
        this(context, null);
    }

    public SmallGraph(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmallGraph(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SmallGraph(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private int lastAdded = -1;
    private int lastRemoved = -1;
    private Paint mPaint = new Paint();
    private ChartData mChartData;
    private boolean[] selectedCharts;
    private long[][][] points;
    private ValueAnimator alphaAnimator;
    private FirstCalculateAsyncTask mFirstCalculateAsyncTask;
    private long currentMaxY = 0;
    private long currentMinY = 0;

    {
        mPaint.setStrokeWidth(getContext().getResources().getDimension(R.dimen.common_2));
    }

    @Override
    public void doOnExecutedInit(long[][][] points) {
        this.points = points;
        postInvalidateOnAnimation();
    }

    @SuppressLint("LogNotTimber")
    @Override
    protected void onDraw(Canvas canvas) {
        if (points == null) {
            super.onDraw(canvas);
            return;
        }

        for (int chartIndex = 0; chartIndex < points.length; chartIndex++) {

//            boolean needDraw = selectedCharts[chartIndex];
//            if (!needDraw) {
//                continue;
//            }

            mPaint.setColor(Color.parseColor(mChartData.getDataSets()[chartIndex].getColor()));
            mPaint.setAlpha(getAlphaFor(chartIndex));
            float previousX = 0;
            float previousY = 0;

            for (int pointIndex = 0; pointIndex < points[chartIndex].length; pointIndex++) {
                if (pointIndex == 0) {
                    previousX = points[chartIndex][pointIndex][0];
                    previousY = getHeight() - points[chartIndex][pointIndex][1];
                } else {
                    canvas.drawLine(previousX, previousY, points[chartIndex][pointIndex][0], getHeight() - points[chartIndex][pointIndex][1], mPaint);
                    previousX = points[chartIndex][pointIndex][0];
                    previousY = getHeight() - points[chartIndex][pointIndex][1];
                }
            }
        }

        super.onDraw(canvas);
    }

    public void initData(final ChartData chartData, boolean[] selectedCharts) {
        this.mChartData = chartData;
        this.selectedCharts = selectedCharts.clone();
        currentMaxY = chartData.getMaxY();
        currentMinY = chartData.getMinY();

        postDelayed(new Runnable() {
            @Override
            public void run() {
                mFirstCalculateAsyncTask = new FirstCalculateAsyncTask(getWidth(), getHeight(), chartData, SmallGraph.this);
                mFirstCalculateAsyncTask.execute();
            }
        }, 1);
    }

    public void changeSelect(boolean[] selectedCharts) {
        Log.d("TAG", "Change select");
        int oldCount = 0;
        int newCount = 0;
        for (boolean show : this.selectedCharts) {
            if (show) {
                oldCount++;
            }
        }

        for (boolean show : selectedCharts) {
            if (show) {
                newCount++;
            }
        }

        boolean added = newCount > oldCount;
        if (added) {
            lastRemoved = -1;
            for (int i = 0; i < selectedCharts.length; i++) {
                if (selectedCharts[i] && !this.selectedCharts[i]) {
                    lastAdded = i;
                    break;
                }
            }
        } else {
            lastAdded = -1;
            for (int i = 0; i < selectedCharts.length; i++) {
                if (!selectedCharts[i] && this.selectedCharts[i]) {
                    lastRemoved = i;
                    break;
                }
            }
        }

        this.selectedCharts = selectedCharts.clone();
        long newMax = mChartData.getMaxYFrom(selectedCharts);
        long newMin = mChartData.getMinYFrom(selectedCharts);

        if ((currentMaxY == -1 || currentMinY == -1) && !added) {
            clearChart();
        } else if (currentMaxY == newMax && currentMinY == newMin) {
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

            final ValueAnimator min = ValueAnimator.ofFloat(currentMinY, newMin);
            final ValueAnimator max = ValueAnimator.ofFloat(currentMaxY, newMax);
            alphaAnimator = ValueAnimator.ofInt(alphaFrom, alphaTo);
            min.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    long value = ((Float) max.getAnimatedValue()).longValue();
                    if (value == 0)
                        value = currentMaxY;
                    recalculateAndUpdateGraph(((Float) animation.getAnimatedValue()).longValue(), value);
                }
            });

            max.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    long value = ((Float) min.getAnimatedValue()).longValue();
                    if (value == 0)
                        value = currentMinY;
                    recalculateAndUpdateGraph(value, ((Float) animation.getAnimatedValue()).longValue());
                }
            });
            AnimatorSet set = new AnimatorSet();
            set.playTogether(min, max, alphaAnimator);
            set.setInterpolator(new DecelerateInterpolator());
            set.start();

            currentMaxY = newMax;
            currentMinY = newMin;
        }
    }

    private int getAlphaFor(int chartIndex) {
        if (chartIndex == lastAdded) {
            return (int) alphaAnimator.getAnimatedValue();
        } else if (chartIndex == lastRemoved) {
            return (int) alphaAnimator.getAnimatedValue();
        } else if (selectedCharts[chartIndex])
            return 255;
        else
            return 0;
    }

    private void recalculateAndUpdateGraph(long currentMinValue, long currentMaxValue) {
        if (currentMaxValue == 0)
            return;

        DataSet[] dataSets = mChartData.getDataSets();
        long[][][] result = new long[dataSets.length][][];
        int count = mChartData.size();
        DataSet x = mChartData.getX();
        //prepare x values for transformation with min value
        long[] preparedX = getPrepared(count, x, x.getMinValue());
        //x values is sorted! find X max
        long preparedXMax = preparedX[count - 1];
        //transform x to screen width
        long[] xValues = getCordValues(count, preparedX, preparedXMax, getWidth());

        long[][] preparedY = new long[dataSets.length][];
        for (int indexChart = 0; indexChart < dataSets.length; indexChart++) {
            preparedY[indexChart] = getPrepared(count, dataSets[indexChart], currentMinValue);
        }

        long[][] yValues = new long[dataSets.length][];
        //transform y values to screen height
        for (int indexChart = 0; indexChart < dataSets.length; indexChart++) {
            yValues[indexChart] = getCordValues(count, preparedY[indexChart], currentMaxValue, getHeight());
        }

        //fill points
        for (int indexChart = 0; indexChart < dataSets.length; indexChart++) {
            result[indexChart] = new long[count][2];

            for (int i = 0; i < count; i++) {
                result[indexChart][i][0] = xValues[i];
                result[indexChart][i][1] = yValues[indexChart][i];
            }
        }

        this.doOnExecutedInit(result);
    }

    private void clearChart() {
        points = null;
        invalidate();
    }

    private static class FirstCalculateAsyncTask extends AsyncTask<Void, Void, Void> {

        private final long width;
        private final long height;
        private final OnTaskExecuted listener;
        private final ChartData chartData;

        FirstCalculateAsyncTask(long width, long height, ChartData chartData, OnTaskExecuted listener) {
            this.width = width;
            this.height = height;
            this.listener = listener;
            this.chartData = chartData;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            DataSet[] dataSets = chartData.getDataSets();
            long[][][] result = new long[dataSets.length][][];
            DataSet x = chartData.getX();
            int count = chartData.size();
            long minY = chartData.getMinY();

            //prepare x values for transformation with min value
            long[] preparedX = getPrepared(count, x, x.getMinValue());
            //x values is sorted! find X max
            long preparedXMax = preparedX[count - 1];
            //transform x to screen width
            long[] xValues = getCordValues(count, preparedX, preparedXMax, width);

            //prepare Y values for transformation with global minimum Y
            long[][] preparedY = new long[dataSets.length][];
            for (int indexChart = 0; indexChart < dataSets.length; indexChart++) {
                preparedY[indexChart] = getPrepared(count, dataSets[indexChart], minY);
            }

            //find y max
            long preparedYMax = -1;
            for (int indexChart = 0; indexChart < dataSets.length; indexChart++) {
                for (int i = 0; i < count; i++) {
                    if (preparedYMax < preparedY[indexChart][i]) {
                        preparedYMax = preparedY[indexChart][i];
                    }
                }
            }

            long[][] yValues = new long[dataSets.length][];
            //transform y values to screen height
            for (int indexChart = 0; indexChart < dataSets.length; indexChart++) {
                yValues[indexChart] = getCordValues(count, preparedY[indexChart], preparedYMax, height);
            }

            //fill points
            for (int indexChart = 0; indexChart < dataSets.length; indexChart++) {
                result[indexChart] = new long[count][2];

                for (int i = 0; i < count; i++) {
                    result[indexChart][i][0] = xValues[i];
                    result[indexChart][i][1] = yValues[indexChart][i];
                }
            }

            listener.doOnExecutedInit(result);
            return null;
        }
    }

//    @Deprecated()
//    private static class AnimateCalculateAsyncTask extends AsyncTask<DataForChange, AnimateCalculateAsyncTask.UpdatePoints, Void> {
//
//        private final long width;
//        private final long height;
//        private final OnTaskExecuted listener;
//        private final ChartData chartData;
//        private boolean[] selectedCharts;
//        private long oldMaxY;
//        private long newMaxY;
//        private long oldMinY;
//        private long newMinY;
//        private static int STEP_COUNT = 30;
//        private long[][][] result;
//        private long[] xValues;
//
//        AnimateCalculateAsyncTask(long width, long height, ChartData chartData, OnTaskExecuted listener) {
//            this.width = width;
//            this.height = height;
//            this.listener = listener;
//            this.chartData = chartData;
//            selectedCharts = new boolean[chartData.getDataSets().length];
//        }
//
//        @Override
//        protected void onPreExecute() {
//            DataSet[] dataSets = chartData.getDataSets();
//            result = new long[dataSets.length][][];
//            DataSet x = chartData.getX();
//            int count = chartData.size();
//
//            //prepare x values for transformation with min value
//            long[] preparedX = getPrepared(count, x, x.getMinValue());
//            //x values is sorted! find X max
//            long preparedXMax = preparedX[count - 1];
//            //transform x to screen width
//            xValues = getCordValues(count, preparedX, preparedXMax, width);
//
//            super.onPreExecute();
//        }
//
//        @Override
//        protected Void doInBackground(DataForChange... data) {
//            fillData(data[0]);
//            DataSet[] dataSets = chartData.getDataSets();
//            result = new long[dataSets.length][][];
//            int count = chartData.size();
//
//            long stepValueMax = (oldMaxY - newMaxY) / STEP_COUNT;
//            long stepValueMin = (oldMinY - newMinY) / STEP_COUNT;
//
//            for (int step = 0; step < STEP_COUNT + 1; step++) {
//                long currentMaxValue;
//                long currentMinValue;
//                if (step == STEP_COUNT) {
//                    currentMaxValue = newMaxY;
//                    currentMinValue = newMinY;
//                } else {
//                    currentMaxValue = oldMaxY - step * stepValueMax;
//                    currentMinValue = oldMinY - step * stepValueMin;
//                }
//
//                long[][] preparedY = new long[dataSets.length][];
//                for (int indexChart = 0; indexChart < dataSets.length; indexChart++) {
//                    preparedY[indexChart] = getPrepared(count, dataSets[indexChart], currentMinValue);
//                }
//
//                long[][] yValues = new long[dataSets.length][];
//                //transform y values to screen height
//                for (int indexChart = 0; indexChart < dataSets.length; indexChart++) {
//                    yValues[indexChart] = getCordValues(count, preparedY[indexChart], currentMaxValue, height);
//                }
//
//                //fill points
//                for (int indexChart = 0; indexChart < dataSets.length; indexChart++) {
//                    result[indexChart] = new long[count][2];
//
//                    for (int i = 0; i < count; i++) {
//                        result[indexChart][i][0] = xValues[i];
//                        result[indexChart][i][1] = yValues[indexChart][i];
//                    }
//                }
//
//                Log.d("TAG", "Push data! " + result.length);
//                publishProgress(new UpdatePoints(result));
//
//                try {
//                    Thread.sleep(3);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
////                listener.doOnExecutedInit(result);
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onProgressUpdate(UpdatePoints... values) {
//            super.onProgressUpdate(values);
//            Log.d("TAG", "on progreess update! " + values[0].points.length);
//            listener.doOnExecutedInit(values[0].points);
//        }
//
//        private void fillData(DataForChange data) {
//            selectedCharts = data.selectedCharts;
//            oldMaxY = data.oldMaxY;
//            newMaxY = data.newMaxY;
//            oldMinY = data.oldMinY;
//            newMinY = data.newMinY;
//        }
//
//
//        class UpdatePoints {
//            final long[][][] points;
//
//            private UpdatePoints(long[][][] points) {
//                this.points = points;
//            }
//        }
//    }

//    private class DataForChange {
//        final boolean[] selectedCharts;
//        final long oldMaxY;
//        final long newMaxY;
//        final long oldMinY;
//        final long newMinY;
//
//        private DataForChange(boolean[] selectedCharts, long oldMaxY, long newMaxY, long oldMinY, long newMinY) {
//            this.selectedCharts = selectedCharts;
//            this.oldMaxY = oldMaxY;
//            this.newMaxY = newMaxY;
//            this.oldMinY = oldMinY;
//            this.newMinY = newMinY;
//        }
//    }
}
