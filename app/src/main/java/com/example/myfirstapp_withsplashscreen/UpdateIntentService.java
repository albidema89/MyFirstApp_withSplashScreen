package com.example.myfirstapp_withsplashscreen;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class UpdateIntentService extends IntentService {
    public UpdateIntentService() {
        super("UpdateIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        Log.v("UpdateIntentService", "UpdateIntentService -- Starting with action " +action);

        Intent resultIntent = new Intent(this, SplashScreen.class);

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logo_fipav1)
                        .setContentTitle("Aggiornamento riuscito")
                        .setContentText("Calendari e classifiche aggiornati! " +SplashScreen.numero_di_prova);
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);

        Element schedule;
        ArrayList<String> leagues_links_array = new ArrayList<String>();
        ArrayList<String> leagues_array = new ArrayList<String>();
        ArrayList<ArrayList<RowSchedule>> full_schedule_array = new ArrayList<ArrayList<RowSchedule>>();
        ArrayList<ArrayList<RowRanking>> full_ranking_array = new ArrayList<ArrayList<RowRanking>>();

        // Reading contents of the temporary file, if already exists
        File cDir = getBaseContext().getCacheDir();
        // Getting a reference to temporary file, if created earlier
        File tempFile;

        try {

            //Log.d("myTag", "starting downloading teams");
            Document generic_doc = Jsoup.connect("http://www.fipav.pavia.it/calendari.asp").get();
            schedule = generic_doc.getElementsByClass("tabella").get(0); // la prima tabella contiene i campionati

            String absHref_mod = "";

            Elements rows = schedule.select("tr");
            int table_size = rows.size() - 1;
            //Log.d("myTag", "starting parsing the leagues table with " +table_size +" rows");
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

            FileWriter writer=null;
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

                mBuilder.setContentTitle("Aggiornamento fallito");
                mBuilder.setContentText("Errore durante la scrittura delle squadre!");
                mBuilder.setContentIntent(resultPendingIntent);
                mBuilder.setAutoCancel(true);
            }

            for (int i = 0; i < leagues_array.size(); i++) {
                //Log.d("myTag", "starting download at: " + leagues_links_array.get(i));
                Document schedule_doc = Jsoup.connect(leagues_links_array.get(i)).get();
                schedule = schedule_doc.getElementsByClass("tabella").get(1); // la prima tabella contiene i campionati e possiamo scartarla

                ArrayList<RowSchedule> single_league_schedule = new ArrayList<>();

                rows = schedule.select("tr");
                table_size = rows.size() - 1;
                //Log.d("myTag", "starting parsing the schedule table with " + table_size + " rows");
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

            writer=null;
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

                //last_update = "Ultimo aggiornamento " +actual_day +"/" +actual_month +"/" +actual_year +" alle ore " +actual_hour +":" +actual_minute;
            } catch (IOException e) {
                e.printStackTrace();

                mBuilder.setContentTitle("Aggiornamento fallito");
                mBuilder.setContentText("Errore durante la scrittura dei calendari!");
                mBuilder.setContentIntent(resultPendingIntent);
                mBuilder.setAutoCancel(true);
            }

            for (int i = 0; i < leagues_array.size(); i++) {
                //Log.d("myTag", "starting download at: " + leagues_links_array.get(i).replace("calendari", "classifica"));
                Document schedule_doc = Jsoup.connect(leagues_links_array.get(i).replace("calendari", "classifica")).get();
                schedule = schedule_doc.getElementsByClass("tabella").get(1); // la prima tabella contiene i campionati e possiamo scartarla

                ArrayList<RowRanking> single_league_schedule = new ArrayList<>();

                rows = schedule.select("tr");
                table_size = rows.size() - 1;
                //Log.d("myTag", "starting parsing the ranking table with " + table_size + " rows");
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
                Log.d("myTag", actual_day +" " +actual_month +" " +actual_year);
                Log.d("myTag", "hour" +actual_hour +" minutes " +actual_minute);
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
            } catch (IOException e) {
                e.printStackTrace();

                mBuilder.setContentTitle("Aggiornamento fallito");
                mBuilder.setContentText("Errore durante la scrittura delle classifiche!");
                mBuilder.setContentIntent(resultPendingIntent);
                mBuilder.setAutoCancel(true);
            }
        } catch (IOException e) {
            e.printStackTrace();

            mBuilder.setContentTitle("Aggiornamento fallito");
            mBuilder.setContentText("Errore durante il download!");
            mBuilder.setContentIntent(resultPendingIntent);
            mBuilder.setAutoCancel(true);
        }

        if(action.equals("UpdateIntent.SINGLE_UPDATE")) {
            Intent send_intent = new Intent(this, SplashScreen.class);
            send_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(send_intent);
        } else {
            // Sets an ID for the notification
            int mNotificationId = 001;
            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // Builds the notification and issues it.
            mNotifyMgr.notify(mNotificationId, mBuilder.build());
        }
    }
}
