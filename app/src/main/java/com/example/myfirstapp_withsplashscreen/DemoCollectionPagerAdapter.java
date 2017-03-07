package com.example.myfirstapp_withsplashscreen;

import android.graphics.Typeface;
import android.os.Bundle;
import android.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Calendar;

import static android.view.Gravity.CENTER;
import static android.view.Gravity.CENTER_HORIZONTAL;
import static android.view.Gravity.CENTER_VERTICAL;

// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
public class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {

    public DemoCollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new DemoObjectFragment();
        Bundle args = new Bundle();
        // Our object is just an integer :-P
        args.putInt(DemoObjectFragment.ARG_OBJECT, i + 1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        //return 100;
        return 2; // number of tabs
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }

    // Instances of this class are fragments representing a single
    // object in our collection.
    public static class DemoObjectFragment extends Fragment {
        public static final String ARG_OBJECT = "object";

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            TableRow.LayoutParams param_row = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            TableRow.LayoutParams param_row_teams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1);
            LinearLayout.LayoutParams param_lin = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            View rootView;
            Bundle args = getArguments();
            if( args.getInt(ARG_OBJECT)==1 ) {
                rootView = inflater.inflate(R.layout.schedule_frag, container, false);

                TableRow text_row[] = new TableRow[50];

                final TextView text_date[] = new TextView[50];
                final TextView text_hour[] = new TextView[50];
                final TextView text_home[] = new TextView[50];
                final TextView text_away[] = new TextView[50];
                final TextView text_home_score[] = new TextView[50];
                final TextView text_away_score[] = new TextView[50];
                final TextView text_place[] = new TextView[50];
                final TextView text_address[] = new TextView[50];

                LinearLayout date_hour[] = new LinearLayout[50];
                LinearLayout teams[] = new LinearLayout[50];
                LinearLayout scores[] = new LinearLayout[50];

                int j = 0;
                int table_size = SplashScreen.full_schedule_array.get(MainActivity.league_selected).size() - 1;
                Log.d("PagerAdapter", "starting parsing the schedule table with " +table_size +" rows");
                for(int i=0;i<SplashScreen.full_schedule_array.get(MainActivity.league_selected).size();i++)
                {
                    if( (SplashScreen.full_schedule_array.get(MainActivity.league_selected).get(i).get_home().equals(MainActivity.team_selected)) |
                            (SplashScreen.full_schedule_array.get(MainActivity.league_selected).get(i).get_away().equals(MainActivity.team_selected)) ){

                        text_date[j] = new TextView(getContext());
                        text_date[j].setLayoutParams(param_row);
                        text_date[j].setHeight(50);
                        text_date[j].setGravity(CENTER);
                        text_date[j].setText(SplashScreen.full_schedule_array.get(MainActivity.league_selected).get(i).get_date().replace("/20", "/"));

                        text_hour[j] = new TextView(getContext());
                        text_hour[j].setLayoutParams(param_row);
                        text_hour[j].setHeight(50);
                        text_hour[j].setGravity(CENTER);
                        text_hour[j].setText(SplashScreen.full_schedule_array.get(MainActivity.league_selected).get(i).get_hour());

                        date_hour[j] = new LinearLayout(getContext());
                        date_hour[j].setLayoutParams(param_row);
                        date_hour[j].setOrientation(LinearLayout.VERTICAL);
                        date_hour[j].addView(text_date[j]);
                        date_hour[j].addView(text_hour[j]);
                        date_hour[j].setPadding(0, 0, 32, 0);
                        date_hour[j].setGravity(CENTER);

                        text_home[j] = new TextView(getContext());
                        text_home[j].setLayoutParams(param_lin);
                        text_home[j].setHeight(50);
                        text_home[j].setText(SplashScreen.full_schedule_array.get(MainActivity.league_selected).get(i).get_home());

                        text_away[j] = new TextView(getContext());
                        text_away[j].setLayoutParams(param_lin);
                        text_away[j].setHeight(50);
                        text_away[j].setText(SplashScreen.full_schedule_array.get(MainActivity.league_selected).get(i).get_away());

                        teams[j] = new LinearLayout(getContext());
                        //teams[j].setLayoutParams(param_row);
                        teams[j].setLayoutParams(param_row_teams); // useful to let teams fill the screen
                        teams[j].setOrientation(LinearLayout.VERTICAL);
                        teams[j].setPadding(0, 0, 32, 0);
                        teams[j].addView(text_home[j]);
                        teams[j].addView(text_away[j]);

                        text_home_score[j] = new TextView(getContext());
                        text_home_score[j].setLayoutParams(param_lin);
                        text_home_score[j].setHeight(50);
                        text_home_score[j].setWidth(60);
                        text_home_score[j].setText(SplashScreen.full_schedule_array.get(MainActivity.league_selected).get(i).get_home_score());

                        text_away_score[j] = new TextView(getContext());
                        text_away_score[j].setLayoutParams(param_lin);
                        text_away_score[j].setHeight(50);
                        text_away_score[j].setWidth(60);
                        text_away_score[j].setText(SplashScreen.full_schedule_array.get(MainActivity.league_selected).get(i).get_away_score());

                        scores[j] = new LinearLayout(getContext());
                        scores[j].setLayoutParams(param_row);
                        scores[j].setOrientation(LinearLayout.VERTICAL);
                        scores[j].addView(text_home_score[j]);
                        scores[j].addView(text_away_score[j]);

                        text_row[j] = new TableRow(getContext());
                        text_row[j].setLayoutParams(param);
                        text_row[j].setOrientation(LinearLayout.HORIZONTAL);
                        text_row[j].setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
                        text_row[j].setDividerPadding(10);
                        text_row[j].addView(date_hour[j]);
                        text_row[j].addView(teams[j]);
                        text_row[j].addView(scores[j]);

                        // Not added in the row.. just to get the text later.. (use array list?)
                        text_place[j] = new TextView(getContext());
                        text_place[j].setLayoutParams(param_lin);
                        text_place[j].setHeight(50);
                        text_place[j].setWidth(60);
                        text_place[j].setText(SplashScreen.full_schedule_array.get(MainActivity.league_selected).get(i).get_place());
                        text_address[j] = new TextView(getContext());
                        text_address[j].setLayoutParams(param_lin);
                        text_address[j].setHeight(50);
                        text_address[j].setWidth(60);
                        text_address[j].setText(SplashScreen.full_schedule_array.get(MainActivity.league_selected).get(i).get_address());

                        j++;
                    }
                }

                boolean change_made = true;
                String temp_text;
                int year1;
                int year2;
                while(change_made) {
                    change_made = false;

                    for(int i=1; i<j; i++) {
                        String[] date = text_date[i-1].getText().toString().split("/");
                        year1 = Integer.parseInt(date[2]); // year is the third element of the date

                        date = text_date[i].getText().toString().split("/");
                        year2 = Integer.parseInt(date[2]); // year is the third element of the date

                        if(year2<year1) { // swap rows

                            temp_text = text_date[i].getText().toString();
                            text_date[i].setText(text_date[i-1].getText());
                            text_date[i-1].setText(temp_text);

                            temp_text = text_hour[i].getText().toString();
                            text_hour[i].setText(text_hour[i-1].getText());
                            text_hour[i-1].setText(temp_text);

                            temp_text = text_home[i].getText().toString();
                            text_home[i].setText(text_home[i-1].getText());
                            text_home[i-1].setText(temp_text);

                            temp_text = text_away[i].getText().toString();
                            text_away[i].setText(text_away[i-1].getText());
                            text_away[i-1].setText(temp_text);

                            temp_text = text_home_score[i].getText().toString();
                            text_home_score[i].setText(text_home_score[i-1].getText());
                            text_home_score[i-1].setText(temp_text);

                            temp_text = text_away_score[i].getText().toString();
                            text_away_score[i].setText(text_away_score[i-1].getText());
                            text_away_score[i-1].setText(temp_text);

                            temp_text = text_place[i].getText().toString();
                            text_place[i].setText(text_place[i-1].getText());
                            text_place[i-1].setText(temp_text);

                            temp_text = text_address[i].getText().toString();
                            text_address[i].setText(text_address[i-1].getText());
                            text_address[i-1].setText(temp_text);

                            change_made = true;
                        }
                    }
                }

                change_made = true;
                int month1;
                int month2;
                while(change_made) {
                    change_made = false;

                    for(int i=1; i<j; i++) {
                        String[] date = text_date[i-1].getText().toString().split("/");
                        year1 = Integer.parseInt(date[2]); // year is the 3rd element of the date
                        month1 = Integer.parseInt(date[1]); // month is the 2nd element of the date

                        date = text_date[i].getText().toString().split("/");
                        year2 = Integer.parseInt(date[2]); // year is the 3rd element of the date
                        month2 = Integer.parseInt(date[1]); // month is the 2nd element of the date

                        if( (month2<month1) & (year2==year1)) { // swap rows

                            temp_text = text_date[i].getText().toString();
                            text_date[i].setText(text_date[i-1].getText());
                            text_date[i-1].setText(temp_text);

                            temp_text = text_hour[i].getText().toString();
                            text_hour[i].setText(text_hour[i-1].getText());
                            text_hour[i-1].setText(temp_text);

                            temp_text = text_home[i].getText().toString();
                            text_home[i].setText(text_home[i-1].getText());
                            text_home[i-1].setText(temp_text);

                            temp_text = text_away[i].getText().toString();
                            text_away[i].setText(text_away[i-1].getText());
                            text_away[i-1].setText(temp_text);

                            temp_text = text_home_score[i].getText().toString();
                            text_home_score[i].setText(text_home_score[i-1].getText());
                            text_home_score[i-1].setText(temp_text);

                            temp_text = text_away_score[i].getText().toString();
                            text_away_score[i].setText(text_away_score[i-1].getText());
                            text_away_score[i-1].setText(temp_text);

                            temp_text = text_place[i].getText().toString();
                            text_place[i].setText(text_place[i-1].getText());
                            text_place[i-1].setText(temp_text);

                            temp_text = text_address[i].getText().toString();
                            text_address[i].setText(text_address[i-1].getText());
                            text_address[i-1].setText(temp_text);

                            change_made = true;
                        }
                    }
                }

                change_made = true;
                int day1;
                int day2;
                while(change_made) {
                    change_made = false;

                    for(int i=1; i<j; i++) {
                        String[] date = text_date[i-1].getText().toString().split("/");
                        year1 = Integer.parseInt(date[2]); // year is the 3rd element of the date
                        month1 = Integer.parseInt(date[1]); // month is the 2nd element of the date
                        day1 = Integer.parseInt(date[0]); // day is the 1st element of the date

                        date = text_date[i].getText().toString().split("/");
                        year2 = Integer.parseInt(date[2]); // year is the 3rd element of the date
                        month2 = Integer.parseInt(date[1]); // month is the 2nd element of the date
                        day2 = Integer.parseInt(date[0]); // day is the 1st element of the date

                        if( (day2<day1) & (month2==month1) & (year2==year1)) { // swap rows

                            temp_text = text_date[i].getText().toString();
                            text_date[i].setText(text_date[i-1].getText());
                            text_date[i-1].setText(temp_text);

                            temp_text = text_hour[i].getText().toString();
                            text_hour[i].setText(text_hour[i-1].getText());
                            text_hour[i-1].setText(temp_text);

                            temp_text = text_home[i].getText().toString();
                            text_home[i].setText(text_home[i-1].getText());
                            text_home[i-1].setText(temp_text);

                            temp_text = text_away[i].getText().toString();
                            text_away[i].setText(text_away[i-1].getText());
                            text_away[i-1].setText(temp_text);

                            temp_text = text_home_score[i].getText().toString();
                            text_home_score[i].setText(text_home_score[i-1].getText());
                            text_home_score[i-1].setText(temp_text);

                            temp_text = text_away_score[i].getText().toString();
                            text_away_score[i].setText(text_away_score[i-1].getText());
                            text_away_score[i-1].setText(temp_text);

                            temp_text = text_place[i].getText().toString();
                            text_place[i].setText(text_place[i-1].getText());
                            text_place[i-1].setText(temp_text);

                            temp_text = text_address[i].getText().toString();
                            text_address[i].setText(text_address[i-1].getText());
                            text_address[i-1].setText(temp_text);

                            change_made = true;
                        }
                    }
                }

                Calendar c = Calendar.getInstance();
                int actual_day = c.get(Calendar.DAY_OF_MONTH);
                int actual_month = c.get(Calendar.MONTH) + 1;
                int actual_year = c.get(Calendar.YEAR) - 2000;
                Log.d("PagerAdapter", actual_day +" " +actual_month +" " +actual_year);

                // Highlight with bold the selected team's name and score with blue
                // Color the next match
                change_made = false;
                for(int i=0; i<j; i++) {
                    String[] date = text_date[i].getText().toString().split("/");
                    year1 = Integer.parseInt(date[2]); // year is the 3rd element of the date
                    month1 = Integer.parseInt(date[1]); // month is the 2nd element of the date
                    day1 = Integer.parseInt(date[0]); // day is the 1st element of the date
/*
                    if( ((year1>actual_year) |
                            ((year1>=actual_year) & (month1>actual_month)) |
                            ((year1>=actual_year) & (month1>=actual_month) & (day1>=actual_day))) & !change_made) {
                        text_date[i].setTextColor(Color.BLUE);
                        text_hour[i].setTextColor(Color.BLUE);
                        text_home[i].setTextColor(Color.BLUE);
                        text_away[i].setTextColor(Color.BLUE);
                        text_home_score[i].setTextColor(Color.BLUE);
                        text_away_score[i].setTextColor(Color.BLUE);

                        change_made = true;
                    }
*/
                    if(text_home[i].getText().equals(MainActivity.team_selected)) {
                        text_home[i].setTypeface(null, Typeface.BOLD);
                        text_home_score[i].setTypeface(null, Typeface.BOLD);
                    } else if(text_away[i].getText().equals(MainActivity.team_selected)) {
                        text_away[i].setTypeface(null, Typeface.BOLD);
                        text_away_score[i].setTypeface(null, Typeface.BOLD);
                    }
                }

                for(int i=0; i<j; i++) {
                    final String home = text_home[i].getText().toString();
                    final String away = text_away[i].getText().toString();
                    final String date = text_date[i].getText().toString();
                    final String hour = text_hour[i].getText().toString();
                    final String place = text_place[i].getText().toString();
                    final String address = text_address[i].getText().toString();
                    text_row[i].setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            //v.setBackgroundColor(Color.RED);
                            DialogFragment dialog = ShowMatchDetail.newInstance(home, away, date, hour, place, address);

                            dialog.show(getActivity().getFragmentManager(), "missiles");
                        }
                    });
                    ((TableLayout) rootView.findViewById(R.id.table)).addView(text_row[i]);
                }

            } else { // only two tabs
                rootView = inflater.inflate(R.layout.ranking_frag, container, false);

                TableRow header_row;

                TextView header_team;
                header_team = new TextView(getContext());
                header_team.setLayoutParams(param_row_teams);
                header_team.setHeight(50);
                header_team.setPadding(0, 0, 32, 0);
                header_team.setGravity(CENTER);
                header_team.setText("Squadra");
                header_team.setTypeface(null, Typeface.BOLD);
                TextView header_points;
                header_points = new TextView(getContext());
                header_points.setLayoutParams(param_row);
                header_points.setHeight(50);
                header_points.setWidth(60);
                header_points.setGravity(CENTER);
                header_points.setText("P");
                header_points.setTypeface(null, Typeface.BOLD);
                TextView header_played;
                header_played = new TextView(getContext());
                header_played.setLayoutParams(param_row);
                header_played.setHeight(50);
                header_played.setWidth(60);
                header_played.setGravity(CENTER);
                header_played.setText("G");
                header_played.setTypeface(null, Typeface.BOLD);
                TextView header_wons;
                header_wons = new TextView(getContext());
                header_wons.setLayoutParams(param_row);
                header_wons.setHeight(50);
                header_wons.setWidth(60);
                header_wons.setGravity(CENTER);
                header_wons.setText("V");
                header_wons.setTypeface(null, Typeface.BOLD);
                TextView header_set_won;
                header_set_won = new TextView(getContext());
                header_set_won.setLayoutParams(param_row);
                header_set_won.setHeight(50);
                header_set_won.setWidth(60);
                header_set_won.setGravity(CENTER);
                header_set_won.setText("SV");
                header_set_won.setTypeface(null, Typeface.BOLD);
                TextView header_set_lost;
                header_set_lost = new TextView(getContext());
                header_set_lost.setLayoutParams(param_row);
                header_set_lost.setHeight(50);
                header_set_lost.setWidth(60);
                header_set_lost.setGravity(CENTER);
                header_set_lost.setText("SP");
                header_set_lost.setTypeface(null, Typeface.BOLD);
                TextView header_ratio;
                header_ratio = new TextView(getContext());
                header_ratio.setLayoutParams(param_row);
                header_ratio.setHeight(50);
                header_ratio.setWidth(60);
                header_ratio.setGravity(CENTER);
                header_ratio.setText("Q");
                header_ratio.setTypeface(null, Typeface.BOLD);

                header_row = new TableRow(getContext());
                header_row.setLayoutParams(param);
                header_row.setOrientation(TableRow.HORIZONTAL);
                header_row.setShowDividers(TableRow.SHOW_DIVIDER_MIDDLE);
                header_row.setPadding(0, 0, 0, 32);
                header_row.addView(header_team);
                header_row.addView(header_points);
                header_row.addView(header_played);
                header_row.addView(header_wons);
                header_row.addView(header_set_won);
                header_row.addView(header_set_lost);
                header_row.addView(header_ratio);

                ((TableLayout) rootView.findViewById(R.id.ranking_table)).addView(header_row);

                TableRow text_row[] = new TableRow[26];

                TextView text_team[] = new TextView[26];
                TextView text_points[] = new TextView[26];
                TextView text_played[] = new TextView[26];
                TextView text_wons[] = new TextView[26];
                TextView text_set_won[] = new TextView[26];
                TextView text_set_lost[] = new TextView[26];
                TextView text_ratio[] = new TextView[26];

                int j = 0;
                //Elements rows = MainActivity.arr_ranking.select("tr");
                int table_size = SplashScreen.full_ranking_array.get(MainActivity.league_selected).size() - 1;
                Log.d("PagerAdapter", "starting parsing the ranking table with " +table_size +" rows");
                //for(int i=1;i<full_ranking_array.get(league_selected).size().size();i++) // la prima riga contiene le intestazioni della tabella e possiamo scartarla
                for(int i=0;i<SplashScreen.full_ranking_array.get(MainActivity.league_selected).size();i++)
                {
                    //Element row = rows.get(i);
                    //Elements cols = row.select("td");
                    //Element team_r = cols.get(0); // not global "team" variable, this is just the team of this row
                    //Element points = cols.get(1);
                    //Element played = cols.get(2);
                    //Element wons = cols.get(3);
                    //Element set_won = cols.get(4);
                    //Element set_lost = cols.get(5);
                    //Element ratio = cols.get(6);

                    text_team[j] = new TextView(getContext());
                    text_team[j].setLayoutParams(param_row_teams);
                    text_team[j].setHeight(50);
                    text_team[j].setPadding(0, 0, 32, 0);
                    text_team[j].setLines(2);
                    text_team[j].setGravity(CENTER_VERTICAL);
                    text_team[j].setGravity(CENTER_HORIZONTAL);
                    text_team[j].setText(SplashScreen.full_ranking_array.get(MainActivity.league_selected).get(i).get_team());

                    text_points[j] = new TextView(getContext());
                    text_points[j].setLayoutParams(param_row);
                    text_points[j].setHeight(50);
                    text_points[j].setWidth(60);
                    text_points[j].setGravity(CENTER);
                    text_points[j].setText(SplashScreen.full_ranking_array.get(MainActivity.league_selected).get(i).get_points());

                    text_played[j] = new TextView(getContext());
                    text_played[j].setLayoutParams(param_row);
                    text_played[j].setHeight(50);
                    text_played[j].setWidth(60);
                    text_played[j].setGravity(CENTER);
                    text_played[j].setText(SplashScreen.full_ranking_array.get(MainActivity.league_selected).get(i).get_played());

                    text_wons[j] = new TextView(getContext());
                    text_wons[j].setLayoutParams(param_row);
                    text_wons[j].setHeight(50);
                    text_wons[j].setWidth(60);
                    text_wons[j].setGravity(CENTER);
                    text_wons[j].setText(SplashScreen.full_ranking_array.get(MainActivity.league_selected).get(i).get_wons());

                    text_set_won[j] = new TextView(getContext());
                    text_set_won[j].setLayoutParams(param_row);
                    text_set_won[j].setHeight(50);
                    text_set_won[j].setWidth(60);
                    text_set_won[j].setGravity(CENTER);
                    text_set_won[j].setText(SplashScreen.full_ranking_array.get(MainActivity.league_selected).get(i).get_set_won());

                    text_set_lost[j] = new TextView(getContext());
                    text_set_lost[j].setLayoutParams(param_row);
                    text_set_lost[j].setHeight(50);
                    text_set_lost[j].setWidth(60);
                    text_set_lost[j].setGravity(CENTER);
                    text_set_lost[j].setText(SplashScreen.full_ranking_array.get(MainActivity.league_selected).get(i).get_set_lost());

                    text_ratio[j] = new TextView(getContext());
                    text_ratio[j].setLayoutParams(param_row);
                    text_ratio[j].setHeight(50);
                    text_ratio[j].setWidth(60);
                    text_ratio[j].setGravity(CENTER);
                    text_ratio[j].setText(SplashScreen.full_ranking_array.get(MainActivity.league_selected).get(i).get_ratio());

                    text_row[j] = new TableRow(getContext());
                    text_row[j].setLayoutParams(param);
                    text_row[j].setOrientation(LinearLayout.HORIZONTAL);
                    text_row[j].setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
                    text_row[j].setDividerPadding(10);
                    text_row[j].addView(text_team[j]);
                    text_row[j].addView(text_points[j]);
                    text_row[j].addView(text_played[j]);
                    text_row[j].addView(text_wons[j]);
                    text_row[j].addView(text_set_won[j]);
                    text_row[j].addView(text_set_lost[j]);
                    text_row[j].addView(text_ratio[j]);

                    ((TableLayout) rootView.findViewById(R.id.ranking_table)).addView(text_row[j]);

                    j++;
                }

                // Highlight with bold the selected team's name and score
                for(int i=0; i<j; i++) {
                    if(text_team[i].getText().equals(MainActivity.team_selected)) {
                        text_team[i].setTypeface(null, Typeface.BOLD);
                        text_points[i].setTypeface(null, Typeface.BOLD);
                        text_played[i].setTypeface(null, Typeface.BOLD);
                        text_wons[i].setTypeface(null, Typeface.BOLD);
                        text_set_won[i].setTypeface(null, Typeface.BOLD);
                        text_set_lost[i].setTypeface(null, Typeface.BOLD);
                        text_ratio[i].setTypeface(null, Typeface.BOLD);
                    }
                }

            }
            return rootView;
        }
    }
}
