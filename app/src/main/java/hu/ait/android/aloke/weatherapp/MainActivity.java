package hu.ait.android.aloke.weatherapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceTypes;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import hu.ait.android.aloke.weatherapp.adapter.PlacesAutoCompleteAdapter;
import hu.ait.android.aloke.weatherapp.fragment.SearchDialog;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    public static final String URL_BASE = "http://api.openweathermap.org/data/2.5/weather?q=%s&units=imperial";
    private static final String IMG_URL_BASE = "http://openweathermap.org/img/w/%s.png";
    private static final LatLngBounds BOUNDS_WORLD = new LatLngBounds(
            new LatLng(-90, -180), new LatLng(90, 180));

    private TextView tvTemp;
    private TextView tvLowTemp;
    private TextView tvHighTemp;

    private TextView tvHumidity;
    private TextView tvSunrise;
    private TextView tvSunset;

    private ImageView ivWeatherIcon;

    private Toolbar toolbarMain;

    private GoogleApiClient googleApiClient;
    private PlacesAutoCompleteAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbarMain = (Toolbar) findViewById(R.id.toolbarMain);
        toolbarMain.setTitle("");
        setSupportActionBar(toolbarMain);

        getWeather("Budapest");

        tvTemp = (TextView) findViewById(R.id.tvTemp);
        tvLowTemp = (TextView) findViewById(R.id.tvLowTemp);
        tvHighTemp = (TextView) findViewById(R.id.tvHighTemp);

        tvHumidity = (TextView) findViewById(R.id.tvHumidity);
        tvSunrise = (TextView) findViewById(R.id.tvSunrise);
        tvSunset = (TextView) findViewById(R.id.tvSunset);

        ivWeatherIcon = (ImageView) findViewById(R.id.ivWeatherIcon);

        if (googleApiClient == null) {
            rebuildGoogleApiClient();
        }

        //TODO: uncomment when Google fixes this bug:
        // https://github.com/googlesamples/android-play-places/issues/6
        Collection<Integer> filterTypes = new ArrayList<Integer>();
//        filterTypes.add(Place.TYPE_LOCALITY);
        adapter = new PlacesAutoCompleteAdapter(this, android.R.layout.simple_list_item_1, BOUNDS_WORLD, AutocompleteFilter.create(filterTypes));
    }

    private void rebuildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addConnectionCallbacks(this)
                .addApi(Places.GEO_DATA_API)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // subscrive to broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(brWeatherReceiver, new IntentFilter(GetWeather.FILTER_RESULT));
    }

    @Override
    protected void onPause() {
        super.onPause();

        // unsubscribe from the broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(brWeatherReceiver);
    }

    private BroadcastReceiver brWeatherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String rawResult = intent.getStringExtra(GetWeather.KEY_RESULT);

            try {
                JSONObject rawJson = new JSONObject(rawResult);

                String imgName = rawJson.getJSONArray("weather").getJSONObject(0).getString("icon");
                Glide.with(MainActivity.this).load(String.format(IMG_URL_BASE, imgName)).into(ivWeatherIcon);

                JSONObject mainTemp = rawJson.getJSONObject("main");
                JSONObject sys = rawJson.getJSONObject("sys");

                double temp = mainTemp.getDouble("temp");

                tvTemp.setText(mainTemp.getString("temp"));
                tvLowTemp.setText(mainTemp.getString("temp_min"));
                tvHighTemp.setText(mainTemp.getString("temp_max"));

                tvHumidity.setText(mainTemp.getString("humidity"));

                long unixSunrise = sys.getLong("sunrise");
                String sunsetText = getDateFromLong(unixSunrise);
                tvSunrise.setText(sunsetText);

                long unixSunset = sys.getLong("sunset");
                tvSunset.setText(getDateFromLong(unixSunset));

                //TODO: clean this up
                toolbarMain.setTitle(rawJson.getString("name"));
                toolbarMain.setSubtitle(rawJson.getJSONArray("weather").getJSONObject(0).getString("description"));
                setToolbarColor(temp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private String getDateFromLong(long unixSunrise) {
        Date sunriseDate = new Date(unixSunrise * 1000);

        DateFormat df = new SimpleDateFormat("HH:mm");
        df.setTimeZone(TimeZone.getDefault());

        // representation of a date with the defined format.
        return df.format(sunriseDate);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_search:
                launchSearchDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void launchSearchDialog() {
        SearchDialog dialog = new SearchDialog();
        dialog.show(getSupportFragmentManager(), SearchDialog.TAG);
    }


    private void setToolbarColor(double temp) {
        int color;
        if (temp < 0) {
            color = getResources().getColor(R.color.primary_indigo);
        } else if (temp < 10) {
            color = getResources().getColor(R.color.primary_blue);
        } else if (temp < 20) {
            color = getResources().getColor(R.color.primary_light_blue);
        } else if (temp < 30) {
            color = getResources().getColor(R.color.primary_teal);
        } else if (temp < 40) {
            color = getResources().getColor(R.color.primary_light_green);
        } else if (temp < 50) {
            color = getResources().getColor(R.color.primary_green);
        } else if (temp < 60) {
            color = getResources().getColor(R.color.primary_lime);
        } else if (temp < 70) {
            color = getResources().getColor(R.color.primary_yellow);
        } else if (temp < 80) {
            color = getResources().getColor(R.color.primary_amber);
        } else if (temp < 90) {
            color = getResources().getColor(R.color.primary_orange);
        } else {
            color = getResources().getColor(R.color.primary_red);
        }

        toolbarMain.setBackgroundColor(color);

    }

    public void getWeather(String city) {
        String url = null;
        try {
            url = String.format(URL_BASE, URLEncoder.encode(city, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        AsyncTask<String, Void, String> getWeather = new GetWeather(MainActivity.this);
        getWeather.execute(url);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();

        // Disable API access in the adapter because the client was not initialised correctly.
        adapter.setGoogleApiClient(null);
    }

    @Override
    public void onConnected(Bundle bundle) {
        adapter.setGoogleApiClient(googleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        adapter.setGoogleApiClient(null);
    }

    public PlacesAutoCompleteAdapter getAdapter() {
        return adapter;
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }
}


