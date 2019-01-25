package com.terminus.testfaces;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.leon.lfilepickerlibrary.LFilePicker;
import com.terminus.sz.facelibrary.bean.FaceInfo;
import com.terminus.sz.facelibrary.util.ImageUtil;

import java.io.File;
import java.util.List;

public class MainActivity extends BaseActivity {
    private static final int REQUEST_CODE_SELECT_FOLDER = 1000;
    private Task importTask;
    private FaceSDK faceSDK;
    private Handler mHandler;
    private Context context;

    private TextView tv_log;
    private ScrollView scrollview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int contentViewId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        context = this;
        final LFilePicker filePicker = new LFilePicker()
                .withActivity(this)
                .withRequestCode(REQUEST_CODE_SELECT_FOLDER)
                .withChooseMode(false)
                .withIsGreater(false);

        Button btn_import = findViewById(R.id.btn_import);
        btn_import.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filePicker.start();
            }
        });

        tv_log =findViewById(R.id.tv_log);
        scrollview = findViewById(R.id.scrollview);

        Button btn_reset = findViewById(R.id.btn_reset);
        tv_log.setText("日志：\n");
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_log.setText("日志：\n");
            }
        });

        initFaceAlgorithm();
        mHandler = new Handler(Looper.getMainLooper());
    }

    private void requestBitmap(final String url, final BitmapListener listener){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Glide.with(context).load(url).asBitmap().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(final Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        if(resource != null && listener != null){
                            listener.onGet(resource);
                        }
                    }
                });
            }
        });
    }

    private interface BitmapListener{
        void onGet(Bitmap bitmap);
    }


    public void initFaceAlgorithm(){
        if(faceSDK == null){
            faceSDK = new FaceSDK();
        }
        if(!faceSDK.isInit() &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
            faceSDK.init(this, FaceSDK.MODE_IMAGE, FaceSDK.ORIENT_0, 32);
            faceSDK.setFormat(FaceSDK.NV21);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(faceSDK != null){
            faceSDK.release();
            faceSDK = null;
        }

        if(importTask != null){
            importTask.stop();
            importTask = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_SELECT_FOLDER) {
                final String path = data.getStringExtra("path");
                LogUtils.e("The selected path is:" + path);

                Glide.get(context).clearMemory();
                showLoadingDialog(this, "正在导入，请稍等...");

                if(importTask != null){
                    importTask.stop();
                    importTask = null;
                }
                importTask = new Task();
                importTask.setInfinite();
                importTask.setQueueListener(new Task.QueueListener() {
                    @Override
                    public void onTake(Object object) {
                        if(object instanceof TaskHolder){
                            final TaskHolder holder = (TaskHolder)object;
                            requestBitmap(holder.url, new BitmapListener() {
                                @Override
                                public void onGet(final Bitmap bitmap) {
                                    try {
                                        if (bitmap != null && !bitmap.isRecycled()) {
                                            ThreadPoolUtils.execute(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (bitmap != null && !bitmap.isRecycled()) {
                                                        LogUtils.e("url=" + holder.url + "图片大小：宽 = " + bitmap.getWidth() + "，高 = " + bitmap.getHeight());
                                                        byte[] data = ImageUtil.bitmapToNv21(bitmap, bitmap.getWidth(), bitmap.getHeight());
                                                        List<FaceInfo> faceInfos = faceSDK.detectFaces(data, bitmap.getWidth(), bitmap.getHeight());
                                                        byte[] feature = null;
                                                        if(faceInfos != null && faceInfos.size() > 0){
                                                            int index = FaceSDK.findLargestFaceIndex(faceInfos);
                                                            final int faceWidth = faceInfos.get(index).getRect().width();
                                                            final int faceHeight = faceInfos.get(index).getRect().height();
                                                            mHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    tv_log.append("url:" + holder.url + "检测到人脸， 宽：" + faceWidth + "，高：" + faceHeight + "\n");
                                                                    scrollview.fullScroll(ScrollView.FOCUS_DOWN);
                                                                }
                                                            });
//                                                            feature = faceSDK.getFaceFeatureAfterDetect(data, bitmap.getWidth(), bitmap.getHeight(), faceInfos.get(index));
                                                        }else{
                                                            mHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    tv_log.append("url:" + holder.url + "检测不到人脸\n");
                                                                    scrollview.fullScroll(ScrollView.FOCUS_DOWN);
                                                                }
                                                            });
                                                        }
//                                                        byte[] feature = faceSDK.getFaceFeature(bitmap, bitmap.getWidth(), bitmap.getHeight());
//                                                        if (feature != null) {
//                                                            int personId = faceSDK.addPerson(feature);
//                                                            if (personId > 0) {
//                                                                LogUtils.e("算法库添加人员成功：personId = " + personId);
//                                                                //保存照片
//                                                                LogUtils.e("添加人员照片，url：" + holder.url);
//                                                            }else{
//                                                                LogUtils.e("算法库已存在：url：" + holder.url);
//                                                            }
//                                                        } else {
//                                                            LogUtils.e("url:" + holder.url + "，特征值为null");
//                                                        }
                                                        if (holder.position == holder.fileLength - 1) {
                                                            dismissLoadingDialog();
                                                            showTip("照片导入完成");
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                });
                importTask.start();

                ThreadPoolUtils.execute(new Runnable() {
                    @Override
                    public void run() {
                        Glide.get(context).clearDiskCache();
                        File facesPath = new File(path);
                        final File[] files = facesPath.listFiles();
                        if(files != null && files.length > 0) {
                            for (int j = 0; j < files.length; j++) {
                                if (files[j].isFile()) {
                                    if (importTask != null) {
                                        String url = files[j].getPath();
                                        TaskHolder holder = new TaskHolder();
                                        holder.position = j;
                                        holder.url = url;
                                        holder.fileLength = files.length;
                                        importTask.enqueue(holder);
                                    }
                                }
                            }
                        }else{
                            dismissLoadingDialog();
                        }
                    }
                });
            }
        }
    }

    private class TaskHolder{
        public int position;
        public String url;
        public int fileLength;
    }
}
