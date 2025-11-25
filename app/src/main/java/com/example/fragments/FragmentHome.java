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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.adapters.AdapterHomePosts;
import com.example.apiservices.APIClient;
import com.example.apiservices.APIInterface;
import com.example.apiservices.RespHomeList;
import com.example.apiservices.RespStories;
import com.example.eventbus.EventLike;
import com.example.eventbus.EventRequested;
import com.example.eventbus.EventStoryUpload;
import com.example.eventbus.GlobalBus;
import com.example.items.ItemPost;
import com.example.items.ItemStories;
import com.example.socialmedia.R;
import com.example.utils.Constants;
import com.example.utils.DBHelper;
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

public class FragmentHome extends Fragment {

    private Methods methods;
    private SharedPref sharedPref;
    private DBHelper dbHelper;
    RecyclerView rv_home;
    LinearLayoutManager llm;
    AdapterHomePosts adapterHome;
    ArrayList<ItemPost> arrayList = new ArrayList<>();
    SwipeRefreshLayout srl_home;
    ConstraintLayout cl_empty;
    TextView tv_empty;
    CircularProgressBar progressBar;
    int page = 1, totalRecord = 0;
    private Boolean isOver = false, isScroll = false, isLoading = false;
    String errorMsg = "";

    ArrayList<ItemStories> arrayListStories = new ArrayList<>();
    ItemStories itemOwnStories;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        sharedPref = new SharedPref(getActivity());
        methods = new Methods(getActivity());
        dbHelper = new DBHelper(getActivity());
        methods.forceRTLIfSupported();

        rv_home = rootView.findViewById(R.id.rv_home);
        srl_home = rootView.findViewById(R.id.srl_home);
        cl_empty = rootView.findViewById(R.id.cl_empty);
        tv_empty = rootView.findViewById(R.id.tv_empty);
        progressBar = rootView.findViewById(R.id.pb_home);

        llm = new LinearLayoutManager(getActivity());
        rv_home.setLayoutManager(llm);

        EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(llm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!isOver && !isLoading) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isScroll = true;
                            getHome();
                        }
                    }, 0);
                }
            }

            @Override
            public void onScrollStop() {
            }
        };
        rv_home.addOnScrollListener(endlessRecyclerViewScrollListener);

        srl_home.setOnRefreshListener(() -> {
            isOver = false;
            page=1;
            totalRecord = 0;

            int size = arrayList.size();
            arrayList.clear();
            if(adapterHome != null) {
                adapterHome.notifyItemRangeRemoved(0, size);
            }
            srl_home.setRefreshing(true);

            getHome();
        });

        getHome();

        if(sharedPref.getIsUserValidCheck()) {
            methods.getUserValidInvalid(sharedPref);
        }

        return rootView;
    }

//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//
//        requireActivity().addMenuProvider(new MenuProvider() {
//            @Override
//            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
//                menu.clear();
//                menuInflater.inflate(R.menu.menu_search, menu);
//                menu.findItem(R.id.menu_filter).setVisible(false);
//                MenuItem item = menu.findItem(R.id.menu_search);
//                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
//                searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
//                searchView.setOnQueryTextListener(queryTextListener);
//            }
//
//            @Override
//            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
//                return false;
//            }
//        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
//
//        super.onViewCreated(view, savedInstanceState);
//    }

