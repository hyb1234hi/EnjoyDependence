package com.youzan.mobile.libc.manager

import com.youzan.mobile.libc.function.*

/**
 * function管理类
 */
object FunctionManager {
    private val mFunctionWithParam: MutableMap<String, FunctionWithParam<*>> by lazy {
        HashMap<String, FunctionWithParam<*>>()
    }

    private val mFunctionWithResult: MutableMap<String, FunctionWithResult<*>> by lazy {
        HashMap<String, FunctionWithResult<*>>()
    }

    private val mFunctionNoParamAndResult: MutableMap<String, FunctionNoParamAndResult> by lazy {
        HashMap<String, FunctionNoParamAndResult>()
    }

    private val mFunctionWithParamAndResult: MutableMap<String, FunctionWithParamAndResult<*, *>> by lazy {
        HashMap<String, FunctionWithParamAndResult<*, *>>()
    }

    public fun <Param> addFunction(function: FunctionWithParam<Param>?): FunctionManager {
        function ?: return this
        mFunctionWithParam[function.mFunctionName] = function
        return this
    }

    public fun <Result> addFunction(function: FunctionWithResult<Result>?): FunctionManager {
        function ?: return this
        mFunctionWithResult[function.mFunctionName] = function
        return this
    }

    public fun <Result, Param> addFunction(function: FunctionWithParamAndResult<Result, Param>?): FunctionManager {
        function ?: return this
        mFunctionWithParamAndResult[function.mFunctionName] = function
        return this
    }

    public fun addFunction(function: FunctionNoParamAndResult?): FunctionManager {
        function ?: return this
        mFunctionNoParamAndResult[function.mFunctionName] = function
        return this
    }

    @Throws(FunctionException::class)
    public fun invokeFunc(funName: String) {
        mFunctionNoParamAndResult[funName]?.function()
                ?: throw FunctionException("has no this function")
    }

    @Throws(FunctionException::class)
    public fun <Result> invokeFuncWithResult(funName: String, type: Class<Result>): Result? {
        mFunctionWithResult[funName]?.let { return type.cast(it.function()) }
                ?: throw FunctionException("has no this function")
    }

    @Throws(FunctionException::class)
    public fun <Result, Param> invokeFuncWithParamAndResult(funName: String, param: Param, type: Class<Result>): Result? {
        mFunctionWithParamAndResult[funName]?.let { return type.cast((it as FunctionWithParamAndResult<Result, Param>).function(param)) }
                ?: throw FunctionException("has no this function")
    }

    public fun <Param> invokeFuncWithParam(funName: String, param: Param) {
        (mFunctionWithParam[funName] as? FunctionWithParam<Param>)?.function(param)
    }
}