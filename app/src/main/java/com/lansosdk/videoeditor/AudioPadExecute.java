package com.lansosdk.videoeditor;

import android.content.Context;
import android.media.MediaPlayer;

import com.lansosdk.box.AudioSource;
import com.lansosdk.box.AudioPad;
import com.lansosdk.box.onAudioPadCompletedListener;
import com.lansosdk.box.onAudioPadProgressListener;
import com.lansosdk.box.onAudioPadThreadProgressListener;
/**
 *  音频图层后的后台处理.
 *  此类是用来在后台做音频混合处理使用.
 *  
 *  使用在两种场景中:
 *  场景一: 给一段完整的音频上:增加别的声音, 如搞笑声,闪电声等等.生成的文件和源声音一样的长度,只是内容根据您的设置而变化了.
 *  
 *  场景二: 先设置整体的音频长度, 然后在分别增加声音.比如创建一段20s的声音, 1--4秒一种; 3--5秒一种,可以交叉的;最后生成您设置长度的声音.
 *  
 *  
 *  当前处理后的音频编码成aac格式, 采样率是44100, 双通道, 64000码率.
 *  
 *  如果您仅仅用来做音频拼接, 可以采用 {@link AudioConcat}来做.
 *  当前的音频格式支持 MP3, AAC(m4a后缀), 采样率为44100, 通道数为2,其他格式暂不支持,请注意
 *  当前的音频格式支持 MP3, AAC(m4a后缀), 采样率为44100, 通道数为2,其他格式暂不支持,请注意
 *  当前的音频格式支持 MP3, AAC(m4a后缀), 采样率为44100, 通道数为2,其他格式暂不支持,请注意
 *  当前的音频格式支持 MP3, AAC(m4a后缀), 采样率为44100, 通道数为2,其他格式暂不支持,请注意
 */
public class AudioPadExecute {
	
	private static final String TAG = "AudioPadExecute";
	AudioPad   sampleMng;
	
	/**
	 * 构造方法,  
	 * @param ctx
	 * @param dstPath 因编码后的为aac格式, 故此路径的文件后缀需m4a或aac; 比如 "/sdcard/testAudioPad.m4a"
	 */
	public AudioPadExecute(Context ctx, String dstPath)
	{
		sampleMng=new AudioPad(ctx,dstPath);
	}
	/**
	 * 给AudioPad 增加一整段音频.
	 * 开始线程前调用.
	 * 
	 * 增加后, AudioPad会以音频的总长度为pad的长度, 其他增加的音频则是和这个音频的某一段混合.
	 * @param mainAudio
	 * @param volume   初始音量, 默认是1.0f, 大于1.0为放大, 小于1.0为缩小.
	 * @return  返回增加好的这个音频的对象, 可以根据这个来实时调节音量.
	 */
	public AudioSource setAudioPadSource(String mainAudio,float volume)
	{
		 if(sampleMng!=null){
			 return sampleMng.addMainAudio(mainAudio,volume);
		 }else{
			 return null;
		 }
	}
	/**
	 * 设置 音频处理的总长度.单位秒.
	 * 开始线程前调用.
	 * 
	 * 如果您只想在 一整段音乐上增加别的音频,可以用{@link #setAudioPadSource(String)}
	 * @return
	 */
	public AudioSource setAudioPadLength(float  duration)
	{
		 if(sampleMng!=null){
			 return sampleMng.addMainAudio(duration);	 
		 }else{
			 return null;
		 }
	}
	 /**
	  * 增加一个其他音频
	  * (可以反复增加多段音频)
	  *  开始线程前调用.
	  * @param srcPath 音频的完成路径地址
	  * @param startTimeMs  从AudioPad的什么时候开始增加, 可以随意增加, 如果中间有空隙,则默认无声.
	  * @param endTimeMs  到AudioPad的哪个时间段停止, 如果是-1, 则一直放入, 直到当前音频处理完.
	  * @param volume
	  * @return
	  */
	 public AudioSource addSubAudio(String srcPath,long startTimeMs,long endTimeMs,float volume) 
	 {
		 if(sampleMng!=null){
			 return sampleMng.addSubAudio(srcPath, startTimeMs, endTimeMs, volume);	 
		 }else{
			 return null;
		 }
	 }
	 /**
	  * 设置监听当前audioPad的处理进度. 
	  * 
	  * 此监听是通过handler机制,传递到UI线程的, 你可以在里面增加ui的代码.
	  * 因为经过了handler机制, 可能会进度比正在处理延迟一些,不完全等于当前处理的帧时间戳.
	  * @param listener
	  */
	 public void setAudioPadProgressListener(onAudioPadProgressListener listener)
	 {
		 if(sampleMng!=null){
			 sampleMng.setAudioPadProgressListener(listener);
		 }
	 }
	 /**
	  * 设置监听当前audioPad的处理进度.
	  * 一个音频帧处理完毕后, 直接执行您listener中的代码. 在audioPad线程中执行,不能在里面增加UI代码.
	  * 
	  * 建议使用这个.
	  * 
	  * 如果您声音在40s一下,建议使用这个, 因为音频本身很短,处理时间很快.
	  * @param listener
	  */
	 public void setAudioPadThreadProgressListener(onAudioPadThreadProgressListener listener)
	 {
		 if(sampleMng!=null){
			 sampleMng.setAudioPadThreadProgressListener(listener);
		 }
	 }
	 /**
	  * 完成监听.
	  * 经过handler传递到主线程, 可以在里面增加UI代码.
	  * @param listener
	  */
	 public void setAudioPadCompletedListener(onAudioPadCompletedListener listener)
	 {
		 if(sampleMng!=null){
			 sampleMng.setAudioPadCompletedListener(listener);
		 }
	 }
	 
