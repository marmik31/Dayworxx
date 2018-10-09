package com.project.dayworx.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.project.dayworx.R;
import com.project.dayworx.database.DBAdapter;
import com.project.dayworx.database.IDatabase;
import com.project.dayworx.network.DataRequest;
import com.project.dayworx.network.IWebService;
import com.project.dayworx.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ubuntu on 26/3/18.
 */

public class ChangePasswordActivity extends AppCompatActivity {

    private static final String TAG = ChangePasswordActivity.class.getSimpleName();

    @BindView(R.id.rlvGlobalProgressbar)
    RelativeLayout rlvGlobalProgressbar;

    @BindView(R.id.edTxtOldPassword)
    EditText edTxtOldPassword;

    @BindView(R.id.edTxtNewPassword)
    EditText edTxtNewPassword;

    @BindView(R.id.edTxtConfirmPassword)
    EditText edTxtConfirmPassword;

    @BindView(R.id.btnSave)
    Button btnSave;

    @BindView(R.id.btnBack)
    ImageView btnBack;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepassword);
        ButterKnife.bind(this);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
        finish();
    }

    private boolean validPassword() {
        if (Utils.isNullOrEmpty(edTxtOldPassword.getText().toString())) {
            edTxtOldPassword.setError(getString(R.string.error_required));
            edTxtOldPassword.requestFocus();
            return false;
        } else if (Utils.isNullOrEmpty(edTxtNewPassword.getText().toString())) {
            edTxtNewPassword.setError(getString(R.string.error_required));
            edTxtNewPassword.requestFocus();
            return false;
        } else if (Utils.isNullOrEmpty(edTxtConfirmPassword.getText().toString())) {
            edTxtConfirmPassword.setError(getString(R.string.error_required));
            edTxtConfirmPassword.requestFocus();
            return false;
        }
        return true;
    }


    private boolean validData() {
        if (!edTxtNewPassword.getText().toString().trim().equalsIgnoreCase(edTxtConfirmPassword.getText().toString())) {
            Utils.showAlert(this, "", "Confirm Password should be same as New Password");
            return false;
        }
        return true;
    }

    @OnClick(R.id.btnSave)
    public void submitData() {
        if (!validPassword()) {
            return;
        } else if (!validData()) {
            return;
        } else {
//            call for change password service
            requestForChangePassword();
        }

    }

    private void requestForChangePassword() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(IWebService.KEY_REQ_ACTION, IWebService.KEY_ACTION_CHANGE_PASSWORD);
            jsonObject.put(IWebService.KEY_RES_USER_ID, DBAdapter.getMapKeyValueString(ChangePasswordActivity.this, IWebService.KEY_USER_ID));
            jsonObject.put(IWebService.KEY_REQ_OLD_PASSWORD, edTxtOldPassword.getText().toString().trim());
            jsonObject.put(IWebService.KEY_REQ_NEW_PASSWORD, edTxtNewPassword.getText().toString().trim());


            DataRequest changePassDataRequest = new DataRequest(ChangePasswordActivity.this);
            changePassDataRequest.execute(IWebService.MAIN_URL, jsonObject.toString(), new DataRequest.CallBack() {
                @Override
                public void onPreExecute() {

                    rlvGlobalProgressbar.setVisibility(View.VISIBLE);

                }

                @Override
                public void onPostExecute(String response) {

                    rlvGlobalProgressbar.setVisibility(View.GONE);
                    if (!DataRequest.hasError(ChangePasswordActivity.this, response, true)) {
                        Log.d(TAG, "response ==> " + response);

                        try {


                            JSONObject responseObj = new JSONObject(response);
                            Toast.makeText(ChangePasswordActivity.this, responseObj.getString(IWebService.KEY_RES_MESSAGE), Toast.LENGTH_SHORT).show();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
