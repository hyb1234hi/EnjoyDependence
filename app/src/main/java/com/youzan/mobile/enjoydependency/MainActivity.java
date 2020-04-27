package com.youzan.mobile.enjoydependency;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.youzan.mobile.liba.ARouter;
import com.youzan.mobile.libb.BRouter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ARouter.gotoA();
        BRouter.gotoB();
    }
}
