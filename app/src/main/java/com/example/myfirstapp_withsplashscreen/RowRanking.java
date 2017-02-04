package com.example.myfirstapp_withsplashscreen;

public class RowRanking {

    String team;
    String points;
    String played;
    String wons;
    String set_won;
    String set_lost;
    String ratio;

    public RowRanking(){
    }

    public void init_RowRanking() {
        team = "";
        points = "";
        played = "";
        wons = "";
        set_won = "";
        set_lost = "";
        ratio = "";
    }

    public void set_team(String string) {
        team = string;
    }

    public String get_team() {
        return team;
    }

    public void set_points(String string) {
        points = string;
    }

    public String get_points() {
        return points;
    }

    public void set_played(String string) {
        played = string;
    }

    public String get_played() {
        return played;
    }

    public void set_wons(String string) {
        wons = string;
    }

    public String get_wons() {
        return wons;
    }

    public void set_set_won(String string) {
        set_won = string;
    }

    public String get_set_won() {
        return set_won;
    }

    public void set_set_lost(String string) {
        set_lost = string;
    }

    public String get_set_lost() {
        return set_lost;
    }

    public void set_ratio(String string) {
        ratio = string;
    }

    public String get_ratio() {
        return ratio;
    }
}
