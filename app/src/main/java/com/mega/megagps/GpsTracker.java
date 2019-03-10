package com.mega.megagps;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public class GpsTracker extends Service implements LocationListener{

    private Context mContext = null;

    // flag for GPS status
    private boolean isGPSEnabled = false;

    // flag for network status
    private boolean isNetworkEnabled = false;

    private boolean canGetLocation = false;

    private Location location; // location
    private final double RErth = 6371e3;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 2; // meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1; // sec

    private String locationService = "";

    private double curLat = 0;
    private double curLon = 0;

    private double prevLat = 0;
    private double prevLon = 0;

    private double destLat = 0;
    private double destLon = 0;

    double[] dist_azimuth = new double[2];

    private double motionDirection = 0;
    private double prevMotionDirection = 0;
    private double destDirection = 0;
    private double destDistance = 0;

    // Declaring a Location Manager
    private LocationManager locationManager;

    public GpsTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    public void LatLongToDistAz(
            double lat1,
            double lon1,
            double lat2,
            double lon2,
            double[] dist_azimuth
            ) {
        // Haversine formula
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);
        double sindlat_2 = Math.sin((lat2 - lat1) / 2);
        double sindlon_2 = Math.sin((lon2 - lon1) / 2);

        double a = sindlat_2 * sindlat_2 + Math.cos(lat1) * Math.cos(lat2) * sindlon_2 * sindlon_2;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double bearing = Math.atan2(Math.sin(lon2 - lon1) * Math.cos(lat2),
                Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1) );
        dist_azimuth[0] = RErth * c;
        dist_azimuth[1] = Math.toDegrees(bearing);
    }

    public void ToDistAzimuth(double lat2, double lon2, double[] dist_azimuth) {
        if(this.canGetLocation())
        {
            double lat1 = this.getLatitude();
            double lon1 = this.getLongitude();

            LatLongToDistAz(lat1, lon1, lat2, lon2, dist_azimuth);
        }
    }

    public void getLocation() {
        try {
            this.canGetLocation = false;

            if(locationManager == null) {
                locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
                if (locationManager == null) {
                    throw new Exception("Can't cretae location manager");
                }
            }
            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // getting network status
            ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected())
            {
                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            }
            else
            {
                isNetworkEnabled = false;
            }

            if (!isGPSEnabled && !isNetworkEnabled) {
                location = null;
            }
            else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if(isGPSEnabled ) {
                    if( ActivityCompat.checkSelfPermission(mContext,
                            Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(mContext,
                             Manifest.permission.ACCESS_COARSE_LOCATION) !=
                                    PackageManager.PERMISSION_GRANTED) {
                        // ...
                    }
                    if(location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this, Looper.getMainLooper());
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            onLocationChanged(location);
                            locationService = LocationManager.GPS_PROVIDER;
                        }
                    }
                }

                if(location == null && isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this, Looper.getMainLooper());

                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        onLocationChanged(location);
                        locationService = LocationManager.NETWORK_PROVIDER;
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getLatitude() {
        if(location != null) {
            return location.getLatitude();
        }
        return 0;
    }

    public double getLongitude()  {
        if(location != null) {
            return location.getLongitude();
        }
        return 0;
    }

    public double getAltitude() {
        if(location != null) {
            return location.getAltitude();
        }
        return 0;
    }

    public double getSpeed() {
        if(location != null) {
            return location.getSpeed();
        }
        return 0;
    }

    /**
     * Function to check if best network provider
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int width) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int width) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS() {
        if(locationManager != null) {
            locationManager.removeUpdates(GpsTracker.this);
        }
    }

    public double getMotionDirection() {

        double prev = prevMotionDirection;
        double cur = motionDirection;
        if(prev - cur > 180) {
            prev -= 360;
        }
        else if(cur - prev > 180) {
            cur -= 360;
        }
        prevMotionDirection = (prev * 39 + cur) / 40;
        if(prevMotionDirection < 0) prevMotionDirection += 360;

        return prevMotionDirection;
    }

    public double getDestDirection() {
        return destDirection;
    }

    public double getDestDistance() {
        return destDistance;
    }

    public void setDestination(double destLat, double destLon) {
        this.destLat = destLat;
        this.destLon = destLon;
        updateDest();
    }

    private void updateDest() {
        LatLongToDistAz(prevLat, prevLon, curLat, curLon, dist_azimuth);

        double prev = prevMotionDirection;
        double cur = dist_azimuth[1];
        if(cur < 0) cur += 360;
        if(prev - cur > 180) prev -= 360;
        else if(cur - prev > 180) cur -= 360;

        motionDirection = (prev + cur) / 2;
        if(motionDirection < 0) motionDirection += 360;

        LatLongToDistAz(curLat, curLon, destLat, destLon, dist_azimuth);
        destDirection = dist_azimuth[1];
        if(destDirection < 0) destDirection += 360;

        destDistance = dist_azimuth[0];
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        prevLat = curLat;
        prevLon = curLon;
        this.location = location;

        curLat = this.location.getLatitude();
        curLon = this.location.getLongitude();

        updateDest();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
