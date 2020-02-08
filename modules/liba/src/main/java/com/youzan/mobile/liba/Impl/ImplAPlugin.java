package com.youzan.mobile.liba.Impl;

import com.youzan.mobile.lib_common.annotation.MediatorRegister;
import com.youzan.mobile.liba.export.ExportAPlugin;

@MediatorRegister(pluginName = "implAPlugin")
public class ImplAPlugin extends ExportAPlugin {
    @Override
    public void saySomething() {
        System.out.println("hello : APlugin success");
    }

    @Override
    public void doSomething() {
        System.out.println("hello : 真他妈过瘾");
    }
}
