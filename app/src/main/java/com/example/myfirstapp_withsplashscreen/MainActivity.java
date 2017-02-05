package com.example.myfirstapp_withsplashscreen;

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

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    public LinearLayout parent;

    ArrayAdapter<String> leagues_spinnerAdapter;
    Spinner leagues_sp;
    ArrayAdapter<String> teams_spinnerAdapter;
    Spinner teams_sp;

    public static int league_selected;
    public static String team_selected;

    TextView last_update_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        ArrayList<String> leagues_array = intent.getStringArrayListExtra("leagues");

        for(int i=0; i<SplashScreen.full_teams_array.size(); i++){
            Collections.sort(SplashScreen.full_teams_array.get(i), String.CASE_INSENSITIVE_ORDER);
        }

        LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        parent = new LinearLayout(this);
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setLayoutParams(param);

        setContentView(parent);

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
                TextView txt = (TextView) arg1.findViewById(arg1.getId());
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
        leagues_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
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

        final Button update = new Button(this);
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

    @Override
    protected void onResume() {
        last_update_view.setText(SplashScreen.last_update);
        Log.d("myTag", "qui è scattato onResume");
        super.onResume();
    }
}
