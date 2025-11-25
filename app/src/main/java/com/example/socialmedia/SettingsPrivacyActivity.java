package blogtalk.com.socialmedia;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespSuccess;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsPrivacyActivity extends AppCompatActivity {

    SharedPref sharedPref;
    Methods methods;
    ImageView iv_back;
    ConstraintLayout cl_change_password, cl_acc_privacy, cl_delete_acc;
    TextView tv_acc_privacy;
    SwitchMaterial switch_acc_privacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_privacy);

        sharedPref = new SharedPref(this);
        methods = new Methods(this);
        methods.forceRTLIfSupported();

        iv_back = findViewById(R.id.iv_privacy_back);
        cl_acc_privacy = findViewById(R.id.cl_privacy_acc);
        cl_change_password = findViewById(R.id.cl_privacy_change_pass);
        cl_delete_acc = findViewById(R.id.cl_privacy_delete_acc);
        tv_acc_privacy = findViewById(R.id.tv_privacy_acc);
        switch_acc_privacy = findViewById(R.id.switch_privacy_acc_pri);

        switch_acc_privacy.setChecked(sharedPref.getProfilePrivacy().equals(Constants.TAG_PROFILE_PRIVATE));
        setAccountPrivacyText();

        iv_back.setOnClickListener(view -> onBackPressed());

        if(sharedPref.getLoginType().equals(Constants.LOGIN_TYPE_NORMAL)) {
            cl_change_password.setVisibility(View.VISIBLE);
            cl_change_password.setOnClickListener(view -> {
                openBottomSheetChangePassword();
            });
        } else {
            cl_change_password.setVisibility(View.GONE);
        }

        cl_acc_privacy.setOnClickListener(view -> {

        });

        cl_delete_acc.setOnClickListener(view -> {
            Intent intent = new Intent(SettingsPrivacyActivity.this, DeleteAccountActivity.class);
            startActivity(intent);
        });


//        switch_acc_privacy.setOnCheckedChangeListener((buttonView, isChecked) -> {
//
//        });

        switch_acc_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switch_acc_privacy.isChecked()) {
                    getChangeAccountPrivacy(Constants.TAG_PROFILE_PRIVATE);
                } else {
                    getChangeAccountPrivacy(Constants.TAG_PROFILE_PUBLIC);
                }
            }
        });
    }



    private void openBottomSheetChangePassword() {
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.layout_bottom_change_password, null);

        BottomSheetDialog dialog = new BottomSheetDialog(SettingsPrivacyActivity.this, R.style.BottomSheetDialogStyle);
        dialog.setContentView(view);
        dialog.show();

        TextInputEditText et_old_pass = dialog.findViewById(R.id.et_change_pass_old);
        TextInputEditText et_new_pass = dialog.findViewById(R.id.et_change_pass_new);
        TextInputEditText et_confirm_pass = dialog.findViewById(R.id.et_change_pass_confirm);
        MaterialButton bnt_cancel = dialog.findViewById(R.id.btn_change_pass_cancel);
        MaterialButton bnt_update = dialog.findViewById(R.id.btn_change_pass_update);

        assert bnt_cancel != null;
        bnt_cancel.setOnClickListener(view1 -> {
            dialog.dismiss();
        });

        assert bnt_update != null;
        bnt_update.setOnClickListener(view1 -> {
            assert et_old_pass != null;
            et_old_pass.setError(null);
            assert et_new_pass != null;
            et_new_pass.setError(null);
            assert et_confirm_pass != null;
            et_confirm_pass.setError(null);

            if(et_old_pass.getText().toString().trim().isEmpty()){
                et_old_pass.setError(getString(R.string.cannot_empty));
                et_old_pass.requestFocus();
            } else if(!et_old_pass.getText().toString().equals(sharedPref.getPassword())){
                et_old_pass.setError(getString(R.string.err_old_password_wrong));
                et_old_pass.requestFocus();
            } else if(et_new_pass.getText().toString().trim().isEmpty()){
                et_new_pass.setError(getString(R.string.cannot_empty));
                et_new_pass.requestFocus();
            } else if(et_confirm_pass.getText().toString().trim().isEmpty()){
                et_confirm_pass.setError(getString(R.string.cannot_empty));
                et_confirm_pass.requestFocus();
            } else if(!et_confirm_pass.getText().toString().equals(et_new_pass.getText().toString())){
                et_confirm_pass.setError(getString(R.string.err_password_cpass_not_matched));
                et_confirm_pass.requestFocus();
            }else {
                getChangePassword(et_new_pass.getText().toString());
            }
        });
    }

    private void getChangePassword(String password) {
        if (methods.isNetworkAvailable()) {
            ProgressDialog progressDialog = new ProgressDialog(SettingsPrivacyActivity.this);
            progressDialog.setTitle(getString(R.string.change_password));
            progressDialog.setMessage(getString(R.string.changing_password));
            progressDialog.show();

            Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getChangePassword(methods.getAPIRequest(Constants.URL_CHANGE_PASSWORD, "", "", "", "", "", "", "", "", password, "", sharedPref.getUserId(), ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                    progressDialog.dismiss();
                    if (response.body() != null) {
                        progressDialog.dismiss();
                        if (response.body().getSuccess().equals("1")) {

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
                }
            });
        } else {
            methods.showToast(getString(R.string.err_internet_not_connected));
        }
    }

    private void getChangeAccountPrivacy(String privacy) {
        if (methods.isNetworkAvailable()) {
            ProgressDialog progressDialog = new ProgressDialog(SettingsPrivacyActivity.this);
            progressDialog.setTitle(getString(R.string.account_privacy));
            progressDialog.setMessage(getString(R.string.changing_acc_privacy));
            progressDialog.show();

            Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getUserPrivacyUpdate(methods.getAPIRequest(Constants.URL_USER_PRIVACY_UPDATE, "", "", "", "", "", "", "", "", "", "", sharedPref.getUserId(), privacy));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                    progressDialog.dismiss();
                    if (response.body() != null && response.body().getSuccess() != null) {
                        if (response.body().getSuccess().equals("1")) {
                            sharedPref.setProfilePrivacy(privacy);
                            setAccountPrivacyText();
                        } else {
                            switch_acc_privacy.setChecked(!switch_acc_privacy.isChecked());
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

    private void setAccountPrivacyText() {
        if (sharedPref.getProfilePrivacy().equals(Constants.TAG_PROFILE_PUBLIC)) {
            tv_acc_privacy.setText(getString(R.string.your_account_is_public));
        } else {
            tv_acc_privacy.setText(getString(R.string.your_account_is_private));
        }
    }
}