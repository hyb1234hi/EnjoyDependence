package com.youzan.mobile.liba.Impl;

import com.youzan.mobile.lib_common.LibAPlugin;
import com.youzan.mobile.lib_common.annotation.MediatorRegister;

@MediatorRegister(pluginName = "aPlugin")
public class APlugin extends LibAPlugin {

    @Override
    public void say() {
        System.out.println("我是libA");
    }

    @Override
    public void ask() {
        System.out.println("我是libA，我要问话，我是个测试");
    }
}
