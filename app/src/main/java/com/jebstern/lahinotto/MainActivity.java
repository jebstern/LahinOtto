package com.jebstern.lahinotto;


import android.annotation.SuppressLint;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, ClusterManager.OnClusterItemInfoWindowClickListener<MarkerBean> {

    private GoogleMap mMap;

    private static final String TAG = MainActivity.class.getSimpleName();

    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private boolean mRequestingLocationUpdates = true;
    private LocationRequest mLocationRequest;

    // For the sake of clarity, these local variables are displayed here
    private static int UPDATE_INTERVAL = 10000; // 10 seconds
    private static int FATEST_INTERVAL = 5000; // 5 seconds
    private static int DISPLACEMENT = 10; // 10 meters
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private MarkerBean clickedClusterItem;
    ClusterManager<MarkerBean> mClusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map); // Obtaining SupportMapFragment
        mapFragment.getMapAsync(this); // The callback method 'onMapReady(GoogleMap googleMap)' provides a GoogleMap instance guaranteed to be non-null and ready to be used.


        // First we need to check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
        }

        new MarkerSetupTask().execute();  // Read csv file with AsyncTask: it allows us to perform background operations and publish results directly on the UI thread
    }

    @Override
    public void onClusterItemInfoWindowClick(MarkerBean markerBean) {
        // This method is not useful for the user at the moment
    }


    private class MarkerSetupTask extends AsyncTask<Void, Void, Void> {

        private List<MarkerBean> markers;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Read a csv file from assets folder, second constructor parameter contains the file name of the csv we want to read
            MarkerSetup setup = new MarkerSetup(getApplicationContext(), "ottokoordinaatit2015_10_23_fi.csv");
            markers = setup.readCsv();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mMap.setOnCameraChangeListener(mClusterManager);
            mMap.setOnMarkerClickListener(mClusterManager);
            for (MarkerBean ottoAutomaatti : markers) {
                mClusterManager.addItem(ottoAutomaatti);
            }
            mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(new MyCustomAdapterForItems());
        }
    }


    public class MyCustomAdapterForItems implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        @SuppressLint("InflateParams")
        public MyCustomAdapterForItems() {
            myContentsView = getLayoutInflater().inflate(R.layout.info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getInfoContents(Marker marker) {
            TextView tvLocation = ((TextView) myContentsView.findViewById(R.id.tv_location));
            TextView tvAddress = ((TextView) myContentsView.findViewById(R.id.tv_address));
            TextView tvOpeningHours = ((TextView) myContentsView.findViewById(R.id.tv_opening_hours));
            TextView tvAccessibility = ((TextView) myContentsView.findViewById(R.id.tv_accessibility));

            tvLocation.setText(clickedClusterItem.getSijaintipaikka());
            tvAddress.setText(getResources().getString(R.string.infowindow_tv_address) + clickedClusterItem.getOsoite());
            tvOpeningHours.setText(getResources().getString(R.string.infowindow_tv_opening_hours) + clickedClusterItem.getAukioloaika());

            if (clickedClusterItem.getMalli().equalsIgnoreCase("EP")) {
                tvAccessibility.setText(getResources().getString(R.string.infowindow_tv_access_ep));
            } else if (clickedClusterItem.getMalli().equalsIgnoreCase("P")) {
                tvAccessibility.setText(getResources().getString(R.string.infowindow_tv_access_p));
            } else if (clickedClusterItem.getMalli().equalsIgnoreCase("T")) {
                tvAccessibility.setText(getResources().getString(R.string.infowindow_tv_access_t));
            }

            return myContentsView;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    //  Method to display the location on UI
    private void displayLocation() {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            LatLng userPosition = new LatLng(latitude, longitude);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(userPosition, 15);
            mMap.animateCamera(cameraUpdate);
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_message_location), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;
        displayLocation();
    }


    //  Creating google api client object
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    //  Creating location request object
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    //  Method to verify google play services on the device
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_message_playservices), Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    // Starting the location updates
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    //  Stopping location updates
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    //  Google api callback methods
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        displayLocation();
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setMyLocationEnabled(true);


        mClusterManager = new ClusterManager<>(getApplicationContext(), mMap);
        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());

        mMap.setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MarkerBean>() {
            @Override
            public boolean onClusterItemClick(MarkerBean item) {
                clickedClusterItem = item;
                return false;
            }
        });

    }
}
