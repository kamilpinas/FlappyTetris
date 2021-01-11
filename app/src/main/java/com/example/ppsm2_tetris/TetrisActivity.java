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
import android.os.Handler;
import android.os.Message;
import android.view.View;

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

    final int timerGap = 800;

    int[][] blocksMatrix = new int[MatrixSizeHeight][MatrixSizeWidth];
    double mBlockSize = 0;
    Point screenSize = new Point(0, 0);
    int mNewBlockArea = 5;
    int[][] mArNewBlock = new int[mNewBlockArea][mNewBlockArea];
    int[][] mArNextBlock = new int[mNewBlockArea][mNewBlockArea];
    Point newBlockPos = new Point(0, 0);
    Bitmap[] mArBmpCell = new Bitmap[8];
    AlertDialog mDlgMsg = null;
    SharedPreferences mPref = null;
    int mScore = 0;
    int mTopScore = 0;

    Bitmap[] birds;
    int birdFrame = 0;
    int velocity = 0, gravity = 1;
    int birdXpos, birdYpos;//position

    Rect getBlockArea(int x, int y) {
        Rect rtBlock = new Rect();
        rtBlock.left = (int) (x * mBlockSize);
        rtBlock.right = (int) (rtBlock.left + mBlockSize);
        rtBlock.bottom = screenSize.y - (int) (y * mBlockSize);
        rtBlock.top = (int) (rtBlock.bottom - mBlockSize);
        return rtBlock;
    }

    int random(int min, int max) {
        int rand = (int) (Math.random() * (max - min + 1)) + min;
        return rand;
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


        mPref = context.getSharedPreferences("info", MODE_PRIVATE);
        mTopScore = mPref.getInt("TopScore", 0);

        birds = new Bitmap[2];
        birds[0] = BitmapFactory.decodeResource(getResources(), R.drawable.bluebird_midflap);
        birds[1] = BitmapFactory.decodeResource(getResources(), R.drawable.bluebird_upflap);
        birdXpos = 0 + birds[0].getHeight();//bird starting pos
        birdYpos = screenSize.y / 2 - birds[0].getHeight() / 2;

    }

    void initVariables(Canvas canvas) {
        screenSize.x = canvas.getWidth();
        screenSize.y = canvas.getHeight();
        mBlockSize = screenSize.x / MatrixSizeWidth;

        startGame();
    }

    void addNewBlock(int[][] arBlock) {
        for (int i = 0; i < mNewBlockArea; i++) {
            for (int j = 0; j < mNewBlockArea; j++) {
                arBlock[i][j] = 0;
            }
        }
        newBlockPos.x = 0;//!!!!!!!!!!!!!!!!!!!!!! zeby sie klocek poza ekranem nie pojawil bo od razu game over xD
        newBlockPos.y = 3;

        int blockType = random(1, 7);
        //   blockType = 4; // DO TESTOWANIA

        switch (blockType) {
            case 1:
                // Block 1 : --
                arBlock[2][1] = 1;
                arBlock[2][2] = 1;
                arBlock[2][3] = 1;
                arBlock[2][4] = 1;
                break;
            case 2:
                // Block 2 : └-
                arBlock[3][1] = 2;
                arBlock[2][1] = 2;
                arBlock[2][2] = 2;
                arBlock[2][3] = 2;
                break;
            case 3:
                // Block 3 : -┘
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
        for (int i = 0; i < mNewBlockArea; i++) {
            for (int j = 0; j < mNewBlockArea; j++) {
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
                    int[][] arRotate = new int[mNewBlockArea][mNewBlockArea];
                    for (int i = 0; i < mNewBlockArea; i++) {
                        for (int j = 0; j < mNewBlockArea; j++) {
                            arRotate[mNewBlockArea - j - 1][i] = arNewBlock[i][j];
                        }
                    }
                    for (int i = 0; i < mNewBlockArea; i++) {
                        for (int j = 0; j < mNewBlockArea; j++) {
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

    int[][] duplicateBlockArray(int[][] arBlock) {
        int size1 = mNewBlockArea, size2 = mNewBlockArea;
        int[][] arClone = new int[size1][size2];
        for (int i = 0; i < size1; i++) {
            for (int j = 0; j < size2; j++) {
                arClone[i][j] = arBlock[i][j];
            }
        }
        return arClone;
    }

    void copyBlock2Matrix(int[][] arBlock, Point posBlock) {
        for (int i = 0; i < mNewBlockArea; i++) {
            for (int j = 0; j < mNewBlockArea; j++) {
                if (arBlock[i][j] == 0)
                    continue;
                blocksMatrix[posBlock.y + i][posBlock.x + j] = arBlock[i][j];
                arBlock[i][j] = 0;
            }
        }
    }

    int checkLineFilled() {
        int filledCount = 0;
        boolean bFilled;

        int x = 0;
        for (int y = 0; y < MatrixSizeWidth; y++) {
            bFilled = true;
            for (x = 0; x < MatrixSizeHeight; x++) {
                if (blocksMatrix[x][y] == 0) {// jesli JEST PRZERWA W kolumnie  to wraca do false
                    bFilled = false;// TO DZIALA BO JAK JEST CALA KOLUMNA TO ZWRACA TRUE
                    break;

                }
            }
            System.out.println(bFilled + "!!!!!!!!!!!!!!!!!!!");
            if (bFilled == false)//ten nie byl pelny
                continue;//// wiec sprawdza kolejny wiersz - for wyzej

            filledCount++;
            for (int y2 = y + 1; y2 < MatrixSizeWidth - 1; y2++) {
                for (int x2 = 0; x2 < MatrixSizeHeight; x2++) {
                    blocksMatrix[x2][y2 + 1] = blocksMatrix[x2][y2];// i przesuwa
                }
            }

            for (int j = 0; j < MatrixSizeHeight; j++) {//usuwanie
                blocksMatrix[j][MatrixSizeWidth - 1] = 0;// to chyba dziala
            }


            x--;
        }


        mScore += filledCount * filledCount;
        if (mTopScore < mScore) {
            mTopScore = mScore;
            SharedPreferences.Editor edit = mPref.edit();
            edit.putInt("TopScore", mTopScore);
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


        /*
                int[][] arBackup = duplicateBlockArray(mArNewBlock);
        for (int i = 0; i < mNewBlockArea; i++) {
            for (int j = 0; j < mNewBlockArea; j++) {
                mArNewBlock[j][i] = arBackup[i][j];///tez przyukelajnei do prawej
            }
        }*/

        newBlockPos.set(posBackup.x, posBackup.y);//umieszczanie klocka  JAK ZAMIENISZ X I Y TO PRZYKLEJA DO PRAWEJ
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
        int fontSize = screenSize.x / 20;
        Paint pnt = new Paint();
        pnt.setTextSize(fontSize);
        pnt.setARGB(128, 255, 255, 255);
        int posX = (int) (fontSize * 0.5);
        int poxY = (int) (fontSize * 1.5);
        canvas.drawText("Score : " + mScore, posX, poxY, pnt);

        poxY += (int) (fontSize * 1.5);
        canvas.drawText("Top Score : " + mTopScore, posX, poxY, pnt);
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


    public static Bitmap createBitmap(
            int width,
            int height,
            Bitmap.Config config) {
        return Bitmap.createBitmap(width, height, config);
    }

    public void addCellImage(int index, Bitmap bmp) {
        mArBmpCell[index] = bmp;
    }

    public boolean block2Left() {
        return moveNewBlock(DirLeft);
    }

    public boolean block2Right() {
        return moveNewBlock(DirRight);
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

    public boolean block2Bottom() {
        mTimerFrame.removeMessages(0);
        mTimerFrame.sendEmptyMessageDelayed(0, 10);
        return true;
    }

    public void pauseGame() {
        if (mDlgMsg != null)
            return;

        mTimerFrame.removeMessages(0);
    }

    public void restartGame() {
        if (mDlgMsg != null)
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
        mDlgMsg = new AlertDialog.Builder(context)
                .setTitle("Notice")
                .setMessage("Game over! Your score is " + mScore)
                .setPositiveButton("Again",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mDlgMsg = null;
                                startGame();
                            }
                        })
                .show();
    }

    public void onDraw(Canvas canvas) {
        if (mBlockSize < 1)
            initVariables(canvas);

        showMatrix(canvas, blocksMatrix, false);
        showNewBlock(canvas);
        showScore(canvas, mScore);
        //showNextBlock(canvas, mArNextBlock); // wyświetlenie okna kolejnego klocka

        addBirdControls();

        canvas.drawBitmap(birds[birdFrame], birdXpos, birdYpos, null);
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
            if (birdYpos > screenSize.y) {
                birdYpos = screenSize.y - birds[0].getHeight();
            }
            velocity += gravity;
            birdYpos += velocity;
            birdXpos += 10;
            if (birdXpos >= screenSize.x) {
                birdXpos = 0;
            }
        } else {
            velocity = 0;
        }

    }

    void showNewBlock(Canvas canvas) {
        for (int i = 0; i < mNewBlockArea; i++) {
            for (int j = 0; j < mNewBlockArea; j++) {
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
        for (int i = 0; i < mNewBlockArea; i++) {
            for (int j = 0; j < mNewBlockArea; j++) {
                arTo[i][j] = arFrom[i][j];
            }
        }
    }

    void showNextBlock(Canvas canvas, int[][] arBlock) {
        for (int i = 0; i < mNewBlockArea; i++) {
            for (int j = 0; j < mNewBlockArea; j++) {
                int blockX = j;
                int blockY = mNewBlockArea - i;
                showBlockColor(canvas, blockX, blockY, arBlock[i][j]);
            }
        }
    }

    void showBlockColor(Canvas canvas, int blockX, int blockY, int blockType) {
        int[] arColor = {Color.argb(32, 255, 255, 255),
                Color.argb(128, 255, 0, 0),
                Color.argb(128, 255, 255, 0),
                Color.argb(128, 255, 160, 160),
                Color.argb(128, 100, 255, 100),
                Color.argb(128, 255, 128, 100),
                Color.argb(128, 0, 0, 255),
                Color.argb(128, 100, 100, 255)};
        int previewBlockSize = screenSize.x / 20;

        Rect rtBlock = new Rect();
        rtBlock.top = (blockY - 1) * previewBlockSize;
        rtBlock.bottom = rtBlock.top + previewBlockSize;
        rtBlock.left = screenSize.x - previewBlockSize * (mNewBlockArea - blockX);
        rtBlock.right = rtBlock.left + previewBlockSize;
        int crBlock = arColor[blockType];

        Paint pnt = new Paint();
        pnt.setStyle(Paint.Style.FILL);
        pnt.setColor(crBlock);
        canvas.drawRect(rtBlock, pnt);
    }
}
