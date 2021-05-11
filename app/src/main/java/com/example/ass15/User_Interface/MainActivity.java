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
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.ass15.Models.PolylineData;
import com.example.ass15.Models.User;
import com.example.ass15.Models.UserLocation;
import com.example.ass15.UserClient;
import com.example.ass15.services.JavaMailAPI;
import com.example.ass15.services.LocationService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.ass15.R;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

//firebase
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

import static com.example.ass15.Utility.Constants.ERROR_DIALOG_REQUEST;
import static com.example.ass15.Utility.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;
import static com.example.ass15.Utility.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static java.lang.Math.sqrt;

import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.PolyUtil;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

// Accelerometer
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;




public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnPolylineClickListener,
        Password_Dialog.PasswordDialogListener,
        SensorEventListener {
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
        mGoogleMap.setOnPolylineClickListener(this);
    }


    //Widgets
    private MapView mMapView;
    private DrawerLayout drawer;
    private GoogleMap mGoogleMap;
    private static final float DEFAULT_ZOOM = 15f;
    private FirebaseFirestore mDb;
    public EditText mSearchBox;

    //Variables
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private UserLocation mUserLocation;
    private static final int REQUEST_CALL = 1;
    private GeoApiContext mGeoApiContext = null;
    private ArrayList<PolylineData> mPolyLinesData = new ArrayList<>();
    private ArrayList<Marker> mTripMarker = new ArrayList<>();
    private Marker mSelectedMarker = null;
    public List<LatLng> mList;
    private boolean UserArrivedAtTheDestination = false;
    private double mtripDuration;
    public static long Passwordcount = 0;

    private final static int INTERVAL = 1000 * 30 * 1; //1 minutes
    Handler mHandler = new Handler();
    private String mPasswordcheck;
    private Sensor mySensor;
    private SensorManager SM;


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
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        mDb = FirebaseFirestore.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

