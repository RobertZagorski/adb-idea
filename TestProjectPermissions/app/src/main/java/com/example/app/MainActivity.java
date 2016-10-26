package com.example.app;

import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        Button location;
        Button storage;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            location = (Button) view.findViewById(R.id.location);
            location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Toast.makeText(getActivity(), "Manifest.permission_group.LOCATION is granted", Toast.LENGTH_SHORT).show();
                }
            });
            storage = (Button) view.findViewById(R.id.storage);
            storage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),
                                Uri.parse("content://media/external/images/media/35634"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ContextCompat.getExternalFilesDirs(getActivity(), android.os.Environment.DIRECTORY_MUSIC);
                    Toast.makeText(getActivity(), "Manifest.permission_group.STORAGE is granted", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
