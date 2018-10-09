package com.project.dayworx.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.project.dayworx.R;
import com.project.dayworx.database.DBAdapter;
import com.project.dayworx.database.IDatabase;
import com.project.dayworx.network.IWebService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ubuntu on 1/9/17.
 */

public class MenuActivity extends AppCompatActivity {

    @BindView(R.id.imgProfile)
    ImageView imgProfile;

    @BindView(R.id.txtViewUserName)
    TextView txtViewUserName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);


    }

    @OnClick(R.id.btnClose)
    public void finishActivity() {
        onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Load basic data user profile
        initData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
        finish();
    }

    @OnClick(R.id.btnNewReport)
    public void switchToReportActivity() {
//        Intent intent = new Intent(ReportVerificationActivity.this, GalleryActivity.class);
//        startActivity(intent);
//        Toast.makeText(this, "Report Saved", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MenuActivity.this, ReportActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
        finish();
    }

    @OnClick(R.id.btnProfile)
    public void switchToProfileActivity() {
        Intent intent = new Intent(MenuActivity.this, ProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    @OnClick(R.id.dayWorxHistory)
    public void switchToReportHistoryActivity() {
        Intent intent = new Intent(MenuActivity.this, ReportHistoryActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    @OnClick(R.id.changePassword)
    public void switchToChangePasswordActivity() {
        Intent intent = new Intent(MenuActivity.this, ChangePasswordActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    @OnClick(R.id.feedback)
    public void switchToFeedBackActivity() {
        Intent intent = new Intent(MenuActivity.this, FeedbackActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    @OnClick(R.id.btnLogOut)
    public void switchToLoginActivity() {

        DBAdapter.setMapKeyValueBoolean(MenuActivity.this, IDatabase.IMap.IS_LOGIN, false);

        Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
        finish();
    }

    private void initData() {

        String userName = DBAdapter.getMapKeyValueString(MenuActivity.this, IWebService.KEY_REQ_NAME);
        txtViewUserName.setText(userName);

        String profileUrl = DBAdapter.getMapKeyValueString(MenuActivity.this, IWebService.KEY_RES_PROFILE_PIC);
        Glide.with(this).load(profileUrl).placeholder(R.drawable.placeholder).into(imgProfile);
    }


}
