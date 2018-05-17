package com.example.advanceDemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.lansoeditor.advanceDemo.R;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.Layer;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onVideoLayerProgressListener;
import com.lansosdk.videoeditor.DrawPadVideoExecute;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKFileUtils;

import jp.co.cyberagent.lansongsdk.gpuimage.IF1977Filter;

/**
 */
public class TestExecuteSeekVideoDemoActivity extends Activity {

    private static final String TAG = "ExecuteSeekVideoDemoActivity";
    MediaInfo mInfo;
    TextView tvProgressHint;
    TextView tvHint;
    long beforeDraw = 0;
    int seekOnce = 0; // 只是一次;
    private String videoPath = null;
    private String dstPath = null;
    private DrawPadVideoExecute drawPad = null;
    private boolean isExecuting = false;
    private VideoLayer videoLayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        videoPath = getIntent().getStringExtra("videopath");

        mInfo = new MediaInfo(videoPath);
        mInfo.prepare();

        setContentView(R.layout.execute_edit_demo_layout);
        initView();

        // 在手机的默认路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
        dstPath = SDKFileUtils.newMp4PathInBox();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (drawPad != null) {
            drawPad.release();
            drawPad = null;
        }

        SDKFileUtils.deleteFile(dstPath);
    }

    private void startDrawPadExecute() {
        if (isExecuting)
            return;

        beforeDraw = System.currentTimeMillis();
        isExecuting = true;
        // 设置pad的宽度和高度.
        int padWidth = mInfo.vWidth;
        int padHeight = mInfo.vHeight;
        if (mInfo.vRotateAngle == 90 || mInfo.vRotateAngle == 270) {
            padWidth = mInfo.vHeight;
            padHeight = mInfo.vWidth;
        }

        drawPad = new DrawPadVideoExecute(
                TestExecuteSeekVideoDemoActivity.this, videoPath, padWidth,
                padHeight, (int) (mInfo.vBitRate * 1.5f), new IF1977Filter(
                getApplicationContext()), dstPath);

        /**
         * 设置DrawPad处理的进度监听, 回传的currentTimeUs单位是微秒.
         */
        drawPad.setDrawPadProgressListener(new onDrawPadProgressListener() {

            @Override
            public void onProgress(DrawPad v, long currentTimeUs) {
                drawPadProgress(v, currentTimeUs);
            }
        });
        /**
         * 设置DrawPad处理完成后的监听.
         */
        drawPad.setDrawPadCompletedListener(new onDrawPadCompletedListener() {

            @Override
            public void onCompleted(DrawPad v) {
                drawPadCompleted();
            }
        });
        drawPad.addTimeStretch(0.5f, 8000 * 1000, 16 * 1000 * 1000);

        drawPad.pauseRecord();

        if (drawPad.startDrawPad()) {
            videoLayer = drawPad.getMainVideoLayer();
            // videoLayer.addTimeStretchForExecute(2.0f, 1000*1000,
            // 30*1000*1000);
            // videoLayer.addTimeStretchForExecute(0.5f, 8000*1000,
            // 12*1000*1000);
            // videoLayer.addTimeStretchForExecute(2.0f, 13000*1000,
            // 18*1000*1000);
            drawPad.resumeRecord(); // 开始恢复处理.
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("DrawPad开启错误.或许视频分辨率过高导致..")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                }
                            }).show();
        }
    }

    private void testVideoSeek() {
        if (videoLayer != null) {
            videoLayer
                    .setVideoLayerProgressListener(new onVideoLayerProgressListener() {

                        @Override
                        public void onProgress(Layer layer, long currentPtsUs) {

                            // if(currentPtsUs>6*1000*1000 && seekOnce<1){
                            // seekOnce++;
                            // videoLayer.seekForExecute(1*1000*1000);
                            // //如果到了6秒,则seek到1秒;
                            // }

                            // if(currentPtsUs>6*1000*1000 &&
                            // currentPtsUs<10*1000*1000){
                            // videoLayer.adjustSpeedForExecute(0.5f);
                            // }else{
                            // videoLayer.adjustSpeedForExecute(1.0f);
                            // }
                        }
                    });
        }
    }

    /**
     * DrawPad容器的进度监听, 走到什么位置后,设置对应的内容.
     *
     * @param v
     * @param currentTimeUs
     */
    private void drawPadProgress(DrawPad v, long currentTimeUs) {
        tvProgressHint.setText(String.valueOf(currentTimeUs));
    }

    /**
     * 完成后, 去播放
     */
    private void drawPadCompleted() {
        tvProgressHint.setText("DrawPadExecute Completed!!!");
        isExecuting = false;
        findViewById(R.id.id_video_edit_btn2).setEnabled(true);
    }

    private void showHintDialog() {
        new AlertDialog.Builder(this).setTitle("提示")
                .setMessage("视频过大,可能会需要一段时间,您确定要处理吗?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startDrawPadExecute();
                    }
                }).setNegativeButton("取消", null).show();
    }

    private void initView() {
        tvHint = (TextView) findViewById(R.id.id_video_editor_hint);
        tvHint.setText(R.string.filterLayer_execute_hint);

        tvProgressHint = (TextView) findViewById(R.id.id_video_edit_progress_hint);
        findViewById(R.id.id_video_edit_btn).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (mInfo.vDuration > 60 * 1000) {// 大于60秒
                            showHintDialog();
                        } else {
                            startDrawPadExecute();
                        }
                    }
                });

        findViewById(R.id.id_video_edit_btn2).setEnabled(false);
        findViewById(R.id.id_video_edit_btn2).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (SDKFileUtils.fileExist(dstPath)) {
                            Intent intent = new Intent(
                                    TestExecuteSeekVideoDemoActivity.this,
                                    VideoPlayerActivity.class);
                            intent.putExtra("videopath", dstPath);
                            startActivity(intent);
                        } else {
                            Toast.makeText(
                                    TestExecuteSeekVideoDemoActivity.this,
                                    "目标文件不存在", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}