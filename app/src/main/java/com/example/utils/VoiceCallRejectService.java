package blogtalk.com.utils;

import android.app.IntentService;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import blogtalk.com.chat.ChatHelper;
import blogtalk.com.eventbus.GlobalBus;

import io.agora.rtm.ErrorInfo;
import io.agora.rtm.PublishOptions;
import io.agora.rtm.ResultCallback;

public class VoiceCallRejectService extends IntentService {
    public VoiceCallRejectService() {
        super(VoiceCallRejectService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        String userID = intent.getStringExtra("call_id");

        if (userID != null && action != null && action.equals(Constants.CALL_REJECTED)) {
            try {
                PublishOptions options = new PublishOptions();
                options.setCustomType(Constants.CALL_REJECTED);
                ChatHelper.rtmClient.publish(userID, "asd", options, new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void responseInfo) {
                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) {
                    }
                });
            } catch (Exception ignored) {
            }
            NotificationManagerCompat.from(VoiceCallRejectService.this).cancel(Integer.parseInt(userID.concat(new SharedPref(getApplicationContext()).getUserId())));
        }
    }
}