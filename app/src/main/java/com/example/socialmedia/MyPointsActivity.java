package com.example.socialmedia;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapters.AdapterWithdraw;
import com.example.apiservices.APIClient;
import com.example.apiservices.APIInterface;
import com.example.apiservices.RespWithdrawList;
import com.example.items.ItemUser;
import com.example.items.ItemWithdraw;
import com.example.utils.Constants;
import com.example.utils.EndlessRecyclerViewScrollListener;
import com.example.utils.Methods;
import com.example.utils.SharedPref;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Locale;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPointsActivity extends AppCompatActivity {

    Methods methods;
    MaterialButton btn_withdraw;
    TextView tv_points, tv_money, tv_points_converter, tv_empty;
    RecyclerView rv_withdraw;
    ArrayList<ItemWithdraw> arrayList = new ArrayList<>();
    AdapterWithdraw adapterWithdraw;
    CircularProgressBar progressBar;
    ItemUser itemUser;
    private Boolean isOver = false, isScroll = false, isLoading = false;
    int page = 1;
    LinearLayoutManager llm;
    EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_points);

        itemUser = (ItemUser) getIntent().getSerializableExtra("item");

        methods = new Methods(this);
        methods.forceRTLIfSupported();

        findViewById(R.id.iv_back).setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        rv_withdraw = findViewById(R.id.rv_withdraw);
        tv_points = findViewById(R.id.tv_points);
        tv_money = findViewById(R.id.tv_money);
        btn_withdraw = findViewById(R.id.btn_withdraw);
        progressBar = findViewById(R.id.progressBar);
        tv_points_converter = findViewById(R.id.tv_points_converter);
        tv_empty = findViewById(R.id.tv_empty);

        llm = new LinearLayoutManager(this);
        rv_withdraw.setLayoutManager(llm);

        setPoints();
        tv_points_converter.setText(getString(R.string.points_converter, String.valueOf(Constants.onePoint), methods.getCurrencySymbol(new SharedPref(MyPointsActivity.this).getCurrencyCode()), String.valueOf(Constants.oneMoney)));

        btn_withdraw.setOnClickListener(view -> {
            Intent intent = new Intent(MyPointsActivity.this, WithdrawActivity.class);
            intent.putExtra("item", itemUser);
            activityResultLauncher.launch(intent);
        });

        endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(llm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!isOver && !isLoading) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isScroll = true;
                            getWithdrawHistory();
                        }
                    }, 0);
                }
            }
            @Override
            public void onScrollStop() {}
        };
        rv_withdraw.addOnScrollListener(endlessRecyclerViewScrollListener);

        getWithdrawHistory();
    }

    private void getWithdrawHistory() {
        if (methods.isNetworkAvailable()) {
            if(!isScroll) {
                progressBar.setVisibility(View.VISIBLE);
            }

            Call<RespWithdrawList> call = APIClient.getClient().create(APIInterface.class).getWithdrawHistory(page, methods.getAPIRequest(Constants.URL_WITHDRAW_HISTORY, "", "", "", "", "", "", "", "", "", "", new SharedPref(MyPointsActivity.this).getUserId(), ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespWithdrawList> call, @NonNull Response<RespWithdrawList> response) {
                    if (response.body() != null) {
                        if (response.body().getArrayListWithdraw() != null) {
                            if(!response.body().getArrayListWithdraw().isEmpty()) {
                                arrayList.addAll(response.body().getArrayListWithdraw());
                                page = page + 1;
                                setAdapter();
                            } else {
                                isOver = true;
                                try {
                                    adapterWithdraw.hideProgressBar();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                setEmpty();
                            }
                        } else {
                            try {
                                adapterWithdraw.hideProgressBar();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            setEmpty();
                        }
                    } else {
                        try {
                            adapterWithdraw.hideProgressBar();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        setEmpty();
                        methods.showToast(getString(R.string.err_server_error));
                    }
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(@NonNull Call<RespWithdrawList> call, @NonNull Throwable t) {
                    call.cancel();
                    try {
                        adapterWithdraw.hideProgressBar();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    setEmpty();
                    progressBar.setVisibility(View.GONE);
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setAdapter() {
        if (!isScroll) {
            adapterWithdraw = new AdapterWithdraw(MyPointsActivity.this, arrayList);
            rv_withdraw.setAdapter(adapterWithdraw);
            setEmpty();
        } else {
            adapterWithdraw.notifyDataSetChanged();
        }
    }

    private void setEmpty() {
        if (!arrayList.isEmpty()) {
            rv_withdraw.setVisibility(View.VISIBLE);
            tv_empty.setVisibility(View.GONE);
            findViewById(R.id.tv2).setVisibility(View.VISIBLE);
        } else {
            rv_withdraw.setVisibility(View.GONE);
            tv_empty.setVisibility(View.VISIBLE);
            findViewById(R.id.tv2).setVisibility(View.GONE);
        }
    }

    private void setPoints() {
        tv_points.setText(String.valueOf(itemUser.getTotalPoints()));
        double money = (float) (itemUser.getTotalPoints() * Constants.oneMoney) /Constants.onePoint;
        tv_money.setText("(".concat(methods.getCurrencySymbol(new SharedPref(this).getCurrencyCode())).concat(String.format(Locale.getDefault(),"%.2f", money)).concat(")"));
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        if (result.getData().hasExtra("withdraw")) {
                            itemUser.setTotalPoints(itemUser.getTotalPoints() - result.getData().getIntExtra("points", 0));
                            setPoints();
                            Constants.isPointsUpdated = true;
                            arrayList.clear();
                            page = 1;
                            isOver = false;
                            isScroll = false;
                            endlessRecyclerViewScrollListener.resetItemCount();
                            if(adapterWithdraw != null) {
                                adapterWithdraw.notifyDataSetChanged();
                            }
                            getWithdrawHistory();
                        }
                    }
                }
            });
}