package com.youzan.mobile.enjoydependency;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.youzan.mobile.enjoydependency.voice.VoiceBuilder;
import com.youzan.mobile.enjoydependency.voice.VoicePlay;
import com.youzan.mobile.enjoydependency.voice.VoiceTextTemplate;

public class VoiceDemoActivity extends AppCompatActivity {

    private boolean mCheckNum;

    private EditText editText;
    private Button btPlay;
    private Button btDel;
    private LinearLayout llMoneyList;
    private Switch switchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_demo_layout);

        initView();
        initClick();
    }

    void initView() {
        editText = findViewById(R.id.edittext);
        btPlay = findViewById(R.id.bt_play);
        btDel = findViewById(R.id.bt_del);
        llMoneyList = findViewById(R.id.ll_money_list);
        switchView = findViewById(R.id.switch_view);
    }

    void initClick() {
        btPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount = editText.getText().toString().trim();
                if (TextUtils.isEmpty(amount)) {
                    Toast.makeText(VoiceDemoActivity.this, "请输入金额", Toast.LENGTH_SHORT).show();
                    return;
                }

                VoicePlay.with(VoiceDemoActivity.this).play(amount, mCheckNum);

                llMoneyList.addView(getTextView(amount), 0);
                editText.setText("");
            }
        });

        btDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llMoneyList.removeAllViews();
            }
        });

        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCheckNum = isChecked;
            }
        });
    }

    TextView getTextView(String amount) {
        VoiceBuilder voiceBuilder = new VoiceBuilder.Builder()
                .start("success")
                .money(amount)
                .unit("yuan")
                .checkNum(mCheckNum)
                .builder();

        StringBuffer text = new StringBuffer()
                .append("角标: ").append(llMoneyList.getChildCount())
                .append("\n")
                .append("输入金额: ").append(amount)
                .append("\n");
        if (mCheckNum) {
            text.append("全数字式: ").append(VoiceTextTemplate.genVoiceList(voiceBuilder).toString());
        } else {
            text.append("中文样式: ").append(VoiceTextTemplate.genVoiceList(voiceBuilder).toString());
        }

        TextView view = new TextView(VoiceDemoActivity.this);
        view.setPadding(0, 8, 0, 0);
        view.setText(text.toString());
        return view;
    }
}
