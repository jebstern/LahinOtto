package com.jebstern.lahinotto;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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
    private final static int REQUEST_GOOGLE_PLAY_SERVICES = 1000;
    private static final int REQUEST_MYLOCATION = 0;


    public MarkerBean mClickedClusterMarkerBean;
    ClusterManager<MarkerBean> mClusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Obtaining SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // The callback method 'onMapReady(GoogleMap googleMap)' provides a GoogleMap instance guaranteed to be non-null and ready to be used.
        // If Google Play services is not installed on the user's device, the callback WILL NOT be triggered until the user installs Play services.
        mapFragment.getMapAsync(this);


        // First we need to check availability of Google Play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
        }

        new MarkerSetupTask().execute();  // Read csv file with AsyncTask: it allows us to perform background operations and publish results directly on the UI thread
    }

    //  Creating a Google API Client object
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestMyLocation();
        } else {
            mMap.setMyLocationEnabled(true);
        }


        mClusterManager = new ClusterManager<>(getApplicationContext(), mMap);
        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());

        mMap.setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MarkerBean>() {
            @Override
            public boolean onClusterItemClick(MarkerBean item) {
                mClickedClusterMarkerBean = item;
                return false;
            }
        });

    }


    private void requestMyLocation() {
        Log.i(TAG, "MyLocation permission has NOT been granted. Requesting permission.");

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG, "Displaying MyLocation permission rationale to provide additional context.");

            AlertDialog.Builder alert = new AlertDialog.Builder(getApplicationContext());
            alert.setTitle("Permission request");
            alert.setMessage("If you want to have your location shown, please accept the permissions.");

            alert.setPositiveButton("ACCEPT", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_MYLOCATION);
                }
            });

            alert.setNegativeButton("DECLINE", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            });

            alert.show();

        } else {
            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_MYLOCATION);
        }
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
            for (MarkerBean ottoATM : markers) {
                mClusterManager.addItem(ottoATM);
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

            tvLocation.setText(mClickedClusterMarkerBean.getLocationPlace());
            tvAddress.setText(getResources().getString(R.string.infowindow_tv_address) + mClickedClusterMarkerBean.getAddress());
            tvOpeningHours.setText(getResources().getString(R.string.infowindow_tv_opening_hours) + mClickedClusterMarkerBean.getOpeningHours());

            if (mClickedClusterMarkerBean.getModel().equalsIgnoreCase("EP")) {
                tvAccessibility.setText(getResources().getString(R.string.infowindow_tv_access_ep));
            } else if (mClickedClusterMarkerBean.getModel().equalsIgnoreCase("P")) {
                tvAccessibility.setText(getResources().getString(R.string.infowindow_tv_access_p));
            } else if (mClickedClusterMarkerBean.getModel().equalsIgnoreCase("T")) {
                tvAccessibility.setText(getResources().getString(R.string.infowindow_tv_access_t));
            }

            return myContentsView;
        }
    }


    //  Method to display the location on UI
    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            LatLng userPosition = new LatLng(latitude, longitude);  // Get the position of the user
            float zoom = mMap.getCameraPosition().zoom; // Get the zoom level that the user is using.

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(userPosition, zoom);
            mMap.animateCamera(cameraUpdate);

        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_message_location), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }


    //  Method to verify google play services on the device. This is the new version: GoogleApiAvailability, not deprectaed GooglePlayServicesUtil
    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result, REQUEST_GOOGLE_PLAY_SERVICES).show();
            }
            return false;
        }
        return true;
    }

    // Starting the location updates
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    //  Stopping location updates
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location and star location updates
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
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
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
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        // Stopping the periodic location updates
        stopLocationUpdates();
    }

}
