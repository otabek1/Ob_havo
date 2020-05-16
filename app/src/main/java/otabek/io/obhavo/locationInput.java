package otabek.io.obhavo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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


public class locationInput extends AppCompatActivity {
    public int longitude;
    public int latitude;
    String message;
    int counter ;
    final String TAG = "aka";
    LocationManager mLocationManager;
    LocationListener mLocationListener;


    boolean gps_enabled = false;
    boolean network_enabled = false;






    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            }

        }
    }

    @SuppressWarnings("CatchMayIgnoreException")
    private void getWeatherForCurrentLocation() {

        LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        //noinspection CatchMayIgnoreException
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}


        if(!gps_enabled && !network_enabled) {
            counter = -1;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("GPS Yoqilmagan");
            builder.setPositiveButton("GPS sozlamalarni ochish", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    locationInput.this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            })
                    .setNegativeButton("Bekor qilish", null)
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
            message = "Shahar aniqlanmoqda. Bir oz sabr qiling . . .";
        } else if (counter > 0){
            message = "Sabr qil dedimku ey inson :-)";
        }
        if (counter != -1){
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_input);



        
        
        
        

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


    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            Log.i(TAG, "onKeyDown: ");
          finishAffinity();
          System.exit(0);

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}
