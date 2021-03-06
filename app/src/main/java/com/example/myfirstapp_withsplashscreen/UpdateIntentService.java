package com.example.myfirstapp_withsplashscreen;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class UpdateIntentService extends IntentService {
    public UpdateIntentService() {
        super("UpdateIntentService");
    }

    Element schedule;
    Elements rows;
    ArrayList<String> leagues_links_array = new ArrayList<String>();
    ArrayList<String> leagues_array = new ArrayList<String>();
    ArrayList<ArrayList<RowSchedule>> full_schedule_array = new ArrayList<ArrayList<RowSchedule>>();
    ArrayList<ArrayList<RowRanking>> full_ranking_array = new ArrayList<ArrayList<RowRanking>>();

    String favorite_league_link;
    String favorite_league = "";
    String favorite_team = "";
    ArrayList<RowSchedule> favorite_schedule_array = new ArrayList<RowSchedule>();
    ArrayList<RowRanking> favorite_ranking_array = new ArrayList<RowRanking>();

    ArrayList<RowSchedule> old_favorite_schedule_array = new ArrayList<RowSchedule>();
    ArrayList<String> score_updates = new ArrayList<>();
    ArrayList<String> date_updates = new ArrayList<>();

    FileWriter writer;
    // Reading contents of the temporary file, if already exists
    File cDir;
    // Getting a reference to temporary file, if created earlier
    File tempFile;
    String strLine;

    boolean load_all = false;
    boolean update_favorite = false;
    boolean should_break = false;

    int last_update_year;
    int last_update_month;
    int last_update_day;

    Intent resultIntent;

    // Because clicking the notification opens a new ("special") activity, there's
    // no need to create an artificial back stack.
    PendingIntent resultPendingIntent;
    NotificationCompat.Builder mBuilder;

    // Sets an ID for the notification
    int mNotificationId;
    NotificationManager mNotifyMgr;

    // Variables for calendar update
    String account;

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        Log.d("UpdateIntentService", "UpdateIntentService -- Starting with action " +action);

        mNotificationId = 1;

        resultIntent = new Intent(this, SplashScreen.class);
        resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder = new NotificationCompat.Builder(this);
        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        load_all = false; // by default, don't load all the schedules and rankings every time
        should_break = false; // by default, never break from IF branches

        mBuilder.setSmallIcon(R.drawable.ic_launcher_fipav);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_fipav));
        mBuilder.setContentTitle("Aggiornamento riuscito");
        mBuilder.setContentText("Calendari e classifiche aggiornati! " +SplashScreen.numero_di_prova);

        if(action.equals("UpdateIntent.SINGLE_UPDATE")) {
            mBuilder.setContentTitle("Aggiornamento calendari in corso");
            mBuilder.setContentText("E' in corso l'aggiornamento dei calendari: non riavviare l'applicazione");
            mNotifyMgr.notify(mNotificationId, mBuilder.build());

            load_all = true; // set flag in order to load all schedules and rankings, no matter last update time
        } else if(action.equals("UpdateIntent.FAVORITE_UPDATE")) {
            update_favorite = true;
        } else if(action.equals("UpdateIntent.FIRST_UPDATE")) {
            load_all = true;
        }

        if(!update_favorite) {
            try {
                read_last_update();
                if (last_update_is_old()) { load_all = true; Log.d("UpdateIntentService", "last update is old"); }
                else Log.d("UpdateIntentService", "last update is NOT old");
            } catch (IOException e) {
                load_all = true; // set flag to load all schedules and rankings, because the last file has not been found
            }
        }

        Log.d("UpdateIntentService", "update_favorite is " +update_favorite);
        Log.d("UpdateIntentService", "load_all is " +load_all);

        if(load_all) {
            read_old_schedule();

            // Download and write all schedules and rankings
            load_all:
            try {
                load_leagues();
                write_leagues();

                load_all_schedules();
                write_all_schedules();

                load_all_rankings();
                write_all_rankings();
            } catch (Exception e) {
                e.printStackTrace();

                mBuilder.setContentTitle("Aggiornamento fallito");
                mBuilder.setContentText("load_all: error during load_all!");
                mBuilder.setContentIntent(resultPendingIntent);
                mBuilder.setAutoCancel(true);
                should_break = true;
            }

            download_favorite: try {
                load_leagues();
                read_favorite();
                if( !(favorite_league.equals("")) && !(favorite_team.equals(""))) {
                    download_favorite_schedule();
                    write_favorite_schedule();
                    download_favorite_ranking();
                    write_favorite_ranking();
                }

                account = getUserAccount();

                Log.d("UpdateIntentService", "sending broadcast TRUE");
                sendBroadcast(true);
            } catch (Exception e) {
                e.printStackTrace();

                mBuilder.setContentTitle("Aggiornamento incompleto");
                if(should_break) mBuilder.setContentText("load_all: error during download_favorite!");
                else mBuilder.setContentText("load_all: error on load_all AND download_favorite!");
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("load_all: error on load_all AND download_favorite!"));
                mBuilder.setContentIntent(resultPendingIntent);
                mBuilder.setAutoCancel(true);

                Log.d("UpdateIntentService", "sending broadcast FALSE");
                sendBroadcast(false);

                should_break = true;
            }
        } else if(update_favorite) {
            Log.v("UpdateIntentService", "Entered in update_favorite branch");
            read_favorite();
            Log.v("UpdateIntentService", "Favorite written");

            try {
                load_leagues();
                Log.v("UpdateIntentService", "Links loaded");
                // 2017/03/02: consider to add check on league being present inside leagues array ...
                if( !(favorite_league.equals("")) && !(favorite_team.equals(""))) {
                    download_favorite_schedule();
                    write_favorite_schedule();
                    Log.v("UpdateIntentService", "Updated schedule");

                    download_favorite_ranking();
                    write_favorite_ranking();
                    Log.v("UpdateIntentService", "Updated ranking");

                    Log.d("UpdateIntentService", "sending broadcast TRUE");
                    sendBroadcast(true);
                }
            } catch (Exception e) {
                e.printStackTrace();

                mBuilder.setContentTitle("Aggiornamento fallito");
                mBuilder.setContentText("Errore durante l'aggiornamento dei preferiti!");
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("Errore durante l'aggiornamento dei preferiti!"));
                mBuilder.setContentIntent(resultPendingIntent);
                mBuilder.setAutoCancel(true);

                Log.d("UpdateIntentService", "sending broadcast FALSE");
                sendBroadcast(false);

                should_break = true;
            }
        } else {
            read_old_schedule();

            load_favorite:
            try {
                load_leagues();
                read_favorite();

                if( !(favorite_league.equals("")) && !(favorite_team.equals(""))) {
                    download_favorite_schedule();
                    write_favorite_schedule();
                    download_favorite_ranking();
                    write_favorite_ranking();
                }

                Log.d("UpdateIntentService", "sending broadcast TRUE");
                sendBroadcast(true);
            } catch (IOException e) {
                e.printStackTrace();

                mBuilder.setContentTitle("Aggiornamento fallito");
                mBuilder.setContentText("Errore durante il download dei preferiti!");
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("Errore durante il download dei preferiti!"));
                mBuilder.setContentIntent(resultPendingIntent);
                mBuilder.setAutoCancel(true);

                Log.d("UpdateIntentService", "sending broadcast FALSE");
                sendBroadcast(false);

                should_break = true;
            } catch (Exception e) {
                e.printStackTrace();

                mBuilder.setContentTitle("Aggiornamento fallito (non IO)");
                mBuilder.setContentText("Errore durante il download dei preferiti!");
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("Errore durante il download dei preferiti!"));
                mBuilder.setContentIntent(resultPendingIntent);
                mBuilder.setAutoCancel(true);

                Log.d("UpdateIntentService", "sending broadcast FALSE");
                sendBroadcast(false);

                should_break = true;
            }
        }

        if(action.equals("UpdateIntent.SINGLE_UPDATE")) {
            Intent send_intent = new Intent(this, SplashScreen.class);
            send_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(send_intent);
            mNotifyMgr.cancel(mNotificationId);
        } else {
            if(!should_break & update_favorite) {
                mBuilder.setContentText("Preferiti scritti correttamente!");
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("Preferiti scritti correttamente!"));
                append_log("Preferiti scritti correttamente!");
            }
            else {
                if(!should_break & load_all) {
                    mBuilder.setContentText("Tutti i calendari sono stati aggiornati!");
                    mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("Tutti i calendari sono stati aggiornati!"));
                    append_log("Tutti i calendari sono stati aggiornati!");
                }
                else if(!should_break) {
                    mBuilder.setContentText("Preferiti aggiornati correttamente!");
                    mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("Preferiti aggiornati correttamente!"));
                    append_log("Preferiti aggiornati correttamente!");
                }
                mBuilder.setContentIntent(resultPendingIntent);
            }
            mBuilder.setAutoCancel(true);
            // Builds the notification and issues it.
            //mNotifyMgr.notify(mNotificationId, mBuilder.build());
        }

        // TODO: better control of notifications ID
        // for example: always use 1 for the previous, 2 for skip check and 3+ for updates notifcations,
        // but you have to be sure to not re-use one of the 3+ if that notification has not been cancelled
        // by user, otherwise you would override the update!
        // 2017/03/07 update: now that you have big text inside notifications, consider to add all the updates
        // in one single notification and, if it has not been removed, append the new ones to the existing ones!

        //StatusBarNotification sbn[] = mNotifyMgr.getActiveNotifications();
        //sbn[0].getId()

        // Checking if something has changed since last update
        if(!update_favorite & !should_break) {
            if(favorite_schedule_array.size() != old_favorite_schedule_array.size()) {
                mBuilder.setContentTitle("Skipping changes check");
                mBuilder.setContentText("Size of two arrays is different" +favorite_schedule_array.size() +" " +old_favorite_schedule_array.size());
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("Size of two arrays is different" +favorite_schedule_array.size() +" " +old_favorite_schedule_array.size()));
                mBuilder.setContentIntent(resultPendingIntent);
                mBuilder.setAutoCancel(true);
                //mNotifyMgr.notify(++mNotificationId, mBuilder.build());
                append_log("Size of two arrays is different" +favorite_schedule_array.size() +" " +old_favorite_schedule_array.size());
            } else {
                check_score_updates();

                if(score_updates.size() == 0) {
                    mBuilder.setContentTitle(favorite_league +"(ID: " +(mNotificationId+1) +")");
                    mBuilder.setContentText("Nessun nuovo risultato");
                    mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("Nessun nuovo risultato"));
                    mBuilder.setContentIntent(resultPendingIntent);
                    mBuilder.setAutoCancel(true);
                    //mNotifyMgr.notify(++mNotificationId, mBuilder.build());
                    append_log("Nessun nuovo risultato");
                } else {
                    for (int i = 0; i < score_updates.size(); i++) {
                        mNotificationId = mNotifyMgr.getActiveNotifications().length;

                        mBuilder.setContentTitle(favorite_league +" (ID: " +(mNotificationId+1) +")");
                        mBuilder.setContentText(score_updates.get(i));
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(score_updates.get(i)));
                        mBuilder.setContentIntent(resultPendingIntent);
                        mBuilder.setAutoCancel(true);
                        mNotifyMgr.notify(++mNotificationId, mBuilder.build());
                        append_log(score_updates.get(i));
                    }
                }

                check_date_updates();

                if(date_updates.size() == 0) {
                    mBuilder.setContentTitle(favorite_league +"(ID: " +(mNotificationId+1) +")");
                    mBuilder.setContentText("Nessun cambio di data");
                    mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("Nessun cambio di data"));
                    mBuilder.setContentIntent(resultPendingIntent);
                    mBuilder.setAutoCancel(true);
                    //mNotifyMgr.notify(++mNotificationId, mBuilder.build());
                    append_log("Nessun cambio di data");
                } else {
                    for (int i = 0; i < date_updates.size(); i++) {
                        mNotificationId = mNotifyMgr.getActiveNotifications().length;

                        mBuilder.setContentTitle(favorite_league +" (ID: " +(mNotificationId+1) +")");
                        mBuilder.setContentText(date_updates.get(i));
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(date_updates.get(i)));
                        mBuilder.setContentIntent(resultPendingIntent);
                        mBuilder.setAutoCancel(true);
                        mNotifyMgr.notify(++mNotificationId, mBuilder.build());
                        append_log(date_updates.get(i));
                    }
                }
            }
        }
    }

    private void sendBroadcast (boolean success){
        Intent intent = new Intent ("UpdateIntentService.UPDATE_DONE"); //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra("success", success);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    void load_leagues () throws IOException {
        Document generic_doc = Jsoup.connect("http://www.fipav.pavia.it/calendari.asp").get();
        schedule = generic_doc.getElementsByClass("tabella").get(0); // la prima tabella contiene i campionati

        String absHref_mod;

        Elements rows = schedule.select("tr");

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
    }

    void write_leagues () {
        cDir = getBaseContext().getCacheDir();
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
    }

    void load_all_schedules () throws IOException {
        for (int i = 0; i < leagues_array.size(); i++) {
            Document schedule_doc = Jsoup.connect(leagues_links_array.get(i)).get();
            schedule = schedule_doc.getElementsByClass("tabella").get(1); // la prima tabella contiene i campionati e possiamo scartarla

            ArrayList<RowSchedule> single_league_schedule = new ArrayList<>();

            rows = schedule.select("tr");
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
    }

    void write_all_schedules () {
        cDir = getBaseContext().getCacheDir();
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

            mBuilder.setContentTitle("Aggiornamento fallito");
            mBuilder.setContentText("Errore durante la scrittura dei calendari!");
            mBuilder.setContentIntent(resultPendingIntent);
            mBuilder.setAutoCancel(true);
        }
    }

    void load_all_rankings () throws IOException {
        for (int i = 0; i < leagues_array.size(); i++) {
            Document schedule_doc = Jsoup.connect(leagues_links_array.get(i).replace("calendari", "classifica")).get();
            schedule = schedule_doc.getElementsByClass("tabella").get(1); // la prima tabella contiene i campionati e possiamo scartarla

            ArrayList<RowRanking> single_league_schedule = new ArrayList<>();

            rows = schedule.select("tr");
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
    }

    void write_all_rankings () {
        cDir = getBaseContext().getCacheDir();
        tempFile = new File(cDir.getPath() + "/" + "full_ranking_array.txt");
        try {
            writer = new FileWriter(tempFile);

            Calendar c = Calendar.getInstance();
            int actual_day = c.get(Calendar.DAY_OF_MONTH);
            int actual_month = c.get(Calendar.MONTH) + 1;
            int actual_year = c.get(Calendar.YEAR) - 2000;
            int actual_hour = c.get(Calendar.HOUR_OF_DAY);
            int actual_minute = c.get(Calendar.MINUTE);
            Log.d("UpdateIntentService", actual_day +" " +actual_month +" " +actual_year);
            Log.d("UpdateIntentService", "hour" +actual_hour +" minutes " +actual_minute);
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
    }

    void read_last_update () throws IOException {
        cDir = getBaseContext().getCacheDir();
        tempFile = new File(cDir.getPath() + "/" + "full_ranking_array.txt");
        FileReader fReader = new FileReader(tempFile);
        BufferedReader bReader = new BufferedReader(fReader);

        if( (strLine=bReader.readLine()) != null  ) last_update_day = Integer.parseInt(strLine);
        if( (strLine=bReader.readLine()) != null  ) last_update_month = Integer.parseInt(strLine);
        if( (strLine=bReader.readLine()) != null  ) last_update_year = Integer.parseInt(strLine);
    }

    boolean last_update_is_old () {
        Calendar c = Calendar.getInstance();
        int actual_day = c.get(Calendar.DAY_OF_MONTH);
        int actual_month = c.get(Calendar.MONTH) + 1;
        int actual_year = c.get(Calendar.YEAR) - 2000;
        int actual_hour = c.get(Calendar.HOUR_OF_DAY);
        int actual_minute = c.get(Calendar.MINUTE);
        Log.d("UpdateIntentService", actual_day +" " +actual_month +" " +actual_year);
        Log.d("UpdateIntentService", "hour" +actual_hour +" minutes " +actual_minute);

        return( (actual_year >last_update_year) ||
                (actual_year==last_update_year) && (actual_month >last_update_month) ||
                (actual_year==last_update_year) && (actual_month==last_update_month) && (actual_day > last_update_day) );
    }

    void download_favorite_schedule () throws Exception {
        // Obtain http link to favorite league schedule
        for (int i = 0; i < leagues_array.size(); i++) {
            if(leagues_array.get(i).equals(favorite_league)) favorite_league_link = leagues_links_array.get(i);
        }
        Document schedule_doc = Jsoup.connect(favorite_league_link).get();
        schedule = schedule_doc.getElementsByClass("tabella").get(1); // la prima tabella contiene i campionati e possiamo scartarla

        rows = schedule.select("tr");
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
            favorite_schedule_array.add(schedule_row);
        }
    }

    void write_favorite_schedule () {
        cDir = getBaseContext().getCacheDir();
        tempFile = new File(cDir.getPath() + "/" + "favorite_schedule_array.txt");
        try {
            writer = new FileWriter(tempFile);

            // Saving the contents to the file
            if(favorite_schedule_array.size()==0) writer.write(favorite_schedule_array.size() +"\n");
            for (int j=0; j<favorite_schedule_array.size(); j++) {
                if(j==0) writer.write(favorite_schedule_array.size() +"\n");
                writer.write(favorite_schedule_array.get(j).get_home() +"\n");
                writer.write(favorite_schedule_array.get(j).get_away() +"\n");
                writer.write(favorite_schedule_array.get(j).get_date() +"\n");
                writer.write(favorite_schedule_array.get(j).get_hour() +"\n");
                writer.write(favorite_schedule_array.get(j).get_place() +"\n");
                writer.write(favorite_schedule_array.get(j).get_address() +"\n");
                writer.write(favorite_schedule_array.get(j).get_home_score() +"\n");
                writer.write(favorite_schedule_array.get(j).get_away_score() +"\n");
            }
            // Closing the writer object
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();

            mBuilder.setContentTitle("Aggiornamento fallito");
            mBuilder.setContentText("Errore durante la scrittura del calendario!");
            mBuilder.setContentIntent(resultPendingIntent);
            mBuilder.setAutoCancel(true);
        }
    }

    void download_favorite_ranking () throws Exception {
        Document schedule_doc = Jsoup.connect(favorite_league_link.replace("calendari", "classifica")).get();
        schedule = schedule_doc.getElementsByClass("tabella").get(1); // la prima tabella contiene i campionati e possiamo scartarla

        rows = schedule.select("tr");
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

            favorite_ranking_array.add(ranking_row);
        }
    }

    void write_favorite_ranking () {
        cDir = getBaseContext().getCacheDir();
        tempFile = new File(cDir.getPath() + "/" + "favorite_ranking_array.txt");
        try {
            writer = new FileWriter(tempFile);

            // Saving the contents to the file
            if(favorite_ranking_array.size()==0) writer.write(favorite_ranking_array.size() +"\n");
            for (int j=0; j<favorite_ranking_array.size(); j++) {
                if(j==0) writer.write(favorite_ranking_array.size() +"\n");
                writer.write(favorite_ranking_array.get(j).get_team() +"\n");
                writer.write(favorite_ranking_array.get(j).get_points() +"\n");
                writer.write(favorite_ranking_array.get(j).get_played() +"\n");
                writer.write(favorite_ranking_array.get(j).get_wons() +"\n");
                writer.write(favorite_ranking_array.get(j).get_set_won() +"\n");
                writer.write(favorite_ranking_array.get(j).get_set_lost() +"\n");
                writer.write(favorite_ranking_array.get(j).get_ratio() +"\n");
            }
            // Closing the writer object
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();

            mBuilder.setContentTitle("Aggiornamento fallito");
            mBuilder.setContentText("Errore durante la scrittura della classifica!");
            mBuilder.setContentIntent(resultPendingIntent);
            mBuilder.setAutoCancel(true);
        }
    }

    void read_favorite () {
        cDir = getBaseContext().getCacheDir();
        tempFile = new File(cDir.getPath() + "/" + "favorite.txt");
        Log.d("UpdateIntentService", "Opening file: " +tempFile);
        try {
            FileReader fReader = new FileReader(tempFile);
            BufferedReader bReader = new BufferedReader(fReader);

            if( (strLine=bReader.readLine()) != null  ) favorite_league = strLine;
            if( (strLine=bReader.readLine()) != null  ) favorite_team = strLine;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("UpdateIntentService", "Lettura di favorite.txt fallita! Salto il download!");
            favorite_league = "";
            favorite_team = "";
        }
    }

    void read_old_schedule () {
        cDir = getBaseContext().getCacheDir();
        tempFile = new File(cDir.getPath() + "/" + "favorite_schedule_array.txt");

        int rows_count = 0;

        try {
            FileReader fReader = new FileReader(tempFile);
            BufferedReader bReader = new BufferedReader(fReader);

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

                old_favorite_schedule_array.add(schedule_row);
            }
        } catch (IOException e) {
            e.printStackTrace();

            should_break = true;
        }
    }

    void check_score_updates () {
        score_updates.clear();
        for (int i=0; i<old_favorite_schedule_array.size(); i++) {
            String old_home = old_favorite_schedule_array.get(i).get_home();
            String old_away = old_favorite_schedule_array.get(i).get_away();
            String old_home_score = old_favorite_schedule_array.get(i).get_home_score();
            String old_away_score = old_favorite_schedule_array.get(i).get_away_score();

            for (int j=0; j<favorite_schedule_array.size(); j++) {
                String home = favorite_schedule_array.get(j).get_home();
                String away = favorite_schedule_array.get(j).get_away();
                String home_score = favorite_schedule_array.get(j).get_home_score();
                String away_score = favorite_schedule_array.get(j).get_away_score();

                if(old_home.equals(home) & old_away.equals(away) & // is the same match
                        (!(old_home_score.equals(home_score)) | !(old_away_score.equals(away_score))) ) { // at least one of the scores has changed
                    //score_updates.add(home +" - " +away +": " +home_score +" - " +away_score);
                    score_updates.add(home +" - " +away +": " +home_score +" - " +away_score +" (previous was " +old_home_score +" - " +old_away_score +")");
                    Log.d("UpdateIntentService", score_updates.get(score_updates.size()-1));
                }
            }
        }
    }

    void check_date_updates () {
        date_updates.clear();
        for (int i=0; i<old_favorite_schedule_array.size(); i++) {
            String old_home = old_favorite_schedule_array.get(i).get_home();
            String old_away = old_favorite_schedule_array.get(i).get_away();
            String old_date = old_favorite_schedule_array.get(i).get_date();
            String old_hour = old_favorite_schedule_array.get(i).get_hour();

            for (int j=0; j<favorite_schedule_array.size(); j++) {
                String home = favorite_schedule_array.get(j).get_home();
                String away = favorite_schedule_array.get(j).get_away();
                String date = favorite_schedule_array.get(j).get_date();
                String hour = favorite_schedule_array.get(j).get_hour();

                if(old_home.equals(home) & old_away.equals(away) & // is the same match
                        (!(old_date.equals(date)) | !(old_hour.equals(hour))) ) { // at least one of date/hour has changed
                    date_updates.add(home +" - " +away +": " +date +" - " +hour +" (previous was " +old_date +" - " +old_hour +")");
                    Log.d("UpdateIntentService", date_updates.get(date_updates.size()-1));
                }
            }
        }
    }

    void append_log (String string) {
        cDir = getBaseContext().getFilesDir();
        cDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        // Make sure the Pictures directory exists.
        cDir.mkdirs();

        tempFile = new File(cDir.getPath() + "/" + "myfirstapp_log.txt");
        Log.d("UpdateIntentService", "Writing at this path: " +cDir.getPath());
        try {
            if(tempFile.exists()) writer = new FileWriter(tempFile, true); // append mode
            else {
                tempFile.createNewFile();
                writer = new FileWriter(tempFile);
            }
            writer.write(Calendar.getInstance().getTime() +" --- " +string +"\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getUserAccount () {
        AccountManager manager = (AccountManager) this.getSystemService(ACCOUNT_SERVICE);
        Account[] list = manager.getAccounts();
        String gmail = null;

        for (Account account : list) {
            //Log.d("TestCalendar", "Looking into account: " +account);
            if (account.type.equalsIgnoreCase("com.google")) {
                gmail = account.name;
                //Log.d("TestCalendar", "Found account: " +gmail);
                break;
            }
        }
        Log.d("UpdateIntentService", "Returning account: " +gmail);
        return gmail;
    }
}
