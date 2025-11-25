package blogtalk.com.socialmedia;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import blogtalk.com.eventbus.EventRequested;
import blogtalk.com.eventbus.GlobalBus;
import blogtalk.com.interfaces.InterAdListener;
import blogtalk.com.interfaces.MoreOptionListener;
import blogtalk.com.items.ItemPost;
import blogtalk.com.items.ItemUser;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;
import com.google.android.material.button.MaterialButton;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.webtoonscorp.android.readmore.ReadMoreTextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TextPostDetailActivity extends AppCompatActivity {

    Methods methods;
    ItemPost itemPost;
    MaterialButton btn_follow;
    RoundedImageView iv_back;
    TextView tv_user_name, tv_like, tv_comments, tv_date;
    ReadMoreTextView tv_text_post;
    ImageView iv_prof, iv_fav, iv_like, iv_more, iv_comment, iv_share, iv_acc_verified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_post_detail);

        itemPost = (ItemPost) getIntent().getSerializableExtra("item");
        if(itemPost == null) {
            if(!Constants.arrayListPosts.isEmpty()) {
                itemPost = Constants.arrayListPosts.get(0);
            } else {
                return;
            }
        }

        methods = new Methods(this, (position, type) -> {
            methods.sharePost(itemPost.getPostImage(),itemPost.getShareUrl(), itemPost.getPostType().equalsIgnoreCase("text"));
        });

        btn_follow = findViewById(R.id.btn_status_follow);
        tv_text_post = findViewById(R.id.tv_status_text);
        tv_user_name = findViewById(R.id.tv_status_user_name);
        iv_prof = findViewById(R.id.iv_status_prof);
        iv_fav = findViewById(R.id.iv_status_fav);
        iv_like = findViewById(R.id.iv_status_like);
        iv_more = findViewById(R.id.iv_status_more);
        iv_comment = findViewById(R.id.iv_status_comment);
        iv_share = findViewById(R.id.iv_status_share);
        tv_like = findViewById(R.id.tv_status_likes);
        tv_comments = findViewById(R.id.tv_status_comment);
        tv_date = findViewById(R.id.tv_status_date);
        iv_acc_verified = findViewById(R.id.iv_prof_account_verify);
        iv_back = findViewById(R.id.iv_back);

        iv_acc_verified.setVisibility(new SharedPref(this).getIsAccountVerifyOn() && itemPost.getIsUserAccVerified() ? View.VISIBLE : View.GONE);

        SpannableString spannableString = methods.highlightHashtagsAndMentions(itemPost.getCaptions(), R.color.text_medium, R.color.text_dark);
        tv_text_post.setText(spannableString);
        tv_text_post.setMovementMethod(new Methods.CustomLinkMovementMethod());

        tv_user_name.setText(itemPost.getUserName());
        tv_like.setText(methods.formatNumber(itemPost.getTotalLikes()));
        tv_comments.setText(methods.formatNumber(itemPost.getTotalComments()));
        tv_date.setText(itemPost.getDate());

        Picasso.get().load(itemPost.getUserImage())
                .placeholder(R.drawable.placeholder)
                .into(iv_prof);

        if (itemPost.isFavourite()) {
            iv_fav.setImageResource(R.drawable.ic_fav_hover);
            iv_fav.setColorFilter(ContextCompat.getColor(TextPostDetailActivity.this, R.color.primary), PorterDuff.Mode.SRC_IN);
        } else {
            iv_fav.setImageResource(R.drawable.ic_fav);
            iv_fav.setColorFilter(null);
        }


        btn_follow.setVisibility(((new SharedPref(TextPostDetailActivity.this).isLogged() && itemPost.getUserId().equals(new SharedPref(TextPostDetailActivity.this).getUserId()))) ? View.GONE : View.VISIBLE);

        if (itemPost.isUserRequested()) {
            btn_follow.setText(getString(R.string.requested));
        } else if (itemPost.isUserFollowed()) {
            btn_follow.setText(getString(R.string.unfollow));
        } else {
            btn_follow.setText(getString(R.string.follow));
        }

        if (itemPost.isLiked()) {
            iv_like.setImageResource(R.drawable.ic_like_hover);
            iv_like.setColorFilter(ContextCompat.getColor(TextPostDetailActivity.this, R.color.red), PorterDuff.Mode.SRC_IN);
        } else {
            iv_like.setImageResource(R.drawable.ic_like);
            iv_like.setColorFilter(null);
        }

        iv_back.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        iv_fav.setOnClickListener(view -> {
            if (methods.isNetworkAvailable()) {
                if (methods.isLoggedAndVerified(true)) {
                    iv_fav.setEnabled(false);
                    if (!itemPost.isFavourite()) {
                        iv_fav.setImageResource(R.drawable.ic_fav_hover);
                        iv_fav.setColorFilter(ContextCompat.getColor(TextPostDetailActivity.this, R.color.primary), PorterDuff.Mode.SRC_IN);
                        itemPost.setFavourite(true);
                    } else {
                        iv_fav.setImageResource(R.drawable.ic_fav);
                        iv_fav.setColorFilter(null);
                        itemPost.setFavourite(false);
                    }

                    methods.getDoFav(itemPost.getPostID(), null, new MoreOptionListener() {
                        @Override
                        public void onFavDone(String success, boolean isFav, int total) {
                            iv_fav.setEnabled(true);
                        }

                        @Override
                        public void onUserPostDelete() {
                        }
                    });
                }
            } else {
                methods.showToast(getString(R.string.err_internet_not_connected));
            }
        });

        iv_like.setOnClickListener(view -> {
            if (methods.isNetworkAvailable()) {
                if (methods.isLoggedAndVerified(true)) {
                    iv_like.setEnabled(false);
                    methods.animateHeartButton(iv_like);

                    if (!itemPost.isLiked()) {
                        iv_like.setImageResource(R.drawable.ic_like_hover);
                        iv_like.setColorFilter(ContextCompat.getColor(TextPostDetailActivity.this, R.color.red), PorterDuff.Mode.SRC_IN);
                        itemPost.setLiked(true);
                    } else {
                        iv_like.setImageResource(R.drawable.ic_like);
                        iv_like.setColorFilter(null);
                        itemPost.setLiked(false);
                    }

                    methods.getDoLike(itemPost.getPostID(), new MoreOptionListener() {
                        @Override
                        public void onFavDone(String success, boolean isFav, int totalLikes) {
//                                    if (success.equals("1")) {
                            itemPost.setTotalLikes(String.valueOf(totalLikes));
                            tv_like.setText(methods.formatNumber(itemPost.getTotalLikes()));
//                                    } else {
//                                    }
                            iv_like.setEnabled(true);
                        }

                        @Override
                        public void onUserPostDelete() {
                        }
                    });
                }
            } else {
                methods.showToast(getString(R.string.err_internet_not_connected));
            }
        });

        iv_more.setOnClickListener(view -> methods.openMoreDialog(itemPost, new MoreOptionListener() {
            @Override
            public void onFavDone(String success, boolean isFav, int total) {
                if (isFav) {
                    iv_fav.setImageResource(R.drawable.ic_fav_hover);
                    itemPost.setFavourite(true);
                } else {
                    iv_fav.setImageResource(R.drawable.ic_fav);
                    itemPost.setFavourite(false);
                }
            }

            @Override
            public void onUserPostDelete() {
//                        openDeleteAlertDialog(holder.getAbsoluteAdapterPosition());
            }
        }));

        iv_comment.setOnClickListener(view -> methods.openCommentDialog(itemPost));

        iv_share.setOnClickListener(view -> methods.showInter(0, "share"));

        btn_follow.setOnClickListener(view -> methods.openFollowUnFollowAlert(itemPost.getUserId(), btn_follow, null, null));

        tv_user_name.setOnClickListener(view -> {
            Intent intent = new Intent(TextPostDetailActivity.this, ProfileActivity.class);
            intent.putExtra("item_user", new ItemUser(itemPost.getUserId(), itemPost.getUserName(), itemPost.getPostImage(), "", "", "", "No"));
            startActivity(intent);
        });

        iv_prof.setOnClickListener(view -> {
            Intent intent = new Intent(TextPostDetailActivity.this, ProfileActivity.class);
            intent.putExtra("item_user", new ItemUser(itemPost.getUserId(), itemPost.getUserName(), itemPost.getPostImage(), "", "", "", "No"));
            startActivity(intent);
        });

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

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onFollowChange(EventRequested eventRequested) {
        GlobalBus.getBus().removeStickyEvent(eventRequested);
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
                if (Constants.pushType.isEmpty()) {
                    finish();
                } else {
                    Constants.pushType = "";
                    Intent intent = new Intent(TextPostDetailActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}