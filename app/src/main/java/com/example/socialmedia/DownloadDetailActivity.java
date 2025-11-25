package com.example.socialmedia;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.adapters.AdapterDownloadImageDetailPager;
import com.example.adapters.AdapterDownloadVideoDetailPager;
import com.example.utils.Constants;
import com.example.utils.Methods;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

public class DownloadDetailActivity extends AppCompatActivity {

    Methods methods;
    ViewPager2 viewPager2;
    AdapterDownloadImageDetailPager adapterDownloadImageDetailPager;
    AdapterDownloadVideoDetailPager adapterDownloadVideoDetailPager;
    ArrayList<Uri> arrayList = new ArrayList<>();
    boolean isVideo = false;
    int oldPos = -1, currentPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        currentPos = getIntent().getIntExtra("pos", 0);

        isVideo = getIntent().getBooleanExtra("isvideo", false);

        methods = new Methods(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_details);
        toolbar.setTitle("");
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        arrayList.addAll(Constants.arrayListDownloads);

        viewPager2 = findViewById(R.id.vp_details);
        viewPager2.setOffscreenPageLimit(2);
        if (isVideo) {
            adapterDownloadVideoDetailPager = new AdapterDownloadVideoDetailPager(this, arrayList);
            viewPager2.setAdapter(adapterDownloadVideoDetailPager);

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
                        try {
                            if (adapterDownloadVideoDetailPager.getPlayer(previousIndex) != null) {
                                adapterDownloadVideoDetailPager.getPlayerView(position).setVisibility(View.GONE);
                                adapterDownloadVideoDetailPager.getPlayer(previousIndex).pause();
                                adapterDownloadVideoDetailPager.getPlayer(previousIndex).setPlayWhenReady(false);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    oldPos = position;

                    if (position != -1 && adapterDownloadVideoDetailPager.getPlayer(position) != null) {
                        adapterDownloadVideoDetailPager.getPlayerView(position).setVisibility(View.VISIBLE);
                        adapterDownloadVideoDetailPager.getPlayer(position).setPlayWhenReady(true);
                        adapterDownloadVideoDetailPager.getPlayer(position).play();
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    super.onPageScrollStateChanged(state);
                }
            });
        } else {
            adapterDownloadImageDetailPager = new AdapterDownloadImageDetailPager(this, arrayList);
            viewPager2.setAdapter(adapterDownloadImageDetailPager);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                viewPager2.setCurrentItem(currentPos);
            }
        }, 100);

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        if (isVideo) {
            try {
                adapterDownloadVideoDetailPager.getPlayer(viewPager2.getCurrentItem()).pause();
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
                adapterDownloadVideoDetailPager.destroyPlayers();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}