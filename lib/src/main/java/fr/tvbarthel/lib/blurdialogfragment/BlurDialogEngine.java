package fr.tvbarthel.lib.blurdialogfragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Encapsulate the whole behaviour to provide a blur effect on a DialogFragment.
 * <p/>
 * All the screen behind the dialog will be blurred except the action bar.
 * <p/>
 * Simply linked all methods to the matching lifecycle ones.
 */
class BlurDialogEngine {

    /**
     * Since image is going to be blurred, we don't care about resolution.
     * Down scale factor to reduce blurring time and memory allocation.
     */
    static final float DEFAULT_BLUR_DOWN_SCALE_FACTOR = 4.0f;

    /**
     * Radius used to blur the background
     */
    static final int DEFAULT_BLUR_RADIUS = 8;

    /**
     * Default dimming policy.
     */
    static final boolean DEFAULT_DIMMING_POLICY = false;

    /**
     * Default debug policy.
     */
    static final boolean DEFAULT_DEBUG_POLICY = false;

    /**
     * Default action bar blurred policy.
     */
    static final boolean DEFAULT_ACTION_BAR_BLUR = false;

    /**
     * Default use of RenderScript.
     */
    static final boolean DEFAULT_USE_RENDERSCRIPT = false;

    /**
     * Log cat
     */
    private static final String TAG = BlurDialogEngine.class.getSimpleName();

    /**
     * Image view used to display blurred background.
     */
    private ImageView mBlurredBackgroundView;

    /**
     * Layout params used to add blurred background.
     */
    private FrameLayout.LayoutParams mBlurredBackgroundLayoutParams;

    /**
     * Task used to capture screen and blur it.
     */
    private BlurAsyncTask mBluringTask;

    /**
     * Used to enable or disable debug mod.
     */
    private boolean mDebugEnable = false;

    /**
     * Factor used to down scale background. High quality isn't necessary
     * since the background will be blurred.
     */
    private float mDownScaleFactor = DEFAULT_BLUR_DOWN_SCALE_FACTOR;

    /**
     * Radius used for fast blur algorithm.
     */
    private int mBlurRadius = DEFAULT_BLUR_RADIUS;

    /**
     * Holding activity.
     */
    private Activity mHoldingActivity;

    /**
     * Allow to use a toolbar without set it as action bar.
     */
    private Toolbar mToolbar;

    /**
     * Duration used to animate in and out the blurred image.
     * <p/>
     * In milli.
     */
    private int mAnimationDuration;

    /**
     * Boolean used to know if the actionBar should be blurred.
     */
    private boolean mBlurredActionBar;

    /**
     * Boolean used to know if RenderScript should be used
     */
    private boolean mUseRenderScript;


    /**
     * Constructor.
     *
     * @param holdingActivity activity which holds the DialogFragment.
     */
    public BlurDialogEngine(Activity holdingActivity) {
        mHoldingActivity = holdingActivity;
        mAnimationDuration = holdingActivity.getResources().getInteger(R.integer.blur_dialog_animation_duration);
    }

    /**
     * Resume the engine.
     *
     * @param retainedInstance use getRetainInstance.
     */
    public void onResume(boolean retainedInstance) {
        if (mBlurredBackgroundView == null || retainedInstance) {
            mBluringTask = new BlurAsyncTask();
            mBluringTask.execute();
        }
    }

