package com.example.it.beaconhack;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diego on 21/11/15.
 */
public class FindingView extends View {
    static String TAG = FindingView.class.getSimpleName();

    private int width, cx, cy;
    private List<Circulo> circulos;
    private Rect rect;
    private Paint cPaint;
    private boolean addNew = true;

    public FindingView(Context context) {
        this(context, null);
    }

    public FindingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        circulos = new ArrayList<>();
        circulos.add(new Circulo());

        cPaint = new Paint();
        cPaint.setStrokeWidth(2);
        cPaint.setStyle(Paint.Style.STROKE);
        cPaint.setColor(Color.BLACK);
        cPaint.setAntiAlias(true);

        Runnable animator = new Runnable() {
            @Override
            public void run() {
                for(Circulo circulo : circulos) {
                    circulo.update();
                    if(circulo.value == 100 && addNew)
                        circulos.add(new Circulo());

                    if(circulo.isDone(width)) {
                        addNew = false;
                        if(circulo.value%100 == 0)
                            circulo.value = 0;
                    }
                }
                postDelayed(this, 15);
                invalidate(rect);
            }
        };

        removeCallbacks(animator);
        post(animator);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for(Circulo circulo : circulos){
            canvas.drawCircle(cx, cy, circulo.value, cPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rect = new Rect(0,0,w,h);
        cx = w/2;
        cy = h/2;
        width = w;
    }

    class Circulo {
        int value;

        public Circulo() {
            value = 1;
        }

        synchronized public void update() {
            value++;
        }

        synchronized public boolean isDone(int width) {
            return value > width;
        }
    }
}
