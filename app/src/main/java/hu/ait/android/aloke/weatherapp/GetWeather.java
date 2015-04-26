package hu.ait.android.aloke.weatherapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Aloke on 4/18/15.
 */
public class GetWeather extends AsyncTask<String, Void, String> {
    public static final String FILTER_RESULT = "FILTER_RESULT";
    public static final String KEY_RESULT = "KEY_RESULT";

    private Context context;

    public GetWeather(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        String result = "";

        HttpURLConnection connection = null;
        InputStream is = null;

        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();

            setTimeoutValues(connection);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                is = connection.getInputStream();
                result = getResultFromConnection(is);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                connection.disconnect();
            }
        }

        return result;
    }

    private String getResultFromConnection(InputStream is) throws IOException {
        int ch;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        while ((ch = is.read()) != -1) {
            bos.write(ch);
        }

        return new String(bos.toByteArray(), "UTF-8");
    }

    private void setTimeoutValues(HttpURLConnection connection) {
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
    }

    @Override
    protected void onPostExecute(String result) {
        Intent intentBrResult = new Intent(FILTER_RESULT);
        intentBrResult.putExtra("KEY_RESULT", result);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intentBrResult);
    }
}
