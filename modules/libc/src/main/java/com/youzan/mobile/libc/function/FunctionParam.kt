package com.youzan.mobile.libc.function

import android.os.Bundle

/**
 * 参数获取
 * 备注：获取时的顺序一定和定义的顺序一致
 */
public class FunctionParam(private val bundle: Bundle, private val map: MutableMap<String, Any>) {

    private var paramIndex: Int = -1
    public fun getInt(): Int {
        return bundle.getInt(paramIndex++.toString())
    }

    public fun getString(): String? {
        return bundle.getString(paramIndex++.toString())
    }

    public fun getBoolean(): Boolean {
        return bundle.getBoolean(paramIndex++.toString())
    }

    public fun <Param> getObject(type: Class<Param>): Param? {
        return map[paramIndex++.toString()]?.let { return@let type.cast(it) }
    }

    public inner class FunctionParamBuilder {
        private val bundle: Bundle by lazy {
            Bundle()
        }

        private val map: MutableMap<String, Any> by lazy {
            HashMap<String, Any>()
        }

        private var paramIndex: Int = -1

        public fun putInt(param: Int): FunctionParamBuilder {
            bundle.putInt(paramIndex++.toString(), param)
            return this
        }

        public fun putString(param: String): FunctionParamBuilder {
            bundle.putString(paramIndex++.toString(), param)
            return this
        }

        public fun putBoolean(param: Boolean): FunctionParamBuilder {
            bundle.putBoolean(paramIndex++.toString(), param)
            return this
        }

        public fun putObject(param: Any): FunctionParamBuilder {
            map.put(paramIndex++.toString(), param)
            return this
        }

        public fun creat(): FunctionParam {
            return FunctionParam(bundle, map)
        }
    }
}