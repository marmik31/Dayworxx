package com.project.dayworx.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ImageWriter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

/**
 * Created by ubuntu on 1/9/17.
 */

public class GalleryActivity extends AppCompatActivity {

    private static final String TAG = GalleryActivity.class.getSimpleName();

    @BindView(R.id.btnBack)
    ImageView btnBack;

    @BindView(R.id.btnAddImage)
    ImageView btnAddImage;

    @BindView(R.id.btnCaptureImage)
    ImageView btnCaptureImage;

    @BindView(R.id.btnDelete)
    ImageView btnDelete;

    @BindView(R.id.recycleView)
    RecyclerView recyclerView;

    @BindView(R.id.rlvGlobalProgressbar)
    RelativeLayout rlvGlobalProgressbar;

    @BindView(R.id.btnNext)
    Button btnNext;

    PhotoAdapter photoAdapter;

    private ArrayList<String> encodedImageList = new ArrayList<>();
    private static String REPORT_ID;

    private static Boolean isReportDraft = false;

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;

    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "DayWorx";

    private Uri fileUri; // file url to store image

    private ArrayList<Bitmap> imageBitmapArray = new ArrayList<>();

    //public boolean isDeleteSelected = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_pickup);
        ButterKnife.bind(this);


        photoAdapter = new PhotoAdapter(this, imageBitmapArray, encodedImageList, recyclerView, GalleryActivity.this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(photoAdapter);

        if (getIntent() != null) {
            REPORT_ID = getIntent().getStringExtra(IWebService.KEY_RES_REPORT_ID);
            Log.d(TAG, "REPORT_ID ==> " + REPORT_ID);
            isReportDraft = getIntent().getBooleanExtra(IWebService.KEY_CONSTANT_DRAFT, false);
            if (isReportDraft) {
                loadDraftData(REPORT_ID);
                btnNext.setText("UPDATE");
            } else {
                btnNext.setText("NEXT");
            }

        }
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                onBackPressed();
            }
        });


        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImageFromGallery();
