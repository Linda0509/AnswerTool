package com.linda.answerhelper.event;

import android.graphics.Bitmap;

/**
 * Created by Dell on 2018/1/12.
 */

public class BitmapEvent {
    private Bitmap mBitmap;

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }
}
