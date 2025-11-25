package com.example.socialmedia;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fragments.FragmentDownloads;
import com.example.utils.Methods;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class DownloadActivity extends AppCompatActivity {

    Methods methods;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager2 mViewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        methods = new Methods(this);
        methods.forceRTLIfSupported();

        findViewById(R.id.iv_download_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), getLifecycle());

        mViewPager = findViewById(R.id.vp_downloads);
        mViewPager.setOffscreenPageLimit(5);
        tabLayout = findViewById(R.id.tabs);

        mViewPager.setAdapter(mSectionsPagerAdapter);

        initTabs();

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void initTabs() {
        mViewPager.setAdapter(mSectionsPagerAdapter);

        new TabLayoutMediator(tabLayout, mViewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText(getString(R.string.images));
                    } else {
                        tab.setText(getString(R.string.videos));
                    }
                }).attach();
    }

    public static class SectionsPagerAdapter extends FragmentStateAdapter {

        SectionsPagerAdapter(FragmentManager fm, Lifecycle lifecycle) {
            super(fm, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return FragmentDownloads.newInstance(position);
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}