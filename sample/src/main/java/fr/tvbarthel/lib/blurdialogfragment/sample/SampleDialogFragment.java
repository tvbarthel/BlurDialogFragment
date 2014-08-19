package fr.tvbarthel.lib.blurdialogfragment.sample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

import fr.tvbarthel.lib.blurdialogfragment.BlurDialogFragment;

/**
 * Simple fragment with blur effect behind.
 */
public class SampleDialogFragment extends BlurDialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.debug(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View customView = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment, null);
        TextView label = ((TextView) customView.findViewById(R.id.textView));
        label.setMovementMethod(LinkMovementMethod.getInstance());
        Linkify.addLinks(label, Linkify.WEB_URLS);
        builder.setView(customView);
        return builder.create();
    }
}
