package com.example.socialmedia;

import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.apiservices.APIClient;
import com.example.apiservices.APIInterface;
import com.example.apiservices.RespGenerateChatToken;
import com.example.chat.ChatHelper;
import com.example.eventbus.EventCalling;
import com.example.eventbus.GlobalBus;
import com.example.items.ItemChatList;
import com.example.utils.Constants;
import com.example.utils.Methods;
import com.example.utils.SharedPref;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.agora.onetoone.CallStateReason;
import io.agora.onetoone.CallStateType;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.RtcEngineEx;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.PublishOptions;
import io.agora.rtm.ResultCallback;
import retrofit2.Call;
import retrofit2.Response;

public class VoiceCallActivity extends AppCompatActivity {

    Methods methods;
    SharedPref sharedPref;
    private RtcEngineEx mRtcEngine;
    TextView tvName, tvStatus;
    RoundedImageView ivUser;
    FloatingActionButton fabCallEnd, fabCallStart, fabSpeaker, fabMute;
    ItemChatList itemChatList;
    CallStateType mCallState = CallStateType.Idle;
    private static final int PERMISSION_REQ_ID = 22;
    boolean isCall = false, isReceive = false, isUserJoinedCall = false, isCallAcceptFromNotification = false, isMuted = false;
    int callFromUserID = 0;
    String callFromUserName = "", callFromUserImage = "", rtcToken = "";
    Chronometer chronometer;
    public static final String NEW_LOGIN = "NEW_LOGIN";
    public static final String RENEW_TOKEN = "RENEW_TOKEN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);

        methods = new Methods(this);
        sharedPref = new SharedPref(this);

        itemChatList = (ItemChatList) getIntent().getSerializableExtra("item");
        isCall = getIntent().getBooleanExtra("is_call", false);
        isReceive = getIntent().getBooleanExtra("is_received", false);
        if(getIntent().hasExtra("call_from_id")) {
            callFromUserID = Integer.parseInt(getIntent().getStringExtra("call_from_id"));
            callFromUserName = getIntent().getStringExtra("call_from_name");
            callFromUserImage = getIntent().getStringExtra("call_from_image");
            isCallAcceptFromNotification = getIntent().getBooleanExtra("is_accept", false);
        }

        ivUser = findViewById(R.id.iv_user);
        tvName = findViewById(R.id.tv_user_name);
        tvStatus = findViewById(R.id.tv_call_status);
        fabCallEnd = findViewById(R.id.fab_call_end);
        fabCallStart = findViewById(R.id.fab_call_start);
        fabSpeaker = findViewById(R.id.fab_call_speaker);
        fabMute = findViewById(R.id.fab_call_mute);
        chronometer = findViewById(R.id.chronometer);
        chronometer.animate();

        if(isCall) {
            Picasso.get().load(itemChatList.getImage()).into(ivUser);
            tvName.setText(itemChatList.getName());
            tvStatus.setText(getString(R.string.calling));

            fabCallEnd.setVisibility(View.VISIBLE);
            fabCallStart.setVisibility(View.GONE);
        } else {
            Picasso.get().load(callFromUserImage).into(ivUser);
            tvName.setText(String.valueOf(callFromUserName));
            tvStatus.setText(getString(R.string.incoming_call));

            fabCallEnd.setVisibility(View.VISIBLE);
            fabCallStart.setVisibility(View.VISIBLE);
        }

        fabCallEnd.setOnClickListener(view -> {
            hangupAction();
        });

        fabCallStart.setOnClickListener(view -> {
            if (checkPermissions() && rtcToken != null && !rtcToken.isEmpty()) {
                if(isCall) {
                joinChannel(sharedPref.getUserId().concat(itemChatList.getId()));
                } else if(isReceive) {
                    joinChannel(String.valueOf(callFromUserID).concat(sharedPref.getUserId()));
                }
            } else {
                ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSION_REQ_ID);
            }
        });

        fabSpeaker.setOnClickListener(view -> {
            if(mRtcEngine != null) {
                mRtcEngine.setEnableSpeakerphone(!mRtcEngine.isSpeakerphoneEnabled());
                if(mRtcEngine.isSpeakerphoneEnabled()) {
                    fabSpeaker.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.primary)));
                } else {
                    fabSpeaker.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.bg_speaker_off)));
                }
            }
        });

        fabMute.setOnClickListener(view -> {
            if(mRtcEngine != null) {
                isMuted = !isMuted;
                if(isMuted) {
                    mRtcEngine.muteLocalAudioStream(true);
                    fabMute.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.primary)));
                } else {
                    mRtcEngine.muteLocalAudioStream(false);
                    fabMute.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.bg_speaker_off)));
                }
            }
        });

//        api = new CallApiImpl(this);
//        prepareConfig = new PrepareConfig();
//        prepareConfig.setRtcToken(rtcToken);

        initializeRtcEngine();

//        updateCallState(CallStateType.Idle, null);
    }

