package otabek.io.obhavo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.Arrays;

import cz.msebera.android.httpclient.Header;


public class locationInput extends AppCompatActivity {

    //Memeber variables
    public int longitude;
    public int latitude;
    String message;
    int counter ;
    // tag for logcat
    final String TAG = "aka";
    LocationManager mLocationManager;
    LocationListener mLocationListener;

    // for check whether gps is on or off
    boolean gps_enabled = false;
    boolean network_enabled = false;


    public void hasActiveInternetConnection() {
        AsyncHttpClient check = new AsyncHttpClient();
        check.get("https://google.com", new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
                Log.i(TAG, "onStart: ");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
//                Log.i(TAG, "onSuccess: "+response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.i(TAG, "onFailure: " + statusCode + " " + Arrays.toString(errorResponse));
                AlertDialog.Builder builder = new AlertDialog.Builder(locationInput.this);
                builder.setMessage(R.string.no_internet)
                        .setCancelable(false)
                        .setPositiveButton(R.string.turn_wifi, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                            }
                        })
                        .setNegativeButton(R.string.cancel, null);
                AlertDialog alert = builder.create();
                alert.show();
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            }

        }
    }


    private void getWeatherForCurrentLocation() {

        LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        //noinspection CatchMayIgnoreException
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        //asking user to enable gps if not
        if(!gps_enabled && !network_enabled) {
            counter = -1;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.no_gps);
            builder.setPositiveButton(R.string.gps_settings, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    locationInput.this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            Log.i(TAG, "onCreate: enable gps");
            // notify user
        }

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
//                Log.i(TAG, "onLocationChanged: " + location.toString());
                latitude = (int) location.getLatitude();
                longitude = (int) location.getLongitude();
                if (longitude != 0 && latitude !=0){
                    Intent myIntent = new Intent(getBaseContext(),MainActivity.class);
                    myIntent.putExtra("long",longitude);
                    myIntent.putExtra("lat",latitude);
                    Log.i(TAG, "onLocationChanged: "+latitude+" "+longitude);
                    locationInput.this.startActivity(myIntent);

                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };



        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1000, mLocationListener);
        }
        if (counter == 0) {
            message = getString(R.string.city_identifying);
        } else if (counter > 0){
            message = getString(R.string.be_patient);
        }
        if (counter != -1){
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_input);

        // linking variables with layouts
        final EditText cityName = findViewById(R.id.cityname);
        Button gpsFinder = findViewById(R.id.gps);
        ImageButton swapButton = findViewById(R.id.swap);


        swapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(locationInput.this,MainActivity.class);
                locationInput.this.startActivity(sendIntent);
            }
        });

        gpsFinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getWeatherForCurrentLocation();

                counter ++;

                Log.i(TAG, "onClick: "+latitude);
                Log.i(TAG, "onClick: "+longitude);

            }
        });


        cityName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK ) {

                    System.exit(0);
                    return true;
                }
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String enteredCityName = cityName.getText().toString();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(cityName.getWindowToken(), 0);
                    Log.i(TAG, "onKey: " + enteredCityName);
                    Intent mIntent = new Intent(getApplicationContext(),MainActivity.class);
                    mIntent.putExtra("city",enteredCityName);
                    locationInput.this.startActivity(mIntent);
                    return true;

                }

                return false;
            }
        });
        //checking user's internet connection
        hasActiveInternetConnection();

    }

    //exits app when back key is pressed
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }



}
