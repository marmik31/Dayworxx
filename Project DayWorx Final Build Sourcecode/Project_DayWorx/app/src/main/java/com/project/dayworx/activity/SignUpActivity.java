package com.project.dayworx.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.project.dayworx.R;
import com.project.dayworx.database.DBAdapter;
import com.project.dayworx.database.IDatabase;
import com.project.dayworx.network.DataRequest;
import com.project.dayworx.network.IWebService;
import com.project.dayworx.util.UniqueId;
import com.project.dayworx.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ubuntu on 1/9/17.
 */

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = SignUpActivity.class.getSimpleName();

    @BindView(R.id.rlvGlobalProgressbar)
    RelativeLayout rlvGlobalProgressbar;

    @BindView(R.id.edTxtFirstName)
    EditText edTxtFirstName;

    @BindView(R.id.edTxtLastName)
    EditText edTxtLastName;

    @BindView(R.id.edTxtMobileNumber)
    EditText edTxtMobileNumber;

    @BindView(R.id.edTxtEmail)
    EditText edTxtEmail;

    @BindView(R.id.edTxtPassword)
    EditText edTxtPassword;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        edTxtPassword.setTypeface(Typeface.SERIF);
    }

    @OnClick(R.id.btnToLogin)
    public void switchToLoginActivity() {
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
        finish();
    }

    public void switchToReportActivity() {
        Intent intent = new Intent(SignUpActivity.this, ReportActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }


    private boolean signUpValidation() {

        boolean isValid = true;

        edTxtFirstName.setError(null);
        edTxtLastName.setError(null);
        edTxtMobileNumber.setError(null);
        edTxtEmail.setError(null);
        edTxtPassword.setError(null);

        if (Utils.isNullOrEmpty(edTxtFirstName.getText().toString())) {
            edTxtFirstName.setError(getString(R.string.error_required));
            edTxtFirstName.requestFocus();
            isValid = false;
        } else if (Utils.isNullOrEmpty(edTxtLastName.getText().toString())) {
            edTxtLastName.setError(getString(R.string.error_required));
            edTxtLastName.requestFocus();
            isValid = false;
        } else if (Utils.isNullOrEmpty(edTxtMobileNumber.getText().toString())) {
            edTxtMobileNumber.setError(getString(R.string.error_required));
            edTxtMobileNumber.requestFocus();
            isValid = false;
        } else if (Utils.isNullOrEmpty(edTxtEmail.getText().toString())) {
            edTxtEmail.setError(getString(R.string.error_required));
            edTxtEmail.requestFocus();
            isValid = false;
        } else if (Utils.isNullOrEmpty(edTxtPassword.getText().toString())) {
            edTxtPassword.setError(getString(R.string.error_required));
            edTxtPassword.requestFocus();
            isValid = false;
        }
        return isValid;
    }

    @OnClick(R.id.btnSignIn)
    public void signUp() {

        if (signUpValidation()) {

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(IWebService.KEY_REQ_ACTION, IWebService.KEY_ACTION_SIGNUP);
                jsonObject.put(IWebService.KEY_REQ_DEVICE_ID, UniqueId.getUniqueId(SignUpActivity.this));
                jsonObject.put(IWebService.KEY_REQ_NAME, edTxtFirstName.getText().toString().trim() + " " + edTxtLastName.getText().toString().trim());
                jsonObject.put(IWebService.KEY_REQ_MOBILE, edTxtMobileNumber.getText().toString().trim());
                jsonObject.put(IWebService.KEY_REQ_EMAIL, edTxtEmail.getText().toString().trim());
                jsonObject.put(IWebService.KEY_REQ_PASSWORD, edTxtPassword.getText().toString().trim());

                DataRequest signupDataRequest = new DataRequest(SignUpActivity.this);
                signupDataRequest.execute(IWebService.MAIN_URL, jsonObject.toString(), new DataRequest.CallBack() {
                    @Override
                    public void onPreExecute() {

                        rlvGlobalProgressbar.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onPostExecute(String response) {

                        rlvGlobalProgressbar.setVisibility(View.GONE);
                        if (!DataRequest.hasError(SignUpActivity.this, response, true)) {
                            Log.d(TAG, "response ==> " + response);

                            try {

                                JSONObject dataJObject = DataRequest.getJObjWebdata(response);
                                DBAdapter.insertUpdateMap(SignUpActivity.this, IWebService.KEY_USER_ID,
                                        dataJObject.getString(IWebService.KEY_RES_USER_ID));
                                DBAdapter.setMapKeyValueBoolean(SignUpActivity.this, IDatabase.IMap.IS_LOGIN, true);
                                DBAdapter.insertUpdateMap(SignUpActivity.this, IWebService.KEY_REQ_NAME, dataJObject.getString(IWebService.KEY_REQ_NAME));
                                DBAdapter.insertUpdateMap(SignUpActivity.this, IWebService.KEY_REQ_EMAIL, dataJObject.getString(IWebService.KEY_REQ_EMAIL));
                                DBAdapter.insertUpdateMap(SignUpActivity.this, IWebService.KEY_REQ_MOBILE, dataJObject.getString(IWebService.KEY_REQ_MOBILE));
                                DBAdapter.insertUpdateMap(SignUpActivity.this, IWebService.KEY_RES_PROFILE_PIC, dataJObject.getString(IWebService.KEY_RES_PROFILE_PIC));

                                switchToReportActivity();

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
}
