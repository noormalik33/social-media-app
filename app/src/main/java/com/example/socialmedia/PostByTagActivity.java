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

import com.example.adapters.AdapterHomePosts;
import com.example.adapters.AdapterPosts;
import com.example.apiservices.APIClient;
import com.example.apiservices.APIInterface;
import com.example.apiservices.RespPostList;
import com.example.eventbus.EventRequested;
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

public class PostByTagActivity extends AppCompatActivity {

    Methods methods;
    SharedPref sharedPref;
    RecyclerView rv_post;
    LinearLayoutManager llm;
    AdapterPosts adapterPosts;
    ArrayList<ItemPost> arrayList = new ArrayList<>();
    ConstraintLayout cl_empty;
    TextView tv_title, tv_empty;
    CircularProgressBar progressBar;
    int page = 1, totalRecord = 0;
    private Boolean isOver = false, isScroll = false, isLoading = false;
    String searchText = "", errorMsg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_by_tag);

        searchText = getIntent().getStringExtra("tag");

        sharedPref = new SharedPref(this);
        methods = new Methods(this);
        methods.forceRTLIfSupported();

        findViewById(R.id.iv_post_back).setOnClickListener(v -> onBackPressed());

        rv_post = findViewById(R.id.rv_post);
        tv_title = findViewById(R.id.tv_post_title);
        llm = new LinearLayoutManager(PostByTagActivity.this);
        rv_post.setLayoutManager(llm);
        cl_empty = findViewById(R.id.cl_empty);
        tv_empty = findViewById(R.id.tv_empty);
        progressBar = findViewById(R.id.pb_post);

        tv_title.setText("#".concat(searchText));

        rv_post.addOnScrollListener(new EndlessRecyclerViewScrollListener(llm) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
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
        });

        getPost();

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);
    }

    private void getPost() {
        if (methods.isNetworkAvailable()) {

            Call<RespPostList> call = APIClient.getClient().create(APIInterface.class).getSearchTag(page, methods.getAPIRequest(Constants.URL_SEARCH_TAG, "", "", "", searchText, "", "", "", "", "", "", sharedPref.getUserId(), ""));
            call.enqueue(new Callback<RespPostList>() {
                @Override
                public void onResponse(@NonNull Call<RespPostList> call, @NonNull Response<RespPostList> response) {
                    if (response.body() != null) {
                        if (response.body().getArrayListPost() != null) {
                            if (!response.body().getArrayListPost().isEmpty()) {
                                totalRecord = totalRecord + response.body().getArrayListPost().size();
//                                arrayList.addAll(response.body().getArrayListPost());

                                for (int i = 0; i < response.body().getArrayListPost().size(); i++) {
                                    arrayList.add(response.body().getArrayListPost().get(i));

                                    if (Constants.isNativeAd || Constants.isCustomAdsTags) {
                                        int abc = arrayList.lastIndexOf(null);
                                        if ((((arrayList.size() - (abc + 1)) % (!Constants.isCustomAdsTags ? Constants.nativeAdShow : Constants.customAdTagPos)) == 0) && (response.body().getArrayListPost().size() - 1 != i || totalRecord != response.body().getTotalRecords())) {
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
                            methods.showToast(getString(R.string.err_server_error));
                            setEmpty();
                        }
                    } else {
                        errorMsg = getString(R.string.err_server_error);
                        setEmpty();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespPostList> call, @NonNull Throwable t) {
                    call.cancel();
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
            adapterPosts = new AdapterPosts(PostByTagActivity.this, arrayList, false, Constants.TAG_FROM_TAG);
            rv_post.setAdapter(adapterPosts);
        } else {
            adapterPosts.notifyDataSetChanged();
        }
        setEmpty();
    }

    private void setEmpty() {
        progressBar.setVisibility(View.GONE);
        if (!arrayList.isEmpty()) {
            rv_post.setVisibility(View.VISIBLE);
            cl_empty.setVisibility(View.GONE);
        } else {
            rv_post.setVisibility(View.GONE);
            tv_empty.setText(errorMsg);
            cl_empty.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onFollowChange(EventRequested eventRequested) {
        try {
            for (int i = 0; i < arrayList.size(); i++) {
                if(eventRequested.isRequested()) {
                    if(eventRequested.getType().equals("request")) {
                        arrayList.get(i).setUserRequested(true);
                    } else if(eventRequested.getType().equals("accept")) {
                        arrayList.get(i).setUserFollowed(true);
                    }
                } else {
                    arrayList.get(i).setUserFollowed(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        GlobalBus.getBus().removeStickyEvent(eventRequested);
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

    @Override
    public void onDestroy() {
        if (adapterPosts != null) {
            adapterPosts.destroyNativeAds();
        }
        super.onDestroy();
    }
}