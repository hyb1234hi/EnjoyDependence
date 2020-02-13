package com.youzan.mobile.liba.export;

import com.youzan.mobile.lib_common.baseInterface.IPlugin;
import com.youzan.mobile.lib_common.annotation.Export;

/**
 * 暴露给其他模块的接口
 */
@Export
public abstract class ExportAPlugin implements IPlugin {
    public abstract void saySomething();

    public abstract AModel doSomething();
}
