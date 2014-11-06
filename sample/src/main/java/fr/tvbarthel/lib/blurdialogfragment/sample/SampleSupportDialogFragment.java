package fr.tvbarthel.lib.blurdialogfragment.sample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

import fr.tvbarthel.lib.blurdialogfragment.SupportBlurDialogFragment;

/**
 * Simple fragment with blur effect behind.
 */
public class SampleSupportDialogFragment extends SupportBlurDialogFragment {

    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment, null);
        TextView label = ((TextView) view.findViewById(R.id.textView));
        label.setMovementMethod(LinkMovementMethod.getInstance());
        Linkify.addLinks(label, Linkify.WEB_URLS);
        builder.setView(view);
        return builder.create();
    }

}
