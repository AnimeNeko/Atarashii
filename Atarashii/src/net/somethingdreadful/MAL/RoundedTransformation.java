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
    private final int radius;
    private final String username;

    // radius is corner radius in dp
    public RoundedTransformation(final int radius, final String username) {
        this.radius = radius;
        this.username = username;
    }

    @Override
    public Bitmap transform(final Bitmap source) {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawRoundRect(new RectF(0, 0, source.getWidth(), source.getHeight()), radius, radius, paint);

        if (source != output) {
            source.recycle();
        }

        return output;
    }

    @Override
    public String key() {
        return "rounded" + username + radius;
    }
}