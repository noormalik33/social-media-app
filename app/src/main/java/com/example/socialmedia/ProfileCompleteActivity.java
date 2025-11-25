package blogtalk.com.socialmedia;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespUserList;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileCompleteActivity extends AppCompatActivity {

    MaterialToolbar toolbar;
    Methods methods;
    SharedPref sharedPref;
    MaterialButton button_update;
    ImageView iv_profile, iv_back;
    TextInputEditText et_name, et_email, et_phone, et_address;
    RadioButton rb_male, rb_female;
    TextInputLayout til_email;
    TextView tv_name, tv_birthdate;
    String imagePath = "";
    ProgressDialog progressDialog;
    int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        methods = new Methods(this);
        methods.forceRTLIfSupported();
        sharedPref = new SharedPref(this);

        toolbar = findViewById(R.id.toolbar_prof_edit);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        button_update = findViewById(R.id.button_edit_prof);
        iv_profile = findViewById(R.id.iv_prof_edit);
        iv_back = findViewById(R.id.iv_prof_back);
        et_name = findViewById(R.id.et_prof_edit_name);
        et_email = findViewById(R.id.et_prof_edit_email);
        et_phone = findViewById(R.id.et_prof_edit_phone);
        et_address = findViewById(R.id.et_prof_edit_address);
        tv_birthdate = findViewById(R.id.tv_prof_edit_birthdate);
        rb_male = findViewById(R.id.rb_prof_edit_male);
        rb_female = findViewById(R.id.rb_prof_edit_female);
        til_email = findViewById(R.id.til_email);
        tv_name = findViewById(R.id.tv_edit_prof_name);

        tv_name.setText(sharedPref.getName());
        et_name.setText(sharedPref.getName());
        et_email.setText(sharedPref.getEmail());
        et_phone.setText(sharedPref.getUserPhone());
        if (sharedPref.getGender().equals("Male")) {
            rb_male.setChecked(true);
        } else {
            rb_female.setChecked(true);
        }
        et_address.setText(sharedPref.getAddress());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        Picasso.get().load(sharedPref.getUserImage()).placeholder(R.drawable.placeholder).into(iv_profile);

        iv_profile.setOnClickListener(v -> {
            if (methods.checkPer()) {
                pickImage();
            }
        });

        tv_birthdate.setOnClickListener(view -> {
//            openDatePicker();

            MaterialDatePicker<Long> datePicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText(getString(R.string.date_of_birth))
                            .build();
            datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                @Override
                public void onPositiveButtonClick(Long selection) {

                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                    String selectedDateText = sdf.format(new Date(selection));
                    tv_birthdate.setText(selectedDateText);
                }
            });

            datePicker.show(getSupportFragmentManager(), "tag");
        });

        button_update.setOnClickListener(v -> {
            if (validate()) {
                getEditProfile();
//                methods.showToast("Profile update is disabled in demo application");
            }
        });

        iv_back.setOnClickListener(view -> onBackPressed());

        if (sharedPref.getIsEmailVerified()) {
            et_email.setEnabled(false);
            til_email.setBoxBackgroundColor(ContextCompat.getColor(ProfileCompleteActivity.this, R.color.et_stroke));
        } else {
            til_email.setError(getString(R.string.err_email_not_verified));
            til_email.setErrorIconOnClickListener(view -> methods.openEmailVerifyDialog());
        }

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);
    }

    private Boolean validate() {
        et_name.setError(null);
        et_email.setError(null);
        et_phone.setError(null);
        if (et_name.getText().toString().trim().isEmpty()) {
            et_name.setError(getString(R.string.cannot_empty));
            et_name.requestFocus();
            return false;
        } else if (et_email.getText().toString().trim().isEmpty()) {
            et_email.setError(getString(R.string.enter_email));
            et_email.requestFocus();
            return false;
        } else if (!rb_male.isChecked() && !rb_female.isChecked()) {
            methods.showToast(getString(R.string.err_select_gender));
            return false;
        } else {
            return true;
        }
    }

    private void getProfile() {
        if (methods.isNetworkAvailable()) {
            progressDialog.show();

            Call<RespUserList> call = APIClient.getClient().create(APIInterface.class).getProfile(methods.getAPIRequest(Constants.URL_PROFILE, "", "", "", "", "", "", "", "", "", "", sharedPref.getUserId(), ""));
            call.enqueue(new Callback<RespUserList>() {
                @Override
                public void onResponse(@NonNull Call<RespUserList> call, @NonNull Response<RespUserList> response) {
                    progressDialog.dismiss();
                    if (response.body() != null) {
                        if (response.body().getSuccess().equals("1")) {
                            tv_name.setText(response.body().getUserDetail().getName());
                            et_name.setText(response.body().getUserDetail().getName());
                            et_email.setText(response.body().getUserDetail().getEmail());
                            et_phone.setText(response.body().getUserDetail().getMobile());
//                                Picasso.get().load(response.body().getUserDetail().getImage()).placeholder(R.drawable.placeholder).into(iv_prof);

                            sharedPref.setName(response.body().getUserDetail().getName());
                            sharedPref.setEmail(response.body().getUserDetail().getEmail());
                            sharedPref.setUserMobile(response.body().getUserDetail().getMobile());
                            sharedPref.setUserImage(response.body().getUserDetail().getImage());
                            sharedPref.setProfileComplete(response.body().getUserDetail().getProfileCompleted());
                            sharedPref.setGender(response.body().getUserDetail().getGender());
                            sharedPref.setBirthdate(response.body().getUserDetail().getDateOfBirth());
                            sharedPref.setAddress(response.body().getUserDetail().getAddress());
                        }
                    } else {
                        methods.showToast(getString(R.string.err_server_error));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespUserList> call, @NonNull Throwable t) {
                    call.cancel();
                    progressDialog.dismiss();
                }
            });
        } else {
            progressDialog.dismiss();
        }
    }

    private void getEditProfile() {
        if (methods.isNetworkAvailable()) {
            progressDialog.show();

            File file = null;


            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            builder.addFormDataPart("data", methods.getAPIRequest(Constants.URL_PROFILE_UPDATE, "", rb_male.isChecked() ? "Male" : "Female", "", et_address.getText().toString(), "", tv_birthdate.getText().toString(), et_name.getText().toString(), et_email.getText().toString(), "", et_phone.getText().toString(), sharedPref.getUserId(), ""));
            if (imagePath != null && !imagePath.isEmpty()) {
                file = new File(imagePath);
                builder.addFormDataPart("user_image", file.getName(), RequestBody.create(MediaType.parse("image/*"), file));
            }

            RequestBody requestBody = builder.build();
            Call<RespUserList> call = APIClient.getClient().create(APIInterface.class).getProfileUpdate(requestBody);
            call.enqueue(new Callback<RespUserList>() {
                @Override
                public void onResponse(@NonNull Call<RespUserList> call, @NonNull Response<RespUserList> response) {
                    progressDialog.dismiss();
                    if (response.body() != null && response.body().getUserDetail() != null) {
                        if (response.body().getSuccess().equals("1")) {
                            updateArray(response.body().getUserDetail().getImage());
                            imagePath = "";
                            Constants.isProfileUpdate = true;
                            finish();
                            methods.showToast(response.body().getMessage());
                        } else {
                            if (response.body().getMessage().contains("Email address already used")) {
                                et_email.setError(response.body().getMessage());
                                et_email.requestFocus();
                            }
                        }
                    } else {
                        methods.showToast(getString(R.string.err_server_error));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespUserList> call, @NonNull Throwable t) {
                    call.cancel();
                    progressDialog.dismiss();
                    methods.showToast(getString(R.string.err_server_error));
                }
            });
        } else {
            methods.showToast(getString(R.string.err_internet_not_connected));
        }
    }

    public void setProfileVar() {
        et_name.setText(sharedPref.getName());
        et_phone.setText(sharedPref.getUserPhone());
        et_email.setText(sharedPref.getEmail());

        if (!sharedPref.getUserImage().isEmpty()) {
            Picasso.get()
                    .load(sharedPref.getUserImage())
                    .into(iv_profile);
        }
    }

    private void updateArray(String image) {
        sharedPref.setName(et_name.getText().toString());
        sharedPref.setEmail(et_email.getText().toString());
        sharedPref.setUserMobile(et_phone.getText().toString());
        sharedPref.setGender(rb_male.isChecked() ? "Male" : "Female");
        sharedPref.setBirthdate(tv_birthdate.getText().toString());
        sharedPref.setAddress(et_address.getText().toString());
        sharedPref.setUserImage(image);
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
                        Uri resultUri = result.getUriContent();
                        iv_profile.setImageURI(resultUri);
                        imagePath = result.getUriFilePath(ProfileCompleteActivity.this, true);
                    } else {

                    }
                }
            });

    @Override
    protected void onResume() {
        if (Constants.isEmailVerificationChanged) {
            til_email.setErrorEnabled(false);
        }
        super.onResume();
    }
}