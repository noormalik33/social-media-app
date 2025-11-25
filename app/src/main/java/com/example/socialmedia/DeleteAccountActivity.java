package blogtalk.com.socialmedia;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespSuccess;
import blogtalk.com.apiservices.RespTotalPost;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeleteAccountActivity extends AppCompatActivity {

    Methods methods;
    SharedPref sharedPref;
    MaterialToolbar toolbar;
    MaterialButton button_delete;
    ImageView iv_profile, iv_back;
    TextView tv_name, tv_total_images, tv_total_videos;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        sharedPref = new SharedPref(this);
        methods = new Methods(this);
        methods.forceRTLIfSupported();

        toolbar = findViewById(R.id.toolbar_delete_acc);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        button_delete = findViewById(R.id.button_delete_acc);
        tv_name = findViewById(R.id.tv_delete_acc_name);
        tv_total_images = findViewById(R.id.tv_delete_acc_total_image);
        tv_total_videos = findViewById(R.id.tv_delete_acc_total_video);
        iv_profile = findViewById(R.id.iv_delete_account);
        iv_back = findViewById(R.id.iv_delete_acc_back);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        Picasso.get().load(sharedPref.getUserImage()).placeholder(R.drawable.placeholder).into(iv_profile);

        button_delete.setOnClickListener(v -> {
            openDeleteAlertDialog();
        });

        tv_name.setText(sharedPref.getName());
        iv_back.setOnClickListener(view -> onBackPressed());

        getTotalPost();

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);
    }

    private void getTotalPost() {
        if (methods.isNetworkAvailable()) {
            progressDialog.show();

            Call<RespTotalPost> call = APIClient.getClient().create(APIInterface.class).getTotalPost(methods.getAPIRequest(Constants.URL_TOTAL_POST, "", "", "", "", "", "", "", "", "", "", sharedPref.getUserId(), ""));
            call.enqueue(new Callback<RespTotalPost>() {
                @Override
                public void onResponse(@NonNull Call<RespTotalPost> call, @NonNull Response<RespTotalPost> response) {
                    progressDialog.dismiss();
                    if (response.body() != null && response.body().getItemTotalPost() != null) {
                        if (response.body().getItemTotalPost().getImagePost() != null) {
                            tv_total_images.setText(response.body().getItemTotalPost().getImagePost());
                            tv_total_videos.setText(response.body().getItemTotalPost().getVideoPost());
                        }
                    } else {
                        methods.showToast(getString(R.string.err_server_error));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespTotalPost> call, @NonNull Throwable t) {
                    call.cancel();
                    progressDialog.dismiss();
                    methods.showToast(getString(R.string.err_server_error));
                }
            });
        } else {
            methods.showToast(getString(R.string.err_internet_not_connected));
        }
    }

    private void getDeleteAccount() {
        if (methods.isNetworkAvailable()) {
            progressDialog.show();

            Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getDeleteAccount(methods.getAPIRequest(Constants.URL_DELETE_ACCOUNT, "", "", "", "", "", "", "", "", "", "", sharedPref.getUserId(), ""));
            call.enqueue(new Callback<RespSuccess>() {
                @Override
                public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                    progressDialog.dismiss();
                    if (response.body() != null && response.body().getSuccess() != null) {
                        if (response.body().getSuccess().equals("1")) {
                            methods.showToast(getString(R.string.account_del_succ));

                            methods.logout(DeleteAccountActivity.this, sharedPref);
                        } else {
                            methods.showToast(response.message());
                        }
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

    private void openDeleteAlertDialog() {
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_delete_ac, null);

        BottomSheetDialog dialog_theme = new BottomSheetDialog(DeleteAccountActivity.this, R.style.BottomSheetDialogStyle);
        dialog_theme.setContentView(view);
        dialog_theme.show();

        MaterialButton btn_cancel = dialog_theme.findViewById(R.id.btn_del_ac_cancel);
        MaterialButton btn_delete = dialog_theme.findViewById(R.id.btn_del_ac_delete);
        btn_delete.getBackground().setTint(ContextCompat.getColor(DeleteAccountActivity.this, R.color.delete));

        btn_cancel.setOnClickListener(v -> dialog_theme.dismiss());

        btn_delete.setOnClickListener(view1 -> {
            getDeleteAccount();
        });
    }
}