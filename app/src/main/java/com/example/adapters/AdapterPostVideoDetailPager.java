package com.example.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.text.SpannableString;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSourceFactory;
import androidx.media3.datasource.cache.Cache;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.LoadControl;
import androidx.media3.exoplayer.analytics.PlaybackStats;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.upstream.DefaultAllocator;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.apiservices.APIClient;
import com.example.apiservices.APIInterface;
import com.example.apiservices.RespSuccess;
import com.example.apiservices.RespView;
import com.example.eventbus.EventLike;
import com.example.eventbus.GlobalBus;
import com.example.interfaces.ActionDoneListener;
import com.example.interfaces.DoubleClickListener;
import com.example.interfaces.MoreOptionListener;
import com.example.items.ItemPost;
import com.example.items.ItemUser;
import com.example.socialmedia.MyApplication;
import com.example.socialmedia.ProfileActivity;
import com.example.socialmedia.R;
import com.example.utils.Constants;
import com.example.utils.DoubleClick;
import com.example.utils.Methods;
import com.example.utils.SharedPref;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@UnstableApi public class AdapterPostVideoDetailPager extends RecyclerView.Adapter<AdapterPostVideoDetailPager.ViewHolder> {

    Context context;
    Methods methods;
    ArrayList<ItemPost> arrayList;
    SparseArray<ExoPlayer> arrayPlayer = new SparseArray<>();
    SparseArray<PlayerView> arrayPlayerView = new SparseArray<>();
    boolean isUser = false;
    private final Cache cache;

    public AdapterPostVideoDetailPager(Context context, ArrayList<ItemPost> arrayList, boolean isUser) {
        this.context = context;
        this.arrayList = arrayList;
        this.isUser = isUser;
        methods = new Methods(context);
        cache = MyApplication.getSimpleCache();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_video, iv_comments, iv_user_image, iv_like, iv_more, iv_share, iv_acc_verified;
        MaterialButton btn_follow;
        LinearLayout ll_details;
        TextView tv_desc, tv_user_name, tv_total_like, tv_total_view, tv_total_comments;
        ProgressBar progressBar;
        LinearLayout ll_player;
        RecyclerView rv_tags;
        AdapterTags adapterTags;
        AppCompatSeekBar seekbar_video;
        Handler handlerSeekbar = new Handler(), handlerPlay = new Handler();
        View view_player_click;
        LinearLayout ll_player_pause;
        ImageView iv_play;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ll_details = itemView.findViewById(R.id.ll_post_details);
            iv_video = itemView.findViewById(R.id.iv_detail);
            btn_follow = itemView.findViewById(R.id.btn_status_follow);
            tv_desc = itemView.findViewById(R.id.tv_details_desc);
            tv_user_name = itemView.findViewById(R.id.tv_status_user_name);
            iv_user_image = itemView.findViewById(R.id.iv_status_prof);
            iv_comments = itemView.findViewById(R.id.iv_detail_comment);
            iv_like = itemView.findViewById(R.id.iv_detail_like);
            iv_share = itemView.findViewById(R.id.iv_detail_share);
            iv_more = itemView.findViewById(R.id.iv_detail_more);
            progressBar = itemView.findViewById(R.id.pb_detail);
            ll_player = itemView.findViewById(R.id.ll_player);
            tv_total_like = itemView.findViewById(R.id.tv_detail_total_like);
            tv_total_view = itemView.findViewById(R.id.tv_detail_total_views);
            tv_total_comments = itemView.findViewById(R.id.tv_detail_total_comments);
            seekbar_video = itemView.findViewById(R.id.seekbar_video);
            view_player_click = itemView.findViewById(R.id.view_player_click);
            ll_player_pause = itemView.findViewById(R.id.ll_player_pause);
            iv_play = itemView.findViewById(R.id.iv_player_play);
            iv_acc_verified = itemView.findViewById(R.id.iv_prof_account_verify);

            rv_tags = itemView.findViewById(R.id.rv_details_tags);
            FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(context);
            flexboxLayoutManager.setJustifyContent(JustifyContent.FLEX_START);
            rv_tags.setLayoutManager(flexboxLayoutManager);

            seekbar_video.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (b) {
                        try {
                            ((PlayerView) ll_player.getChildAt(0)).getPlayer().seekTo(i);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        public void seekUpdate() {
            try {
                seekbar_video.setProgress((int) ((PlayerView) ll_player.getChildAt(0)).getPlayer().getCurrentPosition());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private final Runnable updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                seekUpdate();

                // Schedule the next update after a short delay
                handlerSeekbar.postDelayed(this, 1000);
            }
        };

        private final Runnable runnablePlay = new Runnable() {
            @Override
            public void run() {
                ll_player_pause.setVisibility(View.GONE);
            }
        };

        private void setData(int pos) {
//            arrayPlayer.append(pos, getVPlayer(context));
//            arrayPlayerView.append(pos, getVideoSource(context));
        }
//        private void setVideoPath(ItemPost itemPost, int pos) {
//            if(playerView == null) {
//                player = new ExoPlayer.Builder(context).build();
//                player.addListener(new Player.Listener() {
//                    @Override
//                    public void onPlaybackStateChanged(int playbackState) {
//                        if (playbackState == Player.STATE_BUFFERING) {
//                            progressBar.setVisibility(View.VISIBLE);
//                        } else if (playbackState == Player.STATE_READY) {
//                            progressBar.setVisibility(View.GONE);
//                        }
//                    }
//
//                    @Override
//                    public void onPlayerError(PlaybackException error) {
//                        Player.Listener.super.onPlayerError(error);
//                        Toast.makeText(context, "Can't play this video", Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//                playerView = new StyledPlayerView(context);
//                playerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//                ll_player.addView(playerView);
//                playerView.setPlayer(player);
//
//                MediaItem mediaItem = MediaItem.fromUri(Uri.parse("https://download.pexels.com/vimeo/500956900/pexels-mikhail-nilov-6507676.mp4?width=1080"));
//                player.setMediaItem(mediaItem);
//                player.prepare();
//                player.play();
//
//                videoPreparedListener.onVideoPrepared(new ExoPlayerItem(player, 1));
//            } else {
//
//            }
//        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_post_video_details, parent, false);
        view.setTag("imagePager" + viewType);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        holder.setData(position);
//        holder.setVideoPath(arrayList.get(holder.getAbsoluteAdapterPosition()), position);
        holder.iv_acc_verified.setVisibility(new SharedPref(context).getIsAccountVerifyOn() && arrayList.get(position).getIsUserAccVerified() ? View.VISIBLE : View.GONE);

        SpannableString spannableString = methods.highlightHashtagsAndMentions(arrayList.get(holder.getAbsoluteAdapterPosition()).getCaptions(), R.color.white, R.color.white);
        holder.tv_desc.setText(spannableString);
        holder.tv_desc.setMovementMethod(new Methods.CustomLinkMovementMethod());

        holder.tv_user_name.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getUserName());
        holder.tv_total_like.setText(methods.formatNumber(arrayList.get(holder.getAbsoluteAdapterPosition()).getTotalLikes()));
        holder.tv_total_like.setOnClickListener(view -> {
            methods.openPostLikesUsersList(arrayList.get(holder.getAbsoluteAdapterPosition()).getPostID());
        });
        holder.tv_total_view.setText(methods.formatNumber(arrayList.get(holder.getAbsoluteAdapterPosition()).getTotalViews()));
        holder.tv_total_comments.setText(methods.formatNumber(arrayList.get(holder.getAbsoluteAdapterPosition()).getTotalComments()));

        if (arrayList.get(holder.getAbsoluteAdapterPosition()).isUserRequested()) {
            holder.btn_follow.setText(context.getString(R.string.requested));
        } else if (arrayList.get(holder.getAbsoluteAdapterPosition()).isUserFollowed()) {
            holder.btn_follow.setText(context.getString(R.string.unfollow));
        } else {
            holder.btn_follow.setText(context.getString(R.string.follow));
        }

        Glide.with(context).load(arrayList.get(holder.getAbsoluteAdapterPosition()).getVideoUrl()).into(holder.iv_video);
        Picasso.get()
                .load(arrayList.get(holder.getAbsoluteAdapterPosition()).getUserImage())
                .placeholder(R.drawable.placeholder)
                .into(holder.iv_user_image);

        if (arrayList.get(holder.getAbsoluteAdapterPosition()).isLiked()) {
            holder.iv_like.setColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN);
            holder.iv_like.setImageResource(R.drawable.ic_like_hover);
        } else {
            holder.iv_like.setColorFilter(null);
            holder.iv_like.setImageResource(R.drawable.ic_like);
        }

        holder.btn_follow.setVisibility(((new SharedPref(context).isLogged() && arrayList.get(holder.getAbsoluteAdapterPosition()).getUserId().equals(new SharedPref(context).getUserId())) || isUser) ? View.GONE : View.VISIBLE);

        holder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.openFollowUnFollowAlert(arrayList.get(holder.getAbsoluteAdapterPosition()).getUserId(), holder.btn_follow, null, new ActionDoneListener() {
                    @Override
                    public void onWorkDone(String success, boolean isDone, int position) {
//                        if (success.equals("1")) {
//                            if (isDone) {
//                                holder.btn_follow.setTextColor(ContextCompat.getColor(context, R.color.primary));
//                                holder.btn_follow.setStrokeColorResource(R.color.primary);
//                            } else {
//                                holder.btn_follow.setTextColor(ContextCompat.getColor(context, R.color.text_90));
//                                holder.btn_follow.setStrokeColorResource(R.color.button_follow_stroke);
//                            }
//                        }
                    }
                });
            }
        });

        if (holder.adapterTags == null && arrayList.get(holder.getAbsoluteAdapterPosition()).getTags() !=null) {
            ArrayList<String> arrayListTags = new ArrayList<>(Arrays.asList(arrayList.get(holder.getAbsoluteAdapterPosition()).getTags().split(",")));
            holder.adapterTags = new AdapterTags(context, arrayListTags);
            holder.rv_tags.setAdapter(holder.adapterTags);
        }

        holder.iv_comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.openCommentDialog(arrayList.get(holder.getAbsoluteAdapterPosition()));
            }
        });

        holder.iv_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (methods.isNetworkAvailable()) {
                    if (methods.isLoggedAndVerified(true)) {
                        methods.animateHeartButton(holder.iv_like);

                        holder.iv_like.setEnabled(false);
                        if (!arrayList.get(holder.getAbsoluteAdapterPosition()).isLiked()) {
                            holder.iv_like.setColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN);
                            holder.iv_like.setImageResource(R.drawable.ic_like_hover);
                            arrayList.get(holder.getAbsoluteAdapterPosition()).setLiked(true);
                        } else {
                            holder.iv_like.setColorFilter(null);
                            holder.iv_like.setImageResource(R.drawable.ic_like);
                            arrayList.get(holder.getAbsoluteAdapterPosition()).setLiked(false);
                        }

                        methods.getDoLike(arrayList.get(holder.getAbsoluteAdapterPosition()).getPostID(), new MoreOptionListener() {
                            @Override
                            public void onFavDone(String success, boolean isFav, int totalLikes) {
                                holder.iv_like.setEnabled(true);
                                holder.tv_total_like.setText(String.valueOf(totalLikes));
                                arrayList.get(holder.getAbsoluteAdapterPosition()).setTotalLikes(String.valueOf(totalLikes));
                                GlobalBus.getBus().postSticky(new EventLike(holder.getAbsoluteAdapterPosition(), arrayList.get(holder.getAbsoluteAdapterPosition()), true));
                            }

                            @Override
                            public void onUserPostDelete() {
                            }
                        });
                    }
                } else {
                    methods.showToast(context.getString(R.string.err_internet_not_connected));
                }
            }
        });

        holder.iv_share.setOnClickListener(view -> {
            methods.sharePost(arrayList.get(holder.getAbsoluteAdapterPosition()).getVideoUrl(), arrayList.get(holder.getAbsoluteAdapterPosition()).getShareUrl(), false);
        });

        holder.iv_more.setOnClickListener(v -> {
            methods.openMoreDialog(arrayList.get(holder.getAbsoluteAdapterPosition()), new MoreOptionListener() {
                @Override
                public void onFavDone(String success, boolean isFav, int total) {
                    arrayList.get(holder.getAbsoluteAdapterPosition()).setFavourite(isFav);
                    GlobalBus.getBus().postSticky(new EventLike(arrayList.get(holder.getAbsoluteAdapterPosition()), false));
                }

                @Override
                public void onUserPostDelete() {
                    openDeleteAlertDialog(holder.getAbsoluteAdapterPosition());
                }
            });
        });

        holder.tv_user_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("item_user", new ItemUser(arrayList.get(holder.getAbsoluteAdapterPosition()).getUserId(), arrayList.get(holder.getAbsoluteAdapterPosition()).getUserName(), arrayList.get(holder.getAbsoluteAdapterPosition()).getPostImage(), "","","","No"));
                context.startActivity(intent);
            }
        });

        holder.iv_user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("item_user", new ItemUser(arrayList.get(holder.getAbsoluteAdapterPosition()).getUserId(), arrayList.get(holder.getAbsoluteAdapterPosition()).getUserName(), arrayList.get(holder.getAbsoluteAdapterPosition()).getPostImage(), "","","","No"));
                context.startActivity(intent);
            }
        });

