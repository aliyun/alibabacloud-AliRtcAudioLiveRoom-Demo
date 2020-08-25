package com.aliyun.rtc.audiochatroom.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.rtc.audiochatroom.R;
import com.aliyun.rtc.audiochatroom.utils.ScreenUtil;


public class RTCBottomDialog extends Dialog implements View.OnClickListener {

    private TextView mTvTitle;
    private ImageView mIvConfrim;
    private FrameLayout mFlContent;
    private Context mContext;
    private View mContentView;
    private String mTitle;
    private int mResId;
    private OnConfrimListener mOnConfrimListener;

    public RTCBottomDialog(@NonNull Context context) {
        this(context, R.style.CustomDialogStyle);
    }

    public RTCBottomDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.rtc_audioliveroom_layout_bottom_dialog);
        initView();
        Log.i("TAG", "onCreate: ");
        setData();
        initLocation();
    }

    private void initLocation() {
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.x = 0;
            attributes.y = ScreenUtil.getScreenHeight(((Activity) mContext));
            attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setWindowAnimations(R.style.Dialog_Animation);
            onWindowAttributesChanged(attributes);
        }
    }

    private void setData() {
        mIvConfrim.setImageResource(R.drawable.rtc_audioliveroom_close);
        mFlContent.removeAllViews();
        mFlContent.addView(mContentView);
        mTvTitle.setText(!TextUtils.isEmpty(mTitle) ? mTitle : mResId > 0 ? getContext().getString(mResId) : "");
    }

    private void initView() {
        mTvTitle = findViewById(R.id.rtc_audioliveroom_tv_title_bottom_dialog);
        mIvConfrim = findViewById(R.id.rtc_audioliveroom_iv_confrim_bottom_dialog);
        mFlContent = findViewById(R.id.rtc_audioliveroom_fl_content_bottom_dialog);
        mIvConfrim.setOnClickListener(this);
    }

    public void setTitle(CharSequence c){
        if (TextUtils.isEmpty(c)){
            return;
        }
        if (mTvTitle != null) {
            mTvTitle.setText(c);
        }else {
            mTitle = c.toString();
        }
    }

    public void setTitle(int resId){
        if (mTvTitle != null) {
            mTvTitle.setText(resId);
        }else {
            mResId = resId;
        }
    }

    public void setContentView(@NonNull View view){
        Log.i("TAG", "setContentView: " + mFlContent);
        mContentView = view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.rtc_audioliveroom_iv_confrim_bottom_dialog){
            dismiss();
            if (mOnConfrimListener != null) {
                mOnConfrimListener.onConfrim();
            }
        }
    }

    public void setOnConfrimListener(OnConfrimListener onConfrimListener) {
        mOnConfrimListener = onConfrimListener;
    }

    public interface OnConfrimListener {
        void onConfrim();
    }
}
