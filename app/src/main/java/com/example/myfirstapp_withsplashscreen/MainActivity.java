package com.example.myfirstapp_withsplashscreen;

import android.app.DialogFragment;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    public LinearLayout parent;

    ArrayAdapter<String> leagues_spinnerAdapter;
    Spinner leagues_sp;
    ArrayAdapter<String> teams_spinnerAdapter;
    Spinner teams_sp;
    ArrayAdapter<String> leagues_listviewAdapter;

    ArrayList<String> leagues_array;

    public static int league_selected;
    public static String team_selected;

    static String favorite_league = "";
    static String favorite_team = "";

    static String selected_league = "";
    static String selected_team = "";

    TextView last_update_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("MainActivity", "favorite league is " +favorite_league);
        Log.d("MainActivity", "favorite team is " +favorite_team);

        Intent intent = getIntent();
        leagues_array = intent.getStringArrayListExtra("leagues");

        for(int i=0; i<SplashScreen.full_teams_array.size(); i++){
            Collections.sort(SplashScreen.full_teams_array.get(i), String.CASE_INSENSITIVE_ORDER);
        }

        LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        parent = new LinearLayout(this);
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setLayoutParams(param);

        setContentView(parent);

        final Button set_favorite = new Button(this);

        // preparazione dello Spinner per mostrare l'elenco delle squadre
        teams_spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_row);
        teams_sp = new Spinner(this);
        teams_sp.setLayoutParams(param);
        teams_sp.setAdapter(teams_spinnerAdapter);
        teams_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                Log.d("MainActivity", "qui è scattato onItemSelected di teams_sp");
                TextView txt = (TextView) arg1.findViewById(arg1.getId());
                if(teams_sp.getSelectedItem() != null) selected_team = teams_sp.getSelectedItem().toString();

                if(selected_team.equals(favorite_team) && selected_league.equals(favorite_league)) {
                    set_favorite.setClickable(false);
                } else {
                    set_favorite.setClickable(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            { }
        });

        // preparazione dello Spinner per mostrare l'elenco dei campionati
        leagues_spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_row);
        leagues_sp = new Spinner(this);
        leagues_sp.setLayoutParams(param);
        leagues_sp.setAdapter(leagues_spinnerAdapter);
        for(int i=0; i<leagues_sp.getCount(); i++) {
            Log.d("MainActivity", "step " +i +" di leagues_sp: team is " +teams_sp.getItemAtPosition(i).toString());
            if(leagues_sp.getItemAtPosition(i).toString().equals(favorite_league)) leagues_sp.setSelection(i, true);
        }
        leagues_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                teams_spinnerAdapter.clear();
                for(int i=0; i<SplashScreen.full_teams_array.get(leagues_sp.getSelectedItemPosition()).size(); i++) {
                    teams_spinnerAdapter.add(SplashScreen.full_teams_array.get(leagues_sp.getSelectedItemPosition()).get(i).toString());
                }
                for(int i=0; i<teams_sp.getCount(); i++) {
                    if(teams_sp.getItemAtPosition(i).toString().equals(favorite_team)) teams_sp.setSelection(i, true);
                }
                if(leagues_sp.getSelectedItem() != null) selected_league = leagues_sp.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            { }
        });

        leagues_spinnerAdapter.clear();
        for(int i=0; i<leagues_array.size(); i++) {
            leagues_spinnerAdapter.add(leagues_array.get(i));
        }

        for(int i=0; i<leagues_sp.getCount(); i++) {
            if(leagues_sp.getItemAtPosition(i).toString().equals(favorite_league)) leagues_sp.setSelection(i, true);
        }

        parent.addView(leagues_sp);

        parent.addView(teams_sp);

        final Button invio = new Button(this);
        invio.setLayoutParams(param);
        invio.setText("INVIO");

        parent.addView(invio);

        last_update_view = new TextView(this);
        last_update_view.setLayoutParams(param);
        last_update_view.setText(SplashScreen.last_update);
        parent.addView(last_update_view);

        invio.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                sendMessage(v);
                league_selected = leagues_sp.getSelectedItemPosition();
                team_selected = teams_sp.getSelectedItem().toString();
            }
        });

        Button update = new Button(this);
        update.setLayoutParams(param);
        update.setText("AGGIORNA");

        parent.addView(update);

        update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                startUpdateService(v);
                finish();
            }
        });

        final TextView favorite = new TextView(this);
        favorite.setLayoutParams(param);
        if( !(favorite_team.equals("")) && !(favorite_league.equals(""))) favorite.setText(favorite_team +" (" +favorite_league +")");
        else favorite.setText("");

        //Button set_favorite = new Button(this);
        set_favorite.setLayoutParams(param);
        set_favorite.setText("Imposta come preferita");

        set_favorite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                favorite_league = selected_league;
                favorite_team = selected_team;
                startWriteFavorite(v);
                if( !(favorite_team.equals("")) && !(favorite_league.equals(""))) favorite.setText(favorite_team +" (" +favorite_league +")");
            }
        });

        parent.addView(favorite);
        parent.addView(set_favorite);

        Button test_calendar = new Button(this);
        test_calendar.setLayoutParams(param);
        test_calendar.setText("Prova calendario");
        test_calendar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                startTestCalendar(v);
            }
        });
        parent.addView(test_calendar);
