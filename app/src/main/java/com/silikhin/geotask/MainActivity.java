package com.silikhin.geotask;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;


public class MainActivity extends AppCompatActivity {

    LatLng targetFrom, targetTo, targetWrong;
    private final double wrongLat = 43.611559;
    private final double wrongLng = 31.501455;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Location from"));
        tabLayout.addTab(tabLayout.newTab().setText("Location to"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        targetWrong = new LatLng(wrongLat, wrongLng);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        double latFrom = Double.longBitsToDouble(sp.getLong("latFrom", Double.doubleToLongBits(wrongLat)));
        double lngFrom = Double.longBitsToDouble(sp.getLong("lngFrom", Double.doubleToLongBits(wrongLng)));
        double latTo = Double.longBitsToDouble(sp.getLong("latTo", Double.doubleToLongBits(wrongLat)));
        double lngTo = Double.longBitsToDouble(sp.getLong("lngTo", Double.doubleToLongBits(wrongLng)));

        targetFrom = new LatLng(latFrom, lngFrom);
        targetTo = new LatLng(latTo, lngTo);
        Bundle coordinates = new Bundle();
        coordinates.putParcelable("latLngFrom", targetFrom);
        coordinates.putParcelable("latLngTo", targetTo);
        if (targetFrom==null||targetFrom.equals(targetWrong)) {
            Toast.makeText(this, "Please, chose location from", Toast.LENGTH_SHORT).show();
        }
        else if (targetTo==null||targetTo.equals(targetWrong)) {
            Toast.makeText(this, "Please, chose location to", Toast.LENGTH_SHORT).show();
        }
        else {
            Intent intent = new Intent(this, ResultMapActivity.class);
            intent.putExtra("bundle", coordinates);
            startActivity(intent);
        }
        return true;
    }
}