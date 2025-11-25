package blogtalk.com.socialmedia;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespSuccess;
import blogtalk.com.fragments.FragmentUploadMedia;
import blogtalk.com.fragments.FragmentUploadText;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadActivity extends AppCompatActivity {

    Methods methods;
    ViewPager2 vp_upload;
    UploadPageAdapter uploadPageAdapter;
    public FloatingActionButton fab_upload_text;
    MaterialButton btn_media, btn_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        methods = new Methods(this);
        methods.forceRTLIfSupported();

        vp_upload = findViewById(R.id.vp_upload);
        btn_media = findViewById(R.id.btn_upload_media);
        btn_text = findViewById(R.id.btn_upload_text);
        fab_upload_text = findViewById(R.id.fab_upload_text);

        uploadPageAdapter = new UploadPageAdapter(this);
        vp_upload.setAdapter(uploadPageAdapter);

        vp_upload.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                changePage(position);
                super.onPageSelected(position);
            }
        });

        btn_media.setOnClickListener(view -> {
            changePage(0);
            vp_upload.setCurrentItem(0);
        });

        btn_text.setOnClickListener(view -> {
            changePage(1);
            vp_upload.setCurrentItem(1);
        });

        fab_upload_text.setOnClickListener(view -> {
            FragmentUploadText fragment = (FragmentUploadText) getSupportFragmentManager().findFragmentByTag("f" + vp_upload.getCurrentItem());
            if (fragment != null) {
                String value = fragment.getText();
                if(value != null && !value.isEmpty()) {
                    getAddPost(value);
//                    methods.showToast("Upload is disabled in demo application");
                }
            }
        });
    }

    private void changePage(int pos) {
        if (pos == 0) {
            btn_text.setTextColor(ContextCompat.getColor(UploadActivity.this, R.color.text));
            btn_media.setTextColor(ContextCompat.getColor(UploadActivity.this, R.color.white));

            btn_text.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.white)));
            btn_media.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.points)));

            fab_upload_text.setVisibility(View.GONE);
        } else {
            btn_text.setTextColor(ContextCompat.getColor(UploadActivity.this, R.color.white));
            btn_media.setTextColor(ContextCompat.getColor(UploadActivity.this, R.color.text));

            btn_media.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.white)));
            btn_text.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.points)));
        }
    }

    private void getAddPost(String text) {
        if (methods.isNetworkAvailable()) {

            ProgressDialog progressDialog = new ProgressDialog(UploadActivity.this);
            progressDialog.setMessage(getString(R.string.uploading_));
            progressDialog.show();

            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            builder.addFormDataPart("data", methods.getAPIRequest(Constants.URL_ADD_POST, "", "Text", "", text, "", "", "", "", "", "", new SharedPref(UploadActivity.this).getUserId(), ""));

            RequestBody requestBody = builder.build();
            Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getDoUploadPost(requestBody);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                    progressDialog.dismiss();
                    if (response.body() != null) {
                        if (response.body().getSuccess() != null) {
                            if (response.body().getSuccess().equals("true")) {

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
        } else {
            methods.showToast(getString(R.string.err_internet_not_connected));
        }
    }

    public static class UploadPageAdapter extends FragmentStateAdapter {
        public UploadPageAdapter(FragmentActivity fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new FragmentUploadMedia();
            } else {
                return new FragmentUploadText();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions.length > 0) {
            if (grantResults.length > 0 && grantResults[0] != -1) {
                try {
                    FragmentUploadMedia fragment = (FragmentUploadMedia) getSupportFragmentManager().findFragmentByTag("f" + vp_upload.getCurrentItem());
                    fragment.getFolders();
                } catch (Exception ignore) {}
            }
        }
    }
}