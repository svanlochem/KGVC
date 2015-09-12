package com.kgvc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity {
    SharedPreferences prefs;

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

        prefs = getSharedPreferences(SplashScreen.PREFS_NAME,0);

        //If this activity is called using the settings button, remove all team settings in order to let the user to choose another team
        if(prefs.getBoolean("settings", false)){
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.commit();
        }

        mSingleton = this;

        ListenerOnCompetitionSpinner();
        ListenerOnTeamSpinner();
    }

    private void ListenerOnCompetitionSpinner() {
        competitionSpinner = (Spinner) findViewById(R.id.spinner_competition);

        competitionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {

                boolean compSelected = false;

                if(pos==1)      {competitionURL="http://www.toernooi.nl/sport/teams.aspx?id=226F03F4-856F-4DD3-BF1F-CF0BD180D65B"; compSelected=true;} //17.00-18.00
                else if(pos==2) {competitionURL="http://www.toernooi.nl/sport/teams.aspx?id=1E0B0979-4E31-4E64-8BC3-3770E8720B26"; compSelected=true;} //18.00-19.00
                else if(pos==3) {competitionURL="http://www.toernooi.nl/sport/teams.aspx?id=CEA1B07A-F14D-4651-A4D9-509712DB0C36"; compSelected=true;} //19.00-20.00
                else if(pos==4) {competitionURL="http://www.toernooi.nl/sport/teams.aspx?id=FFEE1FE7-F57F-49F2-93DD-C01D796E2828"; compSelected=true;} //20.00-21.00
                else if(pos==5) {competitionURL="http://www.toernooi.nl/sport/teams.aspx?id=F181A6FB-29A1-4C23-A8CC-B3022948F255"; compSelected=true;} //21.00-22.00
                else if(pos==6) {competitionURL="http://www.toernooi.nl/sport/teams.aspx?id=52059CF5-0838-4E85-9867-479A33E206B3"; compSelected=true;} //22.00-23.00

                if(compSelected){
                    CompetitionTask = new MyCompetitionTask();
                    CompetitionTask.execute(competitionURL);
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {    }
        });
    }

    private void ListenerOnTeamSpinner() {
        TeamSpinner = (Spinner) findViewById(R.id.spinner_team);

        TeamSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {

                String TeamName = parent.getSelectedItem().toString();
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("chosenTeamName", TeamName);
                editor.putInt("chosenTeamNumber",pos-1);
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

    class MyCompetitionTask extends AsyncTask<String, Void, Document> {
        ProgressDialog progress;
        Document doc;
        Elements table;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(mSingleton);
            progress.setTitle("Loading");
            progress.setMessage("Teams laden...");
            progress.setCancelable(true);
            progress.show();
        }

        protected Document doInBackground(String... params) {
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
            progress.dismiss();

//            SharedPreferences.Editor editor = prefs.edit();
            Elements tablebody;
            SharedPreferences.Editor editor = prefs.edit();

            tablebody   = result.select("table > tbody > tr > td:eq(0)");

            ArrayList<String> teams = new ArrayList<String>();
            teams.add("Kies een team");
            for(int i=0; i<tablebody.size();i++){
                teams.add(tablebody.get(i).text());

                //Save team names
                String teamName =  "teamName" + Integer.toString(i+1);
                editor.putString(teamName,tablebody.get(i).text());
            }

            editor.putInt("competitionSize",tablebody.size());
            editor.commit();

            String[] teamstr = new String[teams.size()];
            teams.toArray(teamstr);

            fillTeamSpinner(teamstr);
        }
    }

    class MyTeamTask extends AsyncTask<String, Void, Document> {
        Document doc;
        Elements table;

        protected Document doInBackground(String... params) {
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
            tablebody = result.select("td:nth-last-child(2n) a");

            String teamPageURL = tablebody.get(TeamNumber-1).attr("href");

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

            editor.putBoolean("alreadyChosenTeam", true);

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