package com.youzan.mobile.liba;

import android.util.Log;

import com.youzan.mobile.libb.BRouter;

public class ARouter {
    public static void gotoA(){
        Log.d("hello", "去a + 我是源码依赖");
        BRouter.gotoB();
    }
}
