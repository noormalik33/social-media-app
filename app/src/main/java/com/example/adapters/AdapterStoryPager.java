package com.example.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.apiservices.APIClient;
import com.example.apiservices.APIInterface;
import com.example.apiservices.RespFollowUserList;
import com.example.apiservices.RespSuccess;
import com.example.apiservices.RespView;
import com.example.chat.ChatDateUtils;
import com.example.interfaces.StoryListener;
import com.example.interfaces.UserListListener;
import com.example.items.ItemStories;
import com.example.items.ItemUser;
import com.example.socialmedia.ProfileActivity;
import com.example.socialmedia.R;
import com.example.utils.Constants;
import com.example.utils.DBHelper;
import com.example.utils.Methods;
import com.example.utils.SharedPref;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import jp.shts.android.storiesprogressview.StoriesProgressView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterStoryPager extends RecyclerView.Adapter<AdapterStoryPager.StoryViewHolder> {

    Context context;
    StoryListener storyListener;
    private final ArrayList<ItemStories> arrayListStories;
    Methods methods;
    SharedPref sharedPref;

    public AdapterStoryPager(Context context, ArrayList<ItemStories> arrayListStories, StoryListener storyListener) {
        this.context = context;
        this.storyListener = storyListener;
        this.arrayListStories = arrayListStories;
        methods = new Methods(context);
        sharedPref = new SharedPref(context);
    }

    public class StoryViewHolder extends RecyclerView.ViewHolder {
        public StoriesProgressView storiesProgressView;
        RoundedImageView ivUser, ivStory;
        TextView tvUser, tvDate, tvViews;
        CircularProgressBar progressBar;
        View viewPrevious, viewNext;
        ImageView iv_more, iv_acc_verified;
        LinearLayout ll_views;
        public boolean isStoryLoaded = false;

        StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            storiesProgressView = itemView.findViewById(R.id.storiesProgressView);
            ivStory = itemView.findViewById(R.id.story_image);
            ivUser = itemView.findViewById(R.id.iv_story_user);
            tvUser = itemView.findViewById(R.id.tv_story_name);
            tvDate = itemView.findViewById(R.id.tv_story_date);
            tvViews = itemView.findViewById(R.id.tv_story_views);
            progressBar = itemView.findViewById(R.id.pb_story);
            viewPrevious = itemView.findViewById(R.id.viewPrevious);
            viewNext = itemView.findViewById(R.id.viewNext);
            ll_views = itemView.findViewById(R.id.ll_stories_views);
            iv_more = itemView.findViewById(R.id.iv_story_more);
            iv_acc_verified = itemView.findViewById(R.id.iv_story_account_verify);
        }
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_story, parent, false);
        return new StoryViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        ItemStories story = arrayListStories.get(holder.getAbsoluteAdapterPosition());

        holder.storiesProgressView.destroy();
        holder.storiesProgressView.setStoriesCount(story.getArrayListStoryPost().size());
        holder.storiesProgressView.setStoryDuration(Constants.STORY_TIME);
        holder.storiesProgressView.setStoriesListener(new StoriesProgressView.StoriesListener() {
            @Override
            public void onNext() {
                if (story.getCurrentStoryPos() + 1 < story.getArrayListStoryPost().size()) {
                    story.setCurrentStoryPos(story.getCurrentStoryPos() + 1);
                    loadStoryImage(holder, story);
                }
            }

            @Override
            public void onPrev() {
                if (story.getCurrentStoryPos() - 1 >= 0) {
                    story.setCurrentStoryPos(story.getCurrentStoryPos() - 1);
                    loadStoryImage(holder, story);
                }
            }

            @Override
            public void onComplete() {
                storyListener.onNextStory();
            }
        });
        holder.storiesProgressView.startStories(story.getCurrentStoryPos());
        loadStoryImage(holder, story);

        Picasso.get()
                .load(story.getUserImage())
                .placeholder(R.drawable.ic_user)
                .into(holder.ivUser);

        holder.tvUser.setText(story.getUserName());
        holder.iv_acc_verified.setVisibility(new SharedPref(context).getIsAccountVerifyOn() && story.getIsUserAccVerified() ? View.VISIBLE : View.GONE);

        holder.viewNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.storiesProgressView.skip();
            }
        });

        holder.ivUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("item_user", new ItemUser(story.getUserID(), story.getUserName(), story.getUserImage()));
                context.startActivity(intent);
                holder.storiesProgressView.pause();
            }
        });

        holder.viewPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (story.getCurrentStoryPos() - 1 >= 0) {
                    holder.storiesProgressView.reverse();
                } else {
                    storyListener.onPreviousStory();
                }
            }
        });

        holder.ivStory.setOnTouchListener(new View.OnTouchListener() {
            private final Handler handler = new Handler();
            private boolean isLongPressed = false;

            private GestureDetector gestureDetector = new GestureDetector(holder.ivStory.getContext(), new GestureDetector.SimpleOnGestureListener() {
                private static final int SWIPE_THRESHOLD = 100;  // Minimum swipe distance
                private static final int SWIPE_VELOCITY_THRESHOLD = 100;  // Minimum swipe velocity

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();

                    if (Math.abs(diffX) < Math.abs(diffY)) { // Vertical swipe
                        if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffY < 0) {
                                if (sharedPref.getUserId().equals(story.getUserID())) {
                                    openUserViewBottomSheet(story.getArrayListStoryPost().get(story.getCurrentStoryPos()).getStoryID(), holder.getAbsoluteAdapterPosition(), holder.storiesProgressView);
                                }
                                return true;
                            } else if (diffY > 0) {
                                ((Activity) context).finish();
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        handler.postDelayed(() -> {
                            holder.storiesProgressView.pause();
                            isLongPressed = true;
                        }, 400);
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (isLongPressed) {
                            holder.storiesProgressView.resume();
                            isLongPressed = false;
                        }
                        handler.removeCallbacksAndMessages(null);
                        return true;

                    case MotionEvent.ACTION_CANCEL:
                        handler.removeCallbacksAndMessages(null);
                        isLongPressed = false;
                        return true;
                }
                return false;
            }
        });

        if (story.getCurrentStoryPos() + 1 < story.getArrayListStoryPost().size()) {
            preloadNext(story.getArrayListStoryPost().get(story.getCurrentStoryPos() + 1).getPostImage());
        }
    }

    private void preloadNext(String image) {
        Glide.with(context)
                .load(image)
                .preload();
    }

    private void loadStoryImage(@NonNull StoryViewHolder holder, @NonNull ItemStories story) {
        holder.isStoryLoaded = false;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!holder.isStoryLoaded) {
                    holder.storiesProgressView.pause(); // Pause progression while loading
                }
            }
        }, 50);
        int currentStoryPos = story.getCurrentStoryPos(); // Lock the position

        holder.tvDate.setText(ChatDateUtils.getStoryTime(story.getArrayListStoryPost().get(currentStoryPos).getDate()));
        holder.progressBar.setVisibility(View.VISIBLE);
        if (sharedPref.getUserId().equals(story.getUserID())) {
            holder.ll_views.setVisibility(View.VISIBLE);
            holder.iv_more.setVisibility(View.VISIBLE);
            holder.tvViews.setText(story.getArrayListStoryPost().get(currentStoryPos).getTotalViews());

            holder.ll_views.setOnClickListener(view -> {
                openUserViewBottomSheet(story.getArrayListStoryPost().get(currentStoryPos).getStoryID(), holder.getAbsoluteAdapterPosition(), holder.storiesProgressView);
            });
            holder.iv_more.setOnClickListener(view -> {
                openDeleteBottomSheet(holder.getAbsoluteAdapterPosition(), holder.storiesProgressView);
            });
        } else {
            holder.ll_views.setVisibility(View.GONE);
            holder.iv_more.setVisibility(View.GONE);
        }

        Glide.with(holder.itemView.getContext())
                .load(story.getArrayListStoryPost().get(currentStoryPos).getPostImage())
                .addListener(new RequestListener<>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        holder.isStoryLoaded = true;
                        // Resume progression only if the position hasn't changed
//                        if (story.getCurrentStoryPos() == currentStoryPos) {
                        holder.storiesProgressView.resume();
                        getStoryView(story.getArrayListStoryPost().get(currentStoryPos).getStoryID(), story.getUserID());
                        new DBHelper(context).setStorySeen(story.getArrayListStoryPost().get(currentStoryPos).getStoryID());
                        return false;
                    }
                })
                .into(holder.ivStory);

        // Preload the next story if available
        if (currentStoryPos + 1 < story.getArrayListStoryPost().size()) {
            preloadNext(story.getArrayListStoryPost().get(currentStoryPos + 1).getPostImage());
        }
    }

    public void resetStoryProgressBars(int position, ViewPager2 viewPager) {
        ItemStories story = arrayListStories.get(position);
        StoryViewHolder holder = getViewHolderAtPosition(position, viewPager);

        if (holder != null) {
            // Reset and restart the progress bar efficiently
            holder.storiesProgressView.destroy();
            holder.storiesProgressView.setStoriesCount(story.getArrayListStoryPost().size());
            holder.storiesProgressView.setStoryDuration(Constants.STORY_TIME);
        }
    }

    public StoryViewHolder getViewHolderAtPosition(int position, ViewPager2 viewPager) {
        RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);

        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder instanceof StoryViewHolder) {
            return (StoryViewHolder) viewHolder;
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return arrayListStories.size();
    }

    private void getStoryView(String storyID, String storyUserID) {
        if (methods.isNetworkAvailable() && !sharedPref.getUserId().equals(storyUserID) && !new DBHelper(context).isStorySeen(storyID)) {

            Call<RespView> call = APIClient.getClient().create(APIInterface.class).getStoryView(methods.getAPIRequest(Constants.URL_STORY_VIEW, storyID, "", "", "", "", "", "", "", "", "", sharedPref.getUserId(), ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespView> call, @NonNull Response<RespView> response) {
                }

                @Override
                public void onFailure(@NonNull Call<RespView> call, @NonNull Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void openUserViewBottomSheet(String storyID, int arrayPos, StoriesProgressView storiesProgressView) {
        storiesProgressView.pause();

        @SuppressLint("InflateParams") View view = ((Activity) context).getLayoutInflater().inflate(R.layout.layout_bottom_user_list, null);

        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.BottomSheetDialogStyle);
        dialog.setContentView(view);
        dialog.show();
        dialog.setOnDismissListener(dialogInterface -> storiesProgressView.resume());

        RecyclerView rv_user_list = dialog.findViewById(R.id.rv_user_list);
        TextView tv_views = dialog.findViewById(R.id.tv_views);
        CircularProgressBar pb = dialog.findViewById(R.id.pb);
        TextView tv_empty = dialog.findViewById(R.id.tv_empty);
        assert rv_user_list != null;
        assert tv_views != null;
        assert pb != null;
        assert tv_empty != null;

        rv_user_list.setLayoutManager(new LinearLayoutManager(context));

        tv_views.setText(context.getString(R.string.views, arrayListStories.get(arrayPos).getArrayListStoryPost().get(arrayListStories.get(arrayPos).getCurrentStoryPos()).getTotalViews()));

        getUserViewList(storyID, (success, arrayListUser, totalRecords) -> {
            pb.setVisibility(View.GONE);
            if (success.equals("1")) {
                if(!arrayListUser.isEmpty()) {
                    tv_views.setText(context.getString(R.string.views, String.valueOf(totalRecords)));
                    arrayListStories.get(arrayPos).getArrayListStoryPost().get(arrayListStories.get(arrayPos).getCurrentStoryPos()).setTotalViews(String.valueOf(totalRecords));

                    rv_user_list.setVisibility(View.VISIBLE);
                    AdapterUserFollowers adapterUserFollowers = new AdapterUserFollowers(((Activity) context), arrayListUser);
                    rv_user_list.setAdapter(adapterUserFollowers);
                } else {
                    tv_empty.setVisibility(View.VISIBLE);
                }
            } else {
                tv_empty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void openDeleteBottomSheet(int pos, StoriesProgressView storiesProgressView) {
        storiesProgressView.pause();
        @SuppressLint("InflateParams") View view = ((Activity) context).getLayoutInflater().inflate(R.layout.layout_bottom_story_more, null);

        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.BottomSheetDialogStyle);
        dialog.setContentView(view);
        dialog.show();

        TextView tv_delete = dialog.findViewById(R.id.tv_story_delete);
        assert tv_delete != null;
        tv_delete.setOnClickListener(view12 -> {
            dialog.dismiss();
            openDeleteAlertBottomSheet(pos, storiesProgressView);
        });
    }

    private void getUserViewList(String storyID, UserListListener userListListener) {
        if (methods.isNetworkAvailable()) {

            Call<RespFollowUserList> call = APIClient.getClient().create(APIInterface.class).getStoryUserViewList(methods.getAPIRequest(Constants.URL_STORY_USER_VIEW_LIST, storyID, "", "", "", "", "", "", "", "", "", sharedPref.getUserId(), ""));

            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespFollowUserList> call, @NonNull Response<RespFollowUserList> response) {
                    if (response.body() != null && response.body().getArrayListUser() != null) {
                        userListListener.onDataReceived("1", response.body().getArrayListUser(), response.body().getTotalRecords());
                    } else {
                        userListListener.onDataReceived("0", null, 0);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespFollowUserList> call, @NonNull Throwable t) {
                    userListListener.onDataReceived("0", null, 0);
                    call.cancel();
                }
            });
        }
    }

    private void openDeleteAlertBottomSheet(int pos, StoriesProgressView storiesProgressView) {
        storiesProgressView.pause();
        @SuppressLint("InflateParams") View view = ((Activity) context).getLayoutInflater().inflate(R.layout.layout_bottom_delete_ac, null);

        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.BottomSheetDialogStyle);
        dialog.setContentView(view);
        dialog.show();

        MaterialButton btn_delete = dialog.findViewById(R.id.btn_del_ac_delete);
        MaterialButton btn_cancel = dialog.findViewById(R.id.btn_del_ac_cancel);
        TextView tv1 = dialog.findViewById(R.id.tv1);
        TextView tv2 = dialog.findViewById(R.id.tv2);

        tv1.setText(context.getString(R.string.delete));
        tv2.setText(context.getString(R.string.sure_delete_story));

        assert btn_cancel != null;
        btn_cancel.setOnClickListener(view1 -> {
            storiesProgressView.resume();
            dialog.dismiss();
        });
        assert btn_delete != null;
        btn_delete.setOnClickListener(view1 -> {
            dialog.dismiss();
            getUserStoryDelete(pos, storiesProgressView);
        });
    }

    public void getUserStoryDelete(int position, StoriesProgressView storiesProgressView) {
        if (methods.isLoggedAndVerified(true)) {
            if (methods.isNetworkAvailable()) {

                Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getStoryDelete(methods.getAPIRequest(Constants.URL_STORY_DELETE, arrayListStories.get(position).getArrayListStoryPost().get(arrayListStories.get(position).getCurrentStoryPos()).getStoryID(), "", "", "", "", "", "", "", "", "", new SharedPref(context).getUserId(), ""));
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                        if (response.body() != null && response.body().getItemSuccess() != null) {
                                if (response.body().getItemSuccess().getSuccess().equals("1")) {
                                    arrayListStories.get(position).getArrayListStoryPost().remove(arrayListStories.get(position).getCurrentStoryPos());

                                    if(arrayListStories.get(position).getCurrentStoryPos() < arrayListStories.get(position).getArrayListStoryPost().size()-1) {
                                        arrayListStories.get(position).setCurrentStoryPos(arrayListStories.get(position).getCurrentStoryPos()+1);
                                    } else {
                                        arrayListStories.get(position).setCurrentStoryPos(arrayListStories.get(position).getCurrentStoryPos()-1);
                                    }
                                    notifyItemChanged(position);
                                }
                                methods.showToast(response.body().getItemSuccess().getMessage());
                        } else {
                            methods.showToast(context.getString(R.string.err_server_error));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<RespSuccess> call, @NonNull Throwable t) {
                        call.cancel();
                    }
                });
            } else {
                methods.showToast(context.getString(R.string.err_internet_not_connected));
            }
        }
    }
}