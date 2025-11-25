package com.example.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmedia.R;
import com.example.utils.Methods;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterDownloadImageDetailPager extends RecyclerView.Adapter<AdapterDownloadImageDetailPager.ViewHolder> {

    Context context;
    Methods methods;
    ArrayList<Uri> arrayList;

    public AdapterDownloadImageDetailPager(Context context, ArrayList<Uri> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        methods = new Methods(context);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_image;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_image = itemView.findViewById(R.id.iv_image);
            progressBar = itemView.findViewById(R.id.pb_detail);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_download_image_details, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Picasso.get().load(arrayList.get(holder.getAbsoluteAdapterPosition())).into(holder.iv_image);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}