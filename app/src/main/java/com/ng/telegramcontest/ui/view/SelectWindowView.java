package com.ng.telegramcontest.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.ng.telegramcontest.R;

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

    private float currentLeftBorder;
    private float currentRightBorder;

    private void inflateView(Context context) {
        View.inflate(context, R.layout.view_window_selector, this);
        leftBorder = findViewById(R.id.left_border);
        rightBorder = findViewById(R.id.right_border);
        window = findViewById(R.id.window);
        leftBorderTouch = findViewById(R.id.left_border_touch);
        rightBorderTouch = findViewById(R.id.right_border_touch);

        postDelayed(new Runnable() {
            @Override
            public void run() {
                int allWidth = getWidth();
                currentLeftBorder = (leftBorder.getWidth() * 100f) / (float) allWidth;
                currentRightBorder = 100f - ((rightBorder.getWidth() * 100f) / (float) allWidth);
                Log.d("TAG", "Current selection position: " + currentLeftBorder + " to " + currentRightBorder);
            }
        }, 1);
    }

    public Border getBorders() {
        return new Border(currentLeftBorder, currentRightBorder);
    }

    public class Border {
        final float from;
        final float to;

        public Border(float from, float to) {
            this.from = from;
            this.to = to;
        }
    }
}
