package org.artcam.android;

import android.content.Context;
import android.graphics.*;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class FaceView extends ImageView implements View.OnTouchListener {
    public FaceView(Context c) {
        super(c);
        face = Utils.FaceFactory.create();
        Utils.Faces.getInstance().addFace(face);
        action = -1;
        setOnTouchListener(this);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        setScaleType(ScaleType.FIT_START);
        m = new Matrix();
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.eyes);
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        face.setBitmap(BitmapFactory.decodeFile(uri.getPath()));
    }

    @Override
    public void setImageBitmap(Bitmap bmp) {
        super.setImageBitmap(bmp);
        face.setBitmap(bmp);
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        c.save();
        Matrix m = getImageMatrix();
        PointF temp = face.getEyeLPoint(m);
        Point p = new Point(Math.round(temp.x), Math.round(temp.y)) ;
        c.drawBitmap(bmp, new Rect(0, 0, 24, 24), new Rect(p.x - 12, p.y - 12, p.x + 12, p.y +12), null);
        temp = face.getEyeRPoint(m);
        p = new Point(Math.round(temp.x), Math.round(temp.y)) ;
        c.drawBitmap(bmp, new Rect(24, 0, 48, 24), new Rect(p.x - 12, p.y - 12, p.x + 12, p.y +12), null);
        temp = face.getMouthPoint(m);
        p = new Point(Math.round(temp.x), Math.round(temp.y)) ;
        c.drawBitmap(bmp, new Rect(0, 24, 48, 48), new Rect(p.x - 24, p.y - 12, p.x + 24, p.y +12), null);
        c.restore();
    }

    public boolean onTouch(View view, MotionEvent ev) {
        if (view != this)
            return false;
        if (m.isIdentity())
            getImageMatrix().invert(m);
        float[] pts = new float[2];
        pts[0] = ev.getX();
        pts[1] = ev.getY();
        m.mapPoints(pts);
        Log.d("action", "x " + String.valueOf(ev.getX()) + " y " + String.valueOf(ev.getY()) + " pts[0] " + String.valueOf(pts[0]) + " pts[1] " + String.valueOf(pts[1]));
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN && action == -1) {
            float[] dist = face.getAllDistances(pts[0], pts[1]);
            Log.d("action", String.valueOf(dist[0]) + " " + String.valueOf(dist[1]) + " " + String.valueOf(dist[2]) + " ");
            if (dist[0] <= 200) {
                action = 0;
                Log.d("action", "action = 0");
                return true;
            } else if (dist[1] <= 200) {
                action = 1;
                Log.d("action", "action = 1");
                return true;
            } else if (dist[2] <= 200) {
                action = 2;
                Log.d("action", "action = 2");
                return true;
            }
        } else if (ev.getActionMasked() == MotionEvent.ACTION_MOVE && ev.getActionIndex() == 0) {
            switch (action) {
                case 0:
                    face.setEyeLPoint(new PointF(pts[0], pts[1]));
                    Log.d("action", "EL " + String.valueOf(pts[0]) + " " + String.valueOf(pts[1]));
                    invalidate();
                    break;
                case 1:
                    face.setEyeRPoint(new PointF(pts[0], pts[1]));
                    Log.d("action", "ER " + String.valueOf(pts[0]) + " " + String.valueOf(pts[1]));
                    invalidate();
                    break;
                case 2:
                    face.setMouthPoint(new PointF(pts[0], pts[1]));
                    Log.d("action", "MT " + String.valueOf(pts[0]) + " " + String.valueOf(pts[1]));
                    invalidate();
                    break;
                default:
                    return false;
            }
            return true;
        } else if (ev.getActionMasked() == MotionEvent.ACTION_UP) {
            switch (action) {
                case 0:
                    face.setEyeLPoint(new PointF(pts[0], pts[1]));
                    Log.d("action", "EL " + String.valueOf(pts[0]) + " " + String.valueOf(pts[1]));
                    break;
                case 1:
                    face.setEyeRPoint(new PointF(pts[0], pts[1]));
                    Log.d("action", "ER " + String.valueOf(pts[0]) + " " + String.valueOf(pts[1]));
                    break;
                case 2:
                    face.setMouthPoint(new PointF(pts[0], pts[1]));
                    Log.d("action", "MT " + String.valueOf(pts[0]) + " " + String.valueOf(pts[1]));
                    break;
                default:
                    return false;
            }
            if (action >= 0)
                invalidate();
            action = -1;
            Log.d("action", "action = -1");
        }
        return false;
    }

    private Face face;
    private int action;
    private Paint paint;
    private Matrix m;
    private Bitmap bmp;
}