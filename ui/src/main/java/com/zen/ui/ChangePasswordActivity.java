package com.zen.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import android.view.View;
import android.widget.TextView;

import com.zen.ui.base.BaseActivity;

public class ChangePasswordActivity extends BaseActivity {
    public final static String ID = "id";
    public final static String DATE = "DATE";


    private TextView tvLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        tvLeft = findViewById(R.id.tv_left);
        tvLeft.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    public static void showMe(Context content, long id) {
        if (content == null) return;
        Intent intent = new Intent(content, ChangePasswordActivity.class);
        intent.putExtra(ID, id);

        content.startActivity(intent);

    }


}
