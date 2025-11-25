package com.example.socialmedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.fragments.FragmentUserFollow;
import com.example.utils.Methods;
import com.example.utils.SharedPref;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class FollowingActivity extends AppCompatActivity {

    Methods methods;
    ViewPager2 viewPager2;
    FollowingPageAdapter followingPageAdapter;
    int pos = 0;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);

        pos = getIntent().getIntExtra("pos", 0);

        methods = new Methods(this);
        methods.forceRTLIfSupported();

        findViewById(R.id.iv_follow_back).setOnClickListener(v -> onBackPressed());
        TextView tv_title = findViewById(R.id.tv_follow_title);
        tv_title.setText(new SharedPref(FollowingActivity.this).getName());

        viewPager2 = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tabs);

        followingPageAdapter = new FollowingPageAdapter(this);
        viewPager2.setAdapter(followingPageAdapter);
        viewPager2.setCurrentItem(pos);

        initTabs();

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);
    }

    private void initTabs() {
        viewPager2.setAdapter(followingPageAdapter);

        new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText(getString(R.string.followers));
                    } else {
                        tab.setText(getString(R.string.following));
                    }
                }).attach();
    }

    public static class FollowingPageAdapter extends FragmentStateAdapter {
        public FollowingPageAdapter(FragmentActivity fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return FragmentUserFollow.newInstance(position);
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}