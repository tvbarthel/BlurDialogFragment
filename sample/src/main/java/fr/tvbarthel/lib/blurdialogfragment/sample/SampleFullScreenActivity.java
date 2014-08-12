package fr.tvbarthel.lib.blurdialogfragment.sample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;


public class SampleFullScreenActivity extends ActionBarActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        findViewById(R.id.button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                new SampleDialogFragment().show(getSupportFragmentManager(), "blur_sample");
                break;
            default:
                break;
        }
    }
}
