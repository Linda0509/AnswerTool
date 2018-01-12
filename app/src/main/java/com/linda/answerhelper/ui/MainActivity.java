package com.linda.answerhelper.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.linda.answerhelper.service.AnswerService;
import com.linda.answerhelper.event.BitmapEvent;
import com.linda.answerhelper.event.StartCaptureEvent;
import com.linda.answerhelper.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.ByteBuffer;

import static android.graphics.PixelFormat.RGBA_8888;

public class MainActivity extends AppCompatActivity {

    private int REQUEST_PERMISSION = 1;
    private int REQUEST_CAPTURE = 2;

    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private ImageReader mImageReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        initPermission();
    }

    private void initPermission() {
        if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION);
        } else {
            initOverLayerService();
        }
    }

    private void initOverLayerService() {

        Util.write(this);

        if (Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(this, AnswerService.class);
            startService(intent);
            initProjection();
        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(intent);
            finish();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (permissions.length == grantResults.length) {
                initOverLayerService();
            } else {
                finish();
            }
        }
    }

    private void initProjection() {
        mProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        if (mProjectionManager != null) {
            Intent captureIntent = mProjectionManager.createScreenCaptureIntent();
            startActivityForResult(captureIntent, REQUEST_CAPTURE);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAPTURE && resultCode == RESULT_OK) {
            mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            createVirtualDisplay();
        }
    }

    private void createVirtualDisplay() {
        int[] size = Util.getScreenSize(this);
        mImageReader = ImageReader.newInstance(size[0], size[1], RGBA_8888, 5);
        mMediaProjection.createVirtualDisplay(
                "MainScreen",
                size[0],
                size[1],
                Util.getScreenDPI(this),
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(),
                null, null);
    }

    private void startCapture() {
        Image image = mImageReader.acquireLatestImage();
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        //Tode 目前写死了问题区域
        bitmap = Bitmap.createBitmap(bitmap, 0, Util.dp2px(this, 100), width, Util.dp2px(this, 100));
        image.close();

        BitmapEvent bitmapEvent = new BitmapEvent();
        bitmapEvent.setBitmap(bitmap);
        EventBus.getDefault().post(bitmapEvent);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetStartCaptureEvent(StartCaptureEvent event) {
        startCapture();
    }


}
