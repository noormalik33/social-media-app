package com.example.socialmedia;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.example.apiservices.APIClient;
import com.example.apiservices.APIInterface;
import com.example.apiservices.RespSuccess;
import com.example.apiservices.RespUserList;
import com.example.items.ItemUser;
import com.example.utils.Constants;
import com.example.utils.CountingRequestBody;
import com.example.utils.Methods;
import com.example.utils.SharedPref;
import com.google.android.material.button.MaterialButton;
import com.makeramen.roundedimageview.RoundedImageView;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyAccountActivity extends AppCompatActivity {

    Methods methods;
    CardView cv_upload;
    MaterialButton btn_request;
    RoundedImageView iv_doc;
    TextView tv_total_post, tv_total_followers, tv_message;
    ItemUser itemUser;
    String docImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verified_author);

        itemUser = (ItemUser) getIntent().getSerializableExtra("item");

        methods = new Methods(this);
        methods.forceRTLIfSupported();

        findViewById(R.id.iv_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        btn_request = findViewById(R.id.btn_verify_request);
        iv_doc = findViewById(R.id.iv_verify_doc);
        cv_upload = findViewById(R.id.cv_upload_doc);
        tv_total_post = findViewById(R.id.tv_verify_post_condition);
        tv_total_followers = findViewById(R.id.tv_verify_followers_condition);
        tv_message = findViewById(R.id.tv_verify_message);

        tv_total_post.setText(getString(R.string.created_atlleat_10_post, String.valueOf(Constants.minPost)));
        tv_total_followers.setText(getString(R.string.required_atlleat_50_followers, String.valueOf(Constants.minFollowers)));
        tv_message.setText(getString(R.string.author_verify_message, Constants.verifiedDocName));

        tv_total_post.setCompoundDrawablesWithIntrinsicBounds(itemUser.getTotalPost() >= Constants.minPost ? R.drawable.ic_right : R.drawable.ic_wrong, 0, 0, 0);
        tv_total_followers.setCompoundDrawablesWithIntrinsicBounds(itemUser.getTotalFollowers() >= Constants.minFollowers ? R.drawable.ic_right : R.drawable.ic_wrong, 0, 0, 0);

        btn_request.setEnabled(itemUser.getTotalPost() >= Constants.minPost && itemUser.getTotalFollowers() >= Constants.minFollowers && !itemUser.getIsAccountVerificationRequested());

        if(itemUser.getIsAccountVerificationRequested()) {
            btn_request.setText(getString(R.string.requested));
            btn_request.setEnabled(false);
        }

        btn_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(methods.isLoggedAndVerified(true)) {
                    if (docImage != null && !docImage.isEmpty()) {
                        getSendRequest();
                    } else {
                        methods.showToast(getString(R.string.err_upload_doc));
                    }
                }
            }
        });

        cv_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (methods.checkPer()) {
                    pickImage();
                }
            }
        });
    }

    private void getSendRequest() {
        if (methods.isNetworkAvailable()) {
            ProgressDialog progressDialog = new ProgressDialog(VerifyAccountActivity.this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.show();

            File file = null;

            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            builder.addFormDataPart("data", methods.getAPIRequest(Constants.URL_ACCOUNT_VERIFY_REQUEST, "", "", "", "", "", "", "", "", "", "", new SharedPref(VerifyAccountActivity.this).getUserId(), ""));
            if (docImage != null && !docImage.isEmpty()) {
                file = new File(docImage);
                builder.addFormDataPart("account_verification_doc", file.getName(), RequestBody.create(MediaType.parse("image/*"), file));
            }

            RequestBody requestBody = builder.build();
            Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getAccountVerifyRequest(requestBody);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                    progressDialog.dismiss();
                        if (response.body() != null && response.body().getItemSuccess() != null) {
                        if (response.body().getItemSuccess().getSuccess().equals("1")) {

//                            methods.showToast(response.body().getMessage());
                        }
                        methods.showToast(response.body().getItemSuccess().getMessage());
                    } else {
                        methods.showToast(getString(R.string.err_server_error));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespSuccess> call, @NonNull Throwable t) {
                    call.cancel();
                    progressDialog.dismiss();
                    methods.showToast(getString(R.string.err_server_error));
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
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_image)), 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
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

//                        docUri = result.getUriContent();
                        docImage = result.getUriFilePath(VerifyAccountActivity.this, true);
                        try {
                            Glide.with(VerifyAccountActivity.this)
                                    .load(result.getUriContent())
                                    .into(iv_doc);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
}