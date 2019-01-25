package com.terminus.testfaces;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;

import com.terminus.sz.facelibrary.FaceAlgorithm;
import com.terminus.sz.facelibrary.bean.FaceInfo;
import com.terminus.sz.facelibrary.util.ImageUtil;

import java.util.List;

public class FaceSDK {
    private volatile FaceAlgorithm faceAlgorithm;

    private volatile int initResult = -1;

    public static final int NV21 = FaceAlgorithm.Format.NV21;
    public static final int BGR_24 = FaceAlgorithm.Format.BGR24;

    public static final int ATTR_AGE = FaceAlgorithm.Attribute.AGE;
    public static final int ATTR_GENDER = FaceAlgorithm.Attribute.GENDER;
    public static final int ATTR_FACE3DANGLE = FaceAlgorithm.Attribute.FACE3DANGLE;
    public static final int ATTR_LIVENESS = FaceAlgorithm.Attribute.LIVENESS;

    public static final int ATTR_RECOGNIZE_LIVENESS = ATTR_FACE3DANGLE | ATTR_LIVENESS;
    public static final int ATTR_RECOGNIZE = ATTR_FACE3DANGLE;
    public static final int ATTR_RECORD = ATTR_FACE3DANGLE;

    private int format = BGR_24;

    public static final int MODE_VIDEO = FaceAlgorithm.Mode.DETECT_MODE_VIDEO;
    public static final int MODE_IMAGE = FaceAlgorithm.Mode.DETECT_MODE_IMAGE;

    public static final int ORIENT_0 = FaceAlgorithm.Orientation.OP_0_ONLY;
    public static final int ORIENT_90 = FaceAlgorithm.Orientation.OP_90_ONLY;
    public static final int ORIENT_180 = FaceAlgorithm.Orientation.OP_180_ONLY;
    public static final int ORIENT_270 = FaceAlgorithm.Orientation.OP_270_ONLY;
    public static final int ORIENT_ALL = FaceAlgorithm.Orientation.OP_0_HIGHER_EXT;

    public synchronized void init(Context context, int mode, int orientation){
        init(context, mode, orientation, 10);
    }

    public synchronized void init(Context context, int mode, int orientation, int detectFaceScaleVal){
        if(faceAlgorithm == null){
            faceAlgorithm = new FaceAlgorithm(context.getApplicationContext());
            initResult = faceAlgorithm.init(FaceAlgorithm.Company.ALGORITHM_ARC_SOFE, mode, orientation, 0.8f, detectFaceScaleVal, 6);
        }
    }

    public void setFormat(int format){
        this.format = format;
    }

    public synchronized boolean isInit(){
        return initResult == 0;
    }

    public synchronized List<FaceInfo> detectFaces(byte[] data, int width, int height){
        if(faceAlgorithm != null && initResult == 0){
            return faceAlgorithm.detectFaces(data, width, height, format);
        }
        return null;
    }

    public List<FaceInfo> process(byte[] data, int width, int height, List<FaceInfo> faceInfos, int mask){
        if(faceAlgorithm != null && initResult == 0){
            return faceAlgorithm.progress(data, width, height, format, faceInfos, mask);
        }
        return null;
    }

    public synchronized boolean deletePerson(int personId){
        if(faceAlgorithm != null && initResult == 0){
            return faceAlgorithm.deletePersion(personId);
        }
        return false;
    }

    public synchronized float compareFaceFeature(byte[] faceFeature1, byte[] faceFeature2){
        if(faceAlgorithm != null && initResult == 0){
            return faceAlgorithm.compareFaceFeature(faceFeature1, faceFeature2);
        }
        return -1;
    }

    public synchronized byte[] getFaceFeature(byte[] data, int width, int height){
        if(faceAlgorithm != null && initResult == 0){
            List<FaceInfo> faceInfos = faceAlgorithm.detectFaces(data, width, height, NV21);
            if(faceInfos != null && faceInfos.size() > 0){
                int index = findLargestFaceIndex(faceInfos);
                return faceAlgorithm.getFaceFeature(data, width, height, faceInfos.get(index), NV21);
            }
        }
        return null;
    }

    public synchronized byte[] getFaceFeature(Bitmap bitmap, int width, int height){
        if(faceAlgorithm != null && initResult == 0){
            byte[] data = ImageUtil.bitmapToNv21(bitmap, width, height);
            List<FaceInfo> faceInfos = faceAlgorithm.detectFaces(data, width, height, NV21);
            if(faceInfos != null && faceInfos.size() > 0){
                int index = findLargestFaceIndex(faceInfos);
                return faceAlgorithm.getFaceFeature(data, width, height, faceInfos.get(index), NV21);
            }
        }
        return null;
    }

    public synchronized byte[] getFaceFeatureAfterDetect(byte[] data, int width, int height, FaceInfo faceInfo){
        if(faceAlgorithm != null && initResult == 0){
            return faceAlgorithm.getFaceFeature(data, width, height, faceInfo, format);
        }
        return null;
    }

    public synchronized int recognize(byte[] feature){
        if(faceAlgorithm != null && initResult == 0){
            return faceAlgorithm.recognize(feature);
        }
        return 0;
    }

    public synchronized float[] recognizeAndScore(byte[] feature){
        if(faceAlgorithm != null && initResult == 0){
            return faceAlgorithm.recognizeAndScore(feature);
        }
        return null;
    }

    public synchronized int addPerson(byte[] feature) {
        if(faceAlgorithm != null && initResult == 0){
            return faceAlgorithm.addPerson(feature);
        }
        return -1;
    }

    /**
     * 找到最大人脸的索引
     * @return
     */
    public static int findLargestFaceIndex(List<FaceInfo> faces){
        int maxIndex = 0;
        if(faces != null && faces.size() > 0){
            //找到最大人脸框
            for (int i = 1; i < faces.size(); i++) {
                Rect rectMax = faces.get(maxIndex).getRect();
                Rect rect = faces.get(i).getRect();
                if (rectMax.width() * rectMax.height() <= rect.width() * rect.height()) {
                    maxIndex = i;
                }
            }
        }
        return maxIndex;
    }

    public synchronized void release(){
        if(faceAlgorithm != null && initResult == 0){
            faceAlgorithm.release();
            faceAlgorithm = null;
            initResult = -1;
        }
    }
}
