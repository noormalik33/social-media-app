package com.example.items;


import androidx.media3.exoplayer.ExoPlayer;

public class ExoPlayerItem {
    public ExoPlayer exoPlayer;
    public int position;

    public ExoPlayerItem(ExoPlayer exoPlayer, int position) {
        this.exoPlayer = exoPlayer;
        this.position = position;
    }
}
