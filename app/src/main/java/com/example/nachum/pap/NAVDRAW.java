package com.example.nachum.pap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.hardware.Camera;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;

import android.util.Log;
import android.view.Gravity;
import android.view.View;

import android.view.MenuItem;

import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Tag;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NAVDRAW extends AppCompatActivity
        implements OnMapReadyCallback {
    private static final String[] INITIAL_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    };

    // the value for the request to allow fine location
    private static final int INITIAL_REQUEST=1337;
    private GoogleMap mMap;
    private Activity activity = this;

    private DatabaseReference mDatabase;
    private StorageReference storageRef;

    private ListView list;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);

        //sets which xml to use for the activity
        setContentView(R.layout.content_navdraw);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // locks drawer from opening from swipes
        DrawerLayout navDrawer = findViewById(R.id.drawer_layout);
        navDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        //setting the spinner element
        Spinner spinner =  findViewById(R.id.currentStatusEdit);
        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("לא עונים");
        categories.add("לא מעוניינים");
        categories.add("מעוניינים");
        categories.add("קשר המשך");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        arrayList = new ArrayList<>();

        // Adapter: You need three parameters 'the context, id of the layout (it will be where the data is shown),
        // and the array that contains the data
        adapter = new ArrayAdapter<String>(
                getApplicationContext(), android.R.layout.simple_list_item_1,  arrayList);

        // Here, you set the data in your ListView
        list = findViewById(R.id.houseList);
        list.setAdapter(adapter);

        mapFragment.getMapAsync(this);
    }

    //constructs basic vars for the entire run
    HashMap<String, Object> houseData;
    private List<String> reportNameList = new ArrayList<>();
    private List<Report> reportList = new ArrayList<>();
    private static final int CAMERA_PIC_REQUEST = 1337;
    byte[] byteArray = new byte[0];
    final House house = new House();
    final Building building = new Building();
    Boolean isDB = false;
    Boolean isHouseUploaded = false;
    Boolean isBuildingUploaded = false;
    Boolean isInBuilding = false;
    Boolean pictureTaken = false;
    Boolean isFABOpen = false;
    View.OnClickListener house_onclick = null;
    View.OnClickListener building_onclick = null;
    View.OnClickListener add_report_onClick = null;

    @Override
    public void onMapReady(GoogleMap googleMap) {

        final DrawerLayout navDrawer = findViewById(R.id.drawer_layout);
        final ImageView pic = findViewById(R.id.pic);

        //common house titles
        final TextView status = findViewById(R.id.status);
        final TextView activists = findViewById(R.id.activists);
        final TextView lastReportTitle = findViewById(R.id.lastReportTitle);
        final TextView reportActivistsTitle = findViewById(R.id.reportActivistsTitle);
        final TextView reportDateTitle = findViewById(R.id.reportDateTitle);

        //houseViewNavdraw
        final TextView familyName = findViewById(R.id.familyName);
        final TextView address = findViewById(R.id.address);
        final TextView description = findViewById(R.id.description);
        final TextView currentStatus = findViewById(R.id.currentStatus);
        final TextView currentActivists = findViewById(R.id.currentActivists);
        final TextView lastReport = findViewById(R.id.lastReport);
        final TextView reportActivists = findViewById(R.id.reportActivists);
        final TextView reportDate = findViewById(R.id.reportDate);
        final Button submit_edit = findViewById(R.id.submit_edit);

        //houseEditNavdraw
        final EditText familyNameEdit = findViewById(R.id.familyNameEdit);
        final EditText addressEdit = findViewById(R.id.addressEdit);
        final EditText descriptionEdit = findViewById(R.id.descriptionEdit);
        final Spinner currentStatusEdit = findViewById(R.id.currentStatusEdit);
        final EditText currentActivistsEdit = findViewById(R.id.currentActivistsEdit);
        final Button submit_house = findViewById(R.id.submit_house);
        final Button add_report = findViewById(R.id.add_report);

        //reportEditNavdraw
        final EditText reportActivistsEdit = findViewById(R.id.reportActivistsEdit);
        final EditText reportEdit = findViewById(R.id.reportEdit);
        final EditText reportDateEdit = findViewById(R.id.reportDateEdit);
        final Button submit_report = findViewById(R.id.submit_report);

        //buildingViewNavdraw
        final Button add_house = findViewById(R.id.add_house);
        final ListView houseList = findViewById(R.id.houseList);
        final Button submit_building = findViewById(R.id.submit_building);

        FloatingActionButton fab_main = findViewById(R.id.floating_action_button_main);
        FloatingActionButton fab_house = findViewById(R.id.floating_action_button_house);
        FloatingActionButton fab_Building = findViewById(R.id.floating_action_button_building);

        //set google map to be both sat and roadmap view
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setMyLocationEnabled(true);

        LatLng MaalotHomeLatLng = new LatLng(33.009952, 35.286205);

        //hides fabs
        fab_house.hide();
        fab_Building.hide();

        //defines the main fab onclick
        fab_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });

        //set the picture to be clickable to add a photo
        pic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
            }
        });

        //gets the location number from the button tags on the mainmap activity
        int num = Integer.parseInt(getIntent().getStringExtra("num"));
        String location = getIntent().getStringExtra("location");
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

        //sets the location and sets the camera for each location
        if (num == 0){
            location = "Maalot";
            CameraPosition cam = new CameraPosition(Maalot,14, 0, 0);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));
        } else if (num == 1){
            location = "Shderot";
            CameraPosition cam = new CameraPosition(Shderot,14, 0, 0);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));
        } else if (num == 2){
            location = "KfarVardim";
            CameraPosition cam = new CameraPosition(KfarVardim,14, 0, 0);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));
        } else if (num == 3){
            location = "TalEl";
            CameraPosition cam = new CameraPosition(TalEl,15, 0, 0);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));
        } else if (num == 4){
            location = "Cholon";
            CameraPosition cam = new CameraPosition(Cholon,13, 0, 0);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));
        } else if (num == 5){
            location = "Ashdod";
            CameraPosition cam = new CameraPosition(Ashdod,13, 0, 0);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));
        } else if (num == 6){
            location = "Tfachot";
            CameraPosition cam = new CameraPosition(Tfachot,16, 0, 0);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));
        } else if (num == 7){
            location = "Dalton";
            CameraPosition cam = new CameraPosition(Dalton,15, 0, 0);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));
        } else if (num == 8){
            location = "BarYochay";
            CameraPosition cam = new CameraPosition(BarYochay,16, 0, 0);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));
        }

        //defines the location - specific db
        DatabaseReference localDB = FirebaseDatabase.getInstance().getReference().child("houses").child(location);

        //loads the data of houses on the map on start and updates realtime
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //build markers according to db
                HashMap<String, Object> dataMap = (HashMap<String, Object>) dataSnapshot.getValue();
                //negates the potential null pointer exception
                if (dataMap != null){
                    //iterates through all the houses gotten
                    for (String key : dataMap.keySet()) {

                        Object data = dataMap.get(key);

                        if (!key.startsWith("B") && !key.startsWith("A")){
                            //normal house
                            HashMap<String, Object> houseData = (HashMap<String, Object>) data;

                            double lat = Double.parseDouble(houseData.get("lat").toString());
                            double lng = Double.parseDouble(houseData.get("lng").toString());

                            LatLng latLng = new LatLng(lat,lng);

                            if (houseData.get("status").equals("לא עונים")) {
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.home_15x15_grey)));
                                marker.setTag(houseData.get("markerId").toString());
                            } else if (houseData.get("status").equals("לא מעוניינים")){
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.home_15x15_red)));
                                marker.setTag(houseData.get("markerId").toString());
                            } else if (houseData.get("status").equals("מעוניינים")){
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.home_15x15_green)));
                                marker.setTag(houseData.get("markerId").toString());
                            } else if (houseData.get("status").equals("קשר המשך")){
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.home_15x15_blue)));
                                marker.setTag(houseData.get("markerId").toString());
                            }
                        } else if (key.startsWith("B")) {
                            //building
                            HashMap<String, Object> buildingData = (HashMap<String, Object>) data;

                            double lat = Double.parseDouble(buildingData.get("lat").toString());
                            double lng = Double.parseDouble(buildingData.get("lng").toString());

                            LatLng latLng = new LatLng(lat,lng);
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.building15x15)));
                            marker.setTag("B" + buildingData.get("markerId").toString());

                        }

                    }

                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO: Do something about the error
            }
        };
        localDB.addValueEventListener(postListener);

        // sets the onclick of houses on the map
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

                //gets the location number
                int num = Integer.parseInt(getIntent().getStringExtra("num"));
                String location = getIntent().getStringExtra("location");
                //sets the location
                if (num == 0){
                    location = "Maalot";
                } else if (num == 1){
                    location = "Shderot";
                } else if (num == 2){
                    location = "KfarVardim";
                } else if (num == 3){
                    location = "TalEl";
                } else if (num == 4){
                    location = "Cholon";
                } else if (num == 5){
                    location = "Ashdod";
                } else if (num == 6){
                    location = "Tfachot";
                } else if (num == 7){
                    location = "Dalton";
                } else if (num == 8){
                    location = "BarYochay";
                }

                final String loc = location;

                //defines the location - specific db
                DatabaseReference localDB = FirebaseDatabase.getInstance().getReference().child("houses").child(location).child(marker.getTag().toString());

                //gets data for the marker house
                localDB.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        houseData = (HashMap<String, Object>) dataSnapshot.getValue();
                        isDB = true;

                        if (houseData.get("type").equals("House")) {

                            //launches drawer
                            houseViewNavdraw(houseData, house, isDB, status, activists, lastReportTitle, reportActivistsTitle, reportDateTitle, familyName, address, description, currentStatus, currentActivists, lastReport, reportDate, reportActivists, submit_edit);

                            if (houseData.get("reports") != null){
                                DatabaseReference reportDB = FirebaseDatabase.getInstance().getReference().child("reports").child(houseData.get("latestReport").toString());
                                reportDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        HashMap<String, Object> reportData = (HashMap<String, Object>) dataSnapshot.getValue();
                                        lastReport.setText(reportData.get("report").toString());
                                        reportActivists.setText(reportData.get("activists").toString());
                                        reportDate.setText(reportData.get("date").toString());
                                        lastReport.setVisibility(View.VISIBLE);
                                        reportActivists.setVisibility(View.VISIBLE);
                                        reportActivistsTitle.setVisibility(View.VISIBLE);
                                        reportDate.setVisibility(View.VISIBLE);
                                        reportDateTitle.setVisibility(View.VISIBLE);
                                        lastReportTitle.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                        Log.d("reportGetError", databaseError.getMessage());

                                    }
                                });
                            }

                            if (!houseData.get("pic").toString().equals("")){
                                //download image and display in the drawer
                                final long ONE_MEGABYTE = 1024 * 1024;
                                storageRef.child(houseData.get("pic").toString()).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        // Data for "images/island.jpg" is returns, use this as needed
                                        int width = pic.getWidth();
                                        int height = pic.getHeight();
                                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        pic.setImageBitmap(Bitmap.createScaledBitmap(bmp, width, height, false));
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle any errors
                                    }
                                });
                            } else {
                                pic.setImageDrawable(getResources().getDrawable(R.drawable.no_picture));
                            }
                        }

                        if (houseData.get("type").equals("Building")){

                            buildingViewNavdraw(houseData, building, isDB, add_house, houseList, familyName);

                            // this line clears the arraylist
                            arrayList.clear();
                            // next thing you have to do is check if your adapter has changed
                            adapter.notifyDataSetChanged();
                            building.emptyBuilding();
                            building.setAddress(houseData.get("address").toString());

                            ArrayList housesList = (ArrayList) houseData.get("houses");

                            for (int i = 0; i < housesList.size() ; i++){

                                DatabaseReference houseDB = FirebaseDatabase.getInstance().getReference().child("houses").child(loc).child(housesList.get(i).toString());

                                houseDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        HashMap<String, Object> house = (HashMap<String, Object>) dataSnapshot.getValue();

                                        // this line adds the data of the apartmnet and puts in your array
                                        arrayList.add(house.get("apartment") + ": " + "משפחת " + house.get("name"));
                                        // next thing you have to do is check if your adapter has changed
                                        adapter.notifyDataSetChanged();

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                            if (!houseData.get("pic").toString().equals("")){
                                //download image and display in the drawer
                                final long ONE_MEGABYTE = 1024 * 1024;
                                storageRef.child(houseData.get("pic").toString()).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        // Data for "images/island.jpg" is returns, use this as needed
                                        int width = pic.getWidth();
                                        int height = pic.getHeight();
                                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        pic.setImageBitmap(Bitmap.createScaledBitmap(bmp, width, height, false));

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle any errors
                                    }
                                });
                            } else {
                                pic.setImageDrawable(getResources().getDrawable(R.drawable.no_picture));
                            }

                            houseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    String selectedFromList = (String) (houseList.getItemAtPosition(position));
                                    String str = "";
                                    for (int i = 0; i < selectedFromList.length(); i++){
                                        char c = selectedFromList.charAt(i);
                                        if (Character.toString(c).equals(":")) break;

                                        str = str + c;

                                    }

                                    DatabaseReference houseDB = FirebaseDatabase.getInstance().getReference().child("houses").child(loc).child("A" + str + houseData.get("markerId").toString());

                                    houseDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            HashMap<String, Object> houseM = (HashMap<String, Object>) dataSnapshot.getValue();

                                            //launches drawer
                                            isDB = true;
                                            houseViewNavdraw(houseM, house, isDB, status, activists, lastReportTitle, reportActivistsTitle, reportDateTitle, familyName, address, description, currentStatus, currentActivists, lastReport, reportDate, reportActivists, submit_edit);

                                            if (houseM.get("latestReport") != null){
                                                DatabaseReference reportDB = FirebaseDatabase.getInstance().getReference().child("reports").child(houseM.get("latestReport").toString());
                                                reportDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        HashMap<String, Object> reportData = (HashMap<String, Object>) dataSnapshot.getValue();
                                                        lastReport.setText(reportData.get("report").toString());
                                                        reportActivists.setText(reportData.get("activists").toString());
                                                        reportDate.setText(reportData.get("date").toString());
                                                        lastReport.setVisibility(View.VISIBLE);
                                                        reportActivists.setVisibility(View.VISIBLE);
                                                        reportActivistsTitle.setVisibility(View.VISIBLE);
                                                        reportDate.setVisibility(View.VISIBLE);
                                                        reportDateTitle.setVisibility(View.VISIBLE);
                                                        lastReportTitle.setVisibility(View.VISIBLE);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        Log.d("reportGetError", databaseError.getMessage());

                                                    }
                                                });
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                }
                            });

                            View.OnClickListener add_houseOnClick = new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    final EditText input = new EditText(getApplicationContext());

                                    new AlertDialog.Builder(NAVDRAW.this)
                                            .setTitle("דירה חדשה")
                                            .setMessage("איזו דירה זו? (במספרים)").setView(input)
                                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {

                                                    house.setApartment(input.getText().toString());

                                                    houseEditNavdraw(status, familyNameEdit, addressEdit, descriptionEdit, currentStatusEdit, submit_house, add_report);

                                                    add_report.setVisibility(View.INVISIBLE);
                                                    addressEdit.setVisibility(View.INVISIBLE);
                                                    address.setVisibility(View.VISIBLE);
                                                    isHouseUploaded = false;
                                                    isInBuilding = true;
                                                    house.setType("House");

                                                    //copies over content from building to house
                                                    house.setLat(building.getLat());
                                                    house.setLng(building.getLng());
                                                    house.setLatlng(building.getMarkerId());
                                                    house.setMarkerId("A" + house.getApartment() + houseData.get("markerId"));
                                                    house.setLocation(houseData.get("location").toString());

                                                    currentStatusEdit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                        @Override
                                                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                                                            if (currentStatusEdit.getSelectedItem().toString().equals("קשר המשך")){
                                                                currentActivistsEdit.setVisibility(View.VISIBLE);
                                                                activists.setVisibility(View.VISIBLE);
                                                                currentActivists.setVisibility(View.INVISIBLE);
                                                                add_report.setVisibility(View.VISIBLE);
                                                            } else if (currentStatusEdit.getSelectedItem().toString().equals("לא מעוניינים") || currentStatusEdit.getSelectedItem().toString().equals("מעוניינים")){
                                                                currentActivists.setVisibility(View.INVISIBLE);
                                                                activists.setVisibility(View.INVISIBLE);
                                                                currentActivistsEdit.setVisibility(View.INVISIBLE);
                                                                add_report.setVisibility(View.VISIBLE);
                                                            } else if (currentStatusEdit.getSelectedItem().toString().equals("לא עונים")) {
                                                                currentActivists.setVisibility(View.INVISIBLE);
                                                                activists.setVisibility(View.INVISIBLE);
                                                                currentActivistsEdit.setVisibility(View.INVISIBLE);
                                                                add_report.setVisibility(View.INVISIBLE);
                                                            }
                                                        }

                                                        @Override
                                                        public void onNothingSelected(AdapterView<?> parentView) {
                                                            // your code here
                                                        }

                                                    });

                                                    //sets the report onClicks
                                                    add_report.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {

                                                            reportEditNavdraw(reportEdit, reportDateEdit, reportActivistsEdit, submit_report);

                                                            submit_report.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {

                                                                    houseEditNavdraw(status, familyNameEdit, addressEdit, descriptionEdit, currentStatusEdit, submit_house, add_report);

                                                                    if (currentStatusEdit.getSelectedItem().toString() == "קשר המשך"){
                                                                        activists.setVisibility(View.VISIBLE);
                                                                        currentActivistsEdit.setVisibility(View.VISIBLE);
                                                                    }

                                                                    Date date = new Date();

                                                                    Report rpt = new Report(reportActivistsEdit.getText().toString(), reportEdit.getText().toString(), reportDateEdit.getText().toString());
                                                                    reportNameList.add(house.getMarkerId() + date.toString().replace(" ", "").replace(":", "").replace("GMT+", "").replace(".",""));
                                                                    reportList.add(rpt);

                                                                    lastReport.setVisibility(View.VISIBLE);
                                                                    lastReport.setText(reportEdit.getText());
                                                                    reportActivistsTitle.setVisibility(View.VISIBLE);
                                                                    reportActivists.setVisibility(View.VISIBLE);
                                                                    reportActivists.setText(reportActivistsEdit.getText());
                                                                    reportDateTitle.setVisibility(View.VISIBLE);
                                                                    reportDate.setVisibility(View.VISIBLE);
                                                                    reportDate.setText(reportDateEdit.getText());
                                                                    reportEdit.setVisibility(View.INVISIBLE);
                                                                    reportDateEdit.setVisibility(View.INVISIBLE);
                                                                    reportActivistsEdit.setVisibility(View.INVISIBLE);
                                                                    submit_report.setVisibility(View.INVISIBLE);
                                                                    reportDateEdit.setText("");
                                                                    reportActivistsEdit.setText("");
                                                                    reportEdit.setText("");
                                                                }
                                                            });
                                                        }
                                                    });

                                                    submit_house.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            // TODO: add safeguards against partial input

                                                            //setting all the values to house
                                                            house.setName(familyNameEdit.getText().toString());
                                                            house.setAddress(building.getAddress());
                                                            house.setDescription(descriptionEdit.getText().toString());
                                                            house.setStatus(currentStatusEdit.getSelectedItem().toString());
                                                            if (house.getStatus().equals("קשר המשך")){
                                                                house.setActivists(currentActivistsEdit.getText().toString());
                                                            }

                                                            currentStatusEdit.setSelection(0, false);

                                                            // this line adds the data of the apartment and puts in your array
                                                            arrayList.add(house.getApartment() + ": " + "משפחת " + house.getName());
                                                            // next thing you have to do is check if your adapter has changed
                                                            adapter.notifyDataSetChanged();

                                                            //upload reports to db
                                                            if (reportNameList.size() != 0){
                                                                for (int i = 0; i < reportNameList.size(); i++){

                                                                    mDatabase.child("reports").child(reportNameList.get(i)).setValue(reportList.get(i));

                                                                }
                                                                //sets the report list to the house
                                                                house.setReport(reportNameList);
                                                                house.setLatestReport(reportNameList.get(reportNameList.size() - 1));
                                                                //clears the lists for next use
                                                                reportNameList.clear();
                                                                reportList.clear();
                                                            }

                                                            //upload the image to firebase
                                                            if (pictureTaken) {
                                                                String idImg = Double.toString(building.getLat()).replace(".","") + Double.toString(building.getLng()).replace(".","");
                                                                UploadTask uploadTask = storageRef.child("B" + idImg + ".jpg").putBytes(byteArray);
                                                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception exception) {
                                                                        // Handle unsuccessful uploads
                                                                    }
                                                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                                                        // ...
                                                                    }
                                                                });
                                                                building.setPic("B" + idImg + ".jpg");
                                                            } else {
                                                                building.setPic("");
                                                            }
                                                            pictureTaken = false;
                                                            house.setPic(building.getPic());

                                                            //upload reports to db
                                                            if (reportNameList.size() != 0){
                                                                for (int i = 0; i < reportNameList.size(); i++){

                                                                    mDatabase.child("reports").child(reportNameList.get(i)).setValue(reportList.get(i));

                                                                }
                                                                //sets the report list to the house
                                                                house.setReport(reportNameList);
                                                                house.setLatestReport(reportNameList.get(reportNameList.size() - 1));
                                                                //clears the lists for next use
                                                                reportNameList.clear();
                                                                reportList.clear();
                                                                reportActivistsEdit.setText("");
                                                                reportDateEdit.setText("");
                                                                reportEdit.setText("");
                                                            }

                                                            //empty the edits
                                                            familyNameEdit.setText("");
                                                            addressEdit.setText("");
                                                            descriptionEdit.setText("");
                                                            currentStatusEdit.setSelection(0, false);

                                                            mDatabase.child("houses").child(house.getLocation()).child(house.getMarkerId()).setValue(house);
                                                            building.addHouse(house.getMarkerId());
                                                            house.emptyHouse();
                                                            isHouseUploaded = true;

                                                            mDatabase.child("houses").child(house.getLocation()).child("B" + houseData.get("markerId")).setValue(building);

                                                            isInBuilding = false;
                                                            isDB = false;
                                                            buildingViewNavdraw(houseData, building, isDB, add_house, houseList, familyName);

                                                            input.setText("");
                                                        }
                                                    });

                                                }
                                            })
                                            .show();
                                }
                            };

                            add_house.setOnClickListener(add_houseOnClick);

                        }

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("houseGetError", databaseError.getMessage());
                    }
                });

                // locks drawer from opening from swipes
                navDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                // If the navigation drawer is not open then open it, if its already open then close it.
                if(!navDrawer.isDrawerOpen(Gravity.RIGHT)) navDrawer.openDrawer(Gravity.RIGHT);
                else navDrawer.closeDrawer(Gravity.LEFT);

                return true;
            }
        });

        building_onclick = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                closeFABMenu();
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(final LatLng point) {

                        final EditText input = new EditText(getApplicationContext());

                        new AlertDialog.Builder(NAVDRAW.this)
                                .setTitle("בניין חדש")
                                .setMessage("מה הכתובת של הבניין?")
                                .setView(input)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        mMap.setOnMapClickListener(null);

                                        building.setAddress(input.getText().toString());

                                        // this line clears the arraylist
                                        arrayList.clear();
                                        // next thing you have to do is check if your adapter has changed
                                        adapter.notifyDataSetChanged();
                                        building.emptyBuilding();
                                        //the navdraw
                                        isDB = true;
                                        buildingViewNavdraw(houseData, building, isDB, add_house, houseList, familyName);
                                        familyName.setText(building.getAddress());
                                        pic.setImageDrawable(getResources().getDrawable(R.drawable.no_picture));

                                        //launches drawer
                                        // locks drawer from opening from swipes
                                        navDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                                        // If the navigation drawer is not open then open it, if its already open then close it.
                                        if(!navDrawer.isDrawerOpen(Gravity.RIGHT)) navDrawer.openDrawer(Gravity.RIGHT);
                                        else navDrawer.closeDrawer(Gravity.LEFT);

                                        isBuildingUploaded = false;
                                        building.setType("Building");

//                        navDrawer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                            @Override
//                            public void onFocusChange(View v, boolean hasFocus) {
//                                hideKeyboard(activity);
//                            }
//                        });

                                        //sets building latlng
                                        building.setLat(point.latitude);
                                        building.setLng(point.longitude);

                                        //gets the location
                                        int num = Integer.parseInt(getIntent().getStringExtra("num"));
                                        //sets the location
                                        if (num == 0){
                                            building.setLocation("Maalot");
                                        } else if (num == 1){
                                            building.setLocation("Shderot");
                                        } else if (num == 2){
                                            building.setLocation("KfarVardim");
                                        } else if (num == 3){
                                            building.setLocation("TalEl");
                                        } else if (num == 4){
                                            building.setLocation("Cholon");
                                        } else if (num == 5){
                                            building.setLocation("Ashdod");
                                        } else if (num == 6){
                                            building.setLocation("Tfachot");
                                        } else if (num == 7){
                                            building.setLocation("Dalton");
                                        } else if (num == 8){
                                            building.setLocation("BarYochay");
                                        }

                                        building.setMarkerId(Double.toString(building.getLat()).replace(".","") + Double.toString(building.getLng()).replace(".",""));

                                        View.OnClickListener add_houseOnClick = new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                final EditText input = new EditText(getApplicationContext());

                                                new AlertDialog.Builder(NAVDRAW.this)
                                                        .setTitle("דירה חדשה")
                                                        .setMessage("איזו דירה זו? (במספרים)").setView(input)
                                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {

                                                                house.setApartment(input.getText().toString());

                                                                houseEditNavdraw(status, familyNameEdit, addressEdit, descriptionEdit, currentStatusEdit, submit_house, add_report);

                                                                add_report.setVisibility(View.INVISIBLE);
                                                                addressEdit.setVisibility(View.INVISIBLE);
                                                                address.setVisibility(View.VISIBLE);
                                                                address.setText(building.getAddress().toString());
                                                                isHouseUploaded = false;
                                                                isInBuilding = true;
                                                                house.setType("House");

                                                                //copies over content from building to house
                                                                house.setLat(building.getLat());
                                                                house.setLng(building.getLng());
                                                                house.setLatlng(building.getMarkerId());
                                                                house.setMarkerId("A" + house.getApartment() + building.getMarkerId());
                                                                house.setLocation(building.getLocation());

                                                                currentStatusEdit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                                    @Override
                                                                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                                                                        if (currentStatusEdit.getSelectedItem().toString().equals("קשר המשך")){
                                                                            currentActivistsEdit.setVisibility(View.VISIBLE);
                                                                            activists.setVisibility(View.VISIBLE);
                                                                            currentActivists.setVisibility(View.INVISIBLE);
                                                                            add_report.setVisibility(View.VISIBLE);
                                                                        } else if (currentStatusEdit.getSelectedItem().toString().equals("לא מעוניינים") || currentStatusEdit.getSelectedItem().toString().equals("מעוניינים")){
                                                                            currentActivists.setVisibility(View.INVISIBLE);
                                                                            activists.setVisibility(View.INVISIBLE);
                                                                            currentActivistsEdit.setVisibility(View.INVISIBLE);
                                                                            add_report.setVisibility(View.VISIBLE);
                                                                        } else if (currentStatusEdit.getSelectedItem().toString().equals("לא עונים")) {
                                                                            currentActivists.setVisibility(View.INVISIBLE);
                                                                            activists.setVisibility(View.INVISIBLE);
                                                                            currentActivistsEdit.setVisibility(View.INVISIBLE);
                                                                            add_report.setVisibility(View.INVISIBLE);
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onNothingSelected(AdapterView<?> parentView) {
                                                                        // your code here
                                                                    }

                                                                });

                                                                //sets the report onClicks
                                                                add_report.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {

                                                                        reportEditNavdraw(reportEdit, reportDateEdit, reportActivistsEdit, submit_report);

                                                                        submit_report.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View v) {

                                                                                houseEditNavdraw(status, familyNameEdit, addressEdit, descriptionEdit, currentStatusEdit, submit_house, add_report);

                                                                                if (currentStatusEdit.getSelectedItem().toString() == "קשר המשך"){
                                                                                    activists.setVisibility(View.VISIBLE);
                                                                                    currentActivistsEdit.setVisibility(View.VISIBLE);
                                                                                }

                                                                                Date date = new Date();

                                                                                Report rpt = new Report(reportActivistsEdit.getText().toString(), reportEdit.getText().toString(), reportDateEdit.getText().toString());
                                                                                reportNameList.add(house.getMarkerId() + date.toString().replace(" ", "").replace(":", "").replace("GMT+", "").replace(".",""));
                                                                                reportList.add(rpt);

                                                                                lastReport.setVisibility(View.VISIBLE);
                                                                                lastReport.setText(reportEdit.getText());
                                                                                reportActivistsTitle.setVisibility(View.VISIBLE);
                                                                                reportActivists.setVisibility(View.VISIBLE);
                                                                                reportActivists.setText(reportActivistsEdit.getText());
                                                                                reportDateTitle.setVisibility(View.VISIBLE);
                                                                                reportDate.setVisibility(View.VISIBLE);
                                                                                reportDate.setText(reportDateEdit.getText());
                                                                                reportEdit.setVisibility(View.INVISIBLE);
                                                                                reportDateEdit.setVisibility(View.INVISIBLE);
                                                                                reportActivistsEdit.setVisibility(View.INVISIBLE);
                                                                                submit_report.setVisibility(View.INVISIBLE);
                                                                                reportDateEdit.setText("");
                                                                                reportActivistsEdit.setText("");
                                                                                reportEdit.setText("");
                                                                            }
                                                                        });
                                                                    }
                                                                });

                                                                submit_house.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        // TODO: add safeguards against partial input

                                                                        //setting all the values to house
                                                                        house.setName(familyNameEdit.getText().toString());
                                                                        house.setAddress(building.getAddress());
                                                                        house.setDescription(descriptionEdit.getText().toString());
                                                                        house.setStatus(currentStatusEdit.getSelectedItem().toString());
                                                                        if (house.getStatus().equals("קשר המשך")){
                                                                            house.setActivists(currentActivistsEdit.getText().toString());
                                                                        }

                                                                        currentStatusEdit.setSelection(0, false);

                                                                        // this line adds the data of the apartment and puts in your array
                                                                        arrayList.add(house.getApartment() + ": " + "משפחת " + house.getName());
                                                                        // next thing you have to do is check if your adapter has changed
                                                                        adapter.notifyDataSetChanged();

                                                                        //upload reports to db
                                                                        if (reportNameList.size() != 0){
                                                                            for (int i = 0; i < reportNameList.size(); i++){

                                                                                mDatabase.child("reports").child(reportNameList.get(i)).setValue(reportList.get(i));

                                                                            }
                                                                            //sets the report list to the house
                                                                            house.setReport(reportNameList);
                                                                            house.setLatestReport(reportNameList.get(reportNameList.size() - 1));
                                                                            //clears the lists for next use
                                                                            reportNameList.clear();
                                                                            reportList.clear();
                                                                        }

                                                                        //upload the image to firebase
                                                                        if (pictureTaken) {
                                                                            String idImg = Double.toString(building.getLat()).replace(".","") + Double.toString(building.getLng()).replace(".","");
                                                                            UploadTask uploadTask = storageRef.child("B" + idImg + ".jpg").putBytes(byteArray);
                                                                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception exception) {
                                                                                    // Handle unsuccessful uploads
                                                                                }
                                                                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                                @Override
                                                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                                                                    // ...
                                                                                }
                                                                            });
                                                                            building.setPic("B" + idImg + ".jpg");
                                                                        } else {
                                                                            building.setPic("");
                                                                        }
                                                                        pictureTaken = false;
                                                                        house.setPic(building.getPic());

                                                                        //upload reports to db
                                                                        if (reportNameList.size() != 0){
                                                                            for (int i = 0; i < reportNameList.size(); i++){

                                                                                mDatabase.child("reports").child(reportNameList.get(i)).setValue(reportList.get(i));

                                                                            }
                                                                            //sets the report list to the house
                                                                            house.setReport(reportNameList);
                                                                            house.setLatestReport(reportNameList.get(reportNameList.size() - 1));
                                                                            //clears the lists for next use
                                                                            reportNameList.clear();
                                                                            reportList.clear();
                                                                            reportActivistsEdit.setText("");
                                                                            reportDateEdit.setText("");
                                                                            reportEdit.setText("");
                                                                        }

                                                                        //empty the edits
                                                                        familyNameEdit.setText("");
                                                                        addressEdit.setText("");
                                                                        descriptionEdit.setText("");
                                                                        currentStatusEdit.setSelection(0, false);

                                                                        mDatabase.child("houses").child(house.getLocation()).child(house.getMarkerId()).setValue(house);
                                                                        building.addHouse(house.getMarkerId());
                                                                        house.emptyHouse();
                                                                        isHouseUploaded = true;

                                                                        mDatabase.child("houses").child(building.getLocation()).child("B" + building.getMarkerId()).setValue(building);

                                                                        LatLng latLng = new LatLng(building.getLat(), building.getLng());
                                                                        Marker marker = mMap.addMarker(new MarkerOptions()
                                                                                .position(latLng)
                                                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.building15x15)));
                                                                        marker.setTag("B" + building.getMarkerId());

                                                                        isInBuilding = false;
                                                                        isDB = false;
                                                                        buildingViewNavdraw(houseData, building, isDB, add_house, houseList, familyName);

                                                                        input.setText("");
                                                                    }
                                                                });

                                                            }
                                                        })
                                                        .show();
                                            }
                                        };

                                        add_house.setOnClickListener(add_houseOnClick);

                                        add_houseOnClick.onClick(v);

                                        houseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                String selectedFromList = (String) (houseList.getItemAtPosition(position));
                                                String str = "";
                                                for (int i = 0; i < selectedFromList.length(); i++){
                                                    char c = selectedFromList.charAt(i);
                                                    if (Character.toString(c).equals(":")) break;

                                                    str = str + c;

                                                }

                                                DatabaseReference houseDB = FirebaseDatabase.getInstance().getReference().child("houses").child(building.getLocation()).child("A" + str + building.getMarkerId().toString());

                                                houseDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        HashMap<String, Object> houseM = (HashMap<String, Object>) dataSnapshot.getValue();

                                                        //launches drawer
                                                        isDB = true;
                                                        houseViewNavdraw(houseM, house, isDB, status, activists, lastReportTitle, reportActivistsTitle, reportDateTitle, familyName, address, description, currentStatus, currentActivists, lastReport, reportDate, reportActivists, submit_edit);

                                                        address.setText("דירה: " + houseM.get("apartment").toString());

                                                        if (houseM.get("latestReport") != null){
                                                            DatabaseReference reportDB = FirebaseDatabase.getInstance().getReference().child("reports").child(houseM.get("latestReport").toString());
                                                            reportDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    HashMap<String, Object> reportData = (HashMap<String, Object>) dataSnapshot.getValue();
                                                                    lastReport.setText(reportData.get("report").toString());
                                                                    reportActivists.setText(reportData.get("activists").toString());
                                                                    reportDate.setText(reportData.get("date").toString());
                                                                    lastReport.setVisibility(View.VISIBLE);
                                                                    reportActivists.setVisibility(View.VISIBLE);
                                                                    reportActivistsTitle.setVisibility(View.VISIBLE);
                                                                    reportDate.setVisibility(View.VISIBLE);
                                                                    reportDateTitle.setVisibility(View.VISIBLE);
                                                                    lastReportTitle.setVisibility(View.VISIBLE);
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                    Log.d("reportGetError", databaseError.getMessage());

                                                                }
                                                            });
                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        });

                                    }
                                }).show();

                    }
                });
            }

        };

        //sets the report onClicks
        add_report_onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportEditNavdraw(reportEdit, reportDateEdit, reportActivistsEdit, submit_report);
                submit_report.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        houseEditNavdraw(status, familyNameEdit, addressEdit, descriptionEdit, currentStatusEdit, submit_house, add_report);
                        if (currentStatusEdit.getSelectedItem().toString() == "קשר המשך"){
                            activists.setVisibility(View.VISIBLE);
                            currentActivistsEdit.setVisibility(View.VISIBLE);
                        }
                        Date date = new Date();
                        Report rpt = new Report(reportActivistsEdit.getText().toString(), reportEdit.getText().toString(), reportDateEdit.getText().toString());
                        String rptName = house.getMarkerId() + date.toString().replace(" ", "").replace(":", "").replace("GMT+", "").replace(".","");
                        mDatabase.child("houses").child(house.getLocation().toString()).child(house.getMarkerId().toString()).child("latestReport").setValue(rptName);
                        mDatabase.child("reports").child(rptName).setValue(rpt);



                        lastReport.setVisibility(View.VISIBLE);
                        lastReport.setText(reportEdit.getText());
                        reportActivistsTitle.setVisibility(View.VISIBLE);
                        reportActivists.setVisibility(View.VISIBLE);
                        reportActivists.setText(reportActivistsEdit.getText());
                        reportDateTitle.setVisibility(View.VISIBLE);
                        reportDate.setVisibility(View.VISIBLE);
                        reportDate.setText(reportDateEdit.getText());
                        reportEdit.setVisibility(View.INVISIBLE);
                        reportDateEdit.setVisibility(View.INVISIBLE);
                        reportActivistsEdit.setVisibility(View.INVISIBLE);
                        submit_report.setVisibility(View.INVISIBLE);
                        reportDateEdit.setText("");
                        reportActivistsEdit.setText("");
                        reportEdit.setText("");
                    }
                });
            }
        };

        house_onclick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFABMenu();
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                    @Override
                    public void onMapClick(LatLng point) {

                        mMap.setOnMapClickListener(null);

                        houseEditNavdraw(status, familyNameEdit, addressEdit, descriptionEdit, currentStatusEdit, submit_house, add_report);
                        isHouseUploaded = false;
                        pic.setImageDrawable(getResources().getDrawable(R.drawable.no_picture));
                        house.setType("House");

//                        navDrawer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                            @Override
//                            public void onFocusChange(View v, boolean hasFocus) {
//                                hideKeyboard(activity);
//                            }
//                        });

                        //sets house latlng
                        house.setLat(point.latitude);
                        house.setLng(point.longitude);

                        //gets the location
                        int num = Integer.parseInt(getIntent().getStringExtra("num"));
                        //sets the location
                        if (num == 0){
                            house.setLocation("Maalot");
                        } else if (num == 1){
                            house.setLocation("Shderot");
                        } else if (num == 2){
                            house.setLocation("KfarVardim");
                        } else if (num == 3){
                            house.setLocation("TalEl");
                        } else if (num == 4){
                            house.setLocation("Cholon");
                        } else if (num == 5){
                            house.setLocation("Ashdod");
                        } else if (num == 6){
                            house.setLocation("Tfachot");
                        } else if (num == 7){
                            house.setLocation("Dalton");
                        } else if (num == 8){
                            house.setLocation("BarYochay");
                        }

                        currentStatusEdit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                                if (currentStatusEdit.getSelectedItem().toString().equals("קשר המשך")){
                                    currentActivistsEdit.setVisibility(View.VISIBLE);
                                    activists.setVisibility(View.VISIBLE);
                                    currentActivists.setVisibility(View.INVISIBLE);
                                    add_report.setVisibility(View.VISIBLE);
                                } else if (currentStatusEdit.getSelectedItem().toString().equals("לא מעוניינים") || currentStatusEdit.getSelectedItem().toString().equals("מעוניינים")){
                                    currentActivists.setVisibility(View.INVISIBLE);
                                    activists.setVisibility(View.INVISIBLE);
                                    currentActivistsEdit.setVisibility(View.INVISIBLE);
                                    add_report.setVisibility(View.VISIBLE);
                                } else if (currentStatusEdit.getSelectedItem().toString().equals("לא עונים")) {
                                    currentActivists.setVisibility(View.INVISIBLE);
                                    activists.setVisibility(View.INVISIBLE);
                                    currentActivistsEdit.setVisibility(View.INVISIBLE);
                                    add_report.setVisibility(View.INVISIBLE);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parentView) {
                                // your code here
                            }

                        });

                        //launches drawer
                        // locks drawer from opening from swipes
                        navDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        // If the navigation drawer is not open then open it, if its already open then close it.
                        if(!navDrawer.isDrawerOpen(Gravity.RIGHT)) navDrawer.openDrawer(Gravity.RIGHT);
                        else navDrawer.closeDrawer(Gravity.LEFT);

                        house.setMarkerId(Double.toString(house.getLat()).replace(".","") + Double.toString(house.getLng()).replace(".",""));

                        //sets the report onClicks
                        add_report.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                reportEditNavdraw(reportEdit, reportDateEdit, reportActivistsEdit, submit_report);

                                submit_report.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        houseEditNavdraw(status, familyNameEdit, addressEdit, descriptionEdit, currentStatusEdit, submit_house, add_report);

                                        if (currentStatusEdit.getSelectedItem().toString() == "קשר המשך"){
                                            activists.setVisibility(View.VISIBLE);
                                            currentActivistsEdit.setVisibility(View.VISIBLE);
                                        }

                                        Date date = new Date();

                                        Report rpt = new Report(reportActivistsEdit.getText().toString(), reportEdit.getText().toString(), reportDateEdit.getText().toString());
                                        reportNameList.add(house.getMarkerId() + date.toString().replace(" ", "").replace(":", "").replace("GMT+", "").replace(".",""));
                                        reportList.add(rpt);

                                        lastReport.setVisibility(View.VISIBLE);
                                        lastReport.setText(reportEdit.getText());
                                        reportActivistsTitle.setVisibility(View.VISIBLE);
                                        reportActivists.setVisibility(View.VISIBLE);
                                        reportActivists.setText(reportActivistsEdit.getText());
                                        reportDateTitle.setVisibility(View.VISIBLE);
                                        reportDate.setVisibility(View.VISIBLE);
                                        reportDate.setText(reportDateEdit.getText());
                                        reportEdit.setVisibility(View.INVISIBLE);
                                        reportDateEdit.setVisibility(View.INVISIBLE);
                                        reportActivistsEdit.setVisibility(View.INVISIBLE);
                                        submit_report.setVisibility(View.INVISIBLE);
                                        reportDateEdit.setText("");
                                        reportActivistsEdit.setText("");
                                        reportEdit.setText("");
                                    }
                                });
                            }
                        });

                        submit_house.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO: add safeguards against partial input

                                //sets data for "blank" drawer
                                familyName.setText(familyNameEdit.getText());
                                familyNameEdit.setVisibility(View.INVISIBLE);
                                familyName.setVisibility(View.VISIBLE);

                                address.setText(addressEdit.getText());
                                addressEdit.setVisibility(View.INVISIBLE);
                                address.setVisibility(View.VISIBLE);

                                description.setText(descriptionEdit.getText());
                                descriptionEdit.setVisibility(View.INVISIBLE);
                                description.setVisibility(View.VISIBLE);

                                currentStatusEdit.setVisibility(View.INVISIBLE);
                                currentStatus.setText(currentStatusEdit.getSelectedItem().toString());
                                currentStatus.setVisibility(View.VISIBLE);

                                if (currentStatusEdit.getSelectedItem().toString().equals("קשר המשך")){
                                    currentActivists.setText(currentActivistsEdit.getText());
                                    currentActivistsEdit.setVisibility(View.INVISIBLE);
                                    currentActivists.setVisibility(View.VISIBLE);
                                    activists.setVisibility(View.VISIBLE);
                                    house.setActivists(currentActivists.getText().toString());
                                    currentActivists.setText("");
                                }

                                submit_report.setVisibility(View.INVISIBLE);
                                add_report.setVisibility(View.INVISIBLE);

                                //empty the edits
                                familyNameEdit.setText("");
                                addressEdit.setText("");
                                descriptionEdit.setText("");
                                currentStatusEdit.setSelection(0, false);

                                //setting all the values to house
                                house.setName(familyName.getText().toString());
                                house.setAddress(address.getText().toString());
                                house.setDescription(description.getText().toString());
                                house.setStatus(currentStatus.getText().toString());

                                if (house.getStatus().equals("קשר המשך")){
                                    currentActivists.setVisibility(View.VISIBLE);
                                    activists.setVisibility(View.VISIBLE);
                                }

                                //define which image to set to the markers depending on the status
                                BitmapDescriptor icn;
                                if (house.getStatus().equals("לא עונים")) {
                                    icn = BitmapDescriptorFactory.fromResource(R.drawable.home_15x15_grey);
                                } else if (house.getStatus().equals("לא מעוניינים")){
                                    icn = BitmapDescriptorFactory.fromResource(R.drawable.home_15x15_red);
                                } else if (house.getStatus().equals("מעוניינים")){
                                    icn = BitmapDescriptorFactory.fromResource(R.drawable.home_15x15_green);
                                } else if (house.getStatus().equals("קשר המשך")){
                                    icn = BitmapDescriptorFactory.fromResource(R.drawable.home_15x15_blue);
                                }
                                LatLng latLng = new LatLng(house.getLat(),house.getLng());
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.home_15x15_grey)));

                                marker.setTag(house.getMarkerId());

                                //change camera position
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

                                //upload the image to firebase
                                if (pictureTaken) {
                                    String idImg = Double.toString(house.getLat()).replace(".","") + Double.toString(house.getLng()).replace(".","");
                                    UploadTask uploadTask = storageRef.child(idImg + ".jpg").putBytes(byteArray);
                                    uploadTask.addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Handle unsuccessful uploads
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                            // ...
                                        }
                                    });
                                    house.setPic(idImg + ".jpg");
                                } else {
                                    house.setPic("");
                                }
                                pictureTaken = false;

                                //upload reports to db
                                if (reportNameList.size() != 0){
                                    for (int i = 0; i < reportNameList.size(); i++){

                                        mDatabase.child("reports").child(reportNameList.get(i)).setValue(reportList.get(i));

                                    }
                                    //sets the report list to the house
                                    house.setReport(reportNameList);
                                    house.setLatestReport(reportNameList.get(reportNameList.size() - 1));
                                    //clears the lists for next use
                                    reportNameList.clear();
                                    reportList.clear();
                                    reportActivistsEdit.setText("");
                                    reportDateEdit.setText("");
                                    reportEdit.setText("");
                                }

                                mDatabase.child("houses").child(house.getLocation()).child(house.getMarkerId()).setValue(house);
                                isHouseUploaded = true;

                                //hides the button
                                Button btn = findViewById(R.id.submit_house);
                                btn.setVisibility(View.INVISIBLE);
                            }
                        });

                    }

                });
            }
        };

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_PIC_REQUEST && data != null) {
            //get the image
            Bitmap bmp = (Bitmap) data.getExtras().get("data");
            pictureTaken = true;

            //set image to activity
            final ImageView pic = findViewById(R.id.pic);
            int width = pic.getWidth();
            int height = pic.getHeight();
            pic.setImageBitmap(Bitmap.createScaledBitmap(bmp, width, height, false));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byteArray = baos.toByteArray();
        }
    }

    private void showFABMenu(){
        FloatingActionButton fab_main = findViewById(R.id.floating_action_button_main);
        FloatingActionButton fab_house = findViewById(R.id.floating_action_button_house);
        FloatingActionButton fab_Building = findViewById(R.id.floating_action_button_building);
        isFABOpen=true;
        fab_house.setOnClickListener(house_onclick);
        fab_Building.setOnClickListener(building_onclick);
        fab_house.show();
        fab_Building.show();
        fab_house.animate().translationY(-getResources().getDimension(R.dimen.standard_62));
        fab_Building.animate().translationY(-getResources().getDimension(R.dimen.standard_125));
    }

    private void closeFABMenu(){
        FloatingActionButton fab_main = findViewById(R.id.floating_action_button_main);
        FloatingActionButton fab_house = findViewById(R.id.floating_action_button_house);
        FloatingActionButton fab_Building = findViewById(R.id.floating_action_button_building);
        isFABOpen=false;
        fab_house.setOnClickListener(null);
        fab_Building.setOnClickListener(null);
        mMap.setOnMapClickListener(null);
        fab_house.animate().translationY(0);
        fab_Building.animate().translationY(0);
        fab_house.hide();
        fab_Building.hide();
    }

    public void emptyNavdraw () {

        TextView familyName = findViewById(R.id.familyName);
        EditText familyNameEdit = findViewById(R.id.familyNameEdit);
        TextView address = findViewById(R.id.address);
        EditText addressEdit = findViewById(R.id.addressEdit);
        TextView description = findViewById(R.id.description);
        EditText descriptionEdit = findViewById(R.id.descriptionEdit);
        TextView status = findViewById(R.id.status);
        TextView currentStatus = findViewById(R.id.currentStatus);
        Spinner currentStatusEdit = findViewById(R.id.currentStatusEdit);
        TextView activists = findViewById(R.id.activists);
        TextView currentActivists = findViewById(R.id.currentActivists);
        EditText currentActivistsEdit = findViewById(R.id.currentActivistsEdit);
        Button submit_house = findViewById(R.id.submit_house);
        Button submit_report = findViewById(R.id.submit_report);
        Button add_report = findViewById(R.id.add_report);
        TextView lastReport = findViewById(R.id.lastReport);
        EditText reportActivistsEdit = findViewById(R.id.reportActivistsEdit);
        EditText reportEdit = findViewById(R.id.reportEdit);
        TextView lastReportTitle = findViewById(R.id.lastReportTitle);
        TextView reportActivistsTitle = findViewById(R.id.reportActivistsTitle);
        TextView reportActivists = findViewById(R.id.reportActivists);
        TextView reportDateTitle = findViewById(R.id.reportDateTitle);
        TextView reportDate = findViewById(R.id.reportDate);
        EditText reportDateEdit = findViewById(R.id.reportDateEdit);
        Button add_house = findViewById(R.id.add_house);
        ListView houseList = findViewById(R.id.houseList);
        Button submit_building = findViewById(R.id.submit_building);
        Button submit_edit = findViewById(R.id.submit_edit);


        familyName.setVisibility(View.INVISIBLE);
        familyNameEdit.setVisibility(View.INVISIBLE);
        address.setVisibility(View.INVISIBLE);
        addressEdit.setVisibility(View.INVISIBLE);
        description.setVisibility(View.INVISIBLE);
        descriptionEdit.setVisibility(View.INVISIBLE);
        status.setVisibility(View.INVISIBLE);
        currentStatus.setVisibility(View.INVISIBLE);
        currentStatusEdit.setVisibility(View.INVISIBLE);
        activists.setVisibility(View.INVISIBLE);
        currentActivists.setVisibility(View.INVISIBLE);
        currentActivistsEdit.setVisibility(View.INVISIBLE);
        submit_house.setVisibility(View.INVISIBLE);
        submit_report.setVisibility(View.INVISIBLE);
        add_report.setVisibility(View.INVISIBLE);
        lastReport.setVisibility(View.INVISIBLE);
        reportActivistsEdit.setVisibility(View.INVISIBLE);
        reportEdit.setVisibility(View.INVISIBLE);
        lastReportTitle.setVisibility(View.INVISIBLE);
        reportActivistsTitle.setVisibility(View.INVISIBLE);
        reportActivists.setVisibility(View.INVISIBLE);
        reportDateTitle.setVisibility(View.INVISIBLE);
        reportDate.setVisibility(View.INVISIBLE);
        reportDateEdit.setVisibility(View.INVISIBLE);
        add_house.setVisibility(View.INVISIBLE);
        houseList.setVisibility(View.INVISIBLE);
        submit_building.setVisibility(View.INVISIBLE);
        submit_edit.setVisibility(View.INVISIBLE);

    }

    public void houseViewNavdraw (final HashMap houseData, final House house, final Boolean isDB, TextView status, final TextView activists, TextView lastReportTitle, TextView reportActivistsTitle, TextView reportDateTitle, final TextView familyName, final TextView address, final TextView description, final TextView currentStatus, final TextView currentActivists, TextView lastReport, TextView reportDate, TextView reportActivists, final Button submit_edit) {

        emptyNavdraw();

        final EditText familyNameEdit = findViewById(R.id.familyNameEdit);
        final EditText addressEdit = findViewById(R.id.addressEdit);
        final EditText descriptionEdit = findViewById(R.id.descriptionEdit);
        final EditText currentActivistsEdit = findViewById(R.id.currentActivistsEdit);
        final Spinner currentStatusEdit = findViewById(R.id.currentStatusEdit);
        final Button add_report = findViewById(R.id.add_report);

        status.setVisibility(View.VISIBLE);
        familyName.setVisibility(View.VISIBLE);
        address.setVisibility(View.VISIBLE);
        description.setVisibility(View.VISIBLE);
        currentStatus.setVisibility(View.VISIBLE);

        familyName.setText("משפחת" + " " + houseData.get("name").toString());
        address.setText(houseData.get("address").toString());
        description.setText(houseData.get("description").toString());
        currentStatus.setText(houseData.get("status").toString());

        final View.OnClickListener[] onClickListeners = new View.OnClickListener[5];

        //family name
        onClickListeners[0] = null;
        //address
        onClickListeners[1] = null;
        //description
        onClickListeners[2] = null;
        //activists
        onClickListeners[3] = null;
        //status
        onClickListeners[4] = null;

        onClickListeners[0] = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                familyName.setOnClickListener(null);
                address.setOnClickListener(null);
                description.setOnClickListener(null);
                currentActivists.setOnClickListener(null);
                currentStatus.setOnClickListener(null);

                familyNameEdit.setText(familyName.getText().toString().replace("משפחת ", ""));
                familyNameEdit.setVisibility(View.VISIBLE);
                familyName.setVisibility(View.INVISIBLE);
                submit_edit.setVisibility(View.VISIBLE);

                submit_edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        DatabaseReference localDB = null;

                        if (isDB) {
                            localDB = FirebaseDatabase.getInstance().getReference().child("houses").child(houseData.get("location").toString()).child(houseData.get("markerId").toString()).child("name");
                        } else {
                            localDB = FirebaseDatabase.getInstance().getReference().child("houses").child(house.getLocation()).child(house.getMarkerId()).child("name");
                        }

                        localDB.setValue(familyNameEdit.getText().toString());

                        familyName.setText("משפחת " + familyNameEdit.getText());
                        familyNameEdit.setText("");
                        familyName.setVisibility(View.VISIBLE);
                        familyNameEdit.setVisibility(View.INVISIBLE);
                        submit_edit.setVisibility(View.INVISIBLE);

                        familyName.setOnClickListener(onClickListeners[0]);
                        address.setOnClickListener(onClickListeners[1]);
                        description.setOnClickListener(onClickListeners[2]);
                        currentActivists.setOnClickListener(onClickListeners[3]);
                        currentStatus.setOnClickListener(onClickListeners[4]);
                    }
                });
            }
        };

        familyName.setOnClickListener(onClickListeners[0]);

        onClickListeners[1] = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                familyName.setOnClickListener(null);
                address.setOnClickListener(null);
                description.setOnClickListener(null);
                currentActivists.setOnClickListener(null);
                currentStatus.setOnClickListener(null);

                addressEdit.setText(address.getText());
                addressEdit.setVisibility(View.VISIBLE);
                address.setVisibility(View.INVISIBLE);
                submit_edit.setVisibility(View.VISIBLE);

                submit_edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference localDB = null;

                        if (isDB) {
                            localDB = FirebaseDatabase.getInstance().getReference().child("houses").child(houseData.get("location").toString()).child(houseData.get("markerId").toString()).child("address");
                        } else {
                            localDB = FirebaseDatabase.getInstance().getReference().child("houses").child(house.getLocation()).child(house.getMarkerId()).child("address");
                        }localDB.setValue(addressEdit.getText().toString());

                        address.setText(addressEdit.getText());
                        addressEdit.setText("");
                        address.setVisibility(View.VISIBLE);
                        addressEdit.setVisibility(View.INVISIBLE);
                        submit_edit.setVisibility(View.INVISIBLE);
                    }
                });
            }
        };

        address.setOnClickListener(onClickListeners[1]);

        onClickListeners[2] = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                familyName.setOnClickListener(null);
                address.setOnClickListener(null);
                description.setOnClickListener(null);
                currentActivists.setOnClickListener(null);
                currentStatus.setOnClickListener(null);

                descriptionEdit.setText(description.getText());
                descriptionEdit.setVisibility(View.VISIBLE);
                description.setVisibility(View.INVISIBLE);
                submit_edit.setVisibility(View.VISIBLE);

                submit_edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference localDB = null;

                        if (isDB) {
                            localDB = FirebaseDatabase.getInstance().getReference().child("houses").child(houseData.get("location").toString()).child(houseData.get("markerId").toString()).child("description");
                        } else {
                            localDB = FirebaseDatabase.getInstance().getReference().child("houses").child(house.getLocation()).child(house.getMarkerId()).child("description");
                        }localDB.setValue(addressEdit.getText().toString());

                        localDB.setValue(descriptionEdit.getText().toString());

                        description.setText(descriptionEdit.getText());
                        descriptionEdit.setText("");
                        description.setVisibility(View.VISIBLE);
                        descriptionEdit.setVisibility(View.INVISIBLE);
                        submit_edit.setVisibility(View.INVISIBLE);
                    }
                });
            }
        };

        description.setOnClickListener(onClickListeners[2]);

        onClickListeners[3] = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                familyName.setOnClickListener(null);
                address.setOnClickListener(null);
                description.setOnClickListener(null);
                currentActivists.setOnClickListener(null);
                currentStatus.setOnClickListener(null);

                currentActivistsEdit.setText(currentActivists.getText());
                currentActivistsEdit.setVisibility(View.VISIBLE);
                currentActivists.setVisibility(View.INVISIBLE);
                submit_edit.setVisibility(View.VISIBLE);

                submit_edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference localDB = null;

                        if (isDB) {
                            localDB = FirebaseDatabase.getInstance().getReference().child("houses").child(houseData.get("location").toString()).child(houseData.get("markerId").toString()).child("activists");
                        } else {
                            localDB = FirebaseDatabase.getInstance().getReference().child("houses").child(house.getLocation()).child(house.getMarkerId()).child("activists");
                        }localDB.setValue(addressEdit.getText().toString());

                        localDB.setValue(currentActivistsEdit.getText().toString());

                        currentActivists.setText(currentActivistsEdit.getText());
                        currentActivistsEdit.setText("");
                        currentActivists.setVisibility(View.VISIBLE);
                        currentActivistsEdit.setVisibility(View.INVISIBLE);
                        submit_edit.setVisibility(View.INVISIBLE);
                    }
                });
            }
        };

        currentActivists.setOnClickListener(onClickListeners[3]);

        onClickListeners[4] = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                familyName.setOnClickListener(null);
                address.setOnClickListener(null);
                description.setOnClickListener(null);
                currentActivists.setOnClickListener(null);
                currentStatus.setOnClickListener(null);

                submit_edit.setVisibility(View.VISIBLE);

                if (currentStatus.getText().toString().equals("לא עונים")){
                    currentStatusEdit.setSelection(0, false);
                } else if (currentStatus.getText().toString().equals("לא מעוניינים")){
                    currentStatusEdit.setSelection(1, false);
                } else if (currentStatus.getText().toString().equals("מעוניינים")){
                    currentStatusEdit.setSelection(2, false);
                } else if (currentStatus.getText().toString().equals("קשר המשך")){
                    currentStatusEdit.setSelection(3, false);
                }

                currentStatus.setVisibility(View.INVISIBLE);
                currentStatusEdit.setVisibility(View.VISIBLE);

                currentStatusEdit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        if (currentStatusEdit.getSelectedItem().toString().equals("קשר המשך")){
                            currentActivistsEdit.setVisibility(View.VISIBLE);
                            activists.setVisibility(View.VISIBLE);
                            currentActivists.setVisibility(View.INVISIBLE);
                            add_report.setVisibility(View.VISIBLE);
                        } else if (currentStatusEdit.getSelectedItem().toString().equals("לא מעוניינים") || currentStatusEdit.getSelectedItem().toString().equals("מעוניינים")){
                            currentActivists.setVisibility(View.INVISIBLE);
                            activists.setVisibility(View.INVISIBLE);
                            currentActivistsEdit.setVisibility(View.INVISIBLE);
                            add_report.setVisibility(View.VISIBLE);
                        } else if (currentStatusEdit.getSelectedItem().toString().equals("לא עונים")) {
                            currentActivists.setVisibility(View.INVISIBLE);
                            activists.setVisibility(View.INVISIBLE);
                            currentActivistsEdit.setVisibility(View.INVISIBLE);
                            add_report.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                    }

                });

                submit_edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference localDB = null;

                        if (isDB) {
                            localDB = FirebaseDatabase.getInstance().getReference().child("houses").child(houseData.get("location").toString()).child(houseData.get("markerId").toString()).child("status");
                        } else {
                            localDB = FirebaseDatabase.getInstance().getReference().child("houses").child(house.getLocation()).child(house.getMarkerId()).child("status");
                        }localDB.setValue(addressEdit.getText().toString());

                        localDB.setValue(currentStatusEdit.getSelectedItem().toString());

                        currentStatus.setText(currentStatusEdit.getSelectedItem().toString());
                        currentStatusEdit.setSelection(0,false);
                        currentStatus.setVisibility(View.VISIBLE);
                        currentStatusEdit.setVisibility(View.INVISIBLE);
                        submit_edit.setVisibility(View.INVISIBLE);
                    }
                });
            }
        };

        currentStatus.setOnClickListener(onClickListeners[4]);

        if (houseData.get("status").toString().equals("קשר המשך")){
            activists.setVisibility(View.VISIBLE);
            currentActivists.setText(houseData.get("activists").toString());
            currentActivists.setVisibility(View.VISIBLE);

            currentActivists.setOnClickListener(onClickListeners[3]);
        }

        if (houseData.get("latestReport") != null){

            DatabaseReference reportDB = FirebaseDatabase.getInstance().getReference().child("reports").child(houseData.get("latestReport").toString());
            reportDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    TextView lastReport = findViewById(R.id.lastReport);
                    TextView lastReportTitle = findViewById(R.id.lastReportTitle);
                    TextView reportActivistsTitle = findViewById(R.id.reportActivistsTitle);
                    TextView reportActivists = findViewById(R.id.reportActivists);
                    TextView reportDateTitle = findViewById(R.id.reportDateTitle);
                    TextView reportDate = findViewById(R.id.reportDate);

                    HashMap<String, Object> reportData = (HashMap<String, Object>) dataSnapshot.getValue();
                    lastReport.setText(reportData.get("report").toString());
                    reportActivists.setText(reportData.get("activists").toString());
                    reportDate.setText(reportData.get("date").toString());
                    lastReport.setVisibility(View.VISIBLE);
                    reportActivists.setVisibility(View.VISIBLE);
                    reportActivistsTitle.setVisibility(View.VISIBLE);
                    reportDate.setVisibility(View.VISIBLE);
                    reportDateTitle.setVisibility(View.VISIBLE);
                    lastReportTitle.setVisibility(View.VISIBLE);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d("reportGetError", databaseError.getMessage());
                }
            });
        }

        if (!houseData.get("status").toString().equals("לא מעוניינים")){
            house.setLocation(houseData.get("location").toString());
            house.setMarkerId(houseData.get("markerId").toString());

            add_report.setVisibility(View.VISIBLE);
            add_report.setOnClickListener(add_report_onClick);
        }

    }

    public void houseEditNavdraw (TextView status, EditText familyNameEdit, EditText addressEdit, EditText descriptionEdit, Spinner currentStatusEdit, Button submit_house, Button add_report) {

        emptyNavdraw();

        familyNameEdit.setVisibility(View.VISIBLE);
        addressEdit.setVisibility(View.VISIBLE);
        descriptionEdit.setVisibility(View.VISIBLE);
        status.setVisibility(View.VISIBLE);
        currentStatusEdit.setVisibility(View.VISIBLE);
        submit_house.setVisibility(View.VISIBLE);
        // add_report.setVisibility(View.VISIBLE);

    }

    public void reportEditNavdraw (EditText reportActivistsEdit, EditText reportEdit, EditText reportDateEdit, Button submit_report) {

        emptyNavdraw();

        submit_report.setVisibility(View.VISIBLE);
        reportActivistsEdit.setVisibility(View.VISIBLE);
        reportEdit.setVisibility(View.VISIBLE);
        reportDateEdit.setVisibility(View.VISIBLE);
    }

    public void buildingViewNavdraw (final HashMap houseData, final Building building, final Boolean isDB, Button add_house, ListView houseList, final TextView familyName){

        emptyNavdraw();

        final EditText familyNameEdit = findViewById(R.id.familyNameEdit);
        final Button submit_edit = findViewById(R.id.submit_edit);

        familyName.setVisibility(View.VISIBLE);
        add_house.setVisibility(View.VISIBLE);
        houseList.setVisibility(View.VISIBLE);

        familyName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                familyName.setVisibility(View.INVISIBLE);
                familyNameEdit.setVisibility(View.VISIBLE);
                submit_edit.setVisibility(View.VISIBLE);
                familyNameEdit.setText(familyName.getText());

                submit_edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        DatabaseReference localDB = null;

                        if (isDB) {
                            localDB = FirebaseDatabase.getInstance().getReference().child("houses").child(houseData.get("location").toString()).child(houseData.get("markerId").toString()).child("address");
                        } else {
                            localDB = FirebaseDatabase.getInstance().getReference().child("houses").child(building.getLocation()).child(building.getMarkerId()).child("address");
                        }

                        localDB.setValue(familyNameEdit.getText().toString());

                        familyName.setText(familyNameEdit.getText());
                        familyNameEdit.setText("");
                        familyName.setVisibility(View.VISIBLE);
                        familyNameEdit.setVisibility(View.INVISIBLE);
                        submit_edit.setVisibility(View.INVISIBLE);

                    }
                });

            }
        });
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}