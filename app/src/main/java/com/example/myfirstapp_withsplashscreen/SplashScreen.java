package com.example.myfirstapp_withsplashscreen;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class SplashScreen extends Activity {

    Element schedule;
    ArrayList<String> leagues_links_array = new ArrayList<String>();

    static ArrayList<String> leagues_array = new ArrayList<String>();
    static ArrayList<ArrayList<RowSchedule>> full_schedule_array = new ArrayList<ArrayList<RowSchedule>>();
    static ArrayList<ArrayList<RowRanking>> full_ranking_array = new ArrayList<ArrayList<RowRanking>>();
    static ArrayList<ArrayList> full_teams_array = new ArrayList<ArrayList>();

    ArrayList<RowSchedule> favorite_schedule_array = new ArrayList<RowSchedule>();
    ArrayList<RowRanking> favorite_ranking_array = new ArrayList<RowRanking>();
    int favorite_index = 0;

    public static String last_update = "";

    public static int numero_di_prova = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Showing splashscreen while making network calls to download necessary
        // data before launching the app Will use AsyncTask to make http call

        new PrefetchData().execute();
    }

    // Async Task to make http call

    private class PrefetchData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // before making http calls

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            // Reading contents of the temporary file, if already exists
            File cDir = getBaseContext().getCacheDir();
            // Getting a reference to temporary file, if created earlier
            File tempFile;

            String strLine;
            int leagues_count = 0;
            int rows_count = 0;
            try {

                tempFile = new File(cDir.getPath() + "/" + "leagues_array.txt");
                FileReader fReader = new FileReader(tempFile);
                BufferedReader bReader = new BufferedReader(fReader);

                if( (strLine=bReader.readLine()) != null  ) leagues_count = Integer.parseInt(strLine);
                for(int i=0; i<leagues_count; i++) {
                    if( (strLine=bReader.readLine()) != null  ) leagues_array.add(strLine);
                }

                tempFile = new File(cDir.getPath() + "/" + "full_schedule_array.txt");
                fReader = new FileReader(tempFile);
                bReader = new BufferedReader(fReader);

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

                tempFile = new File(cDir.getPath() + "/" + "full_ranking_array.txt");
                fReader = new FileReader(tempFile);
                bReader = new BufferedReader(fReader);

                int write_day = 0;
                int write_month = 0;
                int write_year = 0;
                int write_hour = 0;
                int write_minute = 0;

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

                // Try to load this files only if a favorite league and team have been set
                //if( !(MainActivity.favorite_league.equals("")) && !(MainActivity.favorite_team.equals(""))) {
                try {

                    Log.d("SplashScreen", "starting reading favorites");
                    tempFile = new File(cDir.getPath() + "/" + "favorite.txt");
                    Log.d("SplashScreen", "path is  " +cDir.getPath());
                    fReader = new FileReader(tempFile);
                    bReader = new BufferedReader(fReader);

                    if ((strLine = bReader.readLine()) != null) MainActivity.favorite_league = strLine;
                    Log.d("SplashScreen", "read league is " +strLine);
                    if ((strLine = bReader.readLine()) != null) MainActivity.favorite_team = strLine;
                    Log.d("SplashScreen", "read team is  " +strLine);
                    Log.d("SplashScreen", "finished reading favorites");

                    tempFile = new File(cDir.getPath() + "/" + "favorite_schedule_array.txt");
                    fReader = new FileReader(tempFile);
                    bReader = new BufferedReader(fReader);

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
                } catch (IOException e) {
                    e.printStackTrace();
                    // Do not reload all the data if favorite schedule/ranking are not present
                    // Reset favorite league/team instead
                    MainActivity.favorite_league = "";
                    MainActivity.favorite_team = "";
                    Log.d("SplashScreen", "favorites schedule and ranking not loaded!");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                load_data();
            } catch (IOException e) {
                e.printStackTrace();
                load_data();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // After completing http call
            // will close this activity and lauch main activity
            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            intent.putStringArrayListExtra("leagues", leagues_array);
            intent.putExtra("last_update", last_update);
            startActivity(intent);

            // close this activity
            finish();
        }

        void load_data () {

            // Reading contents of the temporary file, if already exists
            File cDir = getBaseContext().getCacheDir();
            // Getting a reference to temporary file, if created earlier
            File tempFile;

            downloading: try {

                Log.d("myTag", "starting downloading teams");
                Document generic_doc = Jsoup.connect("http://www.fipav.pavia.it/calendari.asp").get();
                schedule = generic_doc.getElementsByClass("tabella").get(0); // la prima tabella contiene i campionati
                String absHref_mod;
                Elements rows = schedule.select("tr");
                int table_size = rows.size() - 1;
                Log.d("myTag", "starting parsing the leagues table with " +table_size +" rows");
                if(rows.size()>0) {
                    for(int i=0;i<rows.size();i++)
                    {
                        Element row = rows.get(i);
                        Elements cols = row.select("td");
                        for(int j=0; j<cols.size(); j++) {
                            Element temp = cols.get(j);
                            String temp_text = temp.text().replace("\u00a0", "");
                            if(!(temp_text.equals(""))) {
                                leagues_array.add(temp_text);
                                Element link = cols.select("a").get(j);
                                String absHref = link.attr("abs:href");
                                absHref_mod = absHref.replace(" ", "%20");
                                leagues_links_array.add(absHref_mod);
                            }
                        }
                    }
                }

                FileWriter writer;
                tempFile = new File(cDir.getPath() + "/" + "leagues_array.txt");
                try {
                    writer = new FileWriter(tempFile);

                    // Saving the contents to the file
                    for (int i=0; i<leagues_array.size(); i++) {
                        if(i==0) writer.write(leagues_array.size() +"\n");
                        writer.write(leagues_array.get(i) +"\n");
                    }
                    // Closing the writer object
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    last_update = "Aggiornamento automatico fallito, premere il tasto AGGIORNA";
                    break downloading;
                }

                for (int i = 0; i < leagues_array.size(); i++) {
                    Log.d("myTag", "starting download at: " + leagues_links_array.get(i));
                    Document schedule_doc = Jsoup.connect(leagues_links_array.get(i)).get();
                    schedule = schedule_doc.getElementsByClass("tabella").get(1); // la prima tabella contiene i campionati e possiamo scartarla
                    ArrayList<RowSchedule> single_league_schedule = new ArrayList<>();
                    rows = schedule.select("tr");
                    table_size = rows.size() - 1;
                    Log.d("myTag", "starting parsing the schedule table with " + table_size + " rows");
                    for (int j = 1; j < rows.size(); j++) // la prima riga contiene le intestazioni della tabella e possiamo scartarla
                    {
                        RowSchedule schedule_row = new RowSchedule();
                        Element row = rows.get(j);
                        Elements cols = row.select("td");
                        schedule_row.set_home(cols.get(1).text());
                        schedule_row.set_away(cols.get(2).text());
                        schedule_row.set_date(cols.get(3).text());
                        schedule_row.set_hour(cols.get(4).text());
                        schedule_row.set_place(cols.get(5).text());
                        schedule_row.set_address(cols.get(6).text());
                        schedule_row.set_home_score(cols.get(7).text());
                        schedule_row.set_away_score(cols.get(9).text());
                        single_league_schedule.add(schedule_row);
                    }
                    full_schedule_array.add(single_league_schedule);
                }

                tempFile = new File(cDir.getPath() + "/" + "full_schedule_array.txt");
                try {
                    writer = new FileWriter(tempFile);

                    // Saving the contents to the file
                    for (int i=0; i<full_schedule_array.size(); i++) {
                        if(i==0) writer.write(full_schedule_array.size() +"\n");
                        if(full_schedule_array.get(i).size()==0) writer.write(full_schedule_array.get(i).size() +"\n");
                        for (int j=0; j<full_schedule_array.get(i).size(); j++) {
                            if(j==0) writer.write(full_schedule_array.get(i).size() +"\n");
                            writer.write(full_schedule_array.get(i).get(j).get_home() +"\n");
                            writer.write(full_schedule_array.get(i).get(j).get_away() +"\n");
                            writer.write(full_schedule_array.get(i).get(j).get_date() +"\n");
                            writer.write(full_schedule_array.get(i).get(j).get_hour() +"\n");
                            writer.write(full_schedule_array.get(i).get(j).get_place() +"\n");
                            writer.write(full_schedule_array.get(i).get(j).get_address() +"\n");
                            writer.write(full_schedule_array.get(i).get(j).get_home_score() +"\n");
                            writer.write(full_schedule_array.get(i).get(j).get_away_score() +"\n");
                        }
                    }
                    // Closing the writer object
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    last_update = "Aggiornamento automatico fallito, premere il tasto AGGIORNA";
                    break downloading;
                }

                for (int i = 0; i < leagues_array.size(); i++) {
                    Log.d("myTag", "starting download at: " + leagues_links_array.get(i).replace("calendari", "classifica"));
                    Document schedule_doc = Jsoup.connect(leagues_links_array.get(i).replace("calendari", "classifica")).get();
                    schedule = schedule_doc.getElementsByClass("tabella").get(1); // la prima tabella contiene i campionati e possiamo scartarla
                    ArrayList<RowRanking> single_league_schedule = new ArrayList<>();
                    rows = schedule.select("tr");
                    table_size = rows.size() - 1;
                    Log.d("myTag", "starting parsing the ranking table with " + table_size + " rows");
                    for (int j = 1; j < rows.size(); j++) // la prima riga contiene le intestazioni della tabella e possiamo scartarla
                    {
                        RowRanking ranking_row = new RowRanking();
                        Element row = rows.get(j);
                        Elements cols = row.select("td");
                        ranking_row.set_team(cols.get(0).text());
                        ranking_row.set_points(cols.get(1).text());
                        ranking_row.set_played(cols.get(2).text());
                        ranking_row.set_wons(cols.get(3).text());
                        ranking_row.set_set_won(cols.get(4).text());
                        ranking_row.set_set_lost(cols.get(5).text());
                        ranking_row.set_ratio(cols.get(6).text());
                        single_league_schedule.add(ranking_row);
                    }
                    full_ranking_array.add(single_league_schedule);
                }

                tempFile = new File(cDir.getPath() + "/" + "full_ranking_array.txt");
                try {
                    writer = new FileWriter(tempFile);

                    Calendar c = Calendar.getInstance();
                    int actual_day = c.get(Calendar.DAY_OF_MONTH);
                    int actual_month = c.get(Calendar.MONTH) + 1;
                    int actual_year = c.get(Calendar.YEAR) - 2000;
                    int actual_hour = c.get(Calendar.HOUR_OF_DAY);
                    int actual_minute = c.get(Calendar.MINUTE);
                    writer.write(actual_day +"\n");
                    writer.write(actual_month +"\n");
                    writer.write(actual_year +"\n");
                    writer.write(actual_hour +"\n");
                    writer.write(actual_minute +"\n");

                    // Saving the contents to the file
                    for (int i=0; i<full_ranking_array.size(); i++) {
                        if(i==0) writer.write(full_ranking_array.size() +"\n");
                        if(full_ranking_array.get(i).size()==0) writer.write(full_ranking_array.get(i).size() +"\n");
                        for (int j=0; j<full_ranking_array.get(i).size(); j++) {
                            if(j==0) writer.write(full_ranking_array.get(i).size() +"\n");
                            writer.write(full_ranking_array.get(i).get(j).get_team() +"\n");
                            writer.write(full_ranking_array.get(i).get(j).get_points() +"\n");
                            writer.write(full_ranking_array.get(i).get(j).get_played() +"\n");
                            writer.write(full_ranking_array.get(i).get(j).get_wons() +"\n");
                            writer.write(full_ranking_array.get(i).get(j).get_set_won() +"\n");
                            writer.write(full_ranking_array.get(i).get(j).get_set_lost() +"\n");
                            writer.write(full_ranking_array.get(i).get(j).get_ratio() +"\n");
                        }
                    }
                    // Closing the writer object
                    writer.close();

                    last_update = "Ultimo aggiornamento " +actual_day +"/" +actual_month +"/" +actual_year +" alle ore " +actual_hour +":" +actual_minute;
                } catch (IOException e) {
                    e.printStackTrace();
                    last_update = "Aggiornamento automatico fallito, premere il tasto AGGIORNA";
                    break downloading;
                }

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
            } catch (IOException e) {
                e.printStackTrace();
                last_update = "Aggiornamento automatico fallito, premere il tasto AGGIORNA";
            }

        }

    }

}