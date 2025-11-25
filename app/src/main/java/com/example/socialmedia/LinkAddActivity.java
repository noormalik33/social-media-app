package blogtalk.com.socialmedia;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
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
import blogtalk.com.items.ItemLinks;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LinkAddActivity extends AppCompatActivity {

    Methods methods;
    SharedPref sharedPref;
    ImageView iv_back;
    MaterialButton btn_add, btn_remove;
    TextInputEditText et_link_title, et_link;
    ArrayList<ItemLinks> arrayListTemp = new ArrayList<>();
    boolean isEdit = false;
    int pos = 0;
    String link1_title = "", link1 = "", link2_title = "", link2 = "", link3_title = "", link3 = "", link4_title = "", link4 = "", link5_title = "", link5 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_links);

        isEdit = getIntent().getBooleanExtra("isEdit", false);
        if (isEdit) {
            pos = getIntent().getIntExtra("pos", 0);
        } else {
            pos = Constants.arrayListLinks.size();
        }
        // pos is for which link number to edit or add

        arrayListTemp.addAll(Constants.arrayListLinks);

        sharedPref = new SharedPref(this);

        methods = new Methods(this);
        methods.forceRTLIfSupported();

        btn_add = findViewById(R.id.button_add_link);
        btn_remove = findViewById(R.id.button_add_link_remove);
        et_link_title = findViewById(R.id.et_add_link_title);
        et_link = findViewById(R.id.et_add_link);
        iv_back = findViewById(R.id.iv_links_back);

        iv_back.setOnClickListener(view -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        if (isEdit) {
            et_link_title.setText(arrayListTemp.get(pos).getTitle());
            et_link.setText(arrayListTemp.get(pos).getUrl());

            btn_add.setText(getString(R.string.update_link));
            btn_remove.setVisibility(View.VISIBLE);

            btn_remove.setOnClickListener(view -> {
                openDeleteAlertDialog(pos);
            });
        }

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!et_link.getText().toString().isEmpty() && !et_link_title.getText().toString().isEmpty()) {
                    if (!isEdit) {
                        arrayListTemp.add(new ItemLinks(et_link_title.getText().toString(), et_link.getText().toString()));
                    } else {
                        arrayListTemp.get(pos).setTitle(et_link_title.getText().toString());
                        arrayListTemp.get(pos).setUrl(et_link.getText().toString());
                    }
                    getUpdateLinks();
                } else {
                    methods.showToast(getString(R.string.err_link_empty));
                }
            }
        });

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);
    }

    private void getUpdateLinks() {
        if (methods.isNetworkAvailable()) {
            ProgressDialog progressDialog = new ProgressDialog(LinkAddActivity.this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.show();

            setLinkStrings();

            Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getLinksUpdate(methods.getAPIRequestProfile(Constants.URL_USER_LINKS_UPDATE, "", "", "", "", "", "", "", "", "", "", link1_title, link1, link2_title, link2, link3_title, link3, link4_title, link4, link5_title, link5, sharedPref.getUserId()));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                    progressDialog.dismiss();
                    if (response.body() != null && response.body().getSuccess() != null) {
                        if (response.body().getSuccess().equals("1")) {

                            Constants.isLinkChanged = true;
                            Constants.isProfileUpdate = true;
                            updateLinks();

                            Constants.arrayListLinks.clear();
                            Constants.arrayListLinks.addAll(arrayListTemp);

                            finish();
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

    private void openDeleteAlertDialog(int pos) {
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_delete_ac, null);

        BottomSheetDialog dialog_delete = new BottomSheetDialog(LinkAddActivity.this, R.style.BottomSheetDialogStyle);
        dialog_delete.setContentView(view);
        dialog_delete.show();

        MaterialButton btn_cancel = dialog_delete.findViewById(R.id.btn_del_ac_cancel);
        MaterialButton btn_delete = dialog_delete.findViewById(R.id.btn_del_ac_delete);
        btn_delete.getBackground().setTint(ContextCompat.getColor(LinkAddActivity.this, R.color.delete));
        TextView tv1 = dialog_delete.findViewById(R.id.tv1);
        TextView tv2 = dialog_delete.findViewById(R.id.tv2);

        tv1.setText(getString(R.string.delete));
        tv2.setText(getString(R.string.sure_delete_link));

        btn_cancel.setOnClickListener(v -> dialog_delete.dismiss());

        btn_delete.setOnClickListener(view1 -> {
            dialog_delete.dismiss();
            arrayListTemp.remove(pos);
            getUpdateLinks();
        });
    }

    private void setLinkStrings() {
        for (int i = 0; i < arrayListTemp.size(); i++) {
            if (i == 0) {
                link1_title = arrayListTemp.get(i).getTitle();
                link1 = arrayListTemp.get(i).getUrl();
            } else if (i == 1) {
                link2_title = arrayListTemp.get(i).getTitle();
                link2 = arrayListTemp.get(i).getUrl();
            } else if (i == 2) {
                link3_title = arrayListTemp.get(i).getTitle();
                link3 = arrayListTemp.get(i).getUrl();
            } else if (i == 3) {
                link4_title = arrayListTemp.get(i).getTitle();
                link4 = arrayListTemp.get(i).getUrl();
            } else if (i == 4) {
                link5_title = arrayListTemp.get(i).getTitle();
                link5 = arrayListTemp.get(i).getUrl();
            }
        }
    }

    private void updateLinks() {
        sharedPref.setLink1Title(link1_title);
        sharedPref.setLink1(link1);
        sharedPref.setLink2Title(link2_title);
        sharedPref.setLink2(link2);
        sharedPref.setLink3Title(link3_title);
        sharedPref.setLink3(link3);
        sharedPref.setLink4Title(link4_title);
        sharedPref.setLink4(link4);
        sharedPref.setLink5Title(link5_title);
        sharedPref.setLink5(link5);
    }
}