//                if (encodedImageList.size() > 6) {
//                    Utils.showAlert(GalleryActivity.this, "", "Maximum 6 images can be uploaded");
//                } else {
//                    selectImageFromGallery();
//                }
            }
        });

        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage();

            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (encodedImageList.size() > 0) {
                    Log.d(TAG, "isDeleteSelected ==> " + Utils.isDeleteSelected);
                    if (Utils.isDeleteSelected) {

                        Utils.isDeleteSelected = false;
                        btnAddImage.setVisibility(View.VISIBLE);
                        btnCaptureImage.setVisibility(View.VISIBLE);
                        btnDelete.setImageResource(R.drawable.delet);

                    } else {
                        Utils.isDeleteSelected = true;
                        btnAddImage.setVisibility(View.GONE);
                        btnCaptureImage.setVisibility(View.GONE);
                        btnDelete.setImageResource(R.drawable.close);
                    }

//                photoAdapter = new PhotoAdapter(GalleryActivity.this, imageBitmapArray, recyclerView, isDeleteSelected);
                    photoAdapter.notifyDataSetChanged();
                }
            }

        });


    }


    //    @OnClick(R.id.btnNext)
    public void switchToReportVerificationActivity() {
        Intent intent = new Intent(GalleryActivity.this, ReportVerificationActivity.class);
        intent.putExtra(IWebService.KEY_RES_REPORT_ID, REPORT_ID);
        intent.putExtra(IWebService.KEY_CONSTANT_DRAFT, isReportDraft);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
        finish();
    }

    @OnClick(R.id.btnSetting)
    public void switchToSettingActivity() {
        Intent intent = new Intent(GalleryActivity.this, MenuActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
        finish();
    }


    //    @OnClick(R.id.btnAddImage)
    public void selectImageFromGallery() {
        Intent intent = new Intent(this, AlbumSelectActivity.class);
//set limit on number of images that can be selected, default is 10
        intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 6);
        startActivityForResult(intent, Constants.REQUEST_CODE);
    }

    public void captureImage() {
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK && data != null) {

//            encodedImageList.clear();
//            imageBitmapArray.clear();

            ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
            int totalImageListSize = images.size() + encodedImageList.size();

            Log.d(TAG, "totalImageListSize ==> " + totalImageListSize);
            if (totalImageListSize <= 6) {
                StringBuffer stringBuffer = new StringBuffer();
                for (int i = 0, l = images.size(); i < l; i++) {
                    stringBuffer.append(images.get(i).path + "\n");
                    BitmapFactory.Options options = new BitmapFactory.Options();

                    // down sizing image as it throws OutOfMemory Exception for larger
                    // images
                    options.inSampleSize = 8;

                    final Bitmap bitmap = BitmapFactory.decodeFile(images.get(i).path.toString(), options);
                    imageBitmapArray.add(bitmap);
                    String encodedImage = Utils.toBASE64(bitmap);
                    encodedImageList.add(encodedImage);
//                bitmap.recycle();
                }

//                Log.d(TAG, "ImagePath ==> " + stringBuffer.toString());
//                Log.d(TAG, "encodedImageList.size() ==> " + encodedImageList.size());

                photoAdapter.notifyDataSetChanged();
            } else {
                Utils.showAlert(GalleryActivity.this, "", "Maximum 6 images can be uploaded");
            }

        } else if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {


//                encodedImageList.clear();
//                imageBitmapArray.clear();
                Log.d(TAG, "encodedImageList.size() camera ==> " + encodedImageList.size());
                if (encodedImageList.size() < 6) {
                    BitmapFactory.Options options = new BitmapFactory.Options();

                    // downsizing image as it throws OutOfMemory Exception for larger
                    // images
                    options.inSampleSize = 8;

                    final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                            options);
                    Bitmap rotatedBitmap = checkImageRotation(bitmap);
                    imageBitmapArray.add(checkImageRotation(rotatedBitmap));
                    String encodedImage = Utils.toBASE64(rotatedBitmap);
                    encodedImageList.add(encodedImage);
//                bitmap.recycle();

                    photoAdapter.notifyDataSetChanged();
                } else {
                    Utils.showAlert(GalleryActivity.this, "", "Maximum 6 images can be uploaded");
                }
            }
        }


    }

    private Bitmap checkImageRotation(Bitmap bitmap) {
        Bitmap encodeImage = null;
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(fileUri.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                encodeImage = RotateBitmap(bitmap, 90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                encodeImage = RotateBitmap(bitmap, 180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                encodeImage = RotateBitmap(bitmap, 270);
                break;
            default:
                encodeImage = bitmap;
                break;
        }
        return encodeImage;

    }

    public void removeImage(int position) {
        imageBitmapArray.remove(position);
        encodedImageList.remove(position);
        photoAdapter.notifyDataSetChanged();

        if (encodedImageList.size() == 0) {
            Utils.isDeleteSelected = false;
            btnAddImage.setVisibility(View.VISIBLE);
            btnCaptureImage.setVisibility(View.VISIBLE);
            btnDelete.setImageResource(R.drawable.delet);
        }
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(),
                source.getHeight(), matrix, true);
    }

    private String createJsonRequestObject() {
        JSONObject jsonObject = new JSONObject();


        JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < encodedImageList.size(); i++) {
            jsonArray.put(encodedImageList.get(i));

        }

        try {

            if (isReportDraft) {
                jsonObject.put(IWebService.KEY_REQ_ACTION, IWebService.KEY_ACTION_REPORT_UPDATE);
//                jsonObject.put(IWebService.KEY_RES_REPORT_ID, REPORT_ID);
            } else {
                jsonObject.put(IWebService.KEY_REQ_ACTION, IWebService.KEY_ACTION_SAVE_IMAGE);
            }
            jsonObject.put(IWebService.KEY_USER_ID, DBAdapter.getMapKeyValueString(GalleryActivity.this, IWebService.KEY_RES_USER_ID));
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
//        if (isValidData()) {
        DataRequest sendReportDataRequest = new DataRequest(GalleryActivity.this);
        sendReportDataRequest.execute(IWebService.MAIN_URL, createJsonRequestObject(), new DataRequest.CallBack() {
            @Override
            public void onPreExecute() {
                rlvGlobalProgressbar.setVisibility(View.VISIBLE);

            }

            @Override
            public void onPostExecute(String response) {
                rlvGlobalProgressbar.setVisibility(View.GONE);
                if (!DataRequest.hasError(GalleryActivity.this, response, true)) {
                    Log.d(TAG, "response ==> " + response);
                    switchToReportVerificationActivity();
                }

            }
        });
//        }
    }

    private boolean isValidData() {
        boolean isValid = true;


        if (encodedImageList.isEmpty()) {
            Utils.showAlert(GalleryActivity.this, "", "Please upload project images");
            isValid = false;
        }


        return isValid;
    }

    private void loadDraftData(String reportId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(IWebService.KEY_REQ_ACTION, IWebService.KEY_ACTION_REPORT_DRAFT);
            jsonObject.put(IWebService.KEY_USER_ID, DBAdapter.getMapKeyValueString(GalleryActivity.this, IWebService.KEY_USER_ID));
            jsonObject.put(IWebService.KEY_REQ_PAGE_NO, "2");
            jsonObject.put(IWebService.KEY_RES_REPORT_ID, reportId);

            DataRequest dataRequest = new DataRequest(GalleryActivity.this);
            dataRequest.execute(IWebService.MAIN_URL, jsonObject.toString(), new DataRequest.CallBack() {
                @Override
                public void onPreExecute() {
                    rlvGlobalProgressbar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onPostExecute(String response) {
                    rlvGlobalProgressbar.setVisibility(View.GONE);

                    if (!DataRequest.hasError(GalleryActivity.this, response, false)) {
                        Log.d(TAG, "response ==> " + response);

                        JSONObject jsonObject = DataRequest.getJObjWebdata(response);
                        try {
                            Log.d(TAG, "draftData ==> " + jsonObject.toString());
                            JSONArray jsonArray = jsonObject.getJSONArray("report_draft");
                            JSONObject draftJsonObject = jsonArray.getJSONObject(0);

                            String imgPath = draftJsonObject.getString("image_path");
                            JSONArray jsonArray1 = draftJsonObject.getJSONArray("image_list");
                            ArrayList<String> imgList = new ArrayList<>();
                            for (int i = 0; i < jsonArray1.length(); i++) {
                                imgList.add(imgPath + jsonArray1.get(i));
                                Log.d(TAG, "image ==>" + imgList.get(i));
                            }

                            loadDraftData(imgList);

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

    private void loadDraftData(ArrayList<String> imgList) {
        imageBitmapArray.clear();
        encodedImageList.clear();
        rlvGlobalProgressbar.setVisibility(View.VISIBLE);

        for (int i = 0; i < imgList.size(); i++) {
            try {
                Glide
                        .with(getApplicationContext())
                        .load(imgList.get(i))
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                imageBitmapArray.add(resource);
                                String encodedImage = Utils.toBASE64(resource);
                                encodedImageList.add(encodedImage);
                                Log.d(TAG, "encodedDownload ==> " + encodedImage);
                                photoAdapter.notifyDataSetChanged();
                            }
                        });


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        rlvGlobalProgressbar.setVisibility(View.GONE);

    }

}
