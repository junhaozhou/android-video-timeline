package com.littlechoc.videotimeline;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Junhao Zhou
 */

public class VideoTimeLine extends View {

  private static final String TAG = "VideoTimeLine";

  private static final int DEFAULT_HEIGHT_DP = 48;

  public static final int DEFAULT_THUMB_WIDTH = 12;

  private List<Bitmap> frameThumbs;

  private VideoInfo videoInfo;

  private Paint framePaint;

  private Paint thumbPaint;

  private Paint shadowPaint;

  private long duration;

  private float begin = 0.0f;

  private float end = 1.0f;

  private long minlength;

  public VideoTimeLine(Context context) {
    this(context, null);
  }

  public VideoTimeLine(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public VideoTimeLine(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VideoTimeLine);
    int thumbColor = typedArray.getColor(R.styleable.VideoTimeLine_thumbColor, ContextCompat.getColor(context, R.color.default_thumb_color));
    int shadowColor = typedArray.getColor(R.styleable.VideoTimeLine_shadowColor, ContextCompat.getColor(context, R.color.default_shadow_color));
    float minSecond = typedArray.getFloat(R.styleable.VideoTimeLine_minLength, 1);
    minlength = (long) (1000 * minSecond);
    typedArray.recycle();

    frameThumbs = new ArrayList<>();
    framePaint = new Paint();

    thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    thumbPaint.setColor(thumbColor);
    thumbPaint.setStyle(Paint.Style.FILL);

    shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    shadowPaint.setStyle(Paint.Style.FILL);
    shadowPaint.setColor(shadowColor);
  }

  public void setVideoInfo(VideoInfo videoInfo) {
    this.videoInfo = videoInfo;
    new Thread(new Runnable() {
      @Override
      public void run() {
        analyseVideo();
        post(new Runnable() {
          @Override
          public void run() {
            invalidate();
          }
        });

      }
    }).start();
  }

  private void analyseVideo() {
    duration = videoInfo.getDuration();
    begin = 0.0f;
    end = 1.0f;

    int videoWidth = videoInfo.getVideoWidth();
    int videoHeight = videoInfo.getVideoHeight();

    int thumbHeight = getMeasuredHeight();
    int thumbWidth = (int) (videoWidth * (1.0f * thumbHeight / videoHeight));

    int frames = (int) (1.0f * getMeasuredWidth() / thumbWidth) + 1;
    int interval = (int) (duration / (frames - 1));

    frameThumbs.clear();
    long time = 0;
    for (int i = 0; i < frames; i++) {
      frameThumbs.add(BitmapUtil.compress(videoInfo.getFrameAtTime(time), thumbWidth, thumbHeight));
      time += interval;
    }

    LogUtil.i(TAG, "duration = " + duration + " ,frames = " + frames + " ,interval = " + interval);
  }


