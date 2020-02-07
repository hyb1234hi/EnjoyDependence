package com.youzan.mobile.libb.Impl;

import com.youzan.mobile.lib_common.LibBPlugin;
import com.youzan.mobile.lib_common.annotation.MediatorRegister;

@MediatorRegister(pluginName = "bPlugin")
public class BPlugin extends LibBPlugin {
    @Override
    public void say() {
        System.out.println("我是libB");
    }
}
