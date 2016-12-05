package com.littlechoc.videotimeline;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * @author Junhao Zhou 2016/12/3
 */

public class BitmapUtil {

  public static Bitmap compress(Bitmap origin, int targetWidth, int targetHeight) {
    if (origin == null) {
      return null;
    }
    int height = origin.getHeight();
    int width = origin.getWidth();
    float scaleWidth = ((float) targetWidth) / width;
    float scaleHeight = ((float) targetHeight) / height;
    Matrix matrix = new Matrix();
    matrix.postScale(scaleWidth, scaleHeight);// 使用后乘
    Bitmap bitmap = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
    if (!origin.isRecycled()) {
      origin.recycle();
    }
    return bitmap;
  }
}
