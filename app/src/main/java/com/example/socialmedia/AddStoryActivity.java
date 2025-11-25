package blogtalk.com.socialmedia;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.palette.graphics.Palette;

import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespSuccess;
import blogtalk.com.items.ItemMedia;
import blogtalk.com.utils.BackgroundTask;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.CountingRequestBody;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.PaletteUtils;
import blogtalk.com.utils.SharedPref;
import blogtalk.com.utils.UploadService;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddStoryActivity extends AppCompatActivity {

    Methods methods;
    MaterialButton btn_upload;
    ImageView iv_story;
    RelativeLayout rl_story;
    Uri itemMediaUri;
    boolean isVideo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        itemMediaUri = getIntent().getParcelableExtra("item");

        methods = new Methods(this);

        findViewById(R.id.iv_back).setOnClickListener(v -> onBackPressed());

        btn_upload = findViewById(R.id.btn_upload_story);
        rl_story = findViewById(R.id.rl_story);
        iv_story = findViewById(R.id.iv_story);

        Picasso.get()
                .load(itemMediaUri)
                .into(iv_story);

        btn_upload.setOnClickListener(view -> {
            getFileFromView(rl_story);
        });

        Picasso.get()
                .load(itemMediaUri)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                        Palette.from(bitmap).generate(palette -> {
//                            if (palette != null) {
//                                // Get two dominant colors (fallback to default if null)
////                                int color1 = palette.getDarkVibrantColor(ContextCompat.getColor(AddStoryActivity.this, android.R.color.black));
////                                int color2 = palette.getDarkMutedColor(ContextCompat.getColor(AddStoryActivity.this, android.R.color.darker_gray));
////                                int color3 = palette.getDominantColor(ContextCompat.getColor(AddStoryActivity.this, android.R.color.darker_gray));
//                                int color4 = palette.getMutedColor(ContextCompat.getColor(AddStoryActivity.this, android.R.color.darker_gray));
////                                int color5 = palette.getLightMutedColor(ContextCompat.getColor(AddStoryActivity.this, android.R.color.darker_gray));
////                                int color6 = palette.getLightVibrantColor(ContextCompat.getColor(AddStoryActivity.this, android.R.color.darker_gray));
////                                int color7 = palette.getVibrantColor(ContextCompat.getColor(AddStoryActivity.this, android.R.color.darker_gray));
////
////                                // Create a gradient drawable
////                                GradientDrawable gradientDrawable = new GradientDrawable(
////                                        GradientDrawable.Orientation.TOP_BOTTOM,
////                                        new int[]{color7, color4}
////                                );
//
//                                // Set the gradient as the background
//                                rl_story.setBackgroundColor(color4);
//                            }
//                        });

//                        iv_story.setImageBitmap(bitmap);

                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        int height = displayMetrics.heightPixels;
                        int width = displayMetrics.widthPixels;

                        Bitmap backgroundDominantColorBitmap = PaletteUtils.getDominantGradient(bitmap,height,width);
                        BitmapDrawable ob = new BitmapDrawable(getResources(), backgroundDominantColorBitmap);
                        rl_story.setBackground(ob);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });

        if (!methods.getPerNotificationStatus()) {
            methods.openNotiPermissionDialog();
        }
    }

    private void getAddStory() {
        if (methods.isNetworkAvailable()) {

            if (methods.getPerNotificationStatus()) {
                Intent intent = new Intent(AddStoryActivity.this, UploadService.class);
                intent.putExtra("requestString", methods.getAPIRequest(Constants.URL_ADD_POST, "", isVideo ? "Video" : "Image", "", "", "", "", "", "", "", "", new SharedPref(AddStoryActivity.this).getUserId(), ""));
                intent.putExtra("selectedImagePath", methods.getPathImage(itemMediaUri));
                intent.putExtra("selectedVideoPath", isVideo ? "methods.getPathVideo(videoUri)" : "");
                intent.putExtra("isVideo", isVideo);
                intent.putExtra("isStory", true);
                startService(intent);

                methods.showToast(getString(R.string.uploading_started));
                Constants.isNewStoryAdded = true;
                finish();
            } else {
                String userID = new SharedPref(AddStoryActivity.this).getUserId();
                ProgressDialog progressDialog = new ProgressDialog(AddStoryActivity.this);
                progressDialog.setTitle(getString(R.string.uploading_));
                progressDialog.setMessage(getString(R.string.uploading_image));
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setMax(100);
                progressDialog.setProgress(0);
                progressDialog.show();

                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                builder.addFormDataPart("data", methods.getAPIRequest(Constants.URL_ADD_POST, "", isVideo ? "Video" : "Image", "", "", "", "", "", "", "", "", userID, ""));

                File fileImage = null, fileVideo = null;
                if (!Constants.arrayListSelectedImagesPath.isEmpty()) {
                    fileImage = new File(Constants.arrayListSelectedImagesPath.get(0));

                    CountingRequestBody countingRequestBody = new CountingRequestBody(RequestBody.create(MediaType.parse("image/*"), fileImage), new CountingRequestBody.Listener() {
                        @Override
                        public void onProgress(long bytesWritten, long contentLength) {
                            // Update your progress bar here
                            int progress = (int) ((bytesWritten / (float) contentLength) * 100);
//                    progressBar.setProgress(progress);
                            progressDialog.setProgress(progress);
                        }
                    });

                    builder.addFormDataPart("media_file", System.currentTimeMillis() + "", countingRequestBody);
                }

                RequestBody requestBody = builder.build();
                Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getUploadStory(requestBody);
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                        progressDialog.dismiss();
                        if (response.body() != null) {
                            if (response.body().getSuccess() != null) {
                                if (response.body().getSuccess().equals("true")) {
                                    Constants.isNewStoryAdded = true;
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

    private void getFileFromView(View view) {
        new BackgroundTask() {
            File file;

            @Override
            public void onPreExecute() {

            }

            @Override
            public boolean doInBackground() {
                try {
                    Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    view.draw(canvas);

                    file = new File(getCacheDir(), System.currentTimeMillis()+".png");
                    FileOutputStream fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (Exception e){
                    return false;
                }
                return true;
            }

            @Override
            public void onPostExecute(Boolean isExecutionSuccess) {
                if(isExecutionSuccess) {
                    Constants.arrayListSelectedImagesPath.clear();
                    Constants.arrayListSelectedImagesPath.add(file.getPath());

                    getAddStory();
                } else {
                    methods.showToast("Try Again");
                }
            }
        }.execute();
    }
}