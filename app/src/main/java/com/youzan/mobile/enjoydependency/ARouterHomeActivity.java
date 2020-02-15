package com.youzan.mobile.enjoydependency;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;

public class ARouterHomeActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arouter_home);
        ARouter.init(getApplication());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openLog:
                ARouter.openLog();
                break;
            case R.id.openDebug:
                ARouter.openDebug();
                break;
            case R.id.init:
                // 调试模式不是必须开启，但是为了防止有用户开启了InstantRun，但是
                // 忘了开调试模式，导致无法使用Demo，如果使用了InstantRun，必须在
                // 初始化之前开启调试模式，但是上线前需要关闭，InstantRun仅用于开
                // 发阶段，线上开启调试模式有安全风险，可以使用BuildConfig.DEBUG
                // 来区分环境
                ARouter.openDebug();
                ARouter.init(getApplication());
                break;
            case R.id.normalNavigation:
                ARouter.getInstance()
                        .build("/liba/degrade/activity2")
                        .navigation();
                break;
            case R.id.kotlinNavigation:
                ARouter.getInstance()
                        .build("/libc/activity1")
                        .withString("name", "老王")
                        .withInt("age", 23)
                        .navigation();
                break;
            case R.id.normalNavigationWithParams:
                Uri testUriMix = Uri.parse("arouter://m.aliyun.com/liba/activity2");
                ARouter.getInstance().build(testUriMix)
                        .withString("key1", "value1")
                        .navigation();
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 666:
                Log.e("activityResult", data.getStringExtra("msg"));
                break;
            default:
                break;
        }
    }
}
