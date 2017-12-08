package com.lansosdk.videoeditor;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.widget.TextView;

import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.CanvasLayer;
import com.lansosdk.box.CanvasRunnable;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.FileParameter;
import com.lansosdk.box.Layer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadThreadProgressListener;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;

/**
 * 说明1:用来演示DrawPad, 同时处理裁剪,缩放,压缩,剪切,增加文字, 增加logo等信息.
 * 我们的DrawPad是一个容器, 内部可以放任意图层,并调节图层的各种移动等.
 * 这里仅仅是演示 视频图层+图片图层+Canvas图层的组合.
 * 您可以参考我们其他的各种例子,来实现您的具体需求.
 * 
 * 说明2: 如果你有除了我们列举的功能外, 还有做别的, 可以直接拷贝这个类, 然后删除没用的, 增加上你的图层, 来完成您的需求.
 * 
 * 说明3: 如果列举的功能,可以满足您的需求,则调用形式是这样的:
 *      场景1: 只裁剪+logo:
 *      则:
 *      videoOneDo=new VideoOneDo(getApplicationContext(), videoPath);
		
		videoOneDo.setOnVideoOneDoProgressListener(进度监听);
		videoOneDo.setOnVideoOneDoCompletedListener(完成监听, 返回处理后的结果);
			videoOneDo.setCropRect(startX, startY, cropW, cropH);  //裁剪
			videoOneDo.setLogo(bmp, VideoOneDo.LOGO_POSITION_RIGHT_TOP); //加logo
		videoOneDo.start(); //开启另一个线程成功返回true, 失败返回false
		
		场景2: 
		增加背景音乐, 剪切时长+logo+文字等
		   则:
		     创建对象 ===>各种set===> 开始执行
		     
 */
public class VideoOneDo {

    private static final String TAG="VideoOneDo";
    public final static int LOGO_POSITION_LELF_TOP=0;
    public final static int LOGO_POSITION_LEFT_BOTTOM=1;
    public final static int LOGO_POSITION_RIGHT_TOP=2;
    public final static int LOGO_POSITION_RIGHT_BOTTOM=3;

    protected String videoPath=null;
    protected MediaInfo   srcInfo;
    protected String srcAudioPath; //从源视频中分离出的音频临时文件.
    protected float tmpvDuration=0.0f;//drawpad处理后的视频时长.

    protected String editTmpPath=null;

    protected DrawPadVideoExecute drawPad=null;
    protected boolean isExecuting=false;

    protected Layer videoLayer=null;
    protected BitmapLayer  logoBmpLayer=null;
    protected CanvasLayer  canvasLayer=null;
    
    protected  Context context;
    
    //-------------------------------------------------
    protected long startTimeUs=0;
    protected long cutDurationUs=0;
    protected FileParameter fileParamter=null;
    protected int startX,startY,cropWidth,cropHeight;
    protected GPUImageFilter  videoFilter=null;
    
    protected Bitmap logoBitmap=null;
    protected int logoPosition=LOGO_POSITION_RIGHT_TOP;
    protected int scaleWidth,scaleHeight;
    protected float  compressFactor=1.0f;

    protected String textAdd=null;


    protected String musicAACPath=null;
    protected String musicMp3Path=null;
    protected MediaInfo  musicInfo;
    protected boolean isMixBgMusic; //是否要混合背景音乐.
    
    protected float bgMusicStartTime=0.0f;
    protected float bgMusicEndTime=0.0f;
    
    protected float bgMusicVolume=0.8f;  //默认减少一点.
    protected float mainMusicVolume=1.0f;  //默认减少一点.
    protected String dstAACPath=null; 
    //在release的时候, 删除临时文件.
    protected ArrayList<String> deletedFileList=new ArrayList<String>();
    
