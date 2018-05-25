package com.example.advanceDemo.cool;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.airbnb.lottie.ImageAssetDelegate;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieImageAsset;
import com.airbnb.lottie.OnCompositionLoadedListener;
import com.example.advanceDemo.DemoUtil;
import com.example.advanceDemo.VideoPlayerActivity;
import com.lansoeditor.advanceDemo.R;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.ViewLayerRelativeLayout;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.DrawPadView;
import com.lansosdk.videoeditor.LanSongMergeAV;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKFileUtils;

public class TestLottieActivity extends Activity {
    final static String TAG="TestLottieActivity";

    /*
    * 这里举例使用的是 LottieDrawable， 如果LottieAnimationView能满足您的需求， 则更简单；*/
//    LottieAnimationView lottieAnimationView;
LottieDrawable drawable;
    DrawPadView drawPadView;
    int vwidth,vheight;
    String dstPath;
    String tmpPath;
    MediaPlayer mplayer;
    VideoLayer videoLayer;
    private String srcVideo;
    private ViewLayerRelativeLayout viewLayerRelativeLayout;
    private ViewLayer mViewLayer = null;
    private MediaInfo mediaInfo;
    private ImageView imageView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lottie_demo1_layout);

        drawPadView=(DrawPadView)findViewById(R.id.id_lottie_demo_drawpadview);
        viewLayerRelativeLayout = (ViewLayerRelativeLayout) findViewById(R.id.id_lottie_demo_gllayout);

        imageView=(ImageView)findViewById(R.id.lottieView);

        srcVideo= CopyFileFromAssets.copyAssets(getApplicationContext(),"mayun.mp4");
        test();

        mediaInfo=new MediaInfo(srcVideo,false);
        if(mediaInfo.prepare()){
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mediaInfo.isHaveVideo()){
            showEditDialog();
        }
    }

    private void startPlayVideo() {
        if(mplayer!=null){
            mplayer.release();
            mplayer=null;
        }

            mplayer = new MediaPlayer();
            try {
                mplayer.setDataSource(srcVideo);
                mplayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        vwidth=mp.getVideoWidth();
                        vheight=mp.getVideoHeight();
                        initDrawPad(mp.getVideoWidth(), mp.getVideoHeight());
                    }
                });
                mplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                       stopDrawPad();

                       MediaInfo.checkFile(tmpPath);
                       if(mediaInfo.isHaveAudio()&& SDKFileUtils.fileExist(tmpPath)){
                           LanSongMergeAV.mergeAVDirectly(srcVideo,tmpPath,dstPath);
                           showCompleteDialog();
                       }else if(SDKFileUtils.fileExist(dstPath)){
                           showCompleteDialog();
                       }else{
                           DemoUtil.showHintDialog(TestLottieActivity.this,"合成视频失败");
                       }
                    }
                });
                mplayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
    private void initDrawPad(int w, int h) {
        SDKFileUtils.deleteFile(dstPath);
        SDKFileUtils.deleteFile(tmpPath);

        tmpPath= SDKFileUtils.createMp4FileInBox();
        dstPath= SDKFileUtils.createMp4FileInBox();

        if(mediaInfo.isHaveAudio()){
            drawPadView.setRealEncodeEnable(vwidth,vheight,(int)mediaInfo.vFrameRate,tmpPath);
        }else{
            drawPadView.setRealEncodeEnable(vwidth,vheight,(int)mediaInfo.vFrameRate,dstPath);
        }

        drawPadView.setDrawPadSize(w, h, new onDrawPadSizeChangedListener() {
            @Override
            public void onSizeChanged(int viewWidth, int viewHeight) {
                startDrawPad();
            }
        });
    }
    private void startDrawPad() {
        drawPadView.pauseDrawPad();
        if (drawPadView.isRunning() == false && drawPadView.startDrawPad()) {
            //先增加一个视频图层
            videoLayer= drawPadView.addMainVideoLayer(mplayer.getVideoWidth(), mplayer.getVideoHeight(), null);
            //再增加一个UI图层;
            addViewLayer();
            if (videoLayer != null) {
                mplayer.setSurface(new Surface(videoLayer.getVideoTexture()));
                mplayer.start();

                imageView.setVisibility(View.VISIBLE);
                drawable.setFrame(0);
                drawable.playAnimation();
            }
            drawPadView.resumeDrawPad();
        }
    }
    private void stopDrawPad(){
        if (drawPadView != null && drawPadView.isRunning()) {
            drawPadView.stopDrawPad();
        }
    }
    /**
     * 增加图层;
     */
    private void addViewLayer() {
        if (drawPadView != null && drawPadView.isRunning()) {
            mViewLayer = drawPadView.addViewLayer();
            viewLayerRelativeLayout.bindViewLayer(mViewLayer);
            viewLayerRelativeLayout.invalidate();

            ViewGroup.LayoutParams params = viewLayerRelativeLayout.getLayoutParams();
            params.height = mViewLayer.getPadHeight(); // 因为布局时, 宽度一致,
            // 这里调整高度,让他们一致.
            viewLayerRelativeLayout.setLayoutParams(params);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mplayer!=null){
            mplayer.release();
            mplayer=null;
        }
        stopDrawPad();
    }
    private void showCompleteDialog(){
        new AlertDialog.Builder(TestLottieActivity.this)
                .setTitle("提示")
                .setMessage("已经生成,是否预览")
                .setPositiveButton("预览", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(TestLottieActivity.this, VideoPlayerActivity.class);
                        intent.putExtra("videopath", dstPath);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }
    String inputText="";
    private void showEditDialog(){
        final EditText et = new EditText(this);
        new AlertDialog.Builder(this).setTitle("内容")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        inputText = et.getText().toString();
                        if (inputText.equals("")) {
                            Toast.makeText(getApplicationContext(), "内容不能为空！", Toast.LENGTH_LONG).show();
                        }else {
                                    startPlayVideo();
                                    Bitmap bmp=textToBitmap(320,240,inputText);
                                    drawable.updateBitmap("image_0",bmp);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
    private void test(){
        if(drawable!=null){
            drawable=null;
        }
        drawable = new LottieDrawable();
        drawable.setImagesAssetsFolder("images");
        LottieComposition.Factory.fromAssetFileName(getApplicationContext(), "dammta.json", new OnCompositionLoadedListener() {
            @Override
            public void onCompositionLoaded(@Nullable LottieComposition composition) {
                drawable.setComposition(composition);
            }
        });
        imageView.setImageDrawable(drawable);
        imageView.setVisibility(View.GONE);
    }

    /**
     * 文字转图片
     * @param width
     * @param height
     * @param text
     * @return
     */
    private Bitmap textToBitmap(int width, int height, String text){
        int fontSize=30;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        paint.setColor(Color.BLUE);
        canvas.drawColor(Color.WHITE);

//        for(int i=0;i<tp.getHeigt();i++){
        canvas.drawText(text, 0, 40, paint);
//            y=y+20;
//        }
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return bitmap;
    }
}
