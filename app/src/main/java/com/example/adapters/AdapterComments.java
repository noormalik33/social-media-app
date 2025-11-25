package com.example.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apiservices.APIClient;
import com.example.apiservices.APIInterface;
import com.example.apiservices.RespSuccess;
import com.example.interfaces.EditCommentListener;
import com.example.items.ItemComments;
import com.example.items.ItemUser;
import com.example.socialmedia.ProfileActivity;
import com.example.socialmedia.R;
import com.example.utils.Constants;
import com.example.utils.Methods;
import com.example.utils.SharedPref;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyViewHolder> {

    Context context;
    ArrayList<ItemComments> arrayList;
    String userID = "";
    Methods methods;
    EditCommentListener editCommentListener;

    public AdapterComments(Context context, ArrayList<ItemComments> arrayList, String userID, EditCommentListener editCommentListener) {
        this.context = context;
        this.arrayList = arrayList;
        this.userID = userID;
        this.editCommentListener = editCommentListener;
        methods = new Methods(context);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_user, iv_more, iv_acc_verified;
        TextView tv_comment, tv_date, tv_username;
        View view_pages;

        MyViewHolder(View view) {
            super(view);
            iv_user = view.findViewById(R.id.iv_comments_user);
            iv_more = view.findViewById(R.id.iv_comments_more);
            tv_comment = view.findViewById(R.id.tv_comments_text);
            tv_date = view.findViewById(R.id.tv_comments_date);
            tv_username = view.findViewById(R.id.tv_comments_username);
            iv_acc_verified = itemView.findViewById(R.id.iv_comments_account_verify);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_comments, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.iv_acc_verified.setVisibility(new SharedPref(context).getIsAccountVerifyOn() && arrayList.get(position).getIsUserAccVerified() ? View.VISIBLE : View.GONE);

        holder.tv_comment.setText(arrayList.get(position).getCommentText());
        holder.tv_date.setText(arrayList.get(position).getDate());
        holder.tv_username.setText(arrayList.get(position).getUserName());
        Picasso.get()
                .load(arrayList.get(position).getUserImage())
                .placeholder(R.drawable.placeholder)
                .into(holder.iv_user);

        if(!arrayList.get(holder.getAbsoluteAdapterPosition()).getUserID().equals(userID)) {
            holder.iv_more.setVisibility(View.GONE);
        } else {
            holder.iv_more.setOnClickListener(view -> {
                openOptionPopUp(holder.iv_more, holder.getAbsoluteAdapterPosition());
            });
        }

        holder.iv_user.setOnClickListener(view -> {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("item_user", new ItemUser(arrayList.get(holder.getAbsoluteAdapterPosition()).getUserID(), arrayList.get(holder.getAbsoluteAdapterPosition()).getUserName(), arrayList.get(holder.getAbsoluteAdapterPosition()).getUserImage()));
            context.startActivity(intent);
        });

        holder.tv_username.setOnClickListener(view -> {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("item_user", new ItemUser(arrayList.get(holder.getAbsoluteAdapterPosition()).getUserID(), arrayList.get(holder.getAbsoluteAdapterPosition()).getUserName(), arrayList.get(holder.getAbsoluteAdapterPosition()).getUserImage()));
            context.startActivity(intent);
        });
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    private void openOptionPopUp(ImageView imageView, final int pos) {
        ContextThemeWrapper ctw = new ContextThemeWrapper(context, R.style.PopupMenu);
        PopupMenu popup = new PopupMenu(ctw, imageView);
        popup.getMenuInflater().inflate(R.menu.popup_comment, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.popup_edit_comment) {
                editCommentListener.onEdit(pos);
            } else if (item.getItemId() == R.id.popup_del_comment) {
                getDeleteComment(pos);
            }
            return true;
        });
        popup.show();
    }

    private void getDeleteComment(int pos) {
        if (methods.isNetworkAvailable()) {

            ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(context.getResources().getString(R.string.loading));
            progressDialog.setCancelable(false);
            progressDialog.show();

            Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getDeleteComment(methods.getAPIRequest(Constants.URL_DELETE_COMMENT, "", "", "", "", arrayList.get(pos).getId(), "", "", "", "", "", "", ""));
            call.enqueue(new Callback<RespSuccess>() {
                @Override
                public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                    progressDialog.dismiss();
                    if (response.body() != null && response.body().getSuccess() != null) {
                        if (response.body().getSuccess().equals("1")) {
                            methods.showToast(context.getString(R.string.comment_del));

                            arrayList.remove(pos);
                            notifyItemRemoved(pos);

                            editCommentListener.onDelete();
                        } else {
                            methods.showToast(response.message());
                        }
                    } else {
                        methods.showToast(context.getString(R.string.err_server_error));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespSuccess> call, @NonNull Throwable t) {
                    call.cancel();
                    progressDialog.dismiss();
                    methods.showToast(context.getString(R.string.err_server_error));
                }
            });
        } else {
            methods.showToast(context.getString(R.string.err_internet_not_connected));
        }
    }
}