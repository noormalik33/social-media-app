package com.example.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.items.ItemChatList;
import com.example.items.ItemUser;
import com.example.socialmedia.ChatActivity;
import com.example.socialmedia.ProfileActivity;
import com.example.socialmedia.R;
import com.example.utils.Constants;
import com.example.utils.Methods;
import com.example.utils.SharedPref;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import io.agora.CallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.Conversation;
import io.agora.chat.TextMessageBody;
import io.agora.exceptions.ChatException;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.MyViewHolder> {

    Context context;
    ArrayList<Conversation> arrayList;
    ArrayList<ItemChatList> arrayListChatList;
    Methods methods;

    public AdapterChatList(Context context, ArrayList<Conversation> arrayList, ArrayList<ItemChatList> arrayListChatList) {
        this.context = context;
        this.arrayList = arrayList;
        this.arrayListChatList = arrayListChatList;
        methods = new Methods(context);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout cl_chat_list;
        MaterialButton btn_unread_count;
        RoundedImageView iv_user;
        TextView tv_chat_list, tv_message;
        ImageView iv_acc_verify;

        MyViewHolder(View view) {
            super(view);
            cl_chat_list = view.findViewById(R.id.cl_chat_list);
            btn_unread_count = view.findViewById(R.id.btn_chat_unread_count);
            iv_user = view.findViewById(R.id.iv_chat_list);
            tv_chat_list = view.findViewById(R.id.tv_chat_list);
            tv_message = view.findViewById(R.id.tv_chat_list_msg);
            iv_acc_verify = view.findViewById(R.id.iv_chat_account_verify);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat_list, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.iv_acc_verify.setVisibility((!new SharedPref(context).getIsAccountVerifyOn() || !arrayListChatList.get(holder.getAbsoluteAdapterPosition()).getIsUserAccountVerified()) ? View.GONE : View.VISIBLE);

        holder.tv_chat_list.setText(!arrayListChatList.get(holder.getAbsoluteAdapterPosition()).isUserDeleted() ? arrayListChatList.get(holder.getAbsoluteAdapterPosition()).getName() : "User Name");

        if(arrayList.get(holder.getAbsoluteAdapterPosition()).getLastMessage() != null) {
            holder.tv_message.setText(((TextMessageBody) arrayList.get(holder.getAbsoluteAdapterPosition()).getLastMessage().getBody()).getMessage());
        } else {
            holder.tv_message.setText("");
        }

        Picasso.get()
                .load(!arrayListChatList.get(holder.getAbsoluteAdapterPosition()).isUserDeleted()
                        ? arrayListChatList.get(holder.getAbsoluteAdapterPosition()).getImage()
                        : "null")
                .placeholder(R.drawable.ic_user)
                .into(holder.iv_user);

        if(arrayList.get(holder.getAbsoluteAdapterPosition()).getUnreadMsgCount() == 0) {
            holder.btn_unread_count.setVisibility(View.GONE);
        } else {
            holder.btn_unread_count.setText(String.valueOf(arrayList.get(holder.getAbsoluteAdapterPosition()).getUnreadMsgCount()));
            holder.btn_unread_count.setVisibility(View.VISIBLE);
        }

        holder.cl_chat_list.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("id",arrayList.get(holder.getAbsoluteAdapterPosition()).conversationId());
            intent.putExtra("name", arrayListChatList.get(holder.getAbsoluteAdapterPosition()).getName());
            intent.putExtra("item", arrayListChatList.get(holder.getAbsoluteAdapterPosition()));
            ((Activity)context).startActivityForResult(intent, 111);

            arrayList.get(holder.getAbsoluteAdapterPosition()).markAllMessagesAsRead();
            try {
                ChatClient.getInstance().chatManager().ackConversationRead(arrayList.get(holder.getAbsoluteAdapterPosition()).conversationId());
            } catch (ChatException e) {
                e.printStackTrace();
            }
        });

        holder.cl_chat_list.setOnLongClickListener(view -> {
            openMoreBottomSheet(holder.getAbsoluteAdapterPosition());
            return false;
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

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void openMoreBottomSheet(int pos) {
        @SuppressLint("InflateParams") View view = ((Activity)context).getLayoutInflater().inflate(R.layout.layout_bottom_chat, null);

        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.BottomSheetDialogStyle);
        dialog.setContentView(view);
        dialog.show();

        TextView tv_prof = dialog.findViewById(R.id.tv_chat_view_prof);
        TextView tv_clear = dialog.findViewById(R.id.tv_chat_clear);
        TextView tv_delete = dialog.findViewById(R.id.tv_chat_delete);

        assert tv_prof != null;
        tv_prof.setOnClickListener(view12 -> {
            dialog.dismiss();
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("item_user", new ItemUser(arrayList.get(pos).conversationId(), arrayListChatList.get(pos).getName(), "", "","","","No"));
            context.startActivity(intent);
        });

        assert tv_clear != null;
        tv_clear.setOnClickListener(view1 -> {
            dialog.dismiss();
            ChatClient.getInstance().chatManager().getConversation(arrayList.get(pos).conversationId()).removeMessagesFromServer(System.currentTimeMillis(), new CallBack() {
                @Override
                public void onSuccess() {
                    ChatClient.getInstance().chatManager().getConversation(arrayList.get(pos).conversationId()).clearAllMessages();
                    ((Activity)context).runOnUiThread(() -> {
                        arrayList.get(pos).clearAllMessages();
                        notifyItemChanged(pos);
                    });
                }

                @Override
                public void onError(int code, String error) {

                }
            });
        });

        tv_delete.setOnClickListener(view1 -> {
            dialog.dismiss();
            openDeleteAlertBottomSheet(pos);
        });
    }

    private void openDeleteAlertBottomSheet(int pos) {
        @SuppressLint("InflateParams") View view = ((Activity)context).getLayoutInflater().inflate(R.layout.layout_bottom_delete_ac, null);

        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.BottomSheetDialogStyle);
        dialog.setContentView(view);
        dialog.show();

        MaterialButton btn_delete = dialog.findViewById(R.id.btn_del_ac_delete);
        MaterialButton btn_cancel = dialog.findViewById(R.id.btn_del_ac_cancel);
        TextView tv1 = dialog.findViewById(R.id.tv1);
        TextView tv2 = dialog.findViewById(R.id.tv2);

        tv1.setText(context.getString(R.string.delete_conversation));
        tv2.setText(context.getString(R.string.sure_delete_conversation));

        btn_delete.setOnClickListener(view1 -> {
            dialog.dismiss();
            ChatClient.getInstance().chatManager().deleteConversationFromServer(arrayList.get(pos).conversationId(), Conversation.ConversationType.Chat, true, new CallBack() {
                @Override
                public void onSuccess() {
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ChatClient.getInstance().chatManager().deleteConversation(arrayList.get(pos).conversationId(),true);
                            Constants.isChatConversationDeleted = true;
                            arrayList.remove(pos);
                            arrayListChatList.remove(pos);
                            notifyItemRemoved(pos);
                        }
                    });
                }

                @Override
                public void onError(int code, String error) {
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            methods.showToast("Error deleting");
                        }
                    });
                }
            });
        });

        btn_cancel.setOnClickListener(view1 -> {
            dialog.dismiss();
        });
    }
}