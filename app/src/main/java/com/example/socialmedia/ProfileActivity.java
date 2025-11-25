package blogtalk.com.socialmedia;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import blogtalk.com.adapters.AdapterLinks;
import blogtalk.com.adapters.AdapterUserPost;
import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespPostList;
import blogtalk.com.interfaces.FunctionListener;
import blogtalk.com.items.ItemChatList;
import blogtalk.com.items.ItemLinks;
import blogtalk.com.items.ItemPost;
import blogtalk.com.items.ItemUser;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.EndlessRecyclerViewScrollListener;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.hasankucuk.socialtextview.SocialTextView;
import com.hasankucuk.socialtextview.model.LinkedType;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private Methods methods;
    SharedPref sharedPref;
    APIInterface apiInterface;
    ConstraintLayout cl_prof;
    MaterialButton button_follow;
    ImageView iv_prof, iv_link;
    SocialTextView tv_bio;
    ImageView iv_chat, iv_acc_verify;
    TextView tv_title, tv_name, tv_total_posts, tv_total_followers, tv_total_following, tv_links, tv_links_more;
    RecyclerView rv_post;
    AdapterUserPost adapterUserPost;
    ArrayList<ItemPost> arrayList = new ArrayList<>();
    GridLayoutManager gridLayoutManager;
    CircularProgressBar progressBar;
    LinearLayout ll_empty;
    int page = 1, totalRecord = 0;
    private Boolean isOver = false, isScroll = false, isLoading = false;
    String errorMsg = "";
    ItemUser itemUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        itemUser = (ItemUser) getIntent().getSerializableExtra("item_user");
        apiInterface = APIClient.getClient().create(APIInterface.class);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_profile);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        methods = new Methods(ProfileActivity.this);
        sharedPref = new SharedPref(ProfileActivity.this);

        cl_prof = findViewById(R.id.cl_prof);
        iv_prof = findViewById(R.id.iv_prof);

        button_follow = findViewById(R.id.btn_prof_follow);
        tv_title = findViewById(R.id.tv_prof_title);
        tv_name = findViewById(R.id.tv_prof_name);
        tv_bio = findViewById(R.id.tv_prof_bio);
        tv_total_posts = findViewById(R.id.tv_prof_total_post);
        tv_total_followers = findViewById(R.id.tv_prof_total_followers);
        tv_total_following = findViewById(R.id.tv_prof_total_following);
        tv_links = findViewById(R.id.tv_prof_links);
        tv_links_more = findViewById(R.id.tv_prof_links_more);
        iv_link = findViewById(R.id.iv1);
        iv_chat = findViewById(R.id.iv_prof_chat);
        progressBar = findViewById(R.id.pb_prof);
        iv_acc_verify = findViewById(R.id.iv_prof_account_verify);

        findViewById(R.id.iv_profile_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

//        findViewById(R.id.tv_followers).setOnClickListener(view -> {
//            Intent intent = new Intent(ProfileActivity.this, FollowingActivity.class);
//            intent.putExtra("pos", 0);
//            startActivity(intent);
//        });
//        tv_total_followers.setOnClickListener(v->{
//            Intent intent = new Intent(ProfileActivity.this, FollowingActivity.class);
//            intent.putExtra("pos", 0);
//            startActivity(intent);
//        });
//
//        findViewById(R.id.tv_following).setOnClickListener(view -> {
//            Intent intent = new Intent(ProfileActivity.this, FollowingActivity.class);
//            intent.putExtra("pos", 1);
//            startActivity(intent);
//        });
//        tv_total_following.setOnClickListener(v->{
//            Intent intent = new Intent(ProfileActivity.this, FollowingActivity.class);
//            intent.putExtra("pos", 1);
//            startActivity(intent);
//        });


        rv_post = findViewById(R.id.rv_user_post);
        ll_empty = findViewById(R.id.ll_empty);

        gridLayoutManager = new GridLayoutManager(this, 3);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (adapterUserPost.getItemViewType(position) >= 1000 || adapterUserPost.isHeader(position)) ? gridLayoutManager.getSpanCount() : 1;
            }
        });
        rv_post.setLayoutManager(gridLayoutManager);

        rv_post.addOnScrollListener(new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
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
            public void onScrollStop() {}
        });

        tv_links.setOnClickListener(v -> {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW);
                String url = itemUser.getLink1();
                if(!url.startsWith("https") && !url.startsWith("http") && !url.startsWith("wwww")) {
                    url = "https://"+url;
                }
                i.setData(Uri.parse(url));
                startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        tv_links_more.setOnClickListener(v -> openBottomSheetLinks());

        methods.getPublicProfile(itemUser.getId(), new FunctionListener() {
            @Override
            public void getUserDetails(String success, ItemUser itemUsers) {
                if (success.equals("1")) {
                    itemUser = itemUsers;
                    if(!itemUser.getUsername().isEmpty() && !itemUser.getUsername().equals("null")) {
                        tv_title.setText(itemUser.getUsername());
                    } else {
                        tv_title.setText(itemUser.getName());
                    }
                    tv_name.setText(itemUser.getName());
                    Picasso.get().load(itemUser.getImage()).placeholder(R.drawable.placeholder).into(iv_prof);

                    button_follow.setText(itemUser.isUserRequested() ? getString(R.string.requested) : itemUser.isUserFollowed() ? getString(R.string.unfollow) : getString(R.string.follow));
                    button_follow.setVisibility(((sharedPref.isLogged() && itemUser.getId().equals(sharedPref.getUserId()))) ? View.GONE : View.VISIBLE);

                    iv_acc_verify.setVisibility((!sharedPref.getIsAccountVerifyOn() || !itemUser.getIsAccountVerified()) ? View.GONE : View.VISIBLE);

                    tv_total_posts.setText(String.valueOf(itemUser.getTotalPost()));
                    tv_total_followers.setText(String.valueOf(itemUser.getTotalFollowers()));
                    tv_total_following.setText(String.valueOf(itemUser.getTotalFollowing()));

                    if(!itemUser.getUserBio().isEmpty()) {
                        tv_bio.appendLinkText(itemUser.getUserBio());
                        tv_bio.setVisibility(View.VISIBLE);
                        tv_bio.setLinkClickListener(new SocialTextView.LinkClickListener() {

                            @Override
                            public void onLinkClicked(@NonNull LinkedType linkedType, @NonNull String s) {
                                switch (linkedType) {
                                    case HASHTAG -> {
                                        Intent intent = new Intent(ProfileActivity.this, PostByTagActivity.class);
                                        intent.putExtra("tag", s.replace("#",""));
                                        startActivity(intent);
                                    }
                                    case URL -> {
                                        try {
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse(s));
                                            startActivity(intent);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    case EMAIL -> {
                                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                                        intent.setData(Uri.parse("mailto:"+s)); // only email apps should handle this
                                        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                                        if (intent.resolveActivity(getPackageManager()) != null) {
                                            startActivity(intent);
                                        }
                                    }
//                                    case PHONE -> {
//                                        Intent intent = new Intent(Intent.ACTION_DIAL);
//                                        intent.setData(Uri.parse("tel:"+s));
//                                        startActivity(intent);
//                                    }
                                }
                            }
                        });
                    } else {
                        tv_bio.setVisibility(View.GONE);
                    }
                    if(itemUsers.getLink1().isEmpty()) {
                        tv_links.setVisibility(View.GONE);
                        tv_links_more.setVisibility(View.GONE);
                        iv_link.setVisibility(View.GONE);
                    } else {
                        tv_links.setVisibility(View.VISIBLE);
                        tv_links_more.setVisibility(View.VISIBLE);
                        iv_link.setVisibility(View.VISIBLE);
                        String moreText="";
                        if(itemUser.getLink2().isEmpty()) {
                            moreText = "";
                        } else if(itemUser.getLink3().isEmpty()) {
                            moreText = getString(R.string.and_1_more);
                        } else if(itemUser.getLink4().isEmpty()) {
                            moreText = getString(R.string.and_2_more);
                        } else if(itemUser.getLink5().isEmpty()) {
                            moreText = getString(R.string.and_3_more);
                        } else {
                            moreText = getString(R.string.and_4_more);
                        }
                        tv_links.setText(itemUser.getLink1());
                        tv_links_more.setText(" ".concat(moreText));
                    }

                    cl_prof.setVisibility(View.VISIBLE);

                    if((itemUser.getUserPrivacy().equals(Constants.TAG_PROFILE_PUBLIC) || itemUser.isUserFollowed())) {
                        getUserPost();
                        if(sharedPref.getIsChatOn() && sharedPref.isLogged() && !itemUser.getId().equals(sharedPref.getUserId())) {
                            iv_chat.setVisibility(View.VISIBLE);
                        }
                    } else {
                        errorMsg = getString(R.string.profile_is_private);
                        setEmpty();
                    }
                }
            }
        });

        tv_name.setText(itemUser.getName());
        Picasso.get().load(itemUser.getImage()).placeholder(R.drawable.placeholder).into(iv_prof);
        cl_prof.setVisibility(View.VISIBLE);

        tv_total_posts.setText(String.valueOf(itemUser.getTotalPost()));
        tv_total_followers.setText(String.valueOf(itemUser.getTotalFollowers()));
        tv_total_following.setText(String.valueOf(itemUser.getTotalFollowing()));

        button_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.openFollowUnFollowAlert(itemUser.getId(), button_follow, null, null);
            }
        });

        iv_chat.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
            intent.putExtra("name", itemUser.getName());
            intent.putExtra("id", itemUser.getId());
            intent.putExtra("item", new ItemChatList(itemUser.getId(), itemUser.getName(), itemUser.getImage(), false, false, false, itemUser.getIsAccountVerified()));
            startActivity(intent);
        });

