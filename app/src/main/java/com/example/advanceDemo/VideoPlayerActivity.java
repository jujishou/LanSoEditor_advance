package com.example.advanceDemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.TextView;
import android.widget.Toast;

import com.lansoeditor.advanceDemo.R;
import com.lansosdk.videoeditor.IRenderView;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.TextureRenderView;
import com.lansosdk.videoplayer.VPlayer;
import com.lansosdk.videoplayer.VideoPlayer;
import com.lansosdk.videoplayer.VideoPlayer.OnPlayerCompletionListener;
import com.lansosdk.videoplayer.VideoPlayer.OnPlayerPreparedListener;

import java.io.IOException;

/**
 * 视频播放
 */
public class VideoPlayerActivity extends Activity {

    private static final boolean VERBOSE = true;
    private static final String TAG = "VideoPlayerActivity";
    String videoPath = null;
    private TextureRenderView textureView;
    private MediaPlayer mediaPlayer = null;
    private VPlayer vplayer = null;
    private boolean isSupport = false;
    private int screenWidth, screenHeight;
    private MediaInfo mediaInfo;

    private TextView tvVideoDuration;
    private TextView tvPlayWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_layout);
        textureView = (TextureRenderView) findViewById(R.id.surface1);

        videoPath = getIntent().getStringExtra("videopath");

        TextView tvScreen = (TextView) findViewById(R.id.id_palyer_screenhinit);
        TextView tvVideoRatio = (TextView) findViewById(R.id.id_palyer_videoRatio);
        tvVideoDuration = (TextView) findViewById(R.id.id_palyer_videoduration);

        tvPlayWidget = (TextView) findViewById(R.id.id_palyer_widget);

        DisplayMetrics dm = new DisplayMetrics();
        dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        String str = "屏幕分辨率：";
        str += String.valueOf(screenWidth);
        str += "x";
        str += String.valueOf(screenHeight);
        tvScreen.setText(str);

        mediaInfo = new MediaInfo(videoPath);

        if (mediaInfo.prepare() == false) {
            showHintDialog();
            isSupport = false;
        } else {
            Log.i(TAG, "info:" + mediaInfo.toString());
            isSupport = true;
            str = "视频分辨率：";
            str += String.valueOf(mediaInfo.getWidth());
            str += "x";
            str += String.valueOf(mediaInfo.getHeight());
            tvVideoRatio.setText(str);

            str = "视频时长:";
            str += String.valueOf(mediaInfo.vDuration);
            tvVideoDuration.setText(str);
        }

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
                                                    int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface,
                                                  int width, int height) {
                if (isSupport) {
                    play(new Surface(surface)); // 采用系统本身的MediaPlayer播放
//					 startVPlayer(new Surface(surface)); //我们SDK提供的播放器.
                }
            }
        });
    }

    private void showHintDialog() {

        new AlertDialog.Builder(this).setTitle("提示")
                .setMessage("抱歉,暂时不支持当前视频格式")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();

    }

    public void play(Surface surface) {

        if (videoPath == null)
            return;

        tvPlayWidget.setText("播放控件: MediaPlayer");
        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                Toast.makeText(VideoPlayerActivity.this, "视频播放完毕!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        try {
            mediaPlayer.setDataSource(videoPath);
            mediaPlayer.setSurface(surface);
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
            textureView.setDispalyRatio(IRenderView.AR_ASPECT_FIT_PARENT);

            textureView.setVideoSize(mediaPlayer.getVideoWidth(),
                    mediaPlayer.getVideoHeight());
            textureView.requestLayout();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startVPlayer(final Surface surface) {
        vplayer = new VPlayer(this);
        vplayer.setVideoPath(videoPath);
        tvPlayWidget.setText("播放控件是:SDK提供的VPlayer");
        vplayer.setOnPreparedListener(new OnPlayerPreparedListener() {

            @Override
            public void onPrepared(VideoPlayer mp) {
                vplayer.setSurface(surface);

                textureView.setDispalyRatio(IRenderView.AR_ASPECT_FIT_PARENT);

                textureView.setVideoSize(mediaInfo.getWidth(), mediaInfo.getHeight());
                textureView.requestLayout();
                vplayer.start();
                vplayer.setLooping(true);
            }
        });
        vplayer.setOnCompletionListener(new OnPlayerCompletionListener() {

            @Override
            public void onCompletion(VideoPlayer mp) {
                Toast.makeText(VideoPlayerActivity.this, "视频播放完成", Toast.LENGTH_SHORT).show();
            }
        });
        vplayer.prepareAsync();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (vplayer != null) {
            vplayer.stop();
            vplayer.release();
            vplayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
