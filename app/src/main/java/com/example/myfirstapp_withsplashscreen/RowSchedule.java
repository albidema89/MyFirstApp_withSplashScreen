package com.example.myfirstapp_withsplashscreen;

import android.content.Context;

import java.io.Serializable;

public class RowSchedule implements Serializable {

    String home;
    String away;
    String date;
    String hour;
    String place;
    String address;
    String home_score;
    String away_score;

    private Context context;
/*
    public RowSchedule(Context context){
        this.context = context;
    }
*/
    public RowSchedule(){
    }

    public void init_RowSchedule() {
        home = "";
        away = "";
        date = "";
        hour = "";
        place = "";
        address = "";
        home_score = "";
        away_score = "";
    }

    public void set_home(String string) {
        home = string;
    }

    public String get_home() {
        return home;
    }

    public void set_away(String string) {
        away = string;
    }

    public String get_away() {
        return away;
    }

    public void set_date(String string) {
        date = string;
    }

    public String get_date() {
        return date;
    }

    public void set_hour(String string) {
        hour = string;
    }

    public String get_hour() {
        return hour;
    }

    public void set_place(String string) {
        place = string;
    }

    public String get_place() {
        return place;
    }

    public void set_address(String string) {
        address = string;
    }

    public String get_address() {
        return address;
    }

    public void set_home_score(String string) {
        home_score = string;
    }

    public String get_home_score() {
        return home_score;
    }

    public void set_away_score(String string) {
        away_score = string;
    }

    public String get_away_score() {
        return away_score;
    }
}