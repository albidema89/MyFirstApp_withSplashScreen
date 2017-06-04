package com.example.myfirstapp_withsplashscreen;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
//import android.icu.util.Calendar;
import java.util.Calendar;

import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.Date;

import static android.content.Context.ACCOUNT_SERVICE;

public class TestCalendarReceiver extends BroadcastReceiver {

    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d("TestCalendar", "Received intent, start testing");

        AccountManager manager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        Account[] list = manager.getAccounts();
        String gmail = null;

        for (Account account : list) {
            Log.d("TestCalendar", "Looking into account: " +account);
            if (account.type.equalsIgnoreCase("com.google")) {
                gmail = account.name;
                Log.d("TestCalendar", "Found account: " +gmail);
                break;
            }
        }

        // Run query
        Cursor cur = null;
        ContentResolver cr = context.getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
        String[] selectionArgs = new String[] {gmail, "com.google", gmail};

        // Submit the query and get a Cursor object back.
        cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);



        //Log.d("TestCalendar", "calendar name is: " +EVENT_PROJECTION(PROJECTION_DISPLAY_NAME_INDEX));
        long calID = 0;
        // Use the cursor to step through the returned records
        while (cur.moveToNext()) {

            String displayName = null;
            String accountName = null;
            String ownerName = null;

            // Get the field values
            calID = cur.getLong(PROJECTION_ID_INDEX);
            Log.d("TestCalendar", "calID is: " +calID);
            displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
            Log.d("TestCalendar", "displayName is: " +displayName);
            accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
            Log.d("TestCalendar", "accountName is: " +accountName);
            ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);
            Log.d("TestCalendar", "ownerName is: " +ownerName);

            // Do something with the values...
            String[] projection2 = new String[] { CalendarContract.Events.CALENDAR_ID, CalendarContract.Events.TITLE, CalendarContract.Events.CALENDAR_DISPLAY_NAME, CalendarContract.Events.DESCRIPTION, CalendarContract.Events._ID, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.ALL_DAY, CalendarContract.Events.EVENT_LOCATION };

            // 0 = January, 1 = February, ...
            Calendar startTime = Calendar.getInstance();
            startTime.set(2017,00,01,00,00);
            //Calendar endTime= Calendar.getInstance();
            //endTime.set(2015,00,01,00,00);
            // the range is all data from 2017

            //String selection2 = "(" + CalendarContract.Events.TITLE + " REGEXP ?)";
            String selection2 = "(( " + CalendarContract.Events.DTSTART + " >= " + startTime.getTimeInMillis() +" ) AND ( "
                    +CalendarContract.Events.CALENDAR_DISPLAY_NAME + " LIKE ? ))";
            String[] selectionArgs2 = new String[] {"%Cassolese%"};
            Cursor cursor = context.getContentResolver().query( CalendarContract.Events.CONTENT_URI, projection2, selection2, selectionArgs2, null );

            // output the events

            if (cursor.moveToFirst()) {
                do {
                    Log.d("TestCalendar", "Title: " + cursor.getString(1) + " Description: " + cursor.getString(3) + " Calendar: " + cursor.getString(2));
                    //ContentResolver cr = context.getContentResolver();
                    ContentValues values = new ContentValues();
                    Uri updateUri = null;
                    // The new title for the event
                    //values.put(CalendarContract.Events.DESCRIPTION, cursor.getString(3) +"x ");
                    values.put(CalendarContract.Events.DESCRIPTION, "");
                    updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, cursor.getLong(4));
                    int rows = context.getContentResolver().update(updateUri, values, null, null);
                    Log.i("TestCalendar", "Rows updated: " + rows);

                    Log.d("TestCalendar", "Title: " + cursor.getString(1) + " Description: " + cursor.getString(3) + " Calendar: " + cursor.getString(2));

                    //Toast.makeText( this.getApplicationContext(), "Title: " + cursor.getString(1) + " Start-Time: " + (new Date(cursor.getLong(3))).toString(), Toast.LENGTH_LONG ).show();
                } while ( cursor.moveToNext());
            }
            Log.d("TestCalendar", "Finish");

            cursor.close();

        }

        cur.close();
