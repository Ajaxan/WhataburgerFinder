package redfootdev.com.locationfinder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker marker;
    private Marker whatamarker;
    private TextView latField;
    private TextView lngField;
    private TextView whatalatField;
    private TextView whatalngField;
    MyAsyncTask myAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        latField = findViewById(R.id.youLat);
        lngField = findViewById(R.id.youLng);
        whatalatField = findViewById(R.id.whataLat);
        whatalngField = findViewById(R.id.whataLng);
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

        LatLng starting = new LatLng(32.1656, -82.9001);
        LatLng whataburger = new LatLng(30.839340, -83.943196);

        latField.setText("32.1656");
        lngField.setText("-82.900");
        whatalatField.setText("30.839340");
        whatalngField.setText("-83.943196");

        whatamarker = mMap.addMarker(new MarkerOptions().position(whataburger).title("Nearby Whataburger"));
        marker = mMap.addMarker(new MarkerOptions().position(starting).title("Your Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(starting));
    }

    /** Called when the user clicks the Send button */
    public void sendMessage(View view){
        Log.d("response","This isn't working");
        EditText editText =(EditText) findViewById(R.id.locationInput);
        String message = editText.getText().toString();
        message = message.replace(" ", "+");
        myAsyncTask=new MyAsyncTask();
        myAsyncTask.execute(message);
    }




    private class MyAsyncTask extends AsyncTask<String, Void, String[]> {
        ProgressDialog dialog = new ProgressDialog(MapsActivity.this);
        String message;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Please wait...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected String[] doInBackground(String... params) {

            String response;
            String whataresponse;
            String message = params[0];

            try {
                response = getLatLongByURL("https://maps.googleapis.com/maps/api/geocode/json?address="+message+"&key=AIzaSyB_A8KIkZlZPej_uymOwrk3AgMZMVkwNo4");
            } catch (Exception e) {
                return new String[]{"error"};
            }

            message += "+Whataburger";

            try {
                whataresponse = getLatLongByURL("https://maps.googleapis.com/maps/api/geocode/json?address="+message+"&key=AIzaSyB_A8KIkZlZPej_uymOwrk3AgMZMVkwNo4");
                Log.d("response",""+response);
                return new String[]{response,whataresponse};
            } catch (Exception e) {
                return new String[]{"error"};
            }
        }

        @Override
        protected void onPostExecute(String... result) {
            try {
                JSONObject jsonObject = new JSONObject(result[0]);

                double lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lng");

                double lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lat");

                Log.d("latitude", "" + lat);
                Log.d("longitude", "" + lng);
                LatLng latlng = new LatLng(lat, lng);

                latField.setText("" + lat);
                lngField.setText("" + lng);
                marker.setPosition(latlng);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                JSONObject jsonObject = new JSONObject(result[1]);

                double lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lng");

                double lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lat");

                Log.d("latitude", "" + lat);
                Log.d("longitude", "" + lng);
                LatLng latlng = new LatLng(lat, lng);
                if(result.length == 2) {
                    whatalatField.setText(""+lat);
                    whatalngField.setText(""+lng);
                    whatamarker.setPosition(latlng);

                } else {
                    whatalatField.setText("No Nearby Whataburger");
                    whatalngField.setText("No Nearby Whataburger");
                }

                //mMap.addMarker(new MarkerOptions().position(latlng).title("Marker in Sydney"));
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            } catch (JSONException e) {
                whatalatField.setText("No Nearby Whataburger");
                whatalngField.setText("No Nearby Whataburger");
                e.printStackTrace();
            }


            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }



        public String getLatLongByURL(String requestURL) {
            URL url;
            String response = "";
            try {
                url = new URL(requestURL);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                conn.setDoOutput(true);
                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                } else {
                    response = "";
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }
    }
}
