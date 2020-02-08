package com.youzan.mobile.liba.export;

import com.youzan.mobile.lib_common.IPlugin;
import com.youzan.mobile.lib_common.annotation.Export;

@Export
public abstract class ExportAPlugin implements IPlugin {
    public abstract void saySomething();
}
