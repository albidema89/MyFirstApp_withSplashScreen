package com.example.myfirstapp_withsplashscreen;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ShowAutostartPrompt extends DialogFragment {

    String manufacturer;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static ShowAutostartPrompt newInstance(String manufacturer) {
        ShowAutostartPrompt f = new ShowAutostartPrompt();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("manufacturer", manufacturer);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        manufacturer = getArguments().getString("manufacturer");
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Log.d("AutostartPrompt", "Building dialog");
        builder.setMessage("Dal momento che utilizzi un dispositivo " +manufacturer +", devi abilitare questa applicazione all'avvio " +
                "automatico, altrimenti non sarà possibile aggiornare in background i calendari e le classifiche.")
                .setPositiveButton("VAI", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("AutostartPrompt", "Selected VAI");
                        //this will open auto start screen where user can enable permission for your app
                        Intent autostart_intent = new Intent();
                        autostart_intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                        startActivity(autostart_intent);
                    }
                })
                .setNegativeButton("NON CHIEDERE PIU'", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        Log.d("myTag", "Selected DON'T ASK ANYMORE");

                        File cDir = getActivity().getBaseContext().getCacheDir();
                        File tempFile = new File(cDir.getPath() + "/" + "autostart_prompt.txt");
                        try {
                            FileWriter writer = new FileWriter(tempFile);
                            writer.write("dontask");
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
    }
}
