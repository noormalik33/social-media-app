package blogtalk.com.socialmedia;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.viewpager2.widget.ViewPager2;

import blogtalk.com.adapters.AdapterPostImageDetailPager;
import blogtalk.com.adapters.AdapterPostVideoDetailPager;
import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespPostList;
import blogtalk.com.apiservices.RespView;
import blogtalk.com.eventbus.EventRequested;
import blogtalk.com.eventbus.GlobalBus;
import blogtalk.com.items.ItemPost;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@OptIn(markerClass = UnstableApi.class)
public class PostDetailActivity extends AppCompatActivity {

    Methods methods;
    ViewPager2 viewPager2;
    AdapterPostVideoDetailPager adapterPostVideoDetailPager;
    AdapterPostImageDetailPager adapterPostImageDetailPager;
    ArrayList<ItemPost> arrayList = new ArrayList<>();
    //    ArrayList<ExoPlayerItem> exoPlayerItems;
    boolean isVideo = false, isUser = false;
    int oldPos = -1, currentPos = 0;
    int page = 1, totalItems = 0;
    private Boolean isOver = false, isLoading = true, isRelated = true, isFromNoti = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(0, 0, 0, 0);
            return insets;
        });

        isUser = getIntent().getBooleanExtra("isuser", false);
        currentPos = getIntent().getIntExtra("pos", 0);
        isFromNoti = getIntent().getBooleanExtra("isFromNoti", false);

        if(!Constants.arrayListPosts.isEmpty()) {
            isVideo = Constants.arrayListPosts.get(0).getPostType().equalsIgnoreCase("video");
        } else {
            Intent intent = new Intent(PostDetailActivity.this, MainActivity.class);
            startActivity(intent);
            return;
        }

        methods = new Methods(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_details);
        toolbar.setTitle("");
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        exoPlayerItems = new ArrayList<>();

        arrayList.addAll(Constants.arrayListPosts);

        viewPager2 = findViewById(R.id.vp_details);
        viewPager2.setOffscreenPageLimit(2);
        if (isVideo) {
            adapterPostVideoDetailPager = new AdapterPostVideoDetailPager(this, arrayList, isUser);
            viewPager2.setAdapter(adapterPostVideoDetailPager);

            viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }

                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);

                    try {
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    View view = viewPager2.findViewWithTag("imagePager" + position);
                    if (view != null) {
                        MaterialButton button = view.findViewById(R.id.btn_status_follow);
                        if (arrayList.get(position).isUserRequested()) {
                            button.setText(getString(R.string.requested));
                        } else if (arrayList.get(position).isUserFollowed()) {
                            button.setText(getString(R.string.unfollow));;
                        } else {
                            button.setText(getString(R.string.follow));
                        }
                    }

                    if(viewPager2.getCurrentItem()+1 == arrayList.size()) {
                        if (isRelated && !isLoading) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    totalItems = arrayList.size();
                                    getPostList();
                                }
                            }, 0);
                        } else if (!isOver && !isLoading) {
                            totalItems = arrayList.size();
                            getPostList();
                        }
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    super.onPageScrollStateChanged(state);
                }
            });
        } else {
            adapterPostImageDetailPager = new AdapterPostImageDetailPager(this, arrayList, isUser);
            viewPager2.setAdapter(adapterPostImageDetailPager);

            viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    getDoView(position);

                    View view = viewPager2.findViewWithTag("imagePager" + position);
                    if (view != null) {
                        MaterialButton button = view.findViewById(R.id.btn_status_follow);
                        if (arrayList.get(position).isUserRequested()) {
                            button.setText(getString(R.string.requested));
//                            button.setTextColor(ContextCompat.getColor(PostDetailActivity.this, R.color.primary));
//                            button.setStrokeColorResource(R.color.primary);
                        } else if (arrayList.get(position).isUserFollowed()) {
                            button.setText(getString(R.string.unfollow));
//                            button.setTextColor(ContextCompat.getColor(PostDetailActivity.this, R.color.primary));
//                            button.setStrokeColorResource(R.color.primary);
                        } else {
                            button.setText(getString(R.string.follow));
//                            button.setTextColor(ContextCompat.getColor(PostDetailActivity.this, R.color.white));
//                            button.setStrokeColorResource(com.wortise.ads.R.color.white);
                        }
                    }


                    if(viewPager2.getCurrentItem()+1 == arrayList.size()) {
                        if (isRelated && !isLoading) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    totalItems = arrayList.size();
                                    getPostList();
                                }
                            }, 0);
                        } else if (!isOver && !isLoading) {
                            totalItems = arrayList.size();
                            getPostList();
                        }
                    }
                }
            });
        }
