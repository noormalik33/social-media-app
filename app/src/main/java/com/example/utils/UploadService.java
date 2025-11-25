package blogtalk.com.utils;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespSuccess;
import blogtalk.com.eventbus.EventStoryUpload;
import blogtalk.com.eventbus.GlobalBus;
import blogtalk.com.socialmedia.MainActivity;
import blogtalk.com.socialmedia.R;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("MissingPermission")
public class UploadService extends IntentService {
    private static final String CHANNEL_ID = "file_upload";
    private static final int NOTIFICATION_ID = 123;
    boolean isVideo = false, isStory = false;
    String requestString = "", selectedVideoPath = "";
    NotificationCompat.Builder builder;
    NotificationManagerCompat notificationManager;

    public UploadService() {
        super("UploadService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            requestString = intent.getStringExtra("requestString");
            selectedVideoPath = intent.getStringExtra("selectedVideoPath");
            isVideo = intent.getBooleanExtra("isVideo", false);
            isStory = intent.getBooleanExtra("isStory", false);

            uploadFileInBackground();
        }
    }

    private void uploadFileInBackground() {
        getAddPost();
    }

    private void showNotification() {
        createNotificationChannel();

        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.uploading_post))
                .setContentText(getString(R.string.uploading_post))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(100, 0, true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void updateNotification(String contentText, int progress) {
        builder.setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(contentText + " - " + progress + "%")
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(100, progress, false);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void updateNotification(String contentText, int progress, int currentImage, int totalImages) {
        String title = String.format("%s - %d%% (%d/%d)", contentText, progress, currentImage, totalImages);
        builder.setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(100, progress, false);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void updateNotificationImage(String imagePath) {

        new BackgroundTask() {
            Bitmap bitmap;

            @Override
            public void onPreExecute() {

            }

            @Override
            public boolean doInBackground() {
                bitmap = getBitmapFromPath(imagePath);
                return false;
            }

            @Override
            public void onPostExecute(Boolean isExecutionSuccess) {
                if (bitmap != null) {
                    builder.setLargeIcon(bitmap);
                }
            }
        }.execute();
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void completedNotification(String contentText) {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("isfromuploadnoti", true);

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 14, intent, PendingIntent.FLAG_IMMUTABLE);

        builder.setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setContentTitle(contentText)
                .setContentText("")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(contentIntent)
                .setProgress(0, 0, false);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.post_upload);
            String description = "Notification for Post Upload Progress";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            notificationManager = NotificationManagerCompat.from(this);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void getAddPost() {
        showNotification();

        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        builder.addFormDataPart("data", requestString);

        File fileImage = null, fileVideo = null;

        if (!Constants.arrayListSelectedImagesPath.isEmpty()) {
            for (int i = 0; i < Constants.arrayListSelectedImagesPath.size(); i++) {
                String imagePath = Constants.arrayListSelectedImagesPath.get(i);
                fileImage = new File(imagePath);

                final int currentImageIndex = i + 1; // 1-based index

                updateNotificationImage(imagePath);

                CountingRequestBody countingRequestBody = new CountingRequestBody(
                        RequestBody.create(MediaType.parse("image/*"), fileImage),
                        new CountingRequestBody.Listener() {
                            @Override
                            public void onProgress(long bytesWritten, long contentLength) {
                                int progress = (int) ((bytesWritten / (float) contentLength) * 100);
//                                updateNotification(getString(R.string.uploading_image), progress);
                                updateNotification(
                                        getString(R.string.uploading_image),
                                        progress,
                                        currentImageIndex,
                                        Constants.arrayListSelectedImagesPath.size()
                                );
                            }
                        }
                );

                if (!isStory) {
                    builder.addFormDataPart(!isVideo ? "image[]" : "image", System.currentTimeMillis() + "", countingRequestBody);
                } else {
                    builder.addFormDataPart("media_file", System.currentTimeMillis() + "", countingRequestBody);
                }
            }
        }


//        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
//            fileImage = new File(selectedImagePath);
//
//            updateNotificationImage(selectedImagePath);
//
//            CountingRequestBody countingRequestBody = new CountingRequestBody(RequestBody.create(MediaType.parse("image/*"), fileImage), new CountingRequestBody.Listener() {
//                @Override
//                public void onProgress(long bytesWritten, long contentLength) {
//                    // Update your progress bar here
//                    int progress = (int) ((bytesWritten / (float) contentLength) * 100);
////                    progressBar.setProgress(progress);
//                    updateNotification(getString(R.string.uploading_image), progress);
//                }
//            });
//
//            if(!isStory) {
//                builder.addFormDataPart("image", System.currentTimeMillis() + "", countingRequestBody);
//            } else {
//                builder.addFormDataPart("media_file", System.currentTimeMillis() + "", countingRequestBody);
//            }
//        }

        if (isVideo) {


            fileVideo = new File(selectedVideoPath);

            RequestBody videoRequestBody = RequestBody.create(MediaType.parse("video/*"), fileVideo);

            CountingRequestBody countingRequestBody = new CountingRequestBody(videoRequestBody, new CountingRequestBody.Listener() {
                @Override
                public void onProgress(long bytesWritten, long contentLength) {
                    int progress = (int) ((bytesWritten / (float) contentLength) * 100);
                    updateNotification(getString(R.string.uploading_video), progress);
                }
            });

            builder.addFormDataPart("video_file", System.currentTimeMillis() + "." + FilenameUtils.getExtension(fileVideo.getName()), countingRequestBody);
        }

        RequestBody requestBody = builder.build();
        Call<RespSuccess> call;
        if (!isStory) {
            call = APIClient.getClient().create(APIInterface.class).getDoUploadPost(requestBody);
        } else {
            call = APIClient.getClient().create(APIInterface.class).getUploadStory(requestBody);
        }
        call.enqueue(new Callback<RespSuccess>() {
            @Override
            public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                if (response.isSuccessful()) {
                    completedNotification(getString(R.string.uploading_complete));
                    if (isStory) {
                        GlobalBus.getBus().postSticky(new EventStoryUpload(true));
                    }
                } else {
                    completedNotification(getString(R.string.uploading_failed));
                }
            }

            @Override
            public void onFailure(@NonNull Call<RespSuccess> call, @NonNull Throwable t) {
                completedNotification(getString(R.string.uploading_failed));
                call.cancel();
            }
        });
    }

    private Bitmap getBitmapFromPath(String path) {
        File sd = new File(path);
        if (sd.exists()) {
            return BitmapFactory.decodeFile(sd.getAbsolutePath());
        }
        return null;
    }
}