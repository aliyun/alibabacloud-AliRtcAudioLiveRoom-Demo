package com.aliyun.rtc.interactiveclassplayer.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.alivc.rtc.AliRtcEngine;
import com.aliyun.rtc.interactiveclassplayer.R;
import com.aliyun.rtc.interactiveclassplayer.bean.AlivcVideoStreamInfo;
import com.aliyun.rtc.interactiveclassplayer.rtc.RtcManager;
import com.serenegiant.usb.IFrameCallback;

import org.webrtc.sdk.SophonSurfaceView;

import java.util.List;

public class StudentListAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<AlivcVideoStreamInfo> mAlivcVideoStreamInfos;
    private ItemClickListener mItemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public StudentListAdapter(Context context, List<AlivcVideoStreamInfo> alivcVideoStreamInfos) {
        mContext = context;
        mAlivcVideoStreamInfos = alivcVideoStreamInfos;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.alivc_big_interactive_item_student_list, parent, false);
        return new StudentListHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((StudentListHolder) holder).bindView(position);
    }

    @Override
    public int getItemCount() {
        return mAlivcVideoStreamInfos == null ? 0 : mAlivcVideoStreamInfos.size();
    }

    /**
     * 移除item的时候先把soponsurfaceview从父view上移除，防止切换大小屏的时候提示view的添加异常
     * @param info 流信息
     */
    public void detachedPreview(AlivcVideoStreamInfo info) {
        if (info != null && info.isLocalStream()) {
            SophonSurfaceView view = info.getAliVideoCanvas().view;
            if (view != null) {
                if (view.getParent() != null) {
                    ((ViewGroup) view.getParent()).removeAllViews();
                }
            }
        }
    }

    private class StudentListHolder extends RecyclerView.ViewHolder {

        private final FrameLayout mRlContainer;
        private final TextView mTvStudentName;
        private final ImageView mIvMuteLocalMic;

        private StudentListHolder(View view) {
            super(view);
            mRlContainer = view.findViewById(R.id.alivc_big_interactive_class_rl_student_preview_container);
            mTvStudentName = view.findViewById(R.id.alivc_big_interactive_class_tv_student_name);
            mIvMuteLocalMic = view.findViewById(R.id.alivc_big_interactive_class_iv_student_mutelocalmic);
        }

        private void bindView(final int position) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRlContainer.removeAllViews();
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClicked(position);
                    }
                }
            });
            //复用，防止重复添加
            AlivcVideoStreamInfo alivcVideoStreamInfo = null;
            if (position < mAlivcVideoStreamInfos.size()) {
                alivcVideoStreamInfo = mAlivcVideoStreamInfos.get(position);
            }
            if (alivcVideoStreamInfo == null) {
                return;
            }
            SophonSurfaceView sophonSurfaceView = alivcVideoStreamInfo.getAliVideoCanvas().view;
            if (sophonSurfaceView == null) {
                sophonSurfaceView = new SophonSurfaceView(itemView.getContext());
                sophonSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
                // true 在最顶层，会遮挡一切view
                sophonSurfaceView.setZOrderOnTop(false);
                //true 如已绘制SurfaceView则在surfaceView上一层绘制。
                sophonSurfaceView.setZOrderMediaOverlay(true);
                alivcVideoStreamInfo.getAliVideoCanvas().view = sophonSurfaceView;
                //设置渲染模式,一共有四种
                alivcVideoStreamInfo.getAliVideoCanvas().renderMode = AliRtcEngine.AliRtcRenderMode.AliRtcRenderModeFill;
                mRlContainer.removeAllViews();
                mRlContainer.addView(sophonSurfaceView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                if (alivcVideoStreamInfo.isLocalStream()) {
                    // preview
                    startPreview(alivcVideoStreamInfo);
                } else {
                    displayStream(alivcVideoStreamInfo);
                }
            } else {
                //已经添加并开始预览就只切换展示的spoonsurfaceview
                if (sophonSurfaceView.getParent() != null) {
                    ((ViewGroup) sophonSurfaceView.getParent()).removeView(sophonSurfaceView);
                }
                // true 在最顶层，会遮挡一切view
                sophonSurfaceView.setZOrderOnTop(false);
                //true 如已绘制SurfaceView则在surfaceView上一层绘制。
                sophonSurfaceView.setZOrderMediaOverlay(true);
                mRlContainer.removeAllViews();
                mRlContainer.addView(sophonSurfaceView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                if (!alivcVideoStreamInfo.isLocalStream()) {
                    return;
                }
                //点击关闭摄像头需要掉rtc sdk的停止预览展示黑背景
                if (alivcVideoStreamInfo.isMuteLocalCamera()) {
                    RtcManager.getInstance().stopPreview();
                } else {
                    RtcManager.getInstance().startPreview();
                }

            }
        }
    }

    /**
     * 展示远端流
     */
    private void displayStream(AlivcVideoStreamInfo alivcVideoStreamInfo) {

        AliRtcEngine.AliVideoCanvas aliVideoCanvas = alivcVideoStreamInfo.getAliVideoCanvas();
        if (aliVideoCanvas != null) {
            AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack = alivcVideoStreamInfo.getAliRtcVideoTrack() == AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackBoth ? AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackScreen : alivcVideoStreamInfo.getAliRtcVideoTrack();
            RtcManager.getInstance().setRemoteViewConfig(aliVideoCanvas, alivcVideoStreamInfo.getUserId(), aliRtcVideoTrack);
        }
    }

    /**
     * 展示本地流
     */
    private void startPreview(AlivcVideoStreamInfo alivcVideoStreamInfo) {

        AliRtcEngine.AliVideoCanvas aliVideoCanvas = alivcVideoStreamInfo.getAliVideoCanvas();
        if (aliVideoCanvas != null) {
            RtcManager.getInstance().setLocalViewConfig(aliVideoCanvas, alivcVideoStreamInfo.getAliRtcVideoTrack());
        }
        if (!alivcVideoStreamInfo.isMuteLocalCamera()) {
            RtcManager.getInstance().startPreview();
        } else {
            RtcManager.getInstance().stopPreview();

        }
    }

    public interface ItemClickListener {
        void onItemClicked(int position);
    }
}
