package blogtalk.com.socialmedia;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespRegister;
import blogtalk.com.apiservices.RespSuccess;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.refactor.library.SmoothCheckBox;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    Methods methods;
    SmoothCheckBox checkbox_register;
    EditText editText_name, editText_email, editText_pass, editText_phone;
    Button button_register;
    TextView tv_login, tv_terms, tv_birthdate;
    ProgressDialog progressDialog;
    SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sharedPref = new SharedPref(this);
        methods = new Methods(this);
        methods.forceRTLIfSupported();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.registering));
        progressDialog.setCancelable(false);

        checkbox_register = findViewById(R.id.checkbox_register);
        button_register = findViewById(R.id.button_register);
        editText_name = findViewById(R.id.et_register_name);
        editText_email = findViewById(R.id.et_register_email);
        editText_pass = findViewById(R.id.et_register_password);
        editText_phone = findViewById(R.id.et_register_phone);
        tv_birthdate = findViewById(R.id.tv_regis_birthdate);
        tv_login = findViewById(R.id.tv_register_login);
        tv_terms = findViewById(R.id.tv_terms_register);

        findViewById(R.id.tv3).setOnClickListener(view -> {
            checkbox_register.setChecked(!checkbox_register.isChecked(), true);
        });

        tv_terms.setOnClickListener(view -> {
            for (int i = 0; i < Constants.itemAbout.getArrayListPages().size(); i++) {
                if (Constants.itemAbout.getArrayListPages().get(i).getId().equals("2")) {
                    Intent intent = new Intent(RegisterActivity.this, WebviewActivity.class);
                    intent.putExtra("item", Constants.itemAbout.getArrayListPages().get(i));
                    startActivity(intent);
                }
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

        button_register.setOnClickListener(view -> {
            if (validate()) {
                loadRegister();
            }
        });

        tv_login.setOnClickListener(view -> onBackPressed());
    }

    private Boolean validate() {
        if (editText_name.getText().toString().trim().isEmpty()) {
            editText_name.setError(getResources().getString(R.string.enter_name));
            editText_name.requestFocus();
            return false;
        } else if (editText_email.getText().toString().trim().isEmpty()) {
            editText_email.setError(getResources().getString(R.string.enter_email));
            editText_email.requestFocus();
            return false;
        } else if (!isEmailValid(editText_email.getText().toString())) {
            editText_email.setError(getString(R.string.err_invalid_email));
            editText_email.requestFocus();
            return false;
        } else if (editText_pass.getText().toString().isEmpty()) {
            editText_pass.setError(getResources().getString(R.string.enter_password));
            editText_pass.requestFocus();
            return false;
        } else if (editText_pass.getText().toString().endsWith(" ")) {
            editText_pass.setError(getResources().getString(R.string.pass_end_space));
            editText_pass.requestFocus();
            return false;
        } else if (tv_birthdate.getText().toString().isEmpty()) {
            methods.showToast(getString(R.string.err_enter_birthdate));
            return false;
        } else if (!checkbox_register.isChecked()) {
            methods.showToast(getString(R.string.agree_terms));
            return false;
        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText_name.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(editText_email.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(editText_pass.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(editText_phone.getWindowToken(), 0);

            editText_name.clearFocus();
            editText_email.clearFocus();
            editText_pass.clearFocus();
            editText_phone.clearFocus();

            return true;
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && !email.contains(" ");
    }

    private void loadRegister() {
        if (methods.isNetworkAvailable()) {
            progressDialog.show();

            Call<RespRegister> call = APIClient.getClient().create(APIInterface.class).getRegistration(methods.getAPIRequest(Constants.URL_REGISTRATION, "", "", "", "", "", tv_birthdate.getText().toString(), editText_name.getText().toString(), editText_email.getText().toString(), editText_pass.getText().toString(), editText_phone.getText().toString(), "", ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespRegister> call, @NonNull Response<RespRegister> response) {
                    if (response.body() != null) {
                        switch (response.body().getSuccess()) {
                            case "1":
                                if(response.body().getUserDetail().isVerifyEmail().equals("on")) {
                                    loadSendVerifyOTP(response.body().getUserDetail().getUserId());
                                } else {
                                    Toast.makeText(RegisterActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtra("from", "");
                                    startActivity(intent);
                                    finish();
                                }
                                break;
                            case "-1":
                                methods.getVerifyDialog(getString(R.string.error_unauth_access), response.body().getMessage());
                                break;
                            default:
                                if (response.body().getMessage().contains("already") || response.body().getMessage().contains("Invalid email format")) {
                                    editText_email.setError(response.body().getMessage());
                                    editText_email.requestFocus();
                                } else {
                                    methods.showToast(response.body().getMessage());
                                }
                                break;
                        }
                    } else {
                        methods.showToast(getString(R.string.err_server_error));
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(@NonNull Call<RespRegister> call, @NonNull Throwable t) {
                    call.cancel();
                    methods.showToast(getString(R.string.err_server_error));
                    progressDialog.dismiss();
                }
            });
        } else {
            methods.showToast(getString(R.string.err_internet_not_connected));
        }
    }

    private void loadSendVerifyOTP(String userID) {
        progressDialog.setMessage(getString(R.string.sending_verification_code));
        progressDialog.show();

        Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getSendVerifyOTP(methods.getAPIRequest(Constants.URL_SEND_VERIFY_OTP, "", "", "", "", "", "", "", "", "", "", userID, ""));
        call.enqueue(new Callback<RespSuccess>() {
            @Override
            public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                if(response.body() != null && response.body().getSuccess() != null) {
                    methods.showToast(response.body().getMessage());

                    if(response.body().getSuccess().equals("1")) {
                        Toast.makeText(RegisterActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, EmailVerificationActivity.class);
                        intent.putExtra("user_id", userID);
                        intent.putExtra("from", false);
                        startActivity(intent);
                        finish();
                    }
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