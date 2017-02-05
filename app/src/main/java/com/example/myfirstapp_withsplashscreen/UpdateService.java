package com.example.myfirstapp_withsplashscreen;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;

public class UpdateService extends JobService {

    //private UpdateAsyncTask mUpdateAsyncTask = null;

    @Override
    public boolean onStartJob(final JobParameters params) {

        Log.v("UpdateServiceAtBoot", "UpdateServiceAtBoot -- onStartJob()");

        Intent i = new Intent(getApplicationContext(), UpdateIntentService.class);
        i.setAction("UpdateIntent.SCHEDULED_UPDATE");
        getApplicationContext().startService(i);
        jobFinished(params, false);
/*
        mUpdateAsyncTask = new UpdateAsyncTask(this) {
            @Override
            protected void onPostExecute(Boolean success) {
                Log.v("UpdateServiceAtBoot", "UpdateServiceAtBoot -- calling jobFinished");
                jobFinished(params, false);
            }
        };
        mUpdateAsyncTask.execute();
*/
        // From Android guidelines:
        // True if your service needs to process the work (on a separate thread). False if there's no more work to be done for this job.
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {

        Log.v("UpdateServiceAtBoot", "UpdateServiceAtBoot -- onStopJob()");
        /*
        // true if we'd like to be rescheduled
        if (mUpdateAsyncTask != null) {
            mUpdateAsyncTask.cancel(true);
        }
        */
        return true;
    }


}
