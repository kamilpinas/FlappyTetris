package com.example.ppsm2_tetris;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {
    TetrisActivity myTetrisActivity;
    Point mScreenSize = new Point(0, 0);
    Point mMousePos = new Point(-1, -1);
    int mCellSize = 0;
    boolean mIsTouchMove = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        DisplayMetrics dm = this.getApplicationContext().getResources().getDisplayMetrics();
        mScreenSize.x = dm.heightPixels;
        mScreenSize.y = dm.widthPixels;
        mCellSize = (int)(mScreenSize.x / 8);

        initTetrisCtrl();
    }

    void initTetrisCtrl() {
        myTetrisActivity = new TetrisActivity(this);
        for(int i=0; i <= 7; i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cell0 + i);
            myTetrisActivity.addCellImage(i, bitmap);
        }
        RelativeLayout layoutCanvas = findViewById(R.id.layoutCanvas);
        layoutCanvas.addView(myTetrisActivity);
    }



    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch( event.getAction() ) {
            case MotionEvent.ACTION_DOWN :
                mIsTouchMove = false;
                if( event.getY() < (int)(mScreenSize.y * 0.75)) {
                    mMousePos.x = (int) event.getX();
                    mMousePos.y = (int) event.getY();
                }
                break;
            case MotionEvent.ACTION_MOVE :
                if( mMousePos.y < 0 )
                    break;
                if( (event.getY() - mMousePos.y) > mCellSize ) {
                    myTetrisActivity.block2Down();
                    mMousePos.x = (int) event.getX();
                    mMousePos.y = (int) event.getY();
                    mIsTouchMove = true;
                } else if( (mMousePos.y - event.getY()) > mCellSize ) {
                    myTetrisActivity.block2Up();
                    mMousePos.x = (int) event.getX();
                    mMousePos.y = (int) event.getY();
                    mIsTouchMove = true;
                }
                break;
            case MotionEvent.ACTION_UP :
                if( mIsTouchMove == false && mMousePos.x > 0 )
                    myTetrisActivity.velocity=-20;
                    if(mMousePos.x< mScreenSize.x/2){
                        myTetrisActivity.block2Up();
                    }else{
                        myTetrisActivity.block2Rotate();

                    }
                mMousePos.set(-1, -1);
                break;
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        myTetrisActivity.pauseGame();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        myTetrisActivity.restartGame();
    }
}
