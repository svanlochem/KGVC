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
import android.util.Log;
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

public class StandenActivity extends Activity {

    SharedPreferences prefs;
    private static final String TAG = StandenActivity.class.getSimpleName();

    Context mSingleton;
    ScrollView ScrollLayout;
    TableLayout TabLayout;
    MyTask mt;

    String URL = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standen);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ScrollLayout = (ScrollView)findViewById(R.id.scrollView1);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setTitle(prefs.getString("chosenTeamName", "ERROR!!"));

        //Create the layout
        TabLayout = new TableLayout(this);
        TabLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        TabLayout.setStretchAllColumns(true);

        ScrollLayout.addView(TabLayout);

        mSingleton = this;

        URL = prefs.getString("teamURL", null);
        String URLa = URL.substring(0,29);
        String URLb = URL.substring(33);
        URL = URLa + "teamstandings" + URLb;

//        Log.d(TAG, URL);

        mt = new MyTask();
        mt.execute(URL);
    }

    @Override
    protected void onResume(){
        super.onResume();

        LinearLayout ll = (LinearLayout)findViewById(R.id.background);
        ll.getBackground().setAlpha(75);
    }

    private TableRow createHeaderRow(String[] headerstr) {
        TableRow row1 = new TableRow(this);

        for (int i = 0; i < headerstr.length; i++) {
            TextView t1 = new TextView(this);
            t1.setPadding(10, 0, 10, 0);

            headerstr[2] = "P";
            headerstr[3] = "g";
            headerstr[4] = "W";
            headerstr[5] = "G";
            headerstr[6] = "V";
            headerstr[7] = "+";
            headerstr[8] = "-";

            t1.setText(headerstr[i]);
            row1.addView(t1);
        }
        return row1;
    }

    private TableRow createRow(String[] headerstr) {
        TableRow row1 = new TableRow(this);
        boolean makeBold;
        if(headerstr[1].equalsIgnoreCase(prefs.getString("chosenTeamName", "ERROR!!"))) {
            makeBold = true;
        } else {
            makeBold = false;
        }
        for (int i = 0; i < headerstr.length; i++) {
            TextView t1 = new TextView(this);
            t1.setPadding(10, 0, 10, 0);
            if(headerstr[i].length() > 15) {headerstr[i] = headerstr[i].substring(0, 15); }
            t1.setText(headerstr[i]);
            if(makeBold) {
                t1.setTypeface(null, Typeface.BOLD);
                t1.setTextColor(Color.RED);
            }
            row1.addView(t1);
        }
        return row1;
    }

    class MyTask extends AsyncTask<String, Void, Document> {
        ProgressDialog progress;
        Document doc;
        Elements table;
        Elements tablebody;
        String what1="failed";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(mSingleton);
            progress.setTitle("Loading");
            progress.setMessage("Uitslagen laden...");
            progress.setCancelable(true);
            progress.show();
        }

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
            //tvInfo.setText(result.text());\
            progress.dismiss();

            Elements tableheader;
            Elements tablebody;
            tableheader = result.select("table > thead > tr > td");
            tablebody   = result.select("table > tbody > tr");

            ArrayList<String> header = new ArrayList<String>();
            ListIterator<Element> postIt = tableheader.listIterator();
            for(int i = 0; i < 10; i++){
                if(postIt.hasNext()){
                    header.add(postIt.next().text());
                }
            }

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
            case R.id.uitslagen:{
                Intent intent = new Intent(this, UitslagenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;}
//            case R.id.programma:{
//                Intent intent = new Intent(this, ProgrammaActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//                return true;}
//            case R.id.weekuitslagen:{
//                Intent intent = new Intent(this, WeekUitslagenActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//                return true;}
//            case R.id.fieldmap:{
//                Intent intent = new Intent(this, FieldMapActivity.class);
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

//Toast.makeText(getApplicationContext(), prefs.getString("teamURL", "Does not exist!!"), Toast.LENGTH_LONG).show();
//Log.d(TAG, prefs.getString("teamURL", "Does not exist!!"));