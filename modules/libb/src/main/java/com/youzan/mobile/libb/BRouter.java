package com.youzan.mobile.libb;

import android.util.Log;

import com.youzan.mobile.lib_common.LibAPlugin;
import com.youzan.mobile.lib_common.Register;

public class BRouter {
    public static void gotoB(){
        Log.d("hello", "去b我是源码依赖");
        LibAPlugin aPlugin = (LibAPlugin) Register.register.getPlugin("aPlugin");
        aPlugin.say();
    }
}
