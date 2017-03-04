package com.example.myfirstapp_withsplashscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriteFavoriteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d("WriteFavorite", "Received intent, start writing");
        String league = intent.getStringExtra("league");
        String team = intent.getStringExtra("team");

        File cDir = context.getCacheDir();
        File tempFile = new File(cDir.getPath() + "/" + "favorite.txt");
        try {
            FileWriter writer = new FileWriter(tempFile);

            writer.write(league +"\n");
            writer.write(team +"\n");

            // Closing the writer object
            writer.close();
            Log.d("WriteFavorite", "Finished writing");

            Intent i = new Intent(context, UpdateIntentService.class);
            i.setAction("UpdateIntent.FAVORITE_UPDATE");
            context.startService(i);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("WriteFavorite", "Exit writing because of an error");
        }
    }
}
