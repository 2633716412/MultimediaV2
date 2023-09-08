package com.example.multimediav2;

import android.os.Bundle;

import com.example.multimediav2.Utils.VerifyUtil;

public class VerifyActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        verifyMethod();
    }
    private void verifyMethod() {
        VerifyUtil.showExitConfirmationDialog(this,this);
    }
    public static void openMain() {
        SkipTo(MainActivity.class);
    }
}