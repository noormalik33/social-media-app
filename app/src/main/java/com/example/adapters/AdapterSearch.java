package blogtalk.compackage blogtalk.com.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import androidx.recyclerview.widget.RecyclerView;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import blogtalk.com.eventbus.EventLike;
import blogtalk.com.eventbus.GlobalBus;
import blogtalk.com.interfaces.InterAdListener;
import blogtalk.com.interfaces.MoreOptionListener;
import blogtalk.com.items.ItemCustomAds;
import blogtalk.com.items.ItemPost;
import blogtalk.com.socialmedia.PostByUserListActivity;
import blogtalk.com.socialmedia.PostDetailActivity;
import blogtalk.com.socialmedia.R;
import blogtalk.com.socialmedia.SplashActivity;
import blogtalk.com.socialmedia.TextPostDetailActivity;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.Methods;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.ads.mediation.facebook.FacebookMediationAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.startapp.sdk.ads.nativead.NativeAdDetails;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.wortise.ads.AdError;
import com.wortise.ads.RevenueData;
import com.wortise.ads.natives.GoogleNativeAd;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AdapterSearch extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    Methods methods;
    ArrayList<ItemPost> arrayList;
    private int columnWidth = 0, columnHeight = 0;

    final int VIEW_PROG = -1;

    Boolean isAdLoaded = false;
    List<ItemCustomAds> arrayListCustomAds = new ArrayList<>();
    List<NativeAd> mNativeAdsAdmob = new ArrayList<>();
    List<NativeAdDetails> nativeAdsStartApp = new ArrayList<>();

    public AdapterSearch(Context context, ArrayList<ItemPost> arrayList) {
        this.context = context;
        this.arrayList = arrayList;

        methods = new Methods(context, interAdListener);
        columnWidth = methods.getColumnWidth(3, 2);
        columnHeight = (int) (columnWidth / 0.77);
        loadNativeAds();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_fav, ic_type;
        RoundedImageView iv_post;
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

        if (viewType == VIEW_PROG) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_progressbar, parent, false);
            return new ProgressViewHolder(itemView);
        } else if (viewType >= 1000) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_ads, parent, false);
            return new ADViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user_post, parent, false);
            return new MyViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyViewHolder) {
            ((MyViewHolder) holder).iv_fav.setVisibility(View.GONE);

            ((MyViewHolder) holder).iv_post.setLayoutParams(new ConstraintLayout.LayoutParams(columnWidth, columnHeight));

            if (arrayList.get(holder.getAbsoluteAdapterPosition()).getPostType().equalsIgnoreCase("video")) {
                ((MyViewHolder) holder).ic_type.setImageResource(R.drawable.ic_video);
            } else if (arrayList.get(holder.getAbsoluteAdapterPosition()).getPostType().equalsIgnoreCase("image")) {
                ((MyViewHolder) holder).ic_type.setImageResource(R.drawable.ic_image);
            } else {
                ((MyViewHolder) holder).ic_type.setImageResource(R.drawable.ic_text);
            }

            if (!arrayList.get(holder.getAbsoluteAdapterPosition()).getPostType().equalsIgnoreCase("text")) {
                ((MyViewHolder) holder).ll_views.setVisibility(View.VISIBLE);
                ((MyViewHolder) holder).iv_post.setVisibility(View.VISIBLE);
                ((MyViewHolder) holder).tv_text.setVisibility(View.GONE);

                ((MyViewHolder) holder).tv_views.setText(methods.formatNumber(arrayList.get(holder.getAbsoluteAdapterPosition()).getTotalViews()));

                if (arrayList.get(holder.getAbsoluteAdapterPosition()).getPostType().equalsIgnoreCase("video")) {
                    ((MyViewHolder) holder).ic_type.setImageResource(R.drawable.ic_video);
                } else {
                    ((MyViewHolder) holder).ic_type.setImageResource(R.drawable.ic_image);
                }

                Picasso.get()
                        .load(arrayList.get(position).getPostImage())
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
                    if (methods.isLoggedAndVerified(true)) {
                        methods.getDoFav(arrayList.get(holder.getAbsoluteAdapterPosition()).getPostID(), null, new MoreOptionListener() {
                            @Override
                            public void onFavDone(String success, boolean isFav, int total) {
                                if (isFav) {
                                    ((MyViewHolder) holder).iv_fav.setImageResource(R.drawable.ic_fav_hover);
                                    arrayList.get(holder.getAbsoluteAdapterPosition()).setFavourite(true);
                                } else {
                                    ((MyViewHolder) holder).iv_fav.setImageResource(R.drawable.ic_fav);
                                    arrayList.get(holder.getAbsoluteAdapterPosition()).setFavourite(false);
                                }
                                GlobalBus.getBus().postSticky(new EventLike(arrayList.get(holder.getAbsoluteAdapterPosition()), false));
                            }

                            @Override
                            public void onUserPostDelete() {
                            }
                        });
                    }
                });

                ((MyViewHolder) holder).iv_post.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        methods.showInter(holder.getAbsoluteAdapterPosition(), "");
                    }
                });
            } else {
                ((MyViewHolder) holder).tv_text.setLayoutParams(new ConstraintLayout.LayoutParams(columnWidth, columnHeight));

                ((MyViewHolder) holder).tv_text.setVisibility(View.VISIBLE);
                ((MyViewHolder) holder).ll_views.setVisibility(View.GONE);
                ((MyViewHolder) holder).iv_post.setVisibility(View.INVISIBLE);

                SpannableString spannableString = methods.highlightHashtagsAndMentions(arrayList.get(holder.getAbsoluteAdapterPosition()).getCaptions(), R.color.text_bb, R.color.text_dark);
                ((MyViewHolder) holder).tv_text.setText(spannableString);
                ((MyViewHolder) holder).tv_text.setMovementMethod(new Methods.CustomLinkMovementMethod());

                ((MyViewHolder) holder).tv_text.setOnClickListener(view -> {
                    methods.showInter(holder.getAbsoluteAdapterPosition(), "");
                });
            }

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
            return VIEW_PROG;
        } else if (arrayList.get(position) == null) {
            return 1000 + position;
        } else {
            return position;
        }
    }

    public boolean isHeader(int pos) {
        return pos == arrayList.size();
    }

    @SuppressLint("MissingPermission")
    private void loadNativeAds() {
        if (Constants.isCustomAdsSearch) {
            for (int i = 0; i < Constants.arrayListCustomAds.size(); i++) {
                isAdLoaded = true;
                if (Constants.arrayListCustomAds.get(i).getDisplayOn().equals(Constants.TAG_FROM_SEARCH)) {
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

                } else { //Videos
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    InterAdListener interAdListener = new InterAdListener() {
        @Override
        public void onClick(int position, String type) {
            Intent intent;
            if(!arrayList.get(position).getPostType().equalsIgnoreCase("text")) {
                Constants.arrayListPosts.clear();
                Constants.arrayListPosts.add(arrayList.get(position));

                intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("isuser", false);
            } else {
                intent = new Intent(context, TextPostDetailActivity.class);
                intent.putExtra("item", arrayList.get(position));
            }
            context.startActivity(intent);
        }
    };
}