package com.project.dayworx.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.project.dayworx.R;
import com.project.dayworx.database.DBAdapter;
import com.project.dayworx.model.DayTimeModel;
import com.project.dayworx.network.DataRequest;
import com.project.dayworx.network.IWebService;
import com.project.dayworx.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.IllegalFormatWidthException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ubuntu on 1/9/17.
 */

public class ReportVerificationActivity extends AppCompatActivity {

    private static final String TAG = ReportVerificationActivity.class.getSimpleName();

    @BindView(R.id.btnBack)
    ImageView btnBack;

    @BindView(R.id.imgSelfieVerification)
    ImageView imgSelfieVerification;

    @BindView(R.id.edTxtName)
    EditText edTxtName;

    @BindView(R.id.edTxtTitle)
    EditText edTxtTitle;

    @BindView(R.id.edTxtCompany)
    EditText edTxtCompany;

    @BindView(R.id.edTxtComments)
    EditText edTxtComments;

    @BindView(R.id.txtViewDate)
    TextView txtViewDate;

    @BindView(R.id.btnConfirm)
    Button btnConfirm;

    @BindView(R.id.rlvGlobalProgressbar)
    RelativeLayout rlvGlobalProgressbar;

    private static final int RESULT_CAPTURE_IMG = 102;

    private String PROFILE_IMAGE = "";
    private static String REPORT_ID;

