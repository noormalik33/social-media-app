package com.example.socialmedia;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.example.apiservices.APIClient;
import com.example.apiservices.APIInterface;
import com.example.apiservices.RespSuccess;
import com.example.utils.Constants;
import com.example.utils.Methods;
import com.example.utils.SharedPref;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmailVerificationActivity extends AppCompatActivity {

    Methods methods;
    ProgressDialog progressDialog;
    String userID;
    boolean isFromApp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        userID = getIntent().getStringExtra("user_id");
        isFromApp = getIntent().getBooleanExtra("from", false);

        methods = new Methods(this);
        methods.forceRTLIfSupported();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.registering));
        progressDialog.setCancelable(false);

        PinView pinView = findViewById(R.id.pinView);

        MaterialButton button_verify = findViewById(R.id.button_verify);
        MaterialButton button_close = findViewById(R.id.button_verify_close);
        TextView tv_verify_resend = findViewById(R.id.tv_verify_resend);

        button_close.setOnClickListener(v -> {
            onBackPressed();
        });

        pinView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() < 6) {
                    button_verify.setEnabled(false);
                    button_verify.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.btn_disable)));
                } else {
                    button_verify.setEnabled(true);
                    button_verify.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.primary)));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        button_verify.setOnClickListener(view -> {
            if(pinView.getText().toString().length() < 6) {
                methods.showToast("enter otp");
            } else {
                loadVerifyOTP(userID, pinView.getText().toString());
            }
        });

        tv_verify_resend.setOnClickListener(v -> {
            loadSendOTP(userID);
        });

        if(isFromApp) {
            loadSendOTP(userID);
        }

    }

    private void loadSendOTP(String userID) {
        progressDialog.setMessage(getString(R.string.sending_verification_code));
        progressDialog.show();

        Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getSendVerifyOTP(methods.getAPIRequest(Constants.URL_SEND_VERIFY_OTP, "", "", "", "", "", "", "", "", "", "", userID, ""));
        call.enqueue(new Callback<RespSuccess>() {
            @Override
            public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                if(response.body() != null && response.body().getSuccess() != null) {
                    methods.showToast(response.body().getMessage());
                } else {
                    methods.showToast(getString(R.string.err_server_error));
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<RespSuccess> call, @NonNull Throwable t) {
                call.cancel();
                methods.showToast(getString(R.string.err_server_error));
                progressDialog.dismiss();
            }
        });
    }

    private void loadVerifyOTP(String userID, String otp) {
        progressDialog.setMessage(getString(R.string.verifing_code));
        progressDialog.show();

        Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getVerifyOTP(methods.getAPIRequest(Constants.URL_VERIFY_OTP, "", "", "", otp, "", "", "", "", "", "", userID, ""));
        call.enqueue(new Callback<RespSuccess>() {
            @Override
            public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                if(response.body() != null && response.body().getSuccess() != null) {
                    if(response.body().getSuccess().equals("1")) {

                        if (!isFromApp) {
                            Intent intent = new Intent(EmailVerificationActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            Constants.isEmailVerificationChanged = true;
                            new SharedPref(EmailVerificationActivity.this).setIsEmailVerified(true);
                        }
                        finish();
                    }
                    methods.showToast(response.body().getMessage());
                } else {
                    methods.showToast(getString(R.string.err_server_error));
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<RespSuccess> call, @NonNull Throwable t) {
                call.cancel();
                methods.showToast(getString(R.string.err_server_error));
                progressDialog.dismiss();
            }
        });
    }
}