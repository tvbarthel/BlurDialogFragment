package fr.tvbarthel.lib.blurdialogfragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Encapsulate dialog behavior with blur effect.
 * <p/>
 * All the screen behind the dialog will be blurred except the action bar.
 */
public class BlurDialogFragment extends DialogFragment {

    /**
     * Log cat
     */
    private static final String TAG = BlurDialogFragment.class.getSimpleName();

    /**
     * Since image is going to be blurred, we don't care about resolution.
     * Down scale factor to decrease blurring time.
     */
    private static final float BLUR_DOWN_SCALE_FACTOR = 8.0f;

    /**
     * Radius used to blur the background
     */
    private static final int BLUR_RADIUS = 2;

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
     * Used to enable or disable log.
     */
    private boolean mLogEnable = false;

    /**
     * Factor used to down scale background. High quality isn't necessary
     * since the background will be blurred.
     */
    private float mDownScaleFactor = BLUR_DOWN_SCALE_FACTOR;

    /**
     * Radius used for fast blur algorithm.
     */
    private int mBlurRadius = BLUR_RADIUS;


    /**
     * default constructor as needed
     */
    public BlurDialogFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof ActionBarActivity)) {
            throw new IllegalStateException("BlurDialogFragment must be attached to an ActionBarActivity");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBlurredBackgroundView == null || getRetainInstance()) {
            mBluringTask = new BlurAsyncTask();
            mBluringTask.execute();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        //remove blurred background and clear memory
        mBlurredBackgroundView.setVisibility(View.GONE);
        mBlurredBackgroundView = null;

        //cancel async task
        mBluringTask.cancel(true);
        mBluringTask = null;
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    /**
     * Enable / disable log.
     *
     * @param enable true to display log in LogCat.
     */
    public void enableLog(boolean enable) {
        mLogEnable = enable;
    }

    /**
     * Apply custom down scale factor.
     * <p/>
     * By default down scale factor is set to
     * {@link fr.tvbarthel.lib.blurdialogfragment.BlurDialogFragment#BLUR_DOWN_SCALE_FACTOR}
     * <p/>
     * Higher down scale factor will increase blurring speed but reduce final rendering quality.
     *
     * @param factor customized down scale factor.
     */
    public void setDownScaleFactor(float factor) {
        mDownScaleFactor = factor;
    }

    /**
     * Apply custom blur radius.
     * <p/>
     * By default blur radius is set to
     * {@link fr.tvbarthel.lib.blurdialogfragment.BlurDialogFragment#BLUR_RADIUS}
     *
     * @param radius
     */
    public void setBlurRadius(int radius) {
        mBlurRadius = radius;
    }

    /**
     * Blur the given bitmap and add it to the activity.
     *
     * @param bkg      should be a bitmap of the background.
     * @param view     background view.
     * @param activity activity used to display blurred background.
     */
    private void blur(Bitmap bkg, View view, Activity activity) {
        long startMs = System.currentTimeMillis();

        //define layout params to the previous imageView in order to match its parent
        mBlurredBackgroundLayoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        //overlay used to build scaled preview and blur background
        Bitmap overlay = null;

        //evaluate top offset
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        int actionBarHeight = actionBar == null ? 0 : actionBar.getHeight();

        int statusBarHeight = getStatusBarHeight();
        final int topOffset = actionBarHeight + statusBarHeight;

        //add offset to the source boundaries since we don't want to blur actionBar pixels
        Rect srcRect = new Rect(
                0,
                actionBarHeight + statusBarHeight,
                bkg.getWidth(),
                bkg.getHeight()
        );

        //in order to keep the same ratio as the one which will be used for rendering, also
        //add the offset to the overlay.
        overlay = Bitmap.createBitmap((int) (view.getMeasuredWidth() / mDownScaleFactor),
                (int) ((view.getMeasuredHeight() - topOffset) / mDownScaleFactor), Bitmap.Config.RGB_565);

        /**
         * Padding must be added for rendering on pre HONEYCOMB
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            //add offset as top margin since actionBar height must also considered when we display
            // the blurred background. Don't want to draw on the actionBar.
            mBlurredBackgroundLayoutParams.setMargins(
                    0,
                    actionBarHeight,
                    0,
                    0
            );
            mBlurredBackgroundLayoutParams.gravity = Gravity.TOP;
        }

        //scale and draw background view on the canvas overlay
        Canvas canvas = new Canvas(overlay);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);


        //build drawing destination boundaries
        final RectF destRect = new RectF(0, 0, overlay.getWidth(), overlay.getHeight());

        //draw background from source area in source background to the destination area on the overlay
        canvas.drawBitmap(bkg, srcRect, destRect, paint);

        //apply fast blur on overlay
        overlay = FastBlurHelper.doBlur(overlay, mBlurRadius, false);

        if (mLogEnable) {
            Log.d(TAG, "blurred achieved in : " + (System.currentTimeMillis() - startMs) + "ms");
        }

        //set bitmap in an image view for final rendering
        mBlurredBackgroundView = new ImageView(activity);
        mBlurredBackgroundView.setImageDrawable(new BitmapDrawable(getResources(), overlay));
    }

    /**
     * retrieve status bar height in px
     *
     * @return status bar height in px
     */
    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * Async task used to process blur out of ui thread
     */
    public class BlurAsyncTask extends AsyncTask<Void, Void, Void> {

        private final Activity activity
                = BlurDialogFragment.this.getActivity();

        private Bitmap mBackground;

        private View mBackgroundView;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mBackgroundView = activity.getWindow().getDecorView();

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
            blur(mBackground, mBackgroundView, activity);

            //clear memory
            mBackground.recycle();
            mBackgroundView.destroyDrawingCache();
            mBackgroundView.setDrawingCacheEnabled(false);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //display blurred background
            activity.getWindow().addContentView(
                    mBlurredBackgroundView,
                    mBlurredBackgroundLayoutParams
            );
        }
    }
}
