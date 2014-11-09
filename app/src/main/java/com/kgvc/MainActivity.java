package com.kgvc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;


public class MainActivity extends Activity {

    SharedPreferences prefs;
    private static final String TAG = StandenActivity.class.getSimpleName();

    Context mSingleton;
    MyCompetitionTask CompetitionTask;
    MyTeamTask TeamTask;

    private Spinner competitionSpinner, TeamSpinner;
    String competitionURL  = "";
    String teamURL = "";
    int TeamNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        mSingleton = this;

        ListenerOnCompetitionSpinner();
        ListenerOnTeamSpinner();
    }

    private void ListenerOnCompetitionSpinner() {
        competitionSpinner = (Spinner) findViewById(R.id.spinner_competition);

        competitionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {

                boolean compSelected = false;

                if(pos==1)      {competitionURL="http://www.toernooi.nl/sport/teams.aspx?id=CF40B14B-71BD-413A-9B05-18B99D4890CC"; compSelected=true;} //17.00-18.00
                else if(pos==2) {competitionURL="http://www.toernooi.nl/sport/teams.aspx?id=5785966C-9644-4CAA-98CF-CBE646B14D8D"; compSelected=true;} //18.00-19.00
                else if(pos==3) {competitionURL="http://www.toernooi.nl/sport/teams.aspx?id=88BF628B-24D7-4CAA-BBA2-162C938F57E9"; compSelected=true;} //19.00-20.00
                else if(pos==4) {competitionURL="http://www.toernooi.nl/sport/teams.aspx?id=90E04910-E759-4DD0-96D2-2E2C943406C9"; compSelected=true;} //20.00-21.00
                else if(pos==5) {competitionURL="http://www.toernooi.nl/sport/teams.aspx?id=0ECD0EBE-8016-4A99-812C-EF0A20D03089"; compSelected=true;} //21.00-22.00
                else if(pos==6) {competitionURL="http://www.toernooi.nl/sport/teams.aspx?id=DEC55908-B9B3-4E37-8258-F335EB6C8136"; compSelected=true;} //22.00-23.00


                if(compSelected){
                    CompetitionTask = new MyCompetitionTask();
                    CompetitionTask.execute(competitionURL);
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {    }
        });
    }

    private void ListenerOnTeamSpinner() {
        competitionSpinner = (Spinner) findViewById(R.id.spinner_team);

        competitionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {

                String TeamName = parent.getSelectedItem().toString();
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("teamName", TeamName);
                editor.commit();


                TeamNumber = pos;

                if(TeamNumber!=0) {
                    TeamTask = new MyTeamTask();
                    TeamTask.execute(competitionURL);
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {    }
        });
    }

    private void fillTeamSpinner(String[] teamstr){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, teamstr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sItems = (Spinner) findViewById(R.id.spinner_team);
        sItems.setAdapter(adapter);
    }


    ///////////////////////////////////////////////////////////////////////
    class MyCompetitionTask extends AsyncTask<String, Void, Document> {
        ProgressDialog progress;
        Document doc;
        Elements table;
        Elements tablebody;
        String what1="failed";

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

            Elements tablebody;

            tablebody   = result.select("table > tbody > tr > td:eq(0)");

            ArrayList<String> teams = new ArrayList<String>();
            teams.add("Kies een team");
            ListIterator<Element> rowIt = tablebody.listIterator();
            for (int i = 0; i < 20; i++) {
                if(rowIt.hasNext()){
                    teams.add(rowIt.next().text());
                }
            }
            String[] teamstr = new String[teams.size()];
            teams.toArray(teamstr);

            fillTeamSpinner(teamstr);
        }
    }

    ///////////////////////////////////////////////////////////////////////
    class MyTeamTask extends AsyncTask<String, Void, Document> {
        ProgressDialog progress;
        Document doc;
        Elements table;
        Elements tablebody;
        String what1="failed";

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

            String teamPageURL = "ERROR";
            Elements tablebody;
            tablebody = result.select("td:nth-last-child(2n) a");

//            ListIterator<Element> rowIt = tablebody.listIterator();
//            for (int i = 0; i < TeamNumber; i++) {
//                teamPageURL = rowIt.next().attr("href");
//            }

            teamPageURL = tablebody.get(TeamNumber-1).attr("href");

            String basePart = "http://www.toernooi.nl";
            String compPart = teamPageURL.substring(0,57);
            String teamPart = teamPageURL.substring(62);

            teamURL = basePart + compPart + "tid=" + teamPart;

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("teamURL", teamURL);

            for(int i=0; i<tablebody.size();i++){
                String team =  "team" + Integer.toString(i+1);
                editor.putString(team,tablebody.get(i).attr("href").substring(62));
            }

            editor.commit();

            Intent intent = new Intent(MainActivity.this, StandenActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}

//Toast.makeText(getApplicationContext(), url, Toast.LENGTH_LONG).show();