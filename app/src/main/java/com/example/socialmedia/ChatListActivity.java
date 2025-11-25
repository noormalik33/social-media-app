package com.example.socialmedia;

import static com.example.chat.ChatHelper.UPDATE_NICKNAME;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.adapters.AdapterChatList;
import com.example.chat.ChatHelper;
import com.example.interfaces.FunctionListener;
import com.example.items.ItemChatList;
import com.example.items.ItemUser;
import com.example.utils.Constants;
import com.example.utils.DBHelper;
import com.example.utils.Methods;
import com.example.utils.SharedPref;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import io.agora.CallBack;
import io.agora.MessageListener;
import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.Conversation;
import io.agora.chat.CursorResult;
import io.agora.chat.UserInfo;

public class ChatListActivity extends AppCompatActivity {

    Methods methods;
    SharedPref sharedPref;
    DBHelper dbHelper;
    ImageView iv_back;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView rv_chat;
    AdapterChatList adapterChatList;
    ArrayList<Conversation> arrayList = new ArrayList<>();
    ArrayList<ItemChatList> arrayListChatList = new ArrayList<>();
    CircularProgressBar progressBar;
    ConstraintLayout cl_empty;
    TextView tv_empty;
    int page = 1;
    String errorMessage = "";
    boolean isInitChatCalled = false, isFirstTimeListenerCalled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        sharedPref = new SharedPref(this);
        dbHelper = new DBHelper(this);

        methods = new Methods(this);
        methods.forceRTLIfSupported();

        rv_chat = findViewById(R.id.rv_chat);
        swipeRefreshLayout = findViewById(R.id.srl_chat_list);
        iv_back = findViewById(R.id.iv_chat_back);
        cl_empty = findViewById(R.id.cl_empty);
        tv_empty = findViewById(R.id.tv_empty);
        progressBar = findViewById(R.id.pb_chat);

        iv_back.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv_chat.setLayoutManager(llm);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            arrayList.clear();
            arrayListChatList.clear();
            initChatLogin(false);
        });

        initChatLogin(false);

