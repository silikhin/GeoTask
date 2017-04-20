package com.silikhin.geotask;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabFragmentFrom extends Fragment implements OnMapReadyCallback{

    private GoogleMap mMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tab_fragment_from, container, false);

        SupportMapFragment mapFragment = new SupportMapFragment();
        SupportPlaceAutocompleteFragment autocompleteFragment = new SupportPlaceAutocompleteFragment();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.place_autocomplete_from, autocompleteFragment)
                .replace(R.id.mapFrom, mapFragment)
                .commit();

        mapFragment.getMapAsync(this);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (mMap!=null){
                    mMap.clear();

                    LatLng choosenPlaceLatLng = place.getLatLng();
                    String choosenPlaceTitle = place.getName().toString();
                    mMap.addMarker(new MarkerOptions().position(choosenPlaceLatLng).title(choosenPlaceTitle));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(choosenPlaceLatLng));
                }
            }

            @Override
            public void onError(Status status) {
                Log.i("myLogs", "Error: " + status);
            }
        });

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