//        holder.tv_desc.setOnClickListener(view -> {
//            if (holder.tv_desc.getMaxLines() == 2) {
//                holder.rv_tags.setVisibility(View.VISIBLE);
//                holder.tv_desc.setMaxLines(1000);
//                methods.expand(holder.tv_desc, holder.rv_tags);
//            } else {
//                holder.rv_tags.setVisibility(View.GONE);
//                holder.tv_desc.setMaxLines(2);
//                methods.collapse(holder.tv_desc, holder.rv_tags);
//            }
//        });

//        holder.tv_desc.setOnClickListener(view -> {
//            if (holder.tv_desc.getMaxLines() == 2) {
//                holder.rv_tags.setVisibility(View.VISIBLE);
//                holder.tv_desc.setMaxLines(1000);
//                methods.expand(holder.ll_details, holder.rv_tags);
//            } else {
//                holder.rv_tags.setVisibility(View.GONE);
//                holder.tv_desc.setMaxLines(2);
//                methods.collapse(holder.ll_details, holder.rv_tags);
//            }
//        });
//
//        holder.images.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                holder.images.setVisibility(View.GONE);
//                holder.playerView.setVisibility(View.VISIBLE);
//                holder.player.play();
//            }
//        });

//        MediaItem mediaItem = MediaItem.fromUri(Uri.parse("https://download.pexels.com/vimeo/500956900/pexels-mikhail-nilov-6507676.mp4?width=1080"));
//        holder.player.setMediaItem(mediaItem);
//        holder.player.prepare();
//        holder.player.play();
    }

    @OptIn(markerClass = UnstableApi.class)
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {

//        holder.playerView.setPlayer(holder.player);
//        MediaItem mediaItem = MediaItem.fromUri(Uri.parse("https://download.pexels.com/vimeo/500956900/pexels-mikhail-nilov-6507676.mp4?width=1080"));
//        holder.player.setMediaItem(mediaItem);
//        holder.player.prepare();

        int adapterPosition = holder.getAbsoluteAdapterPosition();
//        String mediaitem = arrayList.get(adapterPosition).getVideoUrl();

        if (arrayPlayerView.get(adapterPosition) == null) {
//            String mediaitem = "https://jsoncompare.org/LearningContainer/SampleFiles/Video/MP4/sample-mp4-file.mp4";
            String mediaitem = arrayList.get(adapterPosition).getVideoUrl();
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "Your-App-Name"));
            CacheDataSource.Factory cacheDataSourceFactory = new CacheDataSource.Factory()
                    .setCache(cache)
                    .setUpstreamDataSourceFactory(dataSourceFactory)
                    .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);;
            MediaSource mediaSource = new ProgressiveMediaSource
                    .Factory(cacheDataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(mediaitem));
