package com.example.advanceDemo.camera;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageMultiplyBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBeautyAdvanceFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongMaskColorFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.Rotation;

import com.example.advanceDemo.DemoUtil;
import com.example.advanceDemo.VideoPlayerActivity;
import com.example.advanceDemo.scene.OutBodyDemoActivity;
import com.example.advanceDemo.view.FocusImageView;
import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.CameraLayer;
import com.lansosdk.box.CameraLayerFadeListener;
import com.lansosdk.box.CanvasLayer;
import com.lansosdk.box.CanvasRunnable;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadCameraRunnable;
import com.lansosdk.box.LanSongAlphaPixelFilter;
import com.lansosdk.box.MVLayer;
import com.lansosdk.box.SubLayer;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.ViewLayerRelativeLayout;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadThreadProgressListener;
import com.lansosdk.videoeditor.CopyDefaultVideoAsyncTask;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.DrawPadCameraView;
import com.lansosdk.videoeditor.FilterLibrary;
import com.lansosdk.videoeditor.LanSongUtil;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.DrawPadCameraView.doFousEventListener;
import com.lansosdk.videoeditor.DrawPadCameraView.onViewAvailable;
import com.lansosdk.videoeditor.FilterLibrary.OnGpuImageFilterChosenListener;
import com.lansosdk.videoplayer.VPlayer;
import com.lansosdk.videoplayer.VideoPlayer;
import com.lansosdk.videoplayer.VideoPlayer.OnPlayerCompletionListener;
import com.lansosdk.videoplayer.VideoPlayer.OnPlayerPreparedListener;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 在部分华为手机上出现前置摄像头, 画面倒置的问题, 解决如下:
 * <p>
 * 方案1, 如果当前Activity继承自原生Activity; 则manifest.xml中的代码如下:
 * <activity android:name="com.example.advanceDemo.CameraLayerFullLandscapeActivity"
 * android:screenOrientation="landscape"
 * android:theme="@android:style/Theme.NoTitleBar.Fullscreen"
 * >
 * </activity>
 * 方案2, 如果当前Activity继承自v7包的 AppCompatActivity,则manifest.xml的代码如下:
 * <activity android:name="com.example.advanceDemo.CameraLayerFullLandscapeActivity"
 * android:screenOrientation="landscape"
 * android:theme="@style/Theme.AppCompat.Light.NoActionBar.FullScreen"
 * >
 * </activity>
 * 其中theme需要定义在styles.xml中如下:
 * <style name="Theme.AppCompat.Light.NoActionBar.FullScreen" parent="@style/Theme.AppCompat.Light">
 * <item name="windowNoTitle">true</item>
 * <item name="windowActionBar">false</item>
 * <item name="android:windowFullscreen">true</item>
 * <item name="android:windowContentOverlay">@null</item>
 * </style>
 */
public class CameraLayerKTVDemoActivity extends Activity implements OnClickListener, OnSeekBarChangeListener, CameraLayerFadeListener {
    //public class CameraLayerKTVDemoActivity extends AppCompatActivity implements OnClickListener,OnSeekBarChangeListener{
    private static final long RECORD_CAMERA_TIME = 60 * 1000 * 1000; //300秒.
    private static final String TAG = "CameraLayerFullLandscapeActivity";

    private DrawPadCameraView drawPadCamera;

    private CameraLayer cameraLayer = null;

    private String dstPath = null;


    private PowerManager.WakeLock mWakeLock;
    private ViewLayer mViewLayer = null;
    private ViewLayerRelativeLayout mLayerRelativeLayout;

    private LanSongAlphaPixelFilter alphaPixelFilter;
    private LanSongBeautyAdvanceFilter beautyFilter;
    int zoomCnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        srcVideoPath = getIntent().getStringExtra("videopath");
        LanSongUtil.hideBottomUIMenu(this);

        setContentView(R.layout.cameralayer_ktv_demo_layout);

        if (LanSongUtil.checkRecordPermission(getBaseContext()) == false) {
            Toast.makeText(getApplicationContext(), "请打开权限后,重试!!!", Toast.LENGTH_LONG).show();
            finish();
        }

