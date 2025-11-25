package com.example.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.items.ItemStories;
import com.example.socialmedia.R;
import com.example.socialmedia.SelectStoryActivity;
import com.example.socialmedia.StoryActivity;
import com.example.utils.Constants;
import com.example.utils.DBHelper;
import com.example.utils.Methods;
import com.example.utils.SharedPref;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterStories extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Methods methods;
    Context context;
    DBHelper dbHelper;
    ArrayList<ItemStories> arrayListStories;
    ItemStories itemUserStories;
    final int VIEW_HEADER = -1;

    public AdapterStories(Context context, ArrayList<ItemStories> arrayListStories, ItemStories itemUserStories) {
        this.context = context;
        this.arrayListStories = arrayListStories;
        this.itemUserStories = itemUserStories;
        methods = new Methods(context);
        dbHelper = new DBHelper(context);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView iv_stories;
        View progressBar;
        TextView tv_username;

        MyViewHolder(View view) {
            super(view);
            iv_stories = view.findViewById(R.id.iv_stories);
            progressBar = view.findViewById(R.id.story_progress_bar);
            tv_username = view.findViewById(R.id.tv_story_username);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView iv_stories;
        View progressBar;
        ImageView ivAddStory;

        HeaderViewHolder(View view) {
            super(view);
            iv_stories = view.findViewById(R.id.iv_stories);
            progressBar = view.findViewById(R.id.story_progress_bar);
            ivAddStory = view.findViewById(R.id.iv_prof_edit_upload);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_HEADER) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_stories_header, parent, false);
            return new HeaderViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_stories, parent, false);
            return new MyViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {

            ItemStories itemStories = arrayListStories.get(holder.getAbsoluteAdapterPosition() - 1);

            Picasso.get()
                    .load(itemStories.getUserImage())
                    .placeholder(R.drawable.placeholder)
                    .into(((MyViewHolder) holder).iv_stories);
            ((MyViewHolder) holder).tv_username.setText(itemStories.getUserName());

            ((MyViewHolder) holder).iv_stories.setOnClickListener(view -> {
                Intent intent = new Intent(context, StoryActivity.class);
                Constants.arrayListStories.clear();
                if(!itemUserStories.getArrayListStoryPost().isEmpty()) {
                    Constants.arrayListStories.add(itemUserStories);
                    intent.putExtra("pos",holder.getAbsoluteAdapterPosition());
                } else {
                    intent.putExtra("pos",holder.getAbsoluteAdapterPosition()-1);
                }
                Constants.arrayListStories.addAll(arrayListStories);

                context.startActivity(intent);
            });

            if(dbHelper.isStoriesSeen(itemStories.getUserID())) {
                ((MyViewHolder) holder).progressBar.setBackgroundResource(R.drawable.bg_stories_circular_seen);
            } else {
                ((MyViewHolder) holder).progressBar.setBackgroundResource(R.drawable.bg_stories_circular);
            }

        } else if (holder instanceof HeaderViewHolder) {

            if(!itemUserStories.getArrayListStoryPost().isEmpty()) {
                ((HeaderViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
            } else {
                ((HeaderViewHolder) holder).progressBar.setVisibility(View.GONE);
            }

            Picasso.get()
                    .load(new SharedPref(context).getUserImage())
                    .placeholder(R.drawable.placeholder)
                    .into(((HeaderViewHolder) holder).iv_stories);

            ((HeaderViewHolder) holder).ivAddStory.setOnClickListener(view -> {
                Intent intent = new Intent(context, SelectStoryActivity.class);
                context.startActivity(intent);
            });

            ((HeaderViewHolder) holder).iv_stories.setOnClickListener(view -> {
                Intent intent;
                if(itemUserStories.getArrayListStoryPost().isEmpty()) {
                    intent = new Intent(context, SelectStoryActivity.class);
                } else {
                    intent = new Intent(context, StoryActivity.class);
                    Constants.arrayListStories.clear();
                    Constants.arrayListStories.add(itemUserStories);
                    Constants.arrayListStories.addAll(arrayListStories);
                    intent.putExtra("pos", 0);
                }
                context.startActivity(intent);
            });

            if(dbHelper.isStoriesSeen(itemUserStories.getUserID())) {
                ((HeaderViewHolder) holder).progressBar.setBackgroundResource(R.drawable.bg_stories_circular_seen);
            } else {
                ((HeaderViewHolder) holder).progressBar.setBackgroundResource(R.drawable.bg_stories_circular);
            }
        }
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayListStories.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_HEADER : position;
    }
}