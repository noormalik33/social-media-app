package com.example.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventbus.EventLike;
import com.example.eventbus.GlobalBus;
import com.example.interfaces.ClickListener;
import com.example.interfaces.MoreOptionListener;
import com.example.items.ItemPost;
import com.example.socialmedia.PostByUserListActivity;
import com.example.socialmedia.R;
import com.example.utils.Constants;
import com.example.utils.Methods;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterUserPost extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    Methods methods;
    ArrayList<ItemPost> arrayList;
    private int columnWidth = 0, columnHeight = 0;
    boolean isFav = false;
    final int VIEW_PROGRESS = -1;
    ClickListener clickListener;

    public AdapterUserPost(Context context, ArrayList<ItemPost> arrayList, boolean isFav, ClickListener clickListener) {
        this.context = context;
        this.arrayList = arrayList;
        this.isFav = isFav;
        this.clickListener = clickListener;

        methods = new Methods(context);
        columnWidth = methods.getColumnWidth(3, 2);
        columnHeight = (int) (columnWidth/0.77);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_post, iv_fav, ic_type;
        TextView tv_views, tv_text;
        LinearLayout ll_views;

        MyViewHolder(View view) {
            super(view);
            iv_post = view.findViewById(R.id.iv_user_post);
            iv_fav = view.findViewById(R.id.iv_user_post_fav);
            ic_type = view.findViewById(R.id.iv_user_post_type);
            tv_views = view.findViewById(R.id.tv_user_post_view);
            tv_text = view.findViewById(R.id.tv_user_text);
            ll_views = view.findViewById(R.id.ll_views);
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
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user_post, parent, false);
            return new MyViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            ((MyViewHolder) holder).iv_fav.setVisibility(isFav ? View.VISIBLE : View.GONE);
            ((MyViewHolder) holder).iv_post.setLayoutParams(new ConstraintLayout.LayoutParams(columnWidth, columnHeight));

            if (arrayList.get(holder.getAbsoluteAdapterPosition()).getPostType().equalsIgnoreCase("video")) {
                ((MyViewHolder) holder).ic_type.setImageResource(R.drawable.ic_video);
            } else if (arrayList.get(holder.getAbsoluteAdapterPosition()).getPostType().equalsIgnoreCase("image")) {
                ((MyViewHolder) holder).ic_type.setImageResource(R.drawable.ic_image);
            } else {
                ((MyViewHolder) holder).ic_type.setImageResource(R.drawable.ic_text);
            }

            if (!arrayList.get(holder.getAbsoluteAdapterPosition()).getPostType().equalsIgnoreCase("text")) {
                ((MyViewHolder) holder).tv_text.setVisibility(View.GONE);
                ((MyViewHolder) holder).ll_views.setVisibility(View.VISIBLE);
                ((MyViewHolder) holder).iv_post.setVisibility(View.VISIBLE);

                ((MyViewHolder) holder).tv_views.setText(methods.formatNumber(arrayList.get(holder.getAbsoluteAdapterPosition()).getTotalViews()));

                Picasso.get()
                        .load(!arrayList.get(position).getPostImage().isEmpty() ? arrayList.get(position).getPostImage() : "null")
                        .resize(columnWidth, columnHeight)
                        .centerCrop()
                        .placeholder(R.drawable.placeholder)
                        .into(((MyViewHolder) holder).iv_post);

                if (arrayList.get(holder.getAbsoluteAdapterPosition()).isFavourite()) {
                    ((MyViewHolder) holder).iv_fav.setImageResource(R.drawable.ic_fav_hover);
                } else {
                    ((MyViewHolder) holder).iv_fav.setImageResource(R.drawable.ic_fav);
                }

                ((MyViewHolder) holder).iv_fav.setOnClickListener(view -> {
                    if (methods.isNetworkAvailable()) {
                        if (methods.isLoggedAndVerified(true)) {

                            ((MyViewHolder) holder).iv_fav.setEnabled(false);
                            if (!arrayList.get(holder.getAbsoluteAdapterPosition()).isFavourite()) {
                                ((MyViewHolder) holder).iv_fav.setImageResource(R.drawable.ic_fav_hover);
                                arrayList.get(holder.getAbsoluteAdapterPosition()).setFavourite(true);
                            } else {
                                ((MyViewHolder) holder).iv_fav.setImageResource(R.drawable.ic_fav);
                                arrayList.get(holder.getAbsoluteAdapterPosition()).setFavourite(false);
                            }

                            methods.getDoFav(arrayList.get(holder.getAbsoluteAdapterPosition()).getPostID(), null, new MoreOptionListener() {
                                @Override
                                public void onFavDone(String success, boolean isFav, int total) {
                                    ((MyViewHolder) holder).iv_fav.setEnabled(true);
                                    GlobalBus.getBus().postSticky(new EventLike(arrayList.get(holder.getAbsoluteAdapterPosition()), false));
                                }

                                @Override
                                public void onUserPostDelete() {
                                }
                            });
                        }
                    } else {
                        methods.showToast(context.getString(R.string.err_internet_not_connected));
                    }
                });

                ((MyViewHolder) holder).iv_post.setOnClickListener(view -> clickListener.onClick(holder.getAbsoluteAdapterPosition()));
            } else {
                ((MyViewHolder) holder).tv_text.setVisibility(View.VISIBLE);
                ((MyViewHolder) holder).ll_views.setVisibility(View.GONE);
                ((MyViewHolder) holder).iv_post.setVisibility(View.INVISIBLE);

                SpannableString spannableString = methods.highlightHashtagsAndMentions(arrayList.get(holder.getAbsoluteAdapterPosition()).getCaptions(), R.color.text_bb, R.color.text_dark);
                ((MyViewHolder) holder).tv_text.setText(spannableString);
                ((MyViewHolder) holder).tv_text.setMovementMethod(new Methods.CustomLinkMovementMethod());

                ((MyViewHolder) holder).tv_text.setOnClickListener(view -> {
                    clickListener.onClick(holder.getAbsoluteAdapterPosition());
                });
            }
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

    public boolean isHeader(int pos) {
        return pos == arrayList.size();
    }
}