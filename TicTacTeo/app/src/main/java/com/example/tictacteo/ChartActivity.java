package com.example.tictacteo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class ChartActivity extends AppCompatActivity {

    int WinNumRows,DrawNumRows,LoseNumRows;
    float WinPercentage,DrawPercentage,LosePercentage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.example.tictacteo/GameDB", null, SQLiteDatabase.CREATE_IF_NECESSARY);
        WinNumRows = (int) DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM GameLog Where winningStatus = 'Win';", null);
        DrawNumRows = (int) DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM GameLog Where winningStatus = 'Draw';", null);
        LoseNumRows = (int) DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM GameLog Where winningStatus = 'Lose';", null);


        //change player winning status Count of Win,Draw,lose. to a Percentage.
        //The Sum of Percentage will be 100%.
        WinPercentage = ( WinNumRows / (float)(WinNumRows+DrawNumRows+LoseNumRows) )*100;
        DrawPercentage = ( DrawNumRows / (float)(WinNumRows+DrawNumRows+LoseNumRows) )*100;
        LosePercentage = ( LoseNumRows / (float)(WinNumRows+DrawNumRows+LoseNumRows) )*100;

        setContentView(new Panel(this));



    }

    class Panel extends View {

        public Panel(Context context) {
            super(context);
        }

        String title = "Your Winning Status";
        String items[] = {"Win","Draw" , "Lose"};


        float data[] = {WinPercentage, DrawPercentage, LosePercentage};
        int rColor[] = {0xff32cd32, 0xffffff00,0xffff0000};
        float cDegree = 0;


        public void onDraw(Canvas c) {
            super.onDraw(c);
            Paint paint = new Paint();

            paint.setColor(Color.BLACK);
            c.drawPaint(paint);

            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);

            for (int i = 0; i < data.length; i++) {
                float drawDegree = data[i] * 360 / 100;
                // draw the pie chart, select the the color
                paint.setColor(rColor[i]);
                //set the pie size used as padding, left 50, top 100, right 450, bottom 450

                RectF rec = new RectF(0, getHeight()/9, getWidth(), getWidth());
                c.drawArc(rec, cDegree, drawDegree, true, paint);
                cDegree += drawDegree;
            }

            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(getWidth()/10);
            c.drawText(title, 20, getHeight()/10, paint);

            int vSpace = getHeight()-(getHeight()/3);
            paint.setTextSize(getWidth()/15);
            for (int i = items.length - 1; i >= 0; i--) {
                paint.setColor(rColor[i]);
                c.drawRect(getWidth()/2, vSpace, getWidth()/2 -30, vSpace + 50, paint);

                paint.setColor(Color.WHITE);
                c.drawText(items[i], getWidth()/2 + 30, vSpace +vSpace/22, paint);
                vSpace -= 60;
            }
        }
    }
}