package com.example.satya_05.instagram;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
//import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ChatFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {

    private boolean checkpermission = false;
    int PLACE_PICKER_REQUEST = 1;
    private static final int LocationRequestCode = 1234;
    GoogleMap gMap;
    MapView mapView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    AutoCompleteTextView place;
    ImageView gps,placepicker;
    private GoogleApiClient mGoogleApiClient;
    PlaceAutocompleteAdapter placeAutocompleteAdapter;
    LatLngBounds latLngBounds = new LatLngBounds(new LatLng(-40,-168), new LatLng(70,140));

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        place = view.findViewById(R.id.place);
        gps = view.findViewById(R.id.myplace);
        placepicker = view.findViewById(R.id.placepicker);
        mapView = view.findViewById(R.id.mapView);

        getLocationPermission();
        //Toast.makeText(getContext(),""+checkpermission,Toast.LENGTH_SHORT).show();
        if(checkpermission){
            getCurrentLocation(savedInstanceState);
            detectTextChange();

            gps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    gMap.clear();
                    fetchLocation();
                    place.setText("");
                }
            });

            placepicker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                    try {
                        startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
                    } catch (GooglePlayServicesRepairableException e) {
                        e.printStackTrace();
                    } catch (GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, getContext());

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, place.getId());
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            }
        }
    }

    public void getLocationPermission(){
        String permissions[] = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                checkpermission = true;
                //Toast.makeText(getContext(),"Permission given",Toast.LENGTH_SHORT).show();
            }
            else{
                requestPermissions(permissions,LocationRequestCode);
                //Toast.makeText(getContext(),"Permission requested",Toast.LENGTH_SHORT).show();
            }
        }
        else{
            requestPermissions(permissions,LocationRequestCode);
            //Toast.makeText(getContext(),"Permission requested",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        checkpermission = false;

        switch (requestCode){
            case LocationRequestCode:
                if(grantResults.length > 0){
                    for(int i=0;i<grantResults.length;i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            checkpermission = false;
                            //Toast.makeText(getContext(),"Permission not given",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    checkpermission = true;
                    //Toast.makeText(getContext(),"Permission given",Toast.LENGTH_SHORT).show();
                }
        }
    }

    public void getCurrentLocation(Bundle savedInstanceState){
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                gMap = googleMap;
                fetchLocation();
            }
        });
    }

    public void fetchLocation(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        try {
            com.google.android.gms.tasks.Task location = fusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task task) {
                    if(task.isSuccessful()){
                        Location mlocation = (Location) task.getResult();
                        moveCamera(new LatLng(mlocation.getLatitude(),mlocation.getLongitude()),18f,"Your Location");
                        gMap.setMyLocationEnabled(true);
                        gMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                    else{
                        Toast.makeText(getContext(),"Current location is not available",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    public void moveCamera(LatLng latLng, float zoom, String titile){
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

        if(!titile.equals("Your Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(titile);
            gMap.addMarker(options);
        }
    }

    public void detectTextChange(){

        mGoogleApiClient = new GoogleApiClient
                .Builder(getContext())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(getActivity(), this)
                .build();

        place.setOnItemClickListener(autocompletelistener);

        placeAutocompleteAdapter = new PlaceAutocompleteAdapter(getContext(),mGoogleApiClient,latLngBounds,null);
        place.setAdapter(placeAutocompleteAdapter);

        place.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_DONE
                        || i == EditorInfo.IME_ACTION_SEARCH
                        || keyEvent.getAction() == keyEvent.KEYCODE_ENTER
                        || keyEvent.getAction() == keyEvent.ACTION_DOWN){
                    locatePlace();
                }

                return false;
            }
        });
    }

    public void locatePlace(){
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        String search = place.getText().toString();
        Geocoder geocoder = new Geocoder(getContext());

        List<Address> list = new ArrayList<>();

        try{
            list = geocoder.getFromLocationName(search,1);
        }catch(IOException e){
            e.printStackTrace();
        }

        if(list.size() > 0){
            Address address = list.get(0);
            place.setText(address.getAddressLine(0));
            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),18f,address.getAddressLine(0));
        }
    }

    public AdapterView.OnItemClickListener autocompletelistener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            View view1 = getActivity().getCurrentFocus();
            if (view1 != null) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
            }

            final AutocompletePrediction item = placeAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                places.release();
                return;
            }
            final Place place = places.get(0);

            moveCamera(new LatLng(place.getViewport().getCenter().latitude,
                    place.getViewport().getCenter().longitude), 18f, place.getName().toString());

            places.release();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        mapView.onPause();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
