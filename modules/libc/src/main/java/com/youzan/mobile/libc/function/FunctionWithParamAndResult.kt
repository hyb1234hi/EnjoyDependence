package com.youzan.mobile.libc.function

/**
 * 带参数及返回值的方法
 */
abstract class FunctionWithParamAndResult<Result, Param>(mFunctionName: String) : Function(mFunctionName) {
    abstract fun function(param: Param): Result
}