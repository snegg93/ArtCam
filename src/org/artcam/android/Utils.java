package org.artcam.android;


import android.graphics.*;

import java.util.Vector;

public class Utils {

    private static Utils single = null;

    private Utils() {
    }

    public static Utils getInstance() {
        if (single == null)
            single = new Utils();
        return single;
    }

    public static float getDistance(PointF first, PointF second) {
        return (float) Math.sqrt(Math.pow(first.x - second.x, 2) + Math.pow(first.y - second.y, 2));
    }

    public static class Faces {

        private Vector<Face> faces;
        private static Faces instance = null;

        private Faces() {
            faces = new Vector<>();
        }

        public static Faces getInstance() {
            if (instance == null)
                instance = new Faces();
            return instance;
        }

        public void addFace(Face f) {
            faces.add(f);
        }

        public Face getFace(int i) {
            return faces.get(i);
        }

        public void removeFace(int i) {
            faces.remove(i);
        }

        public void removeFace(Face f) {
            faces.remove(f);
        }

        public void processAll() {
            for (Face f : faces) {
                f.process();
            }
        }

        public void processFace(int i) {
            faces.get(i).process();
        }

        public Face getNextWithState(Face.ProcessState state) {
            for (Face f : faces) {
                if (f.getState() == state)
                    return f;
            }
            return null;
        }
    }

    public static class FaceFactory {
        public static Face create() {
            return new Face(null, new PointF(150, 150.f), new PointF(450.f, 150.f), new PointF(300.f, 300.f));
        }

        public static Face create(Bitmap bmp) {
            return new Face(bmp, new PointF(150, 150.f), new PointF(450.f, 150.f), new PointF(300.f, 300.f));
        }

        public static Face create(Bitmap bmp, PointF eyePoint1, PointF eyePoint2, PointF mPoint) {
            return new Face(bmp, eyePoint1, eyePoint2, mPoint);
        }

        public static Face create(Bitmap bmp, Point eyePoint1, Point eyePoint2, Point mPoint) {
            return new Face(bmp, new PointF(eyePoint1), new PointF(eyePoint2), new PointF(mPoint));
        }
    }
}