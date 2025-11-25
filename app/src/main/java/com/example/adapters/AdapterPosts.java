package blogtalk.compackage blogtalk.com.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespSuccess;
import blogtalk.com.interfaces.DoubleClickListener;
import blogtalk.com.interfaces.InterAdListener;
import blogtalk.com.interfaces.MoreOptionListener;
import blogtalk.com.items.ItemCustomAds;
import blogtalk.com.items.ItemImageGallery;
import blogtalk.com.items.ItemPost;
import blogtalk.com.items.ItemUser;
import blogtalk.com.socialmedia.PostDetailActivity;
import blogtalk.com.socialmedia.ProfileActivity;
import blogtalk.com.socialmedia.R;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.DoubleClick;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.ads.mediation.facebook.FacebookMediationAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;
import com.startapp.sdk.ads.nativead.NativeAdDetails;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;
import com.webtoonscorp.android.readmore.ReadMoreTextView;
import com.wortise.ads.AdError;
import com.wortise.ads.RevenueData;
import com.wortise.ads.natives.GoogleNativeAd;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterPosts extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    Methods methods;
    List<ItemPost> arrayList;
    boolean isUser;
    final int VIEW_PROGRESS = -1;
    String from;
    Boolean isAdLoaded = false;
    List<ItemCustomAds> arrayListCustomAds = new ArrayList<>();
    List<NativeAd> mNativeAdsAdmob = new ArrayList<>();
    List<NativeAdDetails> nativeAdsStartApp = new ArrayList<>();

    public AdapterPosts(Context context, List<ItemPost> arrayList, boolean isUser, String from) {
        this.context = context;
        this.arrayList = arrayList;
        this.isUser = isUser;
        this.from = from;
        methods = new Methods(context, interAdListener);
        loadNativeAds();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialButton btn_follow;
        TextView tv_user_name, tv_like, tv_views, tv_comments, tv_date;
        ReadMoreTextView tv_text_post, tv_title;
        ImageView iv_prof, iv_posts, iv_fav, iv_like, iv_more, iv_comment, iv_share, iv_acc_verified;
        ImageView iv_play;
        ViewPager2 vp_home;
        AdapterImagePager adapterImagePager;
        DotsIndicator dots_indicator;
        LinearLayout ll_views;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            btn_follow = itemView.findViewById(R.id.btn_status_follow);
            tv_text_post = itemView.findViewById(R.id.tv_status_text);
            tv_title = itemView.findViewById(R.id.tv_status_title);
            tv_user_name = itemView.findViewById(R.id.tv_status_user_name);
            iv_prof = itemView.findViewById(R.id.iv_status_prof);
            iv_posts = itemView.findViewById(R.id.iv_posts);
            iv_fav = itemView.findViewById(R.id.iv_status_fav);
            iv_like = itemView.findViewById(R.id.iv_status_like);
            iv_more = itemView.findViewById(R.id.iv_status_more);
            iv_comment = itemView.findViewById(R.id.iv_status_comment);
            iv_share = itemView.findViewById(R.id.iv_status_share);
            tv_like = itemView.findViewById(R.id.tv_status_likes);
            tv_views = itemView.findViewById(R.id.tv_status_views);
            tv_comments = itemView.findViewById(R.id.tv_status_comment);
            tv_date = itemView.findViewById(R.id.tv_status_date);
            iv_play = itemView.findViewById(R.id.iv_status_play);
            vp_home = itemView.findViewById(R.id.vp_home);
            dots_indicator = itemView.findViewById(R.id.dots_indicator);
            iv_acc_verified = itemView.findViewById(R.id.iv_prof_account_verify);
            ll_views = itemView.findViewById(R.id.ll_views);
        }
    }

    private static class ADViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout rl_native_ad;
        boolean isAdRequested = false;

        private ADViewHolder(View view) {
            super(view);
            rl_native_ad = view.findViewById(R.id.rl_native_ad);
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
        } else if (viewType >= 1000) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_ads, parent, false);
            return new ADViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_posts, parent, false);
            return new ViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ViewHolder) {
            ItemPost itemPost = arrayList.get(holder.getAbsoluteAdapterPosition());

            ((ViewHolder) holder).iv_acc_verified.setVisibility(new SharedPref(context).getIsAccountVerifyOn() && itemPost.getIsUserAccVerified() ? View.VISIBLE : View.GONE);

            if (!itemPost.getPostType().equalsIgnoreCase("text")) {

                ((ViewHolder) holder).tv_text_post.setVisibility(View.GONE);
                ((ViewHolder) holder).ll_views.setVisibility(View.VISIBLE);
                ((ViewHolder) holder).tv_views.setText(methods.formatNumber(itemPost.getTotalViews()));

                ((ViewHolder) holder).iv_play.setVisibility(itemPost.getPostType().equalsIgnoreCase("video") ? View.VISIBLE : View.GONE);

                if (!itemPost.getPostType().equalsIgnoreCase("video") && itemPost.getArrayListImageGallery() != null && !itemPost.getArrayListImageGallery().isEmpty()) {
                    ((ViewHolder) holder).vp_home.setVisibility(View.VISIBLE);
                    ((ViewHolder) holder).dots_indicator.setVisibility(View.VISIBLE);
                    ((ViewHolder) holder).iv_posts.setVisibility(View.GONE);

                    if (((ViewHolder) holder).adapterImagePager == null) {
                        itemPost.getArrayListImageGallery().add(0, new ItemImageGallery(itemPost.getPostID(), itemPost.getPostImage()));
                        ((ViewHolder) holder).adapterImagePager = new AdapterImagePager(itemPost.getArrayListImageGallery(), new DoubleClickListener() {
                            @Override
                            public void onSingleClick(View view) {
                                Constants.galleryDetailPos = ((ViewHolder) holder).vp_home.getCurrentItem();
                                methods.showInter(holder.getAbsoluteAdapterPosition(), "post");
                            }

                            @Override
                            public void onDoubleClick(View view) {
                                ((ViewHolder) holder).iv_like.callOnClick();
                            }
                        });
                        ((ViewHolder) holder).vp_home.setAdapter(((ViewHolder) holder).adapterImagePager);
                    }
                    ((ViewHolder) holder).dots_indicator.attachTo(((ViewHolder) holder).vp_home);
                } else {
                    ((ViewHolder) holder).iv_posts.setVisibility(View.VISIBLE);
                    ((ViewHolder) holder).vp_home.setVisibility(View.GONE);
                    ((ViewHolder) holder).dots_indicator.setVisibility(View.GONE);

                    ((ViewHolder) holder).iv_posts.setMaxHeight(Constants.photoHeight);
                    Picasso.get().load(!itemPost.getPostImage().isEmpty() ? itemPost.getPostImage() : "null")
                            .placeholder(R.drawable.placeholder)
                            .into(((ViewHolder) holder).iv_posts);

                    ((ViewHolder) holder).iv_posts.setOnClickListener(new DoubleClick(new DoubleClickListener() {
                        @Override
                        public void onSingleClick(View view) {
                            methods.showInter(holder.getAbsoluteAdapterPosition(), "post");
                        }

                        @Override
                        public void onDoubleClick(View view) {
                            ((ViewHolder) holder).iv_like.callOnClick();
                        }
                    }));
                }
                ((ViewHolder) holder).tv_title.setVisibility(View.VISIBLE);

                SpannableString spannableString = methods.highlightHashtagsAndMentions(itemPost.getCaptions(), R.color.text_bb, R.color.text_dark);
                ((ViewHolder) holder).tv_title.setText(spannableString);
                ((ViewHolder) holder).tv_title.setMovementMethod(new Methods.CustomLinkMovementMethod());
            } else {
                ((ViewHolder) holder).tv_text_post.setVisibility(View.VISIBLE);
                ((ViewHolder) holder).iv_posts.setVisibility(View.GONE);
                ((ViewHolder) holder).iv_play.setVisibility(View.GONE);
                ((ViewHolder) holder).tv_title.setVisibility(View.GONE);
                ((ViewHolder) holder).ll_views.setVisibility(View.GONE);
                ((ViewHolder) holder).iv_posts.setVisibility(View.GONE);
                ((ViewHolder) holder).vp_home.setVisibility(View.GONE);

                SpannableString spannableString = methods.highlightHashtagsAndMentions(itemPost.getCaptions(), R.color.text_medium, R.color.text_dark);
                ((ViewHolder) holder).tv_text_post.setText(spannableString);
                ((ViewHolder) holder).tv_text_post.setMovementMethod(new Methods.CustomLinkMovementMethod());
            }

            ((ViewHolder) holder).tv_user_name.setText(itemPost.getUserName());
            ((ViewHolder) holder).tv_like.setText(methods.formatNumber(itemPost.getTotalLikes()));
            ((ViewHolder) holder).tv_comments.setText(methods.formatNumber(itemPost.getTotalComments()));
            ((ViewHolder) holder).tv_date.setText(itemPost.getDate());

            Picasso.get().load(itemPost.getUserImage())
                    .placeholder(R.drawable.placeholder)
                    .into(((ViewHolder) holder).iv_prof);

            if (itemPost.isFavourite()) {
                ((ViewHolder) holder).iv_fav.setImageResource(R.drawable.ic_fav_hover);
                ((ViewHolder) holder).iv_fav.setColorFilter(ContextCompat.getColor(context, R.color.primary), PorterDuff.Mode.SRC_IN);
            } else {
                ((ViewHolder) holder).iv_fav.setImageResource(R.drawable.ic_fav);
                ((ViewHolder) holder).iv_fav.setColorFilter(null);
            }


            ((ViewHolder) holder).btn_follow.setVisibility(((new SharedPref(context).isLogged() && itemPost.getUserId().equals(new SharedPref(context).getUserId())) || isUser) ? View.GONE : View.VISIBLE);

            if (itemPost.isUserRequested()) {
                ((ViewHolder) holder).btn_follow.setText(context.getString(R.string.requested));
            } else if (itemPost.isUserFollowed()) {
                ((ViewHolder) holder).btn_follow.setText(context.getString(R.string.unfollow));
            } else {
                ((ViewHolder) holder).btn_follow.setText(context.getString(R.string.follow));
            }

            if (itemPost.isLiked()) {
                ((ViewHolder) holder).iv_like.setImageResource(R.drawable.ic_like_hover);
                ((ViewHolder) holder).iv_like.setColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN);
            } else {
                ((ViewHolder) holder).iv_like.setImageResource(R.drawable.ic_like);
                ((ViewHolder) holder).iv_like.setColorFilter(null);
            }

            ((ViewHolder) holder).iv_fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (methods.isNetworkAvailable()) {
                        if (methods.isLoggedAndVerified(true)) {
                            ((ViewHolder) holder).iv_fav.setEnabled(false);
                            if (!itemPost.isFavourite()) {
                                ((ViewHolder) holder).iv_fav.setImageResource(R.drawable.ic_fav_hover);
                                ((ViewHolder) holder).iv_fav.setColorFilter(ContextCompat.getColor(context, R.color.primary), PorterDuff.Mode.SRC_IN);
                                itemPost.setFavourite(true);
                            } else {
                                ((ViewHolder) holder).iv_fav.setImageResource(R.drawable.ic_fav);
                                ((ViewHolder) holder).iv_fav.setColorFilter(null);
                                itemPost.setFavourite(false);
                            }

                            methods.getDoFav(itemPost.getPostID(), null, new MoreOptionListener() {
                                @Override
                                public void onFavDone(String success, boolean isFav, int total) {
                                    ((ViewHolder) holder).iv_fav.setEnabled(true);
                                }

                                @Override
                                public void onUserPostDelete() {
                                }
                            });
                        }
                    } else {

                    }
                }
            });

            ((ViewHolder) holder).iv_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (methods.isNetworkAvailable()) {
                        if (methods.isLoggedAndVerified(true)) {
                            ((ViewHolder) holder).iv_like.setEnabled(false);
                            methods.animateHeartButton(((ViewHolder) holder).iv_like);

                            if (!itemPost.isLiked()) {
                                ((ViewHolder) holder).iv_like.setImageResource(R.drawable.ic_like_hover);
                                ((ViewHolder) holder).iv_like.setColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN);
                                itemPost.setLiked(true);
                            } else {
                                ((ViewHolder) holder).iv_like.setImageResource(R.drawable.ic_like);
                                ((ViewHolder) holder).iv_like.setColorFilter(null);
                                itemPost.setLiked(false);
                            }

                            methods.getDoLike(itemPost.getPostID(), new MoreOptionListener() {
                                @Override
                                public void onFavDone(String success, boolean isFav, int totalLikes) {
//                                    if (success.equals("1")) {
                                    itemPost.setTotalLikes(String.valueOf(totalLikes));
                                    ((ViewHolder) holder).tv_like.setText(methods.formatNumber(itemPost.getTotalLikes()));
//                                    } else {
//                                    }
                                    ((ViewHolder) holder).iv_like.setEnabled(true);
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
            });

            ((ViewHolder) holder).iv_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    methods.openMoreDialog(itemPost, new MoreOptionListener() {
                        @Override
                        public void onFavDone(String success, boolean isFav, int total) {
                            if (isFav) {
                                ((ViewHolder) holder).iv_fav.setImageResource(R.drawable.ic_fav_hover);
                                itemPost.setFavourite(true);
                            } else {
                                ((ViewHolder) holder).iv_fav.setImageResource(R.drawable.ic_fav);
                                itemPost.setFavourite(false);
                            }
                        }

                        @Override
                        public void onUserPostDelete() {
                            openDeleteAlertDialog(holder.getAbsoluteAdapterPosition());
                        }
                    });
                }
            });

            ((ViewHolder) holder).iv_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    methods.openCommentDialog(itemPost);
                }
            });

            ((ViewHolder) holder).iv_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    methods.showInter(holder.getAbsoluteAdapterPosition(), "share");
                }
            });

            ((ViewHolder) holder).btn_follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    methods.openFollowUnFollowAlert(itemPost.getUserId(), ((ViewHolder) holder).btn_follow, null, null);
                }
            });

            ((ViewHolder) holder).tv_user_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.putExtra("item_user", new ItemUser(itemPost.getUserId(), itemPost.getUserName(), itemPost.getPostImage(), "", "", "", "No"));
                    context.startActivity(intent);
                }
            });

            ((ViewHolder) holder).iv_prof.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.putExtra("item_user", new ItemUser(itemPost.getUserId(), itemPost.getUserName(), itemPost.getPostImage(), "", "", "", "No"));
                    context.startActivity(intent);
                }
            });
        } else if (holder instanceof ADViewHolder) {
            if (isAdLoaded) {
                if (((ADViewHolder) holder).rl_native_ad.getChildCount() == 0) {
                    if (arrayListCustomAds.isEmpty()) {
                        switch (Constants.nativeAdType) {
                            case Constants.AD_TYPE_ADMOB:
                            case Constants.AD_TYPE_FACEBOOK:
                                if (!mNativeAdsAdmob.isEmpty()) {

                                    int i = new Random().nextInt(mNativeAdsAdmob.size() - 1);

                                    NativeAdView adView = (NativeAdView) ((Activity) context).getLayoutInflater().inflate(R.layout.layout_native_ad_admob, null);
                                    populateUnifiedNativeAdView(mNativeAdsAdmob.get(i), adView);
                                    ((ADViewHolder) holder).rl_native_ad.removeAllViews();
                                    ((ADViewHolder) holder).rl_native_ad.addView(adView);

                                    ((ADViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                                }
                                break;
                            case Constants.AD_TYPE_STARTAPP:
                                if (!nativeAdsStartApp.isEmpty()) {
                                    int i = new Random().nextInt(nativeAdsStartApp.size() - 1);

                                    RelativeLayout nativeAdView = (RelativeLayout) ((Activity) context).getLayoutInflater().inflate(R.layout.layout_native_ad_startapp, null);
                                    populateStartAppNativeAdView(nativeAdsStartApp.get(i), nativeAdView);

                                    ((ADViewHolder) holder).rl_native_ad.removeAllViews();
                                    ((ADViewHolder) holder).rl_native_ad.addView(nativeAdView);
                                    ((ADViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                                }
                                break;
                            case Constants.AD_TYPE_APPLOVIN:
                                MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(Constants.nativeAdID, context);
                                nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
                                    @Override
                                    public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {
                                        nativeAdView.setPadding(0, 0, 0, 10);
                                        nativeAdView.setBackgroundColor(Color.WHITE);
                                        ((ADViewHolder) holder).rl_native_ad.removeAllViews();
                                        ((ADViewHolder) holder).rl_native_ad.addView(nativeAdView);
                                        ((ADViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onNativeAdLoadFailed(final String adUnitId, final MaxError error) {
                                    }

                                    @Override
                                    public void onNativeAdClicked(final MaxAd ad) {
                                    }
                                });

                                nativeAdLoader.loadAd();
                                break;
                            case Constants.AD_TYPE_WORTISE:
                                if (!((ADViewHolder) holder).isAdRequested) {
                                    GoogleNativeAd googleNativeAd = new GoogleNativeAd(
                                            context, Constants.nativeAdID, new GoogleNativeAd.Listener() {

                                        @Override
                                        public void onNativeRevenuePaid(@NonNull GoogleNativeAd googleNativeAd, @NonNull RevenueData revenueData) {

                                        }

                                        @Override
                                        public void onNativeClicked(@NonNull GoogleNativeAd googleNativeAd) {

                                        }

                                        @Override
                                        public void onNativeFailedToLoad(@NonNull GoogleNativeAd googleNativeAd, @NonNull AdError adError) {
                                            ((ADViewHolder) holder).isAdRequested = false;
                                        }

                                        @Override
                                        public void onNativeImpression(@NonNull GoogleNativeAd googleNativeAd) {

                                        }

                                        @Override
                                        public void onNativeLoaded(@NonNull GoogleNativeAd googleNativeAd, @NonNull NativeAd nativeAd) {
                                            NativeAdView adView = (NativeAdView) ((Activity) context).getLayoutInflater().inflate(R.layout.layout_native_ad_admob, null);
                                            populateUnifiedNativeAdView(nativeAd, adView);
                                            ((ADViewHolder) holder).rl_native_ad.removeAllViews();
                                            ((ADViewHolder) holder).rl_native_ad.addView(adView);

                                            ((ADViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                                        }
                                    });
                                    googleNativeAd.load();
                                    ((ADViewHolder) holder).isAdRequested = true;
                                }
                                break;
                        }
                    } else {
                        View adView = ((Activity) context).getLayoutInflater().inflate(R.layout.layout_custom_ads, null);

                        ConstraintLayout cl_ads = adView.findViewById(R.id.cl_custom_ad);
                        ImageView iv_ads = adView.findViewById(R.id.iv_custom_ads);
                        TextView tv_title = adView.findViewById(R.id.tv_custom_ads);

                        int pos = new Random().nextInt(arrayListCustomAds.size());

                        Picasso.get()
                                .load(arrayListCustomAds.get(pos).getImage())
                                .placeholder(R.drawable.placeholder)
                                .into(iv_ads);

                        tv_title.setText(arrayListCustomAds.get(pos).getTitle());

                        cl_ads.setOnClickListener(view -> {
                            try {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(arrayListCustomAds.get(pos).getUrl()));
                                context.startActivity(i);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

                        ((ADViewHolder) holder).rl_native_ad.removeAllViews();
                        ((ADViewHolder) holder).rl_native_ad.addView(adView);

                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        adView.setLayoutParams(params);

                        ((ADViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                    }
                }
            }
        } else {
            if (getItemCount() < 9) {
                ProgressViewHolder.progressBar.setVisibility(View.GONE);
            }
        }
    }

    public void hideProgressBar() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return arrayList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == arrayList.size()) {
            return VIEW_PROGRESS;
        } else if (arrayList.get(position) == null) {
            return 1000 + position;
        } else {
            return position;
        }
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

    @SuppressLint("MissingPermission")
    private void loadNativeAds() {
        if ((from.equals(Constants.TAG_FROM_OTHER) && Constants.isCustomAdsOther)) {
            for (int i = 0; i < Constants.arrayListCustomAds.size(); i++) {
                isAdLoaded = true;
                if (Constants.arrayListCustomAds.get(i).getDisplayOn().equals(Constants.TAG_FROM_OTHER)) {
                    arrayListCustomAds.add(Constants.arrayListCustomAds.get(i));
                }
            }
        } else if (Constants.isNativeAd) {
            switch (Constants.nativeAdType) {
                case Constants.AD_TYPE_ADMOB:
                case Constants.AD_TYPE_FACEBOOK:
                    AdLoader.Builder builder = new AdLoader.Builder(context, Constants.nativeAdID);
                    AdLoader adLoader = builder.forNativeAd(
                                    new NativeAd.OnNativeAdLoadedListener() {
                                        @Override
                                        public void onNativeAdLoaded(@NotNull NativeAd nativeAd) {
                                            mNativeAdsAdmob.add(nativeAd);
//                                            if (mNativeAdsAdmob.size() == 5) {
//                                                isAdLoaded = true;
//                                            }
                                            isAdLoaded = true;
                                        }


                                    }).withAdListener(new AdListener() {
                                @Override
                                public void onAdFailedToLoad(LoadAdError adError) {
                                }
                            })
                            .build();

                    // Load the Native Express ad.
                    Bundle extras = new Bundle();
                    AdRequest adRequest;
                    if (Constants.nativeAdType.equals(Constants.AD_TYPE_ADMOB)) {
                        adRequest = new AdRequest.Builder()
                                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                                .build();
                    } else {
                        adRequest = new AdRequest.Builder()
                                .addNetworkExtrasBundle(AdMobAdapter.class, new Bundle())
                                .addNetworkExtrasBundle(FacebookMediationAdapter.class, extras)
                                .build();
                    }

                    adLoader.loadAds(adRequest, 5);
                    break;
                case Constants.AD_TYPE_STARTAPP:
                    StartAppNativeAd nativeAd = new StartAppNativeAd(context);

                    nativeAd.loadAd(new NativeAdPreferences()
                            .setAdsNumber(3)
                            .setAutoBitmapDownload(true)
                            .setPrimaryImageSize(2), new AdEventListener() {
                        @Override
                        public void onReceiveAd(Ad ad) {
                            nativeAdsStartApp.addAll(nativeAd.getNativeAds());
                            isAdLoaded = true;
                        }

                        @Override
                        public void onFailedToReceiveAd(Ad ad) {
                        }
                    });
                    break;
                case Constants.AD_TYPE_APPLOVIN:
                    isAdLoaded = true;
                    break;
                case Constants.AD_TYPE_WORTISE:
                    isAdLoaded = true;
                    break;
            }
        }
    }

    private void populateUnifiedNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        // Set the media view. Media content will be automatically populated in the media view once
        // adView.setNativeAd() is called.
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        mediaView.setImageScaleType(ImageView.ScaleType.FIT_CENTER);
        mediaView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                float scale = context.getResources().getDisplayMetrics().density;

                int maxHeightPixels = 175;
                int maxHeightDp = (int) (maxHeightPixels * scale + 0.5f);

                if (child instanceof ImageView) { //Images
                    ImageView imageView = (ImageView) child;
                    imageView.setAdjustViewBounds(true);
                    imageView.setMaxHeight(maxHeightDp);
                } else {
                    ViewGroup.LayoutParams params = child.getLayoutParams();
                    params.height = maxHeightDp;
                    child.setLayoutParams(params);
                }
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
            }
        });
        adView.setMediaView(mediaView);

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.GONE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.GONE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.GONE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.GONE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.GONE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.GONE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        for (int i = 0; i < mediaView.getChildCount(); i++) {
            View view = mediaView.getChildAt(i);
            if (view instanceof ImageView) {
                ((ImageView) view).setAdjustViewBounds(true);
            }
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.
        adView.setNativeAd(nativeAd);
    }

    private void populateStartAppNativeAdView(NativeAdDetails nativeAdDetails, RelativeLayout nativeAdView) {
        ImageView icon = nativeAdView.findViewById(R.id.icon);
        TextView title = nativeAdView.findViewById(R.id.title);
        TextView description = nativeAdView.findViewById(R.id.description);
        Button button = nativeAdView.findViewById(R.id.button);

        icon.setImageBitmap(nativeAdDetails.getImageBitmap());
        title.setText(nativeAdDetails.getTitle());
        description.setText(nativeAdDetails.getDescription());
        button.setText(nativeAdDetails.isApp() ? "Install" : "Open");
    }

    public void destroyNativeAds() {
        try {
            for (int i = 0; i < mNativeAdsAdmob.size(); i++) {
                mNativeAdsAdmob.get(i).destroy();
            }
        } catch (Exception ignore) {
        }
    }

    InterAdListener interAdListener = new InterAdListener() {
        @Override
        public void onClick(int position, String type) {
            if (type.equals("post")) {
                int pos = 0, totalItems = 0;
                Constants.arrayListPosts.clear();
                if (!isUser) {
                    Constants.arrayListPosts.add(arrayList.get(position));
                } else {
                    for (int i = 0; i < arrayList.size(); i++) {
                        if (arrayList.get(i) != null && arrayList.get(position).getPostType().equalsIgnoreCase(arrayList.get(i).getPostType())) {
                            Constants.arrayListPosts.add(arrayList.get(i));

                            if (arrayList.get(position).getPostID().equalsIgnoreCase(arrayList.get(i).getPostID())) {
                                pos = Constants.arrayListPosts.size()-1;
                            }
                        }
                    }
                    Collections.swap(Constants.arrayListPosts, pos, 0);
                }
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("isuser", isUser);
                intent.putExtra("pos", 0);
                context.startActivity(intent);
            } else if (type.equals("share")) {
                methods.sharePost(arrayList.get(position).getPostImage(), arrayList.get(position).getShareUrl(), arrayList.get(position).getPostType().equalsIgnoreCase("text"));
            }
        }
    };

    private void openDeleteAlertDialog(int pos) {
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.layout_bottom_delete_ac, null);

        BottomSheetDialog dialog_delete = new BottomSheetDialog(context, R.style.BottomSheetDialogStyle);
        dialog_delete.setContentView(view);
        dialog_delete.show();

        MaterialButton btn_cancel = dialog_delete.findViewById(R.id.btn_del_ac_cancel);
        MaterialButton btn_delete = dialog_delete.findViewById(R.id.btn_del_ac_delete);
        btn_delete.getBackground().setTint(ContextCompat.getColor(context, R.color.delete));
        TextView tv1 = dialog_delete.findViewById(R.id.tv1);
        TextView tv2 = dialog_delete.findViewById(R.id.tv2);

        tv1.setText(context.getString(R.string.delete));
        tv2.setText(context.getString(R.string.sure_delete_post));

        btn_cancel.setOnClickListener(v -> dialog_delete.dismiss());

        btn_delete.setOnClickListener(view1 -> {
            dialog_delete.dismiss();
            getUserPostDelete(pos);
        });
    }
}