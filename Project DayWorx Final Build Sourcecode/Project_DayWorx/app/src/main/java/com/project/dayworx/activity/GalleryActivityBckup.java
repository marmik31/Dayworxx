package com.project.dayworx.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;
import com.project.dayworx.R;
import com.project.dayworx.adapter.PhotoAdapter;
import com.project.dayworx.database.DBAdapter;
import com.project.dayworx.network.DataRequest;
import com.project.dayworx.network.IWebService;
import com.project.dayworx.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

/**
 * Created by ubuntu on 1/9/17.
 */

public class GalleryActivityBckup extends AppCompatActivity {

    private static final String TAG = GalleryActivityBckup.class.getSimpleName();

    @BindView(R.id.btnBack)
    ImageView btnBack;

    @BindView(R.id.recycleView)
    RecyclerView recyclerView;

    @BindView(R.id.rlvGlobalProgressbar)
    RelativeLayout rlvGlobalProgressbar;

    private ArrayList<String> encodedImageList = new ArrayList<>();
    private static String REPORT_ID;

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;

    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "DayWorx";

    private Uri fileUri; // file url to store image

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_pickup);
        ButterKnife.bind(this);

        if (getIntent() != null) {
            REPORT_ID = getIntent().getStringExtra(IWebService.KEY_RES_REPORT_ID);
            Log.d(TAG, "REPORT_ID ==> " + REPORT_ID);
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    //    @OnClick(R.id.btnNext)
    public void switchToReportVerificationActivity() {
        Intent intent = new Intent(GalleryActivityBckup.this, ReportVerificationActivity.class);
        intent.putExtra(IWebService.KEY_RES_REPORT_ID, REPORT_ID);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }


    @OnClick(R.id.btnAddImage)
    public void selectImageFromGallery() {
        Intent intent = new Intent(this, AlbumSelectActivity.class);
//set limit on number of images that can be selected, default is 10
        intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 10);
        startActivityForResult(intent, Constants.REQUEST_CODE);
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    private void previewCapturedImage() {
        try {
            // hide video preview


            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            encodedImageList.clear();

            ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0, l = images.size(); i < l; i++) {
                stringBuffer.append(images.get(i).path + "\n");
                BitmapFactory.Options options = new BitmapFactory.Options();

                // down sizing image as it throws OutOfMemory Exception for larger
                // images
                options.inSampleSize = 8;

                final Bitmap bitmap = BitmapFactory.decodeFile(images.get(i).path.toString(), options);
                String encodedImage = Utils.toBASE64(bitmap);
                encodedImageList.add(encodedImage);
                bitmap.recycle();
            }

            Log.d(TAG, "ImagePath ==> " + stringBuffer.toString());
            Log.d(TAG, "encodedImageList.size() ==> " + encodedImageList.size());

//            PhotoAdapter photoAdapter = new PhotoAdapter(this, images, recyclerView);
//            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
//            recyclerView.setAdapter(photoAdapter);

        } else if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // display it in image view
                previewCapturedImage();
            }
        }
    }


    private String createJsonRequestObject() {
        JSONObject jsonObject = new JSONObject();


        JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < encodedImageList.size(); i++) {
            jsonArray.put(encodedImageList.get(i));

        }

        try {
            jsonObject.put(IWebService.KEY_REQ_ACTION, IWebService.KEY_ACTION_SAVE_IMAGE);
            jsonObject.put(IWebService.KEY_USER_ID, DBAdapter.getMapKeyValueString(GalleryActivityBckup.this, IWebService.KEY_RES_USER_ID));
            jsonObject.put(IWebService.KEY_REQ_PAGE_NO, "2");
            jsonObject.put(IWebService.KEY_RES_REPORT_ID, REPORT_ID);
            jsonObject.put(IWebService.KEY_REQ_IMAGE_ARRAY, jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "jsonObject.toString() ==> " + jsonObject.toString());

        return jsonObject.toString();
    }

    @OnClick(R.id.btnNext)
    public void sendReportImage() {
        if (isValidData()) {
            DataRequest sendReportDataRequest = new DataRequest(GalleryActivityBckup.this);
            sendReportDataRequest.execute(IWebService.MAIN_URL, createJsonRequestObject(), new DataRequest.CallBack() {
                @Override
                public void onPreExecute() {
                    rlvGlobalProgressbar.setVisibility(View.VISIBLE);

                }

                @Override
                public void onPostExecute(String response) {
                    rlvGlobalProgressbar.setVisibility(View.GONE);
                    if (!DataRequest.hasError(GalleryActivityBckup.this, response, true)) {
                        Log.d(TAG, "response ==> " + response);
                        switchToReportVerificationActivity();
                    }

                }
            });
        }
    }

    private boolean isValidData() {
        boolean isValid = true;


        if (encodedImageList.isEmpty()) {
            Utils.showAlert(GalleryActivityBckup.this, "", "Please upload project images");
            isValid = false;
        }


        return isValid;
    }
}