//    private void initCallApi(Callback<Boolean> completion) {
//        // Create CallConfig object
//        CallConfig config = new CallConfig(
//                Constants.AGORA_APP_ID,
//                Integer.parseInt(sharedPref.getUserId()),
//                mRtcEngine,
//                createRtmSignalClient(ChatHelper.rtmClient)
//        );
//
//        // Initialize the API with the config
//        api.initialize(config);
//
//        // Set prepareConfig fields
//        prepareConfig.setRoomId(sharedPref.getUserId());
//        prepareConfig.setCallTimeoutMillisecond(50000);
////        prepareConfig.setLocalView(mViewBinding.vRight);
////        prepareConfig.setRemoteView(mViewBinding.vLeft);
//
//        // Add listener and prepare the API for a call
//        api.addListener(this);
//        api.prepareForCall(prepareConfig, agError -> {
//            completion.invoke(agError == null);
//            return null;
//        });
//    }

//    private void callAction() {
//        if(methods.isNetworkAvailable()) {
//
//            if (this.mCallState == CallStateType.Prepared) {
//                api.call(Integer.parseInt(itemChatList.getId()), CallType.Audio, aa, agError -> {
//                    if (agError != null) {
//                        api.cancelCall(agError1 -> {
//                            return null;
//                        });
//                    }
//                    return null;
//                });
//            } else {
//                initCallApi(result -> {
//
//                });
//                return;
//            }
//        }
//    }
//
    private void hangupAction() {
        if(mRtcEngine != null) {
            if (isUserJoinedCall) {
                mRtcEngine.leaveChannel();
            } else {
                if (isCall) {
                    mRtcEngine.leaveChannel();
                    PublishOptions options = new PublishOptions();
                    options.setCustomType(Constants.CALL_REJECTED);
                    ChatHelper.rtmClient.publish(String.valueOf(itemChatList.getId()), "asdads", options, new ResultCallback<Void>() {
                        @Override
                        public void onSuccess(Void responseInfo) {
                        }

                        @Override
                        public void onFailure(ErrorInfo errorInfo) {
                        }
                    });
                }
                if (isReceive) {
                    PublishOptions options = new PublishOptions();
                    options.setCustomType(Constants.CALL_REJECTED);
                    ChatHelper.rtmClient.publish(String.valueOf(callFromUserID), "asdads", options, new ResultCallback<Void>() {
                        @Override
                        public void onSuccess(Void responseInfo) {
                        }

                        @Override
                        public void onFailure(ErrorInfo errorInfo) {
                        }
                    });
                }
            }
//        if(methods.isNetworkAvailable()) {
//            api.hangup(connectedUserId, "hangup by user", agError -> {
//                return null;
//            });
//        }
        }
        getOnBackPressedDispatcher().onBackPressed();
    }

    private void closeAction() {
//        api.deinitialize(() -> {
//            api.removeListener(this);
//            mRtcEngine.stopPreview();
//            mRtcEngine.leaveChannel();
//            RtcEngine.destroy();
//            finish();
//            return null;
//        });

            mRtcEngine.stopPreview();
            mRtcEngine.leaveChannel();
            RtcEngine.destroy();
            finish();
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
//            Log.e("aaa","Join channel success");
//            runOnUiThread(() -> {
//                Toast.makeText(VoiceCallActivity.this, "Join channel success", Toast.LENGTH_SHORT).show();
//            });
            if(isCall) {
                PublishOptions options = new PublishOptions();
                options.setCustomType(Constants.CALL_CALLING);
                ChatHelper.rtmClient.publish(itemChatList.getId(), sharedPref.getName().concat("-//-").concat(sharedPref.getUserImage()), options, new ResultCallback<Void>() { // Publishing the message to the channel
                    @Override
                    public void onSuccess(Void responseInfo) {}
                    @Override
                    public void onFailure(ErrorInfo errorInfo) {}
                });
            }
            if(mRtcEngine!= null) {
                mRtcEngine.setEnableSpeakerphone(false);
                if(mRtcEngine.isSpeakerphoneEnabled()) {
                    fabSpeaker.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.primary)));
                } else {
                    fabSpeaker.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.bg_speaker_off)));
                }
            }
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
//            Log.e("aaa","User joined: " + uid);
            super.onUserJoined(uid, elapsed);
            runOnUiThread(() -> {
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.setVisibility(View.VISIBLE);
                chronometer.start();
                fabCallEnd.setVisibility(View.VISIBLE);
                fabSpeaker.setVisibility(View.VISIBLE);
                fabCallStart.setVisibility(View.GONE);
                tvStatus.setText(getString(R.string.on_call));
//                Toast.makeText(VoiceCallActivity.this, "User joined: " + uid, Toast.LENGTH_SHORT).show();
                isUserJoinedCall = true;
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) {
//            Log.e("aaa","User offline: " + uid);
            super.onUserOffline(uid, reason);
            runOnUiThread(() -> {
                getOnBackPressedDispatcher().onBackPressed();
//                Toast.makeText(VoiceCallActivity.this, "User offline: " + uid, Toast.LENGTH_SHORT).show();
                methods.showToast(getString(R.string.call_ended));
            });
        }
    };

    private void initializeRtcEngine() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = Constants.AGORA_APP_ID;
            config.mEventHandler = mRtcEventHandler;
            config.mChannelProfile = io.agora.rtc2.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
            config.mAudioScenario = io.agora.rtc2.Constants.AUDIO_SCENARIO_GAME_STREAMING;
            mRtcEngine = (RtcEngineEx) RtcEngineEx.create(config);
            mRtcEngine.setLogLevel(io.agora.rtc2.Constants.LogLevel.getValue(io.agora.rtc2.Constants.LogLevel.LOG_LEVEL_ERROR));

