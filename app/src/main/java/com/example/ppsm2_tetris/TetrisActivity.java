package com.example.ppsm2_tetris;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Arrays;

import static android.content.Context.MODE_PRIVATE;

public class TetrisActivity extends View {

    Handler handler;
    Runnable runnable;
    final int UPDATE_MILLIS = 16;

    Context context;
    final int MatrixSizeWidth = 18;
    final int MatrixSizeHeight = 10;
    final int DirRotate = 0;
    final int DirLeft = 1;
    final int DirRight = 2;
    final int DirDown = 3;
    final int DirUp = 4;

    int[][] blocksMatrix = new int[MatrixSizeHeight][MatrixSizeWidth];
    double blockSize = 0;
    Point screenSize = new Point(0, 0);
    int blockArraySize = 5;
    int[][] mArNewBlock = new int[blockArraySize][blockArraySize];
    int[][] mArNextBlock = new int[blockArraySize][blockArraySize];
    Point newBlockPos = new Point(0, 0);
    Bitmap[] mArBmpCell = new Bitmap[8];
    AlertDialog alertMsg = null;
    SharedPreferences prefs = null;
    int timerGap = 500;
    int mScore = 0;
    int topScore = 0;

    Bitmap[] birds;
    int birdFrame = 0;
    int velocity = 0, gravity = 1;
    int birdXpos, birdYpos;//position

    Rect getBlockArea(int x, int y) {
        Rect rtBlock = new Rect();
        rtBlock.left = (int) (x * blockSize);
        rtBlock.right = (int) (rtBlock.left + blockSize);
        rtBlock.bottom = screenSize.y - (int) (y * blockSize);
        rtBlock.top = (int) (rtBlock.bottom - blockSize);
        return rtBlock;
    }

    int random(int min, int max) {
        int rand = (int) (Math.random() * (max - min + 1)) + min;
        return rand;
    }

    int setLevel(String level) {
        switch (level) {
            case "EASY":
                return 700;
            case "MEDIUM":
                return 400;
            case "HARD":
                return 200;
            default:
                throw new IllegalStateException("Unexpected value: " + level);
        }
    }

