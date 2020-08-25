package com.aliyun.rtc.audiochatroom.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alivc.rtc.AliRtcEngine;
import com.aliyun.rtc.alivcrtcviewcommon.view.ChannelEditView;
import com.aliyun.rtc.alivcrtcviewcommon.widget.RTCLoadingButton;
import com.aliyun.rtc.audiochatroom.R;
import com.aliyun.rtc.audiochatroom.api.BaseRTCAudioLiveRoomApi;
import com.aliyun.rtc.audiochatroom.api.impl.BaseRTCAudioLiveRoomApiImpl;
import com.aliyun.rtc.audiochatroom.api.BaseRTCAuthInfoLoader;
import com.aliyun.rtc.audiochatroom.api.impl.AudioLiveRoomModelFactoryImpl;
import com.aliyun.rtc.audiochatroom.bean.AudioRoomUserInfoResponse;
import com.aliyun.rtc.audiochatroom.api.net.OkhttpClient;
import com.aliyun.rtc.audiochatroom.bean.ChannelUserNumResponse;
import com.aliyun.rtc.audiochatroom.constant.Constant;
import com.aliyun.rtc.audiochatroom.rtc.BaseRTCAudioLiveRoom;
import com.aliyun.rtc.audiochatroom.rtc.RTCAudioLiveRoomDelegate;
import com.aliyun.rtc.audiochatroom.rtc.SimpleRTCAudioLiveRoomDelegate;
import com.aliyun.rtc.audiochatroom.utils.DoubleClickUtil;
import com.aliyun.rtc.audiochatroom.utils.NetUtils;
import com.aliyun.rtc.audiochatroom.utils.PermissionUtil;
import com.aliyun.rtc.audiochatroom.utils.ToastUtils;
import com.aliyun.rtc.audiochatroom.utils.UIHandlerUtil;

import java.util.List;
import java.util.regex.Pattern;

public class RtcJoinChannelActivity extends AppCompatActivity implements View.OnClickListener, PermissionUtil.PermissionGrantedListener {

