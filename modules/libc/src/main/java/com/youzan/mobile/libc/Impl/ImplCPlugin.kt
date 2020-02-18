package com.youzan.mobile.libc.Impl

import com.youzan.mobile.lib_common.annotation.MediatorRegister
import com.youzan.mobile.libc.export.ExportCPlugin

@MediatorRegister(pluginName = "cPlugin")
class ImplCPlugin : ExportCPlugin() {
    override fun say() {
        println("我是c， 你要是调用我，是需要依赖我导出的jar包")
    }
}