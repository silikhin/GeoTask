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
import com.google.android.gms.maps.model.MarkerOptions;
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

    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private Location mLastKnownLocation;
    private LatLng mLatLngFrom, mLatLngTo, mLatLngLastKnown;
    private String mStringLatLngFrom, mStringLatLngTo;

    private StartLocation mRouteStartLocation;
    private ArrayList<EndLocation> mRoutePoints = new ArrayList<>();

    private final String TAG = "myLogs";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLatLngFrom = getIntent().getBundleExtra("bundle").getParcelable("latLngFrom");
        mLatLngTo = getIntent().getBundleExtra("bundle").getParcelable("latLngTo");
        mStringLatLngFrom = mLatLngFrom.latitude + "," + mLatLngFrom.longitude;
        mStringLatLngTo = mLatLngTo.latitude + "," + mLatLngTo.longitude;

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
                mStringLatLngFrom,
                mStringLatLngTo,
                getResources().getString(R.string.google_maps_key)
        );
        call.enqueue(this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        SupportMapFragment mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapResult);
        mMapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Refer to the reference doc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        Log.e(TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Play services connection suspended");
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

    @Override
    public void onResponse(Call<RouteApiResponse> call, Response<RouteApiResponse> response) {
        RouteApiResponse resp = response.body();
        String status = resp.getStatus();
        if (!status.equals("OK")) {
            Log.e(TAG, status);
            Log.e(TAG, resp.getErrorMessage());
        }
        else {
            List<Route> routes = resp.getRoutes();
            Route bestRoute = routes.get(0);
            List<Leg> legs = bestRoute.getLegs();
            Leg leg = legs.get(0);
            List<Step> steps = leg.getSteps();
            mRouteStartLocation = steps.get(0).getStartLocation();
            for (Step step : steps) {
                mRoutePoints.add(step.getEndLocation());
            }
        }
    }

    @Override
    public void onFailure(Call<RouteApiResponse> call, Throwable t) {
        Log.e(TAG, t.getMessage());
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

    private void updateLocationUI(){
        if (mMap == null) {
            return;
        }

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

    private void addMarkersAndRoute(){
        if (mMap == null) {
            return;
        }

        mMap.clear();

        if (mLastKnownLocation!=null) {
            mLatLngLastKnown = new LatLng(mLastKnownLocation.getLatitude(),
                    mLastKnownLocation.getLongitude());
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        if (mLastKnownLocation!=null) {
            builder.include(mLatLngLastKnown);
            mMap.addMarker(new MarkerOptions().position(mLatLngLastKnown).title("Current location"));
        }
        builder.include(mLatLngFrom);
        builder.include(mLatLngTo);

        LatLngBounds bounds = builder.build();

        mMap.addMarker(new MarkerOptions().position(mLatLngFrom).title("Position From"));
        mMap.addMarker(new MarkerOptions().position(mLatLngTo).title("Position To"));

        PolylineOptions polylineOptions = new PolylineOptions();
        if (mRouteStartLocation !=null& mRoutePoints !=null){
            polylineOptions.add(new LatLng(mRouteStartLocation.getLat(), mRouteStartLocation.getLng()));
            for (EndLocation point: mRoutePoints) {
                polylineOptions.add(new LatLng(point.getLat(),point.getLng()));
            }
        }

        polylineOptions.width(10);
        polylineOptions.color(Color.RED);
        Polyline polyline = mMap.addPolyline(polylineOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
        mMap.animateCamera(cameraUpdate);
    }

}
