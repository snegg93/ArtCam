package org.artcam.android;

import android.content.Context;
import android.widget.ImageView;

class DressedFaceView extends ImageView
{
    public DressedFaceView(Context c) {
        super(c);
        setScaleType(ScaleType.FIT_START);
    }
}