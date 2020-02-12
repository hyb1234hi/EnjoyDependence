package com.youzan.mobile.libc.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.youzan.mobile.libc.R
import com.youzan.mobile.libc.manager.FunctionManager
import kotlinx.android.synthetic.main.fragment1_layout.view.*

class Fragment1 : Fragment() {

    companion object {
        val fragment1: Fragment1 = Fragment1()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment1_layout, null)
        view.fragment1_send.setOnClickListener {
            callF2Method()
        }
        return view
    }

    private fun callF2Method() {
        FunctionManager.invokeFunc(Fragment2.F2SAY)
        FunctionManager.invokeFuncWithParam<String>(Fragment2.F2SAYWITHPARAM, "ni hao")
        FunctionManager.invokeFuncWithParamAndResult<String, String>(Fragment2.F2SAYWITHRESULTANDPARAM, "NI HAO", String::class.java)
    }
}