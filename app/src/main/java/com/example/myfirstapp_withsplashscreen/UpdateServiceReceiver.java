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
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
                intent.getAction().equals("com.example.myfirstapp_withsplashscreen.FIRST_STARTUP")) {
            Log.d("UpdateServiceReceiver", "UpdateServiceReceiver -- Received BOOT broadcast");
            ComponentName serviceComponent = new ComponentName(context, UpdateService.class);
            JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
            //builder.setPeriodic(3600 * 1000); // 1 hour periodicity
            builder.setPeriodic(TimeUnit.MINUTES.toMillis(60));
            builder.setPersisted(true);
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            builder.setRequiresCharging(false); // we don't care if the device is charging or not
            //JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JobScheduler.class);
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(builder.build());
        }
        else if(intent.getAction().equals("com.example.myfirstapp_withsplashscreen.SINGLE_UPDATE")) {
            Log.d("UpdateServiceReceiver", "UpdateServiceReceiver -- Received SINGLE broadcast");
            Intent i = new Intent(context, UpdateIntentService.class);
            i.setAction("UpdateIntent.SINGLE_UPDATE");
            context.startService(i);
        }
    }
}
