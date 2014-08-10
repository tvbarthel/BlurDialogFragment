package fr.tvbarthel.lib.blurdialogfragment.sample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import fr.tvbarthel.lib.blurdialogfragment.BlurDialogFragment;

/**
 * Simple fragment with blur effect behind.
 */
public class SampleDialogFragment extends BlurDialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.enableLog(true);
        this.setBlurRadius(4);
        this.setDownScaleFactor(5.0f);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View customView = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment, null);
        builder.setView(customView);
        return builder.create();
    }
}
