package com.example.myfirstapp_withsplashscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class WriteFavoriteReceiver_new extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String league = intent.getStringExtra("league");
        String team   = intent.getStringExtra("team");
        Integer group = intent.getIntExtra("group", 0);
        Integer child = intent.getIntExtra("child", 0);
        boolean set   = intent.getBooleanExtra("set", false);

        boolean line_found = false;
        if(set) {
            Log.d("WriteFavorite_new", "Received intent, start writing team="+team+" league="+league);
            try {
                File inputFile = new File(context.getCacheDir().getPath() + "/" + "favorite_new.txt");
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                String currentLine;
                while((currentLine = reader.readLine()) != null) {
                        line_found = currentLine.equalsIgnoreCase(league+",,,"+team);
                }
                reader.close();
                if(!line_found) {
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(inputFile, true)); // append mode
                        writer.write(league+",,,"+team + "\n");
                        writer.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        Log.d("WriteFavorite_new", "IO Exception during favorite write, failed appending the string!");
                    }
                }
                MainActivity.expListAdapter.setChild_favorite(group,child,true);
            } catch (IOException e2) {
                e2.printStackTrace();
                Log.d("WriteFavorite_new", "IO Exception during favorite read, creating the file!");

                try {
                    File inputFile = new File(context.getCacheDir().getPath() + "/" + "favorite_new.txt");
                    BufferedWriter writer = new BufferedWriter(new FileWriter(inputFile));
                    writer.write(league+",,,"+team + "\n");
                    writer.close();
                    MainActivity.expListAdapter.setChild_favorite(group,child,true);
                } catch (IOException e3) {
                    e3.printStackTrace();
                    Log.d("WriteFavorite_new", "IO Exception during favorite write, failed creating the file!");
                }
            }
        } else {
            Log.d("WriteFavorite_new", "Received intent, start deleting team="+team+" league="+league);
            try {
                File inputFile = new File(context.getCacheDir().getPath() + "/" + "favorite_new.txt");
                File tempFile = new File(context.getCacheDir().getPath() + "/" + "favorite_new_temp.txt");

                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

                String currentLine;

                while ((currentLine = reader.readLine()) != null) {
                    if (!currentLine.equalsIgnoreCase(league + ",,," + team)) {
                        //writer.write(currentLine + System.getProperty("line.separator"));
                        writer.write(currentLine + "\n");
                    }
                }
                writer.close();
                reader.close();
                boolean successful = tempFile.renameTo(inputFile);

                // TODO: devi spostare questo SET dentro nell'update service,
                // se lo vuoi lanciare ancora dopo la scrittura dei preferiti...
                if (successful) {
                    MainActivity.expListAdapter.setChild_favorite(group, child, false);
                }

                //Intent i = new Intent(context, UpdateIntentService.class);
                //i.setAction("UpdateIntent.FAVORITE_UPDATE");
                //context.startService(i);

                Log.d("WriteFavorite_new", "Rename was " + successful);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("WriteFavorite_new", "IO Exception during favorite write!!");
            }
        }
    }
}
