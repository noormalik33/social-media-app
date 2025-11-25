package blogtalk.com.socialmedia; // Updated Package Name

import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Updated Imports to blogtalk.com
import blogtalk.com.R;
import blogtalk.com.adapters.AdapterChat;
import blogtalk.com.chat.ChatHelper;
import blogtalk.com.items.ItemChatList;
import blogtalk.com.items.ItemUser;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.DBHelper;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import io.agora.CallBack;
import io.agora.ConversationListener;
import io.agora.MessageListener;
import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.Conversation;
import io.agora.chat.CursorResult;
import io.agora.exceptions.ChatException;

public class ChatActivity extends AppCompatActivity {

    TextView tv_toolbar, tv_empty;
    Methods methods;
    RecyclerView rv_chat;
    LinearLayoutManager llm;
    AdapterChat adapterChat;
    ArrayList<ChatMessage> arrayList = new ArrayList<>();
    TextInputEditText et_chat_send;
    ProgressBar pb_loadMore;
    CircularProgressBar pb_chat;
    String chatUserId = "", name = "", errorMessage = "";
    ImageView iv_chat_more, iv_chat_call, iv_account_verify;
    boolean isLoadMore = false, isOver = false, isMessageSynced = false;
    ItemChatList itemChatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatUserId = getIntent().getStringExtra("id");
        name = getIntent().getStringExtra("name");
        itemChatList = (ItemChatList) getIntent().getSerializableExtra("item");

        isMessageSynced = new DBHelper(this).isMessageSynced(chatUserId);

        methods = new Methods(this);

        tv_toolbar = findViewById(R.id.tv_chat_toolbar);
        et_chat_send = findViewById(R.id.et_chat_send);
        rv_chat = findViewById(R.id.rv_chat);
        pb_loadMore = findViewById(R.id.pb_chat_more);
        pb_chat = findViewById(R.id.pb_chat);
        iv_chat_more = findViewById(R.id.iv_chat_more);
        iv_chat_call = findViewById(R.id.iv_chat_call);
        tv_empty = findViewById(R.id.tv_empty);
        iv_account_verify = findViewById(R.id.iv_chat_account_verify);

        ImageView iv_back = findViewById(R.id.iv_chat_back);
        iv_back.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        tv_toolbar.setText(name);

        iv_account_verify.setVisibility(new SharedPref(ChatActivity.this).getIsAccountVerifyOn() && itemChatList.getIsUserAccountVerified() ? View.VISIBLE : View.GONE);

        llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        rv_chat.setLayoutManager(llm);

