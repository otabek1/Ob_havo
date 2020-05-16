package otabek.io.obhavo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    String cityname;

    final String TAG = "aka";
    int latitude;
    int longitude;
    String API_KEY = "0674092fb1dd45d88c6da2b68b4f37c9";
    String url = "https://api.weatherbit.io/v2.0/forecast/daily?";

    private void iconSetter(int[] codes) {
        String[] iconNames = {" ", " ", " ", " ", " ", " ", " "};
        for (int i = 0; i < codes.length; i++) {
            if (codes[i] >= 200 & codes[i] <= 233) {
                iconNames[i] = "ic_lightning";
            }
            if (codes[i] >= 300 && codes[i] <= 522) {
                iconNames[i] = "ic_rainy";
            }
            if (codes[i] >= 611 && codes[i] <= 612) {
                iconNames[i] = "ic_sleet";
            }
            if (codes[i] >= 600 && codes[i] <= 623) {
                iconNames[i] = "ic_snowing";
            }
            if (codes[i] >= 700 && codes[i] <= 751) {
                iconNames[i] = "ic_foggy";
            }
            if (codes[i] >= 800 && codes[i] < 803) {
                iconNames[i] = "ic_sunny";
            }
            if (codes[i] >= 803 && codes[i] <= 804) {
                iconNames[i] = "ic_partcloudy";
            }
        }

        ImageView[] icons = {findViewById(R.id.icon),
                findViewById(R.id.icon1),
                findViewById(R.id.icon2),
                findViewById(R.id.icon3),
                findViewById(R.id.icon4),
                findViewById(R.id.icon5),
                findViewById(R.id.icon6)
        };

        for (int i = 0; i < icons.length; i++) {
            int resId = getResources().getIdentifier(iconNames[i], "drawable", getPackageName());
            icons[i].setImageResource(resId);
        }

    }


    private void tempSetter(int[] codes) {
        TextView[] weatherTemps = {
                findViewById(R.id.temp),
                findViewById(R.id.temp1),
                findViewById(R.id.temp2),
                findViewById(R.id.temp3),
                findViewById(R.id.temp4),
                findViewById(R.id.temp5),
                findViewById(R.id.temp6),
        };

        for (int i = 0; i < codes.length; i++) {
            weatherTemps[i].setText(Integer.toString(codes[i]) + "°");
        }

    }

    private void isWindy(JSONObject response) throws JSONException {
        int windSpeed = ((int) response.getJSONArray("data").getJSONObject(0).getDouble("wind_spd"));
        if (windSpeed > 8) {
            updateBackgroundPhoto(999);
        }

    }

    private void updateBackgroundPhoto(int weatherCode) {
        String backPhotoName = null;


        Random rand = new Random();
        String randomNumber = String.valueOf(rand.nextInt(3));
        if (weatherCode >= 202 && weatherCode <= 522) {
            backPhotoName = "rain";
        }
        if (weatherCode >= 800 && weatherCode <= 804) {
            backPhotoName = "sunny";
        }
        if (weatherCode >= 600 && weatherCode <= 623) {
            backPhotoName = "snow";
        }
        if (weatherCode >= 700 && weatherCode <= 751) {
            backPhotoName = "foggy";
        }
        if (weatherCode == 999) {
            backPhotoName = "wind" + randomNumber;
        }

        int te = getResources().getIdentifier(backPhotoName, "drawable", getPackageName());
        ConstraintLayout constraintLayout = findViewById(R.id.main);
        constraintLayout.setBackgroundResource(te);


    }

    public void jsonParser(JSONObject response) throws JSONException {
        TextView cityNameView = findViewById(R.id.cityName);
        cityNameView.setText(cityname);
        String cityNameFromJson = response.getString("city_name");
        cityNameView.setText(cityNameFromJson);

        int[] codes = {0, 0, 0, 0, 0, 0, 0};
        int[] weatherDegrees = {0, 0, 0, 0, 0, 0, 0};
        int todays_code = response.getJSONArray("data").getJSONObject(0).getJSONObject("weather").getInt("code");
        codes[0] = todays_code;
        updateBackgroundPhoto(todays_code);
        for (int i = 1; i < 7; i++) {
            codes[i] = response.getJSONArray("data").getJSONObject(i).getJSONObject("weather").getInt("code");
            weatherDegrees[i] = (int) response.getJSONArray("data").getJSONObject(i).getDouble("temp");
        }
        Log.i(TAG, "jsonParser: " + Arrays.toString(codes));
        iconSetter(codes);
        tempSetter(weatherDegrees);


    }


    private void byCityName(String cityNamePassed) {
        RequestParams params = new RequestParams();
        params.put("city", cityNamePassed);
        params.put("key", API_KEY);
        Log.i(TAG, "byCityName passed: " + cityNamePassed);
        hasActiveInternetConnection();
        getWeatherInfo(params);

    }

    private void byCordinates(int lat, int lon) {
        RequestParams params = new RequestParams();
        params.put("lat", lat);
        params.put("lon", lon);
        params.put("days", 7);
        params.put("key", API_KEY);
        hasActiveInternetConnection();
        getWeatherInfo(params);
        Log.i(TAG, "byCordinates got: " + lat + " " + lon);
    }

    public void getWeatherInfo(RequestParams params) {
        Log.i(TAG, "getWeatherInfo: ");
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
                Log.i(TAG, "onStart: ");
            }


            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // called when response HTTP status is "200 OK"
