package com.location.rmaps;


import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.location.rmaps.Helpers.DirectionsJSONParser;
import com.location.rmaps.Helpers.HttpConnection;


import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    LatLng kochin = new LatLng(9.93988, 76.26022);
    LatLng coimbatore = new LatLng(11.004556, 76.961632);
    LatLng Madurai = new LatLng(9.925201, 78.119775);
    LatLng Munnar = new LatLng(10.0889333, 77.0595248);
    GoogleMap map;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private String getMapsApiDirectionsUrl() {
        String origin = "origin=" + kochin.latitude + "," + kochin.longitude;
        String waypoints = "waypoints=optimize:true|" + coimbatore.latitude + "," + coimbatore.longitude + "|";
        String destination = "destination=" + Munnar.latitude + "," + Munnar.longitude;

        String sensor = "sensor=false";
        String params = origin + "&" + waypoints + "&"  + destination + "&" + sensor;
        String output = "json";

        /*String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params + "&key="+"AIzaSyD0KDpTwbZOs4ux42vfhrE6ZtipT63-6Wc" ;*/

        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params +"&key=" +"AIzaSyBbl6RPStsJr0_9rPPfvE2hBgRi-tJGzFc";
        return url;
    }

    private void addMarkers() {
        if (map != null) {
            map.addMarker(new MarkerOptions().position(kochin)
                    .title("Kochin"));
            map.addMarker(new MarkerOptions().position(coimbatore)
                    .title("Coimbatore"));
            map.addMarker(new MarkerOptions().position(Madurai)
                    .title("Madurai"));
            map.addMarker(new MarkerOptions().position(Munnar)
                    .title("Munnar"));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        MarkerOptions options = new MarkerOptions();
        options.position(kochin);
        options.position(coimbatore);
        options.position(Madurai);
        options.position(Munnar);
        googleMap.addMarker(options);
      /*  String url = getMapsApiDirectionsUrl();
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(url);*/

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kochin ,
                6));
        addMarkers();
    }

    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(2);
                polyLineOptions.color(Color.BLUE);
            }

            map.addPolyline(polyLineOptions);
        }
    }
}