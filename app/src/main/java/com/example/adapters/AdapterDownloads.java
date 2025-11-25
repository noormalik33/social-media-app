package com.example.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmedia.DownloadDetailActivity;
import com.example.socialmedia.R;
import com.example.utils.Constants;
import com.example.utils.Methods;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;


public class AdapterDownloads extends RecyclerView.Adapter<AdapterDownloads.MyViewHolder> {

    private ArrayList<Uri> arrayList;
    public Context context;
    public Methods methods;
    private int columnWidth = 0, columnHeight = 0;
    boolean isImage = false;

    public AdapterDownloads(Context context, ArrayList<Uri> arrayList, boolean isImage) {
        this.arrayList = arrayList;
        this.context = context;
        this.isImage = isImage;
        methods = new Methods(context);

        columnWidth = methods.getColumnWidth(3, 2);
        columnHeight = (int) (columnWidth / 0.77);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_post, iv_fav, ic_type, iv_delete;
        LinearLayout ll_views;

        MyViewHolder(View view) {
            super(view);
            iv_post = view.findViewById(R.id.iv_user_post);
            iv_fav = view.findViewById(R.id.iv_user_post_fav);
            iv_delete = view.findViewById(R.id.iv_download_delete);
            ic_type = view.findViewById(R.id.iv_user_post_type);
            ll_views = view.findViewById(R.id.ll_views);
            ll_views.setVisibility(View.GONE);
            iv_fav.setVisibility(View.GONE);
            iv_delete.setVisibility(View.VISIBLE);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user_post, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        holder.iv_post.setLayoutParams(new ConstraintLayout.LayoutParams(columnWidth, columnHeight));

        if (isImage) {
            holder.ic_type.setImageResource(R.drawable.ic_image);
        } else {
            holder.ic_type.setImageResource(R.drawable.ic_video);
        }

        holder.iv_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.arrayListDownloads.clear();
                Constants.arrayListDownloads.addAll(arrayList);

                Collections.swap(Constants.arrayListDownloads, holder.getAbsoluteAdapterPosition(), 0);

                Intent intent = new Intent(context, DownloadDetailActivity.class);
                intent.putExtra("isvideo", !isImage);
                intent.putExtra("pos", 0);
                context.startActivity(intent);
            }
        });

        Picasso.get()
                .load(arrayList.get(position))
                .fit().centerCrop()
                .placeholder(R.drawable.placeholder)
                .into(holder.iv_post);

        holder.iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDeleteAlertDialog(holder.getAbsoluteAdapterPosition());
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public void remove(int position) {
        int delete = context.getContentResolver().delete(arrayList.get(position), null, null);
        if (delete == 1) {
            arrayList.remove(position);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    notifyItemRemoved(position);
                }
            }, 500);
        }
    }

    private void openDeleteAlertDialog(int pos) {
        View view = ((Activity)context).getLayoutInflater().inflate(R.layout.layout_bottom_delete_ac, null);

        BottomSheetDialog dialog_theme = new BottomSheetDialog(context, R.style.BottomSheetDialogStyle);
        dialog_theme.setContentView(view);
        dialog_theme.show();

        MaterialButton btn_cancel = dialog_theme.findViewById(R.id.btn_del_ac_cancel);
        MaterialButton btn_delete = dialog_theme.findViewById(R.id.btn_del_ac_delete);
        TextView tv1 = dialog_theme.findViewById(R.id.tv1);
        TextView tv2 = dialog_theme.findViewById(R.id.tv2);

        tv1.setText(context.getString(R.string.delete));
        tv2.setText(context.getString(R.string.sure_delete));

        btn_cancel.setOnClickListener(v -> dialog_theme.dismiss());

        btn_delete.setOnClickListener(view1 -> {
            remove(pos);
            dialog_theme.dismiss();
        });
    }
}