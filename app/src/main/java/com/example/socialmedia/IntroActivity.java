package blogtalk.com.socialmedia;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;
import com.google.android.material.button.MaterialButton;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

public class IntroActivity extends AppCompatActivity {

    private Methods method;
    private int[] layouts;
    private ViewPager viewPager;
    private MaterialButton button_skip, button_start;
    private MyViewPagerAdapter myViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        method = new Methods(this);
        method.forceRTLIfSupported();

        viewPager = findViewById(R.id.vp_intro);
        button_start = findViewById(R.id.btn_intro_start);
        button_skip = findViewById(R.id.btn_intro_skip);

        // layouts of all welcome sliders
        // add few more layouts if you want
        layouts = new int[]{R.layout.layout_intro_1, R.layout.layout_intro_2, R.layout.layout_intro_3};

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        DotsIndicator pageIndicatorView = findViewById(R.id.pageIndicatorView);
        pageIndicatorView.attachTo(viewPager);

        button_skip.setOnClickListener(v -> launchHomeScreen());

        button_skip.setOnClickListener(v -> {
            viewPager.setCurrentItem(myViewPagerAdapter.getCount() - 1);
        });

        button_start.setOnClickListener(v -> {
            launchHomeScreen();
        });
    }

    private void launchHomeScreen() {
        new SharedPref(IntroActivity.this).setIsIntroShown(true);
        startActivity(new Intent(IntroActivity.this, LoginActivity.class)
                .putExtra("isFromApp", false));
        finish();
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                button_skip.setVisibility(View.INVISIBLE);
                button_start.setVisibility(View.VISIBLE);
            } else {
                // still pages are left
                button_skip.setVisibility(View.VISIBLE);
                button_start.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    public class MyViewPagerAdapter extends PagerAdapter {

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            assert layoutInflater != null;
            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}