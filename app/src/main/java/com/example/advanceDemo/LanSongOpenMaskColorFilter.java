package com.example.advanceDemo;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.util.Log;

/**
 *  
 *这样可以扣除黑色.
 *	gl_FragColor=vec4(0.0);\n" +
      "		else \n "+
      "			gl_FragColor=vec4(textureColor.rgb,textureColor.r);\n"+	
      
      "void inRange(vec4 textureColor,vec3 min,vec3 max)"
 */
public class LanSongOpenMaskColorFilter  extends GPUImageFilter {
//	//以下采用这种//绿色分量>128&<255  红色分量<50 蓝色<50
	public static final String LAN_SONG_MASK_COLOR_FRAGMENT_SHADER = 
			"varying highp vec2 textureCoordinate;\n" +
	      "\n" +
//	      " uniform sampler2D inputImageTexture;\n" +
	" uniform vec3 colmin;\n" +
	      " \n" +
	      " void main()\n" +
	      " {\n" +
	      "     highp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
	      "     if (textureColor.g>colmin.g && textureColor.r<colmin.r && textureColor.b<colmin.b)" + 
	      "			gl_FragColor=vec4(0.0);\n" +
	      "		else{" +
	      "			gl_FragColor=textureColor;\n" +
	      "		} \n "+
	      " }"; 
    
	public LanSongOpenMaskColorFilter() {
		super(NO_FILTER_VERTEX_SHADER,LAN_SONG_MASK_COLOR_FRAGMENT_SHADER);
	}
	    @Override
	    public String getFragmentShader()
	    {
	    	return LAN_SONG_MASK_COLOR_FRAGMENT_SHADER;
	    }
	   @Override
	    public void onInit(int programId) {
	        super.onInit(programId);

	        mColorMinLocation = GLES20.glGetUniformLocation(getProgram(), "colmin");

	    }
	   @Override
	   public void onInit() {
	       super.onInit();

	        mColorMinLocation = GLES20.glGetUniformLocation(getProgram(), "colmin");

	   }
	    @Override
	    public void onInitialized() {
	        super.onInitialized();
	      
	        setColorToReplace(0.3f,0.5f,0.3f);
	    }

	    private float[] mColorMin = new float[]{0.3f,0.5f,0.3f};
	    private int mColorMinLocation;
	    /**
	     * 分量最大是1.0, 最小是0.0
	     * @param redComponent
	     * @param greenComponent
	     * @param blueComponent
	     */
	    public void setColorToReplace(float redComponent, float greenComponent, float blueComponent) {
	        mColorMin = new float[]{redComponent, greenComponent, blueComponent};
	        setFloatVec3(mColorMinLocation, mColorMin);
	    }
	    
}