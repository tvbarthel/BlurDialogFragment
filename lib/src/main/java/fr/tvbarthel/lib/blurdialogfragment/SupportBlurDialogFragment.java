package fr.tvbarthel.lib.blurdialogfragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;

/**
 * Encapsulate dialog behavior with blur effect for
 * app using {@link android.support.v4.app.DialogFragment}.
 * <p/>
 * All the screen behind the dialog will be blurred except the action bar.
 */
public abstract class SupportBlurDialogFragment extends DialogFragment {

    /**
     * Engine used to blur.
     */
    private BlurDialogEngine mBlurEngine;

    /**
     * Allow to set a Toolbar which isn't set as actionbar.
     */
    private Toolbar mToolbar;

    /**
     * Dimming policy.
     */
    private boolean mDimmingEffect;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBlurEngine = new BlurDialogEngine(getActivity());

        if (mToolbar != null) {
            mBlurEngine.setToolbar(mToolbar);
        }

        int radius = getBlurRadius();
        if (radius <= 0) {
            throw new IllegalArgumentException("Blur radius must be strictly positive. Found : " + radius);
        }
        mBlurEngine.setBlurRadius(radius);

        float factor = getDownScaleFactor();
        if (factor <= 1.0) {
            throw new IllegalArgumentException("Down scale must be strictly greater than 1.0. Found : " + factor);
        }
        mBlurEngine.setDownScaleFactor(factor);

        mBlurEngine.setUseRenderScript(isRenderScriptEnable());

        mBlurEngine.debug(isDebugEnable());

        mBlurEngine.setBlurActionBar(isActionBarBlurred());

        mDimmingEffect = isDimmingEnable();
    }

    @Override
    public void onStart() {
        Dialog dialog = getDialog();
        if (dialog != null) {

            // enable or disable dimming effect.
            if (!mDimmingEffect) {
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }

            // add default fade to the dialog if no window animation has been set.
            int currentAnimation = dialog.getWindow().getAttributes().windowAnimations;
            if (currentAnimation == 0) {
                dialog.getWindow().getAttributes().windowAnimations
                        = R.style.BlurDialogFragment_Default_Animation;
            }
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mBlurEngine.onResume(getRetainInstance());
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mBlurEngine.onDismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBlurEngine.onDestroy();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    /**
     * Allow to set a Toolbar which isn't set as ActionBar.
     * <p/>
     * Must be called before onCreate.
     *
     * @param toolBar toolBar
     */
    public void setToolbar(Toolbar toolBar) {
        mToolbar = toolBar;
        if (mBlurEngine != null) {
            mBlurEngine.setToolbar(toolBar);
        }
    }

    /**
     * For inheritance purpose.
     * <p/>
     * Enable or disable debug mode.
     *
     * @return true if debug mode should be enabled.
     */
    protected boolean isDebugEnable() {
        return BlurDialogEngine.DEFAULT_DEBUG_POLICY;
    }

    /**
     * For inheritance purpose.
     * <p/>
     * Allow to customize the down scale factor.
     * <p/>
     * The factor down scaled factor used to reduce the size of the source image.
     * Range :  ]1.0,infinity)
     *
     * @return customized down scaled factor.
     */
    protected float getDownScaleFactor() {
        return BlurDialogEngine.DEFAULT_BLUR_DOWN_SCALE_FACTOR;
    }

    /**
     * For inheritance purpose.
     * <p/>
     * Allow to customize the blur radius factor.
     * <p/>
     * radius down scaled factor used to reduce the size of the source image.
     * Range :  [1,infinity)
     *
     * @return customized blur radius.
     */
    protected int getBlurRadius() {
        return BlurDialogEngine.DEFAULT_BLUR_RADIUS;
    }

    /**
     * For inheritance purpose.
     * <p/>
     * Enable or disable the dimming effect.
     * <p/>
     * Disabled by default.
     *
     * @return enable true to enable the dimming effect.
     */
    protected boolean isDimmingEnable() {
        return BlurDialogEngine.DEFAULT_DIMMING_POLICY;
    }

    /**
     * For inheritance purpose.
     * <p/>
     * Enable or disable the blur effect on the action bar.
     * <p/>
     * Disable by default.
     *
     * @return true to enable the blur effect on the action bar.
     */
    protected boolean isActionBarBlurred() {
        return BlurDialogEngine.DEFAULT_ACTION_BAR_BLUR;
    }

    /**
     * For inheritance purpose.
     * <p/>
     * Enable or disable RenderScript.
     * <p/>
     * Disable by default.
     *
     * @return true to enable RenderScript.
     */
    protected boolean isRenderScriptEnable() {
        return BlurDialogEngine.DEFAULT_USE_RENDERSCRIPT;
    }

}
