package com.kgvc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;

public class SplashScreen extends Activity {
    public static final String PREFS_NAME = "appSettings";
    SharedPreferences prefs;
    MyTask mt;
    String latestVersion = "";
    String packageName = "";

    // flag for Internet connection status
    Boolean isInternetPresent = false;
    Boolean touched = false;

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

        packageName = getCurrentVersion();
        TextView tv2 = (TextView)findViewById(R.id.VersionNumbertextView);
        tv2.setText("Versie: " + packageName);

        mt = new MyTask();
        mt.execute();

        setQuote();

        prefs = getSharedPreferences(PREFS_NAME,0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touched = true;
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
                startActivity(intent);
            }
        }
        else {
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

    private void checkVersion(){
        boolean updateAvailable = false;

        int latesta = Integer.parseInt(latestVersion.substring(0,1));
        int latestb = Integer.parseInt(latestVersion.substring(2,3));
        int latestc = Integer.parseInt(latestVersion.substring(4));

        int currenta = Integer.parseInt(packageName.substring(0,1));
        int currentb = Integer.parseInt(packageName.substring(2,3));
        int currentc = Integer.parseInt(packageName.substring(4));

        if (latesta > currenta){ updateAvailable = true;}
        else if (latestb > currentb){ updateAvailable = true;}
        else if (latestc > currentc){ updateAvailable = true;}

        if(updateAvailable){
            View b = findViewById(R.id.button);
            b.setVisibility(View.VISIBLE);
            b.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.dropbox.com/s/7peizo64ticwcgz/KGVC.apk?dl=0")));
                }
            });
        }
    }

    public void onClick(View v) {
        //handle the click events here, in this case open www.google.com with the default browser
        startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.google.com")));
        finish();
        moveTaskToBack(true);
    }

    private String getCurrentVersion(){
        String packageName = "";

        try {
            packageName = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;

        } catch (PackageManager.NameNotFoundException e){
                Log.w("NameNotFoundException","Error",e);
        }

        return packageName;
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

    private class MyTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            String path ="https://www.dropbox.com/s/olr7mcoaizy3yue/mostRecentVersion.txt?dl=1";
            URL u = null;
            try {
                u = new URL(path);
                HttpURLConnection c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("GET");
                c.connect();
                InputStream in = c.getInputStream();
                final ByteArrayOutputStream bo = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                in.read(buffer); // Read from Buffer.
                bo.write(buffer); // Write Into Buffer.

                latestVersion = bo.toString().substring(0, 5);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return latestVersion;
        }

        @Override
        protected void onPostExecute(String result) {
            checkVersion();

            if(!touched) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        chooseActivity();
                    }
                }, SPLASH_DISPLAY_LENGTH);
            }
        }
    }
}