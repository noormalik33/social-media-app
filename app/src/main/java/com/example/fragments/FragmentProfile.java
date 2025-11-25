package com.example.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.adapters.AdapterLinks;
import com.example.apiservices.APIClient;
import com.example.apiservices.APIInterface;
import com.example.apiservices.RespUserList;
import com.example.items.ItemLinks;
import com.example.items.ItemUser;
import com.example.socialmedia.DownloadActivity;
import com.example.socialmedia.EmailVerificationActivity;
import com.example.socialmedia.FollowingActivity;
import com.example.socialmedia.MainActivity;
import com.example.socialmedia.MyPointsActivity;
import com.example.socialmedia.PostByTagActivity;
import com.example.socialmedia.ProfileEditActivity;
import com.example.socialmedia.R;
import com.example.socialmedia.SettingsPrivacyActivity;
import com.example.socialmedia.VerifyAccountActivity;
import com.example.utils.Constants;
import com.example.utils.Methods;
import com.example.utils.SharedPref;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hasankucuk.socialtextview.SocialTextView;
import com.squareup.picasso.Picasso;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentProfile extends Fragment {

    private Methods methods;
    private SharedPref sharedPref;
    ConstraintLayout cl_prof;
    MaterialButton btn_points;
    ImageView iv_prof, iv_verify_warning, iv_link;
    TextView tv_name, tv_email, tv_total_posts, tv_total_followers, tv_total_following, tv_prof_completed,
            tv_prof_edit, tv2, tv_links, tv_links_more;
    SocialTextView tv_bio;
    CircularProgressBar progressBar;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager2 mViewPager;
    private TabLayout tabLayout;
    SeekBar sb_completion;
    ItemUser itemUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        sharedPref = new SharedPref(getActivity());
        methods = new Methods(getActivity());

        cl_prof = rootView.findViewById(R.id.cl_prof);
        iv_prof = rootView.findViewById(R.id.iv_prof);
        iv_verify_warning = rootView.findViewById(R.id.iv_prof_email_verify);
        tv_name = rootView.findViewById(R.id.tv_prof_name);
        tv_email = rootView.findViewById(R.id.tv_prof_sub);
        tv_bio = rootView.findViewById(R.id.tv_prof_bio);
        tv_total_posts = rootView.findViewById(R.id.tv_prof_total_post);
        tv_total_followers = rootView.findViewById(R.id.tv_prof_total_followers);
        tv_total_following = rootView.findViewById(R.id.tv_prof_total_following);
        tv_prof_completed = rootView.findViewById(R.id.tv_prof_completed);
        tv_prof_edit = rootView.findViewById(R.id.tv_prof_edit);
        tv_links = rootView.findViewById(R.id.tv_prof_links);
        tv_links_more = rootView.findViewById(R.id.tv_prof_links_more);
        btn_points = rootView.findViewById(R.id.btn_prof_points);
        tv2 = rootView.findViewById(R.id.tv2);
        iv_link = rootView.findViewById(R.id.iv1);
        sb_completion = rootView.findViewById(R.id.sb_prof_completion);
        progressBar = rootView.findViewById(R.id.pb_prof);

        rootView.findViewById(R.id.tv_followers).setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), FollowingActivity.class);
            intent.putExtra("pos", 0);
            startActivity(intent);
        });
        tv_total_followers.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FollowingActivity.class);
            intent.putExtra("pos", 0);
            startActivity(intent);
        });

        rootView.findViewById(R.id.tv_following).setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), FollowingActivity.class);
            intent.putExtra("pos", 1);
            startActivity(intent);
        });
        tv_total_following.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FollowingActivity.class);
            intent.putExtra("pos", 1);
            startActivity(intent);
        });

        btn_points.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MyPointsActivity.class);
            intent.putExtra("item", itemUser);
            startActivity(intent);
        });

        iv_verify_warning.setOnClickListener(v -> methods.isLoggedAndVerified(false));

        tv_email.setOnClickListener(v -> methods.isLoggedAndVerified(false));

        tv_prof_edit.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileEditActivity.class);
            startActivity(intent);
        });

        tv_links.setOnClickListener(v -> {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW);
                String url = sharedPref.getLink1();
                if(!url.startsWith("https") && !url.startsWith("http") && !url.startsWith("wwww")) {
                    url = "https://"+url;
                }
                i.setData(Uri.parse(url));
                startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        tv_links_more.setOnClickListener(v -> openBottomSheetLinks());

        try {
            if(!sharedPref.getUserName().isEmpty() && !sharedPref.getUserName().equals("null")) {
                ((MainActivity) getActivity()).getSupportActionBar().setTitle(sharedPref.getUserName());
            } else {
                ((MainActivity) getActivity()).getSupportActionBar().setTitle(sharedPref.getName());
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        tv_bio.setLinkClickListener((linkedType, s) -> {
            switch (linkedType) {
                case HASHTAG -> {
                    Intent intent = new Intent(getActivity(), PostByTagActivity.class);
                    intent.putExtra("tag", s.replace("#",""));
                    startActivity(intent);
                }
                case URL -> {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(s));
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                case EMAIL -> {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:"+s)); // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
//                                                    case PHONE -> {
//                                                        Intent intent = new Intent(Intent.ACTION_DIAL);
//                                                        intent.setData(Uri.parse("tel:"+s));
//                                                        startActivity(intent);
//                                                    }
            }
        });

        getProfile();

        mSectionsPagerAdapter = new SectionsPagerAdapter(requireActivity().getSupportFragmentManager(), getLifecycle());

        mViewPager = rootView.findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(5);

        tabLayout = rootView.findViewById(R.id.tabs);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                menuInflater.inflate(R.menu.menu_profile, menu);

            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.item_more) {
                    openProfileOptionsDialog();
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        super.onViewCreated(view, savedInstanceState);
    }

    private void initTabs() {
        mViewPager.setAdapter(mSectionsPagerAdapter);

        new TabLayoutMediator(tabLayout, mViewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setIcon(R.drawable.ic_grid_fill);
                    } else {
                        tab.setIcon(R.drawable.ic_tab_fav);
                    }
                }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    tab.setIcon(R.drawable.ic_grid_fill);
                } else {
                    tab.setIcon(R.drawable.ic_fav_fill);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    tab.setIcon(R.drawable.ic_tab_grid);
                } else {
                    tab.setIcon(R.drawable.ic_tab_fav);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void getProfile() {
        if (methods.isNetworkAvailable()) {
            progressBar.setVisibility(View.VISIBLE);

            Call<RespUserList> call = APIClient.getClient().create(APIInterface.class).getProfile(methods.getAPIRequest(Constants.URL_PROFILE, "", "", "", "", "", "", "", "", "", "", sharedPref.getUserId(), ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespUserList> call, @NonNull Response<RespUserList> response) {
                    if (getActivity() != null) {
                        progressBar.setVisibility(View.GONE);
                        if (response.body() != null) {
                            if (response.body().getSuccess().equals("1")) {
                                if (response.body().getUserDetail().getStatus()==1) {
                                    if (!Constants.isUserFollowingChanged) {

                                        try {
                                            if (!sharedPref.getUserName().isEmpty() && !sharedPref.getUserName().equals("null")) {
                                                ((MainActivity) getActivity()).getSupportActionBar().setTitle(sharedPref.getUserName());
                                            } else {
                                                ((MainActivity) getActivity()).getSupportActionBar().setTitle(sharedPref.getName());
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        itemUser = response.body().getUserDetail();

                                        tv_name.setText(response.body().getUserDetail().getName());
                                        tv_email.setText(response.body().getUserDetail().getEmail());

                                        Picasso.get().load(response.body().getUserDetail().getImage()).placeholder(R.drawable.placeholder).into(iv_prof);
                                        cl_prof.setVisibility(View.VISIBLE);

                                        tv_total_posts.setText(String.valueOf(response.body().getUserDetail().getTotalPost()));
                                        tv_total_followers.setText(String.valueOf(response.body().getUserDetail().getTotalFollowers()));
                                        tv_total_following.setText(String.valueOf(response.body().getUserDetail().getTotalFollowing()));

                                        btn_points.setText("Total Points - " + response.body().getUserDetail().getTotalPoints());

                                        iv_verify_warning.setVisibility(sharedPref.getIsEmailVerified() ? View.GONE : View.VISIBLE);
                                        ((MainActivity) requireActivity()).iv_account_verified.setVisibility((!sharedPref.getIsAccountVerifyOn() || !itemUser.getIsAccountVerified()) ? View.GONE : View.VISIBLE);
                                        btn_points.setVisibility(sharedPref.getIsPointsOn() ? View.VISIBLE : View.GONE);

                                        sharedPref.setUserName(response.body().getUserDetail().getUsername());
                                        sharedPref.setName(response.body().getUserDetail().getName());
                                        sharedPref.setEmail(response.body().getUserDetail().getEmail());
                                        sharedPref.setUserMobile(response.body().getUserDetail().getMobile());
                                        sharedPref.setUserImage(response.body().getUserDetail().getImage());
                                        sharedPref.setProfileComplete(response.body().getUserDetail().getProfileCompleted());
                                        sharedPref.setGender(response.body().getUserDetail().getGender());
                                        sharedPref.setBirthdate(response.body().getUserDetail().getDateOfBirth());
                                        sharedPref.setAddress(response.body().getUserDetail().getAddress());
                                        sharedPref.setProfileShareUrl(response.body().getUserDetail().getShareUrl());
                                        sharedPref.setUserBio(response.body().getUserDetail().getUserBio());
                                        sharedPref.setProfilePrivacy(response.body().getUserDetail().getUserPrivacy());
                                        sharedPref.setIsEmailVerified(response.body().getUserDetail().getIsEmailVerified().equalsIgnoreCase("yes"));
                                        sharedPref.setLink1Title(response.body().getUserDetail().getLink1Title());
                                        sharedPref.setLink1(response.body().getUserDetail().getLink1());
                                        sharedPref.setLink2Title(response.body().getUserDetail().getLink2Title());
                                        sharedPref.setLink2(response.body().getUserDetail().getLink2());
                                        sharedPref.setLink3Title(response.body().getUserDetail().getLink3Title());
                                        sharedPref.setLink3(response.body().getUserDetail().getLink3());
                                        sharedPref.setLink4Title(response.body().getUserDetail().getLink4Title());
                                        sharedPref.setLink4(response.body().getUserDetail().getLink4());
                                        sharedPref.setLink5Title(response.body().getUserDetail().getLink5Title());
                                        sharedPref.setLink5(response.body().getUserDetail().getLink5());

                                        Constants.arrayListLinks.clear();
                                        if (!sharedPref.getLink1().isEmpty()) {
                                            Constants.arrayListLinks.add(new ItemLinks(sharedPref.getLink1Title(), sharedPref.getLink1()));
                                        }
                                        if (!sharedPref.getLink2().isEmpty()) {
                                            Constants.arrayListLinks.add(new ItemLinks(sharedPref.getLink2Title(), sharedPref.getLink2()));
                                        }
                                        if (!sharedPref.getLink3().isEmpty()) {
                                            Constants.arrayListLinks.add(new ItemLinks(sharedPref.getLink3Title(), sharedPref.getLink3()));
                                        }
                                        if (!sharedPref.getLink4().isEmpty()) {
                                            Constants.arrayListLinks.add(new ItemLinks(sharedPref.getLink4Title(), sharedPref.getLink4()));
                                        }
                                        if (!sharedPref.getLink5().isEmpty()) {
                                            Constants.arrayListLinks.add(new ItemLinks(sharedPref.getLink5Title(), sharedPref.getLink5()));
                                        }

                                        setLinks();
                                        setProfileCompletion();
                                        setBio();

                                        initTabs();
                                    } else {
                                        Constants.isUserFollowingChanged = false;
                                        tv_total_following.setText(String.valueOf(response.body().getUserDetail().getTotalFollowing()));
                                    }
                                } else {
                                    methods.showUserInvalidDialog(getString(R.string.err_invalid_user), getString(R.string.err_invalid_user_cannot_continue_using_app));
                                }
                            }
                        } else {
                            methods.showToast(getString(R.string.err_server_error));
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespUserList> call, @NonNull Throwable t) {
                    if (getActivity() != null) {
                        call.cancel();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    public static class SectionsPagerAdapter extends FragmentStateAdapter {

        SectionsPagerAdapter(FragmentManager fm, Lifecycle lifecycle) {
            super(fm, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return FragmentUserPost.newInstance(position);
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    private void setLinks() {
        if(sharedPref.getLink1().isEmpty()) {
            tv_links.setVisibility(View.GONE);
            tv_links_more.setVisibility(View.GONE);
            iv_link.setVisibility(View.GONE);
        } else {
            tv_links.setVisibility(View.VISIBLE);
            tv_links_more.setVisibility(View.VISIBLE);
            iv_link.setVisibility(View.VISIBLE);
            String moreText="";
            if(sharedPref.getLink2().isEmpty()) {
                moreText = "";
            } else if(sharedPref.getLink3().isEmpty()) {
                moreText = getString(R.string.and_1_more);
            } else if(sharedPref.getLink4().isEmpty()) {
                moreText = getString(R.string.and_2_more);
            } else if(sharedPref.getLink5().isEmpty()) {
                moreText = getString(R.string.and_3_more);
            } else {
                moreText = getString(R.string.and_4_more);
            }
            tv_links.setText(sharedPref.getLink1());
            tv_links_more.setText(" ".concat(moreText));
        }
    }

    private void setProfileCompletion(){
        if (sharedPref.getProfileComplete() != 100) {
            tv_prof_completed.setText(String.valueOf(sharedPref.getProfileComplete()).concat("%"));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        sb_completion.setProgress(sharedPref.getProfileComplete(),true);
                    } else {
                        sb_completion.setProgress(sharedPref.getProfileComplete());
                    }
                }
            },1000);
        } else {
            tv_prof_completed.setVisibility(View.GONE);
            sb_completion.setVisibility(View.GONE);
            tv_prof_edit.setVisibility(View.GONE);
            tv2.setVisibility(View.GONE);
        }
    }

    private void setBio() {
        if (!sharedPref.getUserBio().isEmpty()) {
//            String text = response.body().getUserDetail().getUserBio();
//            SpannableString spannableString = methods.createSpannableStringBio(tv_bio, text);
//
//            tv_bio.setMovementMethod(LinkMovementMethod.getInstance());
//            tv_bio.setText(spannableString);
//            tv_bio.setOnClickListener(view -> {
//                if (tv_bio.getMaxLines() != 3) {
//                    tv_bio.setMaxLines(3);
//                } else {
//                    tv_bio.setMaxLines(10);
//                }
//            });
            tv_bio.setText("");
            tv_bio.appendLinkText(sharedPref.getUserBio());
            tv_bio.setVisibility(View.VISIBLE);
        } else {
            tv_bio.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        if (tv_name != null && Constants.isProfileUpdate) {
            Constants.isProfileUpdate = false;
            tv_name.setText(sharedPref.getName());
            tv_email.setText(sharedPref.getEmail());
            tv_bio.setText("");
            tv_bio.appendLinkText(sharedPref.getUserBio());
            if (!sharedPref.getUserImage().isEmpty()) {
                Picasso.get()
                        .load(sharedPref.getUserImage())
                        .placeholder(R.drawable.placeholder)
                        .into(iv_prof);
            }

            try {
                if(!sharedPref.getUserName().isEmpty() && !sharedPref.getUserName().equals("null")) {
                    ((MainActivity) getActivity()).getSupportActionBar().setTitle(sharedPref.getUserName());
                } else {
                    ((MainActivity) getActivity()).getSupportActionBar().setTitle(sharedPref.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            setLinks();
            setProfileCompletion();
            setBio();
        }

        if (Constants.isEmailVerificationChanged) {
            Constants.isEmailVerificationChanged = false;
            iv_verify_warning.setVisibility(sharedPref.getIsEmailVerified() ? View.GONE : View.VISIBLE);
        }

        if (Constants.isUserFollowingChanged || Constants.isPointsUpdated) {
            Constants.isPointsUpdated = false;
            getProfile();
        }
        super.onResume();
    }

    private void openProfileOptionsDialog() {
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.layout_bottom_profile_options, null);

        BottomSheetDialog dialog = new BottomSheetDialog(requireActivity(), R.style.BottomSheetDialogStyle);
        dialog.setContentView(view);
        dialog.show();

        TextView tv_share = dialog.findViewById(R.id.tv_edit_prof_share);
        TextView tv_edit_prof = dialog.findViewById(R.id.tv_edit_prof);
        TextView tv_my_downloads = dialog.findViewById(R.id.tv_my_downloads);
        TextView tv_logout = dialog.findViewById(R.id.tv_logout);
        TextView tv_privacy = dialog.findViewById(R.id.tv_my_acc_sett_privacy);
        TextView tv_verify = dialog.findViewById(R.id.tv_edit_verify);
        ImageView iv6 = dialog.findViewById(R.id.iv6);

        assert iv6 != null;
        assert tv_verify != null;
        if(sharedPref.getIsAccountVerifyOn() && (itemUser == null || !itemUser.getIsAccountVerified())) {
            tv_verify.setVisibility(View.VISIBLE);
            iv6.setVisibility(View.VISIBLE);
        } else {
            tv_verify.setVisibility(View.GONE);
            iv6.setVisibility(View.GONE);
        }

        assert tv_edit_prof != null;
        tv_edit_prof.setOnClickListener(view1 -> {
            dialog.dismiss();
            Intent intent = new Intent(getActivity(), ProfileEditActivity.class);
            startActivity(intent);
        });

        assert tv_share != null;
        tv_share.setOnClickListener(view1 -> {
            dialog.dismiss();

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, sharedPref.getProfileShareUrl());
            startActivity(Intent.createChooser(sendIntent, getString(R.string.share)));
        });

        assert tv_my_downloads != null;
        tv_my_downloads.setOnClickListener(view1 -> {
            dialog.dismiss();
            Intent intent = new Intent(getActivity(), DownloadActivity.class);
            startActivity(intent);
        });

        assert tv_logout != null;
        tv_logout.setOnClickListener(view1 -> {
            dialog.dismiss();
            methods.openLogoutDialog();
        });

        assert tv_privacy != null;
        tv_privacy.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(getActivity(), SettingsPrivacyActivity.class);
            startActivity(intent);
        });

        tv_verify.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(getActivity(), VerifyAccountActivity.class);
            intent.putExtra("item",itemUser);
            startActivity(intent);
        });
    }

    private void openBottomSheetLinks() {
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.layout_bottom_links, null);

        BottomSheetDialog dialog = new BottomSheetDialog(requireActivity(), R.style.BottomSheetDialogStyle);
        dialog.setContentView(view);
        dialog.show();

        RecyclerView rv_links = dialog.findViewById(R.id.rv_links);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        assert rv_links != null;
        rv_links.setLayoutManager(llm);

        AdapterLinks adapterLinks = new AdapterLinks(getActivity(), Constants.arrayListLinks, false);
        rv_links.setAdapter(adapterLinks);
    }
}