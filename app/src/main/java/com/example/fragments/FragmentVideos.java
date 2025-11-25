package com.example.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;
import androidx.viewpager2.widget.ViewPager2;

import com.example.adapters.AdapterPostVideoDetailPager;
import com.example.apiservices.APIClient;
import com.example.apiservices.APIInterface;
import com.example.apiservices.RespPostList;
import com.example.items.ItemPost;
import com.example.socialmedia.R;
import com.example.utils.Constants;
import com.example.utils.Methods;
import com.example.utils.SharedPref;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@OptIn(markerClass = UnstableApi.class)
public class FragmentVideos extends Fragment {

    Methods methods;
    ViewPager2 viewPager2;
    AdapterPostVideoDetailPager adapterPostVideoDetailPager;
    ArrayList<ItemPost> arrayList;
    int page = 1, totalItems = 0;
    private Boolean isOver = false, isScroll = false, isLoading = false;
    int oldPos = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_videos, container, false);

        methods = new Methods(getActivity());
        methods.forceRTLIfSupported();

        arrayList = new ArrayList<>();

        viewPager2 = rootView.findViewById(R.id.vp_videos);
        viewPager2.setOffscreenPageLimit(1);

        adapterPostVideoDetailPager = new AdapterPostVideoDetailPager(getActivity(), arrayList, false);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                int previousIndex;
                if (oldPos != -1) {
                    previousIndex = oldPos;

                    if (adapterPostVideoDetailPager.getPlayer(previousIndex) != null) {
                        adapterPostVideoDetailPager.getPlayerView(position).setVisibility(View.GONE);
                        adapterPostVideoDetailPager.getPlayer(previousIndex).pause();
                        adapterPostVideoDetailPager.getPlayer(previousIndex).setPlayWhenReady(false);
                    }
                }

                oldPos = position;

                if (position != -1 && adapterPostVideoDetailPager.getPlayer(position) != null) {
                    adapterPostVideoDetailPager.getPlayerView(position).setVisibility(View.VISIBLE);
                    adapterPostVideoDetailPager.getPlayer(position).setPlayWhenReady(true);
                    adapterPostVideoDetailPager.getPlayer(position).play();
                }

                View view = viewPager2.findViewWithTag("imagePager" + position);
                if (view != null) {
                    MaterialButton button = view.findViewById(R.id.btn_status_follow);
                    if (arrayList.get(position).isUserRequested()) {
                        button.setText(getString(R.string.requested));
                    } else if (arrayList.get(position).isUserFollowed()) {
                        button.setText(getString(R.string.unfollow));
                    } else {
                        button.setText(getString(R.string.follow));
                    }
                }

                if (totalItems != arrayList.size() && viewPager2.getCurrentItem() + 1 == arrayList.size()) {
                    if (!isOver && !isLoading) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isScroll = true;
                                totalItems = arrayList.size();
                                getPostList();
                            }
                        }, 0);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

        getPostList();

        return rootView;
    }

    private void getPostList() {
        if (methods.isNetworkAvailable()) {
            isLoading = true;
            Call<RespPostList> call = APIClient.getClient().create(APIInterface.class).getLatest(page, methods.getAPIRequest(Constants.URL_LATEST, "", "Video", "", "", "", "", "", "", "", "", new SharedPref(getActivity()).getUserId(), ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespPostList> call, @NonNull Response<RespPostList> response) {
                    if (getActivity() != null) {
                        if (response.body() != null) {
                            if (response.body().getArrayListPost() != null) {
                                if (!response.body().getArrayListPost().isEmpty()) {
                                    Collections.shuffle(response.body().getArrayListPost());
                                    arrayList.addAll(response.body().getArrayListPost());
                                    page = page + 1;
                                    setAdapter();
                                } else {
                                    isOver = true;
                                }
                            } else {
                                isOver = true;
                                methods.showToast(getString(R.string.err_server_error));
                            }
                        } else {
                            isOver = true;
                        }
                    }
                    isLoading = false;
                }

                @Override
                public void onFailure(@NonNull Call<RespPostList> call, @NonNull Throwable t) {
                    if (getActivity() != null) {
                        isOver = true;
                        isLoading = false;
                        call.cancel();
                    }
                }
            });
        } else {
            methods.showToast(getString(R.string.err_internet_not_connected));
        }
    }

    private void setAdapter() {
        if (!isScroll) {
            viewPager2.setAdapter(adapterPostVideoDetailPager);
        } else {
            adapterPostVideoDetailPager.notifyDataSetChanged();
        }
//        setEmpty();
    }

//    private void setEmpty() {
//        progressBar.setVisibility(View.GONE);
//        if(arrayList.size() > 0) {
//            rv_home.setVisibility(View.VISIBLE);
//            cl_empty.setVisibility(View.GONE);
//        } else {
//            rv_home.setVisibility(View.GONE);
//            tv_empty.setText(errorMsg);
//            cl_empty.setVisibility(View.VISIBLE);
//        }
//    }


    @Override
    public void onPause() {
        try {
            adapterPostVideoDetailPager.pausePlayer(viewPager2.getCurrentItem());
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        try {
            adapterPostVideoDetailPager.destroyPlayers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}