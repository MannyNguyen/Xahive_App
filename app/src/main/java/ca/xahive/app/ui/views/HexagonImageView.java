package ca.xahive.app.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.view.View;

import ca.xahive.app.ui.activities.myapp.R;

public class HexagonImageView extends View {
    Bitmap bitmapToClip;

    public Bitmap getBitmapToClip() {
        return bitmapToClip;
    }

    public void setBitmapToClip(Bitmap bitmapToClip) {
        if (this.bitmapToClip == bitmapToClip) {
            return;
        }

        this.bitmapToClip = bitmapToClip;

        invalidate();
    }

    public HexagonImageView(Context context) {
        super(context);

        this.disableHardwareAccelerationOnLayer();
    }

    public HexagonImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.disableHardwareAccelerationOnLayer();
    }

    public HexagonImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        this.disableHardwareAccelerationOnLayer();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.drawHexagonBitmap(canvas);
    }

    // must disable hardware acceleration for this view's layer in order for clipping to work
    public void disableHardwareAccelerationOnLayer() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public void drawHexagonBitmap(Canvas canvas) {
        //create points for hexagon path
        float totalWidth = this.getWidth();
        float hexWidth = this.getWidth() * 0.82f;
        float spacingX = (totalWidth - hexWidth) / 2.0f;

        float height = this.getHeight();

        PointF[] hexPoints = {
                new PointF(spacingX + (hexWidth / 2.0f),
                        height), //BM
                new PointF(spacingX + hexWidth,
                        height * (3.0f / 4.0f)), //BR
                new PointF(spacingX + hexWidth,
                        height * (1.0f / 4.0f)), //TR
                new PointF(spacingX + (hexWidth / 2.0f),
                        0.0f), //TM
                new PointF(spacingX,
                        height * (1.0f / 4.0f)), //TL
                new PointF(spacingX,
                        height * (3.0f / 4.0f)), //BL
        };

        //create hexagon path
        Path roundedHexagonPath = new Path();

        //start shape at bottom middle
        roundedHexagonPath.moveTo(hexPoints[0].x, hexPoints[0].y);

        //draw lines to the other points
        for (int i = 1; i < hexPoints.length; i++) {
            roundedHexagonPath.lineTo(hexPoints[i].x, hexPoints[i].y);
        }

        //close the path so that the first point will also get a rounded edge later
        roundedHexagonPath.close();

        float cornerPathRadius = totalWidth * 0.03f;

        //create paint for hexagon path with rounded edges
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(getResources().getColor(R.color.xa_dark_green));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setDither(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setPathEffect(new CornerPathEffect(cornerPathRadius));
        paint.setAntiAlias(true);

        //draw the path on the canvas before setting it's transfer mode
        canvas.drawPath(roundedHexagonPath, paint);

        if(this.bitmapToClip != null)
        {
            //set transfer mode for paint to source in
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            bitmapToClip = ThumbnailUtils.extractThumbnail(bitmapToClip, getWidth(), getHeight());

            //draw scaled bitmap with the paint's transfer mode
            canvas.drawBitmap(bitmapToClip, 0, 0, paint);

        }
    }
}
