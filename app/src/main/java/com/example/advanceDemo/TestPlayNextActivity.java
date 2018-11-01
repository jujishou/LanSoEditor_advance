package com.example.advanceDemo;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import com.lansoeditor.advanceDemo.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 测试 MediaPlayer 的 多个视频级联播放; 用setNextMediaPlayer
 * 勿删!!!
 */
public class TestPlayNextActivity extends Activity {
    private MediaPlayer firstPlayer,
            nextMediaPlayer,
            cachePlayer,
            currentPlayer;

    private Surface playerSurface;
    private ArrayList<String> VideoListQueue = new ArrayList<String>();
    private HashMap<String, MediaPlayer> playersCache = new HashMap<String, MediaPlayer>();
    private int currentVideoIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_playnext);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firstPlayer != null) {
            if (firstPlayer.isPlaying()) {
                firstPlayer.stop();
            }
            firstPlayer.release();
        }
        if (nextMediaPlayer != null) {
            if (nextMediaPlayer.isPlaying()) {
                nextMediaPlayer.stop();
            }
            nextMediaPlayer.release();
        }

        if (currentPlayer != null) {
            if (currentPlayer.isPlaying()) {
                currentPlayer.stop();
            }
            currentPlayer.release();
        }
        currentPlayer = null;
    }

    private void initView() {
        TextureView textureView = (TextureView) findViewById(R.id.surface);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
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
                getVideoUrls();
                playerSurface=new Surface(surface);
                initFirstPlayer(playerSurface);
            }
        });
    }
    /*
     * 初始化播放首段视频的player
     */
    private void initFirstPlayer(Surface surface) {
        firstPlayer = new MediaPlayer();
        firstPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        firstPlayer.setSurface(surface);
        firstPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                onVideoPlayCompleted(mp);
            }
        });
        //设置cachePlayer为该player对象
        cachePlayer = firstPlayer;
        initNexttPlayer();

        startPlayFirstVideo();
    }

    private void startPlayFirstVideo() {
        try {
            firstPlayer.setDataSource(VideoListQueue.get(currentVideoIndex));
            firstPlayer.prepare();
            firstPlayer.start();
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }

    /**
     * 新开线程负责初始化负责播放剩余视频分段的player对象,避免UI线程做过多耗时操作
     */
    private void initNexttPlayer() {
        new Thread(new Runnable() {

            @Override
            public void run() {

                for (int i = 1; i < VideoListQueue.size(); i++) {
                    nextMediaPlayer = new MediaPlayer();
                    nextMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    nextMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    onVideoPlayCompleted(mp);
                                }
                    });
                    try {
                        nextMediaPlayer.setDataSource(VideoListQueue.get(i));
                        nextMediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    cachePlayer.setNextMediaPlayer(nextMediaPlayer);
                    cachePlayer = nextMediaPlayer;
                    playersCache.put(String.valueOf(i), nextMediaPlayer);
                }
            }
        }).start();
    }

    private void onVideoPlayCompleted(MediaPlayer mp) {
        mp.setDisplay(null);
        currentPlayer = playersCache.get(String.valueOf(++currentVideoIndex));
        if (currentPlayer != null) {
            currentPlayer.setSurface(playerSurface);
        } else {
            Toast.makeText(TestPlayNextActivity.this, "视频播放完毕..", Toast.LENGTH_SHORT).show();
        }
    }

    private void getVideoUrls() {
        VideoListQueue.add("/sdcard/d1.mp4");
        VideoListQueue.add("/sdcard/TEST_720P_15s.mp4");
        VideoListQueue.add("/sdcard/TEST_720P_90DU.mp4");
        VideoListQueue.add("/sdcard/d3.mp4");
        VideoListQueue.add("/sdcard/d4.mp4");
    }
}