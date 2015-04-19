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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends ActionBarActivity {
    public static final String URL_BASE = "http://api.openweathermap.org/data/2.5/weather?q=%s&units=imperial";

    private TextView tvLowTemperature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbarMain = (Toolbar) findViewById(R.id.toolbarMain);
        toolbarMain.setTitle("");
        setSupportActionBar(toolbarMain);
//        final EditText etCityName = (EditText) findViewById(R.id.etCityName);
//        Button btnGetWeather = (Button) findViewById(R.id.btnGetWeather);
//        tvLowTemperature = (TextView) findViewById(R.id.tvLowTemperate);
//
//        btnGetWeather.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // start the async task to get more the weather
//                if (!"".equals(etCityName.getText().toString())) {
//                    String url = String.format(URL_BASE, etCityName.getText());
//
//                    AsyncTask<String, Void, String> getWeather = new GetWeather(MainActivity.this);
//                    getWeather.execute(url);
//                }
//
//            }
//        });

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
                String minTemp = rawJson.getJSONObject("main").getString("temp_min");

                tvLowTemperature.setText(minTemp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
