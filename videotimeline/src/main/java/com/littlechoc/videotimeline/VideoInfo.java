package com.littlechoc.videotimeline;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * @author Junhao Zhou 2016/12/3
 */

public class VideoInfo {

  private MediaMetadataRetriever mediaMetadataRetriever;

  public VideoInfo() {
    this.mediaMetadataRetriever = new MediaMetadataRetriever();
  }

  public void setDataSource(@NonNull String path) {
    mediaMetadataRetriever.setDataSource(path);
  }

  public void setDataSource(@NonNull Context context, @NonNull Uri uri) {
    mediaMetadataRetriever.setDataSource(context, uri);
  }

  public MediaMetadataRetriever getMediaMetadataRetriever() {
    return mediaMetadataRetriever;
  }

  public long getDuration() {
    String duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    return duration == null ? 0 : Long.parseLong(duration);
  }

  public int getVideoWidth() {
    String width = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
    return width == null ? 0 : Integer.parseInt(width);
  }

  public int getVideoHeight() {
    String height = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
    return height == null ? 0 : Integer.parseInt(height);
  }

  public Bitmap getFrameAtTime(long time) {
    return mediaMetadataRetriever.getFrameAtTime(time, MediaMetadataRetriever.OPTION_NEXT_SYNC);
  }
}
