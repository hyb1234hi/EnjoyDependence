package com.youzan.mobile.enjoydependency;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

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
