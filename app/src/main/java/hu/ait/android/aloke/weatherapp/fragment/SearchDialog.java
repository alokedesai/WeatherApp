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
import hu.ait.android.aloke.weatherapp.adapter.PlacesAutoCompleteAdapter;

/**
 * Created by Aloke on 4/19/15.
 */
public class SearchDialog extends DialogFragment {
    public static final String TAG = "SearchDialog";

    private PlacesAutoCompleteAdapter adapter;
    private GoogleApiClient googleApiClient;

    // the dialog that is created in onCreateDialog
    private AlertDialog dialog;

    // the coordinates of the city that was chosen
    private LatLng cityCoordinates;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Select a place");

        adapter = ((MainActivity) getActivity()).getAdapter();
        googleApiClient = ((MainActivity) getActivity()).getGoogleApiClient();

        final AutoCompleteTextView tvCity = new AutoCompleteTextView(getActivity());
        tvCity.setAdapter(adapter);

        tvCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final PlacesAutoCompleteAdapter.PlaceAutocomplete item = adapter.getItem(position);
                final String placeId = String.valueOf(item.placeId);
                Log.i(TAG, "Autocomplete item selected: " + item.description);

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
                            // dismiss the dialog, the attempt at getting the coordinates of
                            // the place was unsuccessful
                            dialog.dismiss();
                            Toast.makeText(getActivity(), "There was an error, please try again", Toast.LENGTH_SHORT).show();
                            return;

                        }
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        places.release();
                    }
                });

                Toast.makeText(getActivity(), "Clicked: " + item.description,
                        Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Called getPlaceById to get Place details for " + item.placeId);
            }
        });

        builder.setView(tvCity);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                String city = tvCity.getText().toString();
                if (!"".equals(city)) {
                    ((MainActivity) getActivity()).getWeatherFromLatLng(cityCoordinates);
                }

            }
        });
        builder.setNegativeButton("Cancel", null);


        dialog =  builder.create();

        // Disable the ok button when the dialog is created. It's only able to be
        // clicked when the coordinates are successfully reached
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });

        return dialog;
    }
}
