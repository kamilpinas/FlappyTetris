package com.example.ppsm2_flappy_tetris;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import com.example.ppsm2_flappy_tetris.R;

public class GameView extends View {
    Handler handler;
    Runnable runnable;
    final int UPDATE_MILLIS = 16;
    Bitmap background;
    Display display;
    Point point;
    int screenWidth, screenHeight; //Device height and width
    Rect rect;
    Bitmap[] birds;
    int birdFrame = 0;

    int velocity = 0, gravity = 3;
    int birdXpos, birdYpos;//position

    public GameView(Context context) {
        super(context);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                invalidate(); //calls ondraw
            }
        };

        background = BitmapFactory.decodeResource(getResources(), R.drawable.background_day);
        display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();

        point = new Point();
        display.getRealSize(point);
        screenWidth = point.x;
        screenHeight = point.y;
        rect = new Rect(0, 0, screenWidth, screenHeight);


        birds = new Bitmap[2];
        birds[0] = BitmapFactory.decodeResource(getResources(), R.drawable.bluebird_midflap);
        birds[1] = BitmapFactory.decodeResource(getResources(), R.drawable.bluebird_upflap);
        birdXpos = 0+birds[0].getHeight();//bird starting pos
        birdYpos = screenHeight / 2 - birds[0].getHeight() / 2;



        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        this.setSystemUiVisibility(flags);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(background, null, rect, null);

        if (birdFrame == 0) {
            birdFrame = 1;
        } else {
            birdFrame = 0;
        }


        //falling and screen bounds
        if (birdYpos < screenHeight - 100||velocity<0) {
            if(birdYpos<0){
                birdYpos= 0;
                velocity=0;
            }
            if(birdYpos>screenHeight){
                birdYpos= screenHeight-birds[0].getHeight();
            }
            velocity +=gravity;
            birdYpos +=velocity;
            birdXpos+=10;
            if(birdXpos>=screenWidth){
            birdXpos=0;
            }
        }else{
            velocity=0;
        }

        //to display bird in the center of the screen
        canvas.drawBitmap(birds[birdFrame], birdXpos, birdYpos, null);
        handler.postDelayed(runnable, UPDATE_MILLIS);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            //if(birdYpos>0-birds[0].getHeight())
            System.out.println(birdYpos);
            velocity = -40;
        }
        return true;
    }


}
