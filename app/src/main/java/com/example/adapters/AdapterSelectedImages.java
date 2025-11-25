package com.example.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.socialmedia.CropImageActivity;
import com.example.socialmedia.R;
import com.example.utils.Constants;
import com.example.utils.Methods;
import com.naver.android.helloyako.imagecrop.view.ImageCropView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterSelectedImages extends RecyclerView.Adapter<AdapterSelectedImages.MyViewHolder> {

    Context context;
    ArrayList<String> arrayList;
    Methods methods;

    public AdapterSelectedImages(Context context, ArrayList<String> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        methods = new Methods(context);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_remove, iv_edit;
        ImageCropView iv_create_post;
        CardView cv_add_post;

        MyViewHolder(View view) {
            super(view);
            iv_remove = view.findViewById(R.id.iv_remove);
            iv_edit = view.findViewById(R.id.iv_edit);
            iv_create_post = view.findViewById(R.id.iv_create_post);
            cv_add_post = view.findViewById(R.id.cv_add_post);

            iv_create_post.setAspectRatio(1, 1);
            iv_create_post.setScrollEnabled(false);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_images_add_post, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.iv_create_post.setImageFilePath(arrayList.get(position));

        if (arrayList.size() > 1) {
            holder.iv_remove.setVisibility(View.VISIBLE);
        } else {
            holder.iv_remove.setVisibility(View.GONE);
        }

        holder.iv_remove.setOnClickListener(view -> {
            arrayList.remove(holder.getAbsoluteAdapterPosition());
            Constants.arrayListSelectedImagesUri.remove(holder.getAbsoluteAdapterPosition());
            notifyItemRemoved(holder.getAbsoluteAdapterPosition());

            if (arrayList.size() == 1) {
                notifyItemChanged(0);
            }
        });

        holder.iv_edit.setOnClickListener(view -> {
            Intent intent = new Intent(context, CropImageActivity.class);
            intent.putExtra("uri", String.valueOf(Constants.arrayListSelectedImagesUri.get(holder.getAbsoluteAdapterPosition())));
            intent.putExtra("pos", holder.getAbsoluteAdapterPosition());
            ((Activity)context).startActivityForResult(intent, 111);
        });
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public Bitmap getCroppedBitmapFromImage(int position, ViewPager2 recyclerView) {

        RecyclerView.ViewHolder holder = ((RecyclerView)recyclerView.getChildAt(0)).findViewHolderForAdapterPosition(position);
        if (holder instanceof MyViewHolder) {
            MyViewHolder adViewHolder = (MyViewHolder) holder;

            if (adViewHolder.iv_create_post != null) {
                return adViewHolder.iv_create_post.getCroppedImage();
            }
        }
        return null;
    }
}