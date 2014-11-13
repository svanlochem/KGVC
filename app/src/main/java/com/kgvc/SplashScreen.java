package com.kgvc;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.TextView;

public class SplashScreen extends Activity {
    public static final String PREFS_NAME = "appSettings";
    SharedPreferences prefs;

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 1500;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_screen);

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

    private void setQuote(){

        TextView tv1 = (TextView)findViewById(R.id.quotetextView);
        tv1.setText("\"Hij zat er allang in voor het buitenspel was\" - Roel de Koning");

    }
}