//                Log.i(TAG, "onSuccess: "+response.toString());
                try {
                    jsonParser(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    int code = response.getJSONArray("data").getJSONObject(0).getJSONObject("weather").getInt("code");
                    Log.i(TAG, "onSucces1s" + code);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Xatolik yuz berdi. Qayta urinib ko'ring!", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "onSuccess2: " + e);
                }
            }


            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });


    }


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
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Internet mavjud emas")
                        .setCancelable(false)
                        .setPositiveButton("Wi Fi ni yoqish", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                            }
                        })
                        .setNegativeButton("Bekor qilish", null);
                AlertDialog alert = builder.create();
                alert.show();
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }


    public String Capitalize(String lowerCaseDayOfTheWeek) {
        return lowerCaseDayOfTheWeek.substring(0, 1).toUpperCase() + lowerCaseDayOfTheWeek.substring(1).toLowerCase();
    }


    public String dayAdder(int numberOfdaysToadd) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Date dt = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.add(Calendar.DATE, numberOfdaysToadd);
        dt = c.getTime();


        String dayOfTheWeek = sdf.format(dt);
        return Capitalize(dayOfTheWeek);

    }


    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("city", cityname);
        editor.putInt("lat", latitude);
        editor.putInt("lon", longitude);
        editor.commit();
//        Log.i(TAG, "onPause: ");

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent getIntent = getIntent();
        cityname = getIntent.getStringExtra("city");
        if (cityname == null) {
            latitude = getIntent.getIntExtra("lat", 0);
            longitude = getIntent.getIntExtra("long", 0);
            Log.i(TAG, "onCreate: " + latitude + " " + longitude);

            if (latitude == 0 || longitude == 0) {
                SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                cityname = sharedPref.getString("city", "0");
                latitude = sharedPref.getInt("lat", 0);
                longitude = sharedPref.getInt("lon", 0);
                if (latitude != 0) {
                    byCordinates(latitude, longitude);
                }

                if (cityname.equals("0") && latitude == 0 && longitude == 0) {
                    Intent sendIntent = new Intent(MainActivity.this, locationInput.class);
                    MainActivity.this.startActivity(sendIntent);
                }

            } else {
                byCordinates(latitude, longitude);
            }
            Log.i(TAG, "latitude " + latitude + " longitude" + longitude);

        } else {
            byCityName(cityname);
        }


//        byCityName(cityname);

//            Log.i(TAG, "onCreate: " + cityname);


        setContentView(R.layout.activity_main);


        TextView cityName = findViewById(R.id.cityname);
        TextView mainTemp = findViewById(R.id.temperature);

        ImageButton changeCityButton = findViewById(R.id.changecity);

        TextView cityNameView = findViewById(R.id.cityName);
        TextView day = findViewById(R.id.day);
        day.setText(dayAdder(0));
        TextView day1 = findViewById(R.id.day1);
        day1.setText(dayAdder(1));
        TextView day2 = findViewById(R.id.day2);
        day2.setText(dayAdder(2));
        TextView day3 = findViewById(R.id.day3);
        day3.setText(dayAdder(3));
        TextView day4 = findViewById(R.id.day4);
        day4.setText(dayAdder(4));
        TextView day5 = findViewById(R.id.day5);
        day5.setText(dayAdder(5));
        TextView day6 = findViewById(R.id.day6);
        day6.setText(dayAdder(6));

        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(MainActivity.this, locationInput.class);
                MainActivity.this.startActivity(sendIntent);
            }
        });


        //Networking


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            this.finishAffinity();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}