    public  VideoOneDo(Context ctx, String videoPath)
    {
        this.videoPath=videoPath;
        context=ctx;
    }
    /**
     * 增加背景音乐.
     * 暂时只支持MP3和aac.
     * 如果背景音乐是MP3格式, 我们会转换为AAC格式.
     * 如果背景音乐时间 比视频短,则会循环播放.
     * 如果背景音乐时间 比视频长,则会从开始截取.
     * @param path
     */
    public void setBackGroundMusic(String path)
    {
    	musicInfo=new MediaInfo(path,false);
    	if(musicInfo.prepare() && musicInfo.isHaveAudio()){
    		if(musicInfo.aCodecName.equalsIgnoreCase("mp3")){
    			musicMp3Path=path;
    			musicAACPath=null;
    		}else if(musicInfo.aCodecName.equalsIgnoreCase("aac")){
    			musicAACPath=path;
    			musicMp3Path=null;
    		}else{
    			musicAACPath=null;
    			musicMp3Path=null;
    		}
    	}else{
    		musicMp3Path=null;
    		musicAACPath=null;
    		musicInfo=null;
    	}
    }
    /**
     * 背景音乐是否要和原视频中的声音混合, 即同时保留原音和背景音乐, 背景音乐通常音量略低一些.
     * 
     * @param path
     * @param isMix   是否增加,
     * @param volume 如增加,则背景音乐的音量调节 =1.0f为不变, 小于1.0降低; 大于1.0提高; 最大2.0;
     */
    public void setBackGroundMusic(String path, boolean isMix,float volume)
    {
    	musicInfo=new MediaInfo(path,false);
    	if(musicInfo.prepare() && musicInfo.isHaveAudio())
    	{
    		isMixBgMusic=isMix;
        	bgMusicVolume=volume;
        	
    		if(musicInfo.aCodecName.equalsIgnoreCase("mp3")){
    			musicMp3Path=path;
    			musicAACPath=null;
    		}else if(musicInfo.aCodecName.equalsIgnoreCase("aac")){
    			musicAACPath=path;
    			musicMp3Path=null;
    		}else{
    			musicAACPath=null;
    			musicMp3Path=null;
    		}
    	}else{
    		Log.e(TAG,"设置背景音乐出错, 音频文件有误.请查看"+musicInfo.toString());
    		musicMp3Path=null;
    		musicAACPath=null;
    		musicInfo=null;
    	}
    }
    /**
     * 增加背景音乐, 背景音乐是否和原声音混合, 混合时各自的音量.
     * @param path
     * @param isMix  是否混合
     * @param mainVolume   原音频的音量, 1.0f为原音量; 小于则降低, 大于则放大, 
     * @param bgVolume
     */
    public void setBackGroundMusic(String path, boolean isMix,float mainVolume,float bgVolume)
    {
    	setBackGroundMusic(path, isMix, bgVolume);
    	mainMusicVolume=mainVolume;
    }
    /**
     * 增加背景音乐,并裁剪背景音乐,  背景音乐是否和原声音混合, 混合时各自的音量.
     * @param path 背景音乐路径
     * @param startTime 背景音乐的开始时间, 单位秒, 如2.5f,则表示从2.5秒处裁剪
     * @param endTime  背景音乐的结束时间, 单位秒, 如10.0f,则表示裁剪到10.0f为止;
     * @param isMix  是否混合元声音, 即保留原声音
     * @param mainVolume 原声音的音量,1.0f为原音量; 小于则降低, 大于则放大, 
     * @param bgVolume 背景音乐的音量
     */
    public void setBackGroundMusic(String path, float startTime,float endTime, boolean isMix,float mainVolume,float bgVolume)
    {
    	setBackGroundMusic(path, isMix, bgVolume);
    	mainMusicVolume=mainVolume;
    	if(musicInfo!=null && startTime>0.0f && startTime<endTime && endTime<=musicInfo.aDuration){
    		bgMusicStartTime=startTime;
    		bgMusicEndTime=endTime;
    	}
    }
    /**
     * 缩放到的目标宽度和高度.
     * @param scaleW
     * @param scaleH
     */
    public void setScaleWidth(int scaleW,int scaleH){
    	if(scaleW>0 && scaleH>0){
    		 scaleWidth=scaleW;
    		 scaleHeight=scaleH;
    	}
    }
    /**
     * 设置压缩比, 此压缩比,在运行时, 会根据缩放后的比例,计算出缩放后的码率
     *  压缩比乘以 缩放后的码率, 等于实际的码率, 如果您缩放后, 建议不要再设置压缩
     * @param percent  压缩比, 值范围0.0f---1.0f;
     */
    public void setCompressPercent(float percent)
    {
    	if(percent>0.0f && percent<1.0f){
    		compressFactor=percent;
    	}
    }

