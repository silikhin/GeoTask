package com.silikhin.geotask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


public class TabFragmentTo extends Fragment implements OnMapReadyCallback{

    private LatLng choosenPlaceToLatLng;
    private GoogleMap mMap;
    private ArrayList<Place> lastSearches;
    private TextView tvRecentlySearched;
    private ListView lvLastLoc;
    private LastLocArrayAdapter adapter;
    private SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tab_fragment_to, container, false);

        editor = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
        editor.clear().commit();

        lastSearches = new ArrayList<>();
        tvRecentlySearched = (TextView) view.findViewById(R.id.tvRecentlySearchedTo);
        lvLastLoc = (ListView)view.findViewById(R.id.lvLastLocTo);
        adapter = new LastLocArrayAdapter(getContext(), R.layout.lv_item, lastSearches);
        lvLastLoc.setAdapter(adapter);

        SupportMapFragment mapFragment = new SupportMapFragment();
        final SupportPlaceAutocompleteFragment autocompleteFragment = new SupportPlaceAutocompleteFragment();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.place_autocomplete_to, autocompleteFragment)
                .replace(R.id.mapTo, mapFragment)
                .commit();

        mapFragment.getMapAsync(this);

        lvLastLoc.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Place clickedPlace = lastSearches.get(i);
                autocompleteFragment.setText(clickedPlace.getName());
                placeSelected(clickedPlace);
            }
        });

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                placeSelected(place);
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

        UiSettings mUISettings = mMap.getUiSettings();
        mUISettings.setZoomControlsEnabled(true);
    }

    public void drawRecentSearches(){
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

    public void placeSelected(Place place) {
        if (mMap != null) {
            mMap.clear();

            for (int i = 0; i < lastSearches.size(); i++) {
                if (lastSearches.get(i).equals(place))
                    lastSearches.remove(i);
            }
            lastSearches.add(0, place);
            adapter.clear();
            adapter.addAll(lastSearches);
            drawRecentSearches();

            choosenPlaceToLatLng = place.getLatLng();
            String choosenPlaceTitle = place.getName().toString();
            saveLatLngTo();

            mMap.addMarker(new MarkerOptions().position(choosenPlaceToLatLng).title(choosenPlaceTitle));

            CameraPosition cameraPosition = new CameraPosition.Builder().target(choosenPlaceToLatLng).zoom(13.0f).build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            mMap.animateCamera(cameraUpdate);
        }
    }

    private void saveLatLngTo(){
        editor.putLong("latTo", Double.doubleToLongBits(choosenPlaceToLatLng.latitude));
        editor.putLong("lngTo", Double.doubleToLongBits(choosenPlaceToLatLng.longitude));
        editor.commit();
    }
}
