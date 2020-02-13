package com.youzan.mobile.enjoydependency;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import com.youzan.mobile.libc.fragment.Fragment1;
import com.youzan.mobile.libc.fragment.Fragment2;

public class MainActivity extends AppCompatActivity {

    FrameLayout container1;
    FrameLayout container2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.container1, Fragment1.Companion.getFragment1())
                .add(R.id.container2, Fragment2.Companion.getFragment2())
                .commit();
    }
}
