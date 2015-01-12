package fr.tvbarthel.lib.blurdialogfragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.WindowManager;

/**
 * Encapsulate dialog behavior with blur effect for
 * app using {@link android.support.v4.app.DialogFragment}.
 * <p/>
 * All the screen behind the dialog will be blurred except the action bar.
 */
public abstract class SupportBlurDialogFragment extends DialogFragment {

    /**
     * Log cat
     */
    private static final String TAG = SupportBlurDialogFragment.class.getSimpleName();

    /**
     * Engine used to blur.
     */
    private BlurDialogEngine mBlurEngine;

    /**
     * Dimming policy.
     */
    private boolean mDimmingEffect;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBlurEngine = new BlurDialogEngine(getActivity());

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

        mBlurEngine.debug(isDebugEnable());

        mDimmingEffect = isDimmingEnable();
    }

    @Override
    public void onStart() {
        Dialog dialog = getDialog();
        if (!mDimmingEffect && dialog != null) {
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
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
     * Add in and out animations on your dialog according to the res id style.
     * <p/>
     * Use resId = -1 for default animation.
     * <p/>
     * To provide a custom one, simple define a new style in your style.xml :
     * <p/>
     * <code>
     * <style name="BlurDialogFragment.Default.Animation" parent="@android:style/Animation.Activity">
     * <item name="android:windowEnterAnimation">@anim/custom_dialog_in</item>
     * <item name="android:windowExitAnimation">@anim/custom_dialog_out</item>
     * </style>
     * </code>
     *
     * @param dialog dialog on which animations will be applied
     * @param resId  res id of your animation style, or -1 for default one.
     */
    protected void addAnimations(Dialog dialog, int resId) {
        int style = resId;
        if (resId == -1) {
            style = R.style.BlurDialogFragment_Default_Animation;
        }
        dialog.getWindow().getAttributes().windowAnimations = style;
    }
}
