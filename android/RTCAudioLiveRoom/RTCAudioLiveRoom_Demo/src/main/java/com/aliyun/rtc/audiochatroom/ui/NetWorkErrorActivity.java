package com.aliyun.rtc.audiochatroom.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.aliyun.rtc.alivcrtcviewcommon.widget.RTCNetWorkError;
import com.aliyun.rtc.audiochatroom.R;
import com.aliyun.rtc.audiochatroom.utils.NetUtils;
import com.aliyun.rtc.audiochatroom.utils.NetWatchdogUtils;
import com.aliyun.rtc.audiochatroom.utils.ToastUtils;

public class NetWorkErrorActivity extends AppCompatActivity implements NetWatchdogUtils.NetChangeListener {

    private RTCNetWorkError mNetWorkError;
    private NetWatchdogUtils mNetWatchdogUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alirtc_audio_live_room_activity__net_work_error);

        initView();
        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNetWatchdogUtils == null) {
            //添加网络监听
            mNetWatchdogUtils = new NetWatchdogUtils(this);
            mNetWatchdogUtils.setNetChangeListener(this);
            mNetWatchdogUtils.startWatch();
        }
    }

    private void initView() {
        mNetWorkError = findViewById(R.id.net_work_error);
    }

    private void initListener() {
        mNetWorkError.setNetWorkErrorListener(new RTCNetWorkError.OnNetWorkErrorListener() {
            @Override
            public void onRetry() {
                boolean networkConnected = NetUtils.isNetworkConnected(NetWorkErrorActivity.this);
                if (networkConnected) {
                    finish();
                } else {
                    ToastUtils.showInCenter(NetWorkErrorActivity.this, getString(R.string.alirtc_audioliveroom_string_network_conn_error));
                }
            }
        });
    }

    @Override
    public void onWifiTo4G() {

    }

    @Override
    public void on4GToWifi() {

    }

    @Override
    public void onReNetConnected(boolean isReconnect) {
        if (isReconnect) {
            finish();
        }
    }

    @Override
    public void onNetUnConnected() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNetWatchdogUtils != null) {
            mNetWatchdogUtils.stopWatch();
            mNetWatchdogUtils.setNetChangeListener(null);
            mNetWatchdogUtils = null;
        }
    }
}
