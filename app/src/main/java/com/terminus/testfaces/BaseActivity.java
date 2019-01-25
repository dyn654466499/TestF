package com.terminus.testfaces;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class BaseActivity extends Activity {
    private Toast mToast;
    private Handler mHandler = new Handler();
    private NetworkRequest networkRequest;
    private ConnectivityManager.NetworkCallback networkCallback;
    private ConnectivityManager connectivityManager;

    protected Handler workerThreadHandler;
    protected HandlerThread workerThread;

    protected Boolean isOnResume = false, customEventBus = false;
    protected Boolean lock = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        PermissionsUtils.
                with(this).
                addPermission(Manifest.permission.ACCESS_FINE_LOCATION).
                addPermission(Manifest.permission.ACCESS_COARSE_LOCATION).
                addPermission(Manifest.permission.ACCESS_NETWORK_STATE).
                addPermission(Manifest.permission.ACCESS_WIFI_STATE).
                addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE).
                addPermission(Manifest.permission.READ_EXTERNAL_STORAGE).
                addPermission(Manifest.permission.CAMERA).
                addPermission(Manifest.permission.CALL_PHONE).
                addPermission(Manifest.permission.READ_PHONE_STATE).
                addPermission(Manifest.permission.CHANGE_NETWORK_STATE).
                addPermission(Manifest.permission.CHANGE_WIFI_STATE).
                addPermission(Manifest.permission.CHANGE_CONFIGURATION).
                initPermission();


        if (mToast == null)
            mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);


        setContentView(contentViewId());
        initView();

    }

    protected abstract int contentViewId();
    protected abstract void initView();

    @Override
    protected void onResume() {
        super.onResume();
        hideBottomUIMenu();
        isOnResume = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isOnResume = false;
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    /**
     * 标准控件——加载对话框
     */
    protected static LoadingDialog mLoadingDialog;

    protected void showLoadingDialog(Context ctx) {
        showLoadingDialog(ctx, null);
    }
    /**
     * 显示加载对话框
     * @param message 默认"正在加载..."
     */
    protected void showLoadingDialog(final Context ctx, final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mLoadingDialog != null) {
                    mLoadingDialog.dismiss();
                    mLoadingDialog = null;
                }
                mLoadingDialog = new LoadingDialog(ctx);
                mLoadingDialog.setCanceledOnTouchOutside(false);
                if (!TextUtils.isEmpty(message))
                    mLoadingDialog.setLoadingText(message);
                mLoadingDialog.show();
            }
        });
    }

    protected void setLoadingText(final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(message) && mLoadingDialog != null){
                    mLoadingDialog.setLoadingText(message);
                }
            }
        });
    }

    /**
     * 使加载对话框消失
     */
    protected void dismissLoadingDialog() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                    mLoadingDialog.dismiss();
                    mLoadingDialog = null;
                }
            }
        });
    }

    /**
     * 提示框
     * @param str
     */
    public void showTip(final String str) {
        if (!TextUtils.isEmpty(str)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mToast.setText(str);
                    mToast.setGravity(Gravity.CENTER,0,0);
                    mToast.show();
                }
            });
        }
    }

    /**
     * 取消提示框
     */
    public void cancelShowTip() {
        if (mToast!=null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mToast.cancel();
                }
            });
        }
    }

    /**
     * 隐藏软键盘(只适用于Activity，不适用于Fragment)
     */
    public static void hideSoftKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 禁止Edittext弹出软件盘，光标依然正常显示。
     */
    public void disableShowSoftInput(EditText editText) {
        if(editText != null) {
            if (Build.VERSION.SDK_INT >= 11) {
                Class<EditText> cls = EditText.class;
                try {
                    Method setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                    setShowSoftInputOnFocus.setAccessible(false);
                    setShowSoftInputOnFocus.invoke(editText, false);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else {
                int type = editText.getInputType();
                editText.setInputType(InputType.TYPE_NULL);// 让系统键盘不弹出
                editText.setInputType(type);
            }
        }
    }

    protected void startActivityByAnimation(Intent intent){
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    protected void startWorkerThread(final Handler.Callback callback){
        stopWorkerThread();
        workerThread = new HandlerThread("workerThread");
        workerThread.start();
        workerThreadHandler = new Handler(workerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(callback != null){
                    callback.handleMessage(msg);
                }
            }
        };
    }

    protected void stopWorkerThread(){
        if(workerThread != null){
            try{
                workerThread.quitSafely();
                workerThread = null;
                workerThreadHandler = null;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissLoadingDialog();
    }


    protected class KeyValue{
        public static final String KEY_1 = "1";
        public static final String KEY_2 = "2";
        public static final String KEY_3 = "3";
        public static final String KEY_4 = "4";
        public static final String KEY_5 = "5";
        public static final String KEY_6 = "6";
        public static final String KEY_7 = "7";
        public static final String KEY_8 = "8";
        public static final String KEY_9 = "9";
        public static final String KEY_0 = "0";
        public static final String KEY_STAR = "*";
        public static final String KEY_JING = "#";
    }
}
