package com.example.myfirstapp_withsplashscreen;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;

public class UpdateService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {

        Log.v("UpdateServiceAtBoot", "UpdateServiceAtBoot -- onStartJob()");

        Intent i = new Intent(getApplicationContext(), UpdateIntentService.class);
        i.setAction("UpdateIntent.SCHEDULED_UPDATE");
        getApplicationContext().startService(i);

        jobFinished(params, false);

        // From Android guidelines:
        // True if your service needs to process the work (on a separate thread). False if there's no more work to be done for this job.
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // true if we'd like to be rescheduled
        return true;
    }


}
