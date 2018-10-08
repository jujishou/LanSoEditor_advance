package com.example.advanceDemo.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.advanceDemo.VideoPlayerActivity;
import com.example.advanceDemo.view.CameraProgressBar;
import com.example.advanceDemo.view.FocusImageView;
import com.lansoeditor.advanceDemo.R;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.CameraLayer;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.MVLayer;
import com.lansosdk.box.onDrawPadErrorListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.videoeditor.BeautyManager;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.DrawPadCameraView;
import com.lansosdk.videoeditor.DrawPadCameraView.doFousEventListener;
import com.lansosdk.videoeditor.DrawPadCameraView.onViewAvailable;
import com.lansosdk.videoeditor.FilterLibrary;
import com.lansosdk.videoeditor.FilterLibrary.OnGpuImageFilterChosenListener;
import com.lansosdk.videoeditor.LanSongUtil;
import com.lansosdk.videoeditor.LanSongFileUtil;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;

@SuppressLint("SdCardPath")
public class CameraLayerFullPortActivity extends Activity implements
        OnClickListener {

    private static final int RECORD_CAMERA_MAX = 15 * 1000 * 1000; // 定义录制的时间为30s

    private static final int RECORD_CAMERA_MIN = 2 * 1000 * 1000; // 定义最小2秒

    private static final String TAG = "CameraFullRecord";
    // ------------------------------------------一下是UI界面和控制部分.---------------------------------------------------
    private DrawPadCameraView drawPadCamera;
    private CameraLayer cameraLayer = null;
    private String dstPath = null; // 用于录制完成后的目标视频路径.
    private FocusImageView focusView;
    private PowerManager.WakeLock mWakeLock;
    private TextView tvTime;
    private Context mContext = null;
    private ImageView btnOk;
    private CameraProgressBar mProgressBar = null;
    private onDrawPadProgressListener drawPadProgressListener = new onDrawPadProgressListener() {

        @Override
        public void onProgress(DrawPad v, long currentTimeUs) {
            if (currentTimeUs >= RECORD_CAMERA_MIN && btnOk != null) {
                btnOk.setVisibility(View.VISIBLE);
            }

            if (currentTimeUs >= RECORD_CAMERA_MAX) {
                stopDrawPad();
                playVideo();
            }
            if (tvTime != null) {
                float timeF = ((float) currentTimeUs / 1000000);
                float b = (float) (Math.round(timeF * 10)) / 10; // 保留一位小数.

                if (b >= 0)
                    tvTime.setText(String.valueOf(b));
            }
            if (mProgressBar != null) {
                mProgressBar.setProgress((int) (currentTimeUs / 1000));
            }

        }
    };
    private BitmapLayer bmpLayer;
    private MVLayer mvLayer;
    private MediaPlayer mplayer2 = null;
    private BeautyManager mBeautyMng;
    private float beautyLevel = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 全屏模式下, 隐藏底部的虚拟按键.
        LanSongUtil.hideBottomUIMenu(this);
        mContext = getApplicationContext();
        if (LanSongUtil.checkRecordPermission(getBaseContext()) == false) {
            Toast.makeText(getApplicationContext(), "当前无权限,请打开权限后,重试!!!",
                    Toast.LENGTH_LONG).show();
            finish();
        }

        setContentView(R.layout.camera_full_record_layout);

        drawPadCamera = (DrawPadCameraView) findViewById(R.id.id_fullrecord_padview);

        initView();
        initBeautyView();
        mProgressBar.setMaxProgress(RECORD_CAMERA_MAX / 1000);
        mProgressBar.setOnProgressTouchListener(new CameraProgressBar.OnProgressTouchListener() {
                    @Override
                    public void onClick(CameraProgressBar progressBar) {

                        if (drawPadCamera != null) {
                            /**
                             * 这里只是暂停和恢复录制, 可以录制多段,但不可以删除录制好的每一段,
                             *
                             * 如果你要分段录制,并支持回删,则可以采用SegmentStart和SegmentStop;
                             */
                            if (drawPadCamera.isRecording()) {
                                drawPadCamera.pauseRecord();
                            } else {
                                drawPadCamera.startRecord();
                            }
                        }
                    }
                });

        dstPath = LanSongFileUtil.newMp4PathInBox();
        initDrawPad(); // 开始录制.
    }

    @Override
    protected void onResume() {
        LanSongUtil.hideBottomUIMenu(this);
        super.onResume();
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
                    TAG);
            mWakeLock.acquire();
        }
        startDrawPad();
    }

    /**
     * Step1: 开始运行 DrawPad 容器
     */
    private void initDrawPad() {
        int padWidth = 544;
        int padHeight = 960;
        int bitrate = 3000 * 1024;
        /**
         * 设置录制时的一些参数.
         */
        drawPadCamera.setRealEncodeEnable(padWidth, padHeight, bitrate, (int) 25, dstPath);
        /**
         * 设置录制处理进度监听.
         */
        drawPadCamera.setOnDrawPadProgressListener(drawPadProgressListener);


        drawPadCamera.setCameraParam(true, null);
        drawPadCamera.setCameraFocusListener(new doFousEventListener() {

            @Override
            public void onFocus(int x, int y) {
                focusView.startFocus(x, y);
            }
        });
        drawPadCamera.setRecordMic(true);
        drawPadCamera.setOnViewAvailable(new onViewAvailable() {

            @Override
            public void viewAvailable(DrawPadCameraView v) {
                startDrawPad();
            }
        });
        drawPadCamera.setOnDrawPadErrorListener(new onDrawPadErrorListener() {

            @Override
            public void onError(DrawPad d, int what) {
                Log.e(TAG, "DrawPad容器线程运行出错!!!" + what);
            }
        });
    }

    /**
     * Step2: 开始运行 Drawpad线程.
     */
    private void startDrawPad() {
        // 如果是屏幕比例大于16:9,则需要重新设置编码参数, 从而画面不变形
        if (LanSongUtil.isFullScreenRatio(drawPadCamera.getViewWidth(), drawPadCamera.getViewHeight())) {
            drawPadCamera.setRealEncodeEnable(544, 1088, 3500 * 1024, (int) 25, dstPath);
        }
        if (drawPadCamera.setupDrawpad()) // 建立图层.
        {
            cameraLayer = drawPadCamera.getCameraLayer(); // 临时
            if (cameraLayer != null) {
                drawPadCamera.startPreview();
            }
        } else {
            Log.i(TAG, "建立drawpad线程失败.");
        }
    }

    /**
     * Step3: 停止容器, 停止后,为新的视频文件增加上音频部分.
     */
    private void stopDrawPad() {
        if (drawPadCamera != null && drawPadCamera.isRunning()) {
            drawPadCamera.stopDrawPad();
            cameraLayer = null;
        }
    }

    /**
     * 选择滤镜效果,
     */
    private void selectFilter() {
        if (drawPadCamera != null && drawPadCamera.isRunning()) {
            FilterLibrary.showDialog(this,
                    new OnGpuImageFilterChosenListener() {

                        @Override
                        public void onGpuImageFilterChosenListener(
                                final GPUImageFilter filter, String name) {
                            if (cameraLayer != null) {
                                cameraLayer.switchFilterTo(filter);
                            }
                        }
                    });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (drawPadCamera != null) {
            drawPadCamera.stopDrawPad();
        }
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopDrawPad();
    }

    /**
     * 增加一个图片图层;
     */
    private void addBitmapLayer() {
        if (drawPadCamera != null && drawPadCamera.isRunning()) {
            String bitmapPath = CopyFileFromAssets.copyAssets(getApplicationContext(), "small.png");

            bmpLayer = drawPadCamera.addBitmapLayer(BitmapFactory.decodeFile(bitmapPath));
            // 把位置放到中间的右侧, 因为获取的高度是中心点的高度.
            bmpLayer.setPosition(
                    bmpLayer.getPadWidth() - bmpLayer.getLayerWidth() / 2,
                    bmpLayer.getPositionY());
        }
    }

    /**
     * 增加MV图层;
     */
    private void addMVLayer() {
        if (mvLayer != null) {
            drawPadCamera.removeLayer(mvLayer);
            mvLayer = null;
        }
        String colorMVPath = CopyFileFromAssets.copyAssets(
                CameraLayerFullPortActivity.this, "mei.mp4");
        String maskMVPath = CopyFileFromAssets.copyAssets(
                CameraLayerFullPortActivity.this, "mei_b.mp4");

        mvLayer = drawPadCamera.addMVLayer(colorMVPath, maskMVPath); // <-----增加MVLayer
        /**
         * mv在播放完后, 有3种模式,消失/停留在最后一帧/循环.默认是循环.
         * 	mvLayer.setEndMode(MVLayerENDMode.INVISIBLE);
         */
    }

//    /**
//     * 增加效果视频
//     */
//    private void addEffectVideo() {
//        mplayer2 = new MediaPlayer();
//        try {
//            mplayer2.setDataSource("/sdcard/taohua.mp4");
//            mplayer2.prepare();
//            /**
//             * 从摄像头图层获取一个surface, 作为视频的输出窗口
//             */
//            mplayer2.setSurface(new Surface(cameraLayer.getVideoTexture2()));
//            mplayer2.start();
//
//            /**
//             * 把视频的滤镜 设置到摄像头图层中. 当然您也可以用switchFilterList来增加多个滤镜对象.比如先美颜,
//             * 最后增加效果视频.
//             */
//            cameraLayer.switchFilterTo(cameraLayer.getEffectFilter());
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void initView() {
        findViewById(R.id.id_fullrecord_cancel).setOnClickListener(this);

        tvTime = (TextView) findViewById(R.id.id_fullscreen_timetv);

        btnOk = (ImageView) findViewById(R.id.id_fullrecord_ok);
        btnOk.setOnClickListener(this);

        focusView = (FocusImageView) findViewById(R.id.id_fullrecord_focusview);

        findViewById(R.id.id_fullrecord_flashlight).setOnClickListener(this);
        findViewById(R.id.id_fullrecord_frontcamera).setOnClickListener(this);
        findViewById(R.id.id_fullrecord_filter).setOnClickListener(this);
        mProgressBar = (CameraProgressBar) findViewById(R.id.id_fullrecord_progress);
    }

    private void initBeautyView() {
        mBeautyMng = new BeautyManager(getApplicationContext());
        findViewById(R.id.id_camerabeauty_btn).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (beautyLevel == 0.0f) {  //美颜加美颜;
                            mBeautyMng.addBeauty(drawPadCamera
                                    .getCameraLayer());
                            beautyLevel += 0.22f;
                        } else {
                            beautyLevel += 0.1f;
                            mBeautyMng.setWarmCool(beautyLevel);
                            Log.i(TAG, "调色, 数值是:" + beautyLevel);

                            if (beautyLevel >= 1.0f) {
                                mBeautyMng.deleteBeauty(drawPadCamera.getCameraLayer());
                                beautyLevel = 0.0f;
                            }
                        }
                    }
                });
        findViewById(R.id.id_camerabeauty_brightadd_btn).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mBeautyMng.increaseBrightness(drawPadCamera.getCameraLayer());
                    }
                });
        findViewById(R.id.id_camerabeaty_brightsub_btn).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mBeautyMng.discreaseBrightness(drawPadCamera.getCameraLayer());
                    }
                });
    }

    private void playVideo() {
        if (LanSongFileUtil.fileExist(dstPath)) {
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("videopath", dstPath);
            startActivity(intent);
        } else {
            Toast.makeText(this, "目标文件不存在", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_fullrecord_cancel:

                this.finish();
                break;
            case R.id.id_fullrecord_ok:
                stopDrawPad();
                playVideo();
                break;
            case R.id.id_fullrecord_frontcamera:
                if (cameraLayer != null) {
                    if (drawPadCamera.isRunning() && CameraLayer.isSupportFrontCamera()) {
                        // 先把DrawPad暂停运行.
                        drawPadCamera.pausePreview();
                        cameraLayer.changeCamera();
                        drawPadCamera.resumePreview(); // 再次开启.
                    }
                }
                break;
            case R.id.id_fullrecord_flashlight:
                if (cameraLayer != null) {
                    cameraLayer.changeFlash();
                }
                break;
            case R.id.id_fullrecord_filter:
                selectFilter();
                break;
            default:
                break;
        }
    }
}