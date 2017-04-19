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



/**
 * A simple {@link Fragment} subclass.
 */
public class TabFragmentFrom extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tab_fragment_from, container, false);

        SupportPlaceAutocompleteFragment autocompleteFragment = new SupportPlaceAutocompleteFragment();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.place_autocomplete_from, autocompleteFragment)
                .commit();

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i("myLogs", "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                Log.i("myLogs", "Error: " + status);
            }
        });

        return view;
    }

}
