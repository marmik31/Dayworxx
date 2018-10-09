package com.project.dayworx.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.darsh.multipleimageselect.models.Image;
import com.project.dayworx.R;
import com.project.dayworx.activity.GalleryActivity;
import com.project.dayworx.activity.ImageViewActivity;
import com.project.dayworx.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ubuntu on 12/8/16.
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.MyViewHolder> {

    private static final String TAG = PhotoAdapter.class.getSimpleName();
//    private ArrayList<Image> imageArrayList = new ArrayList<>();

    private ArrayList<Bitmap> imageArrayList = new ArrayList<>();
    private Context context;
    private RecyclerView rv;
    private GalleryActivity galleryActivity;
    private ArrayList<String> encodedImageList;


    public PhotoAdapter(Context context, ArrayList<Bitmap> imageArrayList, ArrayList<String> encodedImageList, RecyclerView rv, GalleryActivity galleryActivity) {
        this.imageArrayList = imageArrayList;
        this.context = context;
        this.rv = rv;
        this.galleryActivity = galleryActivity;
        this.encodedImageList = encodedImageList;

    }


    public class MyViewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.imgPhoto)
        ImageView imgPhoto;

        @BindView(R.id.placeHolderDelete)
        RelativeLayout btnDeleteImage;


        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cell_image, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

//        holder.txtViewItemCount.setText(userDataList.get(position).getProductQuantity());

//        File imgFile = new File(imageArrayList.get(position).path.toString());
//        if (imgFile.exists()) {
//        BitmapFactory.Options options = new BitmapFactory.Options();
//
//        // down sizing image as it throws OutOfMemory Exception for larger
//        // images
//        options.inSampleSize = 8;
//        final Bitmap bitmap = BitmapFactory.decodeFile(imageArrayList.get(position).path.toString(), options);
//        holder.imgPhoto.setImageBitmap(bitmap);
//        }

        holder.imgPhoto.setImageBitmap(imageArrayList.get(position));

        if (Utils.isDeleteSelected) {
            holder.btnDeleteImage.setVisibility(View.VISIBLE);
        } else {
            holder.btnDeleteImage.setVisibility(View.GONE);

        }

        holder.btnDeleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryActivity.removeImage(position);
            }
        });

        holder.imgPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = imageArrayList.get(position);
                Intent intent = new Intent(context, ImageViewActivity.class);
//                intent.putExtra("BitmapImage", encodedImageList.get(position));


                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bytes = stream.toByteArray();
                intent.putExtra("BMP", bytes);
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return imageArrayList.size();
    }

}