        rv_chat.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isOver) {
                    int firstVisibleItemPosition = llm.findFirstCompletelyVisibleItemPosition();
                    if (dy < 0 && firstVisibleItemPosition == 0 && !isLoadMore) {
                        pb_loadMore.setVisibility(View.VISIBLE);
                        getPreviousChat();
                    }
                }
            }
        });

        setupListeners();
        getPreviousChat();

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                try {
                    ChatClient.getInstance().chatManager().getConversation(chatUserId).markAllMessagesAsRead();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent();
                intent.putExtra("value", chatUserId);
                intent.putExtra("isRemove", false);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        TextInputLayout textInputLayout = findViewById(R.id.til_chat);
        et_chat_send.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    textInputLayout.setEndIconDrawable(R.drawable.ic_send);
                    textInputLayout.getEndIconDrawable().setColorFilter(ContextCompat.getColor(ChatActivity.this, R.color.primary), android.graphics.PorterDuff.Mode.SRC_IN);

                    textInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!Objects.requireNonNull(et_chat_send.getText()).toString().isEmpty()) {
                                sendMessage();
                            }
                        }
                    });
                } else {
                    textInputLayout.setEndIconDrawable(null);

                    textInputLayout.setEndIconOnClickListener(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        iv_chat_more.setOnClickListener(view -> openMoreBottomSheet());
        iv_chat_call.setOnClickListener(view -> {
            if(checkPermissions()) {
                Intent intent = new Intent(ChatActivity.this, VoiceCallActivity.class);
                intent.putExtra("is_call", true);
                intent.putExtra("is_received", false);
                intent.putExtra("item", itemChatList);
                startActivity(intent);
            } else {
                ActivityCompat.requestPermissions(this, getRequiredPermissions(), 22);
            }
        });

        iv_chat_call.setVisibility(new SharedPref(ChatActivity.this).getIsVoiceChatOn() ? View.VISIBLE : View.GONE);
    }

    private void setupListeners() {
        // Add message event callbacks
        ChatClient.getInstance().chatManager().addMessageListener(messageListener);
        ChatClient.getInstance().chatManager().addConversationListener(conversationListener);
    }

    public void sendMessage() {
        String toSendName = chatUserId;
        String content = et_chat_send.getText().toString().trim();

        ChatMessage message = ChatMessage.createTextSendMessage(content, toSendName);

        JSONObject pushObject = new JSONObject();
        JSONArray titleArgs = new JSONArray();
        JSONArray contentArgs = new JSONArray();
        try {
            // Sets the template name.
            pushObject.put("name", "Chat");
            // Sets the template title by specifying the variable.
            titleArgs.put("value1");
            //...
            pushObject.put("title_args", titleArgs);
            // Sets the template content by specifying the variable.
            contentArgs.put("value1");
            //...
            pushObject.put("content_args", contentArgs);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        message.setAttribute("em_push_template", pushObject);

        message.setMessageStatusCallback(new CallBack() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    message.setUnread(true);
                    arrayList.add(message);
                    if (adapterChat != null) {
                        adapterChat.notifyItemInserted(arrayList.size() - 1);
                        llm.scrollToPosition(arrayList.size() - 1);
                    } else {
                        setAdapter(arrayList.size());
                    }
                    et_chat_send.setText("");
                });
            }

            @Override
            public void onError(int code, String error) {
                runOnUiThread(() -> {
                    if (code != 210) {
                        methods.showToast(error);
                    } else {
                        methods.showToast(getString(R.string.err_you_have_blocked));
                    }
                });
            }
        });
        ChatClient.getInstance().chatManager().sendMessage(message);
    }

    private void getPreviousChat() {
        try {
            isLoadMore = true;
            ArrayList<ChatMessage> tempArrayList = (ArrayList<ChatMessage>) ChatClient.getInstance().chatManager().getConversation(chatUserId).loadMoreMsgFromDB(arrayList.isEmpty() ? "" : arrayList.get(0).getMsgId(), 100);
            if (!isMessageSynced || (tempArrayList.isEmpty() && arrayList.size() != ChatClient.getInstance().chatManager().getConversation(chatUserId).getAllMsgCount())) {
                ChatClient.getInstance().chatManager().asyncFetchHistoryMessage(chatUserId, Conversation.ConversationType.Chat, 50, arrayList.isEmpty() ? "" : arrayList.get(0).getMsgId(), new ValueCallBack<CursorResult<ChatMessage>>() {
                    @Override
                    public void onSuccess(CursorResult<ChatMessage> value) {
                        errorMessage = getString(R.string.err_no_data_found);
                        if (value.getData() != null && !value.getData().isEmpty()) {
                            arrayList.addAll(0, value.getData());
                            runOnUiThread(() -> setAdapter(value.getData().size()));
                        } else {
                            isOver = true;
                            runOnUiThread(() -> setEmpty());
                        }
                        new DBHelper(ChatActivity.this).setMessageSynced(chatUserId, true);
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        errorMessage = errorMsg;
                        runOnUiThread(() -> setEmpty());
                    }
                });
            } else {
                if (!tempArrayList.isEmpty()) {
                    arrayList.addAll(0, tempArrayList);
                    setAdapter(tempArrayList.size());
                } else {
                    isMessageSynced = false;
                    setEmpty();
                    getPreviousChat();
                }
            }
        } catch (Exception e) {
            isMessageSynced = false;
            setEmpty();
        }
    }

    private void setAdapter(int totalNewItems) {
        if (adapterChat == null) {
            adapterChat = new AdapterChat(ChatActivity.this, arrayList);
            rv_chat.setAdapter(adapterChat);
            rv_chat.setVisibility(View.VISIBLE);
        } else {
            adapterChat.notifyItemRangeInserted(0, totalNewItems);
            llm.scrollToPosition(totalNewItems - 1);
        }

        setEmpty();
    }

    private void setEmpty() {
        pb_chat.setVisibility(View.GONE);
        if (!arrayList.isEmpty()) {
            rv_chat.setVisibility(View.VISIBLE);
            tv_empty.setVisibility(View.GONE);
        } else {
            tv_empty.setText(errorMessage);
            tv_empty.setVisibility(View.VISIBLE);
            rv_chat.setVisibility(View.GONE);
        }
        pb_loadMore.setVisibility(View.GONE);
        isLoadMore = false;
    }

    MessageListener messageListener = new MessageListener() {
        @Override
        public void onMessageReceived(List<ChatMessage> messages) {
            for (ChatMessage message :  messages) {
                runOnUiThread(() -> {
//                    message.setUnread(false);
                    try {
                        ChatClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
                    } catch (ChatException e) {
                        throw new RuntimeException(e);
                    }
                    arrayList.add(message);
                    adapterChat.notifyItemInserted(arrayList.size() - 1);
                    llm.scrollToPosition(arrayList.size() - 1);
                });
            }
        }

        @Override
        public void onMessageDelivered(List<ChatMessage> messages) {
            runOnUiThread(() -> {
                for (ChatMessage message : messages) {
//                    message.setDelivered(true);
                    try {
                        int pos = arrayList.indexOf(message);
//                        ChatClient.getInstance().chatManager().getMessage(message.getMsgId()).setDelivered(true);
                        arrayList.get(pos).setDelivered(true);
                        adapterChat.notifyItemChanged(pos);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            MessageListener.super.onMessageDelivered(messages);
        }

        @Override
        public void onMessageRead(List<ChatMessage> messages) {
            runOnUiThread(() -> {
                for (ChatMessage message : messages) {
                    message.setUnread(false);
                    try {
                        int pos = arrayList.indexOf(message);
//                        ChatClient.getInstance().chatManager().getMessage(message.getMsgId()).setDelivered(true);
                        arrayList.get(pos).setUnread(false);
                        adapterChat.notifyItemChanged(pos);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            MessageListener.super.onMessageRead(messages);
        }
    };

    ConversationListener conversationListener = new ConversationListener() {
        @Override
        public void onConversationUpdate() {

        }

        @Override
        public void onConversationRead(String from, String to) {
            if (from.equals(chatUserId)) {
                runOnUiThread(() -> adapterChat.notifyDataSetChanged());
            }
        }
    };

    private void openMoreBottomSheet() {
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.layout_bottom_chat, null);

        BottomSheetDialog dialog = new BottomSheetDialog(ChatActivity.this, R.style.BottomSheetDialogStyle);
        dialog.setContentView(view);
        dialog.show();

        boolean isBlocked = new DBHelper(ChatActivity.this).isChatBlocked(chatUserId);

        TextView tv_prof = dialog.findViewById(R.id.tv_chat_view_prof);
        TextView tv_clear = dialog.findViewById(R.id.tv_chat_clear);
        TextView tv_delete = dialog.findViewById(R.id.tv_chat_delete);
        TextView tv_block = dialog.findViewById(R.id.tv_chat_block);

        if (isBlocked) {
            tv_block.setText(getString(R.string.unblock));
        }

        assert tv_prof != null;
        tv_prof.setOnClickListener(view12 -> {
            dialog.dismiss();
            Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
            intent.putExtra("item_user", new ItemUser(chatUserId, name, "", "", "", "", "No"));
            startActivity(intent);
        });

        assert tv_clear != null;
        tv_clear.setOnClickListener(view1 -> {
            dialog.dismiss();
            ChatClient.getInstance().chatManager().getConversation(chatUserId).removeMessagesFromServer(System.currentTimeMillis(), new CallBack() {
                @Override
                public void onSuccess() {
                    ChatClient.getInstance().chatManager().getConversation(chatUserId).clearAllMessages();
                }

                @Override
                public void onError(int code, String error) {

                }
            });

            int totalItems = arrayList.size();
            arrayList.clear();
            adapterChat.notifyItemRangeRemoved(0, totalItems);

            setEmpty();
        });

        assert tv_delete != null;
        tv_delete.setOnClickListener(view1 -> {
            dialog.dismiss();
            openDeleteAlertBottomSheet();
        });

        assert tv_block != null;
        tv_block.setOnClickListener(view1 -> {
            dialog.dismiss();
            try {
                if (!isBlocked) {
                    ChatClient.getInstance().contactManager().asyncAddUserToBlackList(chatUserId, false, new CallBack() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                new DBHelper(ChatActivity.this).setChatBlocked(chatUserId, true);
                                methods.showToast(getString(R.string.user_blocked));
                            });
                        }

                        @Override
                        public void onError(int code, String error) {
                            runOnUiThread(() -> methods.showToast(getString(R.string.err_server_error)));
                        }
                    });
                } else {
                    ChatClient.getInstance().contactManager().asyncRemoveUserFromBlackList(chatUserId, new CallBack() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                new DBHelper(ChatActivity.this).setChatBlocked(chatUserId, false);
                                methods.showToast(getString(R.string.user_unblocked));
                            });
                        }

                        @Override
                        public void onError(int code, String error) {
                            runOnUiThread(() -> methods.showToast(getString(R.string.err_server_error)));
                        }
                    });
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void openDeleteAlertBottomSheet() {
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.layout_bottom_delete_ac, null);

        BottomSheetDialog dialog = new BottomSheetDialog(ChatActivity.this, R.style.BottomSheetDialogStyle);
        dialog.setContentView(view);
        dialog.show();

        MaterialButton btn_delete = dialog.findViewById(R.id.btn_del_ac_delete);
        MaterialButton btn_cancel = dialog.findViewById(R.id.btn_del_ac_cancel);
        TextView tv1 = dialog.findViewById(R.id.tv1);
        TextView tv2 = dialog.findViewById(R.id.tv2);

        tv1.setText(getString(R.string.delete_conversation));
        tv2.setText(getString(R.string.sure_delete_conversation));

        assert btn_delete != null;
        btn_delete.setOnClickListener(view1 -> {
            dialog.dismiss();
            ChatClient.getInstance().chatManager().deleteConversationFromServer(chatUserId, Conversation.ConversationType.Chat, true, new CallBack() {
                @Override
                public void onSuccess() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ChatClient.getInstance().chatManager().deleteConversation(chatUserId, true);
                            Constants.isChatConversationDeleted = true;
                            int totalItems = arrayList.size();
                            arrayList.clear();
                            adapterChat.notifyItemRangeRemoved(0, totalItems);

                            Intent intent = new Intent();
                            intent.putExtra("value", chatUserId);
                            intent.putExtra("isRemove", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    });
                }

                @Override
                public void onError(int code, String error) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            methods.showToast("Error deleting");
                        }
                    });
                }
            });
        });

        assert btn_cancel != null;
        btn_cancel.setOnClickListener(view1 -> {
            dialog.dismiss();
        });
    }

    private boolean checkPermissions() {
        for (String permission : getRequiredPermissions()) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private String[] getRequiredPermissions() {
        // Determine the permissions required when targetSDKVersion is 31 or above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return new String[]{
                    RECORD_AUDIO, // Record audio permission
                    READ_PHONE_STATE, // Read phone state permission
                    BLUETOOTH_CONNECT // Bluetooth connection permission
            };
        } else {
            return new String[]{
                    RECORD_AUDIO,
            };
        }
    }

    @Override
    protected void onDestroy() {
        ChatClient.getInstance().chatManager().removeMessageListener(messageListener);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        ChatHelper.getInstance().pushActivity(ChatActivity.this);
        super.onResume();
    }

    @Override
    protected void onStop() {
        ChatHelper.getInstance().popActivity(ChatActivity.this);
        super.onStop();
    }
}