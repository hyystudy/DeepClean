package com.abclauncher.deepclean;

import android.support.v7.app.AppCompatActivity;


/**
 * Created by sks on 2017/1/9.
 */

public class BaseActivity extends AppCompatActivity {


    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setStatusBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.colorPrimary));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
