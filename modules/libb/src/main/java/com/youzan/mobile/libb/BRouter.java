package com.youzan.mobile.libb;

import android.util.Log;

import com.youzan.mobile.lib_common.LibAPlugin;
import com.youzan.mobile.lib_common.Register;
import com.youzan.mobile.liba.export.ExportAPlugin;

public class BRouter {
    public static void gotoB() {
        Log.d("hello", "去b我是源码依赖");
        LibAPlugin aPlugin = (LibAPlugin) Register.register.getPlugin("aPlugin");
        aPlugin.say();
        ExportAPlugin aExxportPlugin = (ExportAPlugin) Register.register.getPlugin("implAPlugin");
        aExxportPlugin.saySomething();
        System.out.println("hello:" + aExxportPlugin.doSomething().name);
    }
}
