package fr.tvbarthel.lib.blurdialogfragment.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import fr.tvbarthel.lib.blurdialogfragment.BlurDialogFragment;


public class SampleActivity extends ActionBarActivity implements View.OnClickListener {

    /**
     * Seek bar used to change the blur radius.
     */
    SeekBar mBlurRadiusSeekbar;

    /**
     * TextView used to display the current blur radius.
     */
    TextView mBlurRadiusTextView;

    /**
     * Prefix used to explain blur radius.
     */
    String mBlurPrefix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        findViewById(R.id.button).setOnClickListener(this);
        mBlurRadiusTextView = ((TextView) findViewById(R.id.blurRadius));
        mBlurRadiusSeekbar = ((SeekBar) findViewById(R.id.blurRadiusSeekbar));

        setUpView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.actions_fullscreen) {
            startActivity(new Intent(this, SampleFullScreenActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                SampleDialogFragment fragment = new SampleDialogFragment();
                Bundle args = new Bundle();
                args.putInt(
                        BlurDialogFragment.BUNDLE_KEY_BLUR_RADIUS,
                        mBlurRadiusSeekbar.getProgress()
                );
                fragment.setArguments(args);
                fragment.show(getSupportFragmentManager(), "blur_sample");
                break;
            default:
                break;
        }
    }

    /**
     * Set up widgets.
     */
    private void setUpView() {

        mBlurRadiusSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mBlurRadiusTextView.setText(mBlurPrefix + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mBlurPrefix = getString(R.string.activity_sample_blur_radius);

        //set default blur radius to 8.
        mBlurRadiusSeekbar.setProgress(8);
    }
}
