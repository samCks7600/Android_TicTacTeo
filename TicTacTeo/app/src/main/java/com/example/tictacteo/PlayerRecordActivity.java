package com.example.tictacteo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class PlayerRecordActivity extends AppCompatActivity {

    //set sound pool
    private SoundPool soundPool;
    private int sound_btn;

    SQLiteDatabase db;
    String sql;
    Cursor cursor = null;

    String[] columns = {"gameID", "playDate", "playTime", "duration", "winningStatus"};
    private ListView RecordList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_record);

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
        sound_btn = soundPool.load(this,R.raw.btnclick,1);



        RecordList = findViewById(R.id.RecordList);
        initialDB();
    }

    public  void  initialDB(){

        try{

            //open db
            db = SQLiteDatabase.openDatabase("/data/data/com.example.tictacteo/GameDB",null,SQLiteDatabase.CREATE_IF_NECESSARY);
            sql = "CREATE TABLE IF NOT EXISTS GameLog(gameID INTEGER PRIMARY KEY Autoincrement NOT NULL,playDate text,playTime text,duration INTEGER,winningStatus text);";
            db.execSQL(sql);

            cursor = db.rawQuery("SELECT * FROM GameLog",null);

            String[] dataStr = new String[cursor.getCount()];

            //using cursor to get record one by one .
            int i = 0;
            while(cursor.moveToNext()){
                @SuppressLint("Range") String playDate = cursor.getString(cursor.getColumnIndex("playDate"));
                @SuppressLint("Range") String playTime = cursor.getString(cursor.getColumnIndex("playTime"));
                @SuppressLint("Range") int duration = cursor.getInt(cursor.getColumnIndex("duration"));
                @SuppressLint("Range") String winningStatus = cursor.getString(cursor.getColumnIndex("winningStatus"));
                dataStr[i] = playDate + "," + playTime + "\n" + winningStatus + "," +duration + " sec";
                i++;
            }

            //String array adapt to ListView
            //I using R.layout.listViewStyle,R.id.listViewText_view, because i have a xml file for text font design.
            RecordList.setAdapter(new ArrayAdapter<String>(this,R.layout.listviewstyle,R.id.listViewText_view, dataStr));
            db.close();

        }
        catch (SQLException e){

            Log.d("Debug ",e.getMessage());

        }

    }
    public void GoBack(View view) {

        soundPool.play(sound_btn,1,1,0,0,1);

        finish();
    }

    public void ShowChart(View view) {
        soundPool.play(sound_btn,1,1,0,0,1);
        Intent intent = new Intent(this, ChartActivity.class);
        startActivity(intent);
    }


}