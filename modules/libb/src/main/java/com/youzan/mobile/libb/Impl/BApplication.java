package com.youzan.mobile.libb.Impl;

import com.youzan.mobile.lib_common.Register;
import com.youzan.mobile.lib_common.baseInterface.IApplication;
import com.youzan.mobile.liba.export.ExportAPlugin;
import com.youzan.mobile.libc.export.ExportCPlugin;

/**
 * libb依赖了a，c
 * a和c 为b提供了服务：其中a提供了api的同时，也提供了model；c为kotlin编写，只提供了api
 * 其中因为b在初始化的时候调用了a，c的方法，所以b的初始化时机要滞后于a，c
 */
public class BApplication implements IApplication {
    @Override
    public void onCreate() {
        ExportAPlugin exportAPlugin = (ExportAPlugin) Register.register.getPlugin("aPlugin");
        exportAPlugin.saySomething();
        System.out.println("我调用a提供的方法，获取到的信息：" + exportAPlugin.doSomething().name + " " + exportAPlugin.doSomething().age);
        ExportCPlugin exportCPlugin = (ExportCPlugin) Register.register.getPlugin("cPlugin");
        exportCPlugin.say();
    }
}
