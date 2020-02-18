package com.youzan.mobile.enjoydependency;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openFragmentMsg:
                Intent intent = new Intent(MainActivity.this, FragmentMsgHomeActivity.class);
                startActivity(intent);
                break;
            case R.id.openARouter:
                Intent arouter = new Intent(MainActivity.this, ARouterHomeActivity.class);
                startActivity(arouter);
                break;
            default:
                break;
        }
    }
}

