package com.aliyun.rtc.interactiveclassplayer.play;

import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.aliyun.player.AliPlayer;
import com.aliyun.player.AliPlayerFactory;
import com.aliyun.player.IPlayer;
import com.aliyun.player.bean.ErrorCode;
import com.aliyun.player.bean.ErrorInfo;
import com.aliyun.player.nativeclass.PlayerConfig;
import com.aliyun.player.nativeclass.TrackInfo;
import com.aliyun.player.source.UrlSource;
import com.aliyun.rtc.interactiveclassplayer.utils.ApplicationContextUtil;

import java.util.List;

public class PlayHelper implements IPlayer.OnCompletionListener, IPlayer.OnErrorListener, IPlayer.OnPreparedListener, IPlayer.OnVideoSizeChangedListener, IPlayer.OnRenderingStartListener, IPlayer.OnLoadingStatusListener, IPlayer.OnTrackChangedListener, IPlayer.OnStateChangedListener {

    private static final String TAG = PlayHelper.class.getSimpleName();
    private AliPlayer mAliPlayer;
    private LivePlayListener mLivePlayListener;

    private PlayHelper() {}

    private static final class PlayHelperInstance {
        private static final PlayHelper INSTANCE = new PlayHelper();
    }

    public static PlayHelper getInstance() {
        return PlayHelperInstance.INSTANCE;
    }

    public void init() {
        if (mAliPlayer == null) {
            //1.初始化
            mAliPlayer = AliPlayerFactory.createAliPlayer(ApplicationContextUtil.getAppContext());
            //先获取配置
            PlayerConfig config = mAliPlayer.getConfig();
            //设置网络超时时间，单位ms
            config.mNetworkTimeout = 30000;
            config.mMaxDelayTime = 100;
            //设置超时重试次数。每次重试间隔为networkTimeout。networkRetryCount=0则表示不重试，重试策略app决定，默认值为2
            config.mNetworkRetryCount = 1;
            //设置配置给播放器
            mAliPlayer.setConfig(config);
            //值范围0-1，此比例是按系统当前音量再乘以此比例就是当前播放的音量
            mAliPlayer.setVolume(1.0f);
            mAliPlayer.setOnCompletionListener(this);
            mAliPlayer.setOnErrorListener(this);
            mAliPlayer.setOnPreparedListener(this);
            mAliPlayer.setOnVideoSizeChangedListener(this);
            mAliPlayer.setOnRenderingStartListener(this);
            mAliPlayer.setOnLoadingStatusListener(this);
            mAliPlayer.setOnTrackChangedListener(this);
            mAliPlayer.setOnStateChangedListener(this);
        }
    }

    //播放完成事件
    @Override
    public void onCompletion() {
        Log.i(TAG, "onCompletion: ");
        if (mLivePlayListener != null) {
            mLivePlayListener.onCompletion();
        }
    }

    //出错事件
    @Override
    public void onError(ErrorInfo errorInfo) {
        Log.i(TAG, "onError: " + errorInfo.getCode());
        Log.i(TAG, "onError: " + errorInfo.getMsg());
        if (errorInfo.getCode() == ErrorCode.ERROR_NETWORK_CONNECT_TIMEOUT) {
            Log.i(TAG, "onError: 网络链接异常");
        } else if (errorInfo.getCode() == ErrorCode.ERROR_LOADING_TIMEOUT) {
            if (mLivePlayListener != null) {
                mLivePlayListener.loadingTimeOut();
            }
        }
    }

    //准备成功事件
    @Override
    public void onPrepared() {
        Log.i(TAG, "onPrepared: ");
        //在prepare成功之后，通过getMediaInfo可以获取到各个码流的信息，即TrackInfo。
//        List<TrackInfo> trackInfos = mAliPlayer.getMediaInfo().getTrackInfos();
        mAliPlayer.start();
        if (mLivePlayListener != null) {
            mLivePlayListener.onPrepared();
        }
    }

    //视频分辨率变化回调
    @Override
    public void onVideoSizeChanged(int i, int i1) {
        Log.i(TAG, "onVideoSizeChanged: i --> " + i + "; i1 --> " + i1);
        if (mLivePlayListener != null) {
            mLivePlayListener.onVideoSizeChanged(i, i1);
        }
    }

    //首帧渲染显示事件
    @Override
    public void onRenderingStart() {
        Log.i(TAG, "onRenderingStart: ");
        if (mLivePlayListener != null) {
            mLivePlayListener.onRenderingStart();
        }
    }

    //缓冲开始
    @Override
    public void onLoadingBegin() {
        Log.i(TAG, "onLoadingBegin: ");
        if (mLivePlayListener != null) {
            mLivePlayListener.onLoadingBegin();
        }
    }

    //缓冲进度
    @Override
    public void onLoadingProgress(int i, float v) {
        Log.i(TAG, "onLoadingProgress: ");
        if (mLivePlayListener != null) {
            mLivePlayListener.onLoadingProgress(i, v);
        }
    }

    //缓冲结束
    @Override
    public void onLoadingEnd() {
        Log.i(TAG, "onLoadingEnd: ");
        if (mLivePlayListener != null) {
            mLivePlayListener.onLoadingEnd();
        }
    }
    //音视频清晰度切换成功
    @Override
    public void onChangedSuccess(TrackInfo trackInfo) {
        Log.i(TAG, "onChangedSuccess: ");
        if (mLivePlayListener != null) {
            mLivePlayListener.onChangedSuccess(trackInfo);
        }
    }

    //音视频清晰度切换失败
    @Override
    public void onChangedFail(TrackInfo trackInfo, ErrorInfo errorInfo) {
        Log.i(TAG, "onChangedFail: ");
        if (mLivePlayListener != null) {
            mLivePlayListener.onChangedFail(trackInfo, errorInfo);
        }
    }

    //播放器状态改变事件
    @Override
    public void onStateChanged(int i) {
        Log.i(TAG, "onStateChanged: i --> " + i);
        if (mLivePlayListener != null) {
            mLivePlayListener.onStateChanged(i);
        }
    }

    public void setPlayUrl(String dateSource) {
        if (TextUtils.isEmpty(dateSource)) {
            return;
        }
        UrlSource urlSource = new UrlSource();
        urlSource.setUri(dateSource);
        if (mAliPlayer != null) {
            mAliPlayer.setDataSource(urlSource);
        }
    }

    public void setDisplayView(SurfaceView surfaceView) {
        if (surfaceView == null || mAliPlayer == null) {
            return;
        }
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mAliPlayer.setDisplay(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mAliPlayer.redraw();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mAliPlayer.setDisplay(null);
            }
        });
    }

    public void start() {
        if (mAliPlayer != null) {
            mAliPlayer.start();
        }
    }

    public void prepare() {
        if (mAliPlayer != null) {
            mAliPlayer.prepare();
        }
    }

    public void pause() {
        if (mAliPlayer != null) {
            mAliPlayer.pause();
        }
    }

    public void stop() {
        if (mAliPlayer != null) {
            mAliPlayer.stop();
        }
    }

    public void release() {
        if (mAliPlayer != null) {
            mAliPlayer.release();
            mAliPlayer = null;
        }
    }

    public void reset() {
        if (mAliPlayer != null) {
            mAliPlayer.reset();
        }
    }

    public boolean isMute() {
        if (mAliPlayer != null) {
            return mAliPlayer.isMute();
        }
        return false;
    }
    public void setLivePlayListener(LivePlayListener livePlayListener) {
        mLivePlayListener = livePlayListener;
    }

    public LivePlayListener getLivePlayListener() {
        return mLivePlayListener;
    }

}
