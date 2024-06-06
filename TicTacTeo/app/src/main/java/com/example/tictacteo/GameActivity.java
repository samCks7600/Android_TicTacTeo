package com.example.tictacteo;

import static android.graphics.Color.parseColor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import android.os.Handler;

public class GameActivity extends AppCompatActivity {

    private SoundPool soundPool;
    private int sound_lose, sound_win, sound_draw, sound_play, sound_cpu, sound_btn;

    //for cpu input delayed
    final Handler handler = new Handler(Looper.getMainLooper());

    //for calculate play of Time.
    private long startTime = 0;

    private TextView winMessage;

    //this array will store 9 buttons in the game. row 1 :{0,1,2} , row 2 :{3,4,5} , row 3: {6,7,8} .
    private Button[] buttons = new Button[9];

    private Button btn_continue;

    //for calculate player and AI input of Counts.
    private int roundCount, duration;
    private String winningStatus;


    // 0 is empty, 1 is player input , 2 is CPU input. all default empty
    int[] gameTable = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    String gameRound = "player";


    //gameTable index
    //012
    //345
    //678
    int[][] winningWays = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}// rows -
            , {0, 3, 6}, {1, 4, 7}, {2, 5, 8}// columns |
            , {0, 4, 8}, {2, 4, 6}};// cross / or \

    private TextView winline;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //for winLine animation
        winline = findViewById(R.id.winLine);

        //set Sound pool
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(6)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
        }
        sound_lose = soundPool.load(this, R.raw.lose, 1);
        sound_win = soundPool.load(this, R.raw.win, 1);
        sound_draw = soundPool.load(this, R.raw.draw, 1);

        sound_play = soundPool.load(this, R.raw.playerbtn, 1);
        sound_cpu = soundPool.load(this, R.raw.cpubtn, 1);

        //continue btn
        sound_btn = soundPool.load(this, R.raw.btnclick, 1);
        //-------------


        startTime = System.currentTimeMillis();
        btn_continue = (Button) findViewById(R.id.btn_continue);
        winMessage = (TextView) findViewById(R.id.txt_WinText);
        roundCount = 0;

        //insert the 9 buttons in to buttons[array]
        for (int i = 0; i < buttons.length; i++) {

            String buttonID = "btn_num" + (i + 1);
            //resourceID will like R.id.btn_numX;
            int resourceID = getResources().getIdentifier(buttonID, "id", getPackageName());
            buttons[i] = (Button) findViewById(resourceID);
        }

    }

    public void input(View view) {
        boolean userInserted = false;

        if (gameRound.equals("cpu")) {
            return;
        }


        //if the button was selected by player or Ai, return.
        if (!((Button) view).getText().toString().equals("")) {
            return;
        }

        //get the index of button.
        // exp: btn_num3 , i can get int data type of value 3 .
        String buttonID = view.getResources().getResourceEntryName(view.getId());
        int gameStatePointer = Integer.parseInt(buttonID.substring(buttonID.length() - 1, buttonID.length()));

        //mark player point in the tic tac teo map
        gameTable[gameStatePointer - 1] = 1;

        //set Button Text to O.
        buttons[gameStatePointer - 1].setText("O");

        buttons[gameStatePointer - 1].setTextColor(parseColor("#36eef5"));

        soundPool.play(sound_play, 1, 1, 0, 0, 1);

        userInserted = true;
        gameRound = "cpu";
        roundCount++;

        //if player or AI win or Draw, gameEnd will return true.
        if (gameEnd()) {
            return;
        }


        if (roundCount < 9 && userInserted == true) {

            handler.postDelayed(new Runnable() {

                @Override
                public void run() {

                    //AiInput is mean CPU round and CPU will choose one button to select.
                    AiInput();
                    soundPool.play(sound_cpu, 1, 1, 0, 0, 1);
                    if (gameEnd()) {
                        return;
                    }

                }

            }, 600);

        }

    }

    public boolean gameEnd() {

        //checkWin() will return 0,1,2.
        // 0 means no one win.
        // 1 means player win.
        // 2 means CPU win.

        if (checkWin() == 2) {

            //winningStatus is use for sql insert statement .
            winningStatus = "Lose";
            duration = calculateTime();

            //set sound effect
            soundPool.play(sound_lose, 1, 1, 0, 0, 1);

            winMessage.setTextColor(Color.rgb(253, 10, 59));
            winMessage.setText("CPU Win! \n Duration: " + duration + " sec!");

            disableAllButton();
            btn_continue.setVisibility(View.VISIBLE);
            insertDB();
            return true;
        }
        if (checkWin() == 1) {

            //winningStatus is use for sql insert statement .
            winningStatus = "Win";
            duration = calculateTime();

            soundPool.play(sound_win, 1, 1, 0, 0, 1);
            winMessage.setTextColor(Color.rgb(10, 253, 245));
            winMessage.setText("Player Win! \n Duration: " + duration + " sec!");

            disableAllButton();
            btn_continue.setVisibility(View.VISIBLE);
            insertDB();
            return true;
        }
        if (checkWin() == 0 && roundCount == 9) {

            //winningStatus is use for sql insert statement .
            winningStatus = "Draw";
            duration = calculateTime();

            soundPool.play(sound_draw, 1, 1, 0, 0, 1);
            winMessage.setTextColor(Color.rgb(95, 215, 0));
            winMessage.setText("Peace! \n Duration: " + duration + " sec!");

            disableAllButton();
            btn_continue.setVisibility(View.VISIBLE);
            insertDB();
            return true;
        }
        return false;
    }

    public void AiInput() {

        if (gameTable.equals("player")) {
            return;
        }

        boolean inserted = false;
        Random random = new Random();
        //until CPU is selected button on the Tic Tac Teo table.
        while (!inserted) {
            int randomNumber = random.nextInt(9);
            if (buttons[randomNumber].getText().toString().equals("")) {
                gameTable[randomNumber] = 2;
                buttons[randomNumber].setText("X");
                buttons[randomNumber].setTextColor(parseColor("#f23d67"));

                //set sound effect
                soundPool.play(sound_play, 1, 1, 0, 0, 1);
                gameRound = "player";
                inserted = true;
                roundCount++;

            }
        }
    }


    public int checkWin() {
        boolean AiWin = false;
        boolean PlayerWin = false;


        //loop winningWays array,if 3 index is same and value is 1 (Player input),Player will win the game.
        for (int[] winningWay : winningWays) {
            if (gameTable[winningWay[0]] == 1 &&
                    gameTable[winningWay[1]] == 1 &&
                    gameTable[winningWay[2]] == 1) {

                //set the set Win Line (8 ways)
                setWinLine(winningWay[0], winningWay[1], winningWay[2]);

                winline.setBackgroundColor(parseColor("#08c993"));
                winline.setVisibility(View.VISIBLE);

                PlayerWin = true;

            }
        }

        //loop winningWays array,if 3 index is same and value is 2 (CPU input input),CPU will win the game.
        for (int[] winningWay : winningWays) {
            if (gameTable[winningWay[0]] == 2 &&
                    gameTable[winningWay[1]] == 2 &&
                    gameTable[winningWay[2]] == 2) {

                //set the set Win Line (8 ways)
                setWinLine(winningWay[0], winningWay[1], winningWay[2]);

                winline.setBackgroundColor(parseColor("#ff0026"));
                winline.setVisibility(View.VISIBLE);

                AiWin = true;

            }
        }

        if (PlayerWin) {
            return 1;
        } else if (AiWin) {
            return 2;
        } else {
            return 0;
        }

    }

    public void setWinLine(int index_1, int index_2, int index_3) {

        if (index_1 == 0 && index_2 == 1 && index_3 == 2) {
            winline.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.winline_top_raw));
            return;
        }
        if (index_1 == 3 && index_2 == 4 && index_3 == 5) {
            //just keep default
            return;
        }
        if (index_1 == 6 && index_2 == 7 && index_3 == 8) {
            winline.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.winline_bottom_raw));
            return;
        }
        if (index_1 == 0 && index_2 == 3 && index_3 == 6) {
            winline.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.winline_left_column));
            return;
        }
        if (index_1 == 1 && index_2 == 4 && index_3 == 7) {
            winline.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.winline_middle_column));
            return;
        }
        if (index_1 == 2 && index_2 == 5 && index_3 == 8) {
            winline.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.winline_right_column));
            return;
        }
        if (index_1 == 0 && index_2 == 4 && index_3 == 8) {
            winline.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.winline_left_cross));
            return;
        }
        if (index_1 == 2 && index_2 == 4 && index_3 == 6) {
            winline.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.winline_right_cross));
            return;
        }

    }

    //When Some one win or Peace , let user can't click game button more.
    public void disableAllButton() {

        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setEnabled(false);
        }

    }

    //calculateTime() will set in the onCreate()
    //These code for calculating Time to display Duration and insert to db.
    public int calculateTime() {
        long finishTime = System.currentTimeMillis();
        int elapsedTime = (int) (finishTime - startTime) / 1000;

        return elapsedTime;
    }

    //insert record when player click this button and jump to the MainActivity.
    public void btn_continue(View view) {

        soundPool.play(sound_btn, 1, 1, 0, 0, 1);
        finish();
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

    public void insertDB() {
        SQLiteDatabase db;
        String sql;
        Cursor cursor = null;

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String gametime = timeFormat.format(new Date());

        String date = sdf.format(new Date());

        //connect db and insert
        try {

            db = SQLiteDatabase.openDatabase("/data/data/com.example.tictacteo/GameDB", null, SQLiteDatabase.CREATE_IF_NECESSARY);
            sql = "CREATE TABLE IF NOT EXISTS GameLog(gameID INTEGER PRIMARY KEY Autoincrement NOT NULL,playDate text,playTime text,duration INTEGER,winningStatus text);";
            db.execSQL(sql);

            db.execSQL("INSERT INTO GameLog(playDate,playTime,Duration,winningStatus) values" + "('" + date + "','" + gametime + "'," + duration + ",'" + winningStatus + "')");
            db.close();

        } catch (SQLException e) {
            Log.d("Debug ", e.getMessage());
        }

    }


}
