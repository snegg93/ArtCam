package org.artcam.android;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class FaceView extends ImageView implements View.OnTouchListener, Utils.FaceChangedEventListened {

    @Override
    public void onProcessedChanged() {
        setImageBitmap(face.getProcessedBitmap());
    }

    public static enum ToolState {SCALE, ERASER, HAT, EYEBROW, MOUTH}

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
    }

    public void setFaceBitmap(Bitmap bmp) {
        face.setOrigBitmap(bmp);
        setImageBitmap(face.getOrigBitmap());
    }

    public void setEditState(ToolState st) {
        if ((st != state) && currentTool != null) {
            face.setOverlay(currentTool.getOverlayID(), currentTool.getOverlay());
            currentTool.recycle();
        }
        if (st == ToolState.ERASER) {
            currentTool = new Utils.EraserTool();
        }
        if (face.getOverlay(currentTool.getOverlayID()) != null)
            currentTool.setOverlay(face.getOverlay(currentTool.getOverlayID()).copy(Bitmap.Config.ARGB_8888, true));
        else
            currentTool.setOverlay(Bitmap.createBitmap(face.getProcessedBitmap().getWidth(), face.getProcessedBitmap().getHeight(), Bitmap.Config.ARGB_8888));
        state = st;
        setImageBitmap(face.getProcessedBitmap());
        invalidate();
    }
    public ToolState getEditState() {
        return state;
    }

    public void setEraserSize(int size) {
        if (state == ToolState.ERASER)
            currentTool.userAction(Utils.EraserTool.USER_ACTION_SETSIZE, size);
    }
    public int getEraserSize() {
        return currentTool.getUserParam(Utils.EraserTool.USER_ACTION_SETSIZE);
    }

    public void setFaceId(int id) {
        if (id < 0 || Utils.Faces.getInstance().getFace(id) == null) {
            face = Utils.FaceFactory.create();
            Utils.Faces.getInstance().addFace(face);
            setFaceBitmap(Bitmap.createBitmap(0, 0, Bitmap.Config.ARGB_8888));
            setEditState(ToolState.SCALE);
            face.setChangedEventListener(this);
        } else {
            face = Utils.Faces.getInstance().getFace(id);
            setImageBitmap(face.getProcessedBitmap());
            face.setChangedEventListener(this);
            if (face.getState() == Face.ProcessState.PROCESSED)
                setEditState(ToolState.SCALE);
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
        if (currentTool != null)
            currentTool.paint(c, getImageMatrix());
    }

    public void processFace() {
        face.process();
        invalidate();
    }

    public void setBrightness(int b) {
        face.setBrightness(b / 100.f);
        invalidate();
    }
    public int getBrightness() {
        return Math.round(face.getBrightness() * 100);
    }

    public boolean onTouch(View view, MotionEvent ev) {
        if (view != this)
            return false;
        if (m.isIdentity())
            getImageMatrix().invert(m);
        currentTool.setMatrix(m);
        return currentTool.motionEvent(ev);
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

    public void applyEraser() {
        if (state == ToolState.ERASER)
            currentTool.userAction(Utils.EraserTool.USER_ACTION_APPLY, 0);
    }

    public void undoEraser() {
        if (state == ToolState.ERASER)
            currentTool.userAction(Utils.EraserTool.USER_ACTION_UNDO, 0);
    }


    private Face face;
    private int action;
    private Paint paint;
    private Matrix m;
    private ToolState state;
    private Utils.EditTool currentTool;
}