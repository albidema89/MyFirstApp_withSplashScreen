package com.example.myfirstapp_withsplashscreen;

import android.app.DialogFragment;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.mySimpleExpandableListAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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

    ExpandableListView expListView;
    static mySimpleExpandableListAdapter expListAdapter;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

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
                showSchedule(v);
                league_selected = leagues_sp.getSelectedItemPosition();
                Log.d("MainActivity", "Selected league is " +league_selected);
                team_selected = teams_sp.getSelectedItem().toString();
                Log.d("MainActivity", "Selected team is " +team_selected);
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

        // get the listview
        expListView = new ExpandableListView(this);

        //final mySimpleExpandableListAdapter expListAdapter =
        expListAdapter =
                new mySimpleExpandableListAdapter(
                        this,
                        createLeagueList(),            // Creating group List.
                        R.layout.league_row,           // Group item layout XML.
                        new String[] { "League" },     // the key of group item.
                        new int[] { R.id.league_row1 }, // ID of each group item.-Data under the key goes into this TextView.
                        createTeamList(),             // childData describes second-level entries.
                        R.layout.team_row,             // Layout for sub-level entries(second level).
                        new String[] {"Team", "Favorite"},         // Keys in childData maps to display.
                        new int[] { R.id.team_row1, R.id.team_row2 }     // Data under the keys above go into these TextViews.
                );

        // setting list adapter
        expListView.setAdapter(expListAdapter); // setting the adapter in the list.

        // TODO: con più squadre preferite, questa espansione non ha più senso...
        Log.d("MainActivity", "Group count is " +expListAdapter.getGroupCount());
        for(int i=0; i<expListAdapter.getGroupCount(); i++) {
            Log.d("MainActivity", "Children count is " +expListAdapter.getChildrenCount(i));
            for(int j=0; j<expListAdapter.getChildrenCount(i); j++) {
                if(expListAdapter.getChild(i, j).toString().replace("{Team=", "").replaceAll(", Favorite=\\d+\\}", "").equals(favorite_team) &&
                        expListAdapter.getGroup(i).toString().replace("{League=", "").replace("}", "").equals(favorite_league)) {

                    Log.d("MainActivity", "Group " +i +" Team " +j +" is the favorite");
                    expListView.expandGroup(i);
                }
            }
        }

        parent.addView(expListView);

        //expListAdapter.setChild_team(0,0,"PROVA");

        // put this at the end of the code, because the layout is created programmatically
        String manufacturer1 = "xiaomi";
        String manufacturer2 = "meizu";
        if(manufacturer1.equalsIgnoreCase(android.os.Build.MANUFACTURER) ||
           manufacturer2.equalsIgnoreCase(android.os.Build.MANUFACTURER)) {

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
    public void showSchedule(View view) {
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

    /* Creating the Hashmap for the row */
    @SuppressWarnings("unchecked")
    private List createLeagueList() {
        ArrayList result = new ArrayList();
        for( int i = 0 ; i < leagues_array.size() ; ++i ) {
            HashMap m = new HashMap();
            m.put( "League", leagues_array.get(i)); // the key and it's value.
            result.add( m );
        }
        return (List)result;
    }

    /* Creating the HashMap for the children */
    @SuppressWarnings("unchecked")
    private List createTeamList() {

        ArrayList result = new ArrayList();
        for( int i = 0 ; i < leagues_array.size() ; ++i ) {
          /* each group need each HashMap-Here for each group we have 3 subgroups */
            ArrayList secList = new ArrayList();
            for( int n = 0 ; n < SplashScreen.full_teams_array.get(i).size() ; n++ ) {
                HashMap child = new HashMap();
                child.put( "Team", SplashScreen.full_teams_array.get(i).get(n) );
                boolean favorite_found = false;
                for(int j=0; j<SplashScreen.new_favorites_array.size(); j++) {
                    if(SplashScreen.full_teams_array.get(i).get(n).toString().equals(SplashScreen.new_favorites_array.get(j)[1]) &&
                            leagues_array.get(i).equals(SplashScreen.new_favorites_array.get(j)[0]) ) {
                        favorite_found = true;
                    }
                }
                if(favorite_found) { child.put( "Favorite", "1" ); }
                else { child.put( "Favorite", "0" ); }/*
                if(SplashScreen.full_teams_array.get(i).get(n).toString().equals(favorite_team) &&
                        leagues_array.get(i).equals(favorite_league) ) {
                    child.put( "Favorite", "1" );
                } else {
                }
                    child.put( "Favorite", "0" );/*
                }*/
                secList.add( child );
            }
            result.add( secList );
        }
        return result;
    }
/*
    public void prova_bottone (View v) {
        Log.d("MainActivity", "Click on button (from XML)");

    }*/
}
