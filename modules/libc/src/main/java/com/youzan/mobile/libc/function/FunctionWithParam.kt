package com.youzan.mobile.libc.function

/**
 * 带有参数无返回值的方法
 */
abstract class FunctionWithParam<Param>(mFunctionName: String) : Function(mFunctionName) {
    abstract fun function(param: Param)
}