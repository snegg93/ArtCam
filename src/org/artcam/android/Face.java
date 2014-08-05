package org.artcam.android;

import android.graphics.*;

public class Face {

    public enum ProcessState {CREATED, PROCESSED, DECORATED};

    public Face(Bitmap bmp, PointF eyePoint1, PointF eyePoint2, PointF mPoint) {
        setBitmap(bmp);
        eyeLPoint = eyePoint1;
        eyeRPoint = eyePoint2;
        mouthPoint = mPoint;
        state = ProcessState.CREATED;
    }


    public void setBitmap(Bitmap bmp) {
        if (bmp != null)
            bitmap = Bitmap.createBitmap(bmp);
        else
            bitmap = null;
    }
    public Bitmap getBitmap() { return bitmap; }

    public void setPoints(Point eyePoint1, Point eyePoint2, Point mPoint) {
        setPoints(new PointF(eyePoint1), new PointF(eyePoint2), new PointF(mPoint));
    }
    public void setPoints(PointF eyePoint1, PointF eyePoint2, PointF mPoint) {
        eyeLPoint = eyePoint1;
        eyeRPoint = eyePoint2;
        mouthPoint = mPoint;
    }

    public PointF getEyeLPoint() {
        return eyeLPoint;
    }
    public PointF getEyeLPoint(Matrix m) {
        float[] pts = new float[2];
        pts[0] = getEyeLPoint().x;
        pts[1] = getEyeLPoint().y;
        m.mapPoints(pts);
        return new PointF(pts[0], pts[1]);
    }
    public void setEyeLPoint(PointF point) {
        eyeLPoint = point;
    }
    public PointF getEyeRPoint() {
        return eyeRPoint;
    }
    public PointF getEyeRPoint(Matrix m) {
        float[] pts = new float[2];
        pts[0] = getEyeRPoint().x;
        pts[1] = getEyeRPoint().y;
        m.mapPoints(pts);
        return new PointF(pts[0], pts[1]);
    }
    public void setEyeRPoint(PointF point) {
        eyeRPoint = point;
    }
    public PointF getMouthPoint() {
        return mouthPoint;
    }
    public PointF getMouthPoint(Matrix m) {
        float[] pts = new float[2];
        pts[0] = getMouthPoint().x;
        pts[1] = getMouthPoint().y;
        m.mapPoints(pts);
        return new PointF(pts[0], pts[1]);
    }
    public void setMouthPoint(PointF point) {
        mouthPoint = point;
    }

    public float getEyeLDistance(float x, float y) {
        return Utils.getDistance(eyeLPoint, new PointF(x, y));
    }
    public float getEyeRDistance(float x, float y) {
        return Utils.getDistance(eyeRPoint, new PointF(x, y));
    }
    public float getMouthDistance(float x, float y) {
        return Utils.getDistance(mouthPoint, new PointF(x, y));
    }

    public float[] getAllDistances(float x, float y) {
        float[] res = new float[3];
        res[0] = getEyeLDistance(x, y);
        res[1] = getEyeRDistance(x, y);
        res[2] = getMouthDistance(x, y);
        return res;
    }

    public void rotatePoints() {
        PointF tmpPF = new PointF(eyeRPoint.x - eyeLPoint.x, eyeRPoint.y - eyeLPoint.y);
        double cosx = tmpPF.x / (Math.sqrt(Math.pow(tmpPF.x, 2) + Math.pow(tmpPF.y, 2)));
        double sinx = tmpPF.y / (Math.sqrt(Math.pow(tmpPF.x, 2) + Math.pow(tmpPF.y, 2)));
        float angle =(float) Math.toDegrees(Math.acos(cosx));
        setEyeRPoint(new PointF(eyeRPoint.x - eyeLPoint.x, eyeRPoint.y - eyeLPoint.y));
        setMouthPoint(new PointF(mouthPoint.x - eyeLPoint.x, mouthPoint.y - eyeLPoint.y));
        setEyeRPoint(new PointF((float)(eyeRPoint.x * cosx - eyeRPoint.y * sinx),(float)( eyeRPoint.x * sinx + eyeRPoint.y * cosx)));
        setMouthPoint(new PointF((float) (mouthPoint.x * cosx - mouthPoint.y * sinx), (float) (mouthPoint.x * sinx + mouthPoint.y * cosx)));
        setEyeRPoint(new PointF(eyeRPoint.x + eyeLPoint.x, eyeRPoint.y + eyeLPoint.y));
        setMouthPoint(new PointF(mouthPoint.x + eyeLPoint.x, mouthPoint.y + eyeLPoint.y));
    }

    public void process() {
        if (state != ProcessState.CREATED)
            return;
        PointF tmpPF = new PointF(eyeRPoint.x - eyeLPoint.x, eyeRPoint.y - eyeLPoint.y);
        double cosx = tmpPF.x / (Math.sqrt(Math.pow(tmpPF.x, 2) + Math.pow(tmpPF.y, 2)));
        double sinx = tmpPF.y / (Math.sqrt(Math.pow(tmpPF.x, 2) + Math.pow(tmpPF.y, 2)));
        float angle =(float) Math.toDegrees(Math.acos(cosx));
        setEyeRPoint(new PointF(eyeRPoint.x - eyeLPoint.x, eyeRPoint.y - eyeLPoint.y));
        setMouthPoint(new PointF(mouthPoint.x - eyeLPoint.x, mouthPoint.y - eyeLPoint.y));
        setEyeLPoint(new PointF(0, 0));
        setEyeRPoint(new PointF((float)(eyeRPoint.x * cosx - eyeRPoint.y * sinx),(float)( eyeRPoint.x * sinx + eyeRPoint.y * cosx)));
        setMouthPoint(new PointF((float) (mouthPoint.x * cosx - mouthPoint.y * sinx), (float) (mouthPoint.x * sinx + mouthPoint.y * cosx)));
        float distX =  eyeRPoint.x;
        float distY = mouthPoint.y;
        Bitmap temp = Bitmap.createBitmap(Math.round(distX * 2f + 20), Math.round(distY * 2f + 20), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(temp);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        c.translate(0 - eyeLPoint.x + 10 + distX / 2, 0 - eyeLPoint.y + distX / 2 + 10);
        //c.drawOval(new RectF(eyeLPoint.x - 10, eyeLPoint.y - 10, eyeLPoint.x + dist + 20, eyeLPoint.y + dist * 1.5f + 20), paint);
        c.rotate(angle, eyeLPoint.x, eyeLPoint.y);
        float mat[] = {
                0.8f, 0.8f, 0.8f, 0, 0,
                0.8f, 0.8f, 0.8f, 0, 0,
                0.8f, 0.8f, 0.8f, 0, 0,
                0, 0, 0, 1, 0
        };
        paint.setColorFilter(new ColorMatrixColorFilter(mat));
        //paint.setXfermode(new AvoidXfermode(Color.RED, 0, AvoidXfermode.Mode.TARGET));
        c.drawBitmap(bitmap, 0, 0, paint);
        bitmap = temp.copy(Bitmap.Config.ARGB_8888, true);
        state = ProcessState.PROCESSED;
    }

    public ProcessState getState() {
        return state;
    }

    private Bitmap bitmap;
    private PointF eyeLPoint, eyeRPoint, mouthPoint;
    private ProcessState state;
}