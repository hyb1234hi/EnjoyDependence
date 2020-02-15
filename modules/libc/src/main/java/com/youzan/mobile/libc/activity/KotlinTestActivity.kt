package com.youzan.mobile.libc.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.youzan.mobile.libc.R
import kotlinx.android.synthetic.main.activity_kotlin_test.*

@Route(path = "/libc/activity1")
class KotlinTestActivity : AppCompatActivity() {

    @Autowired
    @JvmField var name: String? = null
    @Autowired
    @JvmField var age: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        ARouter.getInstance().inject(this)  // Start auto inject.

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin_test)

        content.text = "name = $name, age = $age"
    }
}
