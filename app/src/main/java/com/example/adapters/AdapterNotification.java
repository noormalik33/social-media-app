package blogtalk.compackage blogtalk.com.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespPostDetails;
import blogtalk.com.items.ItemNotification;
import blogtalk.com.items.ItemUser;
import blogtalk.com.socialmedia.PostDetailActivity;
import blogtalk.com.socialmedia.ProfileActivity;
import blogtalk.com.socialmedia.R;
import blogtalk.com.socialmedia.TextPostDetailActivity;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterNotification extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    ArrayList<ItemNotification> arrayList;
    Methods methods;

    public AdapterNotification(Context context, ArrayList<ItemNotification> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        methods = new Methods(context);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout cl_noti;
        ImageView iv_noti, iv_main;
        TextView tv_message;

        MyViewHolder(View view) {
            super(view);
            cl_noti = view.findViewById(R.id.cl_noti);
            tv_message = view.findViewById(R.id.tv_noti_message);
            iv_noti = view.findViewById(R.id.iv_noti);
            iv_main = view.findViewById(R.id.iv_user_req);
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
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_notification, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyViewHolder) {
            if (arrayList.get(holder.getAbsoluteAdapterPosition()).getNotificationType().isEmpty()) {

                if (arrayList.get(holder.getAbsoluteAdapterPosition()).getImage() == null || arrayList.get(holder.getAbsoluteAdapterPosition()).getImage().isEmpty()) {
                    ((MyViewHolder) holder).iv_noti.setVisibility(View.GONE);
                } else {
                    ((MyViewHolder) holder).iv_noti.setVisibility(View.VISIBLE);
                    String image = "null";
                    if (!arrayList.get(holder.getAbsoluteAdapterPosition()).getImage().isEmpty()) {
                        image = arrayList.get(holder.getAbsoluteAdapterPosition()).getImage();
                    }
                    Picasso.get()
                            .load(image)
                            .placeholder(R.drawable.placeholder)
                            .into(((MyViewHolder) holder).iv_noti);
                }

                ((MyViewHolder) holder).tv_message.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getTitle().concat(" - ").concat(arrayList.get(holder.getAbsoluteAdapterPosition()).getMessage()));
                Picasso.get()
                        .load("asd")
                        .placeholder(R.drawable.placeholder)
                        .into(((MyViewHolder) holder).iv_main);
            } else {
                SpannableStringBuilder str = new SpannableStringBuilder(arrayList.get(holder.getAbsoluteAdapterPosition()).getMessage());
                try {
                    int startIndex = arrayList.get(holder.getAbsoluteAdapterPosition()).getMessage().indexOf(arrayList.get(holder.getAbsoluteAdapterPosition()).getUserName());
                    int endIndex = arrayList.get(holder.getAbsoluteAdapterPosition()).getMessage().indexOf(arrayList.get(holder.getAbsoluteAdapterPosition()).getUserName()) + arrayList.get(holder.getAbsoluteAdapterPosition()).getUserName().length();
                    str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (Exception ignore) {
                }

                ((MyViewHolder) holder).tv_message.setText(str);

                String image = "null";
                if (!arrayList.get(holder.getAbsoluteAdapterPosition()).getUserImage().isEmpty()) {
                    image = arrayList.get(holder.getAbsoluteAdapterPosition()).getUserImage();
                }
                Picasso.get()
                        .load(image)
                        .placeholder(R.drawable.placeholder)
                        .into(((MyViewHolder) holder).iv_main);

                if (arrayList.get(holder.getAbsoluteAdapterPosition()).getNotificationType().equals(Constants.TAG_NOTI_TYPE_LIKE)) {
                    ((MyViewHolder) holder).iv_noti.setVisibility(View.VISIBLE);
                    if (!arrayList.get(holder.getAbsoluteAdapterPosition()).getPostImage().isEmpty()) {
                        Picasso.get()
                                .load(arrayList.get(holder.getAbsoluteAdapterPosition()).getPostImage())
                                .placeholder(R.drawable.placeholder)
                                .into(((MyViewHolder) holder).iv_noti);
                    } else {
                        ((MyViewHolder) holder).iv_noti.setVisibility(View.GONE);
                    }

                } else {
                    ((MyViewHolder) holder).iv_noti.setVisibility(View.GONE);
                }
            }

            ((MyViewHolder) holder).cl_noti.setOnClickListener(v -> {
                if (arrayList.get(holder.getAbsoluteAdapterPosition()).getNotificationType().equals(Constants.TAG_NOTI_TYPE_ACCEPT) || arrayList.get(holder.getAbsoluteAdapterPosition()).getNotificationType().equals(Constants.TAG_NOTI_TYPE_REQUEST)) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.putExtra("item_user", new ItemUser(arrayList.get(holder.getAbsoluteAdapterPosition()).getUserID(), arrayList.get(holder.getAbsoluteAdapterPosition()).getUserName(), "null"));
                    context.startActivity(intent);
                } else if (arrayList.get(holder.getAbsoluteAdapterPosition()).getNotificationType().equals(Constants.TAG_NOTI_TYPE_LIKE)) {
                    getPostDetails(arrayList.get(holder.getAbsoluteAdapterPosition()).getPostID());
                } else if (!arrayList.get(holder.getAbsoluteAdapterPosition()).getUrl().equals("false")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(arrayList.get(holder.getAbsoluteAdapterPosition()).getUrl()));
                    context.startActivity(intent);
                }
            });
        } else {
            if (getItemCount() < 9) {
                ProgressViewHolder.progressBar.setVisibility(View.GONE);
            }
        }
    }

    private void getPostDetails(String postID) {
        if (methods.isNetworkAvailable()) {

            Call<RespPostDetails> call = APIClient.getClient().create(APIInterface.class).getPostDetails(methods.getAPIRequest(Constants.URL_POST_DETAILS, postID, "", "", "", "", "", "", "", "", "", new SharedPref(context).getUserId(), ""));

            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespPostDetails> call, @NonNull Response<RespPostDetails> response) {
                    if (response.body() != null && response.body().getStatusCode().equals("200") && response.body().getItemPost() != null) {
                        Intent intent;
                        if (!response.body().getItemPost().getPostType().equalsIgnoreCase("text")) {
                            Constants.arrayListPosts.clear();
                            Constants.arrayListPosts.add(response.body().getItemPost());
                            intent = new Intent(context, PostDetailActivity.class);
                            intent.putExtra("isuser", false);
                            intent.putExtra("pos", 0);
                        } else {
                            intent = new Intent(context, TextPostDetailActivity.class);
                            intent.putExtra("item", response.body().getItemPost());
                        }
                        context.startActivity(intent);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespPostDetails> call, @NonNull Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
//        if (position == arrayList.size()) {
//            return VIEW_PROGRESS;
//        } else {
        return position;
//        }
    }

    public void hideProgressBar() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }
}