package com.project.dayworx.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.project.dayworx.R;
import com.project.dayworx.database.DBAdapter;
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

public class FeedbackActivity extends AppCompatActivity {

    private static final String TAG = FeedbackActivity.class.getSimpleName();

    @BindView(R.id.rlvGlobalProgressbar)
    RelativeLayout rlvGlobalProgressbar;

    @BindView(R.id.edtTxtFeedback)
    EditText edtTxtFeedback;

    @BindView(R.id.btnBack)
    ImageView btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
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

    @OnClick(R.id.btnSendFeedback)
    public void sendFeedBack() {
        if (Utils.isNullOrEmpty(edtTxtFeedback.getText().toString())) {
            edtTxtFeedback.setError(getString(R.string.error_required));
            edtTxtFeedback.requestFocus();
        } else {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(IWebService.KEY_REQ_ACTION, IWebService.KEY_ACTION_SEND_FEEDBACK);
                jsonObject.put(IWebService.KEY_RES_USER_ID, DBAdapter.getMapKeyValueString(FeedbackActivity.this, IWebService.KEY_USER_ID));
                jsonObject.put(IWebService.KEY_REQ_FEEDBACK, edtTxtFeedback.getText().toString().trim());


                DataRequest feedbackDataRequest = new DataRequest(FeedbackActivity.this);
                feedbackDataRequest.execute(IWebService.MAIN_URL, jsonObject.toString(), new DataRequest.CallBack() {
                    @Override
                    public void onPreExecute() {

                        rlvGlobalProgressbar.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onPostExecute(String response) {

                        rlvGlobalProgressbar.setVisibility(View.GONE);
                        if (!DataRequest.hasError(FeedbackActivity.this, response, true)) {
                            Log.d(TAG, "response ==> " + response);

                            try {


                                JSONObject responseObj = new JSONObject(response);
                                Toast.makeText(FeedbackActivity.this, responseObj.getString(IWebService.KEY_RES_MESSAGE), Toast.LENGTH_SHORT).show();


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
