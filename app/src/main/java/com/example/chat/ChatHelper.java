package blogtalk.com.chat;

import static io.agora.cloud.HttpClientManager.Method_POST;
import static io.agora.cloud.HttpClientManager.Method_PUT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespGenerateChatToken;
import blogtalk.com.eventbus.EventCalling;
import blogtalk.com.eventbus.GlobalBus;
import blogtalk.com.socialmedia.MyApplication;
import blogtalk.com.socialmedia.R;
import blogtalk.com.socialmedia.VoiceCallActivity;
import blogtalk.com.utils.BackgroundTask;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;
import blogtalk.com.utils.VoiceCallRejectService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailabilityLight;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.CallBack;
import io.agora.ConnectionListener;
import io.agora.MessageListener;
import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.ChatOptions;
import io.agora.chat.UserInfo;
import io.agora.cloud.HttpClientManager;
import io.agora.cloud.HttpResponse;
import io.agora.push.PushConfig;
import io.agora.push.PushHelper;
import io.agora.push.PushListener;
import io.agora.push.PushType;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.MessageEvent;
import io.agora.rtm.PresenceEvent;
import io.agora.rtm.PublishOptions;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmClient;
import io.agora.rtm.RtmConfig;
import io.agora.rtm.RtmConstants;
import io.agora.rtm.RtmEventListener;
import io.agora.rtm.SubscribeOptions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatHelper {
    private static ChatHelper instance;
    private static final String AGORA_SERVER_URL = "a61.chat.agora.io";
    private static final String AGORA_APP_URL = "411426730/1627211";
    private static final String REGISTER_URL = "https://" + AGORA_SERVER_URL + "/" + AGORA_APP_URL + "/users";
    public static final String UPDATE_NICKNAME_URL = "https://" + AGORA_SERVER_URL + "/" + AGORA_APP_URL + "/users/";
    public static final String NEW_LOGIN = "NEW_LOGIN";
    public static final String RENEW_TOKEN = "RENEW_TOKEN";
    public static final String UPDATE_NICKNAME = "UPDATE_NICKNAME";
    private List<Activity> activityList = new ArrayList<Activity>();
    public MessageNotifier messageNotifier = new MessageNotifier();
    public static RtmClient rtmClient;

    private ChatHelper() {

    }

    public synchronized static ChatHelper getInstance() {
        if (instance == null) {
            instance = new ChatHelper();
        }
        return instance;
    }

    public void initSDK(Context context) {
        ChatOptions options = new ChatOptions();

        String sdkAppKey = context.getString(R.string.agora_chat_app_key);
        if (TextUtils.isEmpty(sdkAppKey)) {
            Toast.makeText(context, "You should set your Agora AppKey first!", Toast.LENGTH_SHORT).show();
            return;
        }

        options.setAppKey(sdkAppKey);

        options.setUsingHttpsOnly(false);
        options.setAutoLogin(true);
        options.setRequireAck(true);
        options.setRequireDeliveryAck(true);

        PushConfig.Builder builder = new PushConfig.Builder(context);
        builder.enableFCM(context.getString(R.string.firebase_key));
        options.setPushConfig(builder.build());

        ChatClient.getInstance().init(context, options);
        ChatClient.getInstance().setDebugMode(true);

        messageNotifier.init(context);

        doSignInSignUp(context, new SharedPref(context).getUserId(), new SharedPref(context).getEncryptedUserId());
        registerConnectionListener(context);
        registerMessageListener();

        PushHelper.getInstance().setPushListener(new PushListener() {
            @Override
            public void onError(PushType pushType, long errorCode) {
            }

            @Override
            public boolean isSupportPush(PushType pushType, PushConfig pushConfig) {
                // Sets whether FCM is enabled.
                if (pushType == PushType.FCM) {
                    return GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(context)
                            == ConnectionResult.SUCCESS;
                }
                return super.isSupportPush(pushType, pushConfig);
            }
        });

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    return;
                }
                // Gets a new FCM registration token.
                String token = task.getResult();
                ChatClient.getInstance().sendFCMTokenToServer(token);
            }
        });
    }

    public static void signIn(Context context, String username, String pwd, CallBack callBack) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pwd)) {
            return;
        }
        signOut(context, new CallBack() {
            @Override
            public void onSuccess() {
                getTokenFromServer(context, username, pwd, NEW_LOGIN, callBack);
            }

            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }

    public static void signInWithPassword(Context context, String username, String pwd, CallBack callBack) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pwd)) {
            return;
        }
        ChatClient.getInstance().login(username, pwd, new CallBack() {
            @Override
            public void onSuccess() {
                callBack.onSuccess();
            }

            @Override
            public void onError(int code, String error) {
                if (error.contains("The user is already logged in")) {
                    callBack.onSuccess();
                } else {
                    callBack.onError(code, error);
                }
            }

            @Override
            public void onProgress(int progress, String status) {

            }
        });
    }

    public static void getTokenFromServer(Context context, String username, String pwd, String requestType, CallBack callBack) {
        if (new Methods(context).isNetworkAvailable()) {
            Call<RespGenerateChatToken> call = APIClient.getClient().create(APIInterface.class).getGenerateChatToken(new Methods(context).getAPIRequest(Constants.URL_GENERATE_CHAT_TOKEN, "", "", "", "", "", "", "", "", "", "", new SharedPref(context).getUserId(), ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespGenerateChatToken> call, @NonNull Response<RespGenerateChatToken> response) {
                    if (response.body() != null && response.body().getItemChatToken() != null) {
                        if (response.body().getItemChatToken().getUserToken() != null) {
                            String token = response.body().getItemChatToken().getUserToken();
                            if (TextUtils.equals(requestType, NEW_LOGIN)) {
                                ChatClient.getInstance().loginWithAgoraToken(username, token, new CallBack() {
                                    @Override
                                    public void onSuccess() {
                                        callBack.onSuccess();
                                    }

                                    @Override
                                    public void onError(int code, String error) {
                                        callBack.onError(code, error);
                                    }

                                    @Override
                                    public void onProgress(int progress, String status) {

                                    }
                                });
                            } else if (TextUtils.equals(requestType, RENEW_TOKEN)) {
                                ChatClient.getInstance().renewToken(token);
                            } else if (TextUtils.equals(requestType, UPDATE_NICKNAME)) {
                                getUpdateNickName(context, response.body().getItemChatToken().getAuthToken());
                            }
                        }
                    } else {
                        callBack.onError(0, context.getString(R.string.err_server_error));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespGenerateChatToken> call, @NonNull Throwable t) {
                    call.cancel();
                    callBack.onError(0, context.getString(R.string.err_server_error));
                }
            });
        } else {
            callBack.onError(0, context.getString(R.string.err_internet_not_connected));
        }
    }

    public static void getAuthTokenFromServer(Context context, String username, String pwd, CallBack callBack) {
        if (new Methods(context).isNetworkAvailable()) {
            Call<RespGenerateChatToken> call = APIClient.getClient().create(APIInterface.class).getGenerateChatToken(new Methods(context).getAPIRequest(Constants.URL_GENERATE_CHAT_TOKEN, "", "", "", "", "", "", "", "", "", "", new SharedPref(context).getUserId(), ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespGenerateChatToken> call, @NonNull Response<RespGenerateChatToken> response) {
                    if (response.body() != null && response.body().getItemChatToken() != null) {
                        if (response.body().getItemChatToken().getUserToken() != null) {
                            String token = response.body().getItemChatToken().getAuthToken();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Map<String, String> headers = new HashMap<>();
                                        headers.put("Content-Type", "application/json");
                                        String authValue = "Bearer " + token;
                                        headers.put("Authorization", authValue);

                                        JSONObject request = new JSONObject();
                                        request.putOpt("username", username);
                                        request.putOpt("password", pwd);

                                        HttpResponse response = HttpClientManager.httpExecute(REGISTER_URL, headers, request.toString(), Method_POST);
                                        int code = response.code;
                                        String responseInfo = response.content;
                                        if (code == 200) {
                                            if (responseInfo != null && !responseInfo.isEmpty()) {
                                                JSONObject object = new JSONObject(responseInfo);

                                                if (object.has("entities")) {
                                                    JSONArray jsonArray = object.getJSONArray("entities");
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        JSONObject jobjuser = jsonArray.getJSONObject(i);
                                                        if (jobjuser.has("username")) {
                                                            String uname = jobjuser.getString("username");
                                                            if (uname.equals(username)) {
                                                                callBack.onSuccess();
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    if (object.has("error")) {
                                                        String error = object.getString("error");
                                                        callBack.onError(code, error);
                                                    } else {
                                                        callBack.onError(code, context.getString(R.string.err_server_error));
                                                    }
                                                }
                                            } else {
                                                callBack.onError(code, responseInfo);
                                            }
                                        } else {
                                            callBack.onError(code, responseInfo);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        callBack.onError(0, e.getMessage());
                                    }
                                }
                            }).start();
                        }
                    } else {
                        callBack.onError(0, context.getString(R.string.err_server_error));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespGenerateChatToken> call, @NonNull Throwable t) {
                    call.cancel();
                    callBack.onError(0, context.getString(R.string.err_server_error));
                }
            });
        } else {
            callBack.onError(0, context.getString(R.string.err_internet_not_connected));
        }
    }

    public static void signUp(Context context, String username, String pwd, CallBack callBack) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pwd)) {
            callBack.onError(0, "Empty");

            return;
        }

        getAuthTokenFromServer(context, username, pwd, callBack);
    }

    /**
     * Sign out
     *
     * @param context
     * @param callBack
     */
    public static void signOut(Context context, CallBack callBack) {
        if (ChatClient.getInstance().isLoggedInBefore()) {
            ChatClient.getInstance().logout(true, new CallBack() {
                @Override
                public void onSuccess() {
                    if (callBack != null) {
                        callBack.onSuccess();
                    }
                }

                @Override
                public void onError(int code, String error) {
                    if (callBack != null) {
                        callBack.onError(code, error);
                    }
                }

                @Override
                public void onProgress(int progress, String status) {

                }
            });
        } else {
            if (callBack != null) {
                callBack.onSuccess();
            }
        }
    }

    private void doSignInSignUp(Context context, String userID, String password) {
        if (new SharedPref(context).isLogged()) {
            if (new SharedPref(context).isChatRegistered()) {
                if (!ChatClient.getInstance().isLoggedIn() && !ChatClient.getInstance().isLoggedInBefore()) {
                    signIn(context, userID, password, new CallBack() {
                        @Override
                        public void onSuccess() {
                            getRTMTokenFromServer(context, userID, NEW_LOGIN);
                        }

                        @Override
                        public void onError(int code, String error) {

                        }
                    });
                } else {
                    getRTMTokenFromServer(context, userID, NEW_LOGIN);
                }
            } else {
                signUp(context, new SharedPref(context).getUserId(), password, new CallBack() {
                    @Override
                    public void onSuccess() {
                        signIn(context, userID, password, new CallBack() {
                            @Override
                            public void onSuccess() {
                                UserInfo userInfo = new UserInfo();
                                userInfo.setNickname(new SharedPref(context).getName());
                                userInfo.setAvatarUrl(new SharedPref(context).getUserImage());
                                ChatClient.getInstance().userInfoManager().updateOwnInfo(userInfo, new ValueCallBack<String>() {
                                    @Override
                                    public void onSuccess(String value) {

                                    }

                                    @Override
                                    public void onError(int error, String errorMsg) {

                                    }
                                });

                                getTokenFromServer(context, new SharedPref(context).getUserId(), password, UPDATE_NICKNAME, new CallBack() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError(int code, String error) {

                                    }
                                });
                                getRTMTokenFromServer(context, userID, NEW_LOGIN);
                            }

                            @Override
                            public void onError(int code, String error) {

                            }
                        });
                    }

                    @Override
                    public void onError(int code, String error) {
                        if (error.contains("duplicate_unique_property_exists")) {
                            new SharedPref(context).setIsChatRegistered(true);
                            signIn(context, new SharedPref(context).getUserId(), password, new CallBack() {
                                @Override
                                public void onSuccess() {
                                    UserInfo userInfo = new UserInfo();
                                    userInfo.setNickname(new SharedPref(context).getName());
                                    userInfo.setAvatarUrl(new SharedPref(context).getUserImage());
                                    ChatClient.getInstance().userInfoManager().updateOwnInfo(userInfo, new ValueCallBack<String>() {
                                        @Override
                                        public void onSuccess(String value) {

                                        }

                                        @Override
                                        public void onError(int error, String errorMsg) {

                                        }
                                    });

                                    getTokenFromServer(context, new SharedPref(context).getUserId(), password, UPDATE_NICKNAME, new CallBack() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError(int code, String error) {

                                        }
                                    });
                                }

                                @Override
                                public void onError(int code, String error) {

                                }
                            });
                        }
                    }
                });
            }
        }
    }

    public void getRTMTokenFromServer(Context context, String userID, String requestType) {
        if (new Methods(context).isNetworkAvailable()) {
            Call<RespGenerateChatToken> call = APIClient.getClient().create(APIInterface.class).getGenerateRtmToken(new Methods(context).getAPIRequest(Constants.URL_GENERATE_RTM_TOKEN, "", "", "", "", "", "", "", "", "", "", userID, ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespGenerateChatToken> call, @NonNull Response<RespGenerateChatToken> response) {
                    if (response.body() != null && response.body().getItemChatToken() != null) {
                        if (response.body().getItemChatToken().getUserToken() != null) {

                            String token = response.body().getItemChatToken().getUserToken();
                            if (TextUtils.equals(requestType, NEW_LOGIN)) {
                                configureRTM(context, userID, token);
                            } else if (TextUtils.equals(requestType, RENEW_TOKEN)) {
                                if(rtmClient != null) {
                                    rtmClient.renewToken(token, new ResultCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void responseInfo) {}
                                        @Override
                                        public void onFailure(ErrorInfo errorInfo) {}
                                    });
                                }
                            }
                        }
                    }
//                    else {
//                        callBack.onError(0, context.getString(R.string.err_server_error));
//                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespGenerateChatToken> call, @NonNull Throwable t) {
                    call.cancel();
//                    callBack.onError(0, context.getString(R.string.err_server_error));
                }
            });
        }
