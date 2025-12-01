package com.zen.ui;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.view.View;
import android.widget.ImageView;



public class PrivacyAndServiceActivity extends Activity {

    ImageView imageBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacyservice);
        imageBack = findViewById(R.id.privacy_image);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
