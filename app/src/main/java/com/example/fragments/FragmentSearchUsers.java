package com.example.fragments;

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

import com.example.adapters.AdapterUserFollowers;
import com.example.apiservices.APIClient;
import com.example.apiservices.APIInterface;
import com.example.apiservices.RespFollowUserList;
import com.example.items.ItemUser;
import com.example.socialmedia.R;
import com.example.utils.Constants;
import com.example.utils.EndlessRecyclerViewScrollListener;
import com.example.utils.Methods;
import com.example.utils.SharedPref;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentSearchUsers extends Fragment {

    private Methods methods;
    private SharedPref sharedPref;
    RecyclerView rv_post;
    AdapterUserFollowers adapterUsers;
    ArrayList<ItemUser> arrayList;
    LinearLayoutManager linearLayoutManager;
    ConstraintLayout cl_empty;
    TextView tv_empty;
    CircularProgressBar progressBar;
    int page = 1;
    private Boolean isOver = false, isScroll = false, isLoading = false;
    String searchText = "", errorMsg = "";
    EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_post, container, false);

        sharedPref = new SharedPref(getActivity());
        methods = new Methods(getActivity());

//        InterAdListener interAdListener = new InterAdListener() {
//            @Override
//            public void onClick(int pos, String type) {
//                int position = getPosition(adapterCategories.getID(pos));
//
//                FragmentSubCategories frag = new FragmentSubCategories();
//                Bundle bundle = new Bundle();
//                bundle.putString("cid", arrayList.get(position).getId());
//                bundle.putString("from", "");
//                frag.setArguments(bundle);
//                FragmentTransaction ft = getParentFragmentManager().beginTransaction();
////                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//                ft.hide(getParentFragmentManager().findFragmentByTag(getString(R.string.categories)));
//                ft.add(R.id.frame_layout, frag, arrayList.get(position).getName());
//                ft.addToBackStack(arrayList.get(position).getName());
//                ft.commitAllowingStateLoss();
//                ((MainActivity) getActivity()).getSupportActionBar().setTitle(arrayList.get(position).getName());
//            }
//        };

        arrayList = new ArrayList<>();
//
        rv_post = rootView.findViewById(R.id.rv_search_post);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        rv_post.setLayoutManager(linearLayoutManager);
        cl_empty = rootView.findViewById(R.id.cl_empty);
        tv_empty = rootView.findViewById(R.id.tv_empty);
        progressBar = rootView.findViewById(R.id.pb_search);

        endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isOver && !isLoading) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    isScroll = true;
                                    getSearchUser();
                                }
                            }, 0);
                        }
                    }
                }, 200);
            }

            @Override
            public void onScrollStop() {
            }
        };
        rv_post.addOnScrollListener(endlessRecyclerViewScrollListener);

        getSearchUser();

        return rootView;
    }

    private void getSearchUser() {
        if (methods.isNetworkAvailable()) {
            if(!searchText.trim().isEmpty()) {
                isLoading = true;
                Call<RespFollowUserList> call = APIClient.getClient().create(APIInterface.class).getSearchUsers(page, methods.getAPIRequest(Constants.URL_SEARCH_USERS, "", "", "", searchText, "", "", "", "", "", "", sharedPref.getUserId(), ""));
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<RespFollowUserList> call, @NonNull Response<RespFollowUserList> response) {
                        if (getActivity() != null) {
                            if (response.body() != null) {
                                if (response.body().getArrayListUser() != null) {
                                    if (!response.body().getArrayListUser().isEmpty()) {
                                        arrayList.addAll(response.body().getArrayListUser());
                                        page = page + 1;
                                        setAdapter(response.body().getArrayListUser().size());
                                    } else {
                                        isOver = true;
                                        try {
                                            adapterUsers.hideProgressBar();
                                        } catch (Exception ignore) {}
                                        errorMsg = getString(R.string.err_no_data_found);
                                        setEmpty();
                                    }
                                } else {
                                    isOver = true;
                                    try {
                                        adapterUsers.hideProgressBar();
                                    } catch (Exception ignore) {}
                                    methods.showToast(getString(R.string.err_server_error));
                                    setEmpty();
                                }
                            } else {
                                isOver = true;
                                try {
                                    adapterUsers.hideProgressBar();
                                } catch (Exception ignore) {}
                                errorMsg = getString(R.string.err_server_error);
                                setEmpty();
                            }
                            isLoading = false;
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<RespFollowUserList> call, @NonNull Throwable t) {
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
                errorMsg = getString(R.string.err_write_someting_to_search);
                setEmpty();
            }
        } else {
            methods.showToast(getString(R.string.err_internet_not_connected));
            setEmpty();
        }
    }

    private void setAdapter(int size) {
        if (!isScroll) {
            adapterUsers = new AdapterUserFollowers(getActivity(), arrayList);
            rv_post.setAdapter(adapterUsers);
        } else {
            adapterUsers.notifyItemRangeChanged(arrayList.size() - size, size);
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

    public void searchUsers(String search) {
        searchText = search;
        isOver = false;
        arrayList.clear();
        if(adapterUsers != null) {
            adapterUsers.notifyDataSetChanged();
            endlessRecyclerViewScrollListener.resetItemCount();
        }

        progressBar.setVisibility(View.VISIBLE);
        page = 1;
        getSearchUser();
    }
}