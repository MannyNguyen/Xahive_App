package ca.xahive.app.bl.local;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import java.util.Observable;

import ca.xahive.app.bl.utils.Config;
import ca.xahive.app.bl.utils.XADebug;

public class XahLocationListener extends Observable implements LocationListener {
    private boolean isMoscow = Config.IS_IN_MOSCOW;
    private double moscowLat = 55.755786;
    private double moscowLon = 37.617633;
    private Location currentLocation;

    @Override
    public void onLocationChanged(Location location) {
        setCurrentLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        // Nothing to do.
    }

    @Override
    public void onProviderEnabled(String s) {
        // Nothing to do.
    }

    @Override
    public void onProviderDisabled(String s) {
        // Nothing to do.
    }

    public boolean hasLocation() {
        return (getCurrentLocation().getLatitude() != 0.0f && getCurrentLocation().getLongitude() != 0.0f);
    }

    public Location getCurrentLocation() {
        if (isMoscow) {
            XADebug.d(String.format("Caution: In Moscow."));
            Location currLocation = new Location("Moscow");
            currLocation.setLatitude(moscowLat);
            currLocation.setLongitude(moscowLon);
            return currLocation;
        }

        if (currentLocation == null) {
            currentLocation = new Location(UserDefaults.class.toString());
            currentLocation.setLatitude(UserDefaults.getLatitude());
            currentLocation.setLongitude(UserDefaults.getLongitude());

            XADebug.d(String.format("Found saved location: %f %f", currentLocation.getLatitude(), currentLocation.getLongitude()));
        }

        return currentLocation;
    }

    private void setCurrentLocation(Location currentLocation) {
        boolean significant = false;

        if (this.currentLocation == null && currentLocation != null) {
            significant = true;
        }
        else if (this.currentLocation != null && currentLocation != null) {
            float distance = currentLocation.distanceTo(this.currentLocation);
            significant = (distance >= Config.LOCATION_MIN_CHANGE);
        }

        this.currentLocation = currentLocation;

        if (this.currentLocation != null) {
            UserDefaults.setLatitude(this.currentLocation.getLatitude());
            UserDefaults.setLongitude(this.currentLocation.getLongitude());
        }

        if (significant) {
            setChanged();
            notifyObservers();
        }
    }
}
