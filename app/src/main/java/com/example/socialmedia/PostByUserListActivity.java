package com.example.socialmedia;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapters.AdapterPosts;
import com.example.apiservices.APIClient;
import com.example.apiservices.APIInterface;
import com.example.apiservices.RespPostList;
import com.example.eventbus.EventUpdatePost;
import com.example.eventbus.GlobalBus;
import com.example.items.ItemPost;
import com.example.utils.Constants;
import com.example.utils.EndlessRecyclerViewScrollListener;
import com.example.utils.Methods;
import com.example.utils.SharedPref;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostByUserListActivity extends AppCompatActivity {

    Methods methods;
    SharedPref sharedPref;
    RecyclerView rv_post;
    LinearLayoutManager llm;
    AdapterPosts adapterPosts;
    ArrayList<ItemPost> arrayList = new ArrayList<>();
    ConstraintLayout cl_empty;
    TextView tv_title, tv_empty;
    CircularProgressBar progressBar;
    int page = 1, totalRecord = 0, scrollPos = 0;
    private Boolean isOver = false, isScroll = false, isLoading = false;
    String errorMsg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_by_tag);

        page = getIntent().getIntExtra("page", 1);
        scrollPos = getIntent().getIntExtra("pos", 0);

        sharedPref = new SharedPref(this);
        methods = new Methods(this);
        methods.forceRTLIfSupported();

        findViewById(R.id.iv_post_back).setOnClickListener(v -> onBackPressed());

        rv_post = findViewById(R.id.rv_post);
        tv_title = findViewById(R.id.tv_post_title);
        llm = new LinearLayoutManager(PostByUserListActivity.this);
        rv_post.setLayoutManager(llm);
        cl_empty = findViewById(R.id.cl_empty);
        tv_empty = findViewById(R.id.tv_empty);
        progressBar = findViewById(R.id.pb_post);

        tv_title.setText("");

        for (int i = 0; i < Constants.arrayListPosts.size(); i++) {
            arrayList.add(Constants.arrayListPosts.get(i));

            if (Constants.isNativeAd || Constants.isCustomAdsOther) {
                int abc = arrayList.lastIndexOf(null);
                if ((((arrayList.size() - (abc + 1)) % (!Constants.isCustomAdsOther ? Constants.nativeAdShow : Constants.customAdOthersPos)) == 0) && (Constants.arrayListPosts.size() - 1 != i || totalRecord != Constants.arrayListPosts.size())) {
                    arrayList.add(null);
                }
            }
        }

        EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(llm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!isOver && !isLoading) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isScroll = true;
                            getPost();
                        }
                    }, 0);
                }
            }
            @Override
            public void onScrollStop() {}
        };
        rv_post.addOnScrollListener(endlessRecyclerViewScrollListener);

        setAdapter();

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);
    }

    private void getPost() {
        if (methods.isNetworkAvailable()) {

            Call<RespPostList> call = APIClient.getClient().create(APIInterface.class).getUserPost(page, methods.getAPIRequest(Constants.URL_USER_POST, sharedPref.getUserId(), "", "", "", "", "", "", "", "", "", sharedPref.getUserId(), ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespPostList> call, @NonNull Response<RespPostList> response) {
                    if (response.body() != null) {
                        if (response.body().getArrayListPost() != null) {
                            if (!response.body().getArrayListPost().isEmpty()) {
                                totalRecord = totalRecord + response.body().getArrayListPost().size();

//                                arrayList.addAll(response.body().getArrayListPost());

                                for (int i = 0; i < response.body().getArrayListPost().size(); i++) {
                                    arrayList.add(response.body().getArrayListPost().get(i));

                                    if (Constants.isNativeAd || Constants.isCustomAdsOther) {
                                        int abc = arrayList.lastIndexOf(null);
                                        if ((((arrayList.size() - (abc + 1)) % (!Constants.isCustomAdsOther ? Constants.nativeAdShow : Constants.customAdOthersPos)) == 0) && (response.body().getArrayListPost().size() - 1 != i || totalRecord != response.body().getTotalRecords())) {
                                            arrayList.add(null);
                                        }
                                    }
                                }
                                page = page + 1;
                                setAdapter();
                            } else {
                                isOver = true;
                                try {
                                    adapterPosts.hideProgressBar();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                errorMsg = getString(R.string.err_no_data_found);
                                setEmpty();
                            }
                        } else {
                            isOver = true;
                            try {
                                adapterPosts.hideProgressBar();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            methods.showToast(getString(R.string.err_server_error));
                            setEmpty();
                        }
                    } else {
                        isOver = true;
                        try {
                            adapterPosts.hideProgressBar();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        errorMsg = getString(R.string.err_server_error);
                        setEmpty();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespPostList> call, @NonNull Throwable t) {
                    call.cancel();
                    isOver = true;
                    try {
                        adapterPosts.hideProgressBar();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    errorMsg = getString(R.string.err_no_data_found);
                    isOver = true;
                    setEmpty();
                }
            });
        } else {
            methods.showToast(getString(R.string.err_internet_not_connected));
            setEmpty();
        }
    }

    private void setAdapter() {
        if (!isScroll) {
            adapterPosts = new AdapterPosts(PostByUserListActivity.this, arrayList, true, Constants.TAG_FROM_OTHER);
            rv_post.setAdapter(adapterPosts);
            setEmpty();
        } else {
            adapterPosts.notifyDataSetChanged();
        }

//        rv_post.scrollToPosition(scrollPos);
    }

    private void setEmpty() {
        progressBar.setVisibility(View.GONE);
        if(!arrayList.isEmpty()) {
            rv_post.setVisibility(View.VISIBLE);
            cl_empty.setVisibility(View.GONE);
        } else {
            rv_post.setVisibility(View.GONE);
            tv_empty.setText(errorMsg);
            cl_empty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        if(Constants.isUserPostDeleted) {
            arrayList.clear();
            if(adapterPosts != null) {
                adapterPosts.notifyDataSetChanged();
            }
            page = 1;
            isOver = false;
            getPost();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (adapterPosts != null) {
            adapterPosts.destroyNativeAds();
        }
        super.onDestroy();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPostUpdate(EventUpdatePost eventUpdatePost) {
        try {
            for (int i = 0; i < arrayList.size(); i++) {
                if(arrayList.get(i).getPostID().equals(eventUpdatePost.getItemPost().getPostID())) {
                    arrayList.set(i, eventUpdatePost.getItemPost());
                    adapterPosts.notifyItemChanged(i);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        GlobalBus.getBus().removeStickyEvent(eventUpdatePost);
    }

    @Override
    public void onStart() {
        super.onStart();
        GlobalBus.getBus().register(this);
    }

    @Override
    public void onStop() {
        GlobalBus.getBus().unregister(this);
        super.onStop();
    }
}