////        if(isUser) {
//        viewPager2.setCurrentItem(currentPos);
////        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                viewPager2.setCurrentItem(currentPos);
            }
        },100);

        if (!isUser) {
            getPostList();
        }


        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);

        onBackPressedEvent();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getPostList() {
        if (methods.isNetworkAvailable()) {
            isLoading = true;
            Call<RespPostList> call;
            if(isRelated) {
                call = APIClient.getClient().create(APIInterface.class).getRelatedPost(page, methods.getAPIRequest(Constants.URL_RELATED_POST, arrayList.get(0).getPostID(), isVideo ? "video" : "image", "", "", "", "", "", "", "", "", new SharedPref(PostDetailActivity.this).getUserId(), ""));
            } else {
                call = APIClient.getClient().create(APIInterface.class).getLatest(page, methods.getAPIRequest(Constants.URL_LATEST, "", isVideo ? "video" : "image", "", "", "", "", "", "", "", "", new SharedPref(PostDetailActivity.this).getUserId(), ""));
            }
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespPostList> call, @NonNull Response<RespPostList> response) {
                    if (response.body() != null) {
                        if (response.body().getArrayListPost() != null) {
                            if (!response.body().getArrayListPost().isEmpty()) {
                                response.body().getArrayListPost().removeIf(itemPost -> itemPost.getPostID().equals(arrayList.get(0).getPostID()));
                                arrayList.addAll(response.body().getArrayListPost());

                                if(isRelated && arrayList.size()==1) {
                                    isRelated = false;
                                    getPostList();
                                }

                                page = page + 1;
                                setAdapter();
                            } else {
                                if(!isRelated) {
                                    isOver = true;
                                }
                                isRelated = false;
                                if(!isOver) {
                                    page = 1;
                                    getPostList();
                                }
                            }
                        } else {
                            methods.showToast(getString(R.string.err_server_error));
                        }
                    } else {
                        if(!isRelated) {
                            isOver = true;
                        }
                        isRelated = false;
                        if(!isOver) {
                            page = 1;
                            getPostList();
                        }
                    }
                    isLoading = false;
                }

                @Override
                public void onFailure(@NonNull Call<RespPostList> call, @NonNull Throwable t) {
                    call.cancel();
                    if(!isRelated) {
                        isOver = true;
                    }
                    isRelated = false;
                    if(!isOver) {
                        page = 1;
                        getPostList();
                    }
                    isLoading = false;
                }
            });
        } else {
            methods.showToast(getString(R.string.err_internet_not_connected));
        }
    }

    private void setAdapter() {
        if (isVideo) {
            adapterPostVideoDetailPager.notifyDataSetChanged();
        } else {
            adapterPostImageDetailPager.notifyDataSetChanged();
        }
    }

        private void getDoView(int pos) {
            if (methods.isNetworkAvailable()) {
    
                Call<RespView> call = APIClient.getClient().create(APIInterface.class).getDoView(methods.getAPIRequest(Constants.URL_VIEW_POST, arrayList.get(pos).getPostID(), "", "", "", "", "", "", "", "", "", new SharedPref(PostDetailActivity.this).getUserId(), ""));
    
                call.enqueue(new Callback<RespView>() {
                    @Override
                    public void onResponse(@NonNull Call<RespView> call, @NonNull Response<RespView> response) {
    //                    if (response.body() != null && response.body().getSuccess().equals("1") && response.body().getItemSuccess() != null) {
    //
    //                    } else {
    //
    //                    }
                    }
    
                    @Override
                    public void onFailure(@NonNull Call<RespView> call, @NonNull Throwable t) {
                        call.cancel();
                    }
                });
            }
        }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onFollowChange(EventRequested eventRequested) {
        try {
            if(Constants.isUserFollowingChanged) {
                Constants.isUserFollowingChanged = false;
                for (int i = 0; i < arrayList.size(); i++) {
                    if (eventRequested.getUserID().equals(arrayList.get(i).getUserId())) {
                        // if user profile is private it will request otherwise it will be directly followed

                        if(eventRequested.isRequested()) {
                            if(eventRequested.getType().equals("request")) {
                                arrayList.get(i).setUserRequested(true);
                            } else if(eventRequested.getType().equals("accept")) {
                                arrayList.get(i).setUserFollowed(true);
                            }
                        } else {
                            arrayList.get(i).setUserFollowed(false);
                        }

                        View view = viewPager2.findViewWithTag("imagePager" + i);
                        if (view != null) {
                            MaterialButton button = view.findViewById(R.id.btn_status_follow);
                            if (arrayList.get(i).isUserRequested()) {
                                button.setText(getString(R.string.requested));
                            } else if (arrayList.get(i).isUserFollowed()) {
                                button.setText(getString(R.string.unfollow));
                            } else {
                                button.setText(getString(R.string.follow));
                            }
                        }

//                        if(isVideo) {
//                            adapterPostVideoDetailPager.notifyItemChanged(i);
//                        } else {
//                            adapterPostImageDetailPager.notifyItemChanged(i);
//                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        GlobalBus.getBus().removeStickyEvent(eventFollow);
    }

    @Override
    protected void onPause() {
        if (isVideo) {
            try {
                adapterPostVideoDetailPager.getPlayer(viewPager2.getCurrentItem()).pause();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (isVideo) {
            try {
                adapterPostVideoDetailPager.destroyPlayers();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
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

    private void onBackPressedEvent() {
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(Constants.pushType.isEmpty() && !isFromNoti) {
                    finish();
                } else {
                    Constants.pushType = "";
                    Intent intent = new Intent(PostDetailActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}