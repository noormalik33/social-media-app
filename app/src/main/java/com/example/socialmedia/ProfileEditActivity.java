package blogtalk.com.socialmedia;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileEditActivity extends AppCompatActivity {

    MaterialToolbar toolbar;
    Methods methods;
    SharedPref sharedPref;
    MaterialButton button_update, button_add_links;
    ImageView iv_profile, iv_back;
    TextInputEditText et_username, et_name, et_email, et_phone, et_address, et_bio;
    RadioButton rb_male, rb_female, rb_other;
    TextInputLayout til_email, til_address;
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
        button_add_links = findViewById(R.id.button_edit_add_links);
        iv_profile = findViewById(R.id.iv_prof_edit);
        iv_back = findViewById(R.id.iv_prof_back);
        et_username = findViewById(R.id.et_prof_edit_username);
        et_name = findViewById(R.id.et_prof_edit_name);
        et_email = findViewById(R.id.et_prof_edit_email);
        et_phone = findViewById(R.id.et_prof_edit_phone);
        et_address = findViewById(R.id.et_prof_edit_address);
        et_bio = findViewById(R.id.et_prof_edit_bio);
        tv_birthdate = findViewById(R.id.tv_prof_edit_birthdate);
        rb_male = findViewById(R.id.rb_prof_edit_male);
        rb_female = findViewById(R.id.rb_prof_edit_female);
        rb_other = findViewById(R.id.rb_prof_edit_other);
        til_email = findViewById(R.id.til_email);
        til_address = findViewById(R.id.til_address);
        tv_name = findViewById(R.id.tv_edit_prof_name);

        tv_name.setText(sharedPref.getName());
        et_username.setText(sharedPref.getUserName());
        et_name.setText(sharedPref.getName());
        et_email.setText(sharedPref.getEmail());
        et_phone.setText(sharedPref.getUserPhone());
        et_bio.setText(sharedPref.getUserBio());
        if(!sharedPref.getBirthdate().isEmpty()) {
            tv_birthdate.setText(sharedPref.getBirthdate());
        } else{
            tv_birthdate.setText(getString(R.string.date_of_birth));
        }

        if (sharedPref.getGender().equalsIgnoreCase("male")) {
            rb_male.setChecked(true);
        } else if (sharedPref.getGender().equalsIgnoreCase("female")) {
            rb_female.setChecked(true);
        } else if (sharedPref.getGender().equalsIgnoreCase("other")) {
            rb_other.setChecked(true);
        }
        et_address.setText(sharedPref.getAddress());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        Picasso.get().load(sharedPref.getUserImage()).placeholder(R.drawable.placeholder).into(iv_profile);

        til_address.setEndIconOnClickListener(view -> {
            getLocation();
        });

        iv_profile.setOnClickListener(v -> {
            if (methods.checkPer()) {
                pickImage();
            }
        });

        tv_birthdate.setOnClickListener(view -> {
            MaterialDatePicker<Long> datePicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText(getString(R.string.date_of_birth))
                            .build();
            datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                @Override
                public void onPositiveButtonClick(Long selection) {

                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                    Date dateDOB = new Date(selection);
                    String selectedDateText = sdf.format(dateDOB);
                    Calendar calendar = Calendar.getInstance();
                    Date currentDate = calendar.getTime();
//                    calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR)-10);
//                    Date date10 = calendar.getTime();
                    try {
                        if(currentDate.before(dateDOB)) {
                            methods.showToast(getString(R.string.err_birthdate_not_future));
                        } else {
                            tv_birthdate.setText(selectedDateText);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
            til_email.setBoxBackgroundColor(ContextCompat.getColor(ProfileEditActivity.this, R.color.et_stroke));
        } else {
            til_email.setError(getString(R.string.err_email_not_verified));
            til_email.setErrorIconOnClickListener(view -> methods.openEmailVerifyDialog());
        }

        et_username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileEditActivity.this, CheckUsernameActivity.class);
                intent.putExtra("username", et_username.getText().toString());
                startActivity(intent);
            }
        });

        button_add_links.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileEditActivity.this, LinksActivity.class);
            startActivity(intent);
        });

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);
    }

    private Boolean validate() {
        et_name.setError(null);
        et_email.setError(null);
        et_phone.setError(null);
        if (et_username.getText().toString().length() < 5) {
            et_username.setError(getString(R.string.err_username_5_char));
            et_username.requestFocus();
            return false;
        } else if (et_username.getText().toString().trim().isEmpty()) {
            et_username.setError(getString(R.string.cannot_empty));
            et_username.requestFocus();
            return false;
        } else if (et_name.getText().toString().trim().isEmpty()) {
            et_name.setError(getString(R.string.cannot_empty));
            et_name.requestFocus();
            return false;
        } else if (et_email.getText().toString().trim().isEmpty()) {
            et_email.setError(getString(R.string.enter_email));
            et_email.requestFocus();
            return false;
        } else if (et_bio.getText().toString().length() > 150) {
            et_bio.setError(getString(R.string.err_bio_150_char));
            et_bio.requestFocus();
            return false;
        } else if (!rb_male.isChecked() && !rb_female.isChecked() && !rb_other.isChecked()) {
            methods.showToast(getString(R.string.err_select_gender));
            return false;
        } else {
            return true;
        }
    }

    private void getEditProfile() {
        if (methods.isNetworkAvailable()) {
            progressDialog.show();

            File file = null;

            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            builder.addFormDataPart("data", methods.getAPIRequestProfile(Constants.URL_PROFILE_UPDATE, rb_male.isChecked() ? "Male" : rb_female.isChecked() ? "Female" : "Other", et_bio.getText().toString(), et_address.getText().toString(), "", "", !tv_birthdate.getText().toString().equals(getString(R.string.date_of_birth)) ? tv_birthdate.getText().toString() : "", et_name.getText().toString(), et_email.getText().toString(), et_phone.getText().toString(), et_username.getText().toString(), "", "", "", "", "", "", "", "", "", "", sharedPref.getUserId()));
            if (imagePath != null && !imagePath.isEmpty()) {
                file = new File(imagePath);
                builder.addFormDataPart("user_image", file.getName(), RequestBody.create(MediaType.parse("image/*"), file));
            }

            RequestBody requestBody = builder.build();
            Call<RespUserList> call = APIClient.getClient().create(APIInterface.class).getProfileUpdate(requestBody);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespUserList> call, @NonNull Response<RespUserList> response) {
                    progressDialog.dismiss();
                    if (response.body() != null && response.body().getUserDetail() != null) {
                        if (response.body().getSuccess().equals("1")) {
                            updateArray(response.body().getUserDetail().getImage(), response.body().getUserDetail().getProfileCompleted());
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

    private void updateArray(String image, int profileCompleted) {
        sharedPref.setUserName(et_username.getText().toString());
        sharedPref.setName(et_name.getText().toString());
        sharedPref.setEmail(et_email.getText().toString());
        sharedPref.setUserMobile(et_phone.getText().toString());
        sharedPref.setGender(rb_male.isChecked() ? "Male" : rb_female.isChecked() ? "Female" : "Other");
        sharedPref.setBirthdate(tv_birthdate.getText().toString());
        sharedPref.setAddress(et_address.getText().toString());
        sharedPref.setUserBio(et_bio.getText().toString());
        sharedPref.setUserImage(image);
        sharedPref.setProfileComplete(profileCompleted);
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_image)), PICK_IMAGE_REQUEST);
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        if (methods.checkPerLocation()) {
            ProgressDialog progressDialog = new ProgressDialog(ProfileEditActivity.this);
            progressDialog.setTitle("Location");
            progressDialog.setMessage("Getting your location");
            progressDialog.show();

            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(ProfileEditActivity.this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            progressDialog.dismiss();
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                sharedPref.setLatitude(String.valueOf(location.getLatitude()));
                                sharedPref.setLongitude(String.valueOf(location.getLongitude()));

                                getAddressFromLocation(location);
                            }
                        }
                    });
        }
    }

    private void getAddressFromLocation(Location location) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

            et_address.setText(address.concat("\n").concat(city).concat("\n").concat(state).concat("\n").concat(country));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
                        imagePath = result.getUriFilePath(ProfileEditActivity.this, true);
                    } else {

                    }
                }
            }
    );

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions.length > 0 && (permissions[0].equalsIgnoreCase(android.Manifest.permission.ACCESS_FINE_LOCATION) || permissions[0].equalsIgnoreCase(Manifest.permission.ACCESS_COARSE_LOCATION))) {
            if (grantResults.length > 0 && grantResults[0] != -1) {
                getLocation();
            }
        }
    }


    @Override
    protected void onResume() {
        if (Constants.isEmailVerificationChanged) {
            til_email.setErrorEnabled(false);
        }
        if (Constants.isUsernameChanged) {
            Constants.isUsernameChanged = false;
            et_username.setText(Constants.tempUsername);
        }
        super.onResume();
    }
}