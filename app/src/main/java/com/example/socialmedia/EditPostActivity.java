package com.example.socialmedia;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.example.apiservices.APIClient;
import com.example.apiservices.APIInterface;
import com.example.apiservices.RespUpdatePost;
import com.example.eventbus.EventUpdatePost;
import com.example.eventbus.GlobalBus;
import com.example.items.ItemPost;
import com.example.utils.Constants;
import com.example.utils.CountingRequestBody;
import com.example.utils.Methods;
import com.example.utils.SharedPref;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPostActivity extends AppCompatActivity {

    Methods methods;
    ItemPost itemPost;
    TextInputEditText et_caption;
    MaterialButton btn_upload, btn_change_thumb;
    ImageView iv_selected;
    Uri imageUri;
    String selectedImagePath = "";
    boolean isVideo = false;
    private int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        itemPost = (ItemPost) getIntent().getSerializableExtra("item");
        isVideo = itemPost.getPostType().equalsIgnoreCase("video");

        methods = new Methods(this);
        methods.forceRTLIfSupported();

        findViewById(R.id.iv_about_back).setOnClickListener(v -> onBackPressed());

        btn_upload = findViewById(R.id.btn_add_post_upload);
        btn_change_thumb = findViewById(R.id.btn_add_post_change_image);
        et_caption = findViewById(R.id.et_add_post_description);
        iv_selected = findViewById(R.id.iv_add_post);
        iv_selected.setMaxHeight(methods.getScreenWidth());

        btn_change_thumb.setVisibility(isVideo ? View.VISIBLE : View.GONE);

        btn_upload.setText(getString(R.string.update));

        Glide.with(EditPostActivity.this).load(Constants.selectedImage).into(iv_selected);

        btn_change_thumb.setOnClickListener(view -> {
            if (methods.checkPer()) {
                pickImage();
            }
        });

        btn_upload.setOnClickListener(view -> {
            if (et_caption.getText().toString().trim().isEmpty()) {
                methods.showToast(getString(R.string.write_captions));
            } else {
                getEditPost();
            }
        });

        Glide.with(EditPostActivity.this).load(itemPost.getPostImage()).into(iv_selected);
        et_caption.setText(itemPost.getCaptions());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    class LoadSaveImage extends AsyncTask<String, Boolean, Boolean> {
        String filePath;

        LoadSaveImage() {
        }

        @Override
        protected void onPreExecute() {
//            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            String fileName = System.currentTimeMillis() + ".jpeg";
            filePath = getExternalCacheDir() + File.separator + getResources().getString(R.string.upload) + File.separator + fileName;
            boolean success;
            if (!new File(filePath).exists()) {
                success = saveImage(Constants.selectedImage, fileName);
            } else {
                success = true;
            }

            if (success) {
                selectedImagePath = filePath;
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean s) {
//                progressDialog.dismiss();
            if (s) {
                getEditPost();
            } else {
                methods.showToast("error saving image");
            }
            super.onPostExecute(s);
        }
    }


    private boolean saveImage(Bitmap bitmap, String fileName) {
        File directory = new File(getExternalCacheDir() + File.separator + getResources().getString(R.string.upload));

        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, fileName);
        try {
            OutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void getEditPost() {
        if (methods.isNetworkAvailable()) {
            String userID = new SharedPref(EditPostActivity.this).getUserId();
            ProgressDialog progressDialog = new ProgressDialog(EditPostActivity.this);
            progressDialog.setTitle(getString(R.string.uploading_));
            progressDialog.setMessage(getString(R.string.uploading_image));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(100);
            progressDialog.setProgress(0);
            progressDialog.show();

            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

            if (!itemPost.getPostType().equalsIgnoreCase("text")) {
                builder.addFormDataPart("data", methods.getAPIRequest(Constants.URL_EDIT_POST, "", isVideo ? "Video" : "Image", "", et_caption.getText().toString(), itemPost.getPostID(), "", "", "", "", "", userID, ""));

                File fileImage = null;
                if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                    fileImage = new File(selectedImagePath);

                    CountingRequestBody countingRequestBody = new CountingRequestBody(RequestBody.create(MediaType.parse("image/*"), fileImage), new CountingRequestBody.Listener() {
                        @Override
                        public void onProgress(long bytesWritten, long contentLength) {
                            // Update your progress bar here
                            int progress = (int) ((bytesWritten / (float) contentLength) * 100);
//                    progressBar.setProgress(progress);
                            progressDialog.setProgress(progress);
                        }
                    });

                    builder.addFormDataPart("video_thumbnail", System.currentTimeMillis() + "", countingRequestBody);
                }
            } else {
                builder.addFormDataPart("data", methods.getAPIRequest(Constants.URL_EDIT_POST, "", "Text", "", et_caption.getText().toString(), itemPost.getPostID(), "", "", "", "", "", userID, ""));
            }

            RequestBody requestBody = builder.build();
            Call<RespUpdatePost> call = APIClient.getClient().create(APIInterface.class).getDoEditPost(requestBody);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespUpdatePost> call, @NonNull Response<RespUpdatePost> response) {
                    progressDialog.dismiss();
                    if (response.body() != null) {
                        if (response.body().getSuccess() != null) {
                            if (response.body().getSuccess().equals("true")) {
                                itemPost.setCaptions(et_caption.getText().toString());
                                itemPost.setPostImage(response.body().getItemUpdatePost().getImageUrl());

                                et_caption.setText("");

                                GlobalBus.getBus().postSticky(new EventUpdatePost(itemPost));
                                finish();
                            }
                            methods.showToast(response.body().getMessage());
                        } else {
                            methods.showToast(getString(R.string.err_server_error));
                        }
                    } else {
                        methods.showToast(getString(R.string.err_server_error));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespUpdatePost> call, @NonNull Throwable t) {
                    progressDialog.dismiss();
                    call.cancel();
                }


            });
        } else {
            methods.showToast(getString(R.string.err_internet_not_connected));
        }
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_image)), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            CropImageContractOptions cropImageContractOptions = new CropImageContractOptions(uri, new CropImageOptions());
            cropImageActivityResultLauncher.launch(cropImageContractOptions);
        }
    }

    public ActivityResultLauncher<CropImageContractOptions> cropImageActivityResultLauncher = registerForActivityResult(
            new CropImageContract(),
            new ActivityResultCallback<CropImageView.CropResult>() {
                @Override
                public void onActivityResult(CropImageView.CropResult result) {
                    if (result.isSuccessful()) {

                        imageUri = result.getUriContent();
                        selectedImagePath = result.getUriFilePath(EditPostActivity.this, true);

                        try {
                            Glide.with(EditPostActivity.this)
                                    .load(imageUri)
                                    .into(iv_selected);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {

                    }
                }
            });
}