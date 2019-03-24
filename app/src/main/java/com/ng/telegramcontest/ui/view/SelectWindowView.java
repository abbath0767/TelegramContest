package com.ng.telegramcontest.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.ng.telegramcontest.R;
import com.ng.telegramcontest.data.DataSet;

public class SelectWindowView extends RelativeLayout {

    public SelectWindowView(Context context) {
        this(context, null);
    }

    public SelectWindowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SelectWindowView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SelectWindowView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflateView(context);
    }

    private View leftBorder;
    private View rightBorder;
    private View window;
    private View leftBorderTouch;
    private View rightBorderTouch;
    private Border currentBorder;

    private float tmpLeft = -1f;
    private float tmpRight = -1f;

    private DataSet x;

    private BorderChangeListener listener;

    private void inflateView(Context context) {
        View.inflate(context, R.layout.view_window_selector, this);
        leftBorder = findViewById(R.id.left_border);
        rightBorder = findViewById(R.id.right_border);
        window = findViewById(R.id.window);
        leftBorderTouch = findViewById(R.id.left_border_touch);
        rightBorderTouch = findViewById(R.id.right_border_touch);

        window.setOnTouchListener(new OnTouchListener() {
            float tmpX = 0f;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getRawX();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        tmpX = x;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float delta = tmpX - x;

                        RelativeLayout.LayoutParams paramsLeft = (RelativeLayout.LayoutParams) leftBorder.getLayoutParams();
                        RelativeLayout.LayoutParams paramsRight = (RelativeLayout.LayoutParams) rightBorder.getLayoutParams();
                        float toLeftValue = paramsLeft.width - delta;

                        if (toLeftValue <= 0) {
                            toLeftValue = 0;
                        }

                        if (toLeftValue + window.getWidth() >= getWidth()) {
                            toLeftValue = getWidth() - window.getWidth();
                            paramsRight.width = 0;
                            rightBorder.setLayoutParams(paramsRight);
                            paramsLeft.width = Math.round(toLeftValue);
                            leftBorder.setLayoutParams(paramsLeft);
                        } else {
                            paramsRight.width = getWidth() - Math.round(toLeftValue) - window.getWidth();
                            rightBorder.setLayoutParams(paramsRight);
                            paramsLeft.width = (int) toLeftValue;
                            leftBorder.setLayoutParams(paramsLeft);
                        }

                        tmpX = x;
                        tmpLeft = toLeftValue;
                        tmpRight = toLeftValue + window.getWidth();
                        pushChangeBorder(tmpLeft, tmpRight, 0);

                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        tmpX = 0;
                        return true;
                }
                return false;
            }
        });
        leftBorderTouch.setOnTouchListener(new OnTouchListener() {
            float tmpX = 0f;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getRawX();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        tmpX = x;
                        return true;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        float delta = tmpX - x;

                        RelativeLayout.LayoutParams paramsLeft = (RelativeLayout.LayoutParams) leftBorder.getLayoutParams();
                        RelativeLayout.LayoutParams paramsWindow = (RelativeLayout.LayoutParams) window.getLayoutParams();
                        float toLeftValue = paramsLeft.width - delta;
                        float windowWidth = paramsWindow.width + delta;

                        if (toLeftValue <= 0) {
                            toLeftValue = 0;
                        }

                        if ((toLeftValue + rightBorder.getWidth()) > getWidth() * 0.9) {
                            tmpX = x;
                            return true;
                        } else {
                            paramsLeft.width = Math.round(toLeftValue);
                            leftBorder.setLayoutParams(paramsLeft);
                            paramsWindow.width = (int) windowWidth;
                            window.setLayoutParams(paramsWindow);
                        }

                        tmpX = x;
                        tmpLeft = toLeftValue;
                        if (tmpRight != -1f)
                            pushChangeBorder(toLeftValue, tmpRight, 1);
                        else
                            pushChangeBorder(toLeftValue, getWidth() - rightBorder.getWidth(), 1);

                        return true;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        tmpX = 0;
                        return true;
                    }
                }

                return false;
            }
        });
        rightBorderTouch.setOnTouchListener(new OnTouchListener() {
            float tmpX = 0f;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getRawX();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        tmpX = x;
                        return true;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        float delta = tmpX - x;

                        RelativeLayout.LayoutParams paramsRight = (RelativeLayout.LayoutParams) rightBorder.getLayoutParams();
                        RelativeLayout.LayoutParams paramsWindow = (RelativeLayout.LayoutParams) window.getLayoutParams();
                        float toRightValue = paramsRight.width + delta;
                        float windowWidth = paramsWindow.width - delta;

                        if (toRightValue <= 0) {
                            toRightValue = 0;
                        }

                        if ((toRightValue + leftBorder.getWidth()) > getWidth() * 0.9) {
                            tmpX = x;
                            return true;
                        } else {
                            paramsRight.width = Math.round(toRightValue);
                            rightBorder.setLayoutParams(paramsRight);
                            paramsWindow.width = (int) windowWidth;
                            window.setLayoutParams(paramsWindow);
                        }

                        tmpX = x;
                        tmpRight = getWidth() - rightBorder.getWidth();
                        if (tmpLeft != -1f)
                            pushChangeBorder(tmpLeft, getWidth() - rightBorder.getWidth(), 1);
                        else
                            pushChangeBorder(getWidth() - leftBorder.getWidth(), getWidth() - rightBorder.getWidth(), 1);

                        return true;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        tmpX = 0;
                        return true;
                    }
                }

                return false;
            }
        });

        window.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                window.removeOnLayoutChangeListener(this);
                pushChangeBorder(0);
            }
        });
    }

    public void setNightMode(boolean isNightTheme) {
        if (isNightTheme) {
            leftBorder.setBackgroundColor(getResources().getColor(R.color.colorBorderNight));
            rightBorder.setBackgroundColor(getResources().getColor(R.color.colorBorderNight));
        } else {
            leftBorder.setBackgroundColor(getResources().getColor(R.color.colorBorer));
            rightBorder.setBackgroundColor(getResources().getColor(R.color.colorBorer));
        }
    }

    public void initData(final DataSet x) {
        this.x = x;

        postDelayed(new Runnable() {
            @Override
            public void run() {
                int size = x.getValues().length;
                int fromX = (window.getLeft() * size / getWidth());
                int toX = (window.getRight() * size / getWidth());
                currentBorder = new Border(fromX, toX);
            }
        }, 1);
    }

    private void pushChangeBorder(float from, float to, int type) {
        if (listener != null) {
            listener.onBorderChange(from, to, type);
        }
    }

    private void pushChangeBorder(int type) {
        if (listener != null) {
            listener.onBorderChange(window.getLeft(), window.getRight(), type);
        }
    }

    public void addOnBorderChangeListener(BorderChangeListener listener) {
        this.listener = listener;
        if (currentBorder != null && listener != null) {
            listener.onBorderChange(currentBorder.fromX, currentBorder.toX, 0);
        }
    }

    //from: x index from, to: x index to
    public static class Border {
        final int fromX;
        final int toX;

        public Border(int from, int to) {
            this.fromX = from;
            this.toX = to;
        }

        @Override
        public String toString() {
            return "Border{" +
                    "fromX index=" + fromX +
                    ", toX index=" + toX +
                    '}';
        }
    }

    public interface BorderChangeListener {
        void onBorderChange(float fromX, float toX, int type);
    }
}
