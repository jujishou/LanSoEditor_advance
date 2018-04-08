package com.example.advanceDemo;

import android.content.Context;
import android.util.Log;

import com.lansosdk.box.DrawPad;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.videoeditor.DrawPadVideoExecute;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;

/**
 * 临时写的,用来给录制好的视频增加片头
 *
 * @author Administrator
 */
public class VHeaderConcat {

    private static final String TAG = "VHeaderConcat";
    String videoPath = null;
    MediaInfo mInfo;
    private String editTmpPath = null;
    private String dstPath = null;
    private DrawPadVideoExecute mDrawPad = null;
    private String recordVideo;

    public void start(Context ctx, String videoHeader, String recordVideo) {
        this.recordVideo = recordVideo;
        videoPath = videoHeader;

        mInfo = new MediaInfo(recordVideo);
        if (mInfo.prepare()) {
            int padWidth = mInfo.vWidth;
            int padHeight = mInfo.vHeight;
            if (mInfo.vRotateAngle == 90 || mInfo.vRotateAngle == 270) {
                padWidth = mInfo.vHeight;
                padHeight = mInfo.vWidth;
            }

            editTmpPath = SDKFileUtils.newMp4PathInBox();
            dstPath = SDKFileUtils.newMp4PathInBox();

            /**
             * 片头缩放成 recordVideo的尺寸.
             */
            mDrawPad = new DrawPadVideoExecute(ctx, videoPath, padWidth,
                    padHeight, (int) (mInfo.vBitRate * 1.5f), null, editTmpPath);
            /**
             * 设置DrawPad处理完成后的监听.
             */
            mDrawPad.setDrawPadCompletedListener(new onDrawPadCompletedListener() {

                @Override
                public void onCompleted(DrawPad v) {
                    // TODO Auto-generated method stub
                    drawPadCompleted();
                }
            });
            mDrawPad.pauseRecord();
            if (mDrawPad.startDrawPad()) {
                mDrawPad.resumeRecord(); // 开始恢复处理.
            }
        }
    }

    /**
     * 完成后, 去播放
     */
    private void drawPadCompleted() {
        if (SDKFileUtils.fileExist(editTmpPath)) {
            // 合并音频文件.
            boolean ret = VideoEditor.encoderAddAudio(videoPath, editTmpPath,
                    SDKDir.TMP_DIR, dstPath);
            Log.i(TAG, "视频转换完成, 转换后的是:" + dstPath);
            if (!ret) {
                dstPath = editTmpPath;
            } else {
                SDKFileUtils.deleteFile(editTmpPath);
            }
            // 一下是测试.
            VideoEditor editor = new VideoEditor();
            String[] videoArray = {dstPath, recordVideo};
            editor.executeConcatMP4(videoArray, "/sdcard/concat3.mp4");
            dstPath = "/sdcard/concat3.mp4";

        }
    }
}