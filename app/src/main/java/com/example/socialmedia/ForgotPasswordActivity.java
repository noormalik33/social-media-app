package blogtalk.com.socialmedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespSuccess;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.Methods;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    Methods methods;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        methods = new Methods(this);
        methods.forceRTLIfSupported();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        ImageView iv_back = findViewById(R.id.iv_forgot_back);
        iv_back.setOnClickListener(view-> getOnBackPressedDispatcher().onBackPressed());

        TextInputEditText et_email = findViewById(R.id.et_forgot_email);
        MaterialButton button_send = findViewById(R.id.button_send);

        button_send.setOnClickListener(view -> {
            if(et_email.getText().toString().isEmpty()) {
                et_email.setError(getResources().getString(R.string.enter_email));
                et_email.requestFocus();
            } else if(!isEmailValid(et_email.getText().toString())) {
                et_email.setError(getResources().getString(R.string.err_invalid_email));
                et_email.requestFocus();
            } else {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et_email.getWindowToken(), 0);
                et_email.clearFocus();

                loadForgotPass(et_email.getText().toString());
            }
        });

    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && !email.contains(" ");
    }

    private void loadForgotPass(String email) {
        progressDialog.show();

        Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getForgotPassword(methods.getAPIRequest(Constants.URL_FORGOT_PASSWORD, "", "", "", "", "", "", "", email, "", "", "", ""));
        call.enqueue(new Callback<RespSuccess>() {
            @Override
            public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                if(response.body() != null) {
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