package com.example.advanceDemo.layerDemo;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;

import com.lansosdk.box.BitmapLoader;
import com.lansosdk.box.LSLog;

import java.lang.reflect.Array;
import java.util.ArrayList;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImage3x3ConvolutionFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageAddBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageAlphaBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageCGAColorspaceFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageChromaKeyBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageColorBalanceFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageColorBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageColorBurnBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageColorDodgeBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageColorInvertFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageContrastFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageCrosshatchFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageDarkenBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageDifferenceBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageDissolveBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageDivideBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageEmbossFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageExclusionBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageExposureFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFalseColorFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageGammaFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageGlassSphereFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageHalftoneFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageHardLightBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageHazeFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageHighlightShadowFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageHueBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageHueFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageKuwaharaFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageLaplacianFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageLevelsFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageLightenBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageLinearBurnBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageLookupFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageLuminosityBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageMonochromeFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageMultiplyBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageNormalBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageOpacityFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageOverlayBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImagePixelationFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImagePosterizeFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageRGBFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSaturationBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSaturationFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageScreenBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSoftLightBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSourceOverBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSphereRefractionFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSubtractBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSwirlFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageToonFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageTwoInputFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageVignetteFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageWhiteBalanceFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IF1977Filter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFAmaroFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFBrannanFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFEarlybirdFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFHefeFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFHudsonFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFInkwellFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFLomofiFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFLordKelvinFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFNashvilleFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFRiseFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFSierraFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFSutroFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFToasterFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFValenciaFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFWaldenFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFXproIIFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBeautyAdvanceFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBlackMaskBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBlurFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBulgeDistortionFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongDistortionPinchFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongDistortionStretchFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongMaskBlendFilter;

public class TestAllFilter {

    private ArrayList<GPUImageFilter> filters=new ArrayList<>();


    private static TestAllFilter mInstance=null;

    public static  TestAllFilter getInstance(Context ctx){
        if(mInstance==null){
            mInstance=new TestAllFilter(ctx);
        }
        return mInstance;
    }


    public TestAllFilter(Context ctx){
        addFilter(ctx);
    }


