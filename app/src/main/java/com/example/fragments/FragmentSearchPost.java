package blogtalk.com.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import blogtalk.com.adapters.AdapterSearch;
import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespPostList;
import blogtalk.com.items.ItemPost;
import blogtalk.com.socialmedia.R;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.EndlessRecyclerViewScrollListener;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentSearchPost extends Fragment {

    private Methods methods;
    private SharedPref sharedPref;
    RecyclerView rv_post;
    AdapterSearch adapterHome;
    ArrayList<ItemPost> arrayList;
    GridLayoutManager gridLayoutManager;
    ConstraintLayout cl_empty;
    TextView tv_empty;
    CircularProgressBar progressBar;
    int page = 1, totalRecord = 0;
    private Boolean isOver = false, isScroll = false, isLoading = false;
    String searchText = "", errorMsg = "";

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
        gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (adapterHome.getItemViewType(position) >= 1000 || adapterHome.isHeader(position)) ? gridLayoutManager.getSpanCount() : 1;
            }
        });
        rv_post.setLayoutManager(gridLayoutManager);
        cl_empty = rootView.findViewById(R.id.cl_empty);
        tv_empty = rootView.findViewById(R.id.tv_empty);
        progressBar = rootView.findViewById(R.id.pb_search);

        EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!isOver && !isLoading) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isScroll = true;
                            getSearchPost();
                        }
                    }, 0);
                }
            }
            @Override
            public void onScrollStop() {}
        };
        rv_post.addOnScrollListener(endlessRecyclerViewScrollListener);

        getSearchPost();

        return rootView;
    }

    private void getSearchPost() {
        if (methods.isNetworkAvailable()) {
            isLoading = true;
            Call<RespPostList> call = APIClient.getClient().create(APIInterface.class).getSearch(page, methods.getAPIRequest(Constants.URL_SEARCH, "", "", "", searchText, "", "", "", "", "", "", sharedPref.getUserId(), ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespPostList> call, @NonNull Response<RespPostList> response) {
                    if (getActivity() != null) {
                        if (response.body() != null) {
                            if (response.body().getArrayListPost() != null) {
                                if (!response.body().getArrayListPost().isEmpty()) {
                                    totalRecord = totalRecord + response.body().getArrayListPost().size();

                                    for (int i = 0; i < response.body().getArrayListPost().size(); i++) {
                                        arrayList.add(response.body().getArrayListPost().get(i));

                                        if (Constants.isNativeAd || Constants.isCustomAdsSearch) {
                                            int abc = arrayList.lastIndexOf(null);
                                            if ((((arrayList.size() - (abc + 1)) % (!Constants.isCustomAdsSearch ? Constants.nativeAdShow : Constants.customAdSearchPos)) == 0) && (response.body().getArrayListPost().size() - 1 != i || totalRecord != response.body().getTotalRecords())) {
                                                arrayList.add(null);
                                            }
                                        }
                                    }
                                    page = page + 1;
                                    setAdapter(response.body().getArrayListPost().size());
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
                            } else {
                                isOver = true;
                                try {
                                    adapterHome.hideProgressBar();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                methods.showToast(getString(R.string.err_server_error));
                                setEmpty();
                            }
                        } else {
                            isOver = true;
                            try {
                                adapterHome.hideProgressBar();
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
                        isOver = true;
                        try {
                            adapterHome.hideProgressBar();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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

    private void setAdapter(int size) {
        if (!isScroll) {
            adapterHome = new AdapterSearch(getActivity(), arrayList);
            rv_post.setAdapter(adapterHome);
        } else {
            adapterHome.notifyItemRangeChanged(arrayList.size()-size, size);
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

    public void searchPosts(String search) {
        searchText = search;
        isOver = false;
        arrayList.clear();
        adapterHome.notifyDataSetChanged();

        progressBar.setVisibility(View.VISIBLE);
        page = 1;
        getSearchPost();
    }

    @Override
    public void onDestroy() {
        if (adapterHome != null) {
            adapterHome.destroyNativeAds();
        }
        super.onDestroy();
    }
}