//        else {
//            callBack.onError(0, context.getString(R.string.err_internet_not_connected));
//        }
    }

    private void configureRTM(Context context, String userID, String token) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                RtmEventListener eventListener = new RtmEventListener() {
                    @Override
                    public void onMessageEvent(MessageEvent event) {
                        if(event.getCustomType().equalsIgnoreCase(Constants.CALL_CALLING)) {
                            try {
                                showCallNotification(context, event.getPublisherId(), event.getMessage().getData().toString());

                                PublishOptions options = new PublishOptions();
                                options.setCustomType(Constants.CALL_RINGING);
                                rtmClient.publish(event.getPublisherId(), "sdd", options, new ResultCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void responseInfo) {}
                                    @Override
                                    public void onFailure(ErrorInfo errorInfo) {}
                                });

                            } catch (Exception ignored) {}
                        } else if(event.getCustomType().equalsIgnoreCase(Constants.CALL_RINGING)) {
                            GlobalBus.getBus().postSticky(new EventCalling(Constants.CALL_RINGING));
                        } else if(event.getCustomType().equalsIgnoreCase(Constants.CALL_REJECTED)) {
                            if(MyApplication.isVoiceCallActivityRunning()) {
                                GlobalBus.getBus().postSticky(new EventCalling(Constants.CALL_REJECTED));
                            }
                            NotificationManagerCompat.from(context).cancel(Integer.parseInt(event.getPublisherId().concat(userID)));
                        }
                    }

                    @Override
                    public void onPresenceEvent(PresenceEvent event) {
//                        String text = "receive presence event, user: " + event.getPublisherId() + " event: " + event.getEventType() + "\n";
                    }

                    @Override
                    public void onConnectionStateChanged(String mChannelName, RtmConstants.RtmConnectionState state, RtmConstants.RtmConnectionChangeReason reason) {
//                        String text = "Connection state changed to " + state + ", Reason: " + reason + "\n";
                    }

                    @Override
                    public void onTokenPrivilegeWillExpire(String channelName) {
                        RtmEventListener.super.onTokenPrivilegeWillExpire(channelName);

                        getRTMTokenFromServer(context, userID, RENEW_TOKEN);
                    }
                };

                try {
                    RtmConfig config = new RtmConfig.Builder(Constants.AGORA_APP_ID, userID)
                            .eventListener(eventListener)
                            .build();
                    rtmClient = RtmClient.create(config);
                    rtmClient.login(token, new ResultCallback<Void>() {
                        @Override
                        public void onSuccess(Void responseInfo) {

//                            Log.e("aaa", "RTM Successfully logged in to Signaling");

                            SubscribeOptions options = new SubscribeOptions();
                            options.setWithMessage(true);
                            options.setWithPresence(true);
                            options.setWithMetadata(true);
                            options.setWithLock(true);

                            rtmClient.subscribe(userID, options, new ResultCallback<Void>() {
                                @Override
                                public void onSuccess(Void responseInfo) {
//                                    Log.e("aaa", "RTM subscribe channel " + userID + " success");
                                }

                                @Override
                                public void onFailure(ErrorInfo errorInfo) {
//                                    Log.e("aaa", "RTM subscribe channel " + userID + " failed");
                                }
                            });
                        }

                        @Override
                        public void onFailure(ErrorInfo errorInfo) {
//                            CharSequence text = "User: " + userID + " RTM Failed to log in to Signaling!" + errorInfo.toString();
                        }
                    });

                } catch (Exception e) {
//                    Log.e("RTM", "Error initializing RTM: " + e.getMessage());
                }
            }
        },1000);
    }

    @SuppressLint("MissingPermission")
    private void showCallNotification(Context context, String callerId, String callerDetails) {

        String callerName = callerDetails.split("-//-")[0];
        String callerImage = callerDetails.split("-//-")[1];

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        String CHANNEL_ID = "VoiceCall";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence notificationChannelName = "CALL_CHANNEL";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, notificationChannelName, importance);
            manager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .setContentTitle("Incoming Call")
                .setContentText("Call from " + callerName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_user, context.getString(R.string.accept), createAcceptIntent(context, callerId, callerName, callerImage))
                .addAction(R.drawable.ic_chat, context.getString(R.string.decline), createDeclineIntent(context, callerId))
                .setContentIntent(createCallIntent(context, callerId, callerName, callerImage));

        manager.notify(Integer.parseInt(callerId.concat(new SharedPref(context).getUserId())), builder.build());
    }

    private PendingIntent createCallIntent(Context context, String callerId, String callerName, String callerImage) {

        int requestCode = (int) System.currentTimeMillis();

        Intent intent = new Intent(context, VoiceCallActivity.class);
        intent.putExtra("is_call", false);
        intent.putExtra("is_received", true);
        intent.putExtra("call_from_id", callerId);
        intent.putExtra("call_from_name", callerName);
        intent.putExtra("call_from_image", callerImage);

        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private PendingIntent createAcceptIntent(Context context, String callerId, String callerName, String callerImage) {

        int requestCode = (int) System.currentTimeMillis();

        Intent intent = new Intent(context, VoiceCallActivity.class);
        intent.putExtra("is_call", false);
        intent.putExtra("is_received", true);
        intent.putExtra("call_from_id", callerId);
        intent.putExtra("call_from_name", callerName);
        intent.putExtra("is_accept", true);
        intent.putExtra("call_from_image", callerImage);

        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private PendingIntent createDeclineIntent(Context context, String callerId) {

        Intent action1Intent = new Intent(context, VoiceCallRejectService.class);
        action1Intent.setAction(Constants.CALL_REJECTED);
        action1Intent.putExtra("call_id",callerId);

        return PendingIntent.getService(context, 0, action1Intent, PendingIntent.FLAG_IMMUTABLE);
    }

    private void registerConnectionListener(Context context) {
        ChatClient.getInstance().addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onDisconnected(int errorCode) {

            }

            @Override
            public void onTokenExpired() {
                ConnectionListener.super.onTokenExpired();

                if (new SharedPref(context).isLogged() && (ChatClient.getInstance().isLoggedIn() || ChatClient.getInstance().isLoggedInBefore())) {
                    getTokenFromServer(context, new SharedPref(context).getUserId(), new SharedPref(context).getEncryptedUserId(), RENEW_TOKEN, new CallBack() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(int code, String error) {

                        }
                    });
                }
            }

            @Override
            public void onTokenWillExpire() {
                ConnectionListener.super.onTokenWillExpire();

                if (new SharedPref(context).isLogged() && (ChatClient.getInstance().isLoggedIn() || ChatClient.getInstance().isLoggedInBefore())) {
                    getTokenFromServer(context, new SharedPref(context).getUserId(), new SharedPref(context).getEncryptedUserId(), RENEW_TOKEN, new CallBack() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(int code, String error) {

                        }
                    });
                }
            }
        });
    }

    private void registerMessageListener() {
        MessageListener messageListener = new MessageListener() {

            @Override
            public void onMessageReceived(List<ChatMessage> messages) {
                for (ChatMessage message : messages) {

                    // in background, do not refresh UI, notify it in notification bar
//                    if (!hasForegroundActivities()) {
//                        messageNotifier.onNewMsg(message);
//                    } else {
//                        Log.e("aaa", "message received in foreground");
//                    }
                }
            }
        };

        ChatClient.getInstance().chatManager().addMessageListener(messageListener);
    }

    public boolean hasForegroundActivities() {
        return !activityList.isEmpty();
    }

    public void pushActivity(Activity activity) {
        if (!activityList.contains(activity)) {
            activityList.add(0, activity);
        }
    }

    public void popActivity(Activity activity) {
        activityList.remove(activity);
    }

    public static void getUpdateNickName(Context context, String token) {
        if (new Methods(context).isNetworkAvailable()) {
            new BackgroundTask() {
                @Override
                public void onPreExecute() {
                }

                @Override
                public boolean doInBackground() {
                    try {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        String authValue = "Bearer " + token;
                        headers.put("Authorization", authValue);

                        JSONObject request = new JSONObject();
                        request.put("nickname", new SharedPref(context).getName());

                        HttpClientManager.httpExecute(UPDATE_NICKNAME_URL + new SharedPref(context).getUserId(), headers, request.toString(), Method_PUT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }

                @Override
                public void onPostExecute(Boolean isExecutionSuccess) {
                }
            }.execute();
        }
    }


}