  private boolean isBeginThumb = false;
  private boolean isEndThumb = false;

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        LogUtil.i(TAG, "x = " + event.getX() + " y = " + event.getY());
        isBeginThumb = isBeginThumb(event);
        isEndThumb = isEndThumb(event);
        LogUtil.i(TAG, "isBeginThumb = " + isBeginThumb + " isEndThumb = " + isEndThumb);
        break;
      case MotionEvent.ACTION_MOVE:
        if (isBeginThumb) {
          trackBeginThumb(event);
        }
        if (isEndThumb) {
          trackEndThumb(event);
        }
        break;
      case MotionEvent.ACTION_UP:
        isBeginThumb = false;
        isEndThumb = false;
        break;
      case MotionEvent.ACTION_CANCEL:
        break;
    }
    return true;
  }

  private boolean isBeginThumb(MotionEvent event) {
    float x = event.getX();
    float left = getPaddingLeft() + begin * getDisplayWidth();
    float right = left + dp2px(DEFAULT_THUMB_WIDTH);
    LogUtil.i(TAG, "left = " + left + " right = " + right);
    return x >= left && x <= right;
  }

  private boolean isEndThumb(MotionEvent event) {
    float x = event.getX();
    float left = getPaddingLeft() + dp2px(DEFAULT_THUMB_WIDTH) + end * getDisplayWidth();
    float right = left + dp2px(DEFAULT_THUMB_WIDTH);
    return x >= left && x <= right;
  }

  private void trackBeginThumb(MotionEvent event) {
    float x = event.getX();
    x -= getPaddingLeft();
    if (x < 0) {
      x = 0;
    }
    float beginTmp = x / getDisplayWidth();
    LogUtil.i(TAG, "beginTmp = " + beginTmp);
    float threshold = minlength * 1.0f / duration;
    if (beginTmp > end - threshold) {
      beginTmp = end - threshold;
    }
    begin = beginTmp;
    invalidate();
  }

  private void trackEndThumb(MotionEvent event) {
    float x = event.getX();
    x = x - getPaddingLeft() - dp2px(DEFAULT_THUMB_WIDTH);
    if (x < 0) {
      x = 0;
    }
    float endTmp = x / getDisplayWidth();
    LogUtil.i(TAG, "endTmp = " + endTmp);
    float threshold = minlength * 1.0f / duration;
    if (endTmp - threshold > begin) {
      if (endTmp > 1) {
        endTmp = 1.0f;
      }
    } else {
      endTmp = begin + threshold;
    }
    end = endTmp;
    invalidate();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = MeasureSpec.getSize(heightMeasureSpec);

    if (heightMode == MeasureSpec.AT_MOST) {
      height = Math.min(height, getDefaultHeight());
    } else if (heightMode == MeasureSpec.UNSPECIFIED) {
      height = getDefaultHeight();
    }

    if (widthMode == MeasureSpec.AT_MOST) {
      width = Math.min(width, getDefaultWidth());
    } else if (widthMode == MeasureSpec.UNSPECIFIED) {
      width = getDefaultWidth();
    }

    setMeasuredDimension(width, height);
  }

  private int getDefaultHeight() {
    return dp2px(DEFAULT_HEIGHT_DP);
  }

  private int getDefaultWidth() {
    return getResources().getDisplayMetrics().widthPixels; // window width
  }

  private int getDisplayWidth() {
    return getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - dp2px(2 * DEFAULT_THUMB_WIDTH);
  }

  private int getDisplayHeight() {
    return getMeasuredHeight() - getPaddingBottom() - getPaddingTop();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    int offsetX = getPaddingLeft() + dp2px(DEFAULT_THUMB_WIDTH);
    // draw background
    int frameX = offsetX;
    for (Bitmap frameThumb : frameThumbs) {
      if (frameThumb != null) {
        int width = frameThumb.getWidth();
        canvas.drawBitmap(frameThumb, frameX, getPaddingTop(), framePaint);
        frameX += width;
      }
    }

    // draw shadow
    float shadowStart = offsetX;
    float shadowEnd = begin * getDisplayWidth() + shadowStart;
    canvas.drawRect(shadowStart, getPaddingTop(), shadowEnd, getPaddingTop() + getDisplayHeight(), shadowPaint);

    shadowStart = offsetX + end * getDisplayWidth();
    shadowEnd = offsetX + getDisplayWidth();
    canvas.drawRect(shadowStart, getPaddingTop(), shadowEnd, getPaddingTop() + getDisplayHeight(), shadowPaint);

    // draw begin thumb
    float thumbX = offsetX + begin * getDisplayWidth();
    canvas.drawRect(thumbX - dp2px(DEFAULT_THUMB_WIDTH), getPaddingTop(), thumbX, getPaddingTop() + getMeasuredHeight(), thumbPaint);

    // draw end thumb
    thumbX = offsetX + end * getDisplayWidth();
    canvas.drawRect(thumbX, getPaddingTop(),
            thumbX + dp2px(DEFAULT_THUMB_WIDTH), getPaddingTop() + getMeasuredHeight(),
            thumbPaint);
  }

  private int dp2px(int dp) {
    final float scale = getContext().getResources().getDisplayMetrics().density;
    return (int) (dp * scale + 0.5f);
  }
}
