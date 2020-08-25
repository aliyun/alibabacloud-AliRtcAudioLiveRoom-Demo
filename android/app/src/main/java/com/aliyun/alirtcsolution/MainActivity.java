package com.aliyun.alirtcsolution;


import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.rtc.audiochatroom.ui.RtcJoinChannelActivity;
import com.aliyun.rtc.interactiveclassplayer.ui.AlivcJoinChannelActivity;
import com.aliyun.rtc.voicecall.ui.AliyunRtcLoginActivity;
import com.aliyun.svideo.common.utils.PermissionUtils;
import com.aliyun.svideo.common.utils.upgrade.AutoUpgradeClient;


public class MainActivity extends AppCompatActivity {

    private RecyclerView mRcyModelList;
    private int[] mModelIconList = new int[]{
            R.drawable.icon_voice_call_solo, R.drawable.icon_interactive_class,
            R.drawable.icon_audio_live_room
    };

    private int[] mModelNameList = new int[]{
            R.string.string_voice_call_solo_name, R.string.string_interactive_class_name,
            R.string.string_audio_live_room_name
    };

    private String[] mPermissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static final int PERMISSION_REQUEST_CODE = 666;
    private Class[] mModelMainActivitys = new Class[]{
            AliyunRtcLoginActivity.class,
            AlivcJoinChannelActivity.class,
            RtcJoinChannelActivity.class
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        boolean b = PermissionUtils.checkPermissionsGroup(MainActivity.this, mPermissions);
        if (!b){
            PermissionUtils.requestPermissions(MainActivity.this, mPermissions, PERMISSION_REQUEST_CODE);
        }else{
            checkUpdate();
        }
    }

    private void initView() {
        mRcyModelList = findViewById(R.id.rcy_model_list);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        mRcyModelList.setLayoutManager(gridLayoutManager);
        mRcyModelList.setAdapter(new ModelAdapter());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE){
            checkUpdate();
        }
    }

    private void checkUpdate() {
        AutoUpgradeClient.checkUpgrade(MainActivity.this, "/versionProduct/installPackage/RTC_Solution/RTCAudioLiveRoom.json", BuildConfig.VERSION_CODE);
    }

    private class ModelAdapter extends RecyclerView.Adapter {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_model, parent, false);
            return new ModelViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((ModelViewHolder) holder).bindView(position);
        }

        @Override
        public int getItemCount() {
            return mModelIconList.length;
        }

        private class ModelViewHolder extends RecyclerView.ViewHolder {
            ImageView ivModelIcon;
            TextView tvModelName;

            public ModelViewHolder(View view) {
                super(view);
                ivModelIcon = view.findViewById(R.id.iv_model_icon);
                tvModelName = view.findViewById(R.id.tv_model_name);
            }

            public void bindView(final int position) {
                ivModelIcon.setImageResource(mModelIconList[position]);
                tvModelName.setText(getString(mModelNameList[position]));
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, mModelMainActivitys[position]);
                        startActivity(intent);
                    }
                });
            }
        }
    }
}
