package com.abclauncher.deepclean;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by sks on 2016/12/22.
 */

public class RankActivity extends BaseActivity implements RankDao.AllAppsLoadedListener {

    private static final String TAG = "RankActivity";
    @InjectView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @InjectView(R.id.content_view)
    View mContentView;
    @InjectView(R.id.loading_layout)
    View mLoadingLayout;
    @InjectView(R.id.loading_circle)
    View mLoadingCircle;

    @InjectView(R.id.checkbox)
    CheckBox mCheckBox;

    private List<AppInfo> appInfos;
    private Adapter mAdapter;
    private ProgressDialog mProgressDialog;
    private ValueAnimator mRotateAnimator;
    private int currentPosition;


    @OnClick(R.id.back)
    public void finishActivity(){
        onBackPressed();
    }
    private final int CLEAN_APP = 1;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what){
                case CLEAN_APP:
                    Log.d(TAG, "run: " + currentPosition);
                    if (currentPosition < appInfos.size()){
                        showPackageDetail(appInfos.get(currentPosition).pkgName);
                        mHandler.sendEmptyMessageDelayed(CLEAN_APP, 3000);
                        currentPosition++;
                    } else {
                        WindowUtils.hidePopupWindow();
                    }
                    break;
            }
            return false;
        }
    });


    @OnClick(R.id.clean_btn)
    public void onCleanBtnClicked(){
        if (AccessibilityUtils.isAccessibilitySettingsOn(getApplicationContext())) {
            WindowUtils.showPopupWindow(getApplicationContext());
            mHandler.sendEmptyMessageDelayed(CLEAN_APP, 50);
         /*   new Thread(new Runnable() {
                @Override
                public void run() {*/
                 /*   while (currentPosition < appInfos.size()){

                        try {
                            Thread.sleep(1000);
                            currentPosition++;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (currentPosition == appInfos.size()){

                    }*/
               // }
            //}).start();
        } else {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }

    }

    private void showPackageDetail(String packageName){
        Log.d(TAG, "showPackageDetail: ");
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
        startActivity(intent);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank_layout);
        ButterKnife.inject(this);
        // 设置右滑动返回
        RankDao rankDao = RankDao.getInstance(this);
        rankDao.setAppAppsLoadedListener(this);
        startRotateAnim();
        new Thread(new Runnable() {
            @Override
            public void run() {
                appInfos = RankDao.getInstance(getApplicationContext()).initRunningAppList(getApplicationContext(),
                        false);
            }
        }).start();


        mCheckBox.setChecked(true);



        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new Adapter();
        mRecyclerView.addItemDecoration(new RecyclerViewDecoration(getResources(),
                R.color.rank_activity_divider_color, R.dimen.rank_activity_divider_height, LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);
    }

    private void startRotateAnim() {
        mRotateAnimator = ValueAnimator.ofFloat(0f, 360f);
        mRotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                mLoadingCircle.setRotation(animatedValue);
            }
        });
        mRotateAnimator.setRepeatMode(ValueAnimator.RESTART);
        mRotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mRotateAnimator.setInterpolator(new LinearInterpolator());
        mRotateAnimator.setDuration(2000);
        mRotateAnimator.setStartDelay(50);
        mRotateAnimator.start();
    }


    @Override
    protected void setStatusBar() {
        //super.setStatusBar();
        StatusBarUtil.setTransparent(this);
    }

    @Override
    public void onAllAppsInited() {
        Log.d(TAG, "onAllAppsInited: ");

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Animator animator = AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.clean_content_dismiss_anim);
                animator.setTarget(mLoadingLayout);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (mRotateAnimator != null) {
                            mRotateAnimator.cancel();
                        }
                        mLoadingLayout.setVisibility(View.INVISIBLE);

                    }
                });
                animator.setDuration(50);
                animator.start();

                Animator appearAnim = AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.rank_content_appear_anim);
                appearAnim.setTarget(mContentView);
                appearAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        mContentView.setVisibility(View.VISIBLE);
                    }
                });
                appearAnim.setDuration(50);
                appearAnim.start();
                mAdapter.notifyDataSetChanged();

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RankDao.getInstance(getApplicationContext()).cleanData();
    }


    class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder>{

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(getApplicationContext()).inflate(R.layout.rank_activity_item, null));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final AppInfo appInfo = appInfos.get(position);
            holder.mAppName.setText(appInfo.appName);
            holder.mIcon.setImageDrawable(appInfo.icon);
        }

        @Override
        public int getItemCount() {
            return appInfos == null ? 0 : appInfos.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            @InjectView(R.id.app_name)
            public TextView mAppName;
            @InjectView(R.id.icon)
            public ImageView mIcon;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.inject(this, itemView);
            }
        }
    }

}
