package com.example.nachum.pap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GoogleMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng Home = new LatLng(33.009952, 35.286205);

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(50, 50, conf);
        Canvas canvas = new Canvas(bmp);

        // paint defines the text color, stroke width and size
        Paint color = new Paint();
        color.setTextSize(35);
        color.setColor(Color.BLACK);

        // modify canvas
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.home_40x40), 0,0, color);

        // add marker to Map
        mMap.addMarker(new MarkerOptions()
                .position(Home)
                .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                // Specifies the anchor to be at a particular point in the marker image.
                .anchor(0.5f, 1));


        //gets the id tag of the button clicked
        int num = Integer.parseInt(getIntent().getStringExtra("num"));
        //sets latlngs of required locations
        LatLng Maalot = new LatLng(33.016807, 35.278426);
        LatLng Shderot = new LatLng(31.526919, 34.596688);
        LatLng KfarVardim = new LatLng(32.996156, 35.269054);
        LatLng TalEl = new LatLng(32.927300, 35.178117);
        LatLng Cholon = new LatLng(32.012236, 34.783869);
        LatLng Ashdod = new LatLng(31.802808, 34.650000);
        LatLng Tfachot = new LatLng(32.869261, 35.421875);
        LatLng Dalton = new LatLng(33.016550, 35.488083);
        LatLng BarYochay = new LatLng(32.997910, 35.448288);
        //sets the camera for each location
        if (num == 0){
            CameraPosition cam = new CameraPosition(Maalot,14, 0, 0);
            //move the camera to the requested location
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));
        } else if (num == 1){
            CameraPosition cam = new CameraPosition(Shderot,14, 0, 0);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));
        } else if (num == 2){
            CameraPosition cam = new CameraPosition(KfarVardim,14, 0, 0);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));
        } else if (num == 3){
            CameraPosition cam = new CameraPosition(TalEl,15, 0, 0);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));
        } else if (num == 4){
            CameraPosition cam = new CameraPosition(Cholon,13, 0, 0);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));
        } else if (num == 5){
            CameraPosition cam = new CameraPosition(Ashdod,13, 0, 0);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));
        } else if (num == 6){
            CameraPosition cam = new CameraPosition(Tfachot,16, 0, 0);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));
        } else if (num == 7){
            CameraPosition cam = new CameraPosition(Dalton,15, 0, 0);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));
        } else if (num == 8){
            CameraPosition cam = new CameraPosition(BarYochay,16, 0, 0);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                CameraPosition cam = new CameraPosition(marker.getPosition(),marker.getZIndex(),0,0);
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));

                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.openDrawer(Gravity.RIGHT);

                return true;
            }
        });


    }
}
