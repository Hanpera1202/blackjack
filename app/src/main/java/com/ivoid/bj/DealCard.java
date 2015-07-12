package com.ivoid.bj;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

/**
 * Created by nakazato on 2015/06/21.
 */
public class DealCard extends SurfaceView implements SurfaceHolder.Callback {

    private ArrayList<Card> cards;
    private ArrayList<Boolean> animations;
    private int stackPointer;

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        dealThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        dealThread = null;
    }

    public class DealThread extends Thread {
        SurfaceHolder surfaceHolder;
        Context context;
        boolean shouldContinue = true;
        protected Drawable drawableImg;
        protected int width = 50;
        protected int height = 50;
        protected int x;
        protected int y;

        public DealThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
            this.surfaceHolder = surfaceHolder;
            this.context = context;
        }

        @Override
        public void run() {
            while (shouldContinue) {
                Canvas c = surfaceHolder.lockCanvas();
                Log.d("CARD", "size:" + cards.size());
                Log.d("CARD", "stackPointer" + stackPointer);
                if(cards.size() > stackPointer) {
                    Log.d("CARD", String.valueOf(cards.get(stackPointer).getImage()));
                    drawableImg = context.getResources().getDrawable(cards.get(stackPointer).getImage());
                    draw(c, 0, 0);
                    stackPointer++;
                }
                surfaceHolder.unlockCanvasAndPost(c);
            }
        }

        public void draw(Canvas c, int x, int y) {
            drawableImg.setBounds(x, y, x + width, y + height);
            drawableImg.draw(c);
            //c.drawARGB(255, 0, 0, 0);
        }
    }
    DealThread dealThread;

    public DealCard(Context context) {
        super(context);
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public DealCard(Context context, AttributeSet attrs) {
        super(context, attrs);

        stackPointer = 0;
        cards = new ArrayList<Card>();
        animations = new ArrayList<Boolean>();

        SurfaceHolder holder = getHolder();

        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSLUCENT);
        this.setZOrderOnTop(true);

        dealThread = new DealThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        });
    }

    public DealCard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void setCard(Card c, boolean aniF){
        if (c != null) {
            Log.d("TAG", "setCard2:" + String.valueOf(c.getImage()));
            cards.add(c);
            animations.add(aniF);
        }
        //stackPointer++;
    }
}