    /**
     * 设置视频的开始位置,等于截取视频中的一段
     *  单位微秒, 如果你打算从2.3秒处开始处理,则这里的应该是2.3*1000*1000;
     *  支持精确截取.
     * @param timeUs
     */
    public  void setStartPostion(long timeUs){
        startTimeUs=timeUs;
    }
    /**
     * 设置结束时间
     *  支持精确截取.
     * @param timeUs
     */
    public  void setEndPostion(long timeUs)
    {
    	if(timeUs>0 && timeUs>startTimeUs){
    		cutDurationUs=timeUs-startTimeUs;
    	}
    }
    /**
     *设置截取视频中的多长时间.
     * 单位微秒,
     * 支持精确截取.
     * @param timeUs
     */
    public  void setCutDuration(long timeUs)
    {
    	if(timeUs>0){
    		cutDurationUs=timeUs;
    	}
    }
    /**
     * 设置裁剪画面的一部分用来处理,
     *  依靠视频呈现出怎样的区域来裁剪.
     *  比如视频显示720x1280,则您可以认为视频画面的宽度就是720,高度就是1280;不做其他角度的判断.
     *  
     *  裁剪后, 如果设置了缩放,则会把cropW和cropH缩放到指定的缩放宽度.
     * @param startX  画面的开始横向坐标,
     * @param startY  画面的结束纵向坐标
     * @param cropW  裁剪多少宽度
     * @param cropH  裁剪多少高度
     */
    public void setCropRect(int startX,int startY,int cropW,int cropH){
        fileParamter=new FileParameter();
        this.startX=startX;
        this.startY=startY;
        cropWidth=cropW;
        cropHeight=cropH;
    }
    /** 
     * 这里仅仅是举例,用一个滤镜.如果你要增加多个滤镜,可以判断处理进度,来不断切换滤镜
     * @param filter
     */
    public void setFilter(GPUImageFilter filter){
        videoFilter=filter;
    }

    /**
     * 设置logo的位置, 这里仅仅是举例,您可以拷贝这个代码, 自行定制各种功能.
     * 原理:  增加一个图片图层到容器DrawPad中, 设置他的位置.
     * 位置这里举例是:
     * {@link #LOGO_POSITION_LEFT_BOTTOM}
     * {@link #LOGO_POSITION_LELF_TOP}
     * {@link #LOGO_POSITION_RIGHT_BOTTOM}
     * {@value #LOGO_POSITION_RIGHT_TOP}
     * 
     * @param bmp  logo图片对象
     * @param position  位置 
     */
    public void setLogo(Bitmap bmp, int position)
    {
        logoBitmap=bmp;
        if(position<=LOGO_POSITION_RIGHT_BOTTOM){
            logoPosition=position;
        }
    }