    public  GPUImageFilter getFilter(int index){
        if(index>=0 && index<filters.size()){
            LSLog.i("获取 NO."+ index);
            return filters.get(index);
        }else{
            LSLog.e("TestAllFilter  error .return null.index :"+index + "  filters.size:"+filters.size());
            return null;
        }
    }
    public int getFilterSize(){
        return filters.size();
    }
    private void addFilter(Context context)
    {

        //共82个
        filters.add(new GPUImageFilter());
        filters.add(new LanSongBeautyAdvanceFilter());
        filters.add(new GPUImageContrastFilter(2.0f));
        filters.add(new GPUImageGammaFilter(2.0f));
        filters.add(new GPUImageColorInvertFilter());
        filters.add(new GPUImagePixelationFilter());
        filters.add(new GPUImageHueFilter(90.0f));
        filters.add(new GPUImageBrightnessFilter(0.5f));
        filters.add(new GPUImageGrayscaleFilter());
        filters.add(new GPUImageSepiaFilter());
        filters.add(new GPUImagePosterizeFilter());
        filters.add(new GPUImageSaturationFilter(1.0f));
        filters.add(new GPUImageExposureFilter(0.0f));
        filters.add(new GPUImageHighlightShadowFilter(0.0f, 1.0f));
        filters.add(new GPUImageMonochromeFilter(1.0f, new float[]{0.6f, 0.45f, 0.3f, 1.0f}));
        filters.add(new GPUImageRGBFilter(1.0f, 1.0f, 1.0f));
        filters.add(new GPUImageWhiteBalanceFilter(5000.0f, 0.0f));
        filters.add(new LanSongBlurFilter());
        filters.add(new GPUImageVignetteFilter( new PointF(0.5f,0.5f), new float[]{0.0f,0.0f, 0.0f}, 0.3f, 0.75f));
        filters.add(createBlendFilter(context, LanSongBlackMaskBlendFilter.class));
        filters.add(createBlendFilter(context, LanSongMaskBlendFilter.class));
        filters.add(createBlendFilter(context, GPUImageDifferenceBlendFilter.class));
        filters.add(createBlendFilter(context, GPUImageSourceOverBlendFilter.class));
        filters.add(createBlendFilter(context, GPUImageColorBurnBlendFilter.class));
        filters.add(createBlendFilter(context, GPUImageColorDodgeBlendFilter.class));
        filters.add(createBlendFilter(context, GPUImageDarkenBlendFilter.class));
        filters.add(createBlendFilter(context, GPUImageDissolveBlendFilter.class));
        filters.add(createBlendFilter(context,GPUImageExclusionBlendFilter.class));
        filters.add(createBlendFilter(context,GPUImageHardLightBlendFilter.class));
        filters.add(createBlendFilter(context, GPUImageLightenBlendFilter.class));
        filters.add(createBlendFilter(context, GPUImageAddBlendFilter.class));
        filters.add(createBlendFilter(context, GPUImageDivideBlendFilter.class));
        filters.add(createBlendFilter(context, GPUImageMultiplyBlendFilter.class));
        filters.add(createBlendFilter(context, GPUImageOverlayBlendFilter.class));
        filters.add(createBlendFilter(context, GPUImageScreenBlendFilter.class));
        filters.add(createBlendFilter(context, GPUImageAlphaBlendFilter.class));
        filters.add(createBlendFilter(context, GPUImageColorBlendFilter.class));
        filters.add(createBlendFilter(context, GPUImageHueBlendFilter.class));
        filters.add(createBlendFilter(context, GPUImageSaturationBlendFilter.class));
        filters.add(createBlendFilter(context, GPUImageLuminosityBlendFilter.class));
        filters.add(createBlendFilter(context, GPUImageLinearBurnBlendFilter.class));
        filters.add(createBlendFilter(context,GPUImageSoftLightBlendFilter.class));
        filters.add(createBlendFilter(context, GPUImageSubtractBlendFilter.class));
        filters.add(createBlendFilter(context,GPUImageChromaKeyBlendFilter.class));
        filters.add(createBlendFilter(context, GPUImageNormalBlendFilter.class));
        filters.add(new GPUImageCrosshatchFilter());
        filters.add(new GPUImageCGAColorspaceFilter());
        filters.add(new GPUImageKuwaharaFilter());
        filters.add(new LanSongBulgeDistortionFilter());
        filters.add(new LanSongDistortionPinchFilter());
        filters.add(new LanSongDistortionStretchFilter());
        filters.add(new GPUImageGlassSphereFilter());
        filters.add(new GPUImageHazeFilter());
        filters.add(new GPUImageSphereRefractionFilter());
        filters.add(new GPUImageSwirlFilter());
        filters.add(new GPUImageFalseColorFilter());
        filters.add(new GPUImageColorBalanceFilter());
        filters.add(new GPUImageHalftoneFilter());
        filters.add(new IFAmaroFilter(context));
        filters.add(new IFRiseFilter(context));
        filters.add(new IFHudsonFilter(context));
        filters.add(new IFXproIIFilter(context));
        filters.add(new IFSierraFilter(context));
        filters.add(new IFLomofiFilter(context));
        filters.add(new IFEarlybirdFilter(context));
        filters.add(new IFSutroFilter(context));
        filters.add(new IFToasterFilter(context));
        filters.add(new IFToasterFilter(context));
        filters.add(new IFBrannanFilter(context));
        filters.add(new IFInkwellFilter(context));
        filters.add(new IFWaldenFilter(context));
        filters.add(new IFHefeFilter(context));
        filters.add(new IFValenciaFilter(context));
        filters.add(new IFNashvilleFilter(context));
        filters.add(new IFLordKelvinFilter(context));
        filters.add(new IF1977Filter(context));

        filters.add(new GPUImageEmbossFilter());
        filters.add(new GPUImageLaplacianFilter());
        filters.add(new GPUImageToonFilter());




        GPUImageLookupFilter amatorka = new GPUImageLookupFilter();
        String var3 = "assets://LSResource/lookup_amatorka.png";
        amatorka.setBitmap(BitmapLoader.load(context, var3, 0, 0));
        filters.add(amatorka);

        GPUImageLevelsFilter levelsFilter = new GPUImageLevelsFilter();
        levelsFilter.setMin(0.0f, 3.0f, 1.0f);
        filters.add(levelsFilter);

        GPUImage3x3ConvolutionFilter convolution = new GPUImage3x3ConvolutionFilter();
        convolution.setConvolutionKernel(new float[]{-1.0f, 0.0f, 1.0f,
                -2.0f, 0.0f, 2.0f, -1.0f, 0.0f, 1.0f});

        filters.add(convolution);
    }

    private static GPUImageFilter createBlendFilter(Context context,
                                                    Class<? extends GPUImageTwoInputFilter> filterClass) {
        try {
            GPUImageTwoInputFilter filter = filterClass.newInstance();
            String var3 = "assets://LSResource/blend_demo.png"; //这里只是为了方便,用默认图片;
            filter.setBitmap(BitmapLoader.load(context, var3, 0, 0));
            return filter;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
