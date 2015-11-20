package com.ee472.daniel.supertank;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by daniel on 5/24/15.
 */
public class JoystickView extends View {

    private Paint circlePaint;
    private Paint handlePaint;
    private double touchX, touchY;
    private int innerPadding;
    private int handleRadius;
    private int handleInnerBoundaries;
    private JoystickMovedListener listener;
    private int sensitivity;
    private boolean hold;


    public JoystickView(Context context) {
        super (context);
        initJoystickView();
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initJoystickView();
    }

    public JoystickView(Context context, AttributeSet attrs,
                        int defStyle) {
        super(context, attrs, defStyle);
        initJoystickView();
    }

    private void initJoystickView() {
        setFocusable(true);

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.GRAY);
        circlePaint.setStrokeWidth(1);
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        handlePaint.setColor(Color.DKGRAY);
        handlePaint.setStrokeWidth(1);
        handlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        innerPadding = 10;
        sensitivity = 10;

        hold = false;
    }


    public void setOnJoystickMovedListener(JoystickMovedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = measure(widthMeasureSpec);
        int measuredHeight = measure(heightMeasureSpec);
        int d = Math.min(measuredWidth, measuredHeight);

        handleRadius = (int) (d * 0.25);
        handleInnerBoundaries = handleRadius;

        setMeasuredDimension(d, d);
    }

    private int measure(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.UNSPECIFIED) {
            result = 200;
        } else {
            result = specSize;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int px = getMeasuredWidth() / 2;
        int py = getMeasuredHeight() / 2;
        int radius = Math.min(px, py);

        canvas.drawCircle(px, py, radius - innerPadding, circlePaint);

        canvas.drawCircle((int) touchX + px, (int) touchY + py,
                handleRadius, handlePaint);

        canvas.save();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!hold) listener.OnHold();
        hold = true;
        int actionType = event.getAction();
        if (actionType == MotionEvent.ACTION_MOVE) {
            int px = getMeasuredWidth() / 2;
            int py = getMeasuredHeight() / 2;
            int radius = Math.min(px, py) - handleInnerBoundaries;
            double theta = Math.atan(Math.abs(touchY) / Math.abs(touchX));
            int a, b;

            touchX = (event.getX() - px);
            touchY = (event.getY() - py);

            double magnitude = Math.sqrt(Math.pow(touchX, 2) + Math.pow(touchY, 2));
            if (magnitude > radius) {
                touchX = (touchX / magnitude) * radius;
                touchY = (touchY / magnitude) * radius;
            }

            double r = Math.sqrt(Math.pow(touchX, 2) + Math.pow(touchY, 2));

            //quadrant 1
            if (touchX >= 0 && touchY <= 0) {
                a = (int) (r/radius * 10);
                b = (int) (theta/(Math.PI/2) * 20 - 10);
            } else if (touchX < 0 && touchY <= 0) {
                a = (int) (theta/(Math.PI/2) * 20 - 10);
                b = (int) (r/radius * 10);
            } else if (touchX <= 0 && touchY > 0) {
                a = (int) -(theta / (Math.PI / 2) * 20 - 10);
                b = (int) -(r / radius * 10);
            } else {
                a = (int) -(r / radius * 10);
                b = (int) -(theta / (Math.PI / 2) * 20 - 10);
            }

            if (listener != null) {
                listener.OnMoved(a, b);
            }

            invalidate();
        } else if (actionType == MotionEvent.ACTION_UP) {
            returnHandleToCenter();
        }
        return true;
    }

    private void returnHandleToCenter() {
        Handler handler = new Handler();
        int numberOfFrames = 5;
        final double intervalsX = (0 - touchX) / numberOfFrames;
        final double intervalsY = (0 - touchY) / numberOfFrames;

        for (int i = 0; i < numberOfFrames; i++) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    touchX += intervalsX;
                    touchY += intervalsY;
                    invalidate();
                }
            }, i * 40);
        }

        if (listener != null) {
            hold = false;
            listener.OnReleased();
        }
    }




}

