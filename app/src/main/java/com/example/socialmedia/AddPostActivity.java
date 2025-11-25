package blogtalk.com.socialmedia;

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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import blogtalk.com.adapters.AdapterSelectedImages;
import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespSuccess;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.CountingRequestBody;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;
import blogtalk.com.utils.UploadService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPostActivity extends AppCompatActivity {

    Methods methods;
    TextInputEditText et_caption;
    MaterialButton btn_upload, btn_change_thumb;
    ImageView iv_selected;
    boolean isVideo = true;
    Uri videoUri, imageUri;
    String selectedVideoPath = "";
    boolean isVideoImageChanged = false;
    private int PICK_IMAGE_REQUEST = 1;
    ViewPager2 vp_add_post;
    AdapterSelectedImages adapterSelectedImages;
    ArrayList<String> arrayListImagePaths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        videoUri = getIntent().getParcelableExtra("uri");
        isVideo = getIntent().getBooleanExtra("isvideo", false);

        methods = new Methods(this);
        methods.forceRTLIfSupported();

        findViewById(R.id.iv_about_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        vp_add_post = findViewById(R.id.vp_add_post);
        btn_upload = findViewById(R.id.btn_add_post_upload);
        btn_change_thumb = findViewById(R.id.btn_add_post_change_image);
        et_caption = findViewById(R.id.et_add_post_description);
        iv_selected = findViewById(R.id.iv_add_post);

        vp_add_post.setClipToPadding(false);
        vp_add_post.setClipChildren(false);
        vp_add_post.setPadding(90, 0, 90, 0);

        btn_change_thumb.setVisibility(isVideo ? View.VISIBLE : View.GONE);

        btn_change_thumb.setOnClickListener(view -> {
            if (methods.checkPer()) {
                pickImage();
            }
        });

        btn_upload.setOnClickListener(view -> {
            if (et_caption.getText().toString().trim().isEmpty()) {
                methods.showToast(getString(R.string.write_captions));
            } else {
                if (!isVideoImageChanged) {
                    new LoadSaveImage().execute();
//                    methods.showToast("Upload is disabled in demo application");
                } else {
                    getAddPost();
                }
            }
        });

        if (!methods.getPerNotificationStatus()) {
            methods.openNotiPermissionDialog();
        }

        if (Constants.arrayListSelectedImagesUri.isEmpty()) {
            iv_selected.setVisibility(View.VISIBLE);
            vp_add_post.setVisibility(View.GONE);

            iv_selected.setMaxHeight(methods.getScreenWidth());
            Glide.with(AddPostActivity.this)
                    .asBitmap()
                    .load(Constants.selectedImage)
                    .into(iv_selected);
        } else {
            vp_add_post.setVisibility(View.VISIBLE);
            iv_selected.setVisibility(View.GONE);

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) vp_add_post.getLayoutParams();
            params.height = methods.getScreenWidth();

            for (int i = 0; i < Constants.arrayListSelectedImagesUri.size(); i++) {
                arrayListImagePaths.add(methods.getPathImage(Constants.arrayListSelectedImagesUri.get(i)));
            }

            adapterSelectedImages = new AdapterSelectedImages(AddPostActivity.this, arrayListImagePaths);
            vp_add_post.setAdapter(adapterSelectedImages);
            vp_add_post.setOffscreenPageLimit(10);
        }
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
        ArrayList<Bitmap> arrayListSelectedBitmap = new ArrayList<>();
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
            boolean success = false;
            Constants.arrayListSelectedImagesPath.clear();

            if (!Constants.arrayListSelectedImagesUri.isEmpty()) {
                for (int i = 0; i < Constants.arrayListSelectedImagesUri.size(); i++) {
                    arrayListSelectedBitmap.add(adapterSelectedImages.getCroppedBitmapFromImage(i, vp_add_post));
                }
            } else {
                arrayListSelectedBitmap.add(Constants.selectedImage);
            }

            for (int i = 0; i < arrayListSelectedBitmap.size(); i++) {
                String fileName = System.currentTimeMillis() + ".jpeg";
                filePath = getExternalCacheDir() + File.separator + getResources().getString(R.string.upload) + File.separator + fileName;
                if (!new File(filePath).exists()) {
                    success = saveImage(arrayListSelectedBitmap.get(i), fileName);
                } else {
                    success = true;
                }

                if (success) {
                    Constants.arrayListSelectedImagesPath.add(filePath);
                }
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean s) {
            if (s) {
                getAddPost();
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

    private void getAddPost() {
        if (methods.isNetworkAvailable()) {

            if (methods.getPerNotificationStatus()) {
                Intent intent = new Intent(AddPostActivity.this, UploadService.class);
                intent.putExtra("requestString", methods.getAPIRequest(Constants.URL_ADD_POST, "", isVideo ? "Video" : "Image", "", et_caption.getText().toString(), "", "", "", "", "", "", new SharedPref(AddPostActivity.this).getUserId(), ""));
                intent.putExtra("selectedVideoPath", isVideo ? methods.getPathVideo(videoUri) : "");
                intent.putExtra("isVideo", isVideo);
                startService(intent);

                methods.showToast(getString(R.string.uploading_started));
                Constants.isNewPostAdded = true;
                finish();
            } else {
                String userID = new SharedPref(AddPostActivity.this).getUserId();
                ProgressDialog progressDialog = new ProgressDialog(AddPostActivity.this);
                progressDialog.setTitle(getString(R.string.uploading_));
                progressDialog.setMessage(getString(R.string.uploading_image));
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setMax(100);
                progressDialog.setProgress(0);
                progressDialog.show();

                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                builder.addFormDataPart("data", methods.getAPIRequest(Constants.URL_ADD_POST, "", isVideo ? "Video" : "Image", "", et_caption.getText().toString(), "", "", "", "", "", "", userID, ""));

                File fileImage = null, fileVideo = null;


                if (!Constants.arrayListSelectedImagesPath.isEmpty()) {
                    for (int i = 0; i < Constants.arrayListSelectedImagesPath.size(); i++) {
                        String imagePath = Constants.arrayListSelectedImagesPath.get(i);
                        fileImage = new File(imagePath);

                        CountingRequestBody countingRequestBody = new CountingRequestBody(
                                RequestBody.create(MediaType.parse("image/*"), fileImage),
                                new CountingRequestBody.Listener() {
                                    @Override
                                    public void onProgress(long bytesWritten, long contentLength) {
                                        int progress = (int) ((bytesWritten / (float) contentLength) * 100);
                                        progressDialog.setProgress(progress);
                                    }
                                }
                        );

//                        if (!isStory) {
                        builder.addFormDataPart(!isVideo ? "image" : "image[]", System.currentTimeMillis() + "", countingRequestBody);
//                        } else {
//                            builder.addFormDataPart("media_file", System.currentTimeMillis() + "", countingRequestBody);
//                        }
                    }
                }

                if (isVideo) {
                    progressDialog.setMessage(getString(R.string.uploading_video));

                    selectedVideoPath = methods.getPathVideo(videoUri);
                    fileVideo = new File(selectedVideoPath);

                    RequestBody videoRequestBody = RequestBody.create(MediaType.parse("video/*"), fileVideo);

                    CountingRequestBody countingRequestBody = new CountingRequestBody(videoRequestBody, new CountingRequestBody.Listener() {
                        @Override
                        public void onProgress(long bytesWritten, long contentLength) {
                            // Update your progress bar here
                            int progress = (int) ((bytesWritten / (float) contentLength) * 100);
//                    progressBar.setProgress(progress);

//                        progressDialog.setMessage(getString(R.string.uploading_video));
                            progressDialog.setProgress(progress);
                        }
                    });

                    builder.addFormDataPart("video_file", System.currentTimeMillis() + "." + FilenameUtils.getExtension(fileVideo.getName()), countingRequestBody);
                }

                RequestBody requestBody = builder.build();
                Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getDoUploadPost(requestBody);
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                        progressDialog.dismiss();
                        if (response.body() != null) {
                            if (response.body().getSuccess() != null) {
                                if (response.body().getSuccess().equals("true")) {
                                    et_caption.setText("");

                                    Constants.isNewPostAdded = true;
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
                    public void onFailure(@NonNull Call<RespSuccess> call, @NonNull Throwable t) {
                        progressDialog.dismiss();
                        call.cancel();
                    }
                });
            }
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
        } else if (requestCode == 111 && resultCode == Activity.RESULT_OK && data != null) {
            int pos = data.getIntExtra("pos", 0);
            arrayListImagePaths.set(pos, data.getStringExtra("crop_image_path"));
            adapterSelectedImages.notifyItemChanged(pos);
        }
    }

    public ActivityResultLauncher<CropImageContractOptions> cropImageActivityResultLauncher = registerForActivityResult(
            new CropImageContract(),
            new ActivityResultCallback<CropImageView.CropResult>() {
                @Override
                public void onActivityResult(CropImageView.CropResult result) {
                    if (result.isSuccessful()) {

                        isVideoImageChanged = true;
                        imageUri = result.getUriContent();
                        Constants.arrayListSelectedImagesPath.clear();
                        Constants.arrayListSelectedImagesPath.add(result.getUriFilePath(AddPostActivity.this, true));
                        try {
                            Glide.with(AddPostActivity.this)
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