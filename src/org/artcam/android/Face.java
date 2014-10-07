package org.artcam.android;

import android.graphics.*;

import java.util.Vector;

public class Face {

    public enum ProcessState {CREATED, PROCESSED, DECORATED}
    private static final int OVERLAYS_NUM = 1;
    public static final int ERASER_OVERLAY = 0;

    public Face(Bitmap bmp, PointF eyePoint1, PointF eyePoint2, PointF mPoint) {
        setOrigBitmap(bmp);
        eyeLPoint = eyePoint1;
        eyeRPoint = eyePoint2;
        mouthPoint = mPoint;
        state = ProcessState.CREATED;
        processedBitmap = null;
        brightness = 0.8f;
        overlays = new Vector<>();
        for (int i = 0; i < OVERLAYS_NUM; ++i)
            overlays.add(null);
    }


    public void setOrigBitmap(Bitmap bmp) {
        if (bmp != null) {
            origBitmap = Bitmap.createBitmap(bmp);
        }
        else
            origBitmap = null;
    }
    public Bitmap getOrigBitmap() {
        return origBitmap;
    }

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
        angle =(float) Math.toDegrees(Math.acos(cosx));
        setEyeRPoint(new PointF(eyeRPoint.x - eyeLPoint.x, eyeRPoint.y - eyeLPoint.y));
        setMouthPoint(new PointF(mouthPoint.x - eyeLPoint.x, mouthPoint.y - eyeLPoint.y));
        float distX =  eyeRPoint.x;
        float distY = mouthPoint.y;

        faceRect = new RectF();
        faceRect.left = 0 - eyeLPoint.x + 10 + distX / 2;
        faceRect.top = 0 - eyeLPoint.y + distX / 2 + 10;
        faceRect.right = faceRect.left + distX * 2f + 20;
        faceRect.bottom = faceRect.top + distY * 2f + 20;

        setEyeLPoint(new PointF(0, 0));
        setEyeRPoint(new PointF((float)(eyeRPoint.x * cosx - eyeRPoint.y * sinx),(float)( eyeRPoint.x * sinx + eyeRPoint.y * cosx)));
        setMouthPoint(new PointF((float) (mouthPoint.x * cosx - mouthPoint.y * sinx), (float) (mouthPoint.x * sinx + mouthPoint.y * cosx)));

        changeProcess();
        getProcessedBitmap();
        state = ProcessState.PROCESSED;
    }

    public RectF getFaceRect() {
        return faceRect;
    }

    public ColorMatrixColorFilter getFilter() {
        return new ColorMatrixColorFilter(colorMatrix);
    }

    public Bitmap getProcessedBitmap() {
        if (state == ProcessState.CREATED)
            return origBitmap;
        if (processedBitmap != null)
            return processedBitmap;
        return null;
    }

    public void setBrightness(float b) {
        brightness = b;
        changeProcess();
    }

    private void changeProcess() {
        if (processedBitmap == null)
            processedBitmap = Bitmap.createBitmap(Math.round(faceRect.width()), Math.round(faceRect.height()), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(processedBitmap);
        Paint paint = new Paint();
        paint.setColor(0xFFFFFF00);
        c.drawRect(0, 0, processedBitmap.getWidth(), processedBitmap.getHeight(), paint);

        c.save();

        c.translate(faceRect.left, faceRect.top);
        c.rotate(angle, Math.abs(faceRect.left), Math.abs(faceRect.top));


        colorMatrix = new float[] {
                brightness, brightness, brightness, 0, 0,
                brightness, brightness, brightness, 0, 0,
                brightness, brightness, brightness, 0, 0,
                0, 0, 0, 1, 0
        };

        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        c.drawBitmap(origBitmap, 0, 0, paint);
        c.restore();
        for (int i = 0; i < overlays.size(); ++i) {
            if (overlays.elementAt(i) != null && !overlays.elementAt(i).isRecycled())
                c.drawBitmap(overlays.elementAt(i), 0, 0, paint);
        }
        if (evListener != null)
            evListener.onProcessedChanged();
    }

    public void setChangedEventListener( Utils.FaceChangedEventListened listener ) {
        evListener = listener;
    }

    public float getBrightness() {
        return brightness;
    }

    public ProcessState getState() {
        return state;
    }

    public void setOverlay(int num, Bitmap bmp) {
        if (num < overlays.size()) {
            if (overlays.elementAt(num) != null && !overlays.elementAt(num).isRecycled()) {
                overlays.elementAt(num).recycle();
                overlays.setElementAt(null, num);
            }
            overlays.setElementAt(bmp.copy(Bitmap.Config.ARGB_8888, false), num);
            changeProcess();
        }
    }

    public Bitmap getOverlay(int num) {
        return overlays.elementAt(num);
    }

    private Bitmap origBitmap, processedBitmap;
    private RectF faceRect;
    private PointF eyeLPoint, eyeRPoint, mouthPoint;
    private ProcessState state;
    private float colorMatrix[];
    private float angle;
    private float brightness;
    private Vector<Bitmap> overlays;
    private Utils.FaceChangedEventListened evListener;
}