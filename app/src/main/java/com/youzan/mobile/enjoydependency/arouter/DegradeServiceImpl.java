package com.youzan.mobile.enjoydependency.arouter;

import android.content.Context;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.service.DegradeService;
import com.alibaba.android.arouter.launcher.ARouter;

/**
 * 全局降级
 */
@Route(path = "/app/degrade")
public class DegradeServiceImpl implements DegradeService {

    @Override
    public void onLost(Context context, Postcard postcard) {
        if ("/liba/degrade/activity2".equals(postcard.getPath())) {
            ARouter.getInstance()
                    .build("/libc/activity2")
                    .navigation();
        }
    }

    @Override
    public void init(Context context) {

    }
}
