package com.linda.answerhelper.util;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Dell on 2018/1/12.
 */

public class Util {

    public static int[] getScreenSize(Context context) {
        Point point = new Point();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(point);
        return new int[]{point.x, point.y};
    }

    public static int getScreenDPI(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metric);
        return metric.densityDpi;
    }

    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static BitmapFactory.Options getBitmapOption(int inSampleSize) {
        System.gc();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inSampleSize = inSampleSize;
        return options;
    }

    public static void write(Context context){
        InputStream inputStream;
        try {
            inputStream = context.getResources().getAssets().open("chi_sim.traineddata");
            File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile().getAbsolutePath()+"/tessdata");
            if(!file.exists()){
                file.mkdirs();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsoluteFile().getAbsolutePath()+"/tessdata/chi_sim.traineddata");
            byte[] buffer = new byte[512];
            int count = 0;
            while((count = inputStream.read(buffer)) > 0){
                fileOutputStream.write(buffer, 0 ,count);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();
            System.out.println("success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
