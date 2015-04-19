package hu.ait.android.aloke.weatherapp.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import hu.ait.android.aloke.weatherapp.MainActivity;
import hu.ait.android.aloke.weatherapp.adapter.PlacesAutoCompleteAdapter;

/**
 * Created by Aloke on 4/19/15.
 */
public class SearchDialog extends DialogFragment {
    public static final String TAG = "SearchDialog";
    private PlacesAutoCompleteAdapter adapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Select a city");

        adapter = ((MainActivity) getActivity()).getAdapter();

        final AutoCompleteTextView tvCity = new AutoCompleteTextView(getActivity());
        tvCity.setAdapter(adapter);

        builder.setView(tvCity);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                String city = tvCity.getText().toString();
                if (!"".equals(city)) {
                    ((MainActivity) getActivity()).getWeather(city);
                }

            }
        });
        builder.setNegativeButton("Cancel", null);

        return builder.create();
    }
}
