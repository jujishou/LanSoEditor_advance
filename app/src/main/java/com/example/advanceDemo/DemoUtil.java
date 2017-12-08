package com.example.advanceDemo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.lansoeditor.demo.R;
import com.lansosdk.videoeditor.SDKFileUtils;

public class DemoUtil {

private int interval=5;
	
	private List<Bitmap>  bmpLists=new ArrayList<Bitmap>();
	public void pushBitmap(Bitmap bmp){
		
		interval++;
		if(bmpLists.size()<5 && bmp!=null && interval%5==0){
			bmpLists.add(bmp);
		}else{
			Log.i("T"," size >20; push error!");
		}
	}
	/**
	 * 同步执行, 很慢.
	 */
	public void saveToSdcard()
	{
		for(Bitmap bmp: bmpLists){
			savePng(bmp);
		}
	}
	 static int bmtcnt=0;
	 public static void deleteAll()
	 {
		 deleteDir(new File("/sdcard/extract/"));
		 
	 }
	 static boolean deleteDir(File dir) {
	        if (dir.isDirectory()) {
	            String[] children = dir.list();
	            for (int i=0; i<children.length; i++) {
	                boolean success = deleteDir(new File(dir, children[i]));
	                if (!success) {
	                    return false;
	                }
	            }
	        }
	        // 目录此时为空，可以删除
	        return dir.delete();
	    }
	 /**
	  * 把图片保存到文件, 
	  * 这里只是用来调试程序使用.
	  * @param bmp
	  */
	 public static void savePng(Bitmap bmp)
	 {
		 if(bmp!=null)
		 {
			 File dir=new File("/sdcard/extract/");
			 if(dir.exists()==false){
				 dir.mkdirs();
			 }
			  try {
					  BufferedOutputStream  bos;
					  String name="/sdcard/view"+ bmtcnt++ +".png";
					  Log.i("savePng","name:"+name);
					  
					  bos = new BufferedOutputStream(new FileOutputStream(name));
					  bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
					  bos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		 }else{
			 Log.i("savePng","error  bmp  is null");
		 }
	 }
	public static void showToast(Context ctx,String str){
		Toast.makeText(ctx, str, Toast.LENGTH_SHORT).show();
    	Log.i("x",str);
	}
	   public static void showHintDialog(Context ctx,int stringId)
	   	{
	      new AlertDialog.Builder(ctx)
	   		.setTitle("提示")
	   		.setMessage(stringId)
	           .setPositiveButton("确定", new DialogInterface.OnClickListener() {
	   			
	   			@Override
	   			public void onClick(DialogInterface dialog, int which) {
	   			}
	   		})
	           .show();
	   	}
	   public static void showHintDialog(Context ctx,String str)
	   	{
	      new AlertDialog.Builder(ctx)
	   		.setTitle("提示")
	   		.setMessage(str)
	           .setPositiveButton("确定", new DialogInterface.OnClickListener() {
	   			
	   			@Override
	   			public void onClick(DialogInterface dialog, int which) {
	   			}
	   		})
	           .show();
	   	}
//mhandler.sendMessageDelayed(mhandler.obtainMessage(23),10);  //别地方调用	
//	    private HandlerLoop mhandler=new HandlerLoop();
//	    private int maskCnt=1;
//	    private class HandlerLoop extends Handler
//	    {
//	    	@Override
//	    	public void handleMessage(Message msg) {
//	    		super.handleMessage(msg);
//	    
//	    		switchBitmap();
//	    		mhandler.sendMessageDelayed(mhandler.obtainMessage(23),10);
//	    	}
//	    }
}
