package com.kgvc;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class SplashScreen extends Activity {
    public static final String PREFS_NAME = "appSettings";
    SharedPreferences prefs;

    /** Duration of wait **/
    private int SPLASH_DISPLAY_LENGTH = 4000;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_screen);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setQuote();
        prefs = getSharedPreferences(PREFS_NAME,0);

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create Intents that will start the Main Activity or the StandenActivity. */
                if(prefs.getBoolean("alreadyChosenTeam",false)) {
                    Intent intent = new Intent(SplashScreen.this, StandenActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    SplashScreen.this.startActivity(intent);
                    SplashScreen.this.finish();
                }
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if(prefs.getBoolean("alreadyChosenTeam",false)) {
                Intent intent = new Intent(SplashScreen.this, StandenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                SplashScreen.this.startActivity(intent);
                SplashScreen.this.finish();
            }
        }

        return true;
    }

    private void setQuote(){
        String[] quotes =   getResources().getStringArray(R.array.quotes);
        int arrayLength = quotes.length;

        SimpleDateFormat formatterDay = new SimpleDateFormat( "D" );
        String Day = formatterDay.format( new java.util.Date() );
        int Days  = Integer.parseInt(Day);

        TextView tv1 = (TextView)findViewById(R.id.quotetextView);
        tv1.setText(quotes[Days%arrayLength]);
    }
}