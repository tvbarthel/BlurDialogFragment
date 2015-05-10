package fr.tvbarthel.lib.blurdialogfragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RSRuntimeException;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.Log;

/**
 * Simple helper used to blur a bitmap thanks to render script.
 */
final class RenderScriptBlurHelper {

    /**
     * Log cat
     */
    private static final String TAG = RenderScriptBlurHelper.class.getSimpleName();

    /**
     * Non instantiable class.
     */
    private RenderScriptBlurHelper() {

    }

    /**
     * blur a given bitmap
     *
     * @param sentBitmap       bitmap to blur
     * @param radius           blur radius
     * @param canReuseInBitmap true if bitmap must be reused without blur
     * @param context          used by RenderScript, can be null if RenderScript disabled
     * @return blurred bitmap
     */
    public static Bitmap doBlur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap, Context context) {
        Bitmap bitmap;

        if (canReuseInBitmap) {
            bitmap = sentBitmap;
        } else {
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        }

        if (bitmap.getConfig() == Bitmap.Config.RGB_565) {
            // RenderScript hates RGB_565 so we convert it to ARGB_8888
            // (see http://stackoverflow.com/questions/21563299/
            // defect-of-image-with-scriptintrinsicblur-from-support-library)
            bitmap = convertRGB565toARGB888(bitmap);
        }

        try {
            final RenderScript rs = RenderScript.create(context);
            final Allocation input = Allocation.createFromBitmap(rs, bitmap, Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);
            final Allocation output = Allocation.createTyped(rs, input.getType());
            final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            script.setRadius(radius);
            script.setInput(input);
            script.forEach(output);
            output.copyTo(bitmap);
            return bitmap;
        } catch (RSRuntimeException e) {
            Log.e(TAG, "RenderScript known error : https://code.google.com/p/android/issues/detail?id=71347 "
                + "continue with the FastBlur approach.");
        }

        return null;
    }

    private static Bitmap convertRGB565toARGB888(Bitmap bitmap) {
        return bitmap.copy(Bitmap.Config.ARGB_8888, true);
    }
}
