package com.silikhin.geotask;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;

import java.util.ArrayList;

/**
 * Created by B_Silikhin on 021 21.04.17.
 */

public class LastLocArrayAdapter extends ArrayAdapter {
    private final Context mContext;
    private final int mResource;
    private final ArrayList<Place> lastSearches;

    public LastLocArrayAdapter(@NonNull Context context, @LayoutRes int resource, ArrayList<Place> places) {
        super(context, resource);
        this.mContext = context;
        this.mResource = resource;
        this.lastSearches = places;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView==null)
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);

        TextView mPlaceName = (TextView)convertView.findViewById(R.id.message_title);
        TextView mPlaceDescription = (TextView)convertView.findViewById(R.id.message_subtitle);

        if (lastSearches.size()>0){
            mPlaceName.setText(lastSearches.get(position).getName());
            mPlaceDescription.setText(lastSearches.get(position).getAddress());
        }
        return convertView;
    }
}

