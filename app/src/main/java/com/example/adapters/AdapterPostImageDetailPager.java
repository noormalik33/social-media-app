package blogtalk.compackage blogtalk.com.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespSuccess;
import blogtalk.com.eventbus.EventLike;
import blogtalk.com.eventbus.GlobalBus;
import blogtalk.com.interfaces.ActionDoneListener;
import blogtalk.com.interfaces.DoubleClickListener;
import blogtalk.com.interfaces.MoreOptionListener;
import blogtalk.com.items.ItemPost;
import blogtalk.com.items.ItemUser;
import blogtalk.com.socialmedia.ProfileActivity;
import blogtalk.com.socialmedia.R;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.DoubleClick;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterPostImageDetailPager extends RecyclerView.Adapter<AdapterPostImageDetailPager.ViewHolder> {

    Context context;
    Methods methods;
    ArrayList<ItemPost> arrayList;
    boolean isUser;

    public AdapterPostImageDetailPager(Context context, ArrayList<ItemPost> arrayList, boolean isUser) {
        this.context = context;
        this.arrayList = arrayList;
        this.isUser = isUser;
        methods = new Methods(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_comments, iv_user_image, iv_like, iv_more, iv_share, iv_acc_verified;
        MaterialButton btn_follow;
        ImageView iv_image;
        LinearLayout ll_details;
        TextView tv_desc, tv_user_name, tv_total_like, tv_total_view, tv_total_comments;
        ProgressBar progressBar;
        RecyclerView rv_tags;
        AdapterTags adapterTags;

        ViewPager2 vp_image_details;
        AdapterImagePager adapterImagePager;
        DotsIndicator dots_indicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ll_details = itemView.findViewById(R.id.ll_post_details);
            iv_image = itemView.findViewById(R.id.iv_image);
            btn_follow = itemView.findViewById(R.id.btn_status_follow);
            rv_tags = itemView.findViewById(R.id.rv_details_tags);
            tv_desc = itemView.findViewById(R.id.tv_details_desc);
            tv_user_name = itemView.findViewById(R.id.tv_status_user_name);
            iv_user_image = itemView.findViewById(R.id.iv_status_prof);
            iv_comments = itemView.findViewById(R.id.iv_detail_comment);
            iv_like = itemView.findViewById(R.id.iv_detail_like);
            iv_share = itemView.findViewById(R.id.iv_detail_share);
            iv_more = itemView.findViewById(R.id.iv_detail_more);
            progressBar = itemView.findViewById(R.id.pb_detail);
            tv_total_like = itemView.findViewById(R.id.tv_detail_total_like);
            tv_total_view = itemView.findViewById(R.id.tv_detail_total_views);
            tv_total_comments = itemView.findViewById(R.id.tv_detail_total_comments);
            iv_acc_verified = itemView.findViewById(R.id.iv_prof_account_verify);

            vp_image_details = itemView.findViewById(R.id.vp_image_details);
            dots_indicator = itemView.findViewById(R.id.dots_indicator);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_post_image_details, parent, false);
        view.setTag("imagePager" + viewType);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ItemPost itemPost = arrayList.get(holder.getAbsoluteAdapterPosition());

        holder.iv_acc_verified.setVisibility(new SharedPref(context).getIsAccountVerifyOn() && arrayList.get(position).getIsUserAccVerified() ? View.VISIBLE : View.GONE);

        SpannableString spannableString = methods.highlightHashtagsAndMentions(itemPost.getCaptions(), R.color.white, R.color.white);
        holder.tv_desc.setText(spannableString);
        holder.tv_desc.setMovementMethod(new Methods.CustomLinkMovementMethod());

        holder.tv_user_name.setText(itemPost.getUserName());
        holder.tv_total_like.setText(methods.formatNumber(itemPost.getTotalLikes()));
        holder.tv_total_like.setOnClickListener(view -> {
            methods.openPostLikesUsersList(itemPost.getPostID());
        });
        holder.tv_total_view.setText(methods.formatNumber(itemPost.getTotalViews()));
        holder.tv_total_comments.setText(methods.formatNumber(itemPost.getTotalComments()));
        Picasso.get()
                .load(itemPost.getUserImage())
                .placeholder(R.drawable.placeholder)
                .into(holder.iv_user_image);

        if (itemPost.getArrayListImageGallery() != null && !itemPost.getArrayListImageGallery().isEmpty()) {
            holder.vp_image_details.setVisibility(View.VISIBLE);
            holder.dots_indicator.setVisibility(View.VISIBLE);
            holder.iv_image.setVisibility(View.GONE);

            if (holder.adapterImagePager == null) {
                holder.adapterImagePager = new AdapterImagePager(itemPost.getArrayListImageGallery(), new DoubleClickListener() {
                    @Override
                    public void onSingleClick(View view) {
                        methods.showInter(holder.getAbsoluteAdapterPosition(), "post");
                    }

                    @Override
                    public void onDoubleClick(View view) {
                        holder.iv_like.callOnClick();
                    }
                });
                holder.vp_image_details.setAdapter(holder.adapterImagePager);
                holder.vp_image_details.setCurrentItem(Constants.galleryDetailPos);
            }
            holder.dots_indicator.attachTo(holder.vp_image_details);

            holder.vp_image_details.setOnClickListener(new DoubleClick(new DoubleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    methods.showInter(holder.getAbsoluteAdapterPosition(), "post");
                }

                @Override
                public void onDoubleClick(View view) {
                    holder.iv_like.callOnClick();
                }
            }));

        } else {
            holder.iv_image.setVisibility(View.VISIBLE);
            holder.vp_image_details.setVisibility(View.GONE);
            holder.dots_indicator.setVisibility(View.GONE);

            Picasso.get().load(itemPost.getPostImage()).into(holder.iv_image);
        }
        if (itemPost.isLiked()) {
            holder.iv_like.setColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN);
            holder.iv_like.setImageResource(R.drawable.ic_like_hover);
        } else {
            holder.iv_like.setColorFilter(null);
            holder.iv_like.setImageResource(R.drawable.ic_like);
        }

        holder.btn_follow.setVisibility(((new SharedPref(context).isLogged() && itemPost.getUserId().equals(new SharedPref(context).getUserId())) || isUser) ? View.GONE : View.VISIBLE);

        holder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.openFollowUnFollowAlert(itemPost.getUserId(), holder.btn_follow, null, new ActionDoneListener() {
                    @Override
                    public void onWorkDone(String success, boolean isDone, int position) {
                    }
                });
            }
        });

        if (holder.adapterTags == null) {
            ArrayList<String> arrayListTags = new ArrayList<>();
            if (itemPost.getTags() != null && !itemPost.getTags().trim().isEmpty()) {
                arrayListTags = new ArrayList<>(Arrays.asList(itemPost.getTags().split(",", -1)));
            }
            holder.adapterTags = new AdapterTags(context, arrayListTags);
            holder.rv_tags.setAdapter(holder.adapterTags);
        }

        holder.iv_comments.setOnClickListener(view -> methods.openCommentDialog(itemPost));

        holder.iv_like.setOnClickListener(view -> {
            if (methods.isNetworkAvailable()) {
                if (methods.isLoggedAndVerified(true)) {
                    methods.animateHeartButton(holder.iv_like);
                    holder.iv_like.setEnabled(false);
                    if (!itemPost.isLiked()) {
                        holder.iv_like.setColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN);
                        holder.iv_like.setImageResource(R.drawable.ic_like_hover);
                        itemPost.setLiked(true);
                    } else {
                        holder.iv_like.setColorFilter(null);
                        holder.iv_like.setImageResource(R.drawable.ic_like);
                        itemPost.setLiked(false);
                    }

                    methods.getDoLike(itemPost.getPostID(), new MoreOptionListener() {
                        @Override
                        public void onFavDone(String success, boolean isFav, int totalLikes) {

                            holder.iv_like.setEnabled(true);
                            holder.tv_total_like.setText(String.valueOf(totalLikes));
                            itemPost.setTotalLikes(String.valueOf(totalLikes));
                            GlobalBus.getBus().postSticky(new EventLike(holder.getAbsoluteAdapterPosition(), itemPost, true));
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

        holder.iv_image.setOnClickListener(new DoubleClick(new DoubleClickListener() {
            @Override
            public void onSingleClick(View view) {

            }

            @Override
            public void onDoubleClick(View view) {
                if (methods.isNetworkAvailable()) {
                    if (methods.isLoggedAndVerified(true)) {
                        methods.animateHeartButton(holder.iv_like);

                        holder.iv_like.setEnabled(false);

                        if (!itemPost.isLiked()) {
                            holder.iv_like.setColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN);
                            holder.iv_like.setImageResource(R.drawable.ic_like_hover);
                            itemPost.setLiked(true);
                        } else {
                            holder.iv_like.setColorFilter(null);
                            holder.iv_like.setImageResource(R.drawable.ic_like);
                            itemPost.setLiked(false);
                        }

                        methods.getDoLike(itemPost.getPostID(), new MoreOptionListener() {
                            @Override
                            public void onFavDone(String success, boolean isFav, int totalLikes) {

                                holder.iv_like.setEnabled(true);
                                holder.tv_total_like.setText(String.valueOf(totalLikes));
                                itemPost.setTotalLikes(String.valueOf(totalLikes));
                                GlobalBus.getBus().postSticky(new EventLike(holder.getAbsoluteAdapterPosition(), itemPost, true));
                            }

                            @Override
                            public void onUserPostDelete() {
                            }
                        });
                    }
                } else {
                    methods.showToast(context.getString(R.string.err_internet_not_connected));
                }
            }
        }));

        holder.iv_share.setOnClickListener(view -> {
            methods.sharePost(itemPost.getPostImage(), itemPost.getShareUrl(), false);
        });

        holder.iv_more.setOnClickListener(v -> methods.openMoreDialog(itemPost, new MoreOptionListener() {
            @Override
            public void onFavDone(String success, boolean isFav, int totalFav) {
                itemPost.setFavourite(isFav);
                GlobalBus.getBus().postSticky(new EventLike(itemPost, false));
            }

            @Override
            public void onUserPostDelete() {
                openDeleteAlertDialog(holder.getAbsoluteAdapterPosition());
            }
        }));

        holder.tv_user_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("item_user", new ItemUser(itemPost.getUserId(), itemPost.getUserName(), itemPost.getPostImage(), "", "", "", "No"));
                context.startActivity(intent);
            }
        });

        holder.iv_user_image.setOnClickListener(view -> {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("item_user", new ItemUser(itemPost.getUserId(), itemPost.getUserName(), itemPost.getPostImage(), "", "", "", "No"));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void openDeleteAlertDialog(int pos) {
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.layout_bottom_delete_ac, null);

        BottomSheetDialog dialog_delete = new BottomSheetDialog(context, R.style.BottomSheetDialogStyle);
        dialog_delete.setContentView(view);
        dialog_delete.show();

        MaterialButton btn_cancel = dialog_delete.findViewById(R.id.btn_del_ac_cancel);
        MaterialButton btn_delete = dialog_delete.findViewById(R.id.btn_del_ac_delete);
        assert btn_delete != null;
        btn_delete.getBackground().setTint(ContextCompat.getColor(context, R.color.delete));
        TextView tv1 = dialog_delete.findViewById(R.id.tv1);
        TextView tv2 = dialog_delete.findViewById(R.id.tv2);

        assert tv1 != null;
        tv1.setText(context.getString(R.string.delete));
        assert tv2 != null;
        tv2.setText(context.getString(R.string.sure_delete_post));

        assert btn_cancel != null;
        btn_cancel.setOnClickListener(v -> dialog_delete.dismiss());

        btn_delete.setOnClickListener(view1 -> {
            dialog_delete.dismiss();
            getUserPostDelete(pos);
        });
    }

    public void getUserPostDelete(int position) {
        if (methods.isLoggedAndVerified(true)) {
            if (methods.isNetworkAvailable()) {

                Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getDeletePost(methods.getAPIRequest(Constants.URL_DELETE_POST, arrayList.get(position).getPostID(), "", "", "", "", "", "", "", "", "", new SharedPref(context).getUserId(), ""));
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                        if (response.body() != null) {
                            if (response.body().getSuccess() != null) {
                                if (response.body().getSuccess().equals("1")) {
                                    Constants.isUserPostDeleted = true;
                                    arrayList.remove(position);
                                    notifyItemRemoved(position);
                                }
                                methods.showToast(response.body().getMessage());
                            } else {
                                methods.showToast(context.getString(R.string.err_server_error));
                            }
                        } else {
                            methods.showToast(context.getString(R.string.err_server_error));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<RespSuccess> call, @NonNull Throwable t) {
                        call.cancel();
                    }
                });
            } else {
                methods.showToast(context.getString(R.string.err_internet_not_connected));
            }
        }
    }
}