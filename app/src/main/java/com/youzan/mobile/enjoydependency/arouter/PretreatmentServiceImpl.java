package com.youzan.mobile.enjoydependency.arouter;

import android.content.Context;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.service.PretreatmentService;

/**
 * 预处理阶段
 */
@Route(path = "/app/pretreatment")
public class PretreatmentServiceImpl implements PretreatmentService {

    @Override
    public boolean onPretreatment(Context context, Postcard postcard) {
        Toast.makeText(context, "预处理阶段", Toast.LENGTH_LONG).show();
        return true;
    }

    @Override
    public void init(Context context) {

    }
}
