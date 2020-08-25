package com.aliyun.rtc.audiochatroom.runnable;

import android.content.Context;

import com.aliyun.rtc.audiochatroom.utils.ZipUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

public class LoadAssetsFileRunnable implements Runnable {

    private WeakReference<Context> mContextWeakReference;
    private String loadPath;
    private String outPath;
    private RunnableCallBack<List<File>> mCallBack;

    public LoadAssetsFileRunnable(Context context, String pathAssetsBgm, String pathDirBgmOut, RunnableCallBack<List<File>> callBack) {
        mContextWeakReference = new WeakReference<>(context);
        loadPath = pathAssetsBgm;
        outPath = pathDirBgmOut;
        mCallBack = callBack;
    }

    @Override
    public void run() {
        try {
            List<File> files = ZipUtil.unZipAssetsFolder(mContextWeakReference.get(), loadPath, outPath);
            if (mCallBack != null) {
                mCallBack.callBack(files);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mContextWeakReference.clear();
        }

    }
}
