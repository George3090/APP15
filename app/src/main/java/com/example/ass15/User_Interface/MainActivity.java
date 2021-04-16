package com.example.ass15.User_Interface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.ass15.Models.User;
import com.example.ass15.Models.UserLocation;
import com.example.ass15.UserClient;
import com.example.ass15.services.LocationService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.ass15.R;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

//firebase
import com.google.android.libraries.places.api.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.ass15.Utility.Constants.ERROR_DIALOG_REQUEST;
import static com.example.ass15.Utility.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;
import static com.example.ass15.Utility.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import com.google.maps.GeoApiContext;




public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    private static final String TAG = "MainActivity";


    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        //map.addMarker(new MarkerOptions().position(new LatLng(51.481583, -3.179090)).title("Marker"));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        mGoogleMap = map;
        init();
    }

    //Widgets
    private MapView mMapView;
    private DrawerLayout drawer;
    private GoogleMap mGoogleMap;
    private static final float DEFAULT_ZOOM = 15f;
    private FirebaseFirestore mDb;
    private GeoApiContext mGeoApiContext;
    public EditText mSearchBox;


    //VAR
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private UserLocation mUserLocation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //Nav bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_close, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        mDb = FirebaseFirestore.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


//        mMapView = (MapView) findViewById(R.id.map);
        //initGoogleMap(savedInstanceState);



        getUserDetails(); // this should be called once the system checks map services
        //mSearchBox = findViewById(R.id.input_search);
        mSearchBox = findViewById(R.id.input_search);
        init();

    }



//    private void initGoogleMap(Bundle savedInstanceState) {
//        // *** IMPORTANT ***
//        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
//        // objects or sub-Bundles.
//        Bundle mapViewBundle = null;
//        if (savedInstanceState != null) {
//            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
//        }
//        mMapView.onCreate(mapViewBundle);
//        mMapView.getMapAsync(this);
//        if(mGeoApiContext == null){
//            mGeoApiContext = new GeoApiContext.Builder()
//                    .apiKey(getString(R.string.google_maps_key))
//                    .build();
//        }
//    }


    private void init(){
        Log.d(TAG,"init:initializing1");
        mSearchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    geoLocate();
                }
                return false;
            }
        });
    }


    private void geoLocate(){
        Log.d(TAG,"geoLocate: geolocation");
        String searchString = mSearchBox.getText().toString();
        Geocoder geocoder = new Geocoder(MainActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);

        }catch (IOException e){
            Log.e(TAG,"geoLocate: Ioe exeption" + e.getMessage());
        }
        if (list.size() > 0){
            Address address = list.get(0);
            Log.d(TAG,"geoLocate: found location" + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0) );
        }
    }


    private void moveCamera(LatLng latLng, float zoom, String title){
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(title);
        mGoogleMap.addMarker(options);
    }

    private void moveCameraNoMarker(LatLng latLng, float zoom, String title){
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }


    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(this, LocationService.class);
//        this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                MainActivity.this.startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.example.ass15.services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }


    private void getUserDetails() {
        if (mUserLocation == null) {
            mUserLocation = new UserLocation();
            DocumentReference userRef = mDb.collection("collection_users").
                    document(FirebaseAuth.getInstance().getUid());
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        User user = task.getResult().toObject(User.class);
                        mUserLocation.setUser(user);
                        ((UserClient) getApplicationContext()).setUser(user);
                        getLastKnownLocation();

                    }
                }
            });
        } else {
            getLastKnownLocation();

        }
    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {

                    Location location = task.getResult();
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    mUserLocation.setGeo_point(geoPoint);
                    mUserLocation.setTimestamp(null);
                    moveCameraNoMarker(new LatLng(location.getLatitude(), location.getLongitude()),
                                DEFAULT_ZOOM,
                                "My Location");

                    saveUserLocation();
                    startLocationService();
                }
            }
        });
    }

    private void saveUserLocation() {
        if (mUserLocation != null) {
            DocumentReference locationRef = mDb.
                    collection("User Location").
                    document(FirebaseAuth.getInstance().getUid());
            locationRef.set(mUserLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                    }
                }
            });
        }
    }














    // test
    private boolean checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getUserDetails();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            // Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            //Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (mLocationPermissionGranted) {
                    getUserDetails();
                } else {
                    getLocationPermission();
                }
            }
        }

    }















    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//
//        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
//        if (mapViewBundle == null) {
//            mapViewBundle = new Bundle();
//            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
//        }
//
//        mMapView.onSaveInstanceState(mapViewBundle);
//        getUserDetails();
//    }

    @Override
    public void onResume() {
        super.onResume();
//        mMapView.onResume();
        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                getUserDetails();
            }
        } else {
            getLocationPermission();
        }
    }

//    @Override
//    public void onStop() {
//        super.onStop();
//        mMapView.onStop();
//    }
//
//
//    @Override
//    public void onDestroy() {
//        mMapView.onDestroy();
//        super.onDestroy();
//    }
//
//    @Override
//    public void onLowMemory() {
//        super.onLowMemory();
//        mMapView.onLowMemory();
//    }


}