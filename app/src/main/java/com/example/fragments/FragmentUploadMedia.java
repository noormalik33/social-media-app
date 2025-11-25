package blogtalk.com.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.LoadControl;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.upstream.DefaultAllocator;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import blogtalk.com.adapters.AdapterImages;
import blogtalk.com.interfaces.ClickListener;
import blogtalk.com.items.ItemMedia;
import blogtalk.com.socialmedia.AddPostActivity;
import blogtalk.com.socialmedia.R;
import blogtalk.com.utils.BackgroundTask;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.MediaStoreHelper;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.RecyclerItemClickListener;
import blogtalk.com.utils.RecyclerViewDecoration;
import blogtalk.com.utils.SharedPref;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.naver.android.helloyako.imagecrop.view.ImageCropView;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class FragmentUploadMedia extends Fragment {

    private Methods methods;
    private SharedPref sharedPref;
    RecyclerView rv_images;
    AdapterImages adapterMedia;
    ImageCropView iv_selected;
    MaterialButton btn_next;
    ArrayList<ItemMedia> arrayListMedia = new ArrayList<>();
    CoordinatorLayout codl_main;
    TextView tv_empty;
    CircularProgressBar progressBar;
    PlayerView playerView;
    ExoPlayer player;
    int selectedPos = 0;
    ArrayList<String> arrayListFolders = new ArrayList<>();
    Spinner sp_folder;
    ArrayAdapter<String> adapterSpFolders;
    RelativeLayout rl_post;
    ImageView iv_resize;
    boolean isSquare = true;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_create_post, container, false);

        sharedPref = new SharedPref(getActivity());
        methods = new Methods(getActivity());
        methods.forceRTLIfSupported();
        rootView.findViewById(R.id.iv_about_back).setOnClickListener(v -> requireActivity().finish());

        AppBarLayout appBarLayout = rootView.findViewById(R.id.appBarLayout);

        playerView = rootView.findViewById(R.id.playerView);
        iv_selected = rootView.findViewById(R.id.iv_create_post);
        progressBar = rootView.findViewById(R.id.pb_add_post);
        btn_next = rootView.findViewById(R.id.btn_next);
        codl_main = rootView.findViewById(R.id.codl_create_post);
        tv_empty = rootView.findViewById(R.id.tv_empty_create_post);
        iv_resize = rootView.findViewById(R.id.iv_resize);

        player = getVPlayer(requireActivity());

        iv_selected.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true); // Disable parent interception
            return false;
        });

        rl_post = rootView.findViewById(R.id.rl_post);
        rl_post.setLayoutParams(new CollapsingToolbarLayout.LayoutParams(methods.getScreenWidth(), methods.getScreenWidth()));
        playerView.setLayoutParams(new CollapsingToolbarLayout.LayoutParams(methods.getScreenWidth(), methods.getScreenWidth()));
        rv_images = rootView.findViewById(R.id.rv_images);

        rv_images.setLayoutManager(new GridLayoutManager(requireActivity(), 3));
        rv_images.addItemDecoration(new RecyclerViewDecoration(0, 0, 0, 210));

        rv_images.addOnItemTouchListener(new RecyclerItemClickListener(requireActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(!adapterMedia.isMultiSelectMode()) {
                    appBarLayout.setExpanded(true);
                    setDisplayItem(position);
                }
            }
        }));

        appBarLayout.addOnOffsetChangedListener((appBarLayout1, i) -> {
            if(i==0) {
                iv_selected.invalidate();
            }
        });

        btn_next.setOnClickListener(v -> {
            try {
                if (!arrayListMedia.isEmpty()) {
                    if (arrayListMedia.get(selectedPos).getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                        if (arrayListMedia.get(selectedPos).getMediaDuration() / 1000 <= Constants.videoUploadDuration) {
                            if (arrayListMedia.get(selectedPos).getMediaSize() <= Constants.videoUploadSize) {
                                MediaMetadataRetriever mdr = new MediaMetadataRetriever();
                                mdr.setDataSource(requireActivity(), arrayListMedia.get(selectedPos).getMediaUrl());
                                Constants.selectedImage = mdr.getFrameAtTime();

                                Intent intent = new Intent(requireActivity(), AddPostActivity.class);
                                intent.putExtra("uri", arrayListMedia.get(selectedPos).getMediaUrl());
                                intent.putExtra("isvideo", true);
                                startActivity(intent);
                            } else {
                                methods.showToast(getString(R.string.video_large_select_small));
                            }
                        } else {
                            methods.showToast(getString(R.string.video_long_select_short));
                        }
                    } else {
                        Constants.arrayListSelectedImagesPath.clear();
                        Constants.arrayListSelectedImagesUri.clear();
                        if(adapterMedia.isMultiSelectMode() && !adapterMedia.getSelectedItems().isEmpty()) {
                            for (int i = 0; i < adapterMedia.getSelectedItems().size(); i++) {
                                Constants.arrayListSelectedImagesUri.add(adapterMedia.getSelectedItems().get(i).getMediaUrl());
                            }
                        }
                        Constants.selectedImage = iv_selected.getCroppedImage();

                        Intent intent = new Intent(requireActivity(), AddPostActivity.class);
                        intent.putExtra("uri", arrayListMedia.get(selectedPos).getMediaUrl());
                        intent.putExtra("isvideo", false);
                        startActivity(intent);
                    }
                } else {
                    methods.showToast(getString(R.string.err_no_data_found));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        new FastScrollerBuilder(rv_images)
                .useMd2Style()
                .build();

        sp_folder = rootView.findViewById(R.id.sp_folder);

        if (methods.checkPer()) {
            getFolders();
        }

        iv_selected.setAspectRatio(1,1);

        iv_resize.setOnClickListener(view -> {
            isSquare = !isSquare;
            if(isSquare) {
                iv_selected.setAspectRatio(1, 1);
            } else {
                iv_selected.setAspectRatio(3, 4);
            }
        });

        return rootView;
    }

    @OptIn(markerClass = UnstableApi.class)
    public void setDisplayItem(int position) {
        selectedPos = position;
        if (arrayListMedia.get(position).getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
            playerView.setVisibility(View.VISIBLE);
            iv_selected.setVisibility(View.GONE);

            Uri mediaitem = arrayListMedia.get(position).getMediaUrl();
            DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(requireActivity());
            MediaSource mediaSource = new ProgressiveMediaSource
                    .Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(mediaitem));

            player.setMediaSource(mediaSource, true);
            player.prepare();
            playerView.setPlayer(player);

        } else {
            try {
                player.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            iv_selected.setVisibility(View.VISIBLE);
            playerView.setVisibility(View.GONE);
            try {
                iv_selected.setImageFilePath(methods.getPathImage(arrayListMedia.get(position).getMediaUrl()));
            } catch (Exception ignore) {
            }
        }
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

    public void getData() {
        new BackgroundTask() {

            @Override
            public void onPreExecute() {
                arrayListMedia.clear();
                if (adapterMedia != null) {
                    adapterMedia.notifyDataSetChanged();
                }
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public boolean doInBackground() {
                try {
                    arrayListMedia.addAll(MediaStoreHelper.getAllMedia(requireActivity(), arrayListFolders.get(sp_folder.getSelectedItemPosition()), sharedPref.getUploadPostType()));
                } catch (Exception ignored) {
                }
                return false;
            }

            @Override
            public void onPostExecute(Boolean isExecutionSuccess) {
                progressBar.setVisibility(View.GONE);
                if (adapterMedia == null) {
                    adapterMedia = new AdapterImages(requireActivity(), arrayListMedia, false, new ClickListener() {
                        @Override
                        public void onClick(int position) {
                            setDisplayItem(position);
                        }
                    });
                    rv_images.setAdapter(adapterMedia);
                } else {
                    adapterMedia.notifyDataSetChanged();
                }
                if (!arrayListMedia.isEmpty()) {
                    setDisplayItem(0);
//                    codl_main.setVisibility(View.VISIBLE);
//                    tv_empty.setVisibility(View.GONE);
                } else {
//                    tv_empty.setText(getString(R.string.err_no_data_found));
//                    tv_empty.setVisibility(View.VISIBLE);
//                    codl_main.setVisibility(View.GONE);
                }
            }
        }.execute();
    }

    public void getFolders() {
        arrayListFolders.clear();
        arrayListFolders.add(getString(R.string.recent));
        arrayListFolders.addAll(MediaStoreHelper.getAllMediaFolders(requireActivity(), sharedPref.getUploadPostType()));

        if (adapterSpFolders == null) {
            adapterSpFolders = new ArrayAdapter<>(
                    requireActivity(),
                    R.layout.layout_spinner,
                    arrayListFolders
            );

            adapterSpFolders.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp_folder.setAdapter(adapterSpFolders);
            sp_folder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    getData();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });
        } else {
            adapterSpFolders.notifyDataSetChanged();
            sp_folder.setSelection(0);

            getData();
        }
    }

    @Override
    public void onResume() {
        if (Constants.isNewPostAdded) {
            requireActivity().finish();
        }

        try {
            iv_selected.invalidate();
        } catch (Exception ignored){}
        super.onResume();
    }

    @Override
    public void onPause() {
        if(player != null && player.isPlaying()) {
            player.pause();
        }
        super.onPause();
    }
}