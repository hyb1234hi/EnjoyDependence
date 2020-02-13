package com.youzan.mobile.libc.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.youzan.mobile.libc.R
import com.youzan.mobile.libc.function.*
import com.youzan.mobile.libc.manager.FunctionManager
import kotlinx.android.synthetic.main.fragment2_layout.view.*

class Fragment2 : Fragment() {

    private lateinit var rootView: View

    companion object {
        val fragment2: Fragment2 = Fragment2()
        val F2SAY: String = "f2Say"
        val F2SAYWITHPARAM: String = "f2SayWithParam"
        val F2SAYWITHRESULT: String = "f2SayWithResult"
        val F2SAYWITHRESULTANDPARAM: String = "f2SayWithResultAndParam"
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        FunctionManager.addFunction(object : FunctionNoParamAndResult(F2SAY) {
            override fun function() {
                Log.d("hello", "f1调用了f2的无参方法")
            }

        })
        FunctionManager.addFunction(object : FunctionWithParam<String>(F2SAYWITHPARAM) {
            override fun function(param: String) {
                Log.d("hello", "f1调用了f2的有参方法， 参数是：$param")
                rootView.fragment2_content.text = param
            }
        })
        FunctionManager.addFunction(object : FunctionWithResult<String>(F2SAYWITHRESULT) {
            override fun function(): String {
                Log.d("hello", "f1调用了f2的有返回值参数，参数是: world")
                return "world"
            }
        })
        FunctionManager.addFunction(object : FunctionWithParamAndResult<String, String>(F2SAYWITHRESULTANDPARAM) {
            override fun function(param: String): String {
                Log.d("hello", "f1调用了f2的有参方法， 参数是：$param")
                return "world again"
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment2_layout, null)
        rootView.fragment2_send_result_param.setOnClickListener {
            val param = FunctionParam.FunctionParamBuilder()
                    .putInt(1)
                    .putBoolean(true)
                    .putString("这个世界真美")
                    .creat()
            val result = FunctionManager.invokeFuncWithParamAndResult<String, FunctionParam>(Fragment1.F1SAYWITHRESULTANDPARAM, param, String::class.java)
                    ?: ""
            rootView.fragment2_content.text = "来自F1的反馈：$result"
        }
        rootView.fragment2_send_result.setOnClickListener {
            val result = FunctionManager.invokeFuncWithResult<String>(Fragment1.F1SAYWITHRESULT, String::class.java)
                    ?: ""
            rootView.fragment2_content.text = "来自F1的反馈：$result"
        }
        return rootView
    }
}