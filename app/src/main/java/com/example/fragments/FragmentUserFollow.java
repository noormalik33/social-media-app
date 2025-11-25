package blogtalk.com.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import blogtalk.com.adapters.AdapterFollowRequest;
import blogtalk.com.adapters.AdapterUserFollowers;
import blogtalk.com.adapters.AdapterUserFollowing;
import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespFollowUserList;
import blogtalk.com.interfaces.ActionDoneListener;
import blogtalk.com.items.ItemUser;
import blogtalk.com.socialmedia.FollowRequestActivity;
import blogtalk.com.socialmedia.R;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.EndlessRecyclerViewScrollListener;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentUserFollow extends Fragment {

    Methods methods;
    SharedPref sharedPref;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView rv_following;
    LinearLayoutManager llm;
    AdapterUserFollowers adapterUserFollowers;
    AdapterUserFollowing adapterUserFollowing;
    ArrayList<ItemUser> arrayListUsers;
    ConstraintLayout cl_empty;
    TextView tv_empty;
    CircularProgressBar progressBar;
    String errorMsg = "";
    int pos = 0, page = 1, totalRecord = 0;
    private Boolean isOver = false, isScroll = false, isLoading = false;

    public static FragmentUserFollow newInstance(Integer pos) {
        FragmentUserFollow fragment = new FragmentUserFollow();
        Bundle args = new Bundle();
        args.putInt("pos", pos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_follow, container, false);

        pos = getArguments().getInt("pos");

        sharedPref = new SharedPref(getActivity());
        methods = new Methods(getActivity());

        arrayListUsers = new ArrayList<>();

        rv_following = rootView.findViewById(R.id.rv_following);
        llm = new LinearLayoutManager(getActivity());
        rv_following.setLayoutManager(llm);
        swipeRefreshLayout = rootView.findViewById(R.id.srl_follow);

        cl_empty = rootView.findViewById(R.id.cl_empty);
        tv_empty = rootView.findViewById(R.id.tv_empty);
        progressBar = rootView.findViewById(R.id.pb_follow);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                int size = arrayListUsers.size();
                arrayListUsers.clear();
                page=1;
                if(pos == 0 && adapterUserFollowers != null) {
                    adapterUserFollowers.notifyItemRangeRemoved(0, size);
                } else if(adapterUserFollowing != null) {
                    adapterUserFollowing.notifyItemRangeRemoved(0, size);
                }
                swipeRefreshLayout.setRefreshing(true);

                getFollowingUsers();
            }
        });

        rv_following.addOnScrollListener(new EndlessRecyclerViewScrollListener(llm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!isOver && !isLoading) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isScroll = true;
                            getFollowingUsers();
                        }
                    }, 0);
                }
            }
            @Override
            public void onScrollStop() {}
        });

        getFollowingUsers();
 
        return rootView;
    }

    private void getFollowingUsers() {
        if (methods.isNetworkAvailable()) {
            isLoading = true;
            Call<RespFollowUserList> call;
            if(pos == 0) {
                call = APIClient.getClient().create(APIInterface.class).getUserFollowedByOthers(page, methods.getAPIRequest(Constants.URL_USER_FOLLOWED_BY_OTHERS, "", "", "", "", "", "", "", "", "", "", sharedPref.getUserId(), ""));
            } else {
                call = APIClient.getClient().create(APIInterface.class).getUserFollowing(page, methods.getAPIRequest(Constants.URL_USER_FOLLOWING, "", "", "", "", "", "", "", "", "", "", sharedPref.getUserId(), ""));
            }
            call.enqueue(new Callback<RespFollowUserList>() {
                @Override
                public void onResponse(@NonNull Call<RespFollowUserList> call, @NonNull Response<RespFollowUserList> response) {
                    if(getActivity() != null) {
                        if (response.body() != null) {
                            if (response.body().getArrayListUser() != null) {

                                errorMsg = getString(R.string.err_no_data_found);

                                arrayListUsers.addAll(response.body().getArrayListUser());
                                page = page + 1;
                                setAdapter();
                            } else {
                                isOver = true;
                                try {
                                    if(pos == 0) {
                                        adapterUserFollowers.hideProgressBar();
                                    } else {
                                        adapterUserFollowing.hideProgressBar();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                errorMsg = getString(R.string.err_server_error);
                                setEmpty();
                            }
                        } else {
                            isOver = true;
                            try {
                                if(pos == 0) {
                                    adapterUserFollowers.hideProgressBar();
                                } else {
                                    adapterUserFollowing.hideProgressBar();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            errorMsg = getString(R.string.err_server_error);
                            setEmpty();
                        }

                        swipeRefreshLayout.setRefreshing(false);
                    }
                    isLoading = false;
                }

                @Override
                public void onFailure(@NonNull Call<RespFollowUserList> call, @NonNull Throwable t) {
                    if(getActivity() != null) {
                        call.cancel();
                        isOver = true;
                        try {
                            if(pos == 0) {
                                adapterUserFollowers.hideProgressBar();
                            } else {
                                adapterUserFollowing.hideProgressBar();
                            }
                        } catch (Exception ignore) {}
                        errorMsg = getString(R.string.err_server_error);
                        setEmpty();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    isLoading = false;
                }
            });
        } else {
            methods.showToast(getString(R.string.err_internet_not_connected));
            setEmpty();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void setAdapter() {
        if (!isScroll) {
            if(pos == 0) {
                adapterUserFollowers = new AdapterUserFollowers(getActivity(), arrayListUsers);
                rv_following.setAdapter(adapterUserFollowers);
            } else {
                adapterUserFollowing = new AdapterUserFollowing(getActivity(), arrayListUsers);
                rv_following.setAdapter(adapterUserFollowing);
            }
        } else {
            if(pos == 0) {
                adapterUserFollowers.notifyDataSetChanged();
            } else {
                adapterUserFollowing.notifyDataSetChanged();
            }
        }

        setEmpty();
    }

    private void setEmpty() {
        progressBar.setVisibility(View.GONE);
        if (arrayListUsers.size() > 0) {
            rv_following.setVisibility(View.VISIBLE);
            cl_empty.setVisibility(View.GONE);
        } else {
            rv_following.setVisibility(View.GONE);
            tv_empty.setText(errorMsg);
            cl_empty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {

        super.onResume();
    }
}