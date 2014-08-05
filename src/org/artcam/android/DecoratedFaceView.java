package org.artcam.android;

import android.content.Context;
import android.widget.ImageView;

class DecoratedFaceView extends ImageView
{
    public DecoratedFaceView (Context c) {
        super(c);
        setScaleType(ScaleType.FIT_START);
    }
}