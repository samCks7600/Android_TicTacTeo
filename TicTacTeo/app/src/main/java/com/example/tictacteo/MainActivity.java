package com.example.tictacteo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {


    private SoundPool soundPool;
    private int sound_btn;
    private Button PassGameBtn, PassRankBtn, PlayerRecordBtn, CloseBtn;
    MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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


        //back ground music
        player = MediaPlayer.create(this,R.raw.main);
        player.setLooping(true);
        player.start();

        //set btn
        PassGameBtn = findViewById(R.id.btn_Play);
        PassRankBtn = findViewById(R.id.btn_GameRank);
        PlayerRecordBtn = findViewById(R.id.btn_YourRecord);
        CloseBtn = findViewById(R.id.btn_Close);

        //set Btn to implement the animation
        PassGameBtn.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.mainstart_btn1));
        PassRankBtn.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.mainstart_btn2));
        PlayerRecordBtn.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.mainstart_btn3));
        CloseBtn.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.mainstart_btn4));

    }

    public void PassGameActivity(View view) {

        soundPool.play(sound_btn,1,1,0,0,1);
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

    public void PassRankActivity(View view) {

        soundPool.play(sound_btn,1,1,0,0,1);
        Intent intent = new Intent(this, GameRankActivity.class);
        startActivity(intent);
    }

    public void PlayerRecordActivity(View view) {

        soundPool.play(sound_btn,1,1,0,0,1);
        Intent intent = new Intent(this, PlayerRecordActivity.class);
        startActivity(intent);
    }

    public void CloseGame(View view) {

        soundPool.play(sound_btn,1,1,0,0,1);
        finish();
        System.exit(0);
    }

}