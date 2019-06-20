package com.multitv.multitvplayersdk.customeviews;

/**
 * Created by naseeb on 8/26/2016.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.SeekBar;

import java.util.ArrayList;

public class CustomSeekBar extends SeekBar {

    private ArrayList<ProgressItem> mProgressItemsList;

    public CustomSeekBar(Context context) {
        super(context);
    }

    public CustomSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initData(ArrayList<ProgressItem> progressItemsList) {
        this.mProgressItemsList = progressItemsList;
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec,
                                          int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onDraw(Canvas canvas) {
        if (mProgressItemsList != null && mProgressItemsList.size() > 0) {
            int progressBarWidth = getWidth();
            int progressBarHeight = getHeight();
            /*int thumboffset = getThumbOffset();*/
            int thumbOffset = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 29, getResources().getDisplayMetrics());
            int lastProgressX = 0;
            int progressItemWidth, progressItemRight;
            for (int i = 0; i < mProgressItemsList.size(); i++) {
                ProgressItem progressItem = mProgressItemsList.get(i);
                Paint progressPaint = new Paint();
                int progressColor;
                try {
                    progressColor = getResources().getColor(progressItem.color);
                }
                catch (Exception e) { //Handling resource not found exception
                    progressColor = progressItem.color;
                }
                progressPaint.setColor(progressColor);

                progressItemWidth = (int) (progressItem.progressItemPercentage
                        * progressBarWidth / 1000);
                progressItemRight = lastProgressX + progressItemWidth;

                // for last item give right to progress item to the width
                if (i == mProgressItemsList.size() - 1
                        && progressItemRight != progressBarWidth) {
                    progressItemRight = progressBarWidth;
                }
                Rect progressRect = new Rect();
                progressRect.set(lastProgressX, thumbOffset / 2,
                        progressItemRight, progressBarHeight - thumbOffset / 2);
                canvas.drawRect(progressRect, progressPaint);
                lastProgressX = progressItemRight;
            }
        }

        super.onDraw(canvas);
    }

    public static class ProgressItem {
        public int color;
        public float progressItemPercentage;
    }

}
