package hu.ait.android.aloke.weatherapp.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import hu.ait.android.aloke.weatherapp.MainActivity;
import hu.ait.android.aloke.weatherapp.R;
import hu.ait.android.aloke.weatherapp.adapter.PlacesAutoCompleteAdapter;

public class SearchDialog extends DialogFragment {
    public static final String TAG = "SearchDialog";

    private PlacesAutoCompleteAdapter adapter;
    private GoogleApiClient googleApiClient;

    private AlertDialog dialog;
    private LatLng cityCoordinates;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.select_a_place_dialog_text);

        adapter = ((MainActivity) getActivity()).getAdapter();
        googleApiClient = ((MainActivity) getActivity()).getGoogleApiClient();

        final AutoCompleteTextView tvCity = new AutoCompleteTextView(getActivity());
        tvCity.setAdapter(adapter);

        setTvCityListener(tvCity);
        builder.setView(tvCity);

        builder.setPositiveButton(getString(R.string.ok_dialog_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                String city = tvCity.getText().toString();
                if (!"".equals(city)) {
                    ((MainActivity) getActivity()).getWeatherFromLatLng(cityCoordinates);
                }

            }
        });
        builder.setNegativeButton(getString(R.string.cancel_dialog_text), null);


        dialog =  builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });

        return dialog;
    }

    private void setTvCityListener(AutoCompleteTextView tvCity) {
        tvCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final PlacesAutoCompleteAdapter.PlaceAutocomplete item = adapter.getItem(position);
                final String placeId = String.valueOf(item.placeId);

                // Issue a request to the Places Geo Data API to retrieve a Place object
                // with additional details about the place.
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(googleApiClient, placeId);

                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (places.getStatus().isSuccess()) {
                            final Place place = places.get(0);
                            cityCoordinates = place.getLatLng();

                        } else {
                            dialog.dismiss();
                            Toast.makeText(getActivity(), "There was an error, please try again", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        places.release();
                    }
                });

            }
        });
    }
}