    /**
     * 增加文字, 这里仅仅是举例,
     * 原理: 增加一个CanvasLayer图层, 把文字绘制到Canvas图层上.
     * 文字的位置, 是Canvas绘制出来的.
     * @param text
     * @param position
     */
    public void setText(String text)
    {
        textAdd=text;
    }
    private onVideoOneDoProgressListener monVideoOneDoProgressListener;
    public void setOnVideoOneDoProgressListener(onVideoOneDoProgressListener li)
    {
    	monVideoOneDoProgressListener=li;
    }
    private onVideoOneDoCompletedListener monVideoOneDOCompletedListener=null;
    public void setOnVideoOneDoCompletedListener(onVideoOneDoCompletedListener li){
    	monVideoOneDOCompletedListener=li;
    }
    /**
     * 进度回调
     * @param v
     * @param currentTimeUs
     */
    public void progressCallback(DrawPad v, long currentTimeUs)
    {
    	/**
    	 * 您可以继承我们这个类, 然后在这里随着进度来增加您自己的其他代码.
    	 * (此代码工作在UI线程)
    	 */
    }
    public void progressThreadCallback(DrawPad v, long currentTimeUs)
    {
    	/**
    	 * 您可以继承我们这个类, 然后在这里随着进度来增加您自己的其他代码.
    	 * (此代码工作在DrawPad线程,可以增加一些更精确的操作,或纹理操作等. 但不能增加UI操作.)
    	 */
    }
    /**
     * 开始执行, 内部会开启一个线程去执行.
     * 开启成功,返回true. 失败返回false;
     * @return
     */
    public boolean start()
    {
		if(isExecuting)
            return false;

		srcInfo=new MediaInfo(videoPath,false);
        if(srcInfo.prepare()==false) {
        	return false;
        }
        
        if(startTimeUs>0 || cutDurationUs>0)  //有剪切.
        {
        	long du=(long)(srcInfo.vDuration*1000*1000);
        	long aDuration=(long)(srcInfo.aDuration*1000*1000);
        	if(aDuration>0){
        		 du=Math.min(du, aDuration);
        	}
        	if(startTimeUs>du){
       		 	startTimeUs=0;
       		 	Log.w(TAG,"开始时间无效,恢复为0...");
        	}
        	if(du<(startTimeUs+cutDurationUs)){  //如果总时间 小于要截取的时间,则截取时间默认等于总时间.
        		cutDurationUs=0;
        		Log.w(TAG,"剪切时长无效,恢复为0...");
        	}
        }
        
        if(srcInfo.isHaveAudio()){
        	VideoEditor editor=new VideoEditor();
        	srcAudioPath=SDKFileUtils.createAACFileInBox();
			editor.executeDeleteVideo(videoPath, srcAudioPath,(float)startTimeUs/1000000f,(float)cutDurationUs/1000000f);
        }else{
        	isMixBgMusic=false;//没有音频则不混合.
        }
        
        isExecuting=true;
        editTmpPath=SDKFileUtils.createMp4FileInBox();
        
        tmpvDuration=srcInfo.vDuration;
        if(cutDurationUs>0 && cutDurationUs< (srcInfo.vDuration*1000000)){
        	tmpvDuration=(float)cutDurationUs/1000000f;
        }
        
        /**
         * 开启视频的DrawPad容器处理
         */
        if(startVideoThread()){
        	
        	/**
        	 * 视频开启成功, 开启音频处理
        	 */
        	if(musicMp3Path!=null|| musicAACPath!=null){
            	startAudioThread();
            }
        	return true;
        }else{
        	return false;
        }
    }
    private boolean startVideoThread()
    {
    	 //先判断有无裁剪画面
        if(cropHeight>0 && cropWidth>0)
        {
            fileParamter=new FileParameter();
            fileParamter.setDataSoure(videoPath);
            	
        	/**
        	 * 设置当前需要显示的区域 ,以左上角为0,0坐标. 
        	 * 
        	 * @param startX  开始的X坐标, 即从宽度的什么位置开始
        	 * @param startY  开始的Y坐标, 即从高度的什么位置开始
        	 * @param cropW   需要显示的宽度
        	 * @param cropH   需要显示的高度.
        	 */
            fileParamter.setShowRect(startX,startY,cropWidth,cropHeight);
            fileParamter.setStartTimeUs(startTimeUs);
            
            int padWidth=cropWidth;
            int padHeight=cropHeight;
            if(scaleHeight>0 && scaleWidth>0) {
                padWidth=scaleWidth;
                padHeight=scaleHeight;
            }
            
            float f= (float)(padHeight*padWidth) /(float)(fileParamter.info.vWidth * fileParamter.info.vHeight);
            float bitrate= f *fileParamter.info.vBitRate *compressFactor*2.0f;
            drawPad = new DrawPadVideoExecute(context, fileParamter, padWidth, padHeight,(int)bitrate, videoFilter, editTmpPath);
        }else{ //没有裁剪
        	
            int padWidth=srcInfo.getWidth();
            int padHeight=srcInfo.getHeight();
            
            float bitrate= (float)srcInfo.vBitRate*compressFactor*1.5f;
            if(scaleHeight>0 && scaleWidth>0) {
                padWidth=scaleWidth;
                padHeight=scaleHeight;
                float f= (float)(padHeight*padWidth) /(float)(srcInfo.vWidth * srcInfo.vHeight);
                bitrate *=f;
            }
            drawPad=new DrawPadVideoExecute(context,videoPath,startTimeUs/1000,padWidth,padHeight,(int)bitrate,videoFilter,editTmpPath);
        }
        
        drawPad.setUseMainVideoPts(true);
        /**
         * 设置DrawPad处理的进度监听, 回传的currentTimeUs单位是微秒.
         */
        drawPad.setDrawPadProgressListener(new onDrawPadProgressListener() {
            @Override
            public void onProgress(DrawPad v, long currentTimeUs) {

            	progressCallback(v, currentTimeUs);
            	if(monVideoOneDoProgressListener!=null){
            		float time=(float)currentTimeUs/1000000f;
            		
            		float percent=time/(float)tmpvDuration;
            		
            		float b   =  (float)(Math.round(percent*100))/100;  //保留两位小数.
            		if(b<1.0f && monVideoOneDoProgressListener!=null && isExecuting){
            			monVideoOneDoProgressListener.onProgress(VideoOneDo.this, b);
            		}
            	}
                if(cutDurationUs>0 && currentTimeUs>cutDurationUs){  //设置了结束时间, 如果当前时间戳大于结束时间,则停止容器.
                	drawPad.stopDrawPad();
                }
            }
        });
        //容器内部的进度回调.
        drawPad.setDrawPadThreadProgressListener(new onDrawPadThreadProgressListener() {
			
			@Override
			public void onThreadProgress(DrawPad v, long currentTimeUs) {
				progressThreadCallback(v,currentTimeUs);
			}
		});
        
        /**
         * 设置DrawPad处理完成后的监听.
         */
        drawPad.setDrawPadCompletedListener(new onDrawPadCompletedListener() {

            @Override
            public void onCompleted(DrawPad v) {
                completeDrawPad();
            }
        });

        drawPad.pauseRecord();
        if(drawPad.startDrawPad())
        {
            videoLayer=drawPad.getMainVideoLayer();
            videoLayer.setScaledValue(videoLayer.getPadWidth(), videoLayer.getPadHeight());
            
            addBitmapLayer(); //增加图片图层
            
            addCanvasLayer(); //增加文字图层.
            drawPad.resumeRecord();  //开始恢复处理.
            return true;
        }else{
        	return false;
        }
    }
    /**
     * 处理完成后的动作.
     */
    private void completeDrawPad()
    {
    	joinAudioThread();
    	
    	if(isExecuting==false){
    		return ;
    	}
    	
		String dstPath=SDKFileUtils.createMp4FileInBox();
    	if(dstAACPath!=null && isExecuting)  //增加背景音乐.
    	{
    		videoMergeAudio(editTmpPath, dstAACPath,dstPath);
    		deletedFileList.add(editTmpPath);
    	}else if(srcAudioPath!=null && isExecuting){  //增加原音.
    		videoMergeAudio(editTmpPath, srcAudioPath,dstPath); 
    		deletedFileList.add(editTmpPath);
    		deletedFileList.add(srcAudioPath);
    	}else{
    		deletedFileList.add(dstPath);
    		dstPath=editTmpPath;
    	}
    	
    	if(monVideoOneDOCompletedListener!=null && isExecuting){
    		monVideoOneDOCompletedListener.onCompleted(VideoOneDo.this,dstPath);
    	}
    	isExecuting=false;
    	
//    	Log.d(TAG,"最后的视频文件是:"+MediaInfo.checkFile(dstPath));
    }
    public void stop()
    {
    	if(isExecuting){
    		isExecuting=false;
    		  
    		monVideoOneDOCompletedListener=null;
    		monVideoOneDoProgressListener=null;
    		if(drawPad!=null){
    			drawPad.stopDrawPad();
    		}
    		joinAudioThread();
    		videoPath=null;
    		srcInfo=null;
    		drawPad=null;
    	  
    	    logoBitmap=null;
    	    textAdd=null;
    	    dstAACPath=null; 
    	    musicMp3Path=null;
    	    musicInfo=null;
    	}
    }
    public void release()
    {
    	for(String path: deletedFileList){
    		SDKFileUtils.deleteFile(path);
    	}
    	stop();
    }
    /**
     * 增加图片图层
     */
    private void addBitmapLayer()
    {
    	 //如果需要增加图片.
        if(logoBitmap!=null){
        	logoBmpLayer=drawPad.addBitmapLayer(logoBitmap);
        	if(logoBmpLayer!=null)
        	{
        		int w=logoBmpLayer.getLayerWidth();
        		int h=logoBmpLayer.getLayerHeight();
        		if(logoPosition==LOGO_POSITION_LELF_TOP){  //左上角.
        			
        			logoBmpLayer.setPosition(w/2, h/2);
            		
        		}else if(logoPosition==LOGO_POSITION_LEFT_BOTTOM){  //左下角
        			
        			logoBmpLayer.setPosition(w/2,logoBmpLayer.getPadHeight()- h/2);
        		}else if(logoPosition==LOGO_POSITION_RIGHT_TOP){  //右上角
        			
        			logoBmpLayer.setPosition(logoBmpLayer.getPadWidth()-w/2,h/2);
        			
        		}else if(logoPosition==LOGO_POSITION_RIGHT_BOTTOM){  //右下角
        			logoBmpLayer.setPosition(logoBmpLayer.getPadWidth()-w/2, logoBmpLayer.getPadHeight() - h/2);
        		}else{
        			Log.w(TAG,"logo默认居中显示");
        		}
        	}
        }
    }
    /**
     * 增加Android的Canvas类图层.
     */
    private void addCanvasLayer()
    {
    	if(textAdd!=null){
        	canvasLayer=drawPad.addCanvasLayer();
        	
        	canvasLayer.addCanvasRunnable(new CanvasRunnable() {
				
				@Override
				public void onDrawCanvas(CanvasLayer pen, Canvas canvas, long currentTimeUs) {
					Paint paint = new Paint();
	                paint.setColor(Color.RED);
         			paint.setAntiAlias(true);
         			paint.setTextSize(20);
         			canvas.drawText(textAdd,20,20, paint);
				}
			});
        }
    }
    private Thread audioThread=null;
    /**
     * 音频处理线程.
     */
    private void startAudioThread()
    {
    	if(audioThread==null)
    	{
    		audioThread=new Thread(new Runnable() {
    			@Override
    			public void run() {
    				
    				if(bgMusicEndTime>bgMusicStartTime){//需要裁剪,则先裁剪, 然后做处理.
    						String audio;
    						 if(musicMp3Path!=null){
    					    	 audio=SDKFileUtils.createMP3FileInBox();
    					    	 executeAudioCutOut(musicMp3Path, audio, bgMusicStartTime, bgMusicEndTime);
    					    	 //不删除原声音;
    					    	 musicMp3Path=audio;
    					     }else{
    					    	 audio=SDKFileUtils.createAACFileInBox();
    					    	 executeAudioCutOut(musicAACPath, audio, bgMusicStartTime, bgMusicEndTime);
    					    	 musicAACPath=audio;
    					     }
    						 deletedFileList.add(audio);
    				}
    				
    				
    				/**
    				 * 1, 如果mp3,  看是否要mix, 如果要,则长度拼接够, 然后mix;如果不mix,则先转码,再拼接.
    				 * 2, 如果是aac, 是否要mix, 要则拼接 再mix,; 不需要则直接拼接.
    				 */
    				if(musicMp3Path!=null){  //输入的是MP3;
    					  if(isMixBgMusic){  //混合.
    						  dstAACPath=SDKFileUtils.createAACFileInBox();
    						  
    						  String startMp3=getEnoughAudio(musicMp3Path, true);
    						  
    						  VideoEditor  editor=new VideoEditor();
    						  editor.executeAudioVolumeMix(srcAudioPath, startMp3,mainMusicVolume, bgMusicVolume,tmpvDuration, dstAACPath);
    						  
    						  deletedFileList.add(dstAACPath);
    					  }else{//直接增加背景.
    						  
    						    VideoEditor editor=new VideoEditor();
    		    				float duration=(float)cutDurationUs/1000000f;
    		    				String tmpAAC=SDKFileUtils.createAACFileInBox();
    		    				editor.executeConvertMp3ToAAC(musicMp3Path, 0,duration, tmpAAC);
    		    				dstAACPath=getEnoughAudio(tmpAAC, false);
    		    				
    		    				deletedFileList.add(tmpAAC);
    					  }
    				}else if(musicAACPath!=null){
    					 if(isMixBgMusic){  //混合.
    						 dstAACPath=SDKFileUtils.createAACFileInBox();
	   						  String startAAC=getEnoughAudio(musicAACPath, false);
	   						  VideoEditor  editor=new VideoEditor();
	   						  editor.executeAudioVolumeMix(srcAudioPath, startAAC, 1.0f, bgMusicVolume,tmpvDuration, dstAACPath);
	   						  
	   						deletedFileList.add(dstAACPath);
    					 }else{
    						 dstAACPath=getEnoughAudio(musicAACPath, false);
    					 }
    				}
    				audioThread=null;
    			}
    		});
    		audioThread.start();
    	}
    }
    private void joinAudioThread()
    {
    	if(audioThread!=null){
    		try {
				audioThread.join(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				Log.w(TAG,"背景音乐转码失败....使用源音频");
				dstAACPath=null;
			}
    		audioThread=null;
    	}
    }
    /**
     * 得到拼接好的mp3或aac文件. 如果够长,则直接返回;
     * @param input
     * @param isMp3
     * @return
     */
    private String getEnoughAudio(String input, boolean isMp3)
    {
    	String audio=input;
		if(musicInfo.aDuration<tmpvDuration){  //如果小于则自行拼接.
			
			Log.d(TAG,"音频时长不够,开始转换.musicInfo.aDuration:"+musicInfo.aDuration+ " tmpvDuration:"+ tmpvDuration);
			
			 int num= (int)(tmpvDuration/musicInfo.aDuration +1.0f);
			 String[] array=new String[num];  
		     for(int i=0;i<num;i++){  
		    	 array[i]=input;  
		     } 
		     if(isMp3){
		    	 audio=SDKFileUtils.createMP3FileInBox();
		     }else{
		    	 audio=SDKFileUtils.createAACFileInBox();	 
		     }
		     deletedFileList.add(audio);
		     
			 concatAudio(array,audio);  //拼接好.
			 
		}
		return audio;
    }
    /**
     * 拼接aac
     * @param tsArray
     * @param dstFile
     * @return
     */
    private int concatAudio(String[] tsArray,String dstFile)
	   {
		   if(SDKFileUtils.filesExist(tsArray)){
			    String concat="concat:";
			    for(int i=0;i<tsArray.length-1;i++){
			    	concat+=tsArray[i];
			    	concat+="|";
			    }
			    concat+=tsArray[tsArray.length-1];
			    	
				List<String> cmdList=new ArrayList<String>();
				
		    	cmdList.add("-i");
				cmdList.add(concat);

				cmdList.add("-c");
				cmdList.add("copy");
				
				cmdList.add("-y");
				
				cmdList.add(dstFile);
				String[] command=new String[cmdList.size()];  
			     for(int i=0;i<cmdList.size();i++){  
			    	 command[i]=(String)cmdList.get(i);  
			     }  
			     VideoEditor editor=new VideoEditor();
			    return  editor.executeVideoEditor(command);
		  }else{
			  return -1;
		  }
	   }
    public int executeAudioCutOut(String srcFile,String dstFile,float startS,float durationS)
	  {
    			VideoEditor editor=new VideoEditor();
				List<String> cmdList=new ArrayList<String>();
				
				cmdList.add("-ss");
				cmdList.add(String.valueOf(startS));
				
		    	cmdList.add("-i");
				cmdList.add(srcFile);

				cmdList.add("-t");
				cmdList.add(String.valueOf(durationS));
				
				cmdList.add("-acodec");
				cmdList.add("copy");
				cmdList.add("-y");
				cmdList.add(dstFile);
				String[] command=new String[cmdList.size()];  
			     for(int i=0;i<cmdList.size();i++){  
			    	 command[i]=(String)cmdList.get(i);  
			     }
			    return  editor.executeVideoEditor(command);
			  
	  }
    /**
     * 之所有从VideoEditor.java中拿过来另外写, 是为了省去两次MediaInfo的时间;
     */
       private void videoMergeAudio(String videoFile,String audioFile,String dstFile)
	  {
		  		VideoEditor editor=new VideoEditor();
				List<String> cmdList=new ArrayList<String>();
				
		    	cmdList.add("-i");
				cmdList.add(videoFile);
				
				cmdList.add("-i");
				cmdList.add(audioFile);

				if(tmpvDuration>0.0f){
					cmdList.add("-t");
					cmdList.add(String.valueOf(tmpvDuration));
				}
				
				cmdList.add("-vcodec");
				cmdList.add("copy");
				cmdList.add("-acodec");
				cmdList.add("copy");
				
				
				cmdList.add("-absf");
				cmdList.add("aac_adtstoasc");
				
				cmdList.add("-y");
				cmdList.add(dstFile);
				String[] command=new String[cmdList.size()];  
			     for(int i=0;i<cmdList.size();i++){  
			    	 command[i]=(String)cmdList.get(i);  
			     }  
			    editor.executeVideoEditor(command);
	  }
}
