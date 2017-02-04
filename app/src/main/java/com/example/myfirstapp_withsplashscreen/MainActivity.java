package com.example.myfirstapp_withsplashscreen;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    public LinearLayout parent;
    //public RelativeLayout parent;

    ArrayAdapter<String> leagues_spinnerAdapter;
    Spinner leagues_sp;
    ArrayAdapter<String> teams_spinnerAdapter;
    Spinner teams_sp;

    public static int league_selected;
    public static String team_selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        ArrayList<String> leagues_array = intent.getStringArrayListExtra("leagues");
        String last_update = intent.getStringExtra("last_update");

        for(int i=0; i<SplashScreen.full_teams_array.size(); i++){
            Collections.sort(SplashScreen.full_teams_array.get(i), String.CASE_INSENSITIVE_ORDER);
        }

        LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        parent = new LinearLayout(this);
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setLayoutParams(param);

        //RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        //parent = new RelativeLayout(this);
        //parent.setLayoutParams(param);

        setContentView(parent);
/*
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("url", "http://www.fipav.pavia.it/calendari.asp");
        startService(intent);
*/

        // preparazione dello Spinner per mostrare l'elenco delle squadre
        teams_spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_row);
        //param = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //param.addRule(RelativeLayout.CENTER_IN_PARENT);
        teams_sp = new Spinner(this);
        teams_sp.setLayoutParams(param);
        teams_sp.setAdapter(teams_spinnerAdapter);
        teams_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                TextView txt = (TextView) arg1.findViewById(arg1.getId());
                //invio.setText(MainActivity.leagues_sp.getSelectedItem().toString() +" - " +MainActivity.teams_sp.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            { }
        });

        // preparazione dello Spinner per mostrare l'elenco dei campionati
        leagues_spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_row);
        //param = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //param.addRule(RelativeLayout.CENTER_IN_PARENT);
        //param.addRule(RelativeLayout.ABOVE, teams_sp.getId());
        leagues_sp = new Spinner(this);
        leagues_sp.setLayoutParams(param);
        leagues_sp.setAdapter(leagues_spinnerAdapter);
        leagues_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {/*
                TextView txt = (TextView) arg1.findViewById(arg1.getId());
                String s = txt.getText().toString();
                String s_mod = s.replace(" ", "%20");

                Intent intent_teams = new Intent(MainActivity.this, DownloadTeamsService.class);
                intent_teams.putExtra("schedule_url", "http://www.fipav.pavia.it/calendari.asp?id=Camp&scelta=" +s_mod);
                intent_teams.putExtra("ranking_url", "http://www.fipav.pavia.it/classifica.asp?id=Camp&scelta=" +s_mod);
                startService(intent_teams);*/

                teams_spinnerAdapter.clear();
                for(int i=0; i<SplashScreen.full_teams_array.get(leagues_sp.getSelectedItemPosition()).size(); i++) {
                    teams_spinnerAdapter.add(SplashScreen.full_teams_array.get(leagues_sp.getSelectedItemPosition()).get(i).toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            { }
        });

        for(int i=0; i<leagues_array.size(); i++) {
            leagues_spinnerAdapter.add(leagues_array.get(i));
        }

        parent.addView(leagues_sp);

        parent.addView(teams_sp);

        final Button invio = new Button(this);
        //param = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //param.removeRule(RelativeLayout.ABOVE);
        //param.addRule(RelativeLayout.BELOW, teams_sp.getId());
        invio.setLayoutParams(param);
        invio.setText("INVIO");

        parent.addView(invio);

        TextView last_update_view = new TextView(this);
        last_update_view.setLayoutParams(param);
        last_update_view.setText(last_update);
        parent.addView(last_update_view);

        invio.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                sendMessage(v);
                league_selected = leagues_sp.getSelectedItemPosition();
                team_selected = teams_sp.getSelectedItem().toString();
            }
        });

        final Button update = new Button(this);
        update.setLayoutParams(param);
        update.setText("AGGIORNA");

        parent.addView(update);

        update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                startUpdateService(v);
                //league_selected = leagues_sp.getSelectedItemPosition();
                //team_selected = teams_sp.getSelectedItem().toString();
                update.setClickable(false);
                invio.setClickable(false);
                update.setText("ATTENDERE ...");
            }
        });
    }

    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplaySchedule.class);
        startActivity(intent);
    }

    /** Called when the user clicks the Update button */
    public void startUpdateService (View view) {
        //Intent intent = new Intent(this, UpdateService.class);
        //intent.setAction("com.example.myfirstapp_withsplashscreen.UpdateService");
        //startService(intent);
        Intent intent= new Intent();
        intent.setAction("com.example.myfirstapp_withsplashscreen.SINGLE_UPDATE");
        //intent.putExtra(MainActivity.INTENT_EXTRA, "Additional info");
        sendBroadcast(intent);
    }
/*
    private MyTestReceiver mReceiver;
    private TeamsReceiver TeamsReceiver;

    @Override
    protected void onPause() {
        unregisterReceiver(mReceiver);
        unregisterReceiver(TeamsReceiver);
        Log.d("myTag", "qui è scattato onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        mReceiver=new MyTestReceiver(this);
        registerReceiver( mReceiver, new IntentFilter(INTENT_ACTION));
        TeamsReceiver=new TeamsReceiver(this);
        registerReceiver( TeamsReceiver, new IntentFilter(INTENT_DOWNLOAD_TEAMS));
        Log.d("myTag", "qui è scattato onResume");
        super.onResume();
    }*/
}
