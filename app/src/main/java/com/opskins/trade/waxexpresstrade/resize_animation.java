package com.opskins.trade.waxexpresstrade;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class resize_animation extends Animation {
    // ** VARIABLES

    private View view;
    private int start_height;
    private int delta_height;

    // ** MAIN

    resize_animation(View view) {
        this.view = view;
    }

    // ** FUNCTIONS

    @Override
    protected void applyTransformation(float interpolated_time, Transformation transformation) {
        view.getLayoutParams().height = (int) (start_height + (delta_height * interpolated_time));
        view.requestLayout();
    }

    void setParams(int start, int end) {
        start_height = start;
        delta_height = end - start_height;
    }

    @Override
    public void setDuration(long durationMillis) {
        super.setDuration(durationMillis);
    }
}