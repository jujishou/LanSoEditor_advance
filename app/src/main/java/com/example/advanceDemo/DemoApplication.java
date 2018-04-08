package com.example.advanceDemo;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.MediaInfo;

public class DemoApplication extends Application {

    private static DemoApplication instance;
    private String srcVideo;
    private MediaInfo mInfo = null;

    public static DemoApplication getInstance() {
        if (instance == null) {
            throw new NullPointerException("DemoApplication instance is null");
        }
        return instance;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        instance = this;
    }

    public Context getContext() {
        return getBaseContext();
    }

    public Resources getResources() {
        return getBaseContext().getResources();
    }

    public String getVideoPath() {
        if (srcVideo == null) {
            srcVideo = CopyFileFromAssets.copyAssets(getContext(),
                    "ping20s.mp4");
        }
        return srcVideo;
    }

    public void setVideoPath(String video) {
        srcVideo = video;
    }

    public MediaInfo getVideoMediaInfo() {
        if (mInfo == null) {
            mInfo = new MediaInfo(srcVideo);
            if (mInfo.prepare() == false) {
                srcVideo = CopyFileFromAssets.copyAssets(getContext(),
                        "ping20s.mp4");
                mInfo = new MediaInfo(srcVideo);
                mInfo.prepare();
            }
        }
        return mInfo;
    }
}
