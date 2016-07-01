package xniuniux.onefignerslyte;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.Nullable;
import android.view.View;

public class BlurBuilder {
    private static float BITMAP_SCALE = 0.4f;
    private static float BLUR_RADIUS = 7.5f;

    public static Bitmap blur(View v, @Nullable Float radius, @Nullable Float scale) {
        return blur(v.getContext(), getScreenshot(v), radius, scale);
    }

    public static Bitmap blur(Context ctx, Bitmap image, Float radius, Float scale) {
        if (radius != null && radius == 0){ return image;}

        if (scale != null && scale > 0 && scale <= 1) {
            BITMAP_SCALE = scale;
        }

        if (radius != null && radius > 0 && radius <= 25) {
            BLUR_RADIUS = radius;
        }
        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(ctx);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    public static Bitmap getScreenshot(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }


}