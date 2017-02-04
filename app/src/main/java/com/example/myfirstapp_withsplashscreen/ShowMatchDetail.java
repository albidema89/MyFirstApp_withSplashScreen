package com.example.myfirstapp_withsplashscreen;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

public class ShowMatchDetail extends DialogFragment {

    String home;
    String away;
    String date;
    String hour;
    String place;
    String address;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static ShowMatchDetail newInstance(String home, String away, String date, String hour, String place, String address) {
        ShowMatchDetail f = new ShowMatchDetail();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("home", home);
        args.putString("away", away);
        args.putString("date", date);
        args.putString("hour", hour);
        args.putString("place", place);
        args.putString("address", address);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        home = getArguments().getString("home");
        away = getArguments().getString("away");
        date = getArguments().getString("date");
        hour = getArguments().getString("hour");
        place = getArguments().getString("place");
        address = getArguments().getString("address");
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Log.d("myTag", "new builder");
        builder.setMessage(home +" - " +away +"\n\n" +date +" - " +hour +"\n\n" +place +", " +address)/*
                .setPositiveButton("INDIETRO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                        Log.d("myTag", "selected sassi");
                    }
                })
                .setNegativeButton("scemo", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        Log.d("myTag", "selected scemo");
                    }
                })*/;
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
