package blogtalk.com.socialmedia;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespSuccess;
import blogtalk.com.items.ItemUser;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WithdrawActivity extends AppCompatActivity {

    Methods methods;
    SharedPref sharedPref;
    MaterialButton btn_save_payment_info, btn_submit;
    TextInputLayout til_payment_info;
    TextInputEditText et_points, et_payment_info;
    RoundedImageView iv_payment_edit;
    TextView tv_available_points, tv_available_money, tv_money, tv_payment_info;
    ProgressDialog progressDialog;
    ItemUser itemUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);

        itemUser = (ItemUser) getIntent().getSerializableExtra("item");

        methods = new Methods(this);
        sharedPref = new SharedPref(this);
        methods.forceRTLIfSupported();

        findViewById(R.id.iv_back).setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        btn_save_payment_info = findViewById(R.id.btn_save_payment_info);
        btn_submit = findViewById(R.id.btn_submit);
        et_points = findViewById(R.id.et_points);
        tv_money = findViewById(R.id.tv_money_from_points);
        tv_available_points = findViewById(R.id.tv_available_points);
        tv_available_money = findViewById(R.id.tv_available_money);
        til_payment_info = findViewById(R.id.til_payment_info);
        et_payment_info = findViewById(R.id.et_payment_info);
        iv_payment_edit = findViewById(R.id.iv_withdraw_payment_edit);
        tv_payment_info = findViewById(R.id.tv_payment_info);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));

        double money = (float) (itemUser.getTotalPoints() * Constants.oneMoney) / Constants.onePoint;
        tv_available_points.setText(String.valueOf(itemUser.getTotalPoints()));
        tv_available_money.setText("(".concat(methods.getCurrencySymbol(sharedPref.getCurrencyCode())).concat(String.format(Locale.getDefault(), "%.2f", money)).concat(")"));
        et_payment_info.setText(itemUser.getPaymentInfo());
        tv_payment_info.setText(itemUser.getPaymentInfo());

        tv_money.setText("(".concat(methods.getCurrencySymbol(sharedPref.getCurrencyCode())).concat("0").concat(")"));

        btn_save_payment_info.setOnClickListener(view -> {
            if (!et_payment_info.getText().toString().trim().isEmpty()) {
                getSavePaymentInfo(et_payment_info.getText().toString());
            } else {
                methods.showToast(getString(R.string.err_enter_payment_info));
            }
        });

        btn_submit.setOnClickListener(view -> {
            if (!itemUser.getPaymentInfo().isEmpty()) {
                if (methods.isLoggedAndVerified(true)) {
                    if (!et_points.getText().toString().trim().isEmpty() && (Integer.parseInt(et_points.getText().toString()) >= sharedPref.getMinWithdrawPoints())) {
                        if (Integer.parseInt(et_points.getText().toString()) <= itemUser.getTotalPoints()) {
                            getWithdrawRequest(et_points.getText().toString());
                        } else {
                            methods.showToast(getString(R.string.err_not_enough_points));
                        }
                    } else {
                        methods.showToast(getString(R.string.min_points_withdraw, String.valueOf(sharedPref.getMinWithdrawPoints())));
                    }
                }
            } else {
                methods.showToast(getString(R.string.err_enter_payment_info));
            }
        });

        et_points.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    double money = (float) (Integer.parseInt(charSequence.toString()) * Constants.oneMoney) / Constants.onePoint;
//                    tv_money.setText(methods.getCurrencySymbol(sharedPref.getCurrencyCode()).concat(" ").concat());
                    tv_money.setText("(".concat(methods.getCurrencySymbol(sharedPref.getCurrencyCode())).concat(String.format(Locale.getDefault(), "%.2f", money)).concat(")"));
                } else {
                    tv_money.setText("(".concat(methods.getCurrencySymbol(sharedPref.getCurrencyCode())).concat("0").concat(")"));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        iv_payment_edit.setOnClickListener(view -> {
            if (btn_save_payment_info.getVisibility() == View.GONE) {
                btn_save_payment_info.setVisibility(View.VISIBLE);
                til_payment_info.setVisibility(View.VISIBLE);
                tv_payment_info.setVisibility(View.GONE);
            } else {
                btn_save_payment_info.setVisibility(View.GONE);
                til_payment_info.setVisibility(View.GONE);
                tv_payment_info.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getSavePaymentInfo(String paymentInfo) {
        if (methods.isNetworkAvailable()) {
            progressDialog.show();

            Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getUpdatePaymentInfo(methods.getAPIRequest(Constants.URL_UPDATE_PAYMENT_INFO, "", "", "", paymentInfo, "", "", "", "", "", "", sharedPref.getUserId(), ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                    if (response.body() != null && response.body().getItemSuccess() != null) {
                        if (response.body().getItemSuccess().getSuccess().equalsIgnoreCase("1")) {
                            itemUser.setPaymentInfo(paymentInfo);
                            tv_payment_info.setText(paymentInfo);
                            btn_save_payment_info.setVisibility(View.GONE);
                            til_payment_info.setVisibility(View.GONE);
                            tv_payment_info.setVisibility(View.VISIBLE);

                            methods.showToast(response.body().getItemSuccess().getMessage());
                        } else {
                            methods.showToast(response.body().getMessage());
                        }
                    } else {
                        methods.showToast(getString(R.string.err_server_error));
                    }
                    progressDialog.dismiss();
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

    private void getWithdrawRequest(String points) {
        if (methods.isNetworkAvailable()) {
            progressDialog.show();

            Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getWithdrawRequest(methods.getAPIRequest(Constants.URL_WITHDRAW_REQUEST, "", "", "", points, "", "", "", "", "", "", sharedPref.getUserId(), ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                    if (response.body() != null) {
                        if (response.body().getItemSuccess() != null) {

                            methods.showToast(response.body().getItemSuccess().getMessage());

                            Intent intent = new Intent();
                            intent.putExtra("withdraw", true);
                            intent.putExtra("points", Integer.parseInt(points));
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            methods.showToast(getString(R.string.err_server_error));
                        }
                    } else {
                        methods.showToast(getString(R.string.err_server_error));
                    }
                    progressDialog.dismiss();
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
}