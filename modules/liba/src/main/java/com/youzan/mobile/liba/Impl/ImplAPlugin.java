package com.youzan.mobile.liba.Impl;

import com.youzan.mobile.lib_common.annotation.MediatorRegister;
import com.youzan.mobile.liba.export.AModel;
import com.youzan.mobile.liba.export.ExportAPlugin;

@MediatorRegister(pluginName = "aPlugin")
public class ImplAPlugin extends ExportAPlugin {
    @Override
    public void saySomething() {
        System.out.println("你调用了a提供的服务，说明你成功获取到了依赖");
    }

    @Override
    public AModel doSomething() {
        return new AModel("牛X浪人", "30");
    }
}
