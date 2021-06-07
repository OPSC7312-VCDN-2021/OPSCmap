package com.adriann.opscmaps2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {


    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    //Initialise variable
    Spinner spType;
    Button btFind;
    SupportMapFragment supportMapFragment;
    GoogleMap map;
    FusedLocationProviderClient fusedLocationProviderClient;
    double currentLat = 0, currentLong = 0;
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    SearchView searchView;
    Button btnGetDirection, btnConvert, btnFavs;

    MarkerOptions place1,place2;
    Polyline currentPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //searchView:
        searchView = findViewById(R.id.sv_location);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //getting directions and route
        btnGetDirection = findViewById(R.id.directionsBtn);

        //runs conversion class:
        //Conversions cn = new Conversions();
        btnConvert = findViewById(R.id.convertMesurement);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            //lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            //cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        //Assign variable
        spType = findViewById(R.id.sp_type);
        btFind = findViewById(R.id.bt_find);

        //Initialize array of place type
        String[] placeTypeList = {"atm", "bank", "hospital", "movie_theater", "restaurant", "landmark"};
        //Initialize array of place name
        String[] placeNameList = {"ATM", "Bank", "Hospital", "Movie Theater", "Restaurant", "Landmark"};

        //set adapter on spinner
        spType.setAdapter(new ArrayAdapter<>(MapsActivity.this
                , android.R.layout.simple_spinner_dropdown_item, placeNameList));

        //Initialize fused Location provider client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //check permission
        if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //when permission granted
            //Call method
            getCurrentLocation();

        } else {
            //when permission denied
            //Request permission
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        btFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get selected position of spinner
                int i = spType.getSelectedItemPosition();
                //Initialize url
                String url = "https://maps.googleapis.com/api/place/nearbysearch/json" + //Url
                        "?location=" + currentLat + "," + currentLong + //Location Latititude and Longitude
                        "&radius=5000" + //Nearby radius
                        "&types=" + placeTypeList[i] + //Place type
                        "&sensor=true" + //Sensor
                        "&key" + getResources().getString(R.string.google_map_key); //Google map key

                //Execute place task method to download json data
                new PlaceTask().execute(url);
            }
        });
    }


    private void getCurrentLocation() {
        //Initialize task location


        @SuppressLint("MissingPermission")
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                //When success
                if (location != null) {
                    //when location is not equal to null

                    //get current latitude
                    currentLat = location.getLatitude();

                    //get current longitude
                    currentLong = location.getLongitude();

                    //sync map
                    supportMapFragment.getMapAsync(googleMap -> {
                        //when map is ready
                        map = googleMap;

                        //Initialize Lat lng
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                        //Create marker options
                        MarkerOptions options = new MarkerOptions().position(latLng).title("I am there");

                        //Zoom current location on map
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

                        //Add marker on map
                        googleMap.addMarker(options);


                    });
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //when permission granted
                //call method
                getCurrentLocation();
            }
        }
    }


    private class PlaceTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {

            String data = null;
            try {
                //Initialize data
                data = downloadUrl(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            //Execute parser task
            new ParserTask().execute(s);
        }
    }

    private String downloadUrl(String string) throws IOException {
        //Initialize url
        URL url = new URL(string);
        //Initialize connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //Connect connection
        connection.connect();
        //Initialize input stream
        InputStream stream = connection.getInputStream();
        //Initialize buff reader
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        //Initialize string builder
        StringBuilder builder = new StringBuilder();
        //Initialize string variable
        String line = "";
        //use while loop
        while((line = reader.readLine()) != null){
            //Append line
            builder.append((line));
        }
        //get append data
        String data = builder.toString();
        //Close reader
        reader.close();
        //return data
        return data;

    }

    private class ParserTask extends AsyncTask<String,Integer, List<HashMap<String,String>>> {
        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            //Create json parser class
            JsonParser jsonParser = new JsonParser();
            //Intialize hash map list
            List<HashMap<String, String>> mapList = null;

            JSONObject object = null;
            try {
                //Initialize json object
                object = new JSONObject(strings[0]);
                //Parse json object
                mapList = jsonParser.parseResult(object);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e)
            {
                e.printStackTrace();
            }
            //return map List
            return mapList;

        }


        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
            //Clear map
            try {
                map.clear();
            }catch(NullPointerException e)
            {
                e.printStackTrace();
            }
            //Use for loop
            for(int i=0; i<hashMaps.size(); i++)
            {
                //Initialize hash map
                HashMap<String,String> hashMapList = hashMaps.get(i);
                //get Latitude
                double lat = Double.parseDouble(hashMapList.get("lat"));
                //get Longitude
                double lng = Double.parseDouble(hashMapList.get("lng"));
                //get name
                String name = hashMapList.get("name");
                //Concat Latitude and Longitude
                LatLng latLng = new LatLng(lat,lng);
                //Initialize marker options
                MarkerOptions options = new MarkerOptions();
                //Set position
                options.position(latLng);
                //set title
                options.title(name);
                //add marker on map
                map.addMarker(options);
            }
        }
    }
    {

        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openConversions();
            }
        });

        //btnFavs =



//this code is supposed to draw the routing lined between two points
//        place1 = new MarkerOptions().position(new LatLng(27.658143, 85.3199503)).title("Location 1");
//        place1 = new MarkerOptions().position(new LatLng(27.667491, 85.3208583)).title("Location 2");
//
//        String url = getUrl(place1.getPosition(), place2.getPosition(), "driving");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                                              @Override
                                              public boolean onQueryTextSubmit(String query) {
                                                  String location = searchView.getQuery().toString();
                                                  List<Address> addressLst = null;
                                                  if (location != null || !location.equals("")) {
                                                      Geocoder geocoder = new Geocoder(MapsActivity.this);
                                                      try {
                                                          addressLst = geocoder.getFromLocationName(location, 1);
                                                      } catch (IOException e) {
                                                          e.printStackTrace();
                                                      }
                                                      Address address = addressLst.get(0);
                                                      LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                                                      mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                                                      //mMap.animateCamera(CameraUpdateFactory.nevLatLngZoom(latLng,10));
                                                      return false;
                                                  }
                                                  return false;
                                              }

                                                @Override
                                              public boolean onQueryTextChange(String newText) {
                                                  return false;
                                              }
                                          });

    }

    private void openConversions() {
        Intent i = new Intent(this, Conversions.class);
        startActivity(i);
    }

    public String getUrl(LatLng origin, LatLng dest, String directionMode) {

        String str_origin = "origin"+ origin.latitude + "," +origin.longitude;
        String str_dest = "destination"+ dest.latitude +","+ dest.longitude;
        String mode = "mode=" +directionMode;

        String parameters = str_origin + "&" + str_dest + "&" + mode;
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" +output+ "?" + parameters +"&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Change the map type based on the user's selection.
        switch (item.getItemId()) {
            case R.id.normal_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.hybrid_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.satellite_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.terrain_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
//calc distance!
    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;
    }
}
