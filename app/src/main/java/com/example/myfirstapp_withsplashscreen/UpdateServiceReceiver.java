package com.example.myfirstapp_withsplashscreen;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class UpdateServiceReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.v("UpdateServiceReceiver", "UpdateServiceReceiver -- Received BOOT broadcast");
            ComponentName serviceComponent = new ComponentName(context, UpdateService.class);
            JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
            //builder.setPeriodic(3600 * 1000); // 1 hour periodicity
            builder.setPeriodic(TimeUnit.MINUTES.toMillis(30));
            //builder.setPeriodic(300 * 1000); // 5 minutes periodicity
            builder.setRequiresCharging(false); // we don't care if the device is charging or not
            JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
            jobScheduler.schedule(builder.build());
        }
        else if(intent.getAction().equals("com.example.myfirstapp_withsplashscreen.SINGLE_UPDATE")) {
            Log.v("UpdateServiceReceiver", "UpdateServiceReceiver -- Received SINGLE broadcast");
            Intent i = new Intent(context, UpdateIntentService.class);
            i.setAction("UpdateIntent.SINGLE_UPDATE");
            context.startService(i);
        }
    }
}
