package com.youzan.mobile.enjoydependency;

import android.app.Application;
import com.youzan.mobile.liba.Impl.AApplication;
import com.youzan.mobile.libb.Impl.BApplication;
import com.youzan.mobile.libc.Impl.CApplication;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AApplication a = new AApplication();
        a.onCreate();
        CApplication c = new CApplication();
        c.onCreate();
        BApplication b = new BApplication();
        b.onCreate();

    }
}
