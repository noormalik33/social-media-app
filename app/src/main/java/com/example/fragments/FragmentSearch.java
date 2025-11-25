package com.example.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.socialmedia.R;
import com.example.utils.Methods;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;

public class FragmentSearch extends Fragment {

    private Methods methods;

    ViewPager2 viewPager2;
    FollowingPageAdapter followingPageAdapter;
    private TabLayout tabLayout;
    TextInputEditText et_search;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        methods = new Methods(getActivity());

        viewPager2 = rootView.findViewById(R.id.view_pager);
        tabLayout = rootView.findViewById(R.id.tabs);
        et_search = rootView.findViewById(R.id.et_search);

        followingPageAdapter = new FollowingPageAdapter(requireActivity());

        initTabs();

        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    try {
                        View view = getActivity().getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        et_search.setFocusableInTouchMode(false);
                        et_search.setFocusable(false);
                        et_search.setFocusableInTouchMode(true);
                        et_search.setFocusable(true);

                        FragmentSearchPost fragment = (FragmentSearchPost) requireActivity().getSupportFragmentManager().findFragmentByTag("f" + 0);
                        if (fragment != null) {
                            fragment.searchPosts(v.getText().toString());
                        }

                        FragmentSearchUsers fragment2 = (FragmentSearchUsers) requireActivity().getSupportFragmentManager().findFragmentByTag("f" + 1);
                        if (fragment2 != null) {
                            fragment2.searchUsers(v.getText().toString());
                        }
                    } catch (Exception ignored) {
                    }

                    return true;
                }
                return false;
            }
        });

        return rootView;
    }

    private void initTabs() {
        viewPager2.setAdapter(followingPageAdapter);

        new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText(getString(R.string.posts));
                    } else {
                        tab.setText(getString(R.string.accounts));
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
            if (position == 0) {
                return new FragmentSearchPost();
            } else {
                return new FragmentSearchUsers();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}