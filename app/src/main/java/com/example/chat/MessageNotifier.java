package com.example.chat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.items.ItemChatList;
import com.example.socialmedia.ChatListActivity;
import com.example.socialmedia.R;
import com.example.utils.DBHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import io.agora.chat.ChatMessage;
import io.agora.chat.TextMessageBody;
import io.agora.util.EMLog;
import io.agora.util.EasyUtils;

public class MessageNotifier {
    private final static String TAG = "notify";

    private final static String[] msg_eng = { "sent a message", "sent a picture", "sent a voice",
                                                "sent location message", "sent a video", "sent a file", "%1 contacts sent %2 messages"
                                              };

    private static int notifyID = 0525; // start notification id

    private NotificationManager notificationManager = null;

    private HashSet<String> fromUsers = new HashSet<String>();
    private int notificationNum = 0;

    private Context context;
    private String packageName;
    private String[] msgs;
    private long lastNotifyTime;
    private AudioManager audioManager;
    private Vibrator vibrator;

    public MessageNotifier() {
    }
    
    /**
     * this function can be override
     * @param context
     * @return
     */
    public MessageNotifier init(Context context){
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        packageName = this.context.getApplicationInfo().packageName;
        msgs = msg_eng;

        audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
        
        return this;
    }
    
    /**
     * this function can be override
     */
    public void reset(){
        resetNotificationCount();
        cancelNotification();
    }

    void resetNotificationCount() {
        notificationNum = 0;
        fromUsers.clear();
    }
    
    void cancelNotification() {
        if (notificationManager != null)
            notificationManager.cancel(notifyID);
    }

    public synchronized void onNewMsg(ChatMessage message) {
        if (!EasyUtils.isAppRunningForeground(context)) {
            EMLog.d(TAG, "app is running in background");
            sendNotification(message, false);
        } else {
            sendNotification(message, true);
        }
//        vibrateAndPlayTone(message);
    }