//get the existing instances of exoplayer and exoplayer view

            PlayerView playerView = getPlayerView(context, holder);
            ExoPlayer player = getVPlayer(context);

            player.setMediaSource(mediaSource, true);
            player.prepare();
            playerView.setPlayer(player);
            //add player view again to parent
            holder.ll_player.addView(playerView);
            if (adapterPosition == 0) {
                player.setPlayWhenReady(true);
            } else {
                playerView.setVisibility(View.GONE);
            }

            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == PlaybackStats.PLAYBACK_STATE_PLAYING) {
                        holder.seekbar_video.setMax((int) player.getDuration());
                    }
                    if (adapterPosition == 0 && !arrayList.get(adapterPosition).isViewed()) {
                        arrayList.get(adapterPosition).setViewed(true);
                        getDoView(adapterPosition);
                    }
                    holder.iv_video.setVisibility(View.GONE);
                    ExoPlayer.Listener.super.onPlaybackStateChanged(playbackState);
                }

                @Override
                public void onPlayerError(PlaybackException error) {
                    ExoPlayer.Listener.super.onPlayerError(error);
                }

                @Override
                public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                    if (playWhenReady && adapterPosition != 0 && !arrayList.get(adapterPosition).isViewed()) {
                        arrayList.get(adapterPosition).setViewed(true);
                        getDoView(adapterPosition);
                    }

                    if(playWhenReady){
                        holder.handlerPlay.removeCallbacks(holder.runnablePlay);
                        if (arrayPlayer.get(adapterPosition).isPlaying()) {
                            holder.ll_player_pause.setVisibility(View.GONE);
                            holder.iv_play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pause));
                        }

                    }
                    ExoPlayer.Listener.super.onPlayWhenReadyChanged(playWhenReady, reason);
                }

                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    if (isPlaying) {
                        holder.handlerSeekbar.removeCallbacks(holder.updateSeekBarRunnable);
                        holder.handlerSeekbar.postDelayed(holder.updateSeekBarRunnable, 1000);
                    } else {
                        holder.handlerSeekbar.removeCallbacks(holder.updateSeekBarRunnable);
                    }

                    if (!isPlaying && player.getCurrentPosition() >= player.getDuration()) {
                        holder.handlerSeekbar.removeCallbacks(holder.updateSeekBarRunnable);
                        try {
                            holder.iv_play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play));
                            holder.ll_player_pause.setVisibility(View.VISIBLE);
                            ((PlayerView)holder.ll_player.getChildAt(0)).getPlayer().seekTo(0);
                            ((PlayerView)holder.ll_player.getChildAt(0)).getPlayer().pause();
                            holder.seekbar_video.setProgress(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            arrayPlayer.append(adapterPosition, player);
            arrayPlayerView.append(adapterPosition, playerView);
        } else {
            PlayerView playerView = arrayPlayerView.get(adapterPosition);
            ExoPlayer player = arrayPlayer.get(adapterPosition);
            playerView.setPlayer(player);
//            player.setPlayWhenReady(true);
            //add player view again to parent
            holder.ll_player.removeAllViews();
            holder.ll_player.addView(playerView);
        }

        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        int adapterPosition = holder.getAbsoluteAdapterPosition();
        int indexOfChild = holder.ll_player.indexOfChild(arrayPlayerView.get(adapterPosition));
        if (indexOfChild >= 0) {

            holder.ll_player.removeViewAt(indexOfChild);
            arrayPlayerView.remove(adapterPosition);
            arrayPlayer.get(adapterPosition).release();
            arrayPlayer.remove(adapterPosition);
        }

        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @OptIn(markerClass = UnstableApi.class) private ExoPlayer getVPlayer(Context context) {
        LoadControl loadControl = new DefaultLoadControl.Builder()
                .setAllocator(new DefaultAllocator(true, 16))
                .setBufferDurationsMs(Constants.MIN_BUFFER_DURATION,
                        Constants.MAX_BUFFER_DURATION,
                        Constants.MIN_PLAYBACK_START_BUFFER,
                        Constants.MIN_PLAYBACK_RESUME_BUFFER)
                .setTargetBufferBytes(-1)
                .setPrioritizeTimeOverSizeThresholds(true).build();

        return new ExoPlayer.Builder(context)
                .setLoadControl(loadControl)
                .build();
    }

    @OptIn(markerClass = UnstableApi.class) private PlayerView getPlayerView(Context mContext, ViewHolder holder) {
//        val display =(mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
//        Point point = new Point();
//        display.getSize(point)

//        videoSurfaceDefaultHeight = point.x
//        screenDefaultHeight = point.y
        View view = LayoutInflater.from(context).inflate(R.layout.layout_exoplayer, null, false);
        PlayerView videoPlayerView = (PlayerView) view.getRootView();
        videoPlayerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        videoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
//        videoPlayerView.setUseController(false);
        videoPlayerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING);

