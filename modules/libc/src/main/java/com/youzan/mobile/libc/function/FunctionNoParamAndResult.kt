package com.youzan.mobile.libc.function

abstract class FunctionNoParamAndResult(mFunctionName: String) : Function(mFunctionName) {
    abstract fun function()
}