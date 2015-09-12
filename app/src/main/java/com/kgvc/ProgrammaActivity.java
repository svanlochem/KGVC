package com.kgvc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

public class ProgrammaActivity extends Activity {
    SharedPreferences prefs;

    Context mSingleton;
    ScrollView ScrollLayout;
    TableLayout TabLayout;
    MyTask mt;

    String URL = "";
    String chosenTeam = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standen);

        prefs = getSharedPreferences(SplashScreen.PREFS_NAME,0);
        chosenTeam = prefs.getString("chosenTeamName", "ERROR!!");
        setTitle(chosenTeam + " - Programma");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ScrollLayout = (ScrollView)findViewById(R.id.scrollView1);

        //Create the layout
        TabLayout = new TableLayout(this);
        TabLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        TabLayout.setStretchAllColumns(true);

        ScrollLayout.addView(TabLayout);

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

        LinearLayout ll = (LinearLayout)findViewById(R.id.background);
        ll.getBackground().setAlpha(75);
    }

    private TableRow createWeekRow(String[] weekstr) {
        TableRow row1 = new TableRow(this);

        if(weekstr[0].length()>31) {
            weekstr[0] = weekstr[0].substring(31, weekstr[0].length());
        }

        for (int i = 0; i < weekstr.length; i++) {
            TextView t1 = new TextView(this);
            t1.setPadding(10, 0, 10, 0);
            t1.setTypeface(null, Typeface.BOLD);
            t1.setText(weekstr[i]);
            row1.addView(t1);
        }
        return row1;
    }

    private TableRow createHeaderRow(String[] headerstr) {
        TableRow row1 = new TableRow(this);

        if(headerstr.length > 4) {
            headerstr[4] = "";
            headerstr[5] = "";
        }

        for (int i = 0; i < headerstr.length; i++) {
            if(i!=0 && i!=1 && i!=2) {
                TextView t1 = new TextView(this);
                t1.setPadding(10, 0, 10, 0);
                t1.setTypeface(null, Typeface.BOLD);
                t1.setText(headerstr[i]);
                row1.addView(t1);
            }
        }
        return row1;
    }

    private TableRow createRow(String[] headerstr) {
        TableRow row1 = new TableRow(this);

        String FirstPart  = "ERROR";
        String SecondPart = "ERROR";

        if(headerstr[7].contains("Voetbal")) {
            FirstPart = headerstr[7].substring(16, headerstr[7].length()-7);
            SecondPart = headerstr[7].substring(28, headerstr[7].length());
        }

        if(headerstr[7].contains("Hockey")) {
            FirstPart = headerstr[7].substring(16, headerstr[7].length()-7);
            SecondPart = headerstr[7].substring(27, headerstr[7].length());
        }

        headerstr[7]=FirstPart + " " +SecondPart;
        if(headerstr[3].length() > 15) {headerstr[3] = headerstr[3].substring(0, 15); }
        if(headerstr[5].length() > 15) {headerstr[5] = headerstr[5].substring(0, 15); }

        boolean makeBold;

        if(headerstr[3].contains(chosenTeam) || headerstr[5].contains(chosenTeam)) {
            makeBold = true;
        } else {
            makeBold = false;
        }

        for (int i = 0; i < headerstr.length; i++) {
            if(i!=0 && i!=1 && i!=2) {
                TextView t1 = new TextView(this);
                t1.setPadding(10, 0, 10, 0);
                t1.setText(headerstr[i]);
                if(makeBold) {
                    t1.setTypeface(null, Typeface.BOLD);
                    t1.setTextColor(Color.RED);
                }
                row1.addView(t1);
            }
        }
        return row1;
    }


    class MyTask extends AsyncTask<String, Void, Document> {
        ProgressDialog progress;
        Document doc;
        Elements table;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(mSingleton);
            progress.setTitle("Loading");
            progress.setMessage("Programma laden...");
            progress.setCancelable(true);
            progress.show();
        }

        protected Document doInBackground(String... params) {
            String url=params[0];
            try {
                doc = Jsoup.connect(url).get();
                table = doc.select("table");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return doc;
        }

        protected void onPostExecute(Document result) {
            super.onPostExecute(result);
            progress.dismiss();

            Elements tableweek;
            Elements tableheader;
            Elements tablebody;

            tableweek   = result.select("table~table > caption");
            tableheader = result.select("table~table > thead > tr > td");
            tablebody   = result.select("table~table > tbody > tr");

            ArrayList<String> week = new ArrayList<String>();
            ListIterator<Element> weekIt = tableweek.listIterator();
            for(int i = 0; i < 10; i++){
                if(weekIt.hasNext()){
                    week.add(weekIt.next().text());
                }
            }

            String[] weekstr = new String[week.size()];
            week.toArray(weekstr);
            TabLayout.addView(createWeekRow(weekstr));



            ArrayList<String> header = new ArrayList<String>();
            ListIterator<Element> postIt = tableheader.listIterator();
            for(int i = 0; i < 10; i++){
                if(postIt.hasNext()){
                    header.add(postIt.next().text());
                }
            }
            //Add header fields
            header.add("");
            header.add("Veld");

            String[] headerstr = new String[header.size()];
            header.toArray(headerstr);
            TabLayout.addView(createHeaderRow(headerstr));

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
                    TabLayout.addView(createRow(rowstr));
                }
            }
        }
    }


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
            case R.id.weekuitslagen:{
                Intent intent = new Intent(this, WeekUitslagenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;}
            case R.id.fieldmap:{
                Intent intent = new Intent(this, FieldMapActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;}
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