        drawPadCamera = (DrawPadCameraView) findViewById(R.id.id_ktvdemo_drawpadcameraview);
        dstPath = SDKFileUtils.newMp4PathInBox();

        initView();
        initDrawPad();
        DemoUtil.showHintDialog(CameraLayerKTVDemoActivity.this, "此功能 需要对着绿背景拍摄,类似演员在绿幕前表演,共3个图层, 最底层是场景视频,中间层是摄像机,上层是UI");
    }

    private boolean isZoomed = false;

    @Override
    protected void onResume() {
        super.onResume();
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
            mWakeLock.acquire();
        }
        playVideo.setVisibility(View.GONE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startDrawPad();
            }
        }, 200);
    }

    /**
     * Step1: 开始运行 DrawPad 容器
     */
    private void initDrawPad() {
        //因手机屏幕是16:9;全屏模式,建议分辨率设置为960x544;
        int padWidth = 960;
        int padHeight = 544;
        int bitrate = 3000 * 1024;
        int frameRate = 25;

        drawPadCamera.setRealEncodeEnable(padWidth, padHeight, bitrate, frameRate, dstPath);
        /**
         * 设置进度回调
         */
        drawPadCamera.setOnDrawPadProgressListener(drawPadProgressListener);
        drawPadCamera.setOnDrawPadThreadProgressListener(new onDrawPadThreadProgressListener() {

            @Override
            public void onThreadProgress(DrawPad v, long currentTimeUs) {

                //如果第二个视频要切换
                if (isChangedVideo) {
                    //重新增加这个图层.
                    drawPadCamera.removeLayer(currentLayer);
                    currentLayer = null;
                    currentLayer = drawPadCamera.addVideoLayer(videoWidth, videoHeight, null);
                    drawPadCamera.changeLayerPosition(currentLayer, 0);

                    if (vplayer2 != null) {
                        vplayer2.setSurface(new Surface(currentLayer.getVideoTexture()));
                        vplayer2.start();
                    } else if (vplayer3 != null) {
                        vplayer3.setSurface(new Surface(currentLayer.getVideoTexture()));
                        vplayer3.start();
                    }

                    isChangedVideo = false;
                }
            }
        });

        drawPadCamera.setRecordMic(true);
        alphaPixelFilter = new LanSongAlphaPixelFilter();

        beautyFilter = new LanSongBeautyAdvanceFilter();
//    	drawPadCamera.setCameraParam(false, beautyFilter,true);  //设置为美颜.

        drawPadCamera.setCameraParam(false, alphaPixelFilter, true);  //设置是否前置.
        drawPadCamera.setOnViewAvailable(new onViewAvailable() {

            @Override
            public void viewAvailable(DrawPadCameraView v) {
                startDrawPad();
            }
        });
    }

    /**
     * Step2: 开始运行 Drawpad线程.
     */
    private void startDrawPad() {
        if (drawPadCamera.setupDrawpad()) {
            cameraLayer = drawPadCamera.getCameraLayer();
            addVideoLayer();
            addViewLayer();
            drawPadCamera.startPreview();
            drawPadCamera.startRecord();
        }
    }

    /**
     * Step3: 停止容器, 停止后,为新的视频文件增加上音频部分.
     */
    private void stopDrawPad() {
        if (drawPadCamera != null && drawPadCamera.isRunning()) {
            drawPadCamera.stopDrawPad();
            Log.i(TAG, "onViewAvaiable  drawPad停止工作!!!!	");
            toastStop();
            cameraLayer = null;

            if (vplayer != null) {
                vplayer.stop();
                vplayer.release();
                vplayer = null;
            }
            if (vplayer2 != null) {
                vplayer2.stop();
                vplayer2.release();
                vplayer2 = null;
            }
            if (vplayer3 != null) {
                vplayer3.stop();
                vplayer3.release();
                vplayer3 = null;
            }
        }
        playVideo.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (drawPadCamera != null) {
            drawPadCamera.stopDrawPad();
        }
        if (vplayer != null) {
            vplayer.stop();
            vplayer.release();
            vplayer = null;
        }
        if (vplayer2 != null) {
            vplayer2.stop();
            vplayer2.release();
            vplayer2 = null;
        }
        if (vplayer3 != null) {
            vplayer3.stop();
            vplayer3.release();
            vplayer3 = null;
        }
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SDKFileUtils.deleteFile(dstPath);
        dstPath = null;
    }

    private onDrawPadProgressListener drawPadProgressListener = new onDrawPadProgressListener() {

        @Override
        public void onProgress(DrawPad v, long currentTimeUs) {
            if (currentTimeUs >= RECORD_CAMERA_TIME) {
                stopDrawPad();
            }
            if (tvTime != null) {
                long left = RECORD_CAMERA_TIME - currentTimeUs;

                float leftF = ((float) left / 1000000);
                float b = (float) (Math.round(leftF * 10)) / 10;  //保留一位小数.

                if (b >= 0)
                    tvTime.setText(String.valueOf(b));
            }
        }
    };

    /**
     * 增加一个UI图层: ViewLayer
     */
    private void addViewLayer() {
        mLayerRelativeLayout = (ViewLayerRelativeLayout) findViewById(R.id.id_ktvdemo_viewlaylayout);
        mLayerRelativeLayout.setVisibility(View.VISIBLE);
        if (drawPadCamera != null && drawPadCamera.isRunning()) {
            mViewLayer = drawPadCamera.addViewLayer();
            mLayerRelativeLayout.bindViewLayer(mViewLayer);
            mLayerRelativeLayout.invalidate();//刷新一下.

            ViewGroup.LayoutParams params = mLayerRelativeLayout.getLayoutParams();
            params.height = mViewLayer.getPadHeight();  //因为布局时, 宽度一致, 这里调整高度,让他们一致.
            mLayerRelativeLayout.setLayoutParams(params);
        }
    }


    private VPlayer vplayer = null;
    private VPlayer vplayer2 = null;
    private VPlayer vplayer3 = null;
    private VideoLayer currentLayer = null;
    private boolean isChangedVideo = false;
    private int videoWidth, videoHeight;
    private String srcVideoPath;

    /**
     * 增加一个视频图层.
     */
    private void addVideoLayer() {
        if (srcVideoPath != null && drawPadCamera != null && drawPadCamera.isRunning()) {

            vplayer = new VPlayer(CameraLayerKTVDemoActivity.this);
            vplayer.setVideoPath(srcVideoPath);
            vplayer.setOnPreparedListener(new OnPlayerPreparedListener() {

                @Override
                public void onPrepared(VideoPlayer mp) {

                    if (drawPadCamera != null && drawPadCamera.isRunning()) {

                        currentLayer = drawPadCamera.addVideoLayer(vplayer.getVideoWidth(), vplayer.getVideoHeight(), null);
                        vplayer.setSurface(new Surface(currentLayer.getVideoTexture()));
                        vplayer.start();

                        drawPadCamera.changeLayerPosition(currentLayer, 0);
                        //cameraLayer.setScale(0.5f);
                    }
                }
            });
            vplayer.prepareAsync();

        }

    }

    private void prepareVideo2() {
        if (vplayer2 == null) {
            //准备第二个视频,
            String path = CopyFileFromAssets.copyAssets(getApplicationContext(), "ping25s.mp4");
            vplayer2 = new VPlayer(CameraLayerKTVDemoActivity.this);
            vplayer2.setVideoPath(path);
            vplayer2.setOnPreparedListener(new OnPlayerPreparedListener() {

                @Override
                public void onPrepared(VideoPlayer mp) {

                    //第二个视频准备好后, 停止第一个视频.
                    if (vplayer != null) {
                        vplayer.stop();
                        vplayer.release();
                        vplayer = null;
                    }
                    if (vplayer3 != null) {
                        vplayer3.stop();
                        vplayer3.release();
                        vplayer3 = null;
                    }
                    isChangedVideo = true;  //标志下切换.在线程内部切换;
                    videoWidth = mp.getVideoWidth();
                    videoHeight = mp.getVideoHeight();
                }
            });
            vplayer2.prepareAsync();
        }
    }

    private void prepareVideo3() {
        if (vplayer3 == null) {
            //准备第二个视频,
            vplayer3 = new VPlayer(CameraLayerKTVDemoActivity.this);
            vplayer3.setVideoPath(srcVideoPath);
            vplayer3.setOnPreparedListener(new OnPlayerPreparedListener() {

                @Override
                public void onPrepared(VideoPlayer mp) {

                    //第二个视频准备好后, 停止第一个视频.
                    if (vplayer2 != null) {
                        vplayer2.stop();
                        vplayer2.release();
                        vplayer2 = null;
                    }

                    isChangedVideo = true;  //标志下切换.在线程内部切换;
                    videoWidth = mp.getVideoWidth();
                    videoHeight = mp.getVideoHeight();
                }
            });
            vplayer3.prepareAsync();
        }
    }

    //-------------------------------------------一下是UI界面和控制部分.---------------------------------------------------
    private LinearLayout playVideo;
    private TextView tvTime;

    private void initView() {
        tvTime = (TextView) findViewById(R.id.id_ktvdemo_timetv);

        playVideo = (LinearLayout) findViewById(R.id.id_ktvdemo_saveplay);
        playVideo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (SDKFileUtils.fileExist(dstPath)) {
                    Intent intent = new Intent(CameraLayerKTVDemoActivity.this, VideoPlayerActivity.class);
                    intent.putExtra("videopath", dstPath);
                    startActivity(intent);
                } else {
                    Toast.makeText(CameraLayerKTVDemoActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
                }
            }
        });
        playVideo.setVisibility(View.GONE);


        SeekBar skbar = (SeekBar) findViewById(R.id.id_ktvdemo_skbar_fine);
        skbar.setOnSeekBarChangeListener(this);
        skbar.setMax(50);  //细调最大定为0.5

        skbar = (SeekBar) findViewById(R.id.id_ktvdemo_skbar_sketchy);
        skbar.setOnSeekBarChangeListener(this);
        skbar.setMax(20);  //粗调最大定为0.2;


        findViewById(R.id.id_ktvdemo_flashlight).setOnClickListener(this);
        findViewById(R.id.id_ktvdemo_frontcamera).setOnClickListener(this);
        findViewById(R.id.id_ktvdemo_filter).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_ktvdemo_frontcamera:
                if (cameraLayer != null) {
                    if (drawPadCamera.isRunning() && CameraLayer.isSupportFrontCamera()) {
                        //先把DrawPad暂停运行.
                        drawPadCamera.pausePreview();
                        cameraLayer.changeCamera();
                        drawPadCamera.resumePreview(); //再次开启.
                    }
                }
                break;
            case R.id.id_ktvdemo_flashlight:
                if (cameraLayer != null) {
                    cameraLayer.changeFlash();
                }
                break;
            case R.id.id_ktvdemo_filter:
                break;
            default:
                break;
        }
    }

    private void toastStop() {
        Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "录制已停止!!");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.id_ktvdemo_skbar_fine:
                if (alphaPixelFilter != null) {
                    float fine = (float) progress / 100f;
                    alphaPixelFilter.setFineAdjust(fine);
                }
                break;
            case R.id.id_ktvdemo_skbar_sketchy:
                if (alphaPixelFilter != null) {
                    float fine = (float) progress / 100f;
                    alphaPixelFilter.setSketchAdjust(fine);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    //-----------------------------------------------
    @Override
    public void onLayerCacheAlready(CameraLayer layer) {

        if (isZoomed) {
            cameraLayer.setZoom(0);
        } else {
            cameraLayer.setZoom(80);
            isZoomed = true;
        }
    }

    @Override
    public boolean onSubLayerFading(CameraLayer layer, SubLayer sublayer) {
        sublayer.setPosition(sublayer.getPositionX() + 2, sublayer.getPositionY());
        return false;
    }

    @Override
    public void onSubLayerFadeEnd(CameraLayer layer) {

    }
}
