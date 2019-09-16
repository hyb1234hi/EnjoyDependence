package com.youzan.mobile.liba.Impl;

import com.youzan.mobile.lib_common.IApplication;
import com.youzan.mobile.lib_common.Register;

public class AApplication implements IApplication {
    @Override
    public void onCreat() {
        Register.register.regis("aPlugin", new APlugin());
    }
}
