package com.project.dayworx.activity;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.project.dayworx.R;
import com.project.dayworx.database.DBAdapter;
import com.project.dayworx.database.IDatabase;
import com.project.dayworx.network.DataRequest;
import com.project.dayworx.network.IWebService;
import com.project.dayworx.util.UniqueId;
import com.project.dayworx.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ubuntu on 1/11/17.
 */

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = ProfileActivity.class.getSimpleName();

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

    @BindView(R.id.imgProfile)
    ImageView imgProfile;

    @BindView(R.id.btnBack)
    ImageView btnBack;

    private static final int RESULT_LOAD_IMAGE = 101;
    private static final int RESULT_CAPTURE_IMG = 102;

    private String PROFILE_IMAGE = "";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        initData();

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

    private void initData() {

        String userName = DBAdapter.getMapKeyValueString(ProfileActivity.this, IWebService.KEY_REQ_NAME);

        String[] splitName = userName.split(" ");
        edTxtFirstName.setText(splitName[0]);
        edTxtLastName.setText(splitName[1]);

        String profileUrl = DBAdapter.getMapKeyValueString(ProfileActivity.this, IWebService.KEY_RES_PROFILE_PIC);
        Glide.with(this).load(profileUrl).placeholder(R.drawable.placeholder).into(imgProfile);

        Glide
                .with(this)
                .load(profileUrl)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        PROFILE_IMAGE = Utils.toBASE64(resource); // Possibly runOnUiThread()
                    }
                });

        String mobile = DBAdapter.getMapKeyValueString(ProfileActivity.this, IWebService.KEY_REQ_MOBILE);
        edTxtMobileNumber.setText(mobile);

        String email = DBAdapter.getMapKeyValueString(ProfileActivity.this, IWebService.KEY_REQ_EMAIL);
        edTxtEmail.setText(email);
    }


    private void captureImage() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        File file = new File(android.os.Environment
                .getExternalStorageDirectory(), "temp.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));

        // start the image capture Intents
        startActivityForResult(intent, RESULT_CAPTURE_IMG);
    }

    private void loadImage() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }


    @OnClick(R.id.btnUploadPic)
    public void chooseAction() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_choose_action);
        LinearLayout cameraIntent = (LinearLayout) dialog.findViewById(R.id.takeAPicture);
        LinearLayout galleryIntent = (LinearLayout) dialog.findViewById(R.id.chooseFromGallery);
        cameraIntent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                captureImage();
            }
        });

        galleryIntent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                loadImage();
            }
        });
        dialog.show();
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
                        imgProfile.setImageBitmap(encodeImage);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        encodeImage = RotateBitmap(bitmap, 180);
                        imgProfile.setImageBitmap(encodeImage);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        encodeImage = RotateBitmap(bitmap, 270);
                        imgProfile.setImageBitmap(encodeImage);
                        break;
                    default:
                        encodeImage = bitmap;
                        imgProfile.setImageBitmap(bitmap);
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
            if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

                Log.d("load image ==> ", "gallery");

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                BitmapFactory.Options options = new BitmapFactory.Options();

                // down sizing image as it throws OutOfMemory Exception for larger
                // images
                options.inSampleSize = 8;

                final Bitmap bitmap = BitmapFactory.decodeFile(picturePath
                        .toString(), options);
                PROFILE_IMAGE = Utils.toBASE64(bitmap);
                imgProfile.setImageBitmap(bitmap);

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

    private boolean profileValidation() {

        boolean isValid = true;

        edTxtFirstName.setError(null);
        edTxtLastName.setError(null);
        edTxtMobileNumber.setError(null);
        edTxtEmail.setError(null);

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
        }
        return isValid;
    }

    @OnClick(R.id.btnSave)
    public void updateProfile() {

        if (profileValidation()) {

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(IWebService.KEY_RES_USER_ID, DBAdapter.getMapKeyValueString(ProfileActivity.this, IWebService.KEY_USER_ID));
                jsonObject.put(IWebService.KEY_REQ_ACTION, IWebService.KEY_ACTION_UPDATE_PROFILE);
                jsonObject.put(IWebService.KEY_REQ_NAME, edTxtFirstName.getText().toString().trim() + " " + edTxtLastName.getText().toString().trim());
                jsonObject.put(IWebService.KEY_REQ_MOBILE, edTxtMobileNumber.getText().toString().trim());
                jsonObject.put(IWebService.KEY_REQ_EMAIL, edTxtEmail.getText().toString().trim());
                jsonObject.put(IWebService.KEY_REQ_PROFILE_PIC, PROFILE_IMAGE);

                Log.d(TAG, "user_id ==> " + DBAdapter.getMapKeyValueString(ProfileActivity.this, IWebService.KEY_USER_ID));


                DataRequest signupDataRequest = new DataRequest(ProfileActivity.this);
                signupDataRequest.execute(IWebService.MAIN_URL, jsonObject.toString(), new DataRequest.CallBack() {
                    @Override
                    public void onPreExecute() {

                        rlvGlobalProgressbar.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onPostExecute(String response) {

                        rlvGlobalProgressbar.setVisibility(View.GONE);
                        if (!DataRequest.hasError(ProfileActivity.this, response, true)) {
                            Log.d(TAG, "response ==> " + response);

                            try {

                                JSONObject dataJObject = DataRequest.getJObjWebdata(response);
                                DBAdapter.insertUpdateMap(ProfileActivity.this, IWebService.KEY_USER_ID,
                                        dataJObject.getString(IWebService.KEY_RES_USER_ID));
                                DBAdapter.setMapKeyValueBoolean(ProfileActivity.this, IDatabase.IMap.IS_LOGIN, true);
                                DBAdapter.insertUpdateMap(ProfileActivity.this, IWebService.KEY_REQ_NAME, dataJObject.getString(IWebService.KEY_REQ_NAME));
                                DBAdapter.insertUpdateMap(ProfileActivity.this, IWebService.KEY_REQ_EMAIL, dataJObject.getString(IWebService.KEY_REQ_EMAIL));
                                DBAdapter.insertUpdateMap(ProfileActivity.this, IWebService.KEY_REQ_MOBILE, dataJObject.getString(IWebService.KEY_REQ_MOBILE));
                                DBAdapter.insertUpdateMap(ProfileActivity.this, IWebService.KEY_RES_PROFILE_PIC, dataJObject.getString(IWebService.KEY_RES_PROFILE_PIC));

                                JSONObject responseObj = new JSONObject(response);
                                Toast.makeText(ProfileActivity.this, responseObj.getString(IWebService.KEY_RES_MESSAGE), Toast.LENGTH_SHORT).show();


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
