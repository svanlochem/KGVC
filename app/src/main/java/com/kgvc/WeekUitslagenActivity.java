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
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ListIterator;

public class WeekUitslagenActivity  extends Activity{
    SharedPreferences prefs;

    Context mSingleton;
    ScrollView ScrollLayout;
    TableLayout TabLayout;
    MyTask mt;
    private Spinner spinner;
    boolean firstIgnoreSpinner = false;
    String URL = "";
    String chosenTeam = "";
    String baseURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropdown);

        prefs = getSharedPreferences(SplashScreen.PREFS_NAME,0);
        chosenTeam = prefs.getString("chosenTeamName", "ERROR!!");
        setTitle(chosenTeam + " - Week Uitslagen");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ScrollLayout = (ScrollView)findViewById(R.id.scrollView1);

        //Create the layout
        TabLayout = new TableLayout(this);
        TabLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        TabLayout.setStretchAllColumns(true);

        ScrollLayout.addView(TabLayout);

        mSingleton = this;

        URL = findWeekURL(); //Retrieve the initial URL (for the nearest week)
        addListenerOnSpinnerItemSelection(); //Retrieve the spinner input URL

        mt = new MyTask();
        mt.execute(URL);
    }

    @Override
    protected void onResume(){
        super.onResume();

        LinearLayout ll = (LinearLayout)findViewById(R.id.background);
        ll.getBackground().setAlpha(75);
    }

    private void addListenerOnSpinnerItemSelection() {
        spinner = (Spinner) findViewById(R.id.spinner);

        if(URL.substring(84).equals("20150921")) spinner.setSelection(0);
        else if(URL.substring(84).equals("20150928")) spinner.setSelection(1);
        else if(URL.substring(84).equals("20151005")) spinner.setSelection(2);
        else if(URL.substring(84).equals("20151012")) spinner.setSelection(3);
        else if(URL.substring(84).equals("20151019")) spinner.setSelection(4);
        else if(URL.substring(84).equals("20151109")) spinner.setSelection(5);
        else if(URL.substring(84).equals("20151116")) spinner.setSelection(6);
        else if(URL.substring(84).equals("20151123")) spinner.setSelection(7);
        else if(URL.substring(84).equals("20151130")) spinner.setSelection(8);
        else if(URL.substring(84).equals("20151207")) spinner.setSelection(9);
        else if(URL.substring(84).equals("20151214")) spinner.setSelection(10);
        else if(URL.substring(84).equals("20160104")) spinner.setSelection(11);
        else if(URL.substring(84).equals("20160111")) spinner.setSelection(12);
        else if(URL.substring(84).equals("20160208")) spinner.setSelection(13);
        else if(URL.substring(84).equals("20160215")) spinner.setSelection(14);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                if (firstIgnoreSpinner) {
                    changeWeek(pos);
                } else {
                    firstIgnoreSpinner = true;
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    public void changeWeek(int pos){
        String datePart = "";

        if(pos == 0) datePart = "20150921";
        else if(pos == 1) datePart = "20150928";
        else if(pos == 2) datePart = "20151005";
        else if(pos == 3) datePart = "20151012";
        else if(pos == 4) datePart = "20151019";
        else if(pos == 5) datePart = "20151109";
        else if(pos == 6) datePart = "20151116";
        else if(pos == 7) datePart = "20151123";
        else if(pos == 8) datePart = "20151130";
        else if(pos == 9) datePart = "20151207";
        else if(pos == 10) datePart = "20151214";
        else if(pos == 11) datePart = "20160104";
        else if(pos == 12) datePart = "20160111";
        else if(pos == 13) datePart = "20160208";
        else if(pos == 14) datePart = "20160215";

        URL = baseURL + datePart;

        TabLayout.removeAllViews();

        mt = new MyTask();
        mt.execute(URL);
    }

    private String findWeekURL(){

        SimpleDateFormat formatterYear = new SimpleDateFormat( "yyyy" );
        String year = formatterYear.format( new java.util.Date() );

        SimpleDateFormat formatterMonth = new SimpleDateFormat( "MM" );
        String month = formatterMonth.format( new java.util.Date() );
        int monthInt  = Integer.parseInt(month);

        SimpleDateFormat formatterDay = new SimpleDateFormat( "dd" );
        String day = formatterDay.format( new java.util.Date() );
        int dayInt = Integer.parseInt(day);

        SimpleDateFormat formatterDayS = new SimpleDateFormat("E");
        String dayS = formatterDayS.format( new java.util.Date() );

        int minday = 0;

        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        if (Calendar.MONDAY == dayOfWeek) minday = 0;
        else if (Calendar.TUESDAY == dayOfWeek) minday = 1;
        else if (Calendar.WEDNESDAY == dayOfWeek) minday = 2;
        else if (Calendar.THURSDAY == dayOfWeek) minday = 3;
        else if (Calendar.FRIDAY == dayOfWeek) minday = 4;
        else if (Calendar.SATURDAY == dayOfWeek) minday = 5;
        else if (Calendar.SUNDAY == dayOfWeek) minday = 6;

        dayInt = dayInt - minday;

        if(dayInt<1){
            c.add(Calendar.MONTH, -1);
            int monthMaxDays = c.getActualMaximum(Calendar.DAY_OF_MONTH);

            monthInt = monthInt - 1;          //set to previous month
            dayInt   = monthMaxDays + dayInt; //calculate the day in previous month
        }

        int intMonthLength = (int) Math.log10(monthInt) + 1;
        if(intMonthLength==1){
            String monthstr = Integer.toString(monthInt);
            month = "0"+ monthstr;
        }



        int intDayLength = (int) Math.log10(dayInt) + 1;
        if(intDayLength==1){
            String daystr = Integer.toString(dayInt);
            day = "0"+ daystr;
        } else{
            day = Integer.toString(dayInt);
        }

        String dateURL = year + month + day;
        Log.d("TEST",dateURL);

        if(dateURL.equals("20150907") || dateURL.equals("20150914")) {
            dateURL = "20150921";
        }
        if(dateURL.equals("20151026") || dateURL.equals("20151102")) {
            dateURL = "20151019";
        }
        if(dateURL.equals("20151221") || dateURL.equals("20151228")) {
            dateURL = "20151214";
        }
        if(dateURL.equals("20160118") || dateURL.equals("20160125") || dateURL.equals("20160201")) {
            dateURL = "20160111";
        }
        if(Integer.parseInt(dateURL)>20160216) {
            dateURL = "20160215";
        }

        URL = prefs.getString("teamURL", null);

        String URLa = URL.substring(0,29);
        String URLb = URL.substring(33,79);
        baseURL = URLa + "matches" + URLb + "d=";

        String URL = baseURL + dateURL;
        return URL;
    }

    public TableRow createHeaderRow(String[] headerstr) {
        TableRow row1 = new TableRow(this);

//        headerstr[4] = "";
//        headerstr[5] = "";

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

    public TableRow createRow(String[] headerstr) {
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
        if(headerstr[3].length() > 14) {headerstr[3] = headerstr[3].substring(0, 14); }
        if(headerstr[5].length() > 14) {headerstr[5] = headerstr[5].substring(0, 14); }

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
            progress.setMessage("Week laden...");
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

            Elements tableheader;
            Elements tablebody;

            tableheader = result.select("table~table > thead > tr > td");
            tablebody   = result.select("table~table > tbody > tr");

            ArrayList<String> header = new ArrayList<String>();
//            ListIterator<Element> postIt = tableheader.listIterator();
//            for(int i = 0; i < 10; i++){
//                if(postIt.hasNext()){
//                    header.add(postIt.next().text());
//                }
//            }
            //Add header fields
//            header.add("");
//            header.add("Veld");

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
            case R.id.programma:{
                Intent intent = new Intent(this, ProgrammaActivity.class);
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