package fr.tvbarthel.lib.blurdialogfragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
public class BlurDialogEngine {

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
     * Must be linked to the original lifecycle.
     *
     * @param activity holding activity.
     */
    public void onAttach(Activity activity) {
        mHoldingActivity = activity;
    }

    /**
     * Resume the engine.
     *
     * @param retainedInstance use getRetainInstance.
     */
    public void onResume(boolean retainedInstance) {
        if (mBlurredBackgroundView == null || retainedInstance) {
            if (mHoldingActivity.getWindow().getDecorView().isShown()) {
                mBluringTask = new BlurAsyncTask();
                mBluringTask.execute();
            } else {
                mHoldingActivity.getWindow().getDecorView().getViewTreeObserver().addOnPreDrawListener(
                        new ViewTreeObserver.OnPreDrawListener() {
                            @Override
                            public boolean onPreDraw() {
                                // dialog can have been closed before being drawn
                                if (mHoldingActivity != null) {
                                    mHoldingActivity.getWindow().getDecorView()
                                            .getViewTreeObserver().removeOnPreDrawListener(this);
                                    mBluringTask = new BlurAsyncTask();
                                    mBluringTask.execute();
                                }
                                return true;
                            }
                        }
                );
            }
        }
    }

    /**
     * Must be linked to the original lifecycle.
     */
    @SuppressLint("NewApi")
    public void onDismiss() {
        //remove blurred background and clear memory, could be null if dismissed before blur effect
        //processing ends
        //cancel async task
        if (mBluringTask != null) {
            mBluringTask.cancel(true);
        }
        if (mBlurredBackgroundView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
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
            } else {
                removeBlurredView();
            }
        }
    }

    /**
     * Must be linked to the original lifecycle.
     */
    public void onDetach() {
        if (mBluringTask != null) {
            mBluringTask.cancel(true);
        }
        mBluringTask = null;
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
     * <p/>
     * Don't forget to add those lines to your build.gradle
     * <pre>
     *  defaultConfig {
     *  ...
     *  renderscriptTargetApi 22
     *  renderscriptSupportModeEnabled true
     *  ...
     *  }
     * </pre>
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
     * @param bkg should be a bitmap of the background.
     */
    private void blur(Bitmap bkg) {
        long startMs = System.currentTimeMillis();
        //define layout params to the previous imageView in order to match its parent
        mBlurredBackgroundLayoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );

        int actionBarHeight;
        if (mBlurredActionBar) {
            actionBarHeight = 0;
        } else {
            actionBarHeight = getActionBarHeight();
        }

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                    || mHoldingActivity instanceof ActionBarActivity
                    || mHoldingActivity instanceof AppCompatActivity) {
                //add offset as top margin since actionBar height must also considered when we display
                // the blurred background. Don't want to draw on the actionBar.
                mBlurredBackgroundLayoutParams.setMargins(0, actionBarHeight, 0, 0);
                mBlurredBackgroundLayoutParams.gravity = Gravity.TOP;
            }
        } catch (NoClassDefFoundError e) {
            // no dependency to appcompat, that means no additional top offset due to actionBar.
            mBlurredBackgroundLayoutParams.setMargins(0, 0, 0, 0);
        }

        final Bitmap blurredBackground;
        //apply fast blur on overlay
        if (mUseRenderScript) {
            blurredBackground = RenderScriptBlurHelper.doBlur(bkg, mBlurRadius, true, mHoldingActivity);
        } else {
            blurredBackground = FastBlurHelper.doBlur(bkg, mBlurRadius, true);
        }

        if (blurredBackground == null) {
            if (mDebugEnable) {
                Log.e(TAG, "blur failed");
            }
            return;
        }

        if (mDebugEnable) {
            String blurTime = (System.currentTimeMillis() - startMs) + " ms";
            Log.d(TAG, "Blur method : " + (mUseRenderScript ? "RenderScript" : "FastBlur"));
            Log.d(TAG, "Radius : " + mBlurRadius);
            Log.d(TAG, "Down Scale Factor : " + mDownScaleFactor);
            Log.d(TAG, "Blurred achieved in : " + blurTime);

            if (blurredBackground == bkg) {
                Log.d(TAG, "Allocation: " + getByteCount(bkg) + " bytes (single bitmap used for capture and blur)");
            } else {
                Log.d(TAG, "Allocation: " + getByteCount(bkg) + " bytes (screen capture) + "
                        + getByteCount(blurredBackground) + " bytes (blur)");
            }

            if (!mUseRenderScript) {
                // The Fast blue helper uses an internal int array[height * width]
                final int internalIntArrayByteCount = bkg.getHeight() * bkg.getWidth() * 4;
                Log.d(TAG, "Allocation: " + internalIntArrayByteCount + " bytes (internal temp buff)");
            }

            Rect bounds = new Rect();
            Canvas canvas = new Canvas(blurredBackground);
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setAntiAlias(true);
            paint.setTextSize(20.0f);
            paint.getTextBounds(blurTime, 0, blurTime.length(), bounds);
            canvas.drawText(blurTime, 2, bounds.height(), paint);
        }
        //set bitmap in an image view for final rendering
        mBlurredBackgroundView = new ImageView(mHoldingActivity);
        mBlurredBackgroundView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mBlurredBackgroundView.setImageDrawable(new BitmapDrawable(mHoldingActivity.getResources(), blurredBackground));
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
            } else if (mHoldingActivity instanceof AppCompatActivity) {
                ActionBar supportActionBar
                        = ((AppCompatActivity) mHoldingActivity).getSupportActionBar();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
            final Drawable drawable = mBlurredBackgroundView.getDrawable();
            if (drawable instanceof BitmapDrawable) {
                ((BitmapDrawable) drawable).getBitmap().recycle();
            }
            mBlurredBackgroundView = null;
        }
    }

    /**
     * Return the number of bytes used by the a {@link Bitmap}.
     *
     * @param bitmap a {@link Bitmap}.
     * @return the number of bytes.
     */
    private int getByteCount(Bitmap bitmap) {
        if (bitmap == null) {
            return 0;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return bitmap.getAllocationByteCount();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        } else {
            final int bytePerPixel = getBytePerPixel(bitmap.getConfig());
            final int numberOfPixels = bitmap.getWidth() * bitmap.getHeight();
            return bytePerPixel * numberOfPixels;
        }
    }

    /**
     * Get the number of bytes used to store one pixel.
     *
     * @param config a {@link android.graphics.Bitmap.Config}.
     * @return the number of bytes used for storing one pixel.
     */
    private int getBytePerPixel(Bitmap.Config config) {
        if (config == Bitmap.Config.ALPHA_8) {
            return 1;
        } else if (config == Bitmap.Config.RGB_565) {
            return 2;
        } else if (config == Bitmap.Config.ARGB_4444) {
            return 2;
        } else if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        } else {
            return 0;
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

            if (mBackgroundView.getMeasuredHeight() == 0
                    && mBackgroundView.getMeasuredWidth() == 0) {
                final Rect rect = new Rect();
                mBackgroundView.getWindowVisibleDisplayFrame(rect);
                mBackgroundView.measure(
                        View.MeasureSpec.makeMeasureSpec(rect.width(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(rect.height(), View.MeasureSpec.EXACTLY)
                );

                mBackgroundView.layout(
                        0,
                        0,
                        mBackgroundView.getMeasuredWidth(),
                        mBackgroundView.getMeasuredHeight());
            }

            mBackground = buildBackgroundBitmap(mBackgroundView);
        }

        /**
         * Build the the background bitmap of a view.
         * <p>
         * Highly inspired by {@link View#buildDrawingCache()}.
         *
         * @param view the {@link View}.
         * @return a {@link Bitmap}.
         */
        private Bitmap buildBackgroundBitmap(View view) {
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

            // check if status bar is translucent to remove status bar offset in order to provide blur
            // on content bellow the status.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                    && isStatusBarTranslucent()) {
                statusBarHeight = 0;
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

            final double height = Math.floor((view.getHeight() - topOffset - bottomOffset) / mDownScaleFactor);
            final double width = Math.floor((view.getWidth() - rightOffset) / mDownScaleFactor);

            final Bitmap bitmap;
            if (mUseRenderScript) {
                bitmap = Bitmap.createBitmap((int) (width), (int) (height), Bitmap.Config.ARGB_8888);
            } else {
                bitmap = Bitmap.createBitmap((int) (width), (int) (height), Bitmap.Config.RGB_565);
            }

            view.computeScroll();

            final Canvas canvas = new Canvas(bitmap);
            final int restoreCount = canvas.save();
            canvas.scale(1 / mDownScaleFactor, 1 / mDownScaleFactor);
            canvas.translate(-view.getScrollX(), -view.getScrollY() - topOffset);
            view.draw(canvas);
            canvas.restoreToCount(restoreCount);
            canvas.setBitmap(null);

            return bitmap;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (!isCancelled()) {
                blur(mBackground);
            } else {
                mBackground.recycle();
                mBackground = null;
            }
            return null;
        }

        @Override
        @SuppressLint("NewApi")
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mHoldingActivity.getWindow().addContentView(
                    mBlurredBackgroundView,
                    mBlurredBackgroundLayoutParams
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                mBlurredBackgroundView.setAlpha(0f);
                mBlurredBackgroundView
                        .animate()
                        .alpha(1f)
                        .setDuration(mAnimationDuration)
                        .setInterpolator(new LinearInterpolator())
                        .start();
            }
            mBackgroundView = null;
            mBackground = null;
        }
    }
}
