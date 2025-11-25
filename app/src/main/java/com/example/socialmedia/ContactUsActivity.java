package com.example.socialmedia;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apiservices.APIClient;
import com.example.apiservices.APIInterface;
import com.example.apiservices.RespSuccess;
import com.example.utils.Constants;
import com.example.utils.Methods;
import com.example.utils.SharedPref;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactUsActivity extends AppCompatActivity {

    MaterialToolbar toolbar;
    Methods methods;
    SharedPref sharedPref;
    MaterialButton button_send;
    ImageView iv_back;
    TextInputEditText et_name, et_email, et_password, et_phone, et_message, et_subject;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        methods = new Methods(this);
        methods.forceRTLIfSupported();
        sharedPref = new SharedPref(this);

        toolbar = findViewById(R.id.toolbar_contact);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        button_send = findViewById(R.id.button_contact);
        iv_back = findViewById(R.id.iv_prof_back);
        et_name = findViewById(R.id.et_contact_name);
        et_email = findViewById(R.id.et_contact_email);
        et_phone = findViewById(R.id.et_contact_phone);
        et_subject = findViewById(R.id.et_contact_subject);
        et_message = findViewById(R.id.et_contact_message);

        et_name.setText(sharedPref.getName());
        et_email.setText(sharedPref.getEmail());
        et_phone.setText(sharedPref.getUserPhone());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        button_send.setOnClickListener(v -> {
            if(validate()) {
                getContactUs();
            }
        });

        iv_back.setOnClickListener(view -> onBackPressed());

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);
    }

    private boolean validate() {
        et_name.setError(null);
        et_email.setError(null);
        et_phone.setError(null);
        et_subject.setError(null);
        et_message.setError(null);

        // Store values at the time of the login attempt.
        String name = et_email.getText().toString();
        String email = et_email.getText().toString();
        String phone = et_email.getText().toString();
        String subject = et_email.getText().toString();
        String message = et_email.getText().toString();

        boolean isSuccess = true;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            et_email.setError(getString(R.string.err_invalid_email));
            focusView = et_email;
            isSuccess = false;
        } else if (!isEmailValid(email)) {
            et_email.setError(getString(R.string.err_invalid_email));
            focusView = et_email;
            isSuccess = false;
        } else if (TextUtils.isEmpty(name)) {
            et_name.setError(getString(R.string.err_name_empty));
            focusView = et_name;
            isSuccess = false;
        } else if (TextUtils.isEmpty(phone)) {
            et_phone.setError(getString(R.string.err_phone_empty));
            focusView = et_phone;
            isSuccess = false;
        } else if (TextUtils.isEmpty(subject)) {
            et_subject.setError(getString(R.string.err_subject_empty));
            focusView = et_subject;
            isSuccess = false;
        } else if (TextUtils.isEmpty(message)) {
            et_message.setError(getString(R.string.err_message_empty));
            focusView = et_message;
            isSuccess = false;
        }

        if (!isSuccess) {
            focusView.requestFocus();
        } else {

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(et_name.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(et_email.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(et_phone.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(et_subject.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(et_message.getWindowToken(), 0);

            et_name.clearFocus();
            et_email.clearFocus();
            et_phone.clearFocus();
            et_subject.clearFocus();
            et_message.clearFocus();
        }
        return isSuccess;
    }

    private void getContactUs() {
        if (methods.isNetworkAvailable()) {
            progressDialog.show();

            Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getContactUs(methods.getAPIRequest(Constants.URL_CONTACT_US, "", et_subject.getText().toString(), "", et_message.getText().toString(), "", "", et_name.getText().toString(), et_email.getText().toString(), "", et_phone.getText().toString(), "", ""));
            call.enqueue(new Callback<RespSuccess>() {
                @Override
                public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                    progressDialog.dismiss();
                    if (response.body() != null && response.body().getSuccess() != null) {
                        if (response.body().getSuccess().equals("1")) {
                            et_subject.setText("");
                            et_message.setText("");
                        }
                        methods.showToast(response.body().getMessage());
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

    private boolean isEmailValid(String email) {
        return email.contains("@") && !email.contains(" ");
    }
}