package blogtalk.com.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import blogtalk.com.adapters.AdapterUserPost;
import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespPostList;
import blogtalk.com.items.ItemPost;
import blogtalk.com.socialmedia.PostByUserListActivity;
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

public class FragmentUserPost extends Fragment {

    private Methods methods;
    APIInterface apiInterface;
    RecyclerView rv_post;
    AdapterUserPost adapterUserPost;
    ArrayList<ItemPost> arrayList = new ArrayList<>();
    GridLayoutManager gridLayoutManager;
    SwipeRefreshLayout srl_home;
    CircularProgressBar pb;
    int tabPosition = 0;
    LinearLayout ll_empty;
    int page = 1, totalRecord = 0;
    private Boolean isOver = false, isScroll = false, isLoading = false;
    String errorMsg = "";

    public static FragmentUserPost newInstance(int position) {
        FragmentUserPost fragment = new FragmentUserPost();
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_post, container, false);

        apiInterface = APIClient.getClient().create(APIInterface.class);

        tabPosition = getArguments().getInt("pos");

        methods = new Methods(getActivity());

        srl_home = rootView.findViewById(R.id.srl_user_post);
        rv_post = rootView.findViewById(R.id.rv_user_post);
        pb = rootView.findViewById(R.id.pb);
        ll_empty = rootView.findViewById(R.id.ll_empty);

        gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (adapterUserPost.getItemViewType(position) >= 1000 || adapterUserPost.isHeader(position)) ? gridLayoutManager.getSpanCount() : 1;
            }
        });
        rv_post.setLayoutManager(gridLayoutManager);

        EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!isOver && !isLoading) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isScroll = true;
                            getUserPost();
                        }
                    }, 0);
                }
            }

            @Override
            public void onScrollStop() {
            }
        };
        rv_post.addOnScrollListener(endlessRecyclerViewScrollListener);

        srl_home.setOnRefreshListener(() -> {
            isOver = false;
            page = 1;
            totalRecord = 0;
            ll_empty.setVisibility(View.GONE);
            pb.setVisibility(View.VISIBLE);

            int size = arrayList.size();
            arrayList.clear();
            if (adapterUserPost != null) {
                adapterUserPost.notifyItemRangeRemoved(0, size);
            }
            srl_home.setRefreshing(true);

            endlessRecyclerViewScrollListener.resetItemCount();

            getUserPost();
        });

        return rootView;
    }

    private void getUserPost() {
        if (methods.isNetworkAvailable()) {

            Call<RespPostList> call;
            if (tabPosition == 0) {
                call = APIClient.getClient().create(APIInterface.class).getUserPost(page, methods.getAPIRequest(Constants.URL_USER_POST, new SharedPref(requireActivity()).getUserId(), "", "", "", "", "", "", "", "", "", new SharedPref(getActivity()).getUserId(), ""));
            } else {
                call = APIClient.getClient().create(APIInterface.class).getUserFavPost(page, methods.getAPIRequest(Constants.URL_USER_FAV_POST, "", "", "", "", "", "", "", "", "", "", new SharedPref(getActivity()).getUserId(), ""));
            }
            call.enqueue(new Callback<RespPostList>() {
                @Override
                public void onResponse(@NonNull Call<RespPostList> call, @NonNull Response<RespPostList> response) {
                    if (getActivity() != null) {
                        if (response.body() != null) {
                            if (response.body().getArrayListPost() != null) {
                                if (!response.body().getArrayListPost().isEmpty()) {
//                                totalRecord = totalRecord + response.body().getArrayListPost().size();
                                    arrayList.addAll(response.body().getArrayListPost());

//                                for (int i = 0; i < response.body().getArrayListPost().size(); i++) {
//                                    arrayList.add(response.body().getArrayListPost().get(i));
//
//                                    if (Constants.isNativeAd) {
//                                        int abc = arrayList.lastIndexOf(null);
//                                        if (((arrayList.size() - (abc + 1)) % Constants.nativeAdShow == 0) && (response.body().getArrayListPost().size() - 1 != i || totalRecord != response.body().getTotalRecords())) {
//                                            arrayList.add(null);
//                                        }
//                                    }
//                                }
                                    page = page + 1;
                                    setAdapter();
                                } else {
                                    isOver = true;
                                    try {
                                        adapterUserPost.hideProgressBar();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    errorMsg = getString(R.string.err_no_data_found);
                                    setEmpty();
                                }
                            } else {
                                isOver = true;
                                try {
                                    adapterUserPost.hideProgressBar();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                methods.showToast(getString(R.string.err_server_error));
                                setEmpty();
                            }
                        } else {
                            isOver = true;
                            try {
                                adapterUserPost.hideProgressBar();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            errorMsg = getString(R.string.err_server_error);
                            setEmpty();
                        }
                        isLoading = false;
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespPostList> call, @NonNull Throwable t) {
                    if (getActivity() != null) {
                        call.cancel();
                        errorMsg = getString(R.string.err_no_data_found);
                        isOver = true;
                        setEmpty();
                        isLoading = false;
                    }
                }
            });
        } else {
            methods.showToast(getString(R.string.err_internet_not_connected));
            setEmpty();
        }
    }

    private void setAdapter() {
        if (!isScroll) {
            adapterUserPost = new AdapterUserPost(getActivity(), arrayList, tabPosition == 1, position -> {
                Constants.arrayListPosts.clear();
                Constants.arrayListPosts.addAll(arrayList);
                Constants.arrayListPosts.remove(position);
                Constants.arrayListPosts.add(0, arrayList.get(position));

                Intent intent = new Intent(requireActivity(), PostByUserListActivity.class);
                intent.putExtra("pos", position);
                intent.putExtra("page", page);
                startActivity(intent);
            });
            rv_post.setAdapter(adapterUserPost);
        } else {
            adapterUserPost.notifyDataSetChanged();
        }
        setEmpty();
    }

    private void setEmpty() {
        pb.setVisibility(View.GONE);
        srl_home.setRefreshing(false);
        if (arrayList.size() == 0) {
            ((TextView) ll_empty.findViewById(R.id.tv_empty_msg)).setText(errorMsg);
            ll_empty.setVisibility(View.VISIBLE);
            rv_post.setVisibility(View.GONE);
        } else {
            rv_post.setVisibility(View.VISIBLE);
            ll_empty.setVisibility(View.GONE);
        }
    }


    @Override
    public void onResume() {
        if (rv_post != null && arrayList.isEmpty()) {
            getUserPost();
        } else if (Constants.isUserPostDeleted) {
            Constants.isUserPostDeleted = false;
            arrayList.clear();
            if (adapterUserPost != null) {
                adapterUserPost.notifyDataSetChanged();
            }
            page = 1;
            isOver = false;
            getUserPost();
        }
        super.onResume();
    }
}