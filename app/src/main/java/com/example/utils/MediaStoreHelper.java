package com.example.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.items.ItemMedia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MediaStoreHelper {

    public static ArrayList<ItemMedia> getImagesFromFolder(Context context, String folderName) {
        ArrayList<ItemMedia> arrayListImages = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.Images.Media._ID};
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";


        try {
//            Cursor cursor = contentResolver.query(imageUri, projection, selection, selectionArgs, sortOrder);

            Cursor cursor;

            if(folderName.equalsIgnoreCase("recent")) {
                cursor = contentResolver.query(
                        imageUri,
                        projection,
                        null, // No selection to fetch from all folders
                        null, // No selection arguments
                        sortOrder
                );
            } else {
                String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?";
                String[] selectionArgs = new String[]{folderName};

                cursor = contentResolver.query(
                        imageUri,
                        projection,
                        selection,
                        selectionArgs,
                        sortOrder);
            }

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
//                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    // Do something with the image path

                    arrayListImages.add(new ItemMedia(uri, MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE, 0, 0));
                } while (cursor.moveToNext());

                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayListImages;
    }

    // Function to get videos from a specific folder
    public static ArrayList<ItemMedia> getVideosFromFolder(Context context, String folderName) {
        ArrayList<ItemMedia> arrayListVideos = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.SIZE};
        String selection = MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " = ?";
        String[] selectionArgs = new String[]{folderName};
        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";

        try {
            Cursor cursor = contentResolver.query(
                    videoUri,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    Uri uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                    long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))/(1024*1024);
                    arrayListVideos.add(new ItemMedia(uri, MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO, duration, size));
                } while (cursor.moveToNext());

                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayListVideos;
    }

    public static Set<String> getImageFolders(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        String sortOrder = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " ASC";
        Set<String> folderSet = new HashSet<>();
        try {
            Cursor cursor = contentResolver.query(imageUri, projection, null, null, sortOrder);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String folderName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    if (folderName != null) {
                        folderSet.add(folderName);
                    }
                    // Do something with the folder name
                } while (cursor.moveToNext());

                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return folderSet;
    }

    // Function to get folder names for videos
    public static Set<String> getVideoFolders(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.Video.Media.BUCKET_DISPLAY_NAME};
        String sortOrder = MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " ASC";
        Set<String> folderSet = new HashSet<>();
        try {
            Cursor cursor = contentResolver.query(videoUri, projection, null, null, sortOrder);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String folderName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                    if (folderName != null) {
                        folderSet.add(folderName);
                    }
                    // Do something with the folder name
                } while (cursor.moveToNext());

                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return folderSet;
    }

    public static ArrayList<ItemMedia> getAllMedia(Context context, String folderName, String mediaType) {
        ArrayList<ItemMedia> mediaList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        // Define projection to retrieve common fields
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE
        };

        // Initialize selection and selectionArgs
        String selection = "";
        String[] selectionArgs = null;

        // Adjust selection and selectionArgs based on mediaType
        switch (mediaType.toLowerCase()) {
            case "image":
                selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " = ?";
                selectionArgs = new String[]{
                        String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
                };
                break;
            case "video":
                selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " = ?";
                selectionArgs = new String[]{
                        String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
                };
                break;
            case "both":
                selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " IN (?, ?)";
                selectionArgs = new String[]{
                        String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                        String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
                };
                break;
            default:
                throw new IllegalArgumentException("Invalid mediaType. Use 'image', 'video', or 'both'.");
        }

        // Add folder-specific filtering for non-recent folders
        if (!folderName.equalsIgnoreCase("recent")) {
            selection += " AND " + MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?";
            selectionArgs = appendToArray(selectionArgs, folderName); // Helper method to append folderName
        }

        // Sorting by the most recently added
        String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";

        Uri mediaUri = MediaStore.Files.getContentUri("external");

        try (Cursor cursor = contentResolver.query(mediaUri, projection, selection, selectionArgs, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
                    int mediaTypeValue = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE));
                    Uri uri = Uri.withAppendedPath(mediaUri, id);

                    long duration = 0;
                    long size = 0;

                    if (mediaTypeValue == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                        duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                        size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)) / (1024 * 1024); // Size in MB
                    }

                    mediaList.add(new ItemMedia(uri, mediaTypeValue, duration, size));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mediaList;
    }

    // Helper method to append a value to an array
    private static String[] appendToArray(String[] original, String value) {
        if (original == null) {
            return new String[]{value};
        }
        String[] newArray = Arrays.copyOf(original, original.length + 1);
        newArray[original.length] = value;
        return newArray;
    }


    public static Set<String> getAllMediaFolders(Context context, String mediaType) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri mediaUri = MediaStore.Files.getContentUri("external");

        // Define projection
        String[] projection = {MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        // Initialize selection and selectionArgs
        String selection = null;
        String[] selectionArgs = null;

        // Adjust selection based on mediaType
        switch (mediaType.toLowerCase()) {
            case "image":
                selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " = ?";
                selectionArgs = new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)};
                break;
            case "video":
                selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " = ?";
                selectionArgs = new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)};
                break;
            case "both":
                selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " IN (?, ?)";
                selectionArgs = new String[]{
                        String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                        String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
                };
                break;
            default:
                throw new IllegalArgumentException("Invalid mediaType. Use 'image', 'video', or 'both'.");
        }

        String sortOrder = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " ASC";

        Set<String> folderSet = new HashSet<>();
        try (Cursor cursor = contentResolver.query(mediaUri, projection, selection, selectionArgs, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String folderName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    if (folderName != null) {
                        folderSet.add(folderName);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return folderSet;
    }
}
