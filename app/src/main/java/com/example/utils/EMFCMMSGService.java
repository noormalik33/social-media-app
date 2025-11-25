package com.example.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.chat.ChatHelper;
import com.example.items.ItemChatList;
import com.example.socialmedia.ChatListActivity;
import com.example.socialmedia.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.TextMessageBody;

public class EMFCMMSGService extends FirebaseMessagingService {

//    @Override
//    public void handleIntent(@NonNull Intent intent) {
//        Log.e("aaa","aaa handle intnet");
//        String aa = intent.getExtras().getString("gcm.notification.title");
//        String bb = intent.getExtras().getString("gcm.notification.body");
//
//        ChatHelper.getInstance().messageNotifier.onNewMsg(aa, bb);
//
//        super.handleIntent(intent);
//    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (!remoteMessage.getData().isEmpty()) {
            String message = remoteMessage.getData().get("alert");
        }

//        ChatHelper.getInstance().messageNotifier.onNewMsg(remoteMessage.getNotification().getTitle()+" firebase", remoteMessage.getNotification().getBody());
//        sendNotification(getApplicationContext());
    }

    private void sendNotification(Context context) {
        String username = "9";
        try {
            String notifyText = username + " ";


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

                String chatMessageText = "You have got a test message";

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setAutoCancel(true)
                        .setSound(uri)
                        .setLights(Color.RED, 800, 800)
                        .setContentText(chatMessageText)
                        .setChannelId(CHANNEL_ID);

                mBuilder.setSmallIcon(getNotificationIcon(mBuilder, context));

                ItemChatList itemChatList = new DBHelper(context).getChat("3");
                if(itemChatList != null) {
                    mBuilder.setLargeIcon(getBitmapFromURL(itemChatList.getImage()));
                    mBuilder.setContentTitle(itemChatList.getName());
                } else {
                    mBuilder.setContentTitle("3");
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

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        try {
            ChatClient.getInstance().sendFCMTokenToServer(token);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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