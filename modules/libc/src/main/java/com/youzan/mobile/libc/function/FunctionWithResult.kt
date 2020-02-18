package com.youzan.mobile.libc.function

/**
 * 带有返回值的方法
 */
abstract class FunctionWithResult<Result>(mFunctionName: String) : Function(mFunctionName) {
    abstract fun function(): Result
}