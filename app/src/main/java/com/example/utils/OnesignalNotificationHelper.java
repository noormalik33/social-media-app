package blogtalk.com.utils;

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

import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespPostDetails;
import blogtalk.com.items.ItemNotification;
import blogtalk.com.items.ItemUser;
import blogtalk.com.socialmedia.FollowRequestActivity;
import blogtalk.com.socialmedia.PostDetailActivity;
import blogtalk.com.socialmedia.ProfileActivity;
import blogtalk.com.socialmedia.R;
import blogtalk.com.socialmedia.SplashActivity;
import blogtalk.com.socialmedia.TextPostDetailActivity;
import com.onesignal.notifications.IDisplayableMutableNotification;
import com.onesignal.notifications.INotificationReceivedEvent;
import com.onesignal.notifications.INotificationServiceExtension;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class OnesignalNotificationHelper implements INotificationServiceExtension {
    DBHelper dbHelper;
    String message, bigpicture, title, cid = "", cname = "", url = "false", notificationType = "", userID = "", userName = "", userImage = "", postID = "", postImage = "";

    @Override
    public void onNotificationReceived(INotificationReceivedEvent event) {
        IDisplayableMutableNotification notification = event.getNotification();

        boolean showNotification = true;

//        notification.setExtender(builder -> builder.setColor(0xFF0000FF));

//        if (notification.getActionButtons() != null) {
//            for (IActionButton button : notification.getActionButtons()) {
//            }
//        }
//        Log.e("aaa - Onesignal", "notification id - " + notification.getAdditionalData().toString());
        title = notification.getTitle();
        message = notification.getBody();
        bigpicture = notification.getBigPicture();

        try {
            if (notification.getAdditionalData() != null) {
                if (notification.getAdditionalData().has("external_link")) {
                    url = notification.getAdditionalData().getString("external_link");
                }
                if (notification.getAdditionalData().has("noti_type")) {
                    notificationType = notification.getAdditionalData().getString("noti_type");
                    userID = notification.getAdditionalData().getString("user_id");
                    userName = notification.getAdditionalData().getString("user_name");
                    userImage = notification.getAdditionalData().getString("user_image");

                    if (notification.getAdditionalData().has("post_id")) {
                        postID = notification.getAdditionalData().getString("post_id");
                        postImage = notification.getAdditionalData().getString("post_image");
                    }

                    if (notificationType.equalsIgnoreCase(Constants.TAG_NOTI_TYPE_LIKE)) {
                        if (userID.equals(new SharedPref(event.getContext()).getUserId())) {
                            showNotification = false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        event.preventDefault();
        if (showNotification) {
            sendNotification(event.getContext());
        }
    }

    private void sendNotification(Context context) {
        new SharedPref(context).setNewNotification(true);
        String CHANNEL_ID = context.getString(R.string.app_name);
        Random random = new Random();
        int noti_id = random.nextInt(100);

        try {
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent;
            if (cid.isEmpty() && !url.equals("false") && !url.trim().isEmpty()) {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
            } else if (notificationType.equals(Constants.TAG_NOTI_TYPE_ACCEPT)) {
                intent = new Intent(context, ProfileActivity.class);
                Constants.pushPostID = userID;
                Constants.pushType = "profile";
                intent.putExtra("item_user", new ItemUser(Constants.pushPostID, userName, "null"));
            } else if (notificationType.equals(Constants.TAG_NOTI_TYPE_REQUEST)) {
                intent = new Intent(context, FollowRequestActivity.class);
                intent.putExtra("isFromNoti", true);
            } else if (notificationType.equals(Constants.TAG_NOTI_TYPE_LIKE)) {
                if (!postImage.isEmpty()) {
                    intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("isuser", false);
                    intent.putExtra("pos", 0);
                    intent.putExtra("isFromNoti", true);
                } else {
                    intent = new Intent(context, TextPostDetailActivity.class);
                }
                getPostDetails(context, postID);
            } else {
                intent = new Intent(context, SplashActivity.class);
                intent.putExtra("cid", cid);
                intent.putExtra("cname", cname);
            }

            PendingIntent contentIntent = PendingIntent.getActivity(context, noti_id, intent, PendingIntent.FLAG_IMMUTABLE);
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationChannel mChannel;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence notificationChannelName;
                if (notificationType.equalsIgnoreCase("")) {
                    notificationChannelName = "Push Notification";
                } else if (notificationType.equalsIgnoreCase(Constants.TAG_NOTI_TYPE_REQUEST)) {
                    notificationChannelName = context.getString(R.string.follow_request);
                    CHANNEL_ID = context.getString(R.string.follow_request);
                    message = message + " " + userName;
                } else if (notificationType.equalsIgnoreCase(Constants.TAG_NOTI_TYPE_ACCEPT)) {
                    notificationChannelName = context.getString(R.string.follow_request);
                    CHANNEL_ID = context.getString(R.string.follow_request);
                    message = userName + " " + message;
                } else if (notificationType.equalsIgnoreCase(Constants.TAG_NOTI_TYPE_LIKE)) {
                    notificationChannelName = "User Interaction";
                    CHANNEL_ID = "User Interaction";
                    message = userName + " " + message;
                } else {
                    notificationChannelName = "Push Notification";
                }
                int importance = NotificationManager.IMPORTANCE_HIGH;
                mChannel = new NotificationChannel(CHANNEL_ID, notificationChannelName, importance);
                mNotificationManager.createNotificationChannel(mChannel);
            }

            dbHelper = new DBHelper(context);
            dbHelper.addNotifications(new ItemNotification(title, message, bigpicture, url, notificationType, userID, userName, userImage, postID, postImage));

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setAutoCancel(true)
                    .setSound(uri)
                    .setLights(Color.RED, 800, 800)
                    .setContentText(message)
                    .setChannelId(CHANNEL_ID);

            mBuilder.setSmallIcon(getNotificationIcon(mBuilder, context));
            try {
                if (!userImage.isEmpty()) {
                    mBuilder.setLargeIcon(getBitmapFromURL(userImage));
                } else {
                    mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.app_icon));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (title.trim().isEmpty()) {
                mBuilder.setContentTitle(context.getString(R.string.app_name));
                mBuilder.setTicker(context.getString(R.string.app_name));
            } else {
                mBuilder.setContentTitle(title);
                mBuilder.setTicker(title);
            }

            if (!postImage.isEmpty()) {
                mBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(getBitmapFromURL(postImage)).setSummaryText(message));
            } else if (bigpicture != null) {
                mBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(getBitmapFromURL(bigpicture)).setSummaryText(message));
            } else {
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
            }

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(noti_id, mBuilder.build());
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

    private void getPostDetails(Context context, String postID) {
        Methods methods = new Methods(context);
        if (methods.isNetworkAvailable()) {

            Call<RespPostDetails> call = APIClient.getClient().create(APIInterface.class).getPostDetails(methods.getAPIRequest(Constants.URL_POST_DETAILS, postID, "", "", "", "", "", "", "", "", "", new SharedPref(context).getUserId(), ""));

            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespPostDetails> call, @NonNull Response<RespPostDetails> response) {
                    if (response.body() != null && response.body().getStatusCode().equals("200") && response.body().getItemPost() != null) {
                        Constants.arrayListPosts.add(response.body().getItemPost());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespPostDetails> call, @NonNull Throwable t) {
                    call.cancel();
                }
            });
        }
    }
}