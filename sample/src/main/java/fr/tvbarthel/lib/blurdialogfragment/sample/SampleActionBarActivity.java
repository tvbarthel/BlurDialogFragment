package fr.tvbarthel.lib.blurdialogfragment.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;


public class SampleActionBarActivity extends ActionBarActivity implements View.OnClickListener {

    /**
     * Seek bar used to change the blur radius.
     */
    private SeekBar mBlurRadiusSeekbar;

    /**
     * TextView used to display the current blur radius.
     */
    private TextView mBlurRadiusTextView;

    /**
     * Prefix used to explain blur radius.
     */
    private String mBlurPrefix;

    /**
     * Seek bar used to change the down scale factor.
     */
    private SeekBar mDownScaleFactorSeekbar;

    /**
     * TextView used to display the current down scale factor.
     */
    private TextView mDownScaleFactorTextView;

    /**
     * Checkbox used to enable or disable debug mode.
     */
    private CheckBox mDebugMode;

    /**
     * Prefix used to explain down scale factor.
     */
    private String mDownScalePrefix;

    /**
     * Checkbox used to enable / disable dimming effect.
     */
    private CheckBox mDimmingEnable;

    /**
     * Checkbox used to enable / disable blur effect on action bar.
     */
    private CheckBox mBlurredActionBar;

    /**
     * Checkbox used to enable / disable use of RenderScript
     */
    private CheckBox mUseRenderScript;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        findViewById(R.id.button).setOnClickListener(this);
        mBlurRadiusTextView = ((TextView) findViewById(R.id.blurRadius));
        mBlurRadiusSeekbar = ((SeekBar) findViewById(R.id.blurRadiusSeekbar));
        mDownScaleFactorTextView = ((TextView) findViewById(R.id.downScalefactor));
        mDownScaleFactorSeekbar = ((SeekBar) findViewById(R.id.downScaleFactorSeekbar));
        mDebugMode = ((CheckBox) findViewById(R.id.debugMode));
        mDimmingEnable = ((CheckBox) findViewById(R.id.dimmingEnable));
        mBlurredActionBar = ((CheckBox) findViewById(R.id.blur_actionbar_enable));
        mUseRenderScript = ((CheckBox) findViewById(R.id.userendercript));

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
                SampleSupportDialogFragment fragment
                        = SampleSupportDialogFragment.newInstance(
                        mBlurRadiusSeekbar.getProgress() + 1,
                        (mDownScaleFactorSeekbar.getProgress() / 10f) + 2,
                        mDimmingEnable.isChecked(),
                        mDebugMode.isChecked(),
                        mBlurredActionBar.isChecked(),
                        mUseRenderScript.isChecked()
                );
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
                mBlurRadiusTextView.setText(mBlurPrefix + (progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mDownScaleFactorSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mDownScaleFactorTextView.setText(mDownScalePrefix + (progress / 10f + 2));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mBlurPrefix = getString(R.string.activity_sample_blur_radius);
        mDownScalePrefix = getString(R.string.activity_sample_down_scale_factor);

        //set default blur radius to 8.
        mBlurRadiusSeekbar.setProgress(7);
        mDownScaleFactorSeekbar.setProgress(20);
    }
}
