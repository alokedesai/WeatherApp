package hu.ait.android.aloke.weatherapp.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;

import hu.ait.android.aloke.weatherapp.MainActivity;

/**
 * Created by Aloke on 4/19/15.
 */
public class SearchDialog extends DialogFragment {
    public static final String TAG = "SearchDialog";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Select a city");

        final EditText etCity = new EditText(getActivity());
        builder.setView(etCity);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                String city = etCity.getText().toString();
                if (!"".equals(city)) {
                    ((MainActivity) getActivity()).getWeather(city);
                }

            }
        });
        builder.setNegativeButton("Cancel", null);

        return builder.create();
    }
}
