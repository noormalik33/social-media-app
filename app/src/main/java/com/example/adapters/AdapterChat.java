package com.example.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat.ChatDateUtils;
import com.example.socialmedia.R;
import com.example.utils.Methods;

import java.util.ArrayList;
import java.util.Date;

import io.agora.chat.ChatMessage;
import io.agora.chat.TextMessageBody;

public class AdapterChat extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    ArrayList<ChatMessage> arrayList;
    final int VIEW_MESSAGE_SENT = -2;
    final int VIEW_MESSAGE_RECEIVED = -3;
    Methods methods;

    public AdapterChat(Context context, ArrayList<ChatMessage> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        methods = new Methods(context);
    }

    static class MessageSendHolder extends RecyclerView.ViewHolder {
        TextView tv_chat, tv_time;
        ImageView iv_status;

        MessageSendHolder(View view) {
            super(view);
            tv_chat = view.findViewById(R.id.tv_chat);
            tv_time = view.findViewById(R.id.tv_chat_time);
            iv_status = view.findViewById(R.id.iv_chat_status);
        }
    }

    static class MessageReceiveHolder extends RecyclerView.ViewHolder {
        TextView tv_chat, tv_time;

        MessageReceiveHolder(View view) {
            super(view);
            tv_chat = view.findViewById(R.id.tv_chat);
            tv_time = view.findViewById(R.id.tv_chat_time);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_MESSAGE_RECEIVED) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat_received, parent, false);
            return new MessageReceiveHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat_sent, parent, false);
            return new MessageSendHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MessageSendHolder) {
            ((MessageSendHolder) holder).tv_chat.setText(((TextMessageBody) arrayList.get(holder.getAbsoluteAdapterPosition()).getBody()).getMessage());
            setTimestamp(((MessageSendHolder) holder).tv_time, arrayList.get(holder.getAbsoluteAdapterPosition()), holder.getAbsoluteAdapterPosition());

            if (arrayList.get(holder.getAbsoluteAdapterPosition()).isDelivered()) {
                ((MessageSendHolder) holder).iv_status.setImageResource(R.drawable.ic_msg_delivered);
                if (!arrayList.get(holder.getAbsoluteAdapterPosition()).isUnread()) {
                    ((MessageSendHolder) holder).iv_status.setColorFilter(ContextCompat.getColor(context, R.color.primary), PorterDuff.Mode.SRC_IN);
                } else {
                    ((MessageSendHolder) holder).iv_status.setColorFilter(null);
                }
            } else {
                ((MessageSendHolder) holder).iv_status.setImageResource(R.drawable.ic_msg_sent);
            }

        } else if (holder instanceof MessageReceiveHolder) {
            ((MessageReceiveHolder) holder).tv_chat.setText(((TextMessageBody) arrayList.get(holder.getAbsoluteAdapterPosition()).getBody()).getMessage());

            setTimestamp(((MessageReceiveHolder) holder).tv_time, arrayList.get(holder.getAbsoluteAdapterPosition()), holder.getAbsoluteAdapterPosition());
        }
    }

//    @Override
//    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
//        int adapterPosition = holder.getAbsoluteAdapterPosition();
//        if(arrayList.get(adapterPosition).direct() == ChatMessage.Direct.SEND) {
//            Log.e("aaa","unread true");
//            arrayList.get(adapterPosition).setUnread(false);
//        }
//        super.onViewAttachedToWindow(holder);
//    }

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
        if (arrayList.get(position).direct() == ChatMessage.Direct.SEND) {
            return position;
        } else {
            return VIEW_MESSAGE_RECEIVED;
        }
    }
    protected void setTimestamp(TextView tv_time, ChatMessage message, int position) {
        if (position == 0) {
            tv_time.setText(ChatDateUtils.getTimestampString(context, new Date(message.getMsgTime())));
            tv_time.setVisibility(View.VISIBLE);
        } else {
//            show time stamp if interval with last message is > 30 seconds
            ChatMessage prevMessage = arrayList.get(position - 1);

            if (prevMessage != null && ChatDateUtils.isCloseEnough(message.getMsgTime(), prevMessage.getMsgTime())) {
                tv_time.setVisibility(View.GONE);
            } else {
                tv_time.setText(ChatDateUtils.getTimestampString(context, new Date(message.getMsgTime())));
                tv_time.setVisibility(View.VISIBLE);
            }
        }
    }
}