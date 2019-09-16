package com.youzan.mobile.libb.Impl;

import com.youzan.mobile.lib_common.IApplication;
import com.youzan.mobile.lib_common.Register;

public class BApplication implements IApplication {
    @Override
    public void onCreat() {
        Register.register.regis("bPlugin", new BPlugin());
    }
}
