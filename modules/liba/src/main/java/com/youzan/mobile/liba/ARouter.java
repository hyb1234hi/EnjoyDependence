package com.youzan.mobile.liba;

import android.util.Log;

import com.youzan.mobile.lib_common.LibBPlugin;
import com.youzan.mobile.lib_common.Register;

public class ARouter {
    public static void gotoA(){
        Log.d("hello", "去a + 我是源码依赖");
        LibBPlugin bPlugin = (LibBPlugin) Register.register.getPlugin("bPlugin");
        bPlugin.say();
    }
}
