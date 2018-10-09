package com.project.dayworx.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    @BindView(R.id.rlvGlobalProgressbar)
    RelativeLayout rlvGlobalProgressbar;

    @BindView(R.id.edtTxtMobileNumber)
    EditText edtTxtMobileNumber;

    @BindView(R.id.edTxtPassword)
    EditText edTxtPassword;

    private Dialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        edTxtPassword.setTypeface(Typeface.SERIF);
    }

    @OnClick(R.id.btnToSignUp)
    public void switchToSignUpActivity() {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    public void switchToReportActivity() {
        Intent intent = new Intent(LoginActivity.this, ReportActivity.class);
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

    private boolean loginValidation() {
        boolean isValid = true;

        edtTxtMobileNumber.setError(null);
        edTxtPassword.setError(null);

        if (Utils.isNullOrEmpty(edtTxtMobileNumber.getText().toString())) {
            edtTxtMobileNumber.setError(getString(R.string.error_required));
            edtTxtMobileNumber.requestFocus();
            isValid = false;
        } else if (Utils.isNullOrEmpty(edTxtPassword.getText().toString())) {
            edTxtPassword.setError(getString(R.string.error_required));
            edTxtPassword.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    @OnClick(R.id.btnLogin)
    public void signUp() {

        if (loginValidation()) {

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(IWebService.KEY_REQ_ACTION, IWebService.KEY_ACTION_LOGIN);
                jsonObject.put(IWebService.KEY_REQ_MOBILE, edtTxtMobileNumber.getText().toString().trim());
                jsonObject.put(IWebService.KEY_REQ_PASSWORD, edTxtPassword.getText().toString().trim());

                DataRequest signupDataRequest = new DataRequest(LoginActivity.this);
                signupDataRequest.execute(IWebService.MAIN_URL, jsonObject.toString(), new DataRequest.CallBack() {
                    @Override
                    public void onPreExecute() {
                        rlvGlobalProgressbar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onPostExecute(String response) {

                        rlvGlobalProgressbar.setVisibility(View.GONE);
                        if (!DataRequest.hasError(LoginActivity.this, response, true)) {
                            Log.d(TAG, "response ==> " + response);

                            try {

                                JSONObject dataJObject = DataRequest.getJObjWebdata(response);
                                DBAdapter.insertUpdateMap(LoginActivity.this, IWebService.KEY_USER_ID,
                                        dataJObject.getString(IWebService.KEY_RES_USER_ID));
                                DBAdapter.setMapKeyValueBoolean(LoginActivity.this, IDatabase.IMap.IS_LOGIN, true);

                                Log.d(TAG, "user_id ==> " + DBAdapter.getMapKeyValueString(LoginActivity.this, IWebService.KEY_USER_ID));

                                DBAdapter.insertUpdateMap(LoginActivity.this, IWebService.KEY_REQ_NAME, dataJObject.getString(IWebService.KEY_REQ_NAME));
                                DBAdapter.insertUpdateMap(LoginActivity.this, IWebService.KEY_REQ_EMAIL, dataJObject.getString(IWebService.KEY_REQ_EMAIL));
                                DBAdapter.insertUpdateMap(LoginActivity.this, IWebService.KEY_REQ_MOBILE, dataJObject.getString(IWebService.KEY_REQ_MOBILE));
                                DBAdapter.insertUpdateMap(LoginActivity.this, IWebService.KEY_RES_PROFILE_PIC, dataJObject.getString(IWebService.KEY_RES_PROFILE_PIC));

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

    @OnClick(R.id.textViewForgetPassword)
    public void forgetPasswordDialog() {

        dialog = new Dialog(LoginActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_forget_passsword);

        final EditText edtTxtMobileNumber = (EditText) dialog.findViewById(R.id.edtTxtMobileNumber);
        final EditText edTxtEmail = (EditText) dialog.findViewById(R.id.edTxtEmail);

        Button btnSend = (Button) dialog.findViewById(R.id.btnSend);
        ImageView btnClose = (ImageView) dialog.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtTxtMobileNumber.getText().toString().trim().isEmpty()) {
                    edtTxtMobileNumber.setError("Field cannot be empty");
                    requestFocus(edtTxtMobileNumber);
                } else if (edTxtEmail.getText().toString().trim().isEmpty()) {
                    edTxtEmail.setError("Field cannot be empty");
                    requestFocus(edTxtEmail);
                } else {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put(IWebService.KEY_REQ_ACTION, IWebService.KEY_ACTION_FORGET_PASSWORD);
                        jsonObject.put(IWebService.KEY_REQ_MOBILE, edtTxtMobileNumber.getText().toString().trim());
                        jsonObject.put(IWebService.KEY_REQ_EMAIL, edTxtEmail.getText().toString().trim());

                        requestForgetPassword(jsonObject.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        dialog.show();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private void requestForgetPassword(String jsonParams) {

        DataRequest dataRequest = new DataRequest(LoginActivity.this);
        dataRequest.execute(IWebService.MAIN_URL, jsonParams, new DataRequest.CallBack() {
            @Override
            public void onPreExecute() {
                rlvGlobalProgressbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPostExecute(String response) {
                rlvGlobalProgressbar.setVisibility(View.GONE);
                dialog.dismiss();
                Log.d(TAG, "forgetpasswordrespns ==> " + response);

                if (!DataRequest.hasError(LoginActivity.this, response, true)) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        Utils.showAlert(LoginActivity.this, "", jsonObject.getString(IWebService.KEY_RES_MESSAGE));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }
        });

    }
}
