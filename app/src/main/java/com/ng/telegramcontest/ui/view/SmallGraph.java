package com.ng.telegramcontest.ui.view;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.ng.telegramcontest.R;
import com.ng.telegramcontest.data.ChartData;
import com.ng.telegramcontest.data.DataSet;

import java.lang.ref.WeakReference;

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
    private float[][] points;
    private ValueAnimator alphaAnimator;
    private FirstCalculateAsyncTask mFirstCalculateAsyncTask;
    private long currentMaxY = 0;
    private long currentMinY = 0;

    {
        mPaint.setStrokeWidth(getContext().getResources().getDimension(R.dimen.common_1));
    }

    @Override
    public void doOnExecutedInit(float[][] points) {
        this.points = points;
        postInvalidateOnAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (points == null) {
            super.onDraw(canvas);
            return;
        }

        for (int chartIndex = 0; chartIndex < points.length - 1; chartIndex++) {
            mPaint.setColor(Color.parseColor(mChartData.getDataSets()[chartIndex].getColor()));
            mPaint.setAlpha(getAlphaFor(chartIndex));
            float previousX = 0;
            float previousY = 0;

            for (int pointIndex = 0; pointIndex < points[chartIndex].length; pointIndex++) {
                if (pointIndex == 0) {
                    previousX = points[0][pointIndex];
                    previousY = getHeight() - points[chartIndex + 1][pointIndex];
                } else {
                    canvas.drawLine(previousX, previousY, points[0][pointIndex], getHeight() - points[chartIndex + 1][pointIndex], mPaint);
                    previousX = points[0][pointIndex];
                    previousY = getHeight() - points[chartIndex + 1][pointIndex];
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
        DataSet[] dataSets = mChartData.getDataSets();
        float[][] result = new float[dataSets.length + 1][];
        int count = mChartData.size();

        result[0] = new float[count];
        float step = (float) getWidth() / (float) (count - 1);
        for (int i = 0; i < count; i++) {
            result[0][i] = step * i;
        }

        long delta = currentMaxValue - currentMinValue;
        for (int chartIndex = 0; chartIndex < dataSets.length; chartIndex++) {
            result[chartIndex + 1] = new float[count];
            for (int i = 0; i < count; i++) {
                result[chartIndex + 1][i] = ((dataSets[chartIndex].getValues()[i] - currentMinValue)) * getHeight() / delta;
            }
        }

        doOnExecutedInit(result);
    }

    private void clearChart() {
        points = null;
        invalidate();
    }

    //calculated time is 0 ms. mb remove AsyncTask and calculate in main thread?
    private static class FirstCalculateAsyncTask extends AsyncTask<Void, Void, Void> {

        private final long width;
        private final long height;
        private final WeakReference<OnTaskExecuted> listener;
        private final ChartData chartData;

        FirstCalculateAsyncTask(long width, long height, ChartData chartData, OnTaskExecuted listener) {
            this.width = width;
            this.height = height;
            this.listener = new WeakReference<OnTaskExecuted>(listener);
            this.chartData = chartData;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            DataSet[] dataSets = chartData.getDataSets();
            float[][] result = new float[dataSets.length + 1][];
            int count = chartData.size();

            result[0] = new float[count];
            float step = (float) width / (float) (count - 1);
            for (int i = 0; i < count; i++) {
                result[0][i] = step * i;
            }

            long currentYMin = 0;
            long currentYMax = 0;
            for (int chartIndex = 0; chartIndex < dataSets.length; chartIndex++) {
                if (chartIndex == 0) {
                    currentYMin = dataSets[chartIndex].getValues()[0];
                    currentYMax = dataSets[chartIndex].getValues()[0];
                }
                for (int i = 0; i < count; i++) {
                    if (currentYMin > dataSets[chartIndex].getValues()[i]) {
                        currentYMin = dataSets[chartIndex].getValues()[i];
                    }
                    if (currentYMax < dataSets[chartIndex].getValues()[i]) {
                        currentYMax = dataSets[chartIndex].getValues()[i];
                    }
                }
            }

            long delta = currentYMax - currentYMin;
            for (int chartIndex = 0; chartIndex < dataSets.length; chartIndex++) {
                result[chartIndex + 1] = new float[count];
                for (int i = 0; i < count; i++) {
                    result[chartIndex + 1][i] = ((dataSets[chartIndex].getValues()[i] - currentYMin)) * height / delta;
                }
            }

            OnTaskExecuted weak = listener.get();
            if (weak != null) {
                weak.doOnExecutedInit(result);
            }
            return null;
        }
    }
}