    private static Boolean isReportDraft = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_verification);
        ButterKnife.bind(this);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        if (getIntent() != null) {
            REPORT_ID = getIntent().getStringExtra(IWebService.KEY_RES_REPORT_ID);
            Log.d(TAG, "REPORT_ID ==> " + REPORT_ID);
            isReportDraft = getIntent().getBooleanExtra(IWebService.KEY_CONSTANT_DRAFT, false);
            if (isReportDraft) {
                loadDraftData(REPORT_ID);
//                btnNext.setText("UPDATE");
            } else {
//                btnNext.setText("CONFIRM");
            }
        }
        getCurrentDate();

    }

    //    @OnClick(R.id.btnConfirm)
    public void switchToReportActivity() {
//        Intent intent = new Intent(ReportVerificationActivity.this, GalleryActivity.class);
//        startActivity(intent);
        Toast.makeText(this, "Report Saved", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ReportVerificationActivity.this, ReportActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
        finish();
    }


    public void switchToShowPdfActivity(String url) {
        Intent intent = new Intent(ReportVerificationActivity.this, ShowPdfActivity.class);
        intent.putExtra(IWebService.KEY_RES_REPORT_ID, REPORT_ID);
        intent.putExtra(IWebService.KEY_RES_REPORT_PURCHASED_STATUS, "0");
        intent.putExtra("PDF_URL", url);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
        finish();
    }

    @OnClick(R.id.btnSetting)
    public void switchToSettingActivity() {
        Intent intent = new Intent(ReportVerificationActivity.this, MenuActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
        finish();
    }


    @OnClick(R.id.imgSelfieVerification)
    public void captureImage() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        File file = new File(android.os.Environment
                .getExternalStorageDirectory(), "temp.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));

        // start the image capture Intents
        startActivityForResult(intent, RESULT_CAPTURE_IMG);
    }

    @SuppressWarnings("null")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        try {

            if (requestCode == RESULT_CAPTURE_IMG
                    && resultCode == RESULT_OK) {
                Log.d("in if", " ==> yes");

                File file = new File(Environment.getExternalStorageDirectory()
                        .toString());
                for (File temp : file.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        file = temp;
                        Log.d("file exists", " ==> yes");
                        break;
                    }
                }

                BitmapFactory.Options options = new BitmapFactory.Options();

                // down sizing image as it throws OutOfMemory Exception for larger
                // images
                options.inSampleSize = 8;

                final Bitmap bitmap = BitmapFactory.decodeFile(file
                        .getPath().toString(), options);
                Bitmap encodeImage = null;
                ExifInterface ei = new ExifInterface(file.getPath());
                int orientation = ei.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        encodeImage = RotateBitmap(bitmap, 90);
                        imgSelfieVerification.setImageBitmap(encodeImage);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        encodeImage = RotateBitmap(bitmap, 180);
                        imgSelfieVerification.setImageBitmap(encodeImage);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        encodeImage = RotateBitmap(bitmap, 270);
                        imgSelfieVerification.setImageBitmap(encodeImage);
                        break;
                    default:
                        encodeImage = bitmap;
                        imgSelfieVerification.setImageBitmap(bitmap);
                        break;
                }
                PROFILE_IMAGE = Utils.toBASE64(encodeImage);
                Log.d(TAG, "PROFILE_IMAGE ==> " + PROFILE_IMAGE);

                File file_remove = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
                if (file_remove.exists()) {
                    file_remove.delete();
                }

            } else {
                Log.d("in else", "==> yes");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(),
                source.getHeight(), matrix, true);
    }

    @OnClick(R.id.btnConfirm)
    public void sendReportVerification() {

//        if (isValidData()) {
        String verifierName = edTxtName.getText().toString().trim();
        String title = edTxtTitle.getText().toString().trim();
        String company = edTxtCompany.getText().toString().trim();
        String date = txtViewDate.getText().toString().trim();
        String comments = edTxtComments.getText().toString().trim();

        JSONObject jsonObject = new JSONObject();
        try {
            if (isReportDraft) {
                jsonObject.put(IWebService.KEY_REQ_ACTION, IWebService.KEY_ACTION_REPORT_UPDATE);
                jsonObject.put(IWebService.KEY_RES_REPORT_ID, REPORT_ID);
            } else {
                jsonObject.put(IWebService.KEY_REQ_ACTION, IWebService.KEY_ACTION_VERIFY_REPORT);
            }
            jsonObject.put(IWebService.KEY_USER_ID, DBAdapter.getMapKeyValueString(ReportVerificationActivity.this, IWebService.KEY_RES_USER_ID));
            jsonObject.put(IWebService.KEY_REQ_PAGE_NO, "3");
            jsonObject.put(IWebService.KEY_RES_REPORT_ID, REPORT_ID); //Please replce with variable REPORT_ID
            jsonObject.put(IWebService.KEY_REQ_VERIFIER_IMAGE, PROFILE_IMAGE);
            jsonObject.put(IWebService.KEY_REQ_VERIFIER_NAME, verifierName);
            jsonObject.put(IWebService.KEY_REQ_TITLE, title);
            jsonObject.put(IWebService.KEY_REQ_COMPANY, company);
            jsonObject.put(IWebService.KEY_REQ_VERIFICATION_DATE, date);
            jsonObject.put(IWebService.KEY_REQ_VERIFICATION_COMMENTS, comments);

            DataRequest dataRequestVerfication = new DataRequest(ReportVerificationActivity.this);
            dataRequestVerfication.execute(IWebService.MAIN_URL, jsonObject.toString(), new DataRequest.CallBack() {
                @Override
                public void onPreExecute() {
                    rlvGlobalProgressbar.setVisibility(View.VISIBLE);

                }

                @Override
                public void onPostExecute(String response) {
                    rlvGlobalProgressbar.setVisibility(View.GONE);
                    if (!DataRequest.hasError(ReportVerificationActivity.this, response, true)) {
                        Log.d(TAG, "response ==> " + response);
                        JSONObject jsonObj = DataRequest.getJObjWebdata(response);
                        try {
                            String url = jsonObj.getString("html_path");
                            switchToShowPdfActivity(url);
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
//    }

    private void getCurrentDate() {
        dateFormater(new Date());
    }

    private void dateFormater(Date d) {
        boolean isDateAlreadySelected = false;
        SimpleDateFormat format = new SimpleDateFormat("d");
        String date = format.format(d);

        if (date.endsWith("1") && !date.endsWith("11"))
            format = new SimpleDateFormat("d'st' MMM, yyyy");
        else if (date.endsWith("2") && !date.endsWith("12"))
            format = new SimpleDateFormat("d'nd' MMM, yyyy");
        else if (date.endsWith("3") && !date.endsWith("13"))
            format = new SimpleDateFormat("d'rd' MMM, yyyy");
        else
            format = new SimpleDateFormat("d'th' MMM, yyyy");

        String yourDate = format.format(d);

        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EE");
        String day = dayOfWeekFormat.format(d);

        Log.d(TAG, "CurrentDate ==> " + yourDate);


        String selectedDate = day + ", " + yourDate;
        Log.d(TAG, "selectedDate ==> " + selectedDate);
        txtViewDate.setText(selectedDate);


    }

    @OnClick(R.id.txtViewDate)
    public void openDatePicker() {

        Calendar newCalendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                dateFormater(newDate.getTime());
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        datePicker.show();
    }

    private boolean isValidData() {
        boolean isValid = true;

        edTxtName.setError(null);
        edTxtTitle.setError(null);
        edTxtCompany.setError(null);
        edTxtComments.setError(null);

        if (Utils.isNullOrEmpty(edTxtName.getText().toString())) {
            edTxtName.setError(getString(R.string.error_required));
            edTxtName.requestFocus();
            isValid = false;
        } else if (Utils.isNullOrEmpty(edTxtTitle.getText().toString())) {
            edTxtTitle.setError(getString(R.string.error_required));
            edTxtTitle.requestFocus();
            isValid = false;
        } else if (Utils.isNullOrEmpty(edTxtCompany.getText().toString())) {
            edTxtCompany.setError(getString(R.string.error_required));
            edTxtCompany.requestFocus();
            isValid = false;
        } else if (Utils.isNullOrEmpty(edTxtComments.getText().toString())) {
            edTxtComments.setError(getString(R.string.error_required));
            edTxtComments.requestFocus();
            isValid = false;
        } else if (PROFILE_IMAGE.isEmpty()) {
            Utils.showAlert(ReportVerificationActivity.this, "", "Please upload your image");
            isValid = false;
        }


        return isValid;
    }

    private void loadDraftData(String reportId) {
        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put(IWebService.KEY_REQ_ACTION, IWebService.KEY_ACTION_REPORT_DRAFT);
            jsonObject.put(IWebService.KEY_USER_ID, DBAdapter.getMapKeyValueString(ReportVerificationActivity.this, IWebService.KEY_USER_ID));
            jsonObject.put(IWebService.KEY_REQ_PAGE_NO, "3");
            jsonObject.put(IWebService.KEY_RES_REPORT_ID, reportId);

            DataRequest dataRequest = new DataRequest(ReportVerificationActivity.this);
            dataRequest.execute(IWebService.MAIN_URL, jsonObject.toString(), new DataRequest.CallBack() {
                @Override
                public void onPreExecute() {
                    rlvGlobalProgressbar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onPostExecute(String response) {
                    rlvGlobalProgressbar.setVisibility(View.GONE);

                    if (!DataRequest.hasError(ReportVerificationActivity.this, response, false)) {
                        Log.d(TAG, "response ==> " + response);

                        JSONObject jsonObject = DataRequest.getJObjWebdata(response);
                        try {
                            Log.d(TAG, "draftData ==> " + jsonObject.toString());
                            JSONArray jsonArray = jsonObject.getJSONArray("report_draft");
                            JSONObject jsonObjectDraft = jsonArray.getJSONObject(0);


                            loadData(jsonObjectDraft);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        isReportDraft = false;
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadData(JSONObject jsonObject) {
        try {
            edTxtName.setText(jsonObject.getString(IWebService.KEY_REQ_VERIFIER_NAME));
            edTxtTitle.setText(jsonObject.getString(IWebService.KEY_REQ_TITLE));
            edTxtCompany.setText(jsonObject.getString(IWebService.KEY_REQ_COMPANY));
            txtViewDate.setText(jsonObject.getString(IWebService.KEY_REQ_VERIFICATION_DATE));
            edTxtComments.setText(jsonObject.getString(IWebService.KEY_REQ_VERIFICATION_COMMENTS));

            String imgpath = jsonObject.getString("image_path") + jsonObject.getString("image");
//            Glide.with(this).load(imgpath).into(imgSelfieVerification);

            Glide
                    .with(this)
                    .load(imgpath)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
//                            imageBitmapArray.add(resource);
                            imgSelfieVerification.setImageBitmap(resource);
                            String encodedImage = Utils.toBASE64(resource);
                            Log.d(TAG, "encodedDownload ==> " + encodedImage);
                            PROFILE_IMAGE = encodedImage;
                        }
                    });


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
