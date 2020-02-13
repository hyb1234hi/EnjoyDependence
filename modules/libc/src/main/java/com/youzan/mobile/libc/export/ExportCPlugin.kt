package com.youzan.mobile.libc.export

import com.youzan.mobile.lib_common.annotation.Export
import com.youzan.mobile.lib_common.baseInterface.IPlugin

@Export
public abstract class ExportCPlugin : IPlugin {
    public abstract fun say()
}