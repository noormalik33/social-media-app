package blogtalk.compackage blogtalk.com.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.LoadControl;
import androidx.media3.exoplayer.analytics.PlaybackStats;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.upstream.DefaultAllocator;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import blogtalk.com.socialmedia.R;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.Methods;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterDownloadVideoDetailPager extends RecyclerView.Adapter<AdapterDownloadVideoDetailPager.ViewHolder> {

    Context context;
    Methods methods;
    ArrayList<Uri> arrayList;
    SparseArray<ExoPlayer> arrayPlayer = new SparseArray<>();
    SparseArray<PlayerView> arrayPlayerView = new SparseArray<>();

    public AdapterDownloadVideoDetailPager(Context context, ArrayList<Uri> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        methods = new Methods(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_video;
        ProgressBar progressBar;
        LinearLayout ll_player;
        AppCompatSeekBar seekbar_video;
        Handler handlerSeekbar = new Handler(), handlerPlay = new Handler();
        View view_player_click;
        LinearLayout ll_player_pause;
        ImageView iv_play;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_video = itemView.findViewById(R.id.iv_detail);
            progressBar = itemView.findViewById(R.id.pb_detail);
            ll_player = itemView.findViewById(R.id.ll_player);
            seekbar_video = itemView.findViewById(R.id.seekbar_video);
            view_player_click = itemView.findViewById(R.id.view_player_click);
            ll_player_pause = itemView.findViewById(R.id.ll_player_pause);
            iv_play = itemView.findViewById(R.id.iv_player_play);

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
                handlerSeekbar.postDelayed(this, 1000);
            }
        };

        private final Runnable runnablePlay = new Runnable() {
            @Override
            public void run() {
                ll_player_pause.setVisibility(View.GONE);
            }
        };
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_download_video_details, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picasso.get().load(arrayList.get(holder.getAbsoluteAdapterPosition())).into(holder.iv_video);
    }

    @OptIn(markerClass = UnstableApi.class)
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {

        int adapterPosition = holder.getAbsoluteAdapterPosition();

        if (arrayPlayerView.get(adapterPosition) == null) {

            Uri mediaitem = arrayList.get(adapterPosition);
            DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(context);
            MediaSource mediaSource = new ProgressiveMediaSource
                    .Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(mediaitem));

            PlayerView playerView = getPlayerView(holder);
            ExoPlayer player = getVPlayer(context);

            player.setMediaSource(mediaSource, true);
            player.prepare();
            playerView.setPlayer(player);

            holder.ll_player.addView(playerView);
            if (adapterPosition == 0) {
                player.setPlayWhenReady(true);
            } else {
                playerView.setVisibility(View.GONE);
            }

            player.addListener(new ExoPlayer.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == PlaybackStats.PLAYBACK_STATE_PLAYING) {
                        holder.seekbar_video.setMax((int) player.getDuration());
                    }
                    holder.iv_video.setVisibility(View.GONE);
                    Player.Listener.super.onPlaybackStateChanged(playbackState);
                }

                @Override
                public void onPlayerError(@NonNull PlaybackException error) {
                    Player.Listener.super.onPlayerError(error);
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
                            ((PlayerView) holder.ll_player.getChildAt(0)).getPlayer().seekTo(0);
                            ((PlayerView) holder.ll_player.getChildAt(0)).getPlayer().pause();
                            holder.seekbar_video.setProgress(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                    if (playWhenReady) {
                        holder.handlerPlay.removeCallbacks(holder.runnablePlay);
                        if (arrayPlayer.get(adapterPosition).isPlaying()) {
                            holder.ll_player_pause.setVisibility(View.GONE);
                            holder.iv_play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pause));
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
            player.setPlayWhenReady(true);
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

    @OptIn(markerClass = UnstableApi.class)
    private ExoPlayer getVPlayer(Context context) {
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

    @OptIn(markerClass = UnstableApi.class)
    private PlayerView getPlayerView(ViewHolder holder) {

        View view = LayoutInflater.from(context).inflate(R.layout.layout_exoplayer, null, false);
        PlayerView videoPlayerView = (PlayerView) view.getRootView();
        videoPlayerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        videoPlayerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING);


        holder.view_player_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });

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
            arrayPlayerView.set(i, null);
        }
        arrayPlayer.clear();
        arrayPlayerView.clear();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}