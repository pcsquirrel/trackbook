/**
 * TrackerService.java
 * Implements the app's movement tracker service
 * The TrackerService creates a Track object and displays a notification
 *
 * This file is part of
 * TRACKBOOK - Movement Recorder for Android
 *
 * Copyright (c) 2016 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 *
 * Trackbook uses osmdroid - OpenStreetMap-Tools for Android
 * https://github.com/osmdroid/osmdroid
 */

package org.y20k.trackbook;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.y20k.trackbook.core.Track;
import org.y20k.trackbook.helpers.LocationHelper;
import org.y20k.trackbook.helpers.TrackbookKeys;


/**
 * TrackerService class
 */
public class TrackerService extends Service implements TrackbookKeys {

    /* Define log tag */
    private static final String LOG_TAG = TrackerService.class.getSimpleName();


    /* Main class variables */
    private Track mTrack;
    private CountDownTimer mTimer;
    private LocationManager mLocationManager;
    private LocationListener mGPSListener;
    private LocationListener mNetworkListener;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // checking for empty intent
        if (intent == null) {
            Log.v(LOG_TAG, "Null-Intent received. Stopping self.");
            // remove notification
            stopForeground(true);
            stopSelf();
        }

        // ACTION START
        else if (intent.getAction().equals(ACTION_START)) {
            Log.v(LOG_TAG, "Service received command: START");

            // create a new track
            mTrack = new Track(getApplicationContext());

            // acquire reference to Location Manager
            mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            // listeners that responds to location updates
            mGPSListener = createLocationListener();
            mNetworkListener = createLocationListener();

            // register location listeners and request updates
            try {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mGPSListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            try {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mNetworkListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }

            mTimer = new CountDownTimer(CONSTANT_MAXIMAL_DURATION, CONSTANT_TRACKING_INTERVAL) {
                @Override
                public void onTick(long l) {
                    // TODO
                }

                @Override
                public void onFinish() {
                    // TODO
                }
            };

        }

        // ACTION STOP
        else if (intent.getAction().equals(ACTION_STOP)) {
            // remove listeners
            try {
                mLocationManager.removeUpdates(mGPSListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            try {
                mLocationManager.removeUpdates(mNetworkListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            Log.v(LOG_TAG, "Service received command: STOP");
        }

        // START_STICKY is used for services that are explicitly started and stopped as needed
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        // remove listeners
        try {
            mLocationManager.removeUpdates(mGPSListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        try {
            mLocationManager.removeUpdates(mNetworkListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        Log.v(LOG_TAG, "onDestroy called.");

        // cancel notification
        stopForeground(true);
    }


    /* Creates a location listener */
    private LocationListener createLocationListener() {
        return new LocationListener() {
            public void onLocationChanged(Location location) {

                // get number of tracked WayPoints
                int trackSize = mTrack.getWayPoints().size();

                if (trackSize >= 2) {
                    Location lastWayPoint = mTrack.getWayPointLocation(trackSize-2);
                    if (LocationHelper.isNewWayPoint(lastWayPoint, location)) {
                        // add new location to track
                        mTrack.addWayPoint(location);
                    }
                } else {
                    // add first location to track
                    mTrack.addWayPoint(location);
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO do something
            }

            public void onProviderEnabled(String provider) {
                // TODO do something
            }

            public void onProviderDisabled(String provider) {
                // TODO do something
            }
        };
    }
}