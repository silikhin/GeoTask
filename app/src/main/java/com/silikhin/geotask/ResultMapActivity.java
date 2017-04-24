package com.silikhin.geotask;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ResultMapActivity extends AppCompatActivity implements OnMapReadyCallback,
        Callback<RouteApiResponse>,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private final String TAG = "myLogs";
    private LatLng mLatLngFrom, mLatLngTo, mLatLngLastKnown;
    private String stringLatLngFrom, stringLatLngTo;

    private Location mLastKnownLocation;

    private StartLocation routeStartLocation;
    private ArrayList<EndLocation> routePoints = new ArrayList<>();

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLatLngFrom = getIntent().getBundleExtra("bundle").getParcelable("latLngFrom");
        mLatLngTo = getIntent().getBundleExtra("bundle").getParcelable("latLngTo");
        stringLatLngFrom = mLatLngFrom.latitude + "," + mLatLngFrom.longitude;
        stringLatLngTo = mLatLngTo.latitude + "," + mLatLngTo.longitude;
        Log.d(TAG, "Loc from = " + mLatLngFrom);
        Log.d(TAG, "Loc to = " + mLatLngTo);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
        mGoogleApiClient.connect();

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RouteAPI.ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        RouteAPI routeAPI = retrofit.create(RouteAPI.class);

        Call<RouteApiResponse> call = routeAPI.getRoute(
                stringLatLngFrom,
                stringLatLngTo,
                getResources().getString(R.string.google_maps_key)
        );
        Log.d(TAG, call.request().toString());
        call.enqueue(this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapResult);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Refer to the reference doc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        Log.d(TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Play services connection suspended");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        UiSettings mUISettings = mMap.getUiSettings();
        mUISettings.setZoomControlsEnabled(true);

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        // Adding markers with locations
        addMarkersAndRoute();
    }

    private void getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        // A step later in the tutorial adds the code to get the device location.
        if (mLocationPermissionGranted) {
            mLastKnownLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        }

        // Set the map's camera position to the current location of the device.
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLastKnownLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), 14.0f));
        } else {
            Log.d(TAG, "Current location is null.");
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    public void updateLocationUI(){
        if (mMap == null) {
            return;
        }

    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mLastKnownLocation = null;
        }
    }

    public void addMarkersAndRoute(){
        if (mMap == null) {
            return;
        }

        if (mLastKnownLocation!=null) {
            mLatLngLastKnown = new LatLng(mLastKnownLocation.getLatitude(),
                    mLastKnownLocation.getLongitude());
            mCameraPosition = new CameraPosition.Builder().target(mLatLngLastKnown).zoom(13.0f).build();
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        if (mLastKnownLocation!=null)
            builder.include(mLatLngLastKnown);
        builder.include(mLatLngFrom);
        builder.include(mLatLngTo);

        LatLngBounds bounds = builder.build();

        PolylineOptions polylineOptions = new PolylineOptions();
        if (routeStartLocation!=null&routePoints!=null){
            polylineOptions.add(new LatLng(routeStartLocation.getLat(),routeStartLocation.getLng()));
            for (EndLocation point:routePoints) {
                polylineOptions.add(new LatLng(point.getLat(),point.getLng()));
            }
        }

        polylineOptions.width(5);
        polylineOptions.color(Color.BLACK);
        Polyline polyline = mMap.addPolyline(polylineOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
        mMap.animateCamera(cameraUpdate);
    }

    @Override
    public void onResponse(Call<RouteApiResponse> call, Response<RouteApiResponse> response) {
        RouteApiResponse resp = response.body();
        String status = resp.getStatus();
        if (!status.equals("OK")) {
            Log.e(TAG, status);
            Log.e(TAG, resp.getErrorMessage());
        }
        else {
            Log.d(TAG, "It work! Status = " + status);
            List<Route> routes = resp.getRoutes();
            Log.d(TAG, "routes.size = " + routes.size());
            Route bestRoute = routes.get(0);
            List<Leg> legs = bestRoute.getLegs();
            Log.d(TAG, "legs.size = " + legs.size());
            Leg leg = legs.get(0);
            List<Step> steps = leg.getSteps();
            Log.d(TAG, "steps.size = " + steps.size());
            routeStartLocation = steps.get(0).getStartLocation();
            Log.d(TAG, "StartLocationLatLng = " + routeStartLocation.getLat()+","+routeStartLocation.getLng());
            for (Step step : steps) {
                routePoints.add(step.getEndLocation());
                Log.d(TAG, "RoutePointLatLng = " + step.getEndLocation().getLat()+","+step.getEndLocation().getLng());
            }
        }
    }

    @Override
    public void onFailure(Call<RouteApiResponse> call, Throwable t) {
        Log.e(TAG, t.getMessage());
    }
}