    /**
     * Must be linked to the original lifecycle.
     */
    public void onDismiss() {
        //remove blurred background and clear memory, could be null if dismissed before blur effect
        //processing ends
        if (mBlurredBackgroundView != null) {
            mBlurredBackgroundView
                    .animate()
                    .alpha(0f)
                    .setDuration(mAnimationDuration)
                    .setInterpolator(new AccelerateInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            removeBlurredView();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            super.onAnimationCancel(animation);
                            removeBlurredView();
                        }
                    }).start();
        }

        //cancel async task
        mBluringTask.cancel(true);
        mBluringTask = null;
    }

    /**
     * Must be linked to the original lifecycle.
     */
    public void onDestroy() {
        mHoldingActivity = null;
    }

    /**
     * Enable / disable debug mode.
     * <p/>
     * LogCat and graphical information directly on blurred screen.
     *
     * @param enable true to display log in LogCat.
     */
    public void debug(boolean enable) {
        mDebugEnable = enable;
    }

    /**
     * Apply custom down scale factor.
     * <p/>
     * By default down scale factor is set to
     * {@link BlurDialogEngine#DEFAULT_BLUR_DOWN_SCALE_FACTOR}
     * <p/>
     * Higher down scale factor will increase blurring speed but reduce final rendering quality.
     *
     * @param factor customized down scale factor, must be at least 1.0 ( no down scale applied )
     */
    public void setDownScaleFactor(float factor) {
        if (factor >= 1.0f) {
            mDownScaleFactor = factor;
        } else {
            mDownScaleFactor = 1.0f;
        }
    }

    /**
     * Apply custom blur radius.
     * <p/>
     * By default blur radius is set to
     * {@link BlurDialogEngine#DEFAULT_BLUR_RADIUS}
     *
     * @param radius custom radius used to blur.
     */
    public void setBlurRadius(int radius) {
        if (radius >= 0) {
            mBlurRadius = radius;
        } else {
            mBlurRadius = 0;
        }
    }

    /**
     * Set use of RenderScript
     * <p/>
     * By default RenderScript is set to
     * {@link BlurDialogEngine#DEFAULT_USE_RENDERSCRIPT}
     *
     * @param useRenderScript use of RenderScript
     */

    public void setUseRenderScript(boolean useRenderScript) {
        mUseRenderScript = useRenderScript;
    }

    /**
     * Enable / disable blurred action bar.
     * <p/>
     * When enabled, the action bar is blurred in addition of the content.
     *
     * @param enable true to blur the action bar.
     */
    public void setBlurActionBar(boolean enable) {
        mBlurredActionBar = enable;
    }

    /**
     * Set a toolbar which isn't set as action bar.
     *
     * @param toolbar toolbar.
     */
    public void setToolbar(Toolbar toolbar) {
        mToolbar = toolbar;
    }

    /**
     * Blur the given bitmap and add it to the activity.
     *
     * @param bkg  should be a bitmap of the background.
     * @param view background view.
     */
    private void blur(Bitmap bkg, View view) {
        long startMs = System.currentTimeMillis();
        //define layout params to the previous imageView in order to match its parent
        mBlurredBackgroundLayoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );

        //overlay used to build scaled preview and blur background
        Bitmap overlay = null;

        //evaluate top offset due to action bar, 0 if the actionBar should be blurred.
        int actionBarHeight;
        if (mBlurredActionBar) {
            actionBarHeight = 0;
        } else {
            actionBarHeight = getActionBarHeight();
        }

        //evaluate top offset due to status bar
        int statusBarHeight = 0;
        if ((mHoldingActivity.getWindow().getAttributes().flags
                & WindowManager.LayoutParams.FLAG_FULLSCREEN) == 0) {
            //not in fullscreen mode
            statusBarHeight = getStatusBarHeight();
        }

        final int topOffset = actionBarHeight + statusBarHeight;

        // evaluate bottom or right offset due to navigation bar.
        int bottomOffset = 0;
        int rightOffset = 0;
        final int navBarSize = getNavigationBarOffset();
        if (mHoldingActivity.getResources().getBoolean(R.bool.blur_dialog_has_bottom_navigation_bar)) {
            bottomOffset = navBarSize;
        } else {
            rightOffset = navBarSize;
        }


        //add offset to the source boundaries since we don't want to blur actionBar pixels
        Rect srcRect = new Rect(
                0,
                topOffset,
                bkg.getWidth() - rightOffset,
                bkg.getHeight() - bottomOffset
        );

        //in order to keep the same ratio as the one which will be used for rendering, also
        //add the offset to the overlay.
        double height = Math.ceil((view.getHeight() - topOffset - bottomOffset) / mDownScaleFactor);
        double width = Math.ceil(((view.getWidth() - rightOffset) * height
                / (view.getHeight() - topOffset - bottomOffset)));
        overlay = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.RGB_565);

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                    || mHoldingActivity instanceof ActionBarActivity) {
                //add offset as top margin since actionBar height must also considered when we display
                // the blurred background. Don't want to draw on the actionBar.

                mBlurredBackgroundLayoutParams.setMargins(0, actionBarHeight, 0, 0);
                mBlurredBackgroundLayoutParams.gravity = Gravity.TOP;
            }
        } catch (NoClassDefFoundError e) {
            // no dependency to appcompat, that means no additional top offset due to actionBar.
            mBlurredBackgroundLayoutParams.setMargins(0, 0, 0, 0);
        }

        // check if status bar is translucent.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && isStatusBarTranslucent()) {
            // add the status bar height as top margin.
            mBlurredBackgroundLayoutParams.setMargins(0
                    , mBlurredBackgroundLayoutParams.topMargin + statusBarHeight, 0, 0);
        }

        //scale and draw background view on the canvas overlay
        Canvas canvas = new Canvas(overlay);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);

        //build drawing destination boundaries
        final RectF destRect = new RectF(0, 0, overlay.getWidth(), overlay.getHeight());

        //draw background from source area in source background to the destination area
        // on the overlay
        canvas.drawBitmap(bkg, srcRect, destRect, paint);

        //apply fast blur on overlay
        overlay = FastBlurHelper.doBlur(overlay, mBlurRadius, true, mUseRenderScript, mHoldingActivity);

        if (mDebugEnable) {
            String blurTime = (System.currentTimeMillis() - startMs) + " ms";

            //display information in LogCat
            Log.d(TAG, "Blur method : " + (mUseRenderScript ? "RenderScript" : "FastBlur"));
            Log.d(TAG, "Radius : " + mBlurRadius);
            Log.d(TAG, "Down Scale Factor : " + mDownScaleFactor);
            Log.d(TAG, "Blurred achieved in : " + blurTime);
            Log.d(TAG, "Allocation : " + bkg.getRowBytes() + "ko (screen capture) + "
                    + overlay.getRowBytes() + "ko (blurred bitmap)");
            //display blurring time directly on screen
            Rect bounds = new Rect();
            Canvas canvas1 = new Canvas(overlay);
            paint.setColor(Color.BLACK);
            paint.setAntiAlias(true);
            paint.setTextSize(20.0f);
            paint.getTextBounds(blurTime, 0, blurTime.length(), bounds);
            canvas1.drawText(blurTime, 2, bounds.height(), paint);
        }

        //set bitmap in an image view for final rendering
        mBlurredBackgroundView = new ImageView(mHoldingActivity);
        mBlurredBackgroundView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mBlurredBackgroundView.setImageDrawable(new BitmapDrawable(mHoldingActivity.getResources(), overlay));
    }

    /**
     * Retrieve action bar height.
     *
     * @return action bar height in px.
     */
    private int getActionBarHeight() {
        int actionBarHeight = 0;

        try {
            if (mToolbar != null) {
                actionBarHeight = mToolbar.getHeight();
            } else if (mHoldingActivity instanceof ActionBarActivity) {
                ActionBar supportActionBar
                        = ((ActionBarActivity) mHoldingActivity).getSupportActionBar();
                if (supportActionBar != null) {
                    actionBarHeight = supportActionBar.getHeight();
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                android.app.ActionBar actionBar = mHoldingActivity.getActionBar();
                if (actionBar != null) {
                    actionBarHeight = actionBar.getHeight();
                }
            }
        } catch (NoClassDefFoundError e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                android.app.ActionBar actionBar = mHoldingActivity.getActionBar();
                if (actionBar != null) {
                    actionBarHeight = actionBar.getHeight();
                }
            }
        }
        return actionBarHeight;
    }

    /**
     * retrieve status bar height in px
     *
     * @return status bar height in px
     */
    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = mHoldingActivity.getResources()
                .getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = mHoldingActivity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * Retrieve offset introduce by the navigation bar.
     *
     * @return bottom offset due to navigation bar.
     */
    private int getNavigationBarOffset() {
        int result = 0;
        Resources resources = mHoldingActivity.getResources();
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    /**
     * Used to check if the status bar is translucent.
     *
     * @return true if the status bar is translucent.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean isStatusBarTranslucent() {
        TypedValue typedValue = new TypedValue();
        int[] attribute = new int[]{android.R.attr.windowTranslucentStatus};
        TypedArray array = mHoldingActivity.obtainStyledAttributes(typedValue.resourceId, attribute);
        boolean isStatusBarTranslucent = array.getBoolean(0, false);
        array.recycle();
        return isStatusBarTranslucent;
    }

    /**
     * Removed the blurred view from the view hierarchy.
     */
    private void removeBlurredView() {
        if (mBlurredBackgroundView != null) {
            ViewGroup parent = (ViewGroup) mBlurredBackgroundView.getParent();
            if (parent != null) {
                parent.removeView(mBlurredBackgroundView);
            }
            mBlurredBackgroundView = null;
        }
    }

    /**
     * Async task used to process blur out of ui thread
     */
    private class BlurAsyncTask extends AsyncTask<Void, Void, Void> {

        private Bitmap mBackground;

        private View mBackgroundView;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mBackgroundView = mHoldingActivity.getWindow().getDecorView();

            //retrieve background view, must be achieved on ui thread since
            //only the original thread that created a view hierarchy can touch its views.

            Rect rect = new Rect();
            mBackgroundView.getWindowVisibleDisplayFrame(rect);
            mBackgroundView.destroyDrawingCache();
            mBackgroundView.setDrawingCacheEnabled(true);
            mBackgroundView.buildDrawingCache(true);
            mBackground = mBackgroundView.getDrawingCache(true);

            /**
             * After rotation, the DecorView has no height and no width. Therefore
             * .getDrawingCache() return null. That's why we  have to force measure and layout.
             */
            if (mBackground == null) {
                mBackgroundView.measure(
                        View.MeasureSpec.makeMeasureSpec(rect.width(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(rect.height(), View.MeasureSpec.EXACTLY)
                );
                mBackgroundView.layout(0, 0, mBackgroundView.getMeasuredWidth(),
                        mBackgroundView.getMeasuredHeight());
                mBackgroundView.destroyDrawingCache();
                mBackgroundView.setDrawingCacheEnabled(true);
                mBackgroundView.buildDrawingCache(true);
                mBackground = mBackgroundView.getDrawingCache(true);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {

            //process to the blue
            blur(mBackground, mBackgroundView);

            //clear memory
            mBackground.recycle();
            mBackgroundView.destroyDrawingCache();
            mBackgroundView.setDrawingCacheEnabled(false);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mBlurredBackgroundView.setAlpha(0f);
            mHoldingActivity.getWindow().addContentView(
                    mBlurredBackgroundView,
                    mBlurredBackgroundLayoutParams
            );
            mBlurredBackgroundView
                    .animate()
                    .alpha(1f)
                    .setDuration(mAnimationDuration)
                    .setInterpolator(new LinearInterpolator())
                    .start();

            mBackgroundView = null;
            mBackground = null;
        }
    }
}
