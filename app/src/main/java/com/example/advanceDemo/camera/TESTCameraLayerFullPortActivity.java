package com.example.advanceDemo.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.advanceDemo.VideoPlayerActivity;
import com.example.advanceDemo.view.CameraProgressBar;
import com.example.advanceDemo.view.FocusImageView;
import com.lansoeditor.advanceDemo.R;
import com.lansosdk.box.CameraLayer;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.onDrawPadErrorListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.videoeditor.BeautyManager;
import com.lansosdk.videoeditor.DrawPadCameraView;
import com.lansosdk.videoeditor.DrawPadCameraView.doFousEventListener;
import com.lansosdk.videoeditor.DrawPadCameraView.onViewAvailable;
import com.lansosdk.videoeditor.FilterLibrary;
import com.lansosdk.videoeditor.FilterLibrary.OnGpuImageFilterChosenListener;
import com.lansosdk.videoeditor.LanSongUtil;
import com.lansosdk.videoeditor.SDKFileUtils;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;

@SuppressLint("SdCardPath")
public class TESTCameraLayerFullPortActivity extends Activity implements
        OnClickListener {

    private static final int RECORD_CAMERA_MAX = 15 * 1000 * 1000; // 定义录制的时间为30s

    private static final int RECORD_CAMERA_MIN = 2 * 1000 * 1000; // 定义最小2秒

    private static final String TAG = "CameraFullRecordActivity";

    private DrawPadCameraView mDrawPadCamera;

    private CameraLayer mCamLayer = null;

    private String dstPath = null; // 用于录制完成后的目标视频路径.

    private FocusImageView focusView;

    private PowerManager.WakeLock mWakeLock;
    private TextView tvTime;
    private Context mContext = null;

    private ImageView btnOk;
    private CameraProgressBar mProgressBar = null;
    private SeekBar skbarLookup;
    private SeekBar skbarGrind;

    private BeautyManager mBeautyMng;
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

        setContentView(R.layout.test_camera_full_record_layout);
        initBeautyView();
        mDrawPadCamera = (DrawPadCameraView) findViewById(R.id.id_fullrecord_padview);
        skbarLookup = (SeekBar) findViewById(R.id.id_test_camera_seekbar);
        skbarLookup.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                float progressf = (float) progress / 100f;
                mBeautyMng.setPinkColor(progressf);
            }
        });

        // -----------
        skbarGrind = (SeekBar) findViewById(R.id.id_test_camera_seekbar2);
        skbarGrind.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                float progressf = (float) progress / 100f;
                mBeautyMng.setWarmCool(progressf);
            }
        });

        initView();
        mProgressBar.setMaxProgress(RECORD_CAMERA_MAX / 1000);
        mProgressBar
                .setOnProgressTouchListener(new CameraProgressBar.OnProgressTouchListener() {
                    @Override
                    public void onClick(CameraProgressBar progressBar) {

                        if (mDrawPadCamera != null) {
                            /**
                             * 这里只是暂停和恢复录制, 可以录制多段,但不可以删除录制好的每一段,
                             *
                             * 如果你要分段录制,并支持回删,则可以采用SegmentStart和SegmentStop;
                             */
                            if (mDrawPadCamera.isRecording()) {
                                mDrawPadCamera.pauseRecord(); // 暂停录制,如果要停止录制
                            } else {
                                mDrawPadCamera.startRecord();
                            }
                        }
                    }
                });
        dstPath = SDKFileUtils.newMp4PathInBox();
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
        mDrawPadCamera.setRealEncodeEnable(padWidth, padHeight, bitrate,
                (int) 25, dstPath);
        /**
         * 设置录制处理进度监听.
         */
        mDrawPadCamera.setOnDrawPadProgressListener(drawPadProgressListener);

        /**
         * 相机前后置.是否设置滤镜.
         */
        mDrawPadCamera.setCameraParam(true, null, true);
        /**
         * 当手动聚焦的时候, 返回聚焦点的位置,让focusView去显示一个聚焦的动画.
         */
        mDrawPadCamera.setCameraFocusListener(new doFousEventListener() {

            @Override
            public void onFocus(int x, int y) {
                focusView.startFocus(x, y);
            }
        });
        mDrawPadCamera.setRecordMic(true);
        /**
         *
         * UI界面有效后, 开始开启DrawPad线程, 来预览画面.
         */
        mDrawPadCamera.setOnViewAvailable(new onViewAvailable() {

            @Override
            public void viewAvailable(DrawPadCameraView v) {
                startDrawPad();
            }
        });
        mDrawPadCamera.setOnDrawPadErrorListener(new onDrawPadErrorListener() {

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
        if (LanSongUtil.isFullScreenRatio(mDrawPadCamera.getViewWidth(),
                mDrawPadCamera.getViewHeight())) {
            mDrawPadCamera.setRealEncodeEnable(544, 1088, 3500 * 1024,
                    (int) 25, dstPath);
        }
        if (mDrawPadCamera.setupDrawpad()) // 建立图层.
        {
            mCamLayer = mDrawPadCamera.getCameraLayer(); // 临时
            mDrawPadCamera.startPreview();
            mBeautyMng.addBeauty(mCamLayer);
        } else {
            Log.i(TAG, "Beauty--->建立drawpad线程失败.");
        }
    }

    /**
     * Step3: 停止容器, 停止后,为新的视频文件增加上音频部分.
     */
    private void stopDrawPad() {
        if (mDrawPadCamera != null && mDrawPadCamera.isRunning()) {
            mDrawPadCamera.stopDrawPad();
            mCamLayer = null;
        }
    }

    /**
     * 选择滤镜效果,
     */
    private void selectFilter() {
        if (mDrawPadCamera != null && mDrawPadCamera.isRunning()) {
            FilterLibrary.showDialog(this,
                    new OnGpuImageFilterChosenListener() {

                        @Override
                        public void onGpuImageFilterChosenListener(
                                final GPUImageFilter filter, String name) {
                            if (mCamLayer != null) {
                                mCamLayer.switchFilterTo(filter);
                            }
                        }
                    });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDrawPadCamera != null) {
            mDrawPadCamera.stopDrawPad();
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

        SDKFileUtils.deleteFile(dstPath);
        dstPath = null;
    }

    // -------------------------------------------一下是UI界面和控制部分.---------------------------------------------------
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
                        if (mBeautyMng.isBeauting()) {
                            mBeautyMng.deleteBeauty(mDrawPadCamera
                                    .getCameraLayer());
                        } else {
                            mBeautyMng.addBeauty(mDrawPadCamera
                                    .getCameraLayer());
                        }
                    }
                });
        findViewById(R.id.id_camerabeauty_brightadd_btn).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        mBeautyMng.increaseBrightness(mDrawPadCamera
                                .getCameraLayer());
                    }
                });
        findViewById(R.id.id_camerabeaty_brightsub_btn).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mBeautyMng.discreaseBrightness(mDrawPadCamera
                                .getCameraLayer());
                    }
                });
    }

    private void playVideo() {
        if (SDKFileUtils.fileExist(dstPath)) {
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
                if (mCamLayer != null) {
                    if (mDrawPadCamera.isRunning()
                            && CameraLayer.isSupportFrontCamera()) {
                        // 先把DrawPad暂停运行.
                        mDrawPadCamera.pausePreview();
                        mCamLayer.changeCamera();
                        mDrawPadCamera.resumePreview(); // 再次开启.
                    }
                }
                break;
            case R.id.id_fullrecord_flashlight:
                if (mCamLayer != null) {
                    mCamLayer.changeFlash();
                }
                break;
            case R.id.id_fullrecord_filter:
                selectFilter();
                break;
            default:
                break;
        }
    }
    //
    // 总结:
    // 曝光2 在-2 -1 0 , 1 ,2之间;
    // lookup 0.22--0.2
    // 磨皮 0.4

}