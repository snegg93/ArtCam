package org.artcam.android;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.*;
import android.os.Bundle;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import java.util.Vector;

public class Utils {

    private static Utils single = null;
    private Session session;

    private Utils() {
    }

    public static Utils getInstance() {
        if (single == null) {
            single = new Utils();
        }
        return single;
    }

    public Session getSession() {
        return this.session;
    }

    public void setSession(Session s) {
        session = s;
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

        public int addFace(Face f) {
            faces.add(f);
            return faces.size() - 1;
        }

        public Face getFace(int i) {
            return faces.get(i);
        }
        public int getId(Face f) { return faces.indexOf(f);}

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

        public int getCount() {
            return faces.size();
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

    public static class Session implements
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        private GoogleApiClient googleApiClient;
        private boolean intentInProgress;
        private static final int RC_SIGN_IN = 0;
        private Activity activity;
        private boolean needResolution;

        public Session(Activity a) {
            needResolution = false;
            activity = a;
            GoogleApiClient.Builder b = new GoogleApiClient.Builder(a);
            b.addConnectionCallbacks(this);
            b.addOnConnectionFailedListener(this);
            b.addApi(Plus.API, Plus.PlusOptions.builder().build());
            b.addScope(Plus.SCOPE_PLUS_LOGIN);
            googleApiClient = b.build();
            googleApiClient.connect();
        }

        public void activityResult(int requestCode) {
            if (requestCode == RC_SIGN_IN) {
                intentInProgress = false;

                if (!googleApiClient.isConnecting()) {
                    googleApiClient.connect();
                }
            }
        }

        public void close() {
            if (googleApiClient.isConnected()) {
                googleApiClient.disconnect();
            }
        }

        public void connect() {
            needResolution = true;
            googleApiClient.connect();
        }

        public void onConnectionFailed(ConnectionResult result) {
            if (!intentInProgress && result.hasResolution()) {
                try {
                    Toast.makeText(activity, "Cant sign in", Toast.LENGTH_SHORT).show();
                    if (needResolution) {
                        needResolution = false;
                        intentInProgress = true;
                        result.startResolutionForResult(activity, RC_SIGN_IN);
                    }
                } catch (IntentSender.SendIntentException e) {
                    // The intent was canceled before it was sent.  Return to the default
                    // state and attempt to connect to get an updated ConnectionResult.
                    intentInProgress = false;
                    //googleApiClient.connect();
                }
            }
        }


        public void onConnected(Bundle connectionHint) {
            Toast.makeText(activity, "Signed in Google+ with " + Plus.AccountApi.getAccountName(googleApiClient), Toast.LENGTH_SHORT).show();
            needResolution = false;
        }

        public void onConnectionSuspended(int cause) {
            googleApiClient.connect();
        }
    }
}