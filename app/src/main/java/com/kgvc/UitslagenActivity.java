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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class UitslagenActivity extends Activity {
    SharedPreferences prefs;
    private static final String TAG = StandenActivity.class.getSimpleName();

    Context mSingleton;
    ScrollView ScrollLayout;
    TableLayout TabLayout;
    MyTask mt;
    private Spinner spinner;
    boolean firstIgnoreSpinner = false;
    String URL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropdown_team);

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
        URL = URLa + "teammatches" + URLb;

        fillTeamSpinner();

        addListenerOnSpinnerItemSelection();

        mt = new MyTask();
        mt.execute(URL);
    }

    @Override
    protected void onResume(){
        super.onResume();

        LinearLayout ll = (LinearLayout)findViewById(R.id.background);
        ll.getBackground().setAlpha(75);
    }

    private void fillTeamSpinner(){
        ArrayList<String> teamstr = new ArrayList<String>();
        for(int i=0;i<prefs.getInt("competitionSize", -1);i++){
            String teamName = "teamName" + Integer.toString(i+1);
            teamstr.add(prefs.getString(teamName, null));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, teamstr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sItems = (Spinner) findViewById(R.id.spinner2);
        sItems.setAdapter(adapter);
    }

    private void addListenerOnSpinnerItemSelection() {
        spinner = (Spinner) findViewById(R.id.spinner2);

        spinner.setSelection(prefs.getInt("chosenTeamNumber", -1));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {

                if(firstIgnoreSpinner) {
                    changeTeam(pos);
                }
                else {
                    firstIgnoreSpinner = true;
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {    }
        });
    }

    public void changeTeam(int pos){
        String teamID = "team" + Integer.toString(pos+1);
        String TeamNumber = prefs.getString(teamID, null);
        String URLa = URL.substring(0,90);
        URL = URLa + TeamNumber;
        Log.d(TAG, URL);

        TabLayout.removeAllViews();

        mt = new MyTask();
        mt.execute(URL);
    }

    private TableRow createHeaderRow(String[] headerstr) {
        TableRow row1 = new TableRow(this);

        headerstr[1] = "Datum";
        headerstr[4] = "";

        for (int i = 0; i < headerstr.length; i++) {
            if(i!=0 && i!=2) {
                TextView t1 = new TextView(this);
                t1.setPadding(10, 0, 10, 0);
                t1.setText(headerstr[i]);
                row1.addView(t1);
            }
        }
        return row1;
    }

    private TableRow createRow(String[] headerstr) {
        TableRow row1 = new TableRow(this);

        headerstr[1]=headerstr[1].substring(3,headerstr[1].length()-6);
        if(headerstr[3].length() > 15) {headerstr[3] = headerstr[3].substring(0, 15); }
        if(headerstr[5].length() > 15) {headerstr[5] = headerstr[5].substring(0, 15); }

        for (int i = 0; i < headerstr.length; i++) {
            if(i!=0 && i!=2) {
                TextView t1 = new TextView(this);
                t1.setPadding(10, 0, 10, 0);
                t1.setText(headerstr[i]);
                row1.addView(t1);
            }
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
            //tvInfo.setText(result.text());
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
            case R.id.standen:{
                Intent intent = new Intent(this, StandenActivity.class);
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