/*
        leagues_listviewAdapter = new ArrayAdapter<String>(this, R.layout.listview_row);
        leagues_listviewAdapter.clear();
        for(int i=0; i<leagues_array.size(); i++) { leagues_listviewAdapter.add(leagues_array.get(i)); }

        final ListView listView = new ListView(this);
        listView.setAdapter(leagues_listviewAdapter);
        for(int i=0; i<listView.getCount(); i++) {
            Log.d("MainActivity", "step " +i +" di leagues_sp: team is " +listView.getItemAtPosition(i).toString());
            if(listView.getItemAtPosition(i).toString().equals(favorite_league)) listView.setSelection(i);
        }
        parent.addView(listView);
*/
        // put this at the end of the code, because the layout is created programmatically
        String manufacturer1 = "xiaomi";
        String manufacturer2 = "meizu";
        if(manufacturer1.equalsIgnoreCase(android.os.Build.MANUFACTURER) ||
           manufacturer2.equalsIgnoreCase(android.os.Build.MANUFACTURER)) {

            //Log.d("MainActivity", "Manufacturer is " +manufacturer);
            Log.d("MainActivity", "Manufacturer is " +android.os.Build.MANUFACTURER);

            try {
                File cDir = getBaseContext().getCacheDir();
                File tempFile = new File(cDir.getPath() + "/" + "autostart_prompt.txt");
                FileReader fReader = new FileReader(tempFile);
                BufferedReader bReader = new BufferedReader(fReader);

                if( !((bReader.readLine()).equals("dontask") ) ) {
                    Log.d("MainActivity", "Showing autostart prompt because file does not say \"DON'T ASK\"");
                    DialogFragment dialog = ShowAutostartPrompt.newInstance(android.os.Build.MANUFACTURER);
                    dialog.show(this.getFragmentManager(), "autostart");
                }
            } catch (IOException e) {
                e.printStackTrace();

                Log.d("MainActivity", "Showing autostart prompt because of no file");
                DialogFragment dialog = ShowAutostartPrompt.newInstance(android.os.Build.MANUFACTURER);
                dialog.show(this.getFragmentManager(), "autostart");
            }
        }
    }

    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplaySchedule.class);
        startActivity(intent);
    }

    /** Called when the user clicks the Update button */
    public void startUpdateService (View view) {
        Intent intent= new Intent();
        intent.setAction("com.example.myfirstapp_withsplashscreen.SINGLE_UPDATE");
        sendBroadcast(intent);
    }

    /** Called when the user clicks the set_favorite button */
    public void startWriteFavorite (View view) {
        Intent intent= new Intent();
        intent.setAction("com.example.myfirstapp_withsplashscreen.WRITE_FAVORITE");
        intent.putExtra("league", favorite_league);
        intent.putExtra("team", favorite_team);
        sendBroadcast(intent);
    }

    /** Called when the user clicks the test_calendar button */
    public void startTestCalendar (View view) {
        Intent intent= new Intent();
        intent.setAction("com.example.myfirstapp_withsplashscreen.TEST_CALENDAR");
        sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        last_update_view.setText(SplashScreen.last_update);
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("myTag", "qui è scattato onPause");
        super.onPause();
    }
}
