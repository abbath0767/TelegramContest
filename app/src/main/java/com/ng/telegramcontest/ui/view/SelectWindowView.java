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
            int tmpX = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getRawX();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        tmpX = x;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int delta = tmpX - x;

                        RelativeLayout.LayoutParams paramsLeft = (RelativeLayout.LayoutParams) leftBorder.getLayoutParams();
                        RelativeLayout.LayoutParams paramsRight = (RelativeLayout.LayoutParams) rightBorder.getLayoutParams();
                        int toLeftValue = paramsLeft.width - delta;
                        int toRightValue = paramsRight.width + delta;

                        if (toLeftValue <= 0) {
                            toLeftValue = 0;
                        }
                        if (toRightValue <= 0) {
                            toRightValue = 0;
                        }

                        if (toLeftValue != 0 && toRightValue != 0) {
                            paramsRight.width = toRightValue;
                            rightBorder.setLayoutParams(paramsRight);
                            paramsLeft.width = toLeftValue;
                            leftBorder.setLayoutParams(paramsLeft);
                        } else if (toLeftValue == 0) {
                            paramsRight.width = getWidth() - window.getWidth();
                            rightBorder.setLayoutParams(paramsRight);
                            paramsLeft.width = 0;
                            leftBorder.setLayoutParams(paramsLeft);
                        } else {
                            paramsRight.width = 0;
                            rightBorder.setLayoutParams(paramsRight);
                            paramsLeft.width = getWidth() - window.getWidth();
                            leftBorder.setLayoutParams(paramsLeft);
                        }

                        tmpX = x;
                        pushChangeBorder();
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:

                        pushChangeBorder();
                        tmpX = 0;
                        return true;
                }
                return false;
            }
        });
        leftBorderTouch.setOnTouchListener(new OnTouchListener() {
            int tmpX = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getRawX();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        tmpX = x;
                        return true;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        int delta = tmpX - x;

                        RelativeLayout.LayoutParams paramsLeft = (RelativeLayout.LayoutParams) leftBorder.getLayoutParams();
                        RelativeLayout.LayoutParams paramsWindow = (RelativeLayout.LayoutParams) window.getLayoutParams();
                        int toLeftValue = paramsLeft.width - delta;
                        int windowWidth = paramsWindow.width + delta;

                        if (toLeftValue <= 0) {
                            toLeftValue = 0;
                        }

                        if ((toLeftValue + rightBorder.getWidth()) > getWidth() * 0.9) {
                            tmpX = x;
                            return true;
                        }

                        if (toLeftValue != 0) {
                            paramsLeft.width = toLeftValue;
                            leftBorder.setLayoutParams(paramsLeft);
                            paramsWindow.width = windowWidth;
                            window.setLayoutParams(paramsWindow);
                        }

                        tmpX = x;
                        pushChangeBorder();
                        return true;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        pushChangeBorder();
                        tmpX = 0;
                        return true;
                    }
                }

                return false;
            }
        });
        rightBorderTouch.setOnTouchListener(new OnTouchListener() {
            int tmpX = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getRawX();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        tmpX = x;
                        return true;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        int delta = tmpX - x;

                        RelativeLayout.LayoutParams paramsRight = (RelativeLayout.LayoutParams) rightBorder.getLayoutParams();
                        RelativeLayout.LayoutParams paramsWindow = (RelativeLayout.LayoutParams) window.getLayoutParams();

                        int toRightValue = paramsRight.width + delta;
                        int windowWidth = paramsWindow.width - delta;

                        if (toRightValue <= 0) {
                            toRightValue = 0;
                        }

                        if ((toRightValue + leftBorder.getWidth()) > getWidth() * 0.9) {
                            tmpX = x;
                            return true;
                        }

                        if (toRightValue != 0) {
                            paramsRight.width = toRightValue;
                            rightBorder.setLayoutParams(paramsRight);
                            paramsWindow.width = windowWidth;
                            window.setLayoutParams(paramsWindow);
                        }

                        tmpX = x;
                        pushChangeBorder();
                        return true;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        pushChangeBorder();
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
                pushChangeBorder();
            }
        });
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

    private void pushChangeBorder() {
        if (listener != null) {
            int size = x.getValues().length;
            int fromX = (window.getLeft() * size / getWidth());
            int toX = (window.getRight() * size / getWidth());
            listener.onBorderChange(new Border(fromX, toX));
        }
    }

    public void addOnBorderChangeListener(BorderChangeListener listener) {
        this.listener = listener;
        if (currentBorder != null && listener != null) {
            listener.onBorderChange(currentBorder);
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
        void onBorderChange(Border border);
    }
}
