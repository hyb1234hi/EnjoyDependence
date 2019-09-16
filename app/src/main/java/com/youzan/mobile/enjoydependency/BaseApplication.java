package com.youzan.mobile.enjoydependency;

import android.app.Application;

import com.youzan.mobile.liba.Impl.AApplication;
import com.youzan.mobile.libb.Impl.BApplication;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AApplication a = new AApplication();
        a.onCreat();
        BApplication b = new BApplication();
        b.onCreat();
    }
}