    private void sendNotification(ChatMessage message, boolean isForeground) {
        String username = message.getFrom();
        try {
            String notifyText = username + " ";
            switch (message.getType()) {
            case TXT:
                notifyText += msgs[0];
                break;
            case IMAGE:
                notifyText += msgs[1];
                break;
            case VOICE:
                notifyText += msgs[2];
                break;
            case LOCATION:
                notifyText += msgs[3];
                break;
            case VIDEO:
                notifyText += msgs[4];
                break;
            case FILE:
                notifyText += msgs[5];
                break;
            }


            String CHANNEL_ID = "Chat Notification";
            Random random = new Random();
            int noti_id = random.nextInt(100);

            try {
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                Intent intent = new Intent(context, ChatListActivity.class);

                PendingIntent contentIntent = PendingIntent.getActivity(context, noti_id, intent, PendingIntent.FLAG_IMMUTABLE);
                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                NotificationChannel mChannel;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    CharSequence notificationChannelName = "Chat Notification";
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    mChannel = new NotificationChannel(CHANNEL_ID, notificationChannelName, importance);
                    mNotificationManager.createNotificationChannel(mChannel);
                }

                String chatMessageText = ((TextMessageBody) message.getBody()).getMessage();

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setAutoCancel(true)
                        .setSound(uri)
                        .setLights(Color.RED, 800, 800)
                        .setContentText(chatMessageText)
                        .setChannelId(CHANNEL_ID);

                mBuilder.setSmallIcon(getNotificationIcon(mBuilder, context));

                ItemChatList itemChatList = new DBHelper(context).getChat(message.conversationId());
                if(itemChatList != null) {
                    mBuilder.setLargeIcon(getBitmapFromURL(itemChatList.getImage()));
                    mBuilder.setContentTitle(itemChatList.getName());
                } else {
                    mBuilder.setContentTitle(message.getFrom());
                }
                mBuilder.setTicker(notifyText);

                mBuilder.setContentIntent(contentIntent);
                mNotificationManager.notify(noti_id, mBuilder.build());
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void onNewMsg(String from, String content) {
        if (!EasyUtils.isAppRunningForeground(context)) {
//            EMLog.d(TAG, "app is running in background");
            sendNotification(from,content);
        } else {
            sendNotification(from,content);
        }
//        vibrateAndPlayTone(message);
    }

    public void sendNotification(String from, String content) {
        String username = from;
        try {
            String notifyText = username + " ";
//            switch (message.getType()) {
//                case TXT:
                    notifyText += msgs[0];
//                    break;
//                case IMAGE:
//                    notifyText += msgs[1];
//                    break;
//                case VOICE:
//                    notifyText += msgs[2];
//                    break;
//                case LOCATION:
//                    notifyText += msgs[3];
//                    break;
//                case VIDEO:
//                    notifyText += msgs[4];
//                    break;
//                case FILE:
//                    notifyText += msgs[5];
//                    break;
//            }


            String CHANNEL_ID = "Chat Notification";
            Random random = new Random();
            int noti_id = random.nextInt(100);

            try {
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                Intent intent = new Intent(context, ChatListActivity.class);

                PendingIntent contentIntent = PendingIntent.getActivity(context, noti_id, intent, PendingIntent.FLAG_IMMUTABLE);
                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                NotificationChannel mChannel;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    CharSequence notificationChannelName = "Chat Notification";
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    mChannel = new NotificationChannel(CHANNEL_ID, notificationChannelName, importance);
                    mNotificationManager.createNotificationChannel(mChannel);
                }

                String chatMessageText = content;

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setAutoCancel(true)
                        .setSound(uri)
                        .setLights(Color.RED, 800, 800)
                        .setContentText(chatMessageText)
                        .setChannelId(CHANNEL_ID);

                mBuilder.setSmallIcon(getNotificationIcon(mBuilder, context));

                ItemChatList itemChatList = new DBHelper(context).getChat(from);
                if(itemChatList != null) {
                    mBuilder.setLargeIcon(getBitmapFromURL(itemChatList.getImage()));
                    mBuilder.setContentTitle(itemChatList.getName());
                } else {
                    mBuilder.setContentTitle(from);
                }
                mBuilder.setTicker(notifyText);

                mBuilder.setContentIntent(contentIntent);
                mNotificationManager.notify(noti_id, mBuilder.build());
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void vibrateAndPlayTone(ChatMessage message) {
//        if (System.currentTimeMillis() - lastNotifyTime < 1000) {
//            // received new messages within 2 seconds, skip play ringtone
//            return;
//        }
//
//        try {
//            lastNotifyTime = System.currentTimeMillis();
//
//            // check if in silent mode
//            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
//                EMLog.e(TAG, "in silent mode now");
//                return;
//            }
////            EaseUI.EaseSettingsProvider settingsProvider = EaseUI.getInstance().getSettingsProvider();
////            if(settingsProvider.isMsgVibrateAllowed(message)){
////                long[] pattern = new long[] { 0, 180, 80, 120 };
////                vibrator.vibrate(pattern, -1);
////            }
////
////            if(settingsProvider.isMsgSoundAllowed(message)){
////                if (ringtone == null) {
////                    Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
////
////                    ringtone = RingtoneManager.getRingtone(appContext, notificationUri);
////                    if (ringtone == null) {
////                        EMLog.d(TAG, "can't find ringtone at: " + notificationUri.getPath());
////                        return;
////                    }
////                }
////
////                if (!ringtone.isPlaying()) {
////                    String vendor = Build.MANUFACTURER;
////
////                    ringtone.play();
////                    // for samsung S3, we encounter a bug that the phone will continue ringing without stop
////                    // add the following handler to stop it after 3s if needed
////                    if (vendor != null && vendor.toLowerCase().contains("samsung")) {
////                        Thread ctlThread = new Thread() {
////                            public void run() {
////                                try {
////                                    Thread.sleep(3000);
////                                    if (ringtone.isPlaying()) {
////                                        ringtone.stop();
////                                    }
////                                } catch (Exception e) {
////                                }
////                            }
////                        };
////                        ctlThread.run();
////                    }
////                }
////            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private int getNotificationIcon(NotificationCompat.Builder notificationBuilder, Context context) {
        notificationBuilder.setColor(ContextCompat.getColor(context.getApplicationContext(), R.color.primary));
        return R.drawable.ic_notification;
    }

    public Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            InputStream input;
            if (src.contains("https://")) {
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                input = connection.getInputStream();
            } else {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                input = connection.getInputStream();
            }
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            return null;
        }
    }
}