//        try {
//            ChatClient.getInstance().contactManager().asyncGetBlackListFromServer(new ValueCallBack<List<String>>() {
//                @Override
//                public void onSuccess(List<String> value) {
//                    for (String username: value) {
//                        Log.e("aaa","block - " + username);
//                    }
//                }
//
//                @Override
//                public void onError(int error, String errorMsg) {
//                    Log.e("aaa","block list - " + errorMsg);
//                }
//            });
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);
    }

    private void initChatLogin(boolean isFromSignUp) {
        if (sharedPref.isChatRegistered()) {
            if (ChatClient.getInstance().isLoggedIn() || ChatClient.getInstance().isLoggedInBefore()) {

                if (isFromSignUp) {
                    UserInfo userInfo = new UserInfo();
                    userInfo.setNickname(sharedPref.getName());
                    ChatClient.getInstance().userInfoManager().updateOwnInfo(userInfo, new ValueCallBack<String>() {
                        @Override
                        public void onSuccess(String value) {
                        }

                        @Override
                        public void onError(int error, String errorMsg) {
                        }
                    });
                }

                getChatList();
            } else {
                ChatHelper.signIn(this, sharedPref.getUserId(), sharedPref.getEncryptedUserId(), new CallBack() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            UserInfo userInfo = new UserInfo();
                            userInfo.setNickname(sharedPref.getName());
                            userInfo.setAvatarUrl(sharedPref.getUserImage());
                            ChatClient.getInstance().userInfoManager().updateOwnInfo(userInfo, new ValueCallBack<String>() {
                                @Override
                                public void onSuccess(String value) {

                                }

                                @Override
                                public void onError(int error, String errorMsg) {

                                }
                            });

                            getChatList();
                        });
                    }

                    @Override
                    public void onError(int code, String error) {
                        runOnUiThread(() -> {
                            methods.showToast(error);
                        });
                    }
                });
            }
        } else {
            ChatHelper.signUp(this, sharedPref.getUserId(), sharedPref.getEncryptedUserId(), new CallBack() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        sharedPref.setIsChatRegistered(true);
                        initChatLogin(true);

                        ChatHelper.getTokenFromServer(getApplicationContext(), sharedPref.getUserId(), sharedPref.getEncryptedUserId(), UPDATE_NICKNAME, new CallBack() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(int code, String error) {

                            }
                        });
                    });
                }

                @Override
                public void onError(int code, String error) {
                    if (error.contains("duplicate_unique_property_exists")) {
                        runOnUiThread(() -> {
                            sharedPref.setIsChatRegistered(true);
                            initChatLogin(true);
                        });
                    } else {
                        runOnUiThread(() -> {
                            methods.showToast(error);
                        });
                    }
                }
            });
        }
    }

    private void getChatList() {
        isInitChatCalled = true;
        progressBar.setVisibility(View.VISIBLE);

        List<Conversation> aa = ChatClient.getInstance().chatManager().getAllConversationsBySort();
        if (!Constants.isChatConversationDeleted && !aa.isEmpty() && sharedPref.isChatLoadedFromServer()) {
            for (Conversation mapsConversation : aa) {
                arrayList.add(mapsConversation);
                ItemChatList itemChatList = dbHelper.getChat(mapsConversation.conversationId());
                if (itemChatList != null) {
                    arrayListChatList.add(itemChatList);
                } else {
                    arrayListChatList.add(new ItemChatList(mapsConversation.conversationId(), "", "null", false, false, false,false));
                }
            }

            setAdapter();
            setUpListNames();
        } else {
            ChatClient.getInstance().chatManager().asyncFetchConversationsFromServer(50, "", new ValueCallBack<CursorResult<Conversation>>() {
                @Override
                public void onSuccess(CursorResult<Conversation> value) {
                    Constants.isChatConversationDeleted = false;
                    errorMessage = getString(R.string.err_no_data_found);
                    if (value.getData() != null) {
                        sharedPref.setIsChatLoadedFromServer(true);
                        arrayList.addAll(value.getData());
                        for (int i = 0; i < arrayList.size(); i++) {
                            ItemChatList itemChatList = dbHelper.getChat(arrayList.get(i).conversationId());
                            if (itemChatList != null) {
                                arrayListChatList.add(itemChatList);
                            } else {
                                arrayListChatList.add(new ItemChatList(arrayList.get(i).conversationId(), "", "null", false, false, false, false));
                            }
                        }
                    }

                    setupListeners();

                    runOnUiThread(() -> {
                        setAdapter();
                        setUpListNames();
                    });
                }

                @Override
                public void onError(int error, String errorMsg) {
                    errorMessage = errorMsg;
                    runOnUiThread(() -> {
                        setAdapter();
                        setUpListNames();
                    });
                }
            });
        }

        if (!isFirstTimeListenerCalled) {
            setupListeners();
        }
    }

    private void setupListeners() {
        isFirstTimeListenerCalled = true;
//        ChatClient.getInstance().chatManager().addConversationListener(conversationListener);
        ChatClient.getInstance().chatManager().addMessageListener(messageListener);
    }

    private void setAdapter() {
        adapterChatList = new AdapterChatList(ChatListActivity.this, arrayList, arrayListChatList);
        rv_chat.setAdapter(adapterChatList);
        setEmpty();

        Date cDate = new Date();
        String fDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cDate);
        if (!fDate.equals(sharedPref.getUserCheckDate())) {
            sharedPref.setUserCheckDate(fDate);
            for (int i = 0; i < arrayList.size(); i++) {
                checkUserValid(arrayList.get(i).conversationId(), i);
            }
        }
    }

    private void setEmpty() {
        progressBar.setVisibility(View.GONE);
        if (!arrayList.isEmpty()) {
            rv_chat.setVisibility(View.VISIBLE);
            cl_empty.setVisibility(View.GONE);
        } else {
            rv_chat.setVisibility(View.GONE);
            tv_empty.setText(errorMessage);
            cl_empty.setVisibility(View.VISIBLE);
        }

        swipeRefreshLayout.setRefreshing(false);
    }

    private void setUpListNames() {
        for (int i = 0; i < arrayListChatList.size(); i++) {
            if (arrayListChatList.get(i).getName().isEmpty()) {
                getProfile(i);
                adapterChatList.notifyItemChanged(i);
            }
        }
    }

    private void getProfile(int pos) {
        methods.getPublicProfile(arrayList.get(pos).conversationId(), new FunctionListener() {
            @Override
            public void getUserDetails(String success, ItemUser itemUsers) {
                if (success.equals("1")) {
                    arrayListChatList.set(pos, new ItemChatList(itemUsers.getId(), itemUsers.getName(), itemUsers.getImage(), false, false, false, itemUsers.getIsAccountVerified()));
                    adapterChatList.notifyItemChanged(pos);
                    dbHelper.addChatList(new ItemChatList(itemUsers.getId(), itemUsers.getName(), itemUsers.getImage(), false, false, false, itemUsers.getIsAccountVerified()));
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 111) {
                if (data != null)
                    if (adapterChatList != null) {
                        if (!data.getBooleanExtra("isRemove", false)) {
                            for (int i = 0; i < arrayList.size(); i++) {
                                if (arrayList.get(i).conversationId().equals(data.getStringExtra("value"))) {
                                    Conversation conversation = ChatClient.getInstance().chatManager().getConversation(arrayList.get(i).conversationId());
                                    arrayList.set(i, conversation);
                                    adapterChatList.notifyItemChanged(i);
                                    break;
                                }
                            }
                        } else {
                            for (int i = 0; i < arrayList.size(); i++) {
                                if (arrayList.get(i).conversationId().equals(data.getStringExtra("value"))) {
                                    arrayList.get(i).clearAllMessages();
                                    arrayList.remove(i);
                                    arrayListChatList.remove(i);
                                    adapterChatList.notifyItemRemoved(i);
                                    setEmpty();
                                    break;
                                }
                            }
                        }
                    }
            }
        }
    }

    MessageListener messageListener = new MessageListener() {
        @Override
        public void onMessageReceived(List<ChatMessage> messages) {
            for (ChatMessage message : messages) {
                runOnUiThread(() -> {
                    message.setDelivered(true);
                    if (!arrayList.isEmpty()) {
                        for (int i = 0; i < arrayList.size(); i++) {
                            if (arrayList.get(i).conversationId().equals(message.getFrom())) {
                                arrayList.set(i, ChatClient.getInstance().chatManager().getConversation(arrayList.get(i).conversationId()));

                                Conversation tempConv = arrayList.get(i);
                                ItemChatList itemChatList = arrayListChatList.get(i);
                                arrayList.remove(i);
                                arrayListChatList.remove(i);
                                arrayList.add(0, tempConv);
                                arrayListChatList.add(0, itemChatList);

                                adapterChatList.notifyItemRangeChanged(0, i + 1);
                                break;
                            }
                            if (i == arrayList.size() - 1) {
                                arrayList.add(0, ChatClient.getInstance().chatManager().getConversation(message.getFrom()));

                                ItemChatList itemChatList = dbHelper.getChat(message.getFrom());
                                if (itemChatList != null) {
                                    arrayListChatList.add(0, itemChatList);
                                } else {
                                    arrayListChatList.add(0, new ItemChatList(message.getFrom(), "", "null", false, false, false, false));
                                }

                                adapterChatList.notifyItemInserted(0);
                            }
                        }
                    } else {
                        arrayList.add(ChatClient.getInstance().chatManager().getConversation(message.getFrom()));

                        ItemChatList itemChatList = dbHelper.getChat(message.getFrom());
                        if (itemChatList != null) {
                            arrayListChatList.add(itemChatList);
                        } else {
                            arrayListChatList.add(new ItemChatList(message.getFrom(), "", "null", false, false, false, false));
                        }

                        adapterChatList.notifyItemInserted(0);
                        setEmpty();
                    }
                });
            }
        }
    };

    private void checkUserValid(String userID, int pos) {
        methods.getPublicProfile(userID, new FunctionListener() {
            @Override
            public void getUserDetails(String success, ItemUser itemUsers) {
                if (success.equals("0")) {
                    dbHelper.setUserDeleted(arrayList.get(pos).conversationId(), true);
                    arrayListChatList.get(pos).setUserDeleted(true);

                    adapterChatList.notifyItemChanged(pos);
                } else {
                    dbHelper.setUserDeleted(arrayList.get(pos).conversationId(), false);
                    arrayListChatList.get(pos).setUserDeleted(false);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        ChatHelper.getInstance().pushActivity(ChatListActivity.this);
        if (ChatClient.getInstance() != null && ChatClient.getInstance().isConnected()) {
            setupListeners();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        ChatClient.getInstance().chatManager().removeMessageListener(messageListener);
//        ChatClient.getInstance().chatManager().removeConversationListener(conversationListener);
        super.onPause();
    }

    @Override
    protected void onStop() {
        ChatHelper.getInstance().popActivity(ChatListActivity.this);
        super.onStop();
    }
}