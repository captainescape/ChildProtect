package com.example.yinqinghao.childprotect.asyncTask;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;

import java.util.Calendar;

/**
 * Created by yinqinghao on 1/5/17.
 *
 * Get the current location
 */
public class GetLocationTask extends AsyncTask<String, Void, Location> {
    //call back method
    public interface LocationResponse {
        void locationProcessFinish(Location location);
    }
    private GetLocationTask.LocationResponse delegate = null;
    private Context context;
    //lcoation manager
    private static LocationManager locationManager;
    //location
    private static Location lo;
    private static double longitude;
    private static double latitude;
    private static int loopTimes;
    //location listerer
    private static MyLocationListener myLocationListener;

    public GetLocationTask(GetLocationTask.LocationResponse delegate, Activity activity, int loopTimes) {
        this.delegate = delegate;
        this.context = activity;
        this.loopTimes = loopTimes;
        myLocationListener = new MyLocationListener();
    }

    public GetLocationTask(GetLocationTask.LocationResponse delegate, Context context, int loopTimes) {
        this.delegate = delegate;
        this.context = context;
        this.loopTimes = loopTimes;
        myLocationListener = new MyLocationListener();
    }

    @Override
    protected Location doInBackground(String... params) {
        int i = 0;
        if (Looper.myLooper() == null)
            Looper.prepare();
//        while (latitude == 0 && i != 5) {
//            try {
//                //get the current location
//                getLocationInfo(context);
//            } catch (Exception e) {
//                e.printStackTrace();
//                //remove the location listener
//                locationManager.removeUpdates(myLocationListener);
//            }
//            i++;
//        }

        lo =  new Location("");
        lo.setLatitude(-37.8768);
        lo.setLongitude(145.026);
        return lo;
    }

    /**
     * Get the current location
     *
     * @param context
     */
    public static void getLocationInfo(Context context) {
        try {
            longitude = 0.0;
            latitude = 0.0;
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            //Get GPS status
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            //Get the newwork status
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            //if the network is enabled, get the location by network(fingerprinting) first (fast but not accurate)
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
                if (locationManager != null) {
                    Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(networkLocation != null && networkLocation.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
                        longitude = networkLocation.getLongitude();
                        latitude = networkLocation.getLatitude();
                        lo = networkLocation;
                    }
                }
            }
            //get the location by gps (trilateration)
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
                if (locationManager != null) {
                    Location GPSLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    int i = loopTimes;
                    while ((GPSLocation == null
                            || ((GPSLocation != null) &&  GPSLocation.getTime() < Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000))
                            && i >= 0)  {
                        Thread.sleep(500);
                        i--;
                    }

                    if(GPSLocation != null && GPSLocation.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
                        longitude = GPSLocation.getLongitude();
                        latitude = GPSLocation.getLatitude();
                        lo = GPSLocation;
                    }
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * after processing
     *
     * @param location
     */
    @Override
    protected void onPostExecute(Location location) {
//        removeListener();
//        if (location == null) {
//            location = new Location("");
//            location.setLatitude(-37.8768);
//            location.setLongitude(145.026);
//        }
        delegate.locationProcessFinish(location);
        latitude = 0;
    }

    public void removeListener() {
        locationManager.removeUpdates(myLocationListener);
    }

    /**
     * MyLocationListener for location change, and other events
     */
    public static class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            lo = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}