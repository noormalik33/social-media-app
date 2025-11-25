package com.example.socialmedia;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapters.AdapterNotification;
import com.example.apiservices.APIClient;
import com.example.apiservices.APIInterface;
import com.example.apiservices.RespUserRequestsList;
import com.example.items.ItemNotification;
import com.example.utils.BackgroundTask;
import com.example.utils.Constants;
import com.example.utils.DBHelper;
import com.example.utils.Methods;
import com.example.utils.SharedPref;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import io.agora.CallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.Conversation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity {

    Methods methods;
    ImageView iv_back, iv_noti_clear;
    RecyclerView rv_noti;
    AdapterNotification adapterNotifications;
    ArrayList<ItemNotification> arrayList = new ArrayList<>();
    ConstraintLayout cl_follow_req;
    RoundedImageView iv_user_requested_2, iv_user_requested_1;
    CircularProgressBar progressBar;
    ConstraintLayout cl_empty;
    TextView tv_empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        new SharedPref(this).setNewNotification(false);

        methods = new Methods(this);
        methods.forceRTLIfSupported();

        rv_noti = findViewById(R.id.rv_noti);
        cl_follow_req = findViewById(R.id.cl_follow_req);
        iv_user_requested_1 = findViewById(R.id.iv_noti_requested_1);
        iv_user_requested_2 = findViewById(R.id.iv_noti_requested_2);
        iv_back = findViewById(R.id.iv_noti_back);
        cl_empty = findViewById(R.id.cl_empty);
        tv_empty = findViewById(R.id.tv_empty);
        progressBar = findViewById(R.id.pb_noti);
        iv_noti_clear = findViewById(R.id.iv_noti_clear);

        iv_back.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv_noti.setLayoutManager(llm);

        cl_follow_req.setOnClickListener(view -> {
            Intent intent = new Intent(NotificationActivity.this, FollowRequestActivity.class);
            intent.putExtra("isFromNoti", false);
            startActivity(intent);
        });

        iv_noti_clear.setOnClickListener(view -> {
            openClearNotificationAlert();
        });

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);

        getNotifications();
        getUserRequests();
    }

    private void getNotifications() {
        new BackgroundTask() {

            @Override
            public void onPreExecute() {

            }

            @Override
            public boolean doInBackground() {
                try (DBHelper db = new DBHelper(NotificationActivity.this)){
                    arrayList = db.getNotifications();
                } catch (Exception ignore) {}
                return false;
            }

            @Override
            public void onPostExecute(Boolean isExecutionSuccess) {
                setAdapter();
            }
        }.execute();
    }

    private void getUserRequests() {
        if (methods.isNetworkAvailable()) {

            Call<RespUserRequestsList> call = APIClient.getClient().create(APIInterface.class).getUserRequestedList(1, methods.getAPIRequest(Constants.URL_USER_REQUESTED, "", "", "", "", "", "", "", "", "", "", new SharedPref(NotificationActivity.this).getUserId(), ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespUserRequestsList> call, @NonNull Response<RespUserRequestsList> response) {
                    if (response.body() != null) {
                        if (response.body().getArrayListUserRequests() != null) {
                            if (!response.body().getArrayListUserRequests().isEmpty()) {
                                Picasso.get()
                                        .load(response.body().getArrayListUserRequests().get(0).getImage())
                                        .placeholder(R.drawable.placeholder)
                                        .into(iv_user_requested_1);
                                iv_user_requested_1.setVisibility(View.VISIBLE);

                                if (response.body().getArrayListUserRequests().size() > 1) {
                                    Picasso.get()
                                            .load(response.body().getArrayListUserRequests().get(1).getImage())
                                            .placeholder(R.drawable.placeholder)
                                            .into(iv_user_requested_2);
                                    iv_user_requested_2.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespUserRequestsList> call, @NonNull Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void setAdapter() {
        adapterNotifications = new AdapterNotification(NotificationActivity.this, arrayList);
        rv_noti.setAdapter(adapterNotifications);
        setEmpty();
    }

    private void setEmpty() {
        progressBar.setVisibility(View.GONE);
        if (!arrayList.isEmpty()) {
            rv_noti.setVisibility(View.VISIBLE);
            cl_empty.setVisibility(View.GONE);
        } else {
            rv_noti.setVisibility(View.GONE);
            tv_empty.setText(getString(R.string.err_no_data_found));
            cl_empty.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void openClearNotificationAlert() {
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.layout_bottom_delete_ac, null);

        BottomSheetDialog dialog = new BottomSheetDialog(NotificationActivity.this, R.style.BottomSheetDialogStyle);
        dialog.setContentView(view);
        dialog.show();

        MaterialButton btn_delete = dialog.findViewById(R.id.btn_del_ac_delete);
        MaterialButton btn_cancel = dialog.findViewById(R.id.btn_del_ac_cancel);
        TextView tv1 = dialog.findViewById(R.id.tv1);
        TextView tv2 = dialog.findViewById(R.id.tv2);

        tv1.setText(getString(R.string.clear_notification));
        tv2.setText(getString(R.string.sure_clear_notification));

        btn_delete.setOnClickListener(view1 -> {
            dialog.dismiss();
            try (DBHelper db = new DBHelper(NotificationActivity.this)){
                db.clearNotifications();
                arrayList.clear();
                adapterNotifications.notifyDataSetChanged();
                getNotifications();
            } catch (Exception ignore) {}
        });

        btn_cancel.setOnClickListener(view1 -> {
            dialog.dismiss();
        });
    }
}