//        mMapView = (MapView) findViewById(R.id.map);

        getUserDetails(); // this should be called once the system checks map services
        //mSearchBox = findViewById(R.id.input_search);
        mSearchBox = findViewById(R.id.input_search);
        init();

        //testAPI
        if(mGeoApiContext == null){
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey("AIzaSyD2qy1sj-PUts2MqLmL75SzPtZsDXgTOIQ")
                    .build();
        }

        // Accelerometer
        SM = (SensorManager) getSystemService(SENSOR_SERVICE);

        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event){
        int xValue, yValue, zValue;

        xValue = (int) event.values[0];
        yValue = (int) event.values[1];
        zValue = (int) event.values[2];

        if ((xValue > 1.5 && xValue < 3) || ((yValue > 1.5) && (yValue < 3)) || ((zValue > 1.5) && (zValue > 3))){
            sendEmail();
            makePoliceCall();
        }
    }


    Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {
            if (mList != null) {
            getLastKnownLocation();
            CheckIfUserLocationIsOnPath();
            CheckIfUserArrivedAtDestination();
            }
            mHandler.postDelayed(mHandlerTask, INTERVAL);
        }
    };

    void startRepeatingTask()
    {
        mHandlerTask.run();
    }

    void stopRepeatingTask()
    {
        mHandler.removeCallbacks(mHandlerTask);
    }



    private void calculateDirections(Marker marker){
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        mUserLocation.getGeo_point().getLatitude(),
                        mUserLocation.getGeo_point().getLongitude()
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "onResult: routes: " + result.routes[0].toString());
                Log.d(TAG, "onResult: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "onFailure: " + e.getMessage() );

            }
        });
    }


    @Override
    public void onInfoWindowClick(final Marker marker) {
        if(marker.getTitle().contains("Trip #")){
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Open Google Maps?" +
                    "  If you select yes the application will enter 'Safe Mode'")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            String latitude = String.valueOf(marker.getPosition().latitude);
                            String longitude = String.valueOf(marker.getPosition().longitude);
                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");

                            try{
                                if (mapIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                                    startActivity(mapIntent);

                                }
                            } catch (NullPointerException e){
                                Log.e(TAG, "onClick: NullPointerException: Couldn't open map." + e.getMessage() );
                                Toast.makeText(MainActivity.this, "Couldn't open map", Toast.LENGTH_SHORT).show();
                            }

                        }

                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();



            startRepeatingTask(); // this checks if the user is on the route on a given time interval
        }
            else{

                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Calculate directions?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                resetSelectedMarker();
                                mSelectedMarker = marker;
                                calculateDirections(marker);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            }

    }

    /*
    Adding polylines to the map
     */

    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);
                if(mPolyLinesData.size() > 0){
                    for(PolylineData polylineData: mPolyLinesData){
                        polylineData.getPolyline().remove();
                    }
                    mPolyLinesData.clear();
                    mPolyLinesData = new ArrayList<>();
                }

                double duration = 999999999;
                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();
                    mList = newDecodedPath;

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(MainActivity.this, R.color.grey));
                    polyline.setClickable(true);
                    mPolyLinesData.add(new PolylineData(polyline, route.legs[0]));

                    // highlight the fastest route and adjust camera
                    mtripDuration = route.legs[0].duration.inSeconds;
                    double tempDuration = route.legs[0].duration.inSeconds;
                    if(tempDuration < duration){
                        duration = tempDuration;
                        onPolylineClick(polyline);
                        SetCamerViewOnRoute(polyline.getPoints());
                    }

                    mSelectedMarker.setVisible(false);
                }
            }
        });
    }


    @Override
    public void onPolylineClick(Polyline polyline) {

        int index = 0;
        for(PolylineData polylineData: mPolyLinesData){
            index++;
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(ContextCompat.getColor(MainActivity.this, R.color.blue));
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation = new LatLng(
                        polylineData.getLeg().endLocation.lat,
                        polylineData.getLeg().endLocation.lng
                );

                Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(endLocation)
                        .title("Trip #" + index)
                        .snippet("Duration: " + polylineData.getLeg().duration
                        ));

                mTripMarker.add(marker);

                marker.showInfoWindow();

            }
            else{
                polylineData.getPolyline().setColor(ContextCompat.getColor(MainActivity.this, R.color.grey));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }


    private void resetSelectedMarker(){
        if(mSelectedMarker != null){
            mSelectedMarker.setVisible(true);
            mSelectedMarker = null;
            removeTripMarkers();
        }
    }

    private void removeTripMarkers(){
        for(Marker marker: mTripMarker){
            marker.remove();
        }
    }

    public void SetCamerViewOnRoute(List<LatLng> lstLatLngRoute) {

        if (mGoogleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 200;
        LatLngBounds latLngBounds = boundsBuilder.build();

        mGoogleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }



    /*
    This method will be used when the user successfully arrives at the destination
     */
    //TODO add a button to allow the user to reset the map in case if he no longer wishes to travel to the given location
    private void ClearMap(){
        if(mGoogleMap != null) {
            mGoogleMap.clear();

            if(mPolyLinesData.size() > 0){
                mPolyLinesData.clear();
                mPolyLinesData = new ArrayList<>();
            }
        }
    }


/*
     3 Point contact (detecting anomalies in user behaviour that will indicate that the user may be in danger)
 */

    // Stage 1: The user has deviated from the intended route

    public void CheckIfUserLocationIsOnPath(){
        LatLng user = (
                new LatLng(
                        mUserLocation.getGeo_point().getLatitude(),
                        mUserLocation.getGeo_point().getLongitude()
                ));

        if(PolyUtil.isLocationOnPath(user, mList, true,35)){

        }

        else {
            openDialog();
            // Add logic Dialog Fragment

        }
    }



    // stage 2: The user has not arrived at the location
    public void CheckIfUserArrivedAtDestination(){
        LatLng user = (
                new LatLng(
                        mUserLocation.getGeo_point().getLatitude(),
                        mUserLocation.getGeo_point().getLongitude()
                ));
        LatLng destination = mList.get(mList.size()-1);
        double estimateArrival = mtripDuration;

//        //This will check if the user arrived at the destination
//        long startTime = Long.valueOf((long) estimateArrival);
//        new CountDownTimer(startTime, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//
//            }
//
//            @Override
//            public void onFinish() {
//                openDialog();
//
//            }
//        }.start()
        ;



        if(PolyUtil.isLocationOnPath(user, Collections.singletonList(destination), true,35)){
            // user arrived at destination
            Toast.makeText(this, "You have arrived ", Toast.LENGTH_SHORT).show();
            stopRepeatingTask();
            ClearMap();
        }

    }


    // Stage 3: The user accelerometer indicated that the user may be in danger




    //Prompt the user to enter password
    public void openDialog(){
        Password_Dialog password_dialog = new Password_Dialog();
        password_dialog.show(getSupportFragmentManager(),"Password Dialog");
        stopRepeatingTask();
    }

    @Override
    public void applyText(String password) {
        User currentUser = ((UserClient)(getApplicationContext())).getUser();
        //currentUser.getEmail();
        if(password.equals(currentUser.getPassword())){
            Passwordcount = 0 ;
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Would you like to cancel the trip")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener(){

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    stopRepeatingTask();
                                    ClearMap();
                                }
                            })

                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick( DialogInterface dialog, int which) {
                            //startRepeatingTask();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
            Toast.makeText(this, "Password is right", Toast.LENGTH_SHORT).show();

        }
        // Check if user inserted passwords 4 times
        else if (Passwordcount>2){
            sendEmail();
            sendEmailConfirmation();
            ClearMap();
            Passwordcount = 0 ;
        }
        //Password is wrong
        else {
            Passwordcount++; //this will cound how many times the user will enter the password
            Toast.makeText(this, "Password is wrong", Toast.LENGTH_SHORT).show();
            openDialog();
        }



    }

    //Alerting Police
    private void sendEmail() {
        String newline = System.getProperty("line.separator");
        User currentUser = ((UserClient)(getApplicationContext())).getUser();
        UserLocation currentLocation = ((UserClient)(getApplicationContext())).getUserLocation();
        String mEmail = "ass15alertingpolice@gmail.com";
        String mSubject = "User:" + currentUser.getFullName() + "  is in danger";
        String mMessage = "User details:" +"   "+ currentUser.getUsername()
                +newline+"User Full Name: " + currentUser.getFullName()
                +newline+"User Email: " + currentUser.getEmail()
                +newline+"Age: "+ currentUser.getAge()
                +newline+"Phone Nr: "+ currentUser.getPhoneNr()
                +newline+"Last known location: "+ currentLocation.getGeo_point();
        JavaMailAPI javaMailAPI = new JavaMailAPI(this, mEmail, mSubject, mMessage);

        javaMailAPI.execute();
    }
    //This will send an email confirmation to the user
    private void sendEmailConfirmation() {
        String newline = System.getProperty("line.separator");
        User currentUser = ((UserClient)(getApplicationContext())).getUser();
        UserLocation currentLocation = ((UserClient)(getApplicationContext())).getUserLocation();
        String mEmail = currentUser.getEmail();
        String mSubject = "" + currentUser.getFullName() + " You have notified the police";
        String mMessage = "Dear" +"   "+ currentUser.getFullName()
                +newline+" As we are concerned that you may be in danger, a notification was sent to the police. If you are not in danger please contact the police on 999 to cancel the investigation";
        JavaMailAPI javaMailAPI = new JavaMailAPI(this, mEmail, mSubject, mMessage);

        javaMailAPI.execute();
    }

    //Adding record in notification page with the alert(so in case of a false alert the user is able to cancel it)







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
            mGoogleMap.setOnInfoWindowClickListener(this);
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


/*
         Navigation Menu
*/
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.Nav_Call_Police:
                makePoliceCall();
                break;
            case R.id.Nav_Notification:
                //Toast.makeText(this, "Test1", Toast.LENGTH_SHORT).show();
                CancelCurrentTrip();
                break;

            case R.id.Nav_settings:
                Toast.makeText(this, "Test2", Toast.LENGTH_SHORT).show();
                break;

            case R.id.Nav_Sign_Out:
                signOut();
                break;
        }
    drawer.closeDrawer(GravityCompat.START);
    return true;
    }

    public void signOut(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void makePoliceCall() {
        String number = "999";

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CALL_PHONE}, REQUEST_CALL);
        } else {
            String dial = "tel:" + number;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        }

    }
    private void CancelCurrentTrip(){
        openDialog();

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

//    @Override
//    public void onResume() {
//        super.onResume();
////        mMapView.onResume();
//        if (checkMapServices()) {
//            if (mLocationPermissionGranted) {
//                getUserDetails();
//            }
//        } else {
//            getLocationPermission();
//        }
//    }


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