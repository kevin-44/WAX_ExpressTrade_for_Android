package com.opskins.trade.waxexpresstrade;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

public class fragment_loading extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View fragment = inflater.inflate(R.layout.fragment_loading, container, false);
        final DisplayMetrics display_metrics = getResources().getDisplayMetrics();
        final int start_height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 13, display_metrics); // height defined in "layout/fragment_loading.xml"
        final int end_height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 33, display_metrics);
        final int duration = 170;
        final int delay = 70;

        final View loading_shape_1 = fragment.findViewById(R.id.fragment_loading_shape_1);
        final resize_animation resize_loading_shape_1_1 = new resize_animation(loading_shape_1);
        resize_loading_shape_1_1.setParams(start_height, end_height);
        resize_loading_shape_1_1.setDuration(duration);

        final resize_animation resize_loading_shape_1_2 = new resize_animation(loading_shape_1);
        resize_loading_shape_1_2.setParams(end_height, start_height);
        resize_loading_shape_1_2.setDuration(duration);

        final View loading_shape_2 = fragment.findViewById(R.id.fragment_loading_shape_2);
        final resize_animation resize_loading_shape_2_1 = new resize_animation(loading_shape_2);
        resize_loading_shape_2_1.setParams(start_height, end_height);
        resize_loading_shape_2_1.setDuration(duration);

        final resize_animation resize_loading_shape_2_2 = new resize_animation(loading_shape_2);
        resize_loading_shape_2_2.setParams(end_height, start_height);
        resize_loading_shape_2_2.setDuration(duration);

        final View loading_shape_3 = fragment.findViewById(R.id.fragment_loading_shape_3);
        final resize_animation resize_loading_shape_3_1 = new resize_animation(loading_shape_3);
        resize_loading_shape_3_1.setParams(start_height, end_height);
        resize_loading_shape_3_1.setDuration(duration);

        final resize_animation resize_loading_shape_3_2 = new resize_animation(loading_shape_3);
        resize_loading_shape_3_2.setParams(end_height, start_height);
        resize_loading_shape_3_2.setDuration(duration);

        final View loading_shape_4 = fragment.findViewById(R.id.fragment_loading_shape_4);
        final resize_animation resize_loading_shape_4_1 = new resize_animation(loading_shape_4);
        resize_loading_shape_4_1.setParams(start_height, end_height);
        resize_loading_shape_4_1.setDuration(duration);

        final resize_animation resize_loading_shape_4_2 = new resize_animation(loading_shape_4);
        resize_loading_shape_4_2.setParams(end_height, start_height);
        resize_loading_shape_4_2.setDuration(duration);

        final View loading_shape_5 = fragment.findViewById(R.id.fragment_loading_shape_5);
        final resize_animation resize_loading_shape_5_1 = new resize_animation(loading_shape_5);
        resize_loading_shape_5_1.setParams(start_height, end_height);
        resize_loading_shape_5_1.setDuration(duration);

        final resize_animation resize_loading_shape_5_2 = new resize_animation(loading_shape_5);
        resize_loading_shape_5_2.setParams(end_height, start_height);
        resize_loading_shape_5_2.setDuration(duration);

        // -----

        loading_shape_1.startAnimation(resize_loading_shape_1_1);

        resize_loading_shape_1_1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loading_shape_2.startAnimation(resize_loading_shape_2_1);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loading_shape_3.startAnimation(resize_loading_shape_3_1);

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        loading_shape_4.startAnimation(resize_loading_shape_4_1);

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                loading_shape_5.startAnimation(resize_loading_shape_5_1);

                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        loading_shape_1.startAnimation(resize_loading_shape_1_2);

                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                loading_shape_2.startAnimation(resize_loading_shape_2_2);

                                                                new Handler().postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        loading_shape_3.startAnimation(resize_loading_shape_3_2);

                                                                        new Handler().postDelayed(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                loading_shape_4.startAnimation(resize_loading_shape_4_2);

                                                                                new Handler().postDelayed(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        loading_shape_5.startAnimation(resize_loading_shape_5_2);

                                                                                        new Handler().postDelayed(new Runnable() {
                                                                                            @Override
                                                                                            public void run() {
                                                                                                loading_shape_1.startAnimation(resize_loading_shape_1_1);
                                                                                            }
                                                                                        }, delay);
                                                                                    }
                                                                                }, delay);
                                                                            }
                                                                        }, delay);
                                                                    }
                                                                }, delay);
                                                            }
                                                        }, delay);
                                                    }
                                                }, delay);
                                            }
                                        }, delay);
                                    }
                                }, delay);
                            }
                        }, delay);
                    }
                }, delay);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // empty
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // empty
            }
        });
        return fragment;
    }
}