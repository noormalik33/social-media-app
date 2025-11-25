package com.example.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.items.ItemLinks;
import com.example.items.ItemPage;
import com.example.socialmedia.LinkAddActivity;
import com.example.socialmedia.LinksActivity;
import com.example.socialmedia.PostByTagActivity;
import com.example.socialmedia.R;
import com.example.socialmedia.WebviewActivity;

import java.util.ArrayList;

public class AdapterLinks extends RecyclerView.Adapter<AdapterLinks.MyViewHolder> {

    Context context;
    ArrayList<ItemLinks> arrayList;
    boolean isFromAdd;

    public AdapterLinks(Context context, ArrayList<ItemLinks> arrayList, boolean isFromAdd) {
        this.context = context;
        this.arrayList = arrayList;
        this.isFromAdd = isFromAdd;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout cl_link;
        TextView tv_link, tv_link_title;

        MyViewHolder(View view) {
            super(view);
            cl_link = view.findViewById(R.id.cl_link);
            tv_link = view.findViewById(R.id.tv_link);
            tv_link_title = view.findViewById(R.id.tv_link_title);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_links, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.tv_link_title.setText(arrayList.get(position).getTitle());
        holder.tv_link.setText(arrayList.get(position).getUrl());

        holder.cl_link.setOnClickListener(view -> {
            Intent intent;
            if(isFromAdd) {
                intent = new Intent(context, LinkAddActivity.class);
                intent.putExtra("isEdit", true);
                intent.putExtra("pos", holder.getBindingAdapterPosition());
            } else {
                String url = arrayList.get(holder.getBindingAdapterPosition()).getUrl();
                if(!url.startsWith("https") && !url.startsWith("http") && !url.startsWith("wwww")) {
                    url = "https://"+url;
                }
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
            }
            context.startActivity(intent);
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
}