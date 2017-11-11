package com.example.myfirstapp_withsplashscreen;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SplashScreen extends Activity {

    static ArrayList<String> leagues_array = new ArrayList<String>();
    static ArrayList<ArrayList<RowSchedule>> full_schedule_array = new ArrayList<ArrayList<RowSchedule>>();
    static ArrayList<ArrayList<RowRanking>> full_ranking_array = new ArrayList<ArrayList<RowRanking>>();
    static ArrayList<ArrayList> full_teams_array = new ArrayList<ArrayList>();
    static ArrayList<String[]> new_favorites_array = new ArrayList<String[]>();

    ArrayList<RowSchedule> favorite_schedule_array = new ArrayList<RowSchedule>();
    ArrayList<RowRanking> favorite_ranking_array = new ArrayList<RowRanking>();
    int favorite_index = 0;

    public static String last_update = "";

    public static int numero_di_prova = 123;

    private static final String[] PERMS_FIPAV={
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if( (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)  == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {

            Log.d("SplashScreen", "Starting MAIN_TASK from onCreate");
            main_task();
        } else {
            Log.d("SplashScreen", "Requesting all permissions from onCreate");
            ActivityCompat.requestPermissions(this, PERMS_FIPAV, 123);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode==123) {
            Log.d("SplashScreen", "Starting MAIN_TASK without case");
            main_task();
        }
        // consider to add check on which permission has been denied , and take actions ...
    }

    private BroadcastReceiver bReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("SplashScreen", "Broadcast received from UpdateIntentService, launching main activity");
            if(intent.getBooleanExtra("success", false)) {
                alternative_task();
            } else {
                // write a message asking the user to close and re-open the application
                Log.d("SplashScreen", "Unsuccessful intent received from UpdateIntentService, doing nothing");
                TextView text = (TextView)findViewById(R.id.testo_sotto);
                text.setText("Download fallito: chiudi e riapri l'applicazione.");

            }
        }
    };

    void main_task () {
        try {
            Log.d("SplashScreen", "Reading leagues");
            read_leagues_array();
            Log.d("SplashScreen", "Reading schedules");
            read_schedule_array();
            Log.d("SplashScreen", "Reading ranking");
            read_ranking_array();
            Log.d("SplashScreen", "Extracting teams");
            get_teams_array();

            // Try to load the favorites
            try {
                read_favorites();
            } catch (IOException e) {
                e.printStackTrace();
                // Do not reload all the data if favorite schedule/ranking are not present
                // Reset favorite league/team instead
                MainActivity.favorite_league = "";
                MainActivity.favorite_team = "";
                Log.d("SplashScreen", "favorites schedule and ranking not loaded!");
            }
            // Try to load the NEW favorites
            try {
                read_favorites_new();
            } catch (IOException e) {
                e.printStackTrace();
                new_favorites_array.clear();
                Log.d("SplashScreen", "new favorites schedule and ranking not loaded!");
            }

            Log.d("SplashScreen", "Launching main activity");
            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            intent.putStringArrayListExtra("leagues", leagues_array);
            intent.putExtra("last_update", last_update);
            startActivity(intent);

            // close this activity
            finish();

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("SplashScreen", "Got an exception, starting UpdateServiceReceiver");
            Intent intent= new Intent();
            intent.setAction("com.example.myfirstapp_withsplashscreen.FIRST_STARTUP");
            sendBroadcast(intent);
        }
    }

    void alternative_task () {
        try {
            Log.d("SplashScreen", "Reading leagues after broadcast");
            read_leagues_array();
            Log.d("SplashScreen", "Reading schedules after broadcast");
            read_schedule_array();
            Log.d("SplashScreen", "Reading ranking after broadcast");
            read_ranking_array();
            Log.d("SplashScreen", "Extracting teams after broadcast");
            get_teams_array();

            // Try to load the favorites
            try {
                read_favorites();
            } catch (IOException e) {
                e.printStackTrace();
                // Do not reload all the data if favorite schedule/ranking are not present
                // Reset favorite league/team instead
                MainActivity.favorite_league = "";
                MainActivity.favorite_team = "";
                Log.d("SplashScreen", "favorites schedule and ranking not loaded!");
            }

            Log.d("SplashScreen", "Successful intent received from UpdateIntentService, launching main activity");
            //put here whaterver you want your activity to do with the intent received
            Intent next_intent = new Intent(SplashScreen.this, MainActivity.class);
            next_intent.putStringArrayListExtra("leagues", leagues_array);
            next_intent.putExtra("last_update", last_update);
            startActivity(next_intent);

            // close this activity
            finish();

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("SplashScreen", "Got an exception after successful intent, doing nothing");
            TextView text = (TextView)findViewById(R.id.testo_sotto);
            text.setText("Download fallito: chiudi e riapri l'applicazione.");
        }
    }

    protected void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter("UpdateIntentService.UPDATE_DONE"));
    }

    protected void onPause (){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
    }

    void read_leagues_array () throws IOException {
        File cDir = getBaseContext().getCacheDir();
        File tempFile = new File(cDir.getPath() + "/" + "leagues_array.txt");
        FileReader fReader = new FileReader(tempFile);
        BufferedReader bReader = new BufferedReader(fReader);

        String strLine;
        int leagues_count = 0;
        if(leagues_array != null) leagues_array.clear();
        if( (strLine=bReader.readLine()) != null  ) leagues_count = Integer.parseInt(strLine);
        for(int i=0; i<leagues_count; i++) {
            if( (strLine=bReader.readLine()) != null  ) leagues_array.add(strLine);
        }
    }

    void read_schedule_array () throws IOException {
        File cDir = getBaseContext().getCacheDir();
        File tempFile = new File(cDir.getPath() + "/" + "full_schedule_array.txt");
        FileReader fReader = new FileReader(tempFile);
        BufferedReader bReader = new BufferedReader(fReader);

        String strLine;
        int leagues_count = 0;
        int rows_count = 0;
        if( (strLine=bReader.readLine()) != null  ) leagues_count = Integer.parseInt(strLine);
        for(int i=0; i<leagues_count; i++) {
            ArrayList<RowSchedule> single_league_schedule = new ArrayList<>();
            if( (strLine=bReader.readLine()) != null  ) rows_count = Integer.parseInt(strLine);

            for (int j=0; j<rows_count; j++) {
                RowSchedule schedule_row = new RowSchedule();
                if( (strLine=bReader.readLine()) != null  ) schedule_row.set_home(strLine);
                if( (strLine=bReader.readLine()) != null  ) schedule_row.set_away(strLine);
                if( (strLine=bReader.readLine()) != null  ) schedule_row.set_date(strLine);
                if( (strLine=bReader.readLine()) != null  ) schedule_row.set_hour(strLine);
                if( (strLine=bReader.readLine()) != null  ) schedule_row.set_place(strLine);
                if( (strLine=bReader.readLine()) != null  ) schedule_row.set_address(strLine);
                if( (strLine=bReader.readLine()) != null  ) schedule_row.set_home_score(strLine);
                if( (strLine=bReader.readLine()) != null  ) schedule_row.set_away_score(strLine);
                single_league_schedule.add(schedule_row);
            }
            full_schedule_array.add(single_league_schedule);
        }
    }

    void read_ranking_array () throws IOException {
        File cDir = getBaseContext().getCacheDir();
        File tempFile = new File(cDir.getPath() + "/" + "full_ranking_array.txt");
        FileReader fReader = new FileReader(tempFile);
        BufferedReader bReader = new BufferedReader(fReader);

        int write_day = 0;
        int write_month = 0;
        int write_year = 0;
        int write_hour = 0;
        int write_minute = 0;

        String strLine;
        for(int i=0; i<5; i++) {
            switch (i) {
                case 0: if( (strLine=bReader.readLine()) != null  ) write_day = Integer.parseInt(strLine); break;
                case 1: if( (strLine=bReader.readLine()) != null  ) write_month = Integer.parseInt(strLine); break;
                case 2: if( (strLine=bReader.readLine()) != null  ) write_year = Integer.parseInt(strLine); break;
                case 3: if( (strLine=bReader.readLine()) != null  ) write_hour = Integer.parseInt(strLine); break;
                case 4: if( (strLine=bReader.readLine()) != null  ) write_minute = Integer.parseInt(strLine); break;
                default: break;
            }
        }

        int leagues_count = 0;
        int rows_count = 0;
        if( (strLine=bReader.readLine()) != null  ) leagues_count = Integer.parseInt(strLine);
        for(int i=0; i<leagues_count; i++) {
            ArrayList<RowRanking> single_league_ranking = new ArrayList<>();
            if( (strLine=bReader.readLine()) != null  ) rows_count = Integer.parseInt(strLine);

            for (int j=0; j<rows_count; j++) {
                RowRanking ranking_row = new RowRanking();
                if( (strLine=bReader.readLine()) != null  ) ranking_row.set_team(strLine);
                if( (strLine=bReader.readLine()) != null  ) ranking_row.set_points(strLine);
                if( (strLine=bReader.readLine()) != null  ) ranking_row.set_played(strLine);
                if( (strLine=bReader.readLine()) != null  ) ranking_row.set_wons(strLine);
                if( (strLine=bReader.readLine()) != null  ) ranking_row.set_set_won(strLine);
                if( (strLine=bReader.readLine()) != null  ) ranking_row.set_set_lost(strLine);
                if( (strLine=bReader.readLine()) != null  ) ranking_row.set_ratio(strLine);
                single_league_ranking.add(ranking_row);
            }
            full_ranking_array.add(single_league_ranking);
        }

        last_update = "Ultimo aggiornamento " +write_day +"/" +write_month +"/" +write_year +" alle ore " +write_hour +":" +write_minute;
    }

    void get_teams_array () {
        for(int i=0; i<full_schedule_array.size(); i++) {
            boolean team_found;
            ArrayList<String> single_league_teams = new ArrayList<>();

            for(int j=0;j<full_schedule_array.get(i).size();j++)
            {
                if (single_league_teams.size() == 0) {
                    single_league_teams.add(full_schedule_array.get(i).get(j).get_home());
                } else {
                    // Searching for the current team inside teams array
                    team_found = false;
                    for (int k=0; k<single_league_teams.size(); k++) {
                        if (full_schedule_array.get(i).get(j).get_home().equals(single_league_teams.get(k))) {
                            team_found = true;
                        }
                    }
                    // If team has not been found, then add it to the array
                    if (!team_found) {
                        single_league_teams.add(full_schedule_array.get(i).get(j).get_home());
                    }
                }
            }
            full_teams_array.add(single_league_teams);
        }
    }

    void read_favorites () throws IOException {
        Log.d("SplashScreen", "starting reading favorites");
        File cDir = getBaseContext().getCacheDir();
        File tempFile = new File(cDir.getPath() + "/" + "favorite.txt");
        Log.d("SplashScreen", "path is  " +cDir.getPath());
        FileReader fReader = new FileReader(tempFile);
        BufferedReader bReader = new BufferedReader(fReader);

        String strLine;
        if ((strLine = bReader.readLine()) != null) MainActivity.favorite_league = strLine;
        Log.d("SplashScreen", "read league is " +strLine);
        if ((strLine = bReader.readLine()) != null) MainActivity.favorite_team = strLine;
        Log.d("SplashScreen", "read team is  " +strLine);
        Log.d("SplashScreen", "finished reading favorites");

        tempFile = new File(cDir.getPath() + "/" + "favorite_schedule_array.txt");
        fReader = new FileReader(tempFile);
        bReader = new BufferedReader(fReader);

        int rows_count = 0;
        if ((strLine = bReader.readLine()) != null) rows_count = Integer.parseInt(strLine);

        for (int j = 0; j < rows_count; j++) {
            RowSchedule schedule_row = new RowSchedule();
            if ((strLine = bReader.readLine()) != null) schedule_row.set_home(strLine);
            if ((strLine = bReader.readLine()) != null) schedule_row.set_away(strLine);
            if ((strLine = bReader.readLine()) != null) schedule_row.set_date(strLine);
            if ((strLine = bReader.readLine()) != null) schedule_row.set_hour(strLine);
            if ((strLine = bReader.readLine()) != null) schedule_row.set_place(strLine);
            if ((strLine = bReader.readLine()) != null) schedule_row.set_address(strLine);
            if ((strLine = bReader.readLine()) != null) schedule_row.set_home_score(strLine);
            if ((strLine = bReader.readLine()) != null) schedule_row.set_away_score(strLine);

            favorite_schedule_array.add(schedule_row);
        }

        tempFile = new File(cDir.getPath() + "/" + "favorite_ranking_array.txt");
        fReader = new FileReader(tempFile);
        bReader = new BufferedReader(fReader);

        if ((strLine = bReader.readLine()) != null) rows_count = Integer.parseInt(strLine);

        for (int j = 0; j < rows_count; j++) {
            RowRanking ranking_row = new RowRanking();
            if ((strLine = bReader.readLine()) != null) ranking_row.set_team(strLine);
            if ((strLine = bReader.readLine()) != null) ranking_row.set_points(strLine);
            if ((strLine = bReader.readLine()) != null) ranking_row.set_played(strLine);
            if ((strLine = bReader.readLine()) != null) ranking_row.set_wons(strLine);
            if ((strLine = bReader.readLine()) != null) ranking_row.set_set_won(strLine);
            if ((strLine = bReader.readLine()) != null) ranking_row.set_set_lost(strLine);
            if ((strLine = bReader.readLine()) != null) ranking_row.set_ratio(strLine);

            favorite_ranking_array.add(ranking_row);
        }

        // Substitute favorite schedule and rankings inside full ones
        Log.d("SplashScreen", "start substituting schedule of league " +MainActivity.favorite_league +" and team " +MainActivity.favorite_team);
        for (int i = 0; i < leagues_array.size(); i++) {
            if (leagues_array.get(i).equals(MainActivity.favorite_league))
                favorite_index = i;
        }

        Log.d("SplashScreen", "size of full_schedule_array is " +full_schedule_array.get(favorite_index).size());
        Log.d("SplashScreen", "size of favorite_schedule_array is " +favorite_schedule_array.size());
        for (int i=0; i<full_schedule_array.get(favorite_index).size(); i++) {
            full_schedule_array.get(favorite_index).set(i, favorite_schedule_array.get(i));
        }

        Log.d("SplashScreen", "size of full_ranking_array is " +full_ranking_array.get(favorite_index).size());
        Log.d("SplashScreen", "size of favorite_ranking_array is " +favorite_ranking_array.size());
        for (int i=0; i<full_ranking_array.get(favorite_index).size(); i++) {
            full_ranking_array.get(favorite_index).set(i, favorite_ranking_array.get(i));
        }
        Log.d("SplashScreen", "favorites schedule and ranking loaded successfully");
    }

    void read_favorites_new () throws IOException {
        Log.d("SplashScreen", "starting reading NEW favorites");
        File inputFile = new File(getBaseContext().getCacheDir().getPath() + "/" + "favorite_new.txt");
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));

        String currentLine;
        Integer i = 0;
        new_favorites_array.clear();
        while ((currentLine = reader.readLine()) != null) {
            new_favorites_array.add(currentLine.split(",,,"));
            Log.d("SplashScreen", "Added team "+new_favorites_array.get(i)[1]+" of league "+new_favorites_array.get(i)[0]);
            i++;
        }

        Log.d("SplashScreen", "favorites schedule and ranking loaded successfully");
    }
}