//        getUserPost();
        onBackPressedEvent();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_more) {
            openProfileOptionsDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getUserPost() {
        if (methods.isNetworkAvailable()) {

            Call<RespPostList> call = APIClient.getClient().create(APIInterface.class).getUserPost(page, methods.getAPIRequest(Constants.URL_USER_POST, sharedPref.getUserId(), "", "", "", "", "", "", "", "", "", itemUser.getId(), ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespPostList> call, @NonNull Response<RespPostList> response) {
                    if (response.body() != null) {
                        if (response.body().getArrayListPost() != null) {
                            if (!response.body().getArrayListPost().isEmpty()) {

                                arrayList.addAll(response.body().getArrayListPost());
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
                            try {
                                adapterUserPost.hideProgressBar();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            methods.showToast(getString(R.string.err_server_error));
                            setEmpty();
                        }
                    } else {
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

                @Override
                public void onFailure(@NonNull Call<RespPostList> call, @NonNull Throwable t) {
                    call.cancel();
                    errorMsg = getString(R.string.err_no_data_found);
                    isOver = true;
                    setEmpty();
                    isLoading = false;
                }
            });
        } else {
            methods.showToast(getString(R.string.err_internet_not_connected));
            setEmpty();
        }
    }

    private void setAdapter() {
        if (!isScroll) {
            adapterUserPost = new AdapterUserPost(ProfileActivity.this, arrayList, false, position -> {
                Constants.arrayListPosts.clear();
                Constants.arrayListPosts.addAll(arrayList);
                Constants.arrayListPosts.remove(position);
                Constants.arrayListPosts.add(0, arrayList.get(position));

                Intent intent = new Intent(ProfileActivity.this, PostByUserListActivity.class);
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
        progressBar.setVisibility(View.GONE);
        if (arrayList.size() == 0) {
            ((TextView) ll_empty.findViewById(R.id.tv_empty_msg)).setText(errorMsg);
            ll_empty.setVisibility(View.VISIBLE);
            rv_post.setVisibility(View.GONE);
        } else {
            rv_post.setVisibility(View.VISIBLE);
            ll_empty.setVisibility(View.GONE);
        }
    }

    private void onBackPressedEvent() {
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(Constants.pushType.isEmpty()) {
                    finish();
                } else {
                    Constants.pushType = "";
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void openProfileOptionsDialog() {
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_profile_options, null);

        BottomSheetDialog dialog = new BottomSheetDialog(ProfileActivity.this, R.style.BottomSheetDialogStyle);
        dialog.setContentView(view);
        dialog.show();

        TextView tv_share = dialog.findViewById(R.id.tv_edit_prof_share);
        TextView tv_edit_prof = dialog.findViewById(R.id.tv_edit_prof);
        TextView tv_my_downloads = dialog.findViewById(R.id.tv_my_downloads);
        TextView tv_logout = dialog.findViewById(R.id.tv_logout);
        TextView tv_privacy = dialog.findViewById(R.id.tv_my_acc_sett_privacy);
        TextView tv_verify = dialog.findViewById(R.id.tv_edit_verify);

        tv_edit_prof.setVisibility(View.GONE);
        tv_my_downloads.setVisibility(View.GONE);
        tv_logout.setVisibility(View.GONE);
        tv_privacy.setVisibility(View.GONE);
        tv_verify.setVisibility(View.GONE);
        dialog.findViewById(R.id.iv1).setVisibility(View.GONE);
        dialog.findViewById(R.id.iv2).setVisibility(View.GONE);
        dialog.findViewById(R.id.iv3).setVisibility(View.GONE);
        dialog.findViewById(R.id.iv4).setVisibility(View.GONE);
        dialog.findViewById(R.id.iv5).setVisibility(View.GONE);
        dialog.findViewById(R.id.iv6).setVisibility(View.GONE);

        tv_share.setOnClickListener(view1 -> {
            dialog.dismiss();

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, itemUser.getShareUrl());
            startActivity(Intent.createChooser(sendIntent, getString(R.string.share)));
        });
    }

    private void openBottomSheetLinks() {
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.layout_bottom_links, null);

        BottomSheetDialog dialog = new BottomSheetDialog(ProfileActivity.this, R.style.BottomSheetDialogStyle);
        dialog.setContentView(view);
        dialog.show();

        ArrayList<ItemLinks> arrayListLinks = new ArrayList<>();
        if(!itemUser.getLink1().isEmpty()) {
            arrayListLinks.add(new ItemLinks(itemUser.getLink1Title(), itemUser.getLink1()));
        }
        if(!itemUser.getLink2().isEmpty()) {
            arrayListLinks.add(new ItemLinks(itemUser.getLink2Title(), itemUser.getLink2()));
        }
        if(!itemUser.getLink3().isEmpty()) {
            arrayListLinks.add(new ItemLinks(itemUser.getLink3Title(), itemUser.getLink3()));
        }
        if(!itemUser.getLink4().isEmpty()) {
            arrayListLinks.add(new ItemLinks(itemUser.getLink4Title(), itemUser.getLink4()));
        }
        if(!itemUser.getLink5().isEmpty()) {
            arrayListLinks.add(new ItemLinks(itemUser.getLink5Title(), itemUser.getLink5()));
        }


        RecyclerView rv_links = dialog.findViewById(R.id.rv_links);

        LinearLayoutManager llm = new LinearLayoutManager(ProfileActivity.this);
        rv_links.setLayoutManager(llm);

        AdapterLinks adapterLinks = new AdapterLinks(ProfileActivity.this, arrayListLinks, false);
        rv_links.setAdapter(adapterLinks);
    }
}