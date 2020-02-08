package com.youzan.mobile.liba.Impl;

import com.youzan.mobile.lib_common.annotation.MediatorRegister;
import com.youzan.mobile.liba.export.ExportAPlugin;

@MediatorRegister(pluginName = "implAPlugin")
public class ImplAPlugin extends ExportAPlugin {
    @Override
    public void saySomething() {
        System.out.println("APlugin success");
    }
}
