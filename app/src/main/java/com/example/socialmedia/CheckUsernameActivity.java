package blogtalk.com.socialmedia;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespSuccess;
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
import com.google.android.material.progressindicator.IndeterminateDrawable;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckUsernameActivity extends AppCompatActivity {

    MaterialToolbar toolbar;
    Methods methods;
    MaterialButton button_update;
    ImageView iv_back;
    TextInputEditText et_username;
    TextInputLayout til_username;
    Handler handler = new Handler();
    CircularProgressDrawable drawable;
    String username="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_username);

        username = getIntent().getStringExtra("username");

        methods = new Methods(this);
        methods.forceRTLIfSupported();

        toolbar = findViewById(R.id.toolbar_check_username);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        button_update = findViewById(R.id.button_check_username);
        iv_back = findViewById(R.id.iv_back_check_username);
        et_username = findViewById(R.id.et_check_username);
        til_username = findViewById(R.id.til_check_username);

        til_username.setCounterMaxLength(Constants.userNameMaxChar);

        iv_back.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        et_username.setText(username);
        et_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                button_update.setVisibility(View.GONE);
                if(validate(editable.toString())) {
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 1000);
                }
            }
        });

        drawable = new CircularProgressDrawable(this);
        drawable.setStyle(CircularProgressDrawable.DEFAULT);
        drawable.setColorSchemeColors(Color.BLUE);
        drawable.start();
        til_username.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);

        button_update.setOnClickListener(view -> {
            Constants.isUsernameChanged = true;
            Constants.tempUsername = et_username.getText().toString();
            finish();
        });

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            getCheckUsername(et_username.getText().toString());
        }
    };

    private boolean validate(String username) {
        et_username.setError(null);
        if(!isValidUsername(username)) {
            et_username.setError(getString(R.string.err_invalid_username));
            return false;
        }

        return true;
    }

    public static boolean isValidUsername(String username) {
        // Regular expression for the username
        String regex = "^[a-zA-Z]+[a-zA-Z0-9._]*$";
        // Compile the regular expression
        Pattern pattern = Pattern.compile(regex);
        // Create a matcher object
        Matcher matcher = pattern.matcher(username);
        // Check if the username matches the pattern
        return matcher.matches();
    }


    private void getCheckUsername(String username) {
        if (methods.isNetworkAvailable()) {

            til_username.setEndIconDrawable(drawable);
            button_update.setVisibility(View.GONE);

            Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getCheckUsername(methods.getAPIRequestProfile(Constants.URL_CHECK_USERNAME, "", "", "", "", "", "", "", "", "", username, "", "", "", "", "", "", "", "","","",""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {

                    if (response.body() != null && response.body().getSuccess()!=null) {
                        if (response.body().getSuccess().equals("1")) {
                            button_update.setVisibility(View.VISIBLE);
                        }
                        methods.showToast(response.body().getMessage());
                    } else {
                        methods.showToast(getString(R.string.err_server_error));
                    }
                    til_username.setEndIconDrawable(null);
                }

                @Override
                public void onFailure(@NonNull Call<RespSuccess> call, @NonNull Throwable t) {
                    button_update.setVisibility(View.GONE);
                    til_username.setEndIconDrawable(null);
                    call.cancel();
                }
            });
        } else {
            methods.showToast(getString(R.string.err_internet_not_connected));
        }
    }
}