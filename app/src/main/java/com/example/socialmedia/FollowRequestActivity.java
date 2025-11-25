package com.example.socialmedia;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapters.AdapterFollowRequest;
import com.example.apiservices.APIClient;
import com.example.apiservices.APIInterface;
import com.example.apiservices.RespUserRequestsList;
import com.example.interfaces.ActionDoneListener;
import com.example.items.ItemUserRequests;
import com.example.utils.Constants;
import com.example.utils.EndlessRecyclerViewScrollListener;
import com.example.utils.Methods;
import com.example.utils.SharedPref;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowRequestActivity extends AppCompatActivity {

    Methods methods;
    ImageView iv_back;
    RecyclerView rv_noti;
    AdapterFollowRequest adapterFollowRequest;
    ArrayList<ItemUserRequests> arrayList = new ArrayList<>();
    CircularProgressBar progressBar;
    ConstraintLayout cl_empty;
    TextView tv_empty;
    int page = 1, totalRecord = 0;
    private Boolean isFromNoti = false, isOver = false, isScroll = false, isLoading = false;
    String errorMsg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_request);

        isFromNoti = getIntent().getBooleanExtra("isFromNoti", false);

        methods = new Methods(this);
        methods.forceRTLIfSupported();

        rv_noti = findViewById(R.id.rv_follow_req);
        iv_back = findViewById(R.id.iv_follow_req_back);
        cl_empty = findViewById(R.id.cl_empty);
        tv_empty = findViewById(R.id.tv_empty);
        progressBar = findViewById(R.id.pb_follow_req);

        iv_back.setOnClickListener(view -> onBackPressed());

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv_noti.setLayoutManager(llm);
        rv_noti.addOnScrollListener(new EndlessRecyclerViewScrollListener(llm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!isOver && !isLoading) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isScroll = true;
                            getUserRequests();
                        }
                    }, 0);
                }
            }
            @Override
            public void onScrollStop() {}
        });

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);


        getUserRequests();

        onBackPressedEvent();
    }

    private void getUserRequests() {
        if (methods.isNetworkAvailable()) {

            Call<RespUserRequestsList> call = APIClient.getClient().create(APIInterface.class).getUserRequestedList(page, methods.getAPIRequest(Constants.URL_USER_REQUESTED, "", "", "", "", "", "", "", "", "", "", new SharedPref(FollowRequestActivity.this).getUserId(), ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespUserRequestsList> call, @NonNull Response<RespUserRequestsList> response) {
                    if (response.body() != null) {
                        if (response.body().getArrayListUserRequests() != null) {
                            if (!response.body().getArrayListUserRequests().isEmpty()) {

                                arrayList.addAll(response.body().getArrayListUserRequests());
                                page = page + 1;
                                setAdapter();
                            } else {
                                isOver = true;
                                try {
                                    adapterFollowRequest.hideProgressBar();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                errorMsg = getString(R.string.err_no_data_found);
                                setEmpty();
                            }
                        } else {
                            isOver = true;
                            try {
                                adapterFollowRequest.hideProgressBar();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            methods.showToast(getString(R.string.err_server_error));
                            setEmpty();
                        }
                    } else {
                        isOver = true;
                        try {
                            adapterFollowRequest.hideProgressBar();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        errorMsg = getString(R.string.err_server_error);
                        setEmpty();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespUserRequestsList> call, @NonNull Throwable t) {
                    call.cancel();
                    errorMsg = getString(R.string.err_no_data_found);
                    isOver = true;
                    try {
                        adapterFollowRequest.hideProgressBar();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
            adapterFollowRequest = new AdapterFollowRequest(FollowRequestActivity.this, arrayList, new ActionDoneListener() {
                @Override
                public void onWorkDone(String success, boolean isDone, int position) {
                    if (success.equals("1") && isDone) {
                        errorMsg = getString(R.string.err_no_data_found);
                        setEmpty();
                    }
                }
            });
            rv_noti.setAdapter(adapterFollowRequest);
        } else {
            adapterFollowRequest.notifyDataSetChanged();
        }
        setEmpty();
    }

    private void setEmpty() {
        progressBar.setVisibility(View.GONE);
        if (!arrayList.isEmpty()) {
            rv_noti.setVisibility(View.VISIBLE);
            cl_empty.setVisibility(View.GONE);
        } else {
            rv_noti.setVisibility(View.GONE);
            tv_empty.setText(errorMsg);
            cl_empty.setVisibility(View.VISIBLE);
        }
    }


    private void onBackPressedEvent() {
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!isFromNoti) {
                    finish();
                } else {
                    Intent intent = new Intent(FollowRequestActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}