    public TetrisActivity(Context context) {
        super(context);
        this.context = context;
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                invalidate(); //calls ondraw
            }
        };


        prefs = context.getSharedPreferences("info", MODE_PRIVATE);
        topScore = prefs.getInt("TopScore", 0);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        timerGap = setLevel(sharedPref.getString("level", "EASY"));


        birds = new Bitmap[2];
        birds[0] = BitmapFactory.decodeResource(getResources(), R.drawable.bluebird_midflap);
        birds[1] = BitmapFactory.decodeResource(getResources(), R.drawable.bluebird_upflap);
        birdXpos = 0 + birds[0].getHeight();//bird starting pos
        birdYpos = screenSize.y / 2 - birds[0].getHeight() / 2;

    }

    void initVariables(Canvas canvas) {
        screenSize.x = canvas.getWidth();
        screenSize.y = canvas.getHeight();
        blockSize = screenSize.x / MatrixSizeWidth;

        startGame();
    }

    void addNewBlock(int[][] arBlock) {
        for (int i = 0; i < blockArraySize; i++) {
            for (int j = 0; j < blockArraySize; j++) {
                arBlock[i][j] = 0;
            }
        }
        newBlockPos.x = 0;//!!!!!!!!!!!!!!!!!!!!!! zeby sie klocek poza ekranem nie pojawil bo od razu game over
        newBlockPos.y = 3;

        int blockType = random(1, 7);
        //blockType = 4; // DO TESTOWANIA

        switch (blockType) {
            case 1:
                // Block 1 : ----
                arBlock[2][1] = 1;
                arBlock[2][2] = 1;
                arBlock[2][3] = 1;
                arBlock[2][4] = 1;
                break;
            case 2:
                // Block 2 : └
                arBlock[3][1] = 2;
                arBlock[2][1] = 2;
                arBlock[2][2] = 2;
                arBlock[2][3] = 2;
                break;
            case 3:
                // Block 3 : ┘
                arBlock[2][1] = 3;
                arBlock[2][2] = 3;
                arBlock[2][3] = 3;
                arBlock[3][3] = 3;
                break;
            case 4:
                // Block 4 : ▣
                arBlock[2][2] = 4;
                arBlock[2][3] = 4;
                arBlock[3][2] = 4;
                arBlock[3][3] = 4;
                break;
            case 5:
                // Block 5 : ＿｜￣
                arBlock[3][3] = 5;
                arBlock[3][2] = 5;
                arBlock[2][2] = 5;
                arBlock[2][1] = 5;
                break;
            case 6:
                // Block 6 : ＿｜＿
                arBlock[2][1] = 6;
                arBlock[2][2] = 6;
                arBlock[2][3] = 6;
                arBlock[3][2] = 6;
                break;
            default:
                // Block 7 : ￣｜＿
                arBlock[2][3] = 7;
                arBlock[2][2] = 7;
                arBlock[3][2] = 7;
                arBlock[3][1] = 7;
                break;
        }
        redraw();
    }

    public void redraw() {
        this.invalidate();
    }

    boolean checkBlockSafe(int[][] arNewBlock, Point posBlock) {
        for (int i = 0; i < blockArraySize; i++) {
            for (int j = 0; j < blockArraySize; j++) {
                if (arNewBlock[i][j] == 0)//granice matrixa, 0 to caly matrix
                    continue;
                int x = posBlock.x + j;
                int y = posBlock.y + i;
                if (checkCellSafe(x, y) == false)
                    return false;
            }
        }
        return true;
    }

    boolean checkCellSafe(int x, int y) {//ZEBY NIE UMIESCIC KLOCKA POZA EKRANEM - POZA CELL/ POZA MATRIX
        if (x < 0)
            return false;
        if (x >= MatrixSizeWidth)//przekroczenie prawej krawedzi
            return false;
        if (y < 0)
            return false;
        if (y >= MatrixSizeHeight)//to na false blokuje przekroczenie gornej krawedzi
            return false;//
        if (blocksMatrix[y][x] > 0)
            return false;
        return true;
    }

    void moveNewBlock(int dir, int[][] arNewBlock, Point posBlock) {//PORUSZANIE  I OBRACANIE KLOCKA
        switch (dir) {
            case DirRotate:
                if (canRotate(DirDown)) { //////// naprawia odwaracanie ale dodatkowo obniza w dol
                    int[][] arRotate = new int[blockArraySize][blockArraySize];
                    for (int i = 0; i < blockArraySize; i++) {
                        for (int j = 0; j < blockArraySize; j++) {
                            arRotate[blockArraySize - j - 1][i] = arNewBlock[i][j];
                        }
                    }
                    for (int i = 0; i < blockArraySize; i++) {
                        for (int j = 0; j < blockArraySize; j++) {
                            arNewBlock[i][j] = arRotate[i][j];
                        }
                    }
                }

                break;
            case DirLeft:
                posBlock.x--;
                break;
            case DirRight:
                posBlock.x++;
                break;
            case DirDown:

                posBlock.y--;
                break;
            case DirUp:
                posBlock.y++;
                break;
        }
    }

    void copyBlock2Matrix(int[][] arBlock, Point posBlock) {
        for (int i = 0; i < blockArraySize; i++) {
            for (int j = 0; j < blockArraySize; j++) {
                if (arBlock[i][j] == 0)
                    continue;
                blocksMatrix[posBlock.y + i][posBlock.x + j] = arBlock[i][j];
                arBlock[i][j] = 0;
            }
        }
    }

    int checkLineFilled() {

        boolean bFilled;
        int count = 0;
        int filledCount = 0;
        ArrayList<Integer> fullColumns = new ArrayList<>();
        int x, y;

        for (x = MatrixSizeWidth - 1; x > 0; x--) {
            bFilled = true;
            for (y = 0; y < MatrixSizeHeight; y++) {

                if (blocksMatrix[y][x] == 0) {
                    bFilled = false;
                    break;
                }
            }
            if (bFilled == true) {
                fullColumns.add(x);
            }
        }

        for (Integer element : fullColumns) {

            int[][] temp = new int[blocksMatrix.length][];

            for (int i = 0; i < blocksMatrix.length; i++) {
                temp[i] = Arrays.copyOf(blocksMatrix[i], blocksMatrix[i].length);
            }

            if (count > 0) {

                for (int j = 0; j < MatrixSizeHeight; j++) {
                    for (int k = element + count; k > 1; k--) {
                        blocksMatrix[j][k] = temp[j][k - 1];
                    }
                }
            } else {
                for (int j = 0; j < MatrixSizeHeight; j++) {
                    for (int k = element; k > 0; k--) {
                        blocksMatrix[j][k] = temp[j][k - 1];
                    }
                    blocksMatrix[j][0] = 0;
                }
            }
            count++;
        }

        mScore += fullColumns.size() * 10 + 5;
        if (topScore < mScore) {
            topScore = mScore;
            SharedPreferences.Editor edit = prefs.edit();
            edit.putInt("TopScore", topScore);
            edit.commit();
        }
        return filledCount;
    }

    boolean isGameOver() {
        boolean canMove = checkBlockSafe(mArNewBlock, newBlockPos);
        return !canMove;
    }

    boolean moveNewBlock(int dir) {
        Point posBackup = new Point(newBlockPos);
        moveNewBlock(dir, mArNewBlock, newBlockPos);
        boolean canMove = checkBlockSafe(mArNewBlock, newBlockPos);
        if (canMove) {
            redraw();
            return true;
        }

        newBlockPos.set(posBackup.x, posBackup.y);
        return false;
    }

    boolean canRotate(int dir) {
        moveNewBlock(dir, mArNewBlock, newBlockPos);
        boolean canMove = checkBlockSafe(mArNewBlock, newBlockPos);
        if (canMove) {
            redraw();
            return true;
        }
        return false;
    }

    void showScore(Canvas canvas, int score) {

        int fontSize = screenSize.x / 10;

        Paint pnt = new Paint();

        pnt.setTextSize(fontSize);
        pnt.setColor(Color.WHITE);
        Typeface flappyFont = ResourcesCompat.getFont(context, R.font.flap);
        pnt.setTypeface(flappyFont); //TODO
        int posX = (int) (fontSize * 4.8);
        int poxY = (int) (fontSize * 1.5);
        canvas.drawText(String.valueOf(mScore), posX, poxY, pnt);
    }

    void showMatrix(Canvas canvas, int[][] arMatrix, boolean drawEmpth) {
        for (int i = 0; i < MatrixSizeHeight; i++) {
            for (int j = 0; j < MatrixSizeWidth; j++) {
                if (arMatrix[i][j] == 0 && drawEmpth == false)
                    continue;
                showBlockImage(canvas, j, i, arMatrix[i][j]);
            }
        }
    }

    void showBlockImage(Canvas canvas, int blockX, int blockY, int blockType) {
        Rect rtBlock = getBlockArea(blockX, blockY);

        canvas.drawBitmap(mArBmpCell[blockType], null, rtBlock, null);
    }


    public void addCellImage(int index, Bitmap bmp) {
        mArBmpCell[index] = bmp;
    }


    public boolean block2Up() {
        return moveNewBlock(DirUp);
    }

    public boolean block2Down() {
        return moveNewBlock(DirDown);
    }

    public boolean block2Rotate() {
        return moveNewBlock(DirRotate);
    }

    public void pauseGame() {
        if (alertMsg != null)
            return;

        mTimerFrame.removeMessages(0);
    }


    public void restartGame() {
        if (alertMsg != null)
            return;

        mTimerFrame.sendEmptyMessageDelayed(0, 1000);
    }

    public void startGame() {
        mScore = 0;

        for (int i = 0; i < MatrixSizeHeight; i++) {
            for (int j = 0; j < MatrixSizeWidth; j++) {
                blocksMatrix[i][j] = 0;
            }
        }

        addNewBlock(mArNewBlock);
        addNewBlock(mArNextBlock);
        mTimerFrame.sendEmptyMessageDelayed(0, 10);
    }

    void showDialog_GameOver() {
        alertMsg = new AlertDialog.Builder(context)
                .setTitle("Game over!")
                .setMessage("Your score is " + mScore + "\n" + "Top Score is " + topScore)
                .setPositiveButton("Play Again!",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                alertMsg = null;
                                startGame();
                            }
                        })
                .show();
    }

    public void onDraw(Canvas canvas) {
        if (blockSize < 1)
            initVariables(canvas);

        showMatrix(canvas, blocksMatrix, false);
        showNewBlock(canvas);

        addBirdControls();
        canvas.drawBitmap(birds[birdFrame], birdXpos, birdYpos, null);

        showScore(canvas, mScore);

        handler.postDelayed(runnable, UPDATE_MILLIS);
    }

    void addBirdControls() {
        if (birdFrame == 0) {
            birdFrame = 1;
        } else {
            birdFrame = 0;
        }
        //falling and screen bounds
        if (birdYpos < screenSize.y - 100 || velocity < 0) {
            if (birdYpos < 0) {
                birdYpos = 0;
                velocity = 0;
            }
            if (birdYpos > screenSize.y) {//dotyka gornej krawedzi
                birdYpos = screenSize.y;
            }
            velocity += gravity;
            birdYpos += velocity;
            birdXpos += 10;
            if (birdXpos >= screenSize.x) {
                birdXpos = 0;
                birdXpos += 10;
            }
        } else {//dotyka ziemii
            velocity = 0;
            birdXpos += 10;
        }

    }

    void showNewBlock(Canvas canvas) {
        for (int i = 0; i < blockArraySize; i++) {
            for (int j = 0; j < blockArraySize; j++) {
                if (mArNewBlock[i][j] == 0)
                    continue;
                showBlockImage(canvas, newBlockPos.x + j, newBlockPos.y + i, mArNewBlock[i][j]);
            }
        }
    }

    Handler mTimerFrame = new Handler() {
        public void handleMessage(Message msg) {//OPADANIE
            boolean canMove = moveNewBlock(DirRight);
            if (canMove) {
                moveNewBlock(DirDown);
            }

            if (!canMove) {
                copyBlock2Matrix(mArNewBlock, newBlockPos);
                checkLineFilled();
                copyBlockArray(mArNextBlock, mArNewBlock);
                addNewBlock(mArNextBlock);
                if (isGameOver()) {
                    showDialog_GameOver();
                    return;
                }
            }

            this.sendEmptyMessageDelayed(0, timerGap);
        }
    };

    void copyBlockArray(int[][] arFrom, int[][] arTo) {
        for (int i = 0; i < blockArraySize; i++) {
            for (int j = 0; j < blockArraySize; j++) {
                arTo[i][j] = arFrom[i][j];
            }
        }
    }
}