//    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
//        @Override
//        public boolean onQueryTextSubmit(String s) {
//            return true;
//        }
//
//        @Override
//        public boolean onQueryTextChange(String s) {
//            if (!searchView.isIconified() && adapterCategories != null) {
//                adapterCategories.getFilter().filter(s);
//                adapterCategories.notifyDataSetChanged();
//            }
//            return false;
//        }
//    };

    private void getStories() {
        if (methods.isNetworkAvailable()) {

            Call<RespStories> call = APIClient.getClient().create(APIInterface.class).getStories(methods.getAPIRequest(Constants.URL_STORY_LIST, "", "", "", "", "", "", "", "", "", "", sharedPref.getUserId(), ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespStories> call, @NonNull Response<RespStories> response) {
                    if (getActivity() != null) {
                        if (response.body() != null) {
                            if (response.body().getArrayListStories() != null) {
                                arrayListStories.clear();
                                dbHelper.clearOldStories();

                                arrayListStories.addAll(response.body().getArrayListStories());
                                if (response.body().getArrayListOwnStories() != null && !response.body().getArrayListOwnStories().isEmpty()) {
                                    itemOwnStories = new ItemStories("", sharedPref.getUserId(), sharedPref.getName(),sharedPref.getUserImage(), response.body().getArrayListOwnStories());
                                    for (int i = 0; i < itemOwnStories.getArrayListStoryPost().size(); i++) {
                                        dbHelper.addStoriesList(itemOwnStories.getArrayListStoryPost().get(i), itemOwnStories.getUserID());
                                    }
                                } else {
                                    itemOwnStories = new ItemStories("", sharedPref.getUserId(), sharedPref.getName(),sharedPref.getUserImage(), new ArrayList<>());
                                }

                                for (int i = 0; i < arrayListStories.size(); i++) {
                                    for (int j = 0; j < arrayListStories.get(i).getArrayListStoryPost().size(); j++) {
                                        dbHelper.addStoriesList(arrayListStories.get(i).getArrayListStoryPost().get(j), arrayListStories.get(i).getUserID());
                                    }
                                }

                                adapterHome.setStoriesData(arrayListStories, itemOwnStories);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespStories> call, @NonNull Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void getHome() {
        if (methods.isNetworkAvailable()) {
            if(page == 1) {
                rv_home.setVisibility(View.GONE);
            }
            Call<RespHomeList> call = APIClient.getClient().create(APIInterface.class).getHome(page, methods.getAPIRequest(Constants.URL_HOME, "", "", "", "", "", "", "", "", "", "", sharedPref.getUserId(), ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespHomeList> call, @NonNull Response<RespHomeList> response) {
                    if (getActivity() != null) {
                        if (response.body() != null) {
                            if (response.body().getArrayListPost() != null) {
                                if (!response.body().getArrayListPost().isEmpty()) {
                                    totalRecord = totalRecord + response.body().getArrayListPost().size();

                                    for (int i = 0; i < response.body().getArrayListPost().size(); i++) {
                                        arrayList.add(response.body().getArrayListPost().get(i));

                                        if (Constants.isNativeAd || Constants.isCustomAdsHome) {
                                            int abc = arrayList.lastIndexOf(null);
                                            if ((((arrayList.size() - (abc + 1)) % (!Constants.isCustomAdsHome ? Constants.nativeAdShow : Constants.customAdHomePos)) == 0) && (response.body().getArrayListPost().size() - 1 != i || totalRecord != response.body().getTotalRecords())) {
                                                arrayList.add(null);
                                            }
                                        }
                                    }
                                    page = page + 1;
                                    setAdapter();
                                } else {
                                    isOver = true;
                                    try {
                                        adapterHome.hideProgressBar();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    errorMsg = getString(R.string.err_no_data_found);
                                    setEmpty();
                                }
                                if(page == 2) {
                                    getStories();
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
                }

                @Override
                public void onFailure(@NonNull Call<RespHomeList> call, @NonNull Throwable t) {
                    if (getActivity() != null) {
                        call.cancel();
                        errorMsg = getString(R.string.err_no_data_found);
                        isOver = true;
                        setEmpty();
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
            adapterHome = new AdapterHomePosts(getActivity(), arrayList, false, Constants.TAG_FROM_HOME);
            rv_home.setAdapter(adapterHome);
        } else {
            adapterHome.notifyDataSetChanged();
        }
        setEmpty();
    }

    private void setEmpty() {
        progressBar.setVisibility(View.GONE);
        srl_home.setRefreshing(false);
        if(!arrayList.isEmpty()) {
            rv_home.setVisibility(View.VISIBLE);
            cl_empty.setVisibility(View.GONE);
        } else {
            rv_home.setVisibility(View.GONE);
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

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onStoryUpload(EventStoryUpload eventStoryUpload) {
        try {
            if(eventStoryUpload.isStoryUploaded()) {
                getStories();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        GlobalBus.getBus().removeStickyEvent(eventStoryUpload);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onLikeChange(EventLike eventLike) {
        try {
            int pos = arrayList.indexOf(eventLike.getItemPost());
            adapterHome.notifyItemChanged(pos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        GlobalBus.getBus().removeStickyEvent(eventLike);
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
        if (adapterHome != null) {
            adapterHome.destroyNativeAds();
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        if(Constants.isFromStories) {
            Constants.isFromStories = false;
//            if(adapterStories != null) {
//                adapterStories.notifyDataSetChanged();
//            }
        }
        super.onResume();
    }
}