package com.terminus.testfaces;

/**
 * Created by dengyaoning on 17/6/18.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

/**
 */
public class LoadingDialog extends AlertDialog {
    private static final String TAG = LoadingDialog.class.getSimpleName();
    Activity mParentActivity;
    private OnDismissListener dismissListener;
    private OnCancelListener cancelListener;
    private TextView tv_loading_msg;
    /**
     * 构造函数
     *
     * @param context
     * @param isModal 是否模态
     */
    public LoadingDialog(Context context, boolean isModal) {
        super(context, R.style.loadingDialog);
        mParentActivity = (Activity)context;
        setCanceledOnTouchOutside(!isModal);
        setCancelable(false);  // 默认不支持撤销
    }

    public LoadingDialog(Context context) {
        this(context, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dialog_progress);
//        ImageView iv_loading_icon = (ImageView)findViewById(R.id.iv_loading_icon);

        if(!TextUtils.isEmpty(loadingText)){
            tv_loading_msg = findViewById(R.id.tv_loading_msg);
            tv_loading_msg.setText(loadingText);
        }

//        AlphaAnimation alphaAnimation1 = new AlphaAnimation(0.5f, 1.0f);
//        alphaAnimation1.setDuration(500);
//        alphaAnimation1.setRepeatCount(Animation.INFINITE);
//        alphaAnimation1.setRepeatMode(Animation.REVERSE);
//        iv_loading_icon.setAnimation(alphaAnimation1);
//        alphaAnimation1.start();
    }

    private String loadingText;
    public void setLoadingText(final String text){
        if(tv_loading_msg != null){
            tv_loading_msg.setText(text);
        }
        loadingText = text;
    }

    @Override
    public void setOnShowListener(OnShowListener listener) {
        super.setOnShowListener(listener);
    }

    @Override
    public void setOnDismissListener(OnDismissListener listener) {
        super.setOnDismissListener(listener);
        dismissListener = listener;
    }

    @Override
    public void setOnCancelListener(OnCancelListener listener) {
        super.setOnCancelListener(listener);
        cancelListener = listener;
    }

    public void setClickDismissListener(OnClickDismissListener clickDismissListener) {
        this.clickDismissListener = clickDismissListener;
    }

    private OnClickDismissListener clickDismissListener;
    public interface OnClickDismissListener{
        void onClickDismiss();
    }
}


