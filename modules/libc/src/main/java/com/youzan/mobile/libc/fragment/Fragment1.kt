package com.youzan.mobile.libc.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.youzan.mobile.libc.R
import com.youzan.mobile.libc.function.FunctionParam
import com.youzan.mobile.libc.function.FunctionWithParamAndResult
import com.youzan.mobile.libc.function.FunctionWithResult
import com.youzan.mobile.libc.manager.FunctionManager
import kotlinx.android.synthetic.main.fragment1_layout.view.*

class Fragment1 : Fragment() {

    companion object {
        val fragment1: Fragment1 = Fragment1()
        const val F1SAYWITHRESULT: String = "f1SayWithResult"
        const val F1SAYWITHRESULTANDPARAM: String = "f1SayWithResultAndParam"
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        FunctionManager.addFunction(object : FunctionWithResult<String>(F1SAYWITHRESULT) {
            override fun function(): String {
                return "世界因你而美丽"
            }
        })
        FunctionManager.addFunction(object : FunctionWithParamAndResult<String, FunctionParam>(F1SAYWITHRESULTANDPARAM) {
            override fun function(param: FunctionParam): String {
                val num = param.getInt()
                val again = param.getBoolean()
                val info = param.getString()
                var result = "$info,世界因你而美丽"
                if (again) {
                    result += "again"
                }
                return result
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment1_layout, null)
        view.fragment1_send_void.setOnClickListener {
            FunctionManager.invokeFunc(Fragment2.F2SAY)
        }
        view.fragment1_send_param.setOnClickListener {
            FunctionManager.invokeFuncWithParam<String>(Fragment2.F2SAYWITHPARAM, "ni hao F2")
        }
        view.fragment1_send_result.setOnClickListener {
            val result = FunctionManager.invokeFuncWithResult<String>(Fragment2.F2SAYWITHRESULT, String::class.java)
            view.fragment1_content.text = "来自F2的反馈：${result ?: ""}"
        }
        view.fragment1_send_result_param.setOnClickListener {
            val result = FunctionManager.invokeFuncWithParamAndResult<String, String>(Fragment2.F2SAYWITHRESULTANDPARAM, "NI HAO", String::class.java)
            view.fragment1_content.text = "来自F2的反馈：${result ?: ""}"
        }
        return view
    }
}