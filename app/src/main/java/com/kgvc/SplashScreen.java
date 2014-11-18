package com.kgvc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

    // flag for Internet connection status
    Boolean isInternetPresent = false;

    // Connection detector class
    ConnectionDetector cd;

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
                chooseActivity();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            chooseActivity();
        }

        return true;
    }

    private void chooseActivity(){
        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();

        if (isInternetPresent) {

            if (prefs.getBoolean("alreadyChosenTeam", false)) {
                Intent intent = new Intent(SplashScreen.this, StandenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                SplashScreen.this.startActivity(intent);
                SplashScreen.this.finish();
            }
        } else {
            AlertDialog alert = new AlertDialog.Builder(SplashScreen.this).create();
            alert.setTitle("Geen internetverbinding!");
            alert.setMessage("Zet je internet aan!");
            alert.setButton(DialogInterface.BUTTON_POSITIVE ,"OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    SplashScreen.this.finish();
                }
            });
            alert.show();
        }
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

    public void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Showing Alert Message
        alertDialog.show();
    }
}