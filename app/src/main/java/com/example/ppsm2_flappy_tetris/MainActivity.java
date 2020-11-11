package com.example.ppsm2_flappy_tetris;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.ppsm2_flappy_tetris.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startGame(View view){
        Intent intent = new Intent(this, StartGame.class);
        startActivity(intent);
        finish();
    }
}