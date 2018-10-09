package com.project.dayworx.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.project.dayworx.R;
import com.project.dayworx.util.TouchImageView;
import com.project.dayworx.util.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ubuntu on 31/1/18.
 */

public class ImageViewActivity extends Activity {

    private static final String TAG = ImageViewActivity.class.getSimpleName();

//    @BindView(R.id.imageView)
//    SubsamplingScaleImageView imageView;

    @BindView(R.id.imageView)
    TouchImageView imageView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_image_view);
        ButterKnife.bind(this);
        imageView.setMaxZoom(4f);

        if (getIntent() != null) {
            Log.d(TAG, "in intent ==> ");
//            String encodedImage = getIntent().getStringExtra("BitmapImage");
//            Bitmap bitmap = Utils.decodeFromBase64ToBitmap(encodedImage);
//
            byte[] bytes = getIntent().getByteArrayExtra("BMP");
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imageView.setImageBitmap(bmp);
        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
