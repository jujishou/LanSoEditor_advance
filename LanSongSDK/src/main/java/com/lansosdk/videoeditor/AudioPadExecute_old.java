package com.lansosdk.videoeditor;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.lansosdk.box.AudioPad;
import com.lansosdk.box.AudioSource;
import com.lansosdk.box.IAudioSourceInput;
import com.lansosdk.box.LSLog;
import com.lansosdk.box.onAudioPadCompletedListener;
import com.lansosdk.box.onAudioPadProgressListener;
import com.lansosdk.box.onAudioPadThreadProgressListener;

/**
 * 不再使用.请用新的AudioPadExecute
 * 下一个版本删除;
 */
@Deprecated
public class AudioPadExecute_old {

    private static final String TAG = LSLog.TAG;
    static AudioSource audioSrc1;
    static long starttime = 0;
    AudioPad audioPad;

    public AudioPadExecute_old(Context ctx, IAudioSourceInput input) {
        audioPad = new AudioPad(ctx, input);
    }
    public AudioPadExecute_old(Context ctx, String dstPath) {
        audioPad = new AudioPad(ctx, dstPath);
    }
    public AudioSource setAudioPadSource(String mainAudio) {
        if (audioPad != null) {
            return audioPad.addMainAudio(mainAudio);
        } else {
            return null;
        }
    }
    public AudioSource setAudioPadLength(float duration) {
        if (audioPad != null) {
            return audioPad.addMainAudio(duration,44100);
        } else {
            return null;
        }
    }
    public AudioSource addSubAudio(String srcPath) {
        if (audioPad != null) {
            return audioPad.addSubAudio(srcPath);
        } else {
            return null;
        }
    }
    public AudioSource addSubAudio(String srcPath, long startPadUs,
                                   long startAudioUs, long endAudioUs) {
        if (audioPad != null) {
            return audioPad.addSubAudio(srcPath, startPadUs, startAudioUs,
                    endAudioUs);
        } else {
            return null;
        }
    }
    public void setAudioPadProgressListener(onAudioPadProgressListener listener) {
        if (audioPad != null) {
            audioPad.setAudioPadProgressListener(listener);
        }
    }
    public void setAudioPadThreadProgressListener(
            onAudioPadThreadProgressListener listener) {
        if (audioPad != null) {
            audioPad.setAudioPadThreadProgressListener(listener);
        }
    }
    public void setAudioPadCompletedListener(
            onAudioPadCompletedListener listener) {
        if (audioPad != null) {
            audioPad.setAudioPadCompletedListener(listener);
        }
    }
    public boolean start() {
        if (audioPad != null) {
            return audioPad.start();
        } else {
            return false;
        }
    }
    public void waitComplete() {
        if (audioPad != null) {
            audioPad.joinSampleEnd();
        }
    }
    public void stop() {
        if (audioPad != null) {
            audioPad.stop();
        }
    }
    public void release() {
        if (audioPad != null) {
            audioPad.release();
            audioPad = null;
        }
    }
}
