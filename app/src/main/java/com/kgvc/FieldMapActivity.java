package com.kgvc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

public class FieldMapActivity extends Activity {
    SharedPreferences prefs;

    Context mSingleton;
    ImageView icon;
    RelativeLayout.LayoutParams parameters;
    MyTask mt;

    String URL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.field_map);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setTitle(prefs.getString("chosenTeamName", "ERROR!!"));

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mSingleton = this;

        URL = prefs.getString("teamURL", null);

        String URLa = URL.substring(0,29);
        String URLb = URL.substring(33);
        URL = URLa + "matches" + URLb;

        mt = new MyTask();
        mt.execute(URL);
    }


    @Override
    protected void onResume(){
        super.onResume();

        RelativeLayout rl = (RelativeLayout)findViewById(R.id.background);
        rl.getBackground().setAlpha(75);
    }

    public int[] getFieldLoc (String Field){

        int left   = 0;
        int top    = 0;


        if(Field.equals("H1A")) {//Hockey 1A
            left   = 600;
            top    = 240;
        }
        else if(Field.equals("H1B")) {//Hockey 1B
            left   = 600;
            top    = 550;
        }
        else if(Field.equals("H2A")) {//Hockey 2A
            left   = 380;
            top    = 240;
        }
        else if(Field.equals("H2B")) {//Hockey 2B
            left    = 380;
            top     = 550;
        }
        else if(Field.equals("H3A")) {//Hockey 3A
            left   = 590;
            top    = 800;
        }
        else if(Field.equals("H3B")) {//Hockey 3B
            left   = 590;
            top    = 940;
        }
        else if(Field.equals("V1A")) {//Voetbal 1A
            left   = 125;
            top    = 170;
        }
        else if(Field.equals("V1B")) {//Voetbal 1B
            left   = 125;
            top    = 550;
        }
        else if(Field.equals("V2A")) {//Voetbal 2A
            left   = 385;
            top    = 875;
        }
        else if(Field.equals("V2B")) {//Voetbal 2B
            left   = 85;
            top    = 875;
        }
        return new int[] {left,top};
    }

    ///////////////////
    class MyTask extends AsyncTask<String, Void, Document> {
        ProgressDialog progress;
        Document doc;
        Elements table;
        Elements tablebody;
        String what1="failed";

//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//        }

        protected Document doInBackground(String... params) {
            // TimeUnit.SECONDS.sleep(2);
            String url=params[0];
            try {
                doc = Jsoup.connect(url).get();
                table = doc.select("table");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return doc;
        }

        protected void onPostExecute(Document result) {
            super.onPostExecute(result);

            String Field = "";

            Elements tablebody;

            tablebody   = result.select("table~table > tbody > tr");


            ListIterator<Element> rowIt = tablebody.listIterator();
            for (int i = 0; i < 20; i++) {
                if(rowIt.hasNext()){
                    Element onerow = rowIt.next();
                    Elements cols = onerow.select("td");
                    ListIterator<Element> colIt = cols.listIterator();
                    ArrayList<String> row = new ArrayList<String>();
                    for (int j = 0; j < 10; j++) {
                        if(colIt.hasNext()){
                            row.add(colIt.next().text());
                        }
                    }
                    String[] rowstr = new String[row.size()];
                    row.toArray(rowstr);

                    if(rowstr[3].contains("FC uit de Goot") || rowstr[5].contains("FC uit de Goot")){
                        Field = rowstr[7].substring(16,17) + rowstr[7].substring(rowstr[7].length()-2);

                        int[] location = getFieldLoc(Field);

                        icon = (ImageView)findViewById(R.id.icon);
                        parameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);

                        parameters.leftMargin = location[0];
                        parameters.topMargin  = location[1];

                        icon.setLayoutParams(parameters);
                        icon.setScaleX((float) 0.60);
                        icon.setScaleY((float) 0.60);
                        icon.bringToFront();
                        icon.setVisibility(ImageView.VISIBLE);

                        break;
                    }
                }
            }

        }
    }
    /////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.standen:{
                Intent intent = new Intent(this, StandenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;}
            case R.id.uitslagen:{
                Intent intent = new Intent(this, UitslagenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;}
            case R.id.programma:{
                Intent intent = new Intent(this, ProgrammaActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;}
//            case R.id.weekuitslagen:{
//                Intent intent = new Intent(this, WeekUitslagenActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//                return true;}
            case R.id.settings:{
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("settings", true);
                editor.putBoolean("alreadyChosenTeam", false);
                editor.commit();
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;}
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