//            initCallApi(result -> {
////                if(isCall) {
////                    callAction();
////                }
//            });

            getRTCTokenFromServer(VoiceCallActivity.this, sharedPref.getUserId(), NEW_LOGIN);
        } catch (Exception e) {
            throw new RuntimeException("Check the error.");
        }
    }

    private void joinChannel(String channelType) {
        try {
            ChannelMediaOptions options = new ChannelMediaOptions();
            options.clientRoleType = io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER;
            options.channelProfile = io.agora.rtc2.Constants.CHANNEL_PROFILE_COMMUNICATION;
            options.publishMicrophoneTrack = true;
            options.autoSubscribeAudio = true;
            mRtcEngine.setClientRole(io.agora.rtc2.Constants.CLIENT_ROLE_AUDIENCE);
            mRtcEngine.joinChannel(rtcToken, channelType, Integer.parseInt(sharedPref.getUserId()), options);
        } catch (Exception e) {
            throw new RuntimeException("Check the error.");
        }
    }

    private String[] getRequiredPermissions() {
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

    private boolean checkPermissions() {
        for (String permission : getRequiredPermissions()) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void updateCallState(CallStateType state, @Nullable CallStateReason stateReason) {
        mCallState = state;

        switch (mCallState) {
            case Calling:
                fabCallEnd.setVisibility(View.VISIBLE);
//                fabCallStart.setVisibility(View.GONE);
                break;

            case Connected:
                fabCallEnd.setVisibility(View.VISIBLE);
//                fabCallStart.setVisibility(View.GONE);
                break;

            case Prepared:
            case Idle:
            case Failed:
                fabCallEnd.setVisibility(View.GONE);
                fabSpeaker.setVisibility(View.GONE);
                fabCallStart.setVisibility(View.VISIBLE);
                break;

            default:
                // Do nothing for other cases
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        api.removeListener(this);
        closeAction();
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
            mRtcEngine = null;
            RtcEngine.destroy();
        }
        chronometer.stop();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    public interface Callback<T> {
        void invoke(T result);
    }

    public void getRTCTokenFromServer(Context context, String userID, String requestType) {
        if (new Methods(context).isNetworkAvailable()) {
            Call<RespGenerateChatToken> call = APIClient.getClient().create(APIInterface.class).getGenerateRtcToken(new Methods(context).getAPIRequest(Constants.URL_GENERATE_RTC_TOKEN, "", "", "", "", "", "", "", "", "", "", userID, ""));
            call.enqueue(new retrofit2.Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespGenerateChatToken> call, @NonNull Response<RespGenerateChatToken> response) {
                    if (response.body() != null && response.body().getItemChatToken() != null) {
                        if (response.body().getItemChatToken().getUserToken() != null) {

                            rtcToken = response.body().getItemChatToken().getUserToken();
//                            Log.e("aaa - user token", token);
                            if (TextUtils.equals(requestType, NEW_LOGIN)) {
                                if(isCall) {
                                    joinChannel(sharedPref.getUserId().concat(itemChatList.getId()));
                                } else if(isReceive && isCallAcceptFromNotification) {
                                    NotificationManagerCompat.from(VoiceCallActivity.this).cancel(Integer.parseInt(String.valueOf(callFromUserID).concat(new SharedPref(getApplicationContext()).getUserId())));
                                    joinChannel(String.valueOf(callFromUserID).concat(sharedPref.getUserId()));
                                }
                            } else if (TextUtils.equals(requestType, RENEW_TOKEN)) {
                                if(mRtcEngine != null) {
                                    mRtcEngine.renewToken(rtcToken);
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

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCallChanges(EventCalling eventCalling) {
        try {
            if(eventCalling.getCallType().equals(Constants.CALL_RINGING)) {
                tvStatus.setText(getString(R.string.ringing));
            } else if(eventCalling.getCallType().equals(Constants.CALL_REJECTED)) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        } catch (Exception ignored) {
        }
        GlobalBus.getBus().removeStickyEvent(eventCalling);
    }

    @Override
    protected void onStart() {
        GlobalBus.getBus().register(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        GlobalBus.getBus().unregister(this);
        super.onStop();
    }
}