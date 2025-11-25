package blogtalk.com.socialmedia;

import android.annotation.SuppressLint;
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

import blogtalk.com.adapters.AdapterUserFollowers;
import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespFollowUserList;
import blogtalk.com.items.ItemUser;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.EndlessRecyclerViewScrollListener;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserListByPostLikeActivity extends AppCompatActivity {

    Methods methods;
    RecyclerView rv_user;
    LinearLayoutManager llm;
    AdapterUserFollowers adapterUsers;
    ArrayList<ItemUser> arrayList = new ArrayList<>();
    ConstraintLayout cl_empty;
    TextView tv_empty;
    CircularProgressBar progressBar;
    int page = 1;
    private Boolean isOver = false, isScroll = false, isLoading = false;
    String errorMsg = "", postID = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list_by_post_like);

        postID = getIntent().getStringExtra("id");

        methods = new Methods(this);
        methods.forceRTLIfSupported();

        findViewById(R.id.iv_post_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        rv_user = findViewById(R.id.rv_user);
        llm = new LinearLayoutManager(UserListByPostLikeActivity.this);
        rv_user.setLayoutManager(llm);
        cl_empty = findViewById(R.id.cl_empty);
        tv_empty = findViewById(R.id.tv_empty);
        progressBar = findViewById(R.id.pb);

        EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(llm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!isOver && !isLoading) {
                    new Handler().postDelayed(() -> {
                        isScroll = true;
                        getUsers();
                    }, 0);
                }
            }

            @Override
            public void onScrollStop() {
            }
        };
        rv_user.addOnScrollListener(endlessRecyclerViewScrollListener);

        getUsers();

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);
    }

    private void getUsers() {
        if (methods.isNetworkAvailable()) {
            isLoading = true;
            Call<RespFollowUserList> call = APIClient.getClient().create(APIInterface.class).getUserListByPostLike(page, methods.getAPIRequest(Constants.URL_USER_BY_POST_LIKE, postID, "", "", "", "", "", "", "", "", "", new SharedPref(UserListByPostLikeActivity.this).getUserId(), ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespFollowUserList> call, @NonNull Response<RespFollowUserList> response) {
                    if (response.body() != null) {
                        if (response.body().getArrayListUser() != null && !response.body().getArrayListUser().isEmpty()) {

                            errorMsg = getString(R.string.err_no_data_found);

                            arrayList.addAll(response.body().getArrayListUser());
                            page = page + 1;
                            setAdapter();
                        } else {
                            isOver = true;
                            try {
                                adapterUsers.hideProgressBar();
                            } catch (Exception ignore) {
                            }
                            errorMsg = getString(R.string.err_server_error);
                            setEmpty();
                        }
                    } else {
                        isOver = true;
                        try {
                            adapterUsers.hideProgressBar();
                        } catch (Exception ignore) {
                        }
                        errorMsg = getString(R.string.err_server_error);
                        setEmpty();
                    }
                    isLoading = false;
                }

                @Override
                public void onFailure(@NonNull Call<RespFollowUserList> call, @NonNull Throwable t) {
                    call.cancel();
                    isOver = true;
                    try {
                        adapterUsers.hideProgressBar();
                    } catch (Exception ignore) {}
                    errorMsg = getString(R.string.err_server_error);
                    setEmpty();
                    isLoading = false;
                }
            });
        } else {
            errorMsg = getString(R.string.err_internet_not_connected);
            setEmpty();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setAdapter() {
        if (!isScroll) {
            adapterUsers = new AdapterUserFollowers(UserListByPostLikeActivity.this, arrayList);
            rv_user.setAdapter(adapterUsers);
            setEmpty();
        } else {
            adapterUsers.notifyDataSetChanged();
        }
    }

    private void setEmpty() {
        progressBar.setVisibility(View.GONE);
        if (!arrayList.isEmpty()) {
            rv_user.setVisibility(View.VISIBLE);
            cl_empty.setVisibility(View.GONE);
        } else {
            rv_user.setVisibility(View.GONE);
            tv_empty.setText(errorMsg);
            cl_empty.setVisibility(View.VISIBLE);
        }
    }
}