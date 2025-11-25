package com.example.socialmedia;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.adapters.AdapterStoryPager;
import com.example.interfaces.StoryListener;
import com.example.items.ItemStories;
import com.example.utils.Constants;

import java.util.ArrayList;

public class StoryActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private AdapterStoryPager adapterStoryPager;
    private final ArrayList<ItemStories> arrayListStories = new ArrayList<>();
    int pos = 0;
    boolean isFirstTimeLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        pos = getIntent().getIntExtra("pos", 0);

        arrayListStories.addAll(Constants.arrayListStories);

        viewPager = findViewById(R.id.view_pager);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
//
//                // this is to start the story progress when open activity
                if(!isFirstTimeLoad) {
                    new Handler().postDelayed(() -> {
                        adapterStoryPager.resetStoryProgressBars(position, viewPager);
                        ItemStories currentStory = arrayListStories.get(position);
                        RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
                        AdapterStoryPager.StoryViewHolder currentHolder = (AdapterStoryPager.StoryViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                        if (currentHolder != null) {
                            currentHolder.storiesProgressView.startStories(currentStory.getCurrentStoryPos());
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!currentHolder.isStoryLoaded) {
                                        currentHolder.storiesProgressView.pause();
                                    }
                                }
                            }, 10);
                        }
                    }, 100);
                } else {
                    isFirstTimeLoad = false;
                }
            }
        });

        adapterStoryPager = new AdapterStoryPager(StoryActivity.this, arrayListStories, new StoryListener() {
            @Override
            public void onNextStory() {
                if ((viewPager.getCurrentItem() + 1) < arrayListStories.size()) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                } else {
                    finish();
                }
            }

            @Override
            public void onPreviousStory() {
                if ((viewPager.getCurrentItem() - 1) >= 0) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                }
            }
        });
        viewPager.setAdapter(adapterStoryPager);
//        viewPager.setOffscreenPageLimit(arrayListStories.size());
        viewPager.setCurrentItem(pos, false);
        Constants.isFromStories = true;
    }
}