package com.example.ppsm2_tetris;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

public class MainActivity extends Activity {
    FlappyTetrisView myTetrisActivity;
    Point deviceScreenSize = new Point(0, 0);
    Point mousePosition = new Point(-1, -1);
    int cellSize = 0;
    boolean isTouchMove = false;

    private ImageButton pauseBtn;
    private ImageButton restartBtn;
    private ImageButton resumeBtn;
    private ImageButton returnBtn;
    private View gameOverView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        pauseBtn = (ImageButton) findViewById(R.id.pauseButton);
        restartBtn = (ImageButton) findViewById(R.id.restartButton);
        resumeBtn = (ImageButton) findViewById(R.id.resumeButton);
        returnBtn = (ImageButton) findViewById(R.id.returnButton);
        gameOverView = findViewById(R.id.gameOverBackground);


        DisplayMetrics dm = this.getApplicationContext().getResources().getDisplayMetrics();
        deviceScreenSize.x = dm.heightPixels;
        deviceScreenSize.y = dm.widthPixels;
        cellSize = (int) (deviceScreenSize.x / 8);


        startTetrisView();
    }


    void startTetrisView() {
        myTetrisActivity = new FlappyTetrisView(this);
        for (int i = 0; i <= 7; i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cell0 + i);
            myTetrisActivity.addCellImage(i, bitmap);
        }
        FrameLayout layoutCanvas = findViewById(R.id.layoutCanvas);
        layoutCanvas.addView(myTetrisActivity);
    }


    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isTouchMove = false;
                if (event.getY() < (int) (deviceScreenSize.y * 0.75)) {
                    mousePosition.x = (int) event.getX();
                    mousePosition.y = (int) event.getY();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isTouchMove == false && mousePosition.x > 0)
                    myTetrisActivity.velocity = -20;
                if (mousePosition.x < deviceScreenSize.x / 2) {
                    myTetrisActivity.block2Up();
                } else {
                    myTetrisActivity.block2Up();
                    myTetrisActivity.block2Rotate();

                }
                mousePosition.set(-1, -1);
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

    public void pauseGame(View view) {
        restartBtn.setVisibility(View.GONE);
        resumeBtn.setVisibility(View.GONE);
        returnBtn.setVisibility(View.GONE);
        super.onPause();
        myTetrisActivity.pauseGame();
        pauseBtn.setVisibility(View.GONE);
        restartBtn.setVisibility(View.VISIBLE);
        restartBtn.bringToFront();
        resumeBtn.setVisibility(View.VISIBLE);
        resumeBtn.bringToFront();
        returnBtn.setVisibility(View.VISIBLE);
        returnBtn.bringToFront();
    }

    public void resumeGame(View view) {
        super.onResume();
        myTetrisActivity.mTimerFrame.sendEmptyMessageDelayed(0, 10);
        pauseBtn.setVisibility(View.VISIBLE);
        pauseBtn.bringToFront();
        restartBtn.setVisibility(View.GONE);
        returnBtn.setVisibility(View.GONE);
        resumeBtn.setVisibility(View.GONE);
    }

    public void restartGame(View view) {

        super.onRestart();
        // myTetrisActivity.restartGame();
        myTetrisActivity.startGame();
        gameOverView.setVisibility(View.INVISIBLE);
        pauseBtn.setVisibility(View.VISIBLE);
        pauseBtn.bringToFront();
        restartBtn.setVisibility(View.GONE);
        returnBtn.setVisibility(View.GONE);
        resumeBtn.setVisibility(View.GONE);
    }

    public void returnMenu(View view) {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) { //hiding virtual buttons
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