/*
        // NOTE: months starts from 0!
        long startMillis = 0;
        long endMillis = 0;
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(2017, 4, 28, 17, 30);
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(2017, 4, 28, 18, 45);
        endMillis = endTime.getTimeInMillis();
        Log.d("TestCalendar", "Trying to add event ...");
        //ContentResolver cr2 = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis); Log.d("TestCalendar", "Data inizio " +startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis); Log.d("TestCalendar", "Data fine " +endMillis);
        values.put(CalendarContract.Events.TITLE, "Titolo"); Log.d("TestCalendar", "Titolo " +values.get(CalendarContract.Events.TITLE));
        values.put(CalendarContract.Events.DESCRIPTION, "Descrizione"); Log.d("TestCalendar", "Descrizione " +values.get(CalendarContract.Events.DESCRIPTION));
        values.put(CalendarContract.Events.CALENDAR_ID, calID); Log.d("TestCalendar", "CalendarID " +calID);
        //values.put(CalendarContract.Events.CALENDAR_DISPLAY_NAME, "Nome_calendario");
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Roma");
        Uri uri_temp = CalendarContract.Events.CONTENT_URI;
        uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
        long eventID = Long.parseLong(uri.getLastPathSegment());*/
/*
        Uri deleteUri = null;
        deleteUri = ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, 10);
        int rows = context.getContentResolver().delete(deleteUri, null, null);
        Log.i("TestCalendar", "Rows deleted: " + rows);


        //ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();

        values.put(CalendarContract.Calendars.ACCOUNT_NAME, gmail);
        //values.put(CalendarContract.Calendars.ACCOUNT_TYPE, "com.google");
        values.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        values.put(CalendarContract.Calendars.NAME, "Name");
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "Calendar_display_name");
        values.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        values.put(CalendarContract.Calendars.VISIBLE, 1);
        values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        values.put(CalendarContract.Calendars.OWNER_ACCOUNT, gmail);
        values.put(CalendarContract.Calendars.DIRTY, 1);
        values.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, "Europe/Roma");

        Uri calUri = CalendarContract.Calendars.CONTENT_URI;

        calUri = calUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, gmail)
                //.appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, "com.google")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
                .build();

        Uri result = cr.insert(calUri, values);

        long new_calID = Long.parseLong(result.getLastPathSegment());

        // NOTE: months starts from 0!
        long startMillis = 0;
        long endMillis = 0;
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(2017, 4, 28, 17, 30);
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(2017, 4, 28, 18, 45);
        endMillis = endTime.getTimeInMillis();
        Log.d("TestCalendar", "Trying to add event ...");
        //ContentResolver cr2 = context.getContentResolver();
        ContentValues values2 = new ContentValues();
        values2.put(CalendarContract.Events.DTSTART, startMillis); Log.d("TestCalendar", "Data inizio " +startMillis);
        values2.put(CalendarContract.Events.DTEND, endMillis); Log.d("TestCalendar", "Data fine " +endMillis);
        values2.put(CalendarContract.Events.TITLE, "Titolo"); Log.d("TestCalendar", "Titolo " +values2.get(CalendarContract.Events.TITLE));
        values2.put(CalendarContract.Events.DESCRIPTION, "Descrizione"); Log.d("TestCalendar", "Descrizione " +values2.get(CalendarContract.Events.DESCRIPTION));
        values2.put(CalendarContract.Events.CALENDAR_ID, new_calID); Log.d("TestCalendar", "CalendarID " +new_calID);
        //values.put(CalendarContract.Events.CALENDAR_DISPLAY_NAME, "Nome_calendario");
        values2.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Roma");
        Uri uri2 = cr.insert(CalendarContract.Events.CONTENT_URI, values2);
        long eventID = Long.parseLong(uri2.getLastPathSegment());

        // Run query
        cr = context.getContentResolver();
        uri = CalendarContract.Calendars.CONTENT_URI;
        selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
        selectionArgs = new String[] {gmail, "com.google", gmail};

        // Submit the query and get a Cursor object back.
        cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

        //Log.d("TestCalendar", "calendar name is: " +EVENT_PROJECTION(PROJECTION_DISPLAY_NAME_INDEX));
        calID = 0;
        // Use the cursor to step through the returned records
        while (cur.moveToNext()) {

            String displayName = null;
            String accountName = null;
            String ownerName = null;

            // Get the field values
            calID = cur.getLong(PROJECTION_ID_INDEX);
            Log.d("TestCalendar", "calID is: " +calID);
            displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
            Log.d("TestCalendar", "displayName is: " +displayName);
            accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
            Log.d("TestCalendar", "accountName is: " +accountName);
            ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);
            Log.d("TestCalendar", "ownerName is: " +ownerName);

        }*/
    }
}
