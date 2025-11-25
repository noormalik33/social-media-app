package com.example.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.items.ItemUser;
import com.example.socialmedia.ProfileActivity;
import com.example.socialmedia.R;
import com.example.utils.Methods;
import com.example.utils.SharedPref;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUserFollowers extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity activity;
    private final List<ItemUser> arrayList;
    Methods methods;
    final int VIEW_PROGRESS = -1;

    public AdapterUserFollowers(Activity activity, List<ItemUser> arrayList) {
        this.activity = activity;
        this.arrayList = arrayList;
        methods = new Methods(activity);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout cl_follow;
        MaterialButton btn_follow;
        private final ImageView iv_user, iv_acc_verify;
        private final MaterialTextView tv_username;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cl_follow = itemView.findViewById(R.id.cl_follow);
            iv_user = itemView.findViewById(R.id.iv_follow_user);
            iv_acc_verify = itemView.findViewById(R.id.iv_follow_account_verify);
            tv_username = itemView.findViewById(R.id.tv_follow_username);
            btn_follow = itemView.findViewById(R.id.btn_follow);
            btn_follow.setVisibility(View.GONE);
        }
    }

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {
        private static ProgressBar progressBar;

        private ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_PROGRESS) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_progressbar, parent, false);
            return new ProgressViewHolder(itemView);
        } else {
            View view = LayoutInflater.from(activity).inflate(R.layout.layout_user_follow, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ViewHolder) {
            ((ViewHolder) holder).tv_username.setText(arrayList.get(position).getName());

            ((ViewHolder) holder).iv_acc_verify.setVisibility((!new SharedPref(activity).getIsAccountVerifyOn() || !arrayList.get(holder.getAbsoluteAdapterPosition()).getIsAccountVerified()) ? View.GONE : View.VISIBLE);

            Picasso.get().load(arrayList.get(position).getImage())
                    .placeholder(R.drawable.placeholder)
                    .into(((ViewHolder) holder).iv_user);

//        holder.btn_follow.setOnClickListener(v -> {
//            Constants.isUserFollowingChanged = true;
//            methods.getFollowUnfollow(arrayList.get(holder.getAbsoluteAdapterPosition()).getId(), holder.btn_follow, null);
//        });

            ((ViewHolder) holder).cl_follow.setOnClickListener(v -> {
                Intent intent = new Intent(activity, ProfileActivity.class);
                intent.putExtra("item_user", arrayList.get(holder.getAbsoluteAdapterPosition()));
                activity.startActivity(intent);
            });
        } else {
            if (getItemCount() < 9) {
                ProgressViewHolder.progressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == arrayList.size()) {
            return VIEW_PROGRESS;
        } else {
            return position;
        }
    }

    public void hideProgressBar() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }
}
