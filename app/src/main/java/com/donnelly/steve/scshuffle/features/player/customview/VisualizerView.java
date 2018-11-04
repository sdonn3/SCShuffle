package com.donnelly.steve.scshuffle.features.player.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class VisualizerView extends View {
    private static final int LINE_WIDTH = 1; // width of visualizer lines
    private static final int LINE_SCALE = 1; // scales visualizer lines
    private List<Float> amplitudes; // amplitudes for line lengths
    private int width; // width of this View
    private int height; // height of this View
    private Paint linePaint; // specifies line drawing characteristics

    // constructor
    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs); // call superclass constructor
        linePaint = new Paint(); // create Paint for lines
        linePaint.setColor(Color.parseColor("#e8964e")); // set color to green
        linePaint.setStrokeWidth(LINE_WIDTH); // set stroke width
    }

    // called when the dimensions of the View change
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w; // new width of this View
        height = h; // new height of this View
        amplitudes = new ArrayList<Float>(width / LINE_WIDTH);
    }

    // clear all amplitudes to prepare for a new visualization
    public void clear() {
        amplitudes.clear();
    }

    public void setAmplitudes(List<Float> amps) {
        this.amplitudes = amps;
        invalidate();
    }

    // add the given amplitude to the amplitudes ArrayList
    public void addAmplitude(float amplitude) {
        amplitudes.add(amplitude); // add newest to the amplitudes ArrayList

        // if the power lines completely fill the VisualizerView
        if (amplitudes.size() * LINE_WIDTH >= width) {
            amplitudes.remove(0); // remove oldest power value
        }
    }

    // draw the visualizer with scaled lines representing the amplitudes
    @Override
    public void onDraw(Canvas canvas) {
        int middle = height / 2; // get the middle of the View
        float curX = 100; // start curX at zero

        int everyThird = 0;
        float total = 0;

        canvas.drawLine(0, middle, width, middle, linePaint);
        canvas.drawLine(0, middle+1, width, middle+1, linePaint);

        // for each item in the amplitudes ArrayList
        for (float power : amplitudes) {
            if (everyThird >= 2) {
                everyThird = 0;
                total+= power;
                float scaledHeight = (total / 3) / LINE_SCALE; // scale the power
                curX += LINE_WIDTH; // increase X by LINE_WIDTH

                Log.e("crux",String.valueOf(curX));

                // draw a line representing this item in the amplitudes ArrayList
                canvas.drawLine(curX, middle + scaledHeight / 2, curX, middle
                        - scaledHeight / 2, linePaint);
                total = 0;
            }
            else {
                everyThird += 1;
                total += power;
            }
        }
    }
}