	 public boolean start()
	 {
		 if(sampleMng!=null){
			 return sampleMng.start();
		 }else{
			 return false;
		 }
	 }
	 /**
	  * 等待执行完毕. 可选使用.
	  */
	 public void waitComplete()
	 {
		 if(sampleMng!=null){
			 sampleMng.joinSampleEnd();
		 }
	 }
	 public void stop()
	 {
		 if(sampleMng!=null){
			 sampleMng.stop();
		 }
	 }
	 public void release()
	 {
		 if(sampleMng!=null){
			 sampleMng.release();
			 sampleMng=null;
		 }
	 }
	 /**
	  * 测试代码如下.
	    	//举例1: 给完整的一段声音上增加其他声音
//			private AudioItem audioSrc1;
//	    	AudioPadExecute   audioPad=new AudioPadExecute(getApplicationContext(),"/sdcard/i9.m4a");
//	    	audioPad.setAudioPadSource("/sdcard/audioPadTest/niu30s_44100_2.m4a", 1.0f);
//	    	audioPad.addSubAudio("/sdcard/audioPadTest/hongdou10s_44100_2.mp3", 3*1000, 6*1000,5.0f); //中间3秒增加一段
//	    	audioPad.start();
//	    	audioPad.waitComplete();
//	    	audioPad.release();
	    	
	    	
		    //举例2:创建一段静音的音频.
//	    	AudioPadExecute   audioPad=new AudioPadExecute(getApplicationContext(),"/sdcard/i6.m4a");
//	    	audioPad.setAudioPadLength(60.0f);
//	    	audioPad.start();
//	    	audioPad.waitComplete();
//	    	audioPad.release();
	    	
	    	//举例3: 先设置AudioPad的总长度, 然后在不同的时间点增加几段声音.处理成同一个.
//	    	AudioPadExecute   audioPad=new AudioPadExecute(getApplicationContext(),"/sdcard/i8.m4a");
//	    	
//	    	audioPad.setAudioPadLength(60.0f);  //定义生成一段15秒的声音./或者你可以把某一个音频作为一个主音频
//	    	
//	    	audioPad.addSubAudio("/sdcard/audioPadTest/du15s_44100_2.mp3", 0, 3*1000,1.0f);  //在这15内, 的前3秒增加一个声音
//	    	audioSrc1=audioPad.addSubAudio("/sdcard/audioPadTest/hongdou10s_44100_2.mp3", 3*1000, 6*1000,1.0f); //中间3秒增加一段
//	    	audioPad.addSubAudio("/sdcard/audioPadTest/niu30s_44100_2.m4a", 10*1000, -1,1.0f);  //最后3秒增加一段.
//	    	
//	    	
//	    	audioPad.setAudioPadCompletedListener(new onAudioPadCompletedListener() {
//				
//				@Override
//				public void onCompleted(AudioPad v) {
//					Log.i(TAG,"已经执行完毕了....");
//					
//					v.release();   //释放(内部会检测是否执行完, 如没有,则等待执行完毕).
//					MediaPlayer  player=new MediaPlayer();
//			    	try {
//						player.setDataSource("/sdcard/i8.m4a");
//						player.prepare();
//						player.start();
//						
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			});
//	    	audioPad.setAudioPadProgressListener(new onAudioPadProgressListener() {
//				
//				@Override
//				public void onProgress(AudioPad v, long currentTimeUs) {
//					Log.i(TAG,"当前progess的进度是:"+currentTimeUs);
//					tvVideoPath.setText("进度是:"+currentTimeUs/1000);
//				}
//			});
//	    	audioPad.setAudioPadThreadProgressListener(new onAudioPadThreadProgressListener() {
//				
//				@Override
//				public void onProgress(AudioPad v, long currentTimeUs) {
//					Log.i(TAG,"当前Thread progess的进度是:"+currentTimeUs);
//					if(audioSrc1!=null){
//						
//						if(currentTimeUs>5000*1000){
//							audioSrc1.setVolume(0.2f);
//						}else if(currentTimeUs>3500*1000){
//							audioSrc1.setVolume(3.0f);
//						}
//					}
//				}
//			});
//	    	
//	    	audioPad.start();  //开始运行 ,另开一个线程,异步执行.
	  */
}

