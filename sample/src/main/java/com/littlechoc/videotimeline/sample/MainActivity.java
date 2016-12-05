package com.littlechoc.videotimeline.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.littlechoc.videotimeline.VideoInfo;
import com.littlechoc.videotimeline.VideoTimeLine;

public class MainActivity extends AppCompatActivity {

  public static final int REQUEST_CODE = 100;

  private VideoTimeLine videoTimeLine;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    videoTimeLine = (VideoTimeLine) findViewById(R.id.video_timeline);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_home, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    switch (id) {
      case R.id.menu_open:
        chooseVideo();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void chooseVideo() {
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("video/*");
    startActivityForResult(intent, REQUEST_CODE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        if (data != null) {
          Uri uri = data.getData();
          if (uri != null) {
            Log.i("Choose Video", uri.toString());
            VideoInfo videoInfo = new VideoInfo();
            videoInfo.setDataSource(this, uri);
            videoTimeLine.setVideoInfo(videoInfo);
          }
        }
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

}
