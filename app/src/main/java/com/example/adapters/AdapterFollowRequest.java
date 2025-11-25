package blogtalk.compackage blogtalk.com.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import blogtalk.com.interfaces.ActionDoneListener;
import blogtalk.com.items.ItemUser;
import blogtalk.com.items.ItemUserRequests;
import blogtalk.com.socialmedia.ProfileActivity;
import blogtalk.com.socialmedia.R;
import blogtalk.com.utils.Methods;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterFollowRequest extends RecyclerView.Adapter {

    Context context;
    ArrayList<ItemUserRequests> arrayList;
    final int VIEW_PROGRESS = -1;
    Methods methods;
    ActionDoneListener actionDoneListener;

    public AdapterFollowRequest(Context context, ArrayList<ItemUserRequests> arrayList, ActionDoneListener actionDoneListener) {
        this.context = context;
        this.arrayList = arrayList;
        this.actionDoneListener = actionDoneListener;
        methods = new Methods(context);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout cl_requests;
        ImageView iv_user;
        TextView tv_username;
        MaterialButton btn_accept, btn_decline;

        MyViewHolder(View view) {
            super(view);
            cl_requests = view.findViewById(R.id.cl_requests);
            tv_username = view.findViewById(R.id.tv_req_username);
            iv_user = view.findViewById(R.id.iv_user_req);
            btn_accept = view.findViewById(R.id.btn_req_accept);
            btn_decline = view.findViewById(R.id.btn_req_decline);
        }
    }

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {
        private static ProgressBar progressBar;

        private ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_PROGRESS) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_progressbar, parent, false);
            return new ProgressViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user_requests, parent, false);
            return new MyViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyViewHolder) {
            ((MyViewHolder) holder).tv_username.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getUserName());
            Picasso.get()
                    .load(arrayList.get(holder.getAbsoluteAdapterPosition()).getImage())
                    .placeholder(R.drawable.placeholder)
                    .into(((MyViewHolder) holder).iv_user);

            ((MyViewHolder) holder).cl_requests.setOnClickListener(v ->{
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("item_user", new ItemUser(arrayList.get(holder.getAbsoluteAdapterPosition()).getUserID(), arrayList.get(holder.getAbsoluteAdapterPosition()).getUserName(), arrayList.get(holder.getAbsoluteAdapterPosition()).getImage()));
                context.startActivity(intent);
            });

            ((MyViewHolder) holder).btn_accept.setOnClickListener(view -> {
                methods.getAcceptDeclineRequest(arrayList.get(holder.getAbsoluteAdapterPosition()).getRequestID(), arrayList.get(holder.getAbsoluteAdapterPosition()).getUserID(), true, new ActionDoneListener() {
                    @Override
                    public void onWorkDone(String success, boolean isDone, int position) {
                        arrayList.remove(holder.getAbsoluteAdapterPosition());
                        notifyItemRemoved(holder.getAbsoluteAdapterPosition());
                        actionDoneListener.onWorkDone(success, isDone, position);
                    }
                });
            });

            ((MyViewHolder) holder).btn_decline.setOnClickListener(view -> {
                methods.getAcceptDeclineRequest(arrayList.get(holder.getAbsoluteAdapterPosition()).getRequestID(), arrayList.get(holder.getAbsoluteAdapterPosition()).getUserID(), false, new ActionDoneListener() {
                    @Override
                    public void onWorkDone(String success, boolean isDone, int position) {
                        arrayList.remove(holder.getAbsoluteAdapterPosition());
                        notifyItemRemoved(holder.getAbsoluteAdapterPosition());
                        actionDoneListener.onWorkDone(success, isDone, position);
                    }
                });
            });
        } else {
            if (getItemCount() < 9) {
                ProgressViewHolder.progressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == arrayList.size()) {
            return VIEW_PROGRESS;
        } else {
            return position;
        }
    }

    public void hideProgressBar() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }
}