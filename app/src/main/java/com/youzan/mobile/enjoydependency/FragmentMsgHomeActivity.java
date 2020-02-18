package com.youzan.mobile.enjoydependency;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.youzan.mobile.libc.fragment.Fragment1;
import com.youzan.mobile.libc.fragment.Fragment2;

public class FragmentMsgHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_msg_home);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.container1, Fragment1.Companion.getFragment1())
                .add(R.id.container2, Fragment2.Companion.getFragment2())
                .commit();
    }
}