//        holder.view_player_click.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//                    holder.handlerPlay.removeCallbacks(holder.runnablePlay);
//                    if (videoPlayerView.getPlayer().isPlaying()) {
//                        videoPlayerView.getPlayer().pause();
//                        holder.iv_play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play));
//                    } else {
//                        videoPlayerView.getPlayer().play();
//                        holder.iv_play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pause));
//                        holder.handlerPlay.postDelayed(holder.runnablePlay, 1500);
//                    }
//                    holder.ll_player_pause.setVisibility(View.VISIBLE);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });

        holder.view_player_click.setOnClickListener(new DoubleClick(new DoubleClickListener() {
            @Override
            public void onSingleClick(View view) {
                try {
                    holder.handlerPlay.removeCallbacks(holder.runnablePlay);
                    if (videoPlayerView.getPlayer().isPlaying()) {
                        videoPlayerView.getPlayer().pause();
                        holder.iv_play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play));
                    } else {
                        videoPlayerView.getPlayer().play();
                        holder.iv_play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pause));
                        holder.handlerPlay.postDelayed(holder.runnablePlay, 1500);
                    }
                    holder.ll_player_pause.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDoubleClick(View view) {
                if (methods.isNetworkAvailable()) {
                    if (methods.isLoggedAndVerified(true)) {
                        methods.animateHeartButton(holder.iv_like);

                        holder.iv_like.setEnabled(false);
                        if (!arrayList.get(holder.getAbsoluteAdapterPosition()).isLiked()) {
                            holder.iv_like.setColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN);
                            holder.iv_like.setImageResource(R.drawable.ic_like_hover);
                            arrayList.get(holder.getAbsoluteAdapterPosition()).setLiked(true);
                        } else {
                            holder.iv_like.setColorFilter(null);
                            holder.iv_like.setImageResource(R.drawable.ic_like);
                            arrayList.get(holder.getAbsoluteAdapterPosition()).setLiked(false);
                        }

                        methods.getDoLike(arrayList.get(holder.getAbsoluteAdapterPosition()).getPostID(), new MoreOptionListener() {
                            @Override
                            public void onFavDone(String success, boolean isFav, int totalLikes) {
                                holder.iv_like.setEnabled(true);
                                holder.tv_total_like.setText(String.valueOf(totalLikes));
                                arrayList.get(holder.getAbsoluteAdapterPosition()).setTotalLikes(String.valueOf(totalLikes));
                                GlobalBus.getBus().postSticky(new EventLike(holder.getAbsoluteAdapterPosition(), arrayList.get(holder.getAbsoluteAdapterPosition()), true));
                            }

                            @Override
                            public void onUserPostDelete() {
                            }
                        });
                    }
                } else {
                    methods.showToast(context.getString(R.string.err_internet_not_connected));
                }
            }
        }));

        return videoPlayerView;
    }

    public ExoPlayer getPlayer(int pos) {
        return arrayPlayer.get(pos);
    }

    public PlayerView getPlayerView(int pos) {
        return arrayPlayerView.get(pos);
    }

    public void pausePlayer(int pos) {
        arrayPlayer.get(pos).pause();
    }

    public void destroyPlayers() {
        for (int i = 0; i < arrayPlayer.size(); i++) {
            arrayPlayer.get(i).stop();
            arrayPlayer.get(i).release();
            arrayPlayerView.set(i,null);
        }
        arrayPlayer.clear();
        arrayPlayerView.clear();
    }

    private void getDoView(int pos) {
        if (methods.isNetworkAvailable()) {

            Call<RespView> call = APIClient.getClient().create(APIInterface.class).getDoView(methods.getAPIRequest(Constants.URL_VIEW_POST, arrayList.get(pos).getPostID(), "", "", "", "", "", "", "", "", "", new SharedPref(context).getUserId(), ""));

            call.enqueue(new Callback<RespView>() {
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

    @Override
    public int getItemViewType(int position) {
        return position;
    }



    private void openDeleteAlertDialog(int pos) {
        View view = ((Activity)context).getLayoutInflater().inflate(R.layout.layout_bottom_delete_ac, null);

        BottomSheetDialog dialog_delete = new BottomSheetDialog(context, R.style.BottomSheetDialogStyle);
        dialog_delete.setContentView(view);
        dialog_delete.show();

        MaterialButton btn_cancel = dialog_delete.findViewById(R.id.btn_del_ac_cancel);
        MaterialButton btn_delete = dialog_delete.findViewById(R.id.btn_del_ac_delete);
        btn_delete.getBackground().setTint(ContextCompat.getColor(context, R.color.delete));
        TextView tv1 = dialog_delete.findViewById(R.id.tv1);
        TextView tv2 = dialog_delete.findViewById(R.id.tv2);

        tv1.setText(context.getString(R.string.delete));
        tv2.setText(context.getString(R.string.sure_delete_post));

        btn_cancel.setOnClickListener(v -> dialog_delete.dismiss());

        btn_delete.setOnClickListener(view1 -> {
            dialog_delete.dismiss();
            getUserPostDelete(pos);
        });
    }

    public void getUserPostDelete(int position) {
        if (methods.isLoggedAndVerified(true)) {
            if (methods.isNetworkAvailable()) {

                Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getDeletePost(methods.getAPIRequest(Constants.URL_DELETE_POST, arrayList.get(position).getPostID(), "", "", "", "", "", "", "", "", "", new SharedPref(context).getUserId(), ""));
                call.enqueue(new Callback<RespSuccess>() {
                    @Override
                    public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                        if (response.body() != null) {
                            if (response.body().getSuccess() != null) {
                                if (response.body().getSuccess().equals("1")) {
                                    Constants.isUserPostDeleted = true;
                                    arrayList.remove(position);
                                    arrayPlayer.remove(position);
                                    arrayPlayerView.remove(position);
                                    notifyItemRemoved(position);
                                }
                                methods.showToast(response.body().getMessage());
                            } else {
                                methods.showToast(context.getString(R.string.err_server_error));
                            }
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