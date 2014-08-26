package org.artcam.android;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class FaceView extends ImageView implements View.OnTouchListener {

    public static enum EditState {EYES, PROCESSED, ERASER}

    public FaceView(Context c) {
        super(c);
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

    public void setFaceBitmap(Bitmap bmp) {
        face.setOrigBitmap(bmp);
        setImageBitmap(face.getOrigBitmap());
    }

    public void setEditState(EditState st) {
        if (st == EditState.ERASER) {
            if (face.getOverlay(Face.ERASER_OVERLAY) != null)
                eraserBitmap = face.getOverlay(Face.ERASER_OVERLAY).copy(Bitmap.Config.ARGB_8888, true);
            else
                eraserBitmap = Bitmap.createBitmap(face.getProcessedBitmap().getWidth(), face.getProcessedBitmap().getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            if (eraserBitmap != null && !eraserBitmap.isRecycled()) {
                eraserBitmap.recycle();
                eraserBitmap = null;
            }
            if (eraserLayer != null && !eraserLayer.isRecycled()) {
                eraserLayer.recycle();
                eraserLayer = null;
            }
        }
        state = st;
        setImageBitmap(face.getProcessedBitmap());
        invalidate();
    }
    public EditState getEditState() {
        return state;
    }

    public void setEraserSize(int size) {
        eraserSize = size;
        if (eraserPaint != null)
            eraserPaint.setStrokeWidth(size);
    }
    public int getEraserSize() {
        return eraserSize;
    }

    public void setFaceId(int id) {
        if (id < 0 || Utils.Faces.getInstance().getFace(id) == null) {
            face = Utils.FaceFactory.create();
            Utils.Faces.getInstance().addFace(face);
            setFaceBitmap(Bitmap.createBitmap(0, 0, Bitmap.Config.ARGB_8888));
            setEditState(EditState.EYES);
        } else {
            face = Utils.Faces.getInstance().getFace(id);
            setImageBitmap(face.getProcessedBitmap());
            if (face.getState() == Face.ProcessState.PROCESSED)
                setEditState(EditState.PROCESSED);
            else
                setEditState(EditState.EYES);
        }
    }

    @Override
    public void setImageBitmap(Bitmap bmp) {
        super.setImageBitmap(bmp);
        getImageMatrix().invert(m);
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        if (state == EditState.EYES) {
            c.save();
            Matrix m = getImageMatrix();
            PointF temp = face.getEyeLPoint(m);
            Point p = new Point(Math.round(temp.x), Math.round(temp.y));
            c.drawBitmap(bmp, new Rect(0, 0, 24, 24), new Rect(p.x - 12, p.y - 12, p.x + 12, p.y + 12), null);
            temp = face.getEyeRPoint(m);
            p = new Point(Math.round(temp.x), Math.round(temp.y));
            c.drawBitmap(bmp, new Rect(24, 0, 48, 24), new Rect(p.x - 12, p.y - 12, p.x + 12, p.y + 12), null);
            temp = face.getMouthPoint(m);
            p = new Point(Math.round(temp.x), Math.round(temp.y));
            c.drawBitmap(bmp, new Rect(0, 24, 48, 48), new Rect(p.x - 24, p.y - 12, p.x + 24, p.y + 12), null);
            c.restore();
        }
        if (eraserLayer != null && !eraserLayer.isRecycled())
            c.drawBitmap(eraserLayer, getImageMatrix(), eraserPaint);
        if (eraserBitmap != null && !eraserBitmap.isRecycled())
            c.drawBitmap(eraserBitmap, getImageMatrix(), eraserPaint);
    }

    public void processFace() {
        face.process();
        setImageBitmap(face.getProcessedBitmap());
        invalidate();
    }

    public void setBrightness(int b) {
        face.setBrightness(b / 100.f);
        setImageBitmap(face.getProcessedBitmap());
    }
    public int getBrightness() {
        return Math.round(face.getBrightness() * 100);
    }

    public boolean onTouch(View view, MotionEvent ev) {
        if (view != this)
            return false;
        if (m.isIdentity())
            getImageMatrix().invert(m);
        switch (state) {
            case EYES:
                return moveEyes(ev);
            case ERASER:
                return eraser(ev);
            default:
                return false;
        }
    }

    private boolean moveEyes(MotionEvent ev) {
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


    private boolean eraser(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (eraserLayer != null && !eraserLayer.isRecycled()) {
                eraserCanvas = new Canvas(eraserBitmap);
                eraserCanvas.drawBitmap(eraserLayer, 0, 0, null);
                eraserLayer.recycle();
                eraserLayer = null;
            }
            eraserLayer = Bitmap.createBitmap(face.getProcessedBitmap().getWidth(), face.getProcessedBitmap().getHeight(), Bitmap.Config.ARGB_8888);
            eraserCanvas = new Canvas(eraserLayer);
            eraserPaint = new Paint();
            eraserPaint.setColor(Color.WHITE);
            eraserPaint.setStrokeWidth(eraserSize);
            eraserPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            eraserPaint.setStrokeCap(Paint.Cap.ROUND);
            return true;
        } else if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
            float[] pts = new float[4];
            pts[0] = ev.getX();
            pts[1] = ev.getY();
            if (ev.getHistorySize() != 0) {
                pts[2] = ev.getHistoricalX(0);
                pts[3] = ev.getHistoricalY(0);
            }else {
                pts[2] = pts[0];
                pts[3] = pts[1];
            }
            m.mapPoints(pts);
            eraserCanvas.drawLine(pts[2], pts[3], pts[0], pts[1], eraserPaint);
            invalidate();
            return false;
        } else if (ev.getActionMasked() == MotionEvent.ACTION_UP) {
            invalidate();
            return false;
        }
        return false;
    }

    public void undoEraser() {
        if (eraserLayer != null && !eraserLayer.isRecycled()) {
            eraserLayer.recycle();
            eraserLayer = null;
        }
        invalidate();
    }
    public void applyEraser() {
        if (eraserLayer != null && !eraserLayer.isRecycled()) {
            eraserCanvas = new Canvas(eraserBitmap);
            eraserCanvas.drawBitmap(eraserLayer, 0, 0, null);
            eraserLayer.recycle();
            eraserLayer = null;
        }
        face.setOverlay(Face.ERASER_OVERLAY, eraserBitmap);
        setImageBitmap(face.getProcessedBitmap());
    }

    private Face face;
    private int action;
    private Paint paint;
    private Matrix m;
    private Bitmap bmp;
    private Bitmap eraserLayer;
    private Bitmap eraserBitmap;
    private Canvas eraserCanvas;
    private Paint eraserPaint;
    private int eraserSize;
    private EditState state;
}