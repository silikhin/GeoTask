package com.silikhin.geotask;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabFragmentFrom extends Fragment implements OnMapReadyCallback{

    private GoogleMap mMap;
    private ArrayList<Place> lastSearches;
    FrameLayout layoutLastLoc;
    TextView tvRecentlySearched;
    ListView lvLastLoc;
    LastLocArrayAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tab_fragment_from, container, false);

        lastSearches = new ArrayList<>();
        layoutLastLoc = (FrameLayout)view.findViewById(R.id.layout_last_loc);
        tvRecentlySearched = (TextView) view.findViewById(R.id.tvRecentlySearched);
        lvLastLoc = (ListView)view.findViewById(R.id.lvLastLoc);
        adapter = new LastLocArrayAdapter(getContext(), R.layout.lv_item, lastSearches);
        lvLastLoc.setAdapter(adapter);

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

                    lastSearches.add(0, place);
                    adapter.clear();
                    adapter.addAll(lastSearches);
                    locationAdded();
                    Log.d("myLogs", "ArrayList size= "+ lastSearches.size());
                    Log.d("myLogs", "ListView count = " + lvLastLoc.getCount());


                    LatLng choosenPlaceLatLng = place.getLatLng();
                    String choosenPlaceTitle = place.getName().toString();

                    mMap.addMarker(new MarkerOptions().position(choosenPlaceLatLng).title(choosenPlaceTitle));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(choosenPlaceLatLng));
//                  TODO: add animation and zoom for camera
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

    public void locationAdded(){
        if (lastSearches.size()>1){
            tvRecentlySearched.setVisibility(View.VISIBLE);
            lvLastLoc.setVisibility(View.VISIBLE);
        }
        if (lastSearches.size()>3){
            final float scale = getContext().getResources().getDisplayMetrics().density;
            int pixels = (int) (170 * scale + 0.5f);
            Log.d("myLogs", "int pixels = " + pixels);
            Log.d("myLogs", "itemHeight = " + lvLastLoc.getChildAt(0).getHeight());
            Log.d("myLogs", "set ListViewHeight to " + lvLastLoc.getChildAt(0).getHeight()*3);
            lvLastLoc.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pixels));
            Log.d("myLogs", "ListView height = " + lvLastLoc.getLayoutParams().height);
        }
    }

}
