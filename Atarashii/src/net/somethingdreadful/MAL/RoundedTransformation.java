package net.somethingdreadful.MAL;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

// enables hardware accelerated rounded corners
public class RoundedTransformation implements com.squareup.picasso.Transformation {
    private int radius;
    private final String username;
    private int marginHeight = 0;
    private int marginWidth = 0;

    // radius is corner radius in dp
    public RoundedTransformation(final String username) {
        this.username = username;
    }

    @Override
    public Bitmap transform(final Bitmap source) {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        int size;
        if (source.getWidth() < source.getHeight()) {
            size = source.getWidth();
            marginHeight = (source.getHeight() - size) / 2;
        } else {
            size = source.getHeight();
            marginWidth = (source.getWidth() - size) / 2;
        }
        radius = size * 2;

        canvas.drawRoundRect(new RectF(marginWidth, marginHeight, source.getWidth() - marginWidth, source.getHeight() - marginHeight), radius, radius, paint);

        if (source != output) {
            source.recycle();
        }

        return output;
    }

    @Override
    public String key() {
        return "rounded" + username + radius + marginWidth + marginHeight;
    }
}