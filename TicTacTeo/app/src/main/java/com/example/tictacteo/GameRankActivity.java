package com.example.tictacteo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class GameRankActivity extends AppCompatActivity {
    private Button btnGoBack;
    private String tvUrl;
    private ListView RankList;
    private String[] listItems;


    //set soundpool
    private SoundPool soundPool;
    private int sound_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_rank);

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

        soundPool.play(sound_btn,1,1,0,0,1);




        btnGoBack = findViewById(R.id.btn_GoBack);
        RankList = findViewById(R.id.RankList);

        StrictMode.ThreadPolicy tp = StrictMode.ThreadPolicy.LAX;
        StrictMode.setThreadPolicy(tp);

        run();
    }

    //@Override
    public void run() {
        InputStream inputStream = null;

        String result = "";

        URL url = null;

        try {
            url = new URL("http://10.0.2.2/ranking_api.php");

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // Make GET request
            con.setRequestMethod("GET");  //"GET" is default.
            con.connect();



            inputStream = con.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line = "";
            while ((line = bufferedReader.readLine()) != null)
                result += line;

            inputStream.close();

            JSONObject jsonObject;
            JSONArray jsonArr = new JSONArray(result);

            //here is sorting Part.
            JSONArray sortedJsonArray = new JSONArray();
            listItems = new String[jsonArr.length()];

            //set all jsonArr value to  json Values.
            List<JSONObject> jsonValues = new ArrayList<JSONObject>();
            for (int i = 0; i < jsonArr.length(); i++) {
                jsonValues.add(jsonArr.getJSONObject(i));
            }

            //compare the data to sorting.
            Collections.sort( jsonValues, new Comparator<JSONObject>() {
                //You can change "Name" with "ID" if you want to sort by ID
                private static final String KEY_NAME = "Duration";

                @Override
                public int compare(JSONObject a, JSONObject b) {
                    int valA =0;
                    int valB =0;

                    try {
                        valA =(int) a.get(KEY_NAME);
                        valB =(int) b.get(KEY_NAME);
                    }
                    catch (JSONException e) {
                        Log.d("Debug compare() : ",e.toString());
                    }

                    return valA-valB;
                    //compare to
                }
            });

            // export the jsonValues to new array called sortedJsonArray.
            for (int i = 0; i < jsonArr.length(); i++) {
                sortedJsonArray.put(jsonValues.get(i));
            }

            //assign record to listItems[i];
            for (int i = 0; i < sortedJsonArray.length(); i++) {

                jsonObject = sortedJsonArray.getJSONObject(i);
                listItems[i] = "Rank "+ (i+1) + " \n" ;
                listItems[i] += jsonObject.getString("Name")+ " ,";
                listItems[i] += jsonObject.getString("Duration") + " sec";

            }

            //listItems array adapt to ListView.
            RankList.setAdapter(new ArrayAdapter<String>(this,R.layout.listviewstyle,R.id.listViewText_view, listItems));

        } catch (Exception e) {

            Log.d("Debug 2: ", e.toString() );
        }
    }

    public void GoBack(View view) {

        soundPool.play(sound_btn,1,1,0,0,1);
        finish();

    }

}