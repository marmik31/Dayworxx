package com.project.dayworx.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.project.dayworx.R;
import com.project.dayworx.database.DBAdapter;
import com.project.dayworx.database.IDatabase;
import com.project.dayworx.network.IWebService;

/**
 * Created by ubuntu on 1/9/17.
 */

public class SpalshActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switchActivity();

            }
        }, 3000);
    }

    private void switchActivity() {

        if (DBAdapter.getMapKeyValueBoolean(SpalshActivity.this, IDatabase.IMap.IS_LOGIN)) {

            Intent intent = new Intent(SpalshActivity.this, ReportActivity.class);
            intent.putExtra(IWebService.KEY_CONSTANT_DRAFT, false);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(SpalshActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