    private RelativeLayout mRlInteractiveRole;
    private RelativeLayout mRlLiveRole;
    private RTCLoadingButton mTvJoinChannel;
    //默认为互动角色
    private AliRtcEngine.AliRTCSDK_Client_Role mClientRole = AliRtcEngine.AliRTCSDK_Client_Role.AliRTCSDK_Interactive;
    private ChannelEditView mEtChannelId;
    private EditText mEtUserName;
    private String mUserId;
    private AudioRoomUserInfoResponse.RtcAuthInfo mRtcAuthInfo;
    private String[] permissions = new String[]{
            PermissionUtil.PERMISSION_WRITE_EXTERNAL_STORAGE,
            PermissionUtil.PERMISSION_RECORD_AUDIO,
            PermissionUtil.PERMISSION_READ_EXTERNAL_STORAGE
    };
    private boolean joinChannel;
    private BaseRTCAuthInfoLoader model = new AudioLiveRoomModelFactoryImpl().createRTCAuthInfoLoader();
    private BaseRTCAudioLiveRoomApi mRTCAudioLiveRoomApi = new BaseRTCAudioLiveRoomApiImpl();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alirtc_audio_live_room_activity_join_channel);
        initView();
        //请求权限
        PermissionUtil.requestPermissions(RtcJoinChannelActivity.this, permissions, PermissionUtil.PERMISSION_REQUEST_CODE, RtcJoinChannelActivity.this);
        getUserInfoByNet();
    }

    @Override
    protected void onStart() {
        super.onStart();
        BaseRTCAudioLiveRoom.sharedInstance().setRTCAudioLiveRoomDelegate(mCallBack);
    }

    private void initView() {
        mRlInteractiveRole = findViewById(R.id.alirtc_audioliveroom_rl_interactive_role);
        mRlLiveRole = findViewById(R.id.alirtc_audioliveroom_rl_live_role);
        mTvJoinChannel = findViewById(R.id.alirtc_audioliveroom_tv_join_channel);
        mEtUserName = findViewById(R.id.alirtc_audioliveroom_et_username);
        mEtChannelId = findViewById(R.id.alirtc_audioliveroom_et_channel_id);
        ImageView ivBack = findViewById(R.id.alirtc_audioliveroom_iv_back);
        FrameLayout flContent = findViewById(R.id.rtc_audioliveroom_fl_content);
        mEtChannelId.setFocusable(false);
        mEtChannelId.setFocusableInTouchMode(false);

        reflushClientRoleState();
        mRlInteractiveRole.setOnClickListener(this);
        mRlLiveRole.setOnClickListener(this);
        mTvJoinChannel.setOnClickListener(this);
        flContent.setOnClickListener(this);
        mEtChannelId.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        mEtChannelId.addTextChangedListener(new ChannelIdTextWatcher());
        mEtUserName.addTextChangedListener(new UserNameTextWatcher());
    }

    private void reflushClientRoleState() {
        mRlInteractiveRole.setSelected(mClientRole == AliRtcEngine.AliRTCSDK_Client_Role.AliRTCSDK_Interactive);
        mRlLiveRole.setSelected(mClientRole != AliRtcEngine.AliRTCSDK_Client_Role.AliRTCSDK_Interactive);
    }

    @Override
    public void onClick(View v) {
        if (v == null) {
            return;
        }
        int id = v.getId();
        if (id == R.id.alirtc_audioliveroom_rl_interactive_role) {
            mClientRole = AliRtcEngine.AliRTCSDK_Client_Role.AliRTCSDK_Interactive;
            reflushClientRoleState();
        } else if (id == R.id.alirtc_audioliveroom_rl_live_role) {
            mClientRole = AliRtcEngine.AliRTCSDK_Client_Role.AliRTCSDK_live;
            reflushClientRoleState();
        } else if (id == R.id.alirtc_audioliveroom_et_channel_id) {
            mEtChannelId.setText("");
            mEtChannelId.setFocusable(true);
            mEtChannelId.setFocusableInTouchMode(true);
            mEtChannelId.requestFocus();
            mEtChannelId.requestFocusFromTouch();
            showSoftInput(mEtChannelId);
        } else if (id == R.id.alirtc_audioliveroom_tv_join_channel) {
            if (DoubleClickUtil.isDoubleClick(v, 500)) {
                showToastInCenter(getString(R.string.alirtc_audioliveroom_string_hint_double_click));
                return;
            }

            if (TextUtils.isEmpty(getUserName())) {
                showToastInCenter(getString(R.string.alirtc_audioliveroom_string_error_student_name_empty));
                return;
            }

            if (!NetUtils.isNetworkConnected(RtcJoinChannelActivity.this)) {
                showToastInCenter(getString(R.string.alirtc_audioliveroom_string_network_conn_error));
                return;
            }
            joinChannel = true;
            showLoadingView();
            if (mClientRole == AliRtcEngine.AliRTCSDK_Client_Role.AliRTCSDK_Interactive) {
                checkUserNumByChannel();
            } else {
                login();
            }
        } else if (id == R.id.rtc_audioliveroom_fl_content) {
            hideSoftInput();
            mEtChannelId.clearFocus();
            mEtChannelId.setFocusable(false);
            mEtChannelId.setFocusableInTouchMode(false);
        } else if (id == R.id.alirtc_audioliveroom_iv_back) {
            finish();
        }
    }

    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    private void showSoftInput(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editText, 0);
        }
    }


    /**
     * 获取rtc服务器的token信息
     */
    private void getUserInfoByNet() {
        model.loadRTCAuthInfo(joinChannel ? getChannelId() : "", new OkhttpClient.BaseHttpCallBack<AudioRoomUserInfoResponse>() {
            @Override
            public void onSuccess(AudioRoomUserInfoResponse data) {
                if (data == null) {
                    hideLoadingView();
                    return;
                }
                mRtcAuthInfo = data.getData();
                UIHandlerUtil.getInstance().postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        bindUserInfo();
                    }
                });
            }

            @Override
            public void onError(String errorMsg) {
                hideLoadingView();
                showToastInCenter(getString(R.string.alirtc_audioliveroom_string_hint_join_room_faild));
            }
        });
    }

    private void login() {
        BaseRTCAudioLiveRoom.sharedInstance().login(getChannelId(), getUserName(), mClientRole);
    }

    /**
     * 开始通话按钮显示loading画面
     */
    private void showLoadingView() {
        mTvJoinChannel.showLoading();
        mTvJoinChannel.setText(R.string.alirtc_audioliveroom_string_loading_join_channel);
    }

    private void hideLoadingView() {
        UIHandlerUtil.getInstance().postRunnable(new Runnable() {
            @Override
            public void run() {
                mTvJoinChannel.hideLoading();
                mTvJoinChannel.setText(R.string.alirtc_audioliveroom_string_btn_join_channel);
            }
        });
    }

    /**
     * 权限申请成功
     */
    @Override
    public void onPermissionGranted() {

    }

    /**
     * 权限申请失败
     */
    @Override
    public void onPermissionCancel() {
        showToastInCenter(getString(R.string.alirtc_permission));
    }

    /**
     * 把获取到的数据绑定到页面上
     */
    private void bindUserInfo() {
        if (mRtcAuthInfo != null) {
            mEtChannelId.setText(mRtcAuthInfo.getChannelId());
            mEtUserName.setText(mRtcAuthInfo.getUserName());
        }
    }

    private void showToastInCenter(final String msg) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            ToastUtils.showInCenter(RtcJoinChannelActivity.this, msg);
        } else {
            UIHandlerUtil.getInstance().postRunnable(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showInCenter(RtcJoinChannelActivity.this, msg);
                }
            });
        }
    }

    /**
     * 房间号输入框的文字监听器
     */
    private class ChannelIdTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (TextUtils.isEmpty(getChannelId()) || getChannelId().length() < 5 || TextUtils.isEmpty(getUserName())) {
                if (mTvJoinChannel.isEnabled()) {
                    mTvJoinChannel.setEnabled(false);
                }
            } else {
                //改变开始通话按钮状态
                if (!mTvJoinChannel.isEnabled()) {
                    mTvJoinChannel.setEnabled(true);
                }
            }

            if (!TextUtils.isEmpty(s) && s.length() == 5) {
                hideSoftInput();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }

    }

    /**
     * 昵称输入框的文字监听器
     */
    private class UserNameTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String changedStr = s.toString().substring(start, start + count);
            String regex = "[`~!@#$%^&*()_\\-+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
            Pattern pattern = Pattern.compile(regex);
            if (pattern.matcher(changedStr).find()) {
                showToastInCenter("不能输入特殊字符");
                mEtUserName.setText(s.toString().substring(0, start));
                mEtUserName.setSelection(start);
            }
            if (TextUtils.isEmpty(getChannelId()) || getChannelId().length() < 5 || TextUtils.isEmpty(getUserName())) {
                if (mTvJoinChannel.isEnabled()) {
                    mTvJoinChannel.setEnabled(false);
                }
            } else {
                //改变开始通话按钮状态
                if (!mTvJoinChannel.isEnabled()) {
                    mTvJoinChannel.setEnabled(true);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    /**
     * 获取房间号
     *
     * @return 房间号
     */
    private String getChannelId() {
        return mEtChannelId == null ? "" : mEtChannelId.getText().toString().trim();
    }

    private String getUserName() {
        return mEtUserName == null ? "" : mEtUserName.getText().toString().trim();
    }

    private void toChatActivity() {
        RtcChatActivity.start(this, getChannelId(), mUserId, mClientRole == AliRtcEngine.AliRTCSDK_Client_Role.AliRTCSDK_Interactive);
    }

    private RTCAudioLiveRoomDelegate mCallBack = new SimpleRTCAudioLiveRoomDelegate() {
        @Override
        public void onJoinChannelResult(int result, String uid) {
            super.onJoinChannelResult(result, uid);
            if (result == 0) {
                mUserId = uid;
                joinChannelSuccess(mUserId);
                toChatActivity();
            } else {
                String errorMsg = String.format(getString(R.string.alirtc_audioliveroom_string_join_channel_error), "error code : " + result);
                showToastInCenter(errorMsg);
            }
            hideLoadingView();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BaseRTCAudioLiveRoom.sharedInstance().destorySharedInstance();
    }

    private void checkUserNumByChannel() {
        mRTCAudioLiveRoomApi.describeChannelUsers(getChannelId(), new OkhttpClient.BaseHttpCallBack<ChannelUserNumResponse>() {
            @Override
            public void onSuccess(ChannelUserNumResponse data) {
                if (data == null || data.getData() == null) {
                    return;
                }
                List<String> interactiveUserList = data.getData().getInteractiveUserList();
                if (interactiveUserList.size() < Constant.MAX_SEAT_COUNT) {
                    login();
                } else {
                    hideLoadingView();
                    showToastInCenter(getString(R.string.alirtc_audioliveroom_string_channel_user_num_empty));
                }
            }

            @Override
            public void onError(String errorMsg) {
                hideLoadingView();
                showToastInCenter(getString(R.string.alirtc_audioliveroom_string_hint_join_room_faild));
            }
        });
    }

    private void joinChannelSuccess(String uid) {
        mRTCAudioLiveRoomApi.joinChannelSuccess(getChannelId(), uid, getUserName(), null);
    }
}
