package com.example.socialmedia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapters.AdapterImages;
import com.example.interfaces.ClickListener;
import com.example.items.ItemMedia;
import com.example.utils.BackgroundTask;
import com.example.utils.Constants;
import com.example.utils.MediaStoreHelper;
import com.example.utils.Methods;
import com.example.utils.RecyclerItemClickListener;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class SelectStoryActivity extends AppCompatActivity {

    Methods methods;
    int selectedPos = 0;
    Spinner sp_folder;
    RecyclerView rv_images;
    AdapterImages adapterMedia;
    ArrayAdapter<String> adapterSpFolders;
    CircularProgressBar progressBar;
    ArrayList<String> arrayListFolders = new ArrayList<>();
    ArrayList<ItemMedia> arrayListMedia = new ArrayList<>();
    TextView tv_empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_story);

        methods = new Methods(this);
        methods.forceRTLIfSupported();

        findViewById(R.id.iv_back).setOnClickListener(v -> onBackPressed());

        progressBar = findViewById(R.id.progressBar);
        tv_empty = findViewById(R.id.tv_empty);

        rv_images = findViewById(R.id.rv_images);

        rv_images.setLayoutManager(new GridLayoutManager(this, 3));

        rv_images.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(SelectStoryActivity.this, AddStoryActivity.class);
                intent.putExtra("item", arrayListMedia.get(position).getMediaUrl());
                startActivity(intent);
            }
        }));

        new FastScrollerBuilder(rv_images)
                .useMd2Style()
                .build();

        sp_folder = findViewById(R.id.sp_folder);

        if (methods.checkPer()) {
            getFolders();
        }
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
                    arrayListMedia.addAll(MediaStoreHelper.getImagesFromFolder(SelectStoryActivity.this, arrayListFolders.get(sp_folder.getSelectedItemPosition())));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public void onPostExecute(Boolean isExecutionSuccess) {
                progressBar.setVisibility(View.GONE);
                if (adapterMedia == null) {
                    adapterMedia = new AdapterImages(SelectStoryActivity.this, arrayListMedia, true, new ClickListener() {
                        @Override
                        public void onClick(int position) {

                        }
                    });
                    rv_images.setAdapter(adapterMedia);
                } else {
                    adapterMedia.notifyDataSetChanged();
                }
            }
        }.execute();
    }

    public void getFolders() {
        arrayListFolders.clear();
        arrayListFolders.add(getString(R.string.recent));
        arrayListFolders.addAll(MediaStoreHelper.getImageFolders(SelectStoryActivity.this));

        if (adapterSpFolders == null) {
            adapterSpFolders = new ArrayAdapter<>(
                    this,
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
    protected void onResume() {
        if (Constants.isNewStoryAdded) {
            Constants.isNewStoryAdded = false;
            finish();
        }
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions.length > 0) {
            if (grantResults.length > 0 && grantResults[0] != -1) {
                getFolders();
            }
        }
    }
}