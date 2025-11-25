package blogtalk.com.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import blogtalk.com.items.ItemAbout;
import blogtalk.com.items.ItemChatList;
import blogtalk.com.items.ItemNotification;
import blogtalk.com.items.ItemPost;
import blogtalk.com.items.ItemStories;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    EncryptData encryptData;
    static String DB_NAME = "socailmedia.db";
    SQLiteDatabase db;
    final Context context;
    private static final String TABLE_ABOUT = "about";
    public static final String TABLE_POSTS = "posts";
    public static final String TABLE_NOTI = "notifications";
    public static final String TABLE_CHATLIST = "chatlist";
    public static final String TABLE_STORIES = "stories";

    private static final String TAG_ID = "id";
    private static final String TAG_TITLE = "title";
    private static final String TAG_VIEWS = "views";
    private static final String TAG_LIKES = "likes";
    private static final String TAG_IMAGE_BIG = "img_big";
    private static final String TAG_IMAGE_SMALL = "img_small";
    private static final String TAG_VIDEO_URL = "video_url";
    private static final String TAG_TAGS = "tags";
    private static final String TAG_IS_FAV = "is_fav";
    private static final String TAG_NOTI_TITLE = "noti_title";
    private static final String TAG_NOTI_MESSAGE = "noti_message";
    private static final String TAG_NOTI_URL = "noti_url";
    private static final String TAG_NOTI_IMAGE = "noti_image";
    private static final String TAG_NOTI_TYPE = "noti_type";
    private static final String TAG_NOTI_POST_ID = "noti_post_id";
    private static final String TAG_NOTI_POST_IMAGE = "noti_post_image";
    private static final String TAG_NOTI_USER_ID = "noti_user_id";
    private static final String TAG_NOTI_USER_NAME = "noti_user_name";
    private static final String TAG_NOTI_USER_IMAGE = "noti_user_image";

    private static final String TAG_CHAT_ID = "chat_id";
    private static final String TAG_CHAT_NAME = "chat_name";
    private static final String TAG_CHAT_IMAGE = "chat_image";
    private static final String TAG_CHAT_IS_BLOCKED = "is_blocked";
    private static final String TAG_CHAT_IS_USER_DELETED = "is_user_deleted";
    private static final String TAG_CHAT_IS_MSG_SYNC = "is_msg_sync";
    private static final String TAG_CHAT_USER_VERIFIED = "is_user_verified";

    private static final String TAG_ABOUT_NAME = "name";
    private static final String TAG_ABOUT_LOGO = "logo";
    private static final String TAG_ABOUT_VERSION = "version";
    private static final String TAG_ABOUT_AUTHOR = "author";
    private static final String TAG_ABOUT_CONTACT = "contact";
    private static final String TAG_ABOUT_EMAIL = "email";
    private static final String TAG_ABOUT_WEBSITE = "website";
    private static final String TAG_ABOUT_DESC = "description";
    private static final String TAG_ABOUT_PRIVACY = "privacy";
    private static final String TAG_ABOUT_PUB_ID = "ad_pub";
    private static final String TAG_ABOUT_BANNER_ID = "ad_banner";
    private static final String TAG_ABOUT_INTER_ID = "ad_inter";
    private static final String TAG_ABOUT_NATIVE_ID = "ad_native";
    private static final String TAG_ABOUT_IS_BANNER = "isbanner";
    private static final String TAG_ABOUT_IS_INTER = "isinter";
    private static final String TAG_ABOUT_IS_NATIVE = "isNative";
    private static final String TAG_ABOUT_INTER_CLICK = "inter_click";
    private static final String TAG_ABOUT_NATIVE_POS = "native_pos";

    private static final String TAG_ABOUT_FB_LINK = "fb_link";
    private static final String TAG_ABOUT_TWITTER_LINK = "twitter_link";
    private static final String TAG_ABOUT_INSTAGRAM_LINK = "instagram_link";
    private static final String TAG_ABOUT_YOUTUBE_LINK = "youtube_link";
    private static final String TAG_ABOUT_VIDEO_LIMIT = "video_limit";

    private static final String TAG_STORY_ID = "story_id";
    private static final String TAG_STORY_DATE = "story_date";
    private static final String TAG_STORY_USER_ID = "user_id";
    private static final String TAG_STORY_IS_SEEN = "story_is_seen";

    String[] columns_posts = new String[]{TAG_ID, TAG_TITLE, TAG_IMAGE_BIG, TAG_IMAGE_SMALL, TAG_VIDEO_URL, TAG_VIEWS, TAG_LIKES,
            TAG_TAGS, TAG_IS_FAV};

    String[] columns_chat = new String[]{TAG_CHAT_ID, TAG_CHAT_NAME, TAG_CHAT_IMAGE, TAG_CHAT_IS_BLOCKED, TAG_CHAT_IS_MSG_SYNC, TAG_CHAT_IS_USER_DELETED, TAG_CHAT_USER_VERIFIED};

    String[] columns_noti = new String[]{TAG_ID, TAG_NOTI_TITLE, TAG_NOTI_MESSAGE, TAG_NOTI_IMAGE, TAG_NOTI_URL, TAG_NOTI_TYPE, TAG_NOTI_POST_ID, TAG_NOTI_POST_IMAGE,
            TAG_NOTI_USER_ID, TAG_NOTI_USER_NAME, TAG_NOTI_USER_IMAGE};

    String[] columns_stories = new String[]{TAG_STORY_ID, TAG_STORY_DATE, TAG_STORY_USER_ID, TAG_STORY_IS_SEEN};
    String[] columns_about = new String[]{TAG_ABOUT_NAME, TAG_ABOUT_LOGO, TAG_ABOUT_VERSION, TAG_ABOUT_AUTHOR, TAG_ABOUT_CONTACT,
            TAG_ABOUT_EMAIL, TAG_ABOUT_WEBSITE, TAG_ABOUT_DESC, TAG_ABOUT_PRIVACY, TAG_ABOUT_PUB_ID,
            TAG_ABOUT_BANNER_ID, TAG_ABOUT_INTER_ID, TAG_ABOUT_NATIVE_ID, TAG_ABOUT_IS_BANNER, TAG_ABOUT_IS_INTER,
            TAG_ABOUT_IS_NATIVE, TAG_ABOUT_INTER_CLICK, TAG_ABOUT_NATIVE_POS, TAG_ABOUT_FB_LINK, TAG_ABOUT_TWITTER_LINK,
            TAG_ABOUT_INSTAGRAM_LINK, TAG_ABOUT_YOUTUBE_LINK, TAG_ABOUT_VIDEO_LIMIT};


    // Creating table about
    private static final String CREATE_TABLE_ABOUT = "create table " + TABLE_ABOUT + "(" +
            TAG_ABOUT_NAME + " TEXT, " +
            TAG_ABOUT_LOGO + " TEXT, " +
            TAG_ABOUT_VERSION + " TEXT, " +
            TAG_ABOUT_AUTHOR + " TEXT, " +
            TAG_ABOUT_CONTACT + " TEXT, " +
            TAG_ABOUT_EMAIL + " TEXT, " +
            TAG_ABOUT_WEBSITE + " TEXT, " +
            TAG_ABOUT_DESC + " TEXT, " +
            TAG_ABOUT_PRIVACY + " TEXT, " +
            TAG_ABOUT_PUB_ID + " TEXT, " +
            TAG_ABOUT_BANNER_ID + " TEXT, " +
            TAG_ABOUT_INTER_ID + " TEXT, " +
            TAG_ABOUT_NATIVE_ID + " TEXT, " +
            TAG_ABOUT_IS_BANNER + " TEXT, " +
            TAG_ABOUT_IS_INTER + " TEXT, " +
            TAG_ABOUT_IS_NATIVE + " TEXT, " +
            TAG_ABOUT_INTER_CLICK + " TEXT, " +
            TAG_ABOUT_NATIVE_POS + " TEXT, " +
            TAG_ABOUT_FB_LINK + " TEXT, " +
            TAG_ABOUT_TWITTER_LINK + " TEXT, " +
            TAG_ABOUT_INSTAGRAM_LINK + " TEXT, " +
            TAG_ABOUT_YOUTUBE_LINK + " TEXT, " +
            TAG_ABOUT_VIDEO_LIMIT + " TEXT);";

    private static final String CREATE_TABLE_POSTS = "create table " + TABLE_POSTS + "(" +
            TAG_ID + " integer PRIMARY KEY AUTOINCREMENT," +
            TAG_TITLE + " TEXT UNIQUE," +
            TAG_IMAGE_BIG + " TEXT," +
            TAG_IMAGE_SMALL + " TEXT," +
            TAG_VIDEO_URL + " TEXT," +
            TAG_LIKES + " TEXT," +
            TAG_VIEWS + " NUMERIC," +
            TAG_IS_FAV + " TEXT);";

    private static final String CREATE_TABLE_NOTI = "create table " + TABLE_NOTI + "(" +
            TAG_ID + " integer PRIMARY KEY AUTOINCREMENT," +
            TAG_NOTI_TITLE + " TEXT," +
            TAG_NOTI_MESSAGE + " TEXT," +
            TAG_NOTI_IMAGE + " TEXT," +
            TAG_NOTI_URL + " TEXT," +
            TAG_NOTI_TYPE + " TEXT," +
            TAG_NOTI_POST_ID + " TEXT," +
            TAG_NOTI_POST_IMAGE + " TEXT," +
            TAG_NOTI_USER_ID + " TEXT," +
            TAG_NOTI_USER_NAME + " TEXT," +
            TAG_NOTI_USER_IMAGE + " TEXT);";

    private static final String CREATE_TABLE_CHAT = "create table " + TABLE_CHATLIST + "(" +
            TAG_CHAT_ID + " integer PRIMARY KEY AUTOINCREMENT," +
            TAG_CHAT_NAME + " TEXT," +
            TAG_CHAT_IMAGE + " TEXT," +
            TAG_CHAT_IS_BLOCKED + " TEXT," +
            TAG_CHAT_IS_MSG_SYNC + " TEXT," +
            TAG_CHAT_IS_USER_DELETED + " TEXT," +
            TAG_CHAT_USER_VERIFIED + " TEXT);";

    private static final String CREATE_TABLE_STORY = "create table " + TABLE_STORIES + "(" +
            TAG_STORY_ID + " TEXT," +
            TAG_STORY_DATE + " TEXT," +
            TAG_STORY_USER_ID + " TEXT," +
            TAG_STORY_IS_SEEN + " TEXT);";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, 5);
        this.context = context;
        encryptData = new EncryptData(context);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_NOTI);
            db.execSQL(CREATE_TABLE_ABOUT);
            db.execSQL(CREATE_TABLE_POSTS);
            db.execSQL(CREATE_TABLE_CHAT);
            db.execSQL(CREATE_TABLE_STORY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("Range")
    public ArrayList<ItemPost> getPosts() {
        ArrayList<ItemPost> arrayList = new ArrayList<>();


        Cursor cursor = db.query(TABLE_POSTS, columns_posts, null, null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String pid = cursor.getString(cursor.getColumnIndex(TAG_ID));
                String title = cursor.getString(cursor.getColumnIndex(TAG_TITLE));

                String img_big = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_IMAGE_BIG)));
                String video_url = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_VIDEO_URL)));
//                String img_small = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_IMAGE_SMALL)));

//                String views = String.valueOf(cursor.getInt(cursor.getColumnIndex(TAG_VIEWS)));
//                String likes = cursor.getString(cursor.getColumnIndex(TAG_LIKES));
//                String tags = cursor.getString(cursor.getColumnIndex(TAG_TAGS));
//                boolean fav = cursor.getString(cursor.getColumnIndex(TAG_IS_FAV)).equals("1");

                ItemPost itemPost = new ItemPost(pid, title, img_big, video_url, "", "", false, false, "", "", "");
                arrayList.add(itemPost);

                cursor.moveToNext();
            }
            cursor.close();
        }

        return arrayList;
    }

    public void addPosts(ItemPost itemPost) {
        String imageBig = encryptData.encrypt(itemPost.getPostImage().replace(" ", "%20"));
        String videoUrl = encryptData.encrypt(itemPost.getVideoUrl().replace(" ", "%20"));

        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_ID, itemPost.getPostID());
        contentValues.put(TAG_TITLE, itemPost.getCaptions());
        contentValues.put(TAG_IMAGE_BIG, imageBig);
        contentValues.put(TAG_VIDEO_URL, videoUrl);
//            contentValues.put(TAG_IMAGE_SMALL, imageSmall);
//            contentValues.put(TAG_VIEWS, itemPost.getTotalViews());
//            contentValues.put(TAG_LIKES, itemPost.getTotalLikes());
//            contentValues.put(TAG_TAGS, itemPost.getTags());
//            contentValues.put(TAG_IS_FAV, itemPost.isFav());

        db.insert(TABLE_POSTS, null, contentValues);
    }

    public void addNotifications(ItemNotification itemNotification) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_NOTI_TITLE, itemNotification.getTitle());
        contentValues.put(TAG_NOTI_MESSAGE, itemNotification.getMessage());
        contentValues.put(TAG_NOTI_IMAGE, itemNotification.getImage());
        contentValues.put(TAG_NOTI_URL, itemNotification.getUrl());
        contentValues.put(TAG_NOTI_TYPE, itemNotification.getNotificationType());
        contentValues.put(TAG_NOTI_POST_ID, itemNotification.getPostID());
        contentValues.put(TAG_NOTI_POST_IMAGE, itemNotification.getPostImage());
        contentValues.put(TAG_NOTI_USER_ID, itemNotification.getUserID());
        contentValues.put(TAG_NOTI_USER_NAME, itemNotification.getUserName());
        contentValues.put(TAG_NOTI_USER_IMAGE, itemNotification.getUserImage());

        db.insert(TABLE_NOTI, null, contentValues);
    }

    @SuppressLint("Range")
    public ArrayList<ItemNotification> getNotifications() {
        ArrayList<ItemNotification> arrayList = new ArrayList<>();
        try {
            Cursor c = db.query(TABLE_NOTI, columns_noti, null, null, null, null, TAG_ID + " DESC");

            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                for (int i = 0; i < c.getCount(); i++) {
                    String title = c.getString(c.getColumnIndex(TAG_NOTI_TITLE));
                    String message = c.getString(c.getColumnIndex(TAG_NOTI_MESSAGE));
                    String image = c.getString(c.getColumnIndex(TAG_NOTI_IMAGE));
                    String url = c.getString(c.getColumnIndex(TAG_NOTI_URL));
                    String type = c.getString(c.getColumnIndex(TAG_NOTI_TYPE));
                    String post_id = c.getString(c.getColumnIndex(TAG_NOTI_POST_ID));
                    String post_image = c.getString(c.getColumnIndex(TAG_NOTI_POST_IMAGE));
                    String user_id = c.getString(c.getColumnIndex(TAG_NOTI_USER_ID));
                    String user_name = c.getString(c.getColumnIndex(TAG_NOTI_USER_NAME));
                    String user_image = c.getString(c.getColumnIndex(TAG_NOTI_USER_IMAGE));
                    arrayList.add(new ItemNotification(title, message, image, url, type, user_id, user_name, user_image, post_id, post_image));

                    c.moveToNext();
                }
                c.close();
            } else {
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public void setChatBlocked(String conversationId, boolean isBlock) {
//        if(!isChatListPresent(itemChatList.getId())) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(TAG_CHAT_IS_BLOCKED, String.valueOf(isBlock));

        db.update(TABLE_CHATLIST, contentValues, TAG_CHAT_ID + "=" + conversationId, null);
//        }
    }

    public void setMessageSynced(String conversationId, boolean isSync) {
//        if(!isChatListPresent(itemChatList.getId())) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(TAG_CHAT_IS_MSG_SYNC, String.valueOf(isSync));

        db.update(TABLE_CHATLIST, contentValues, TAG_CHAT_ID + "=" + conversationId, null);
//        }
    }

    public void setUserDeleted(String conversationId, boolean isDeleted) {
//        if(!isChatListPresent(itemChatList.getId())) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(TAG_CHAT_IS_USER_DELETED, String.valueOf(isDeleted));

        db.update(TABLE_CHATLIST, contentValues, TAG_CHAT_ID + "=" + conversationId, null);
//        }
    }

    @SuppressLint("Range")
    public boolean isChatBlocked(String conversationId) {
        try {
            Cursor c = db.query(TABLE_CHATLIST, new String[]{TAG_CHAT_ID}, TAG_CHAT_ID + "=" + conversationId + " AND " + TAG_CHAT_IS_BLOCKED + "='true'", null, null, null, null);

            if (c != null) {
                if (c.getCount() > 0) {
                    c.close();
                    return true;
                } else {
                    c.close();
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressLint("Range")
    public boolean isMessageSynced(String conversationId) {
        try {
            Cursor c = db.query(TABLE_CHATLIST, new String[]{TAG_CHAT_ID}, TAG_CHAT_ID + "=" + conversationId + " AND " + TAG_CHAT_IS_MSG_SYNC + "='true'", null, null, null, null);

            if (c != null) {
                if (c.getCount() > 0) {
                    c.close();
                    return true;
                } else {
                    c.close();
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressLint("Range")
    public boolean isUserDeleted(String conversationId) {
        try {
            Cursor c = db.query(TABLE_CHATLIST, new String[]{TAG_CHAT_ID}, TAG_CHAT_ID + "=" + conversationId + " AND " + TAG_CHAT_IS_USER_DELETED + "='true'", null, null, null, null);

            if (c != null) {
                if (c.getCount() > 0) {
                    c.close();
                    return true;
                } else {
                    c.close();
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressLint("Range")
    public boolean isChatListPresent(String conversationId) {
        try {
            Cursor c = db.query(TABLE_CHATLIST, new String[]{TAG_CHAT_ID}, TAG_CHAT_ID + "=" + conversationId, null, null, null, null);

            if (c != null) {
                if (c.getCount() > 0) {
                    c.close();
                    return true;
                } else {
                    c.close();
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressLint("Range")
    public ItemChatList getChat(String conversationId) {
        ItemChatList itemChatList = null;
        try {
            Cursor c = db.query(TABLE_CHATLIST, columns_chat, TAG_CHAT_ID + "=" + conversationId, null, null, null, null);

            if (c != null) {
                if (c.getCount() > 0) {
                    c.moveToFirst();

                    String id = c.getString(c.getColumnIndex(TAG_CHAT_ID));
                    String name = c.getString(c.getColumnIndex(TAG_CHAT_NAME));
                    String image = c.getString(c.getColumnIndex(TAG_CHAT_IMAGE));
                    boolean is_blocked = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_CHAT_IS_BLOCKED)));
                    boolean is_msg_sync = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_CHAT_IS_MSG_SYNC)));
                    boolean is_user_deleted = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_CHAT_IS_USER_DELETED)));
                    boolean is_user_verified = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_CHAT_USER_VERIFIED)));

                    itemChatList = new ItemChatList(id, name, image, is_blocked, is_msg_sync, is_user_deleted, is_user_verified);
                    c.close();
                } else {
                    c.close();
                }
            }
        } catch (Exception e) {
        }
        return itemChatList;
    }

    public void addChatList(ItemChatList itemChatList) {
        if (!isChatListPresent(itemChatList.getId())) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(TAG_CHAT_ID, itemChatList.getId());
            contentValues.put(TAG_CHAT_NAME, itemChatList.getName());
            contentValues.put(TAG_CHAT_IMAGE, itemChatList.getImage());
            contentValues.put(TAG_CHAT_IS_BLOCKED, String.valueOf(itemChatList.isBlocked()));
            contentValues.put(TAG_CHAT_IS_MSG_SYNC, String.valueOf(itemChatList.isMessageSynced()));
            contentValues.put(TAG_CHAT_IS_USER_DELETED, String.valueOf(itemChatList.isUserDeleted()));
            contentValues.put(TAG_CHAT_USER_VERIFIED, String.valueOf(itemChatList.getIsUserAccountVerified()));

            db.insert(TABLE_CHATLIST, null, contentValues);
        }
    }

    @SuppressLint("Range")
    public ArrayList<ItemChatList> getChatList() {
        ArrayList<ItemChatList> arrayList = new ArrayList<>();
        try {
            Cursor c = db.query(TABLE_CHATLIST, columns_chat, null, null, null, null, null);

            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                for (int i = 0; i < c.getCount(); i++) {
                    String id = c.getString(c.getColumnIndex(TAG_CHAT_ID));
                    String name = c.getString(c.getColumnIndex(TAG_CHAT_NAME));
                    String image = c.getString(c.getColumnIndex(TAG_CHAT_IMAGE));
                    boolean is_blocked = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_CHAT_IS_BLOCKED)));
                    boolean is_msg_synced = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_CHAT_IS_MSG_SYNC)));
                    boolean is_user_deleted = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_CHAT_IS_USER_DELETED)));
                    boolean is_user_verified = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_CHAT_USER_VERIFIED)));
                    arrayList.add(new ItemChatList(id, name, image, is_blocked, is_msg_synced, is_user_deleted, is_user_verified));

                    c.moveToNext();
                }
                c.close();
            } else {
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    @SuppressLint("Range")
    public boolean isStoryPresent(String storyId) {
        try {
            Cursor c = db.query(TABLE_STORIES, new String[]{TAG_STORY_ID}, TAG_STORY_ID + "=" + storyId, null, null, null, null);

            if (c != null) {
                if (c.getCount() > 0) {
                    c.close();
                    return true;
                } else {
                    c.close();
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressLint("Range")
    public void setStorySeen(String storyId) {
        try {
            ContentValues contentValues = new ContentValues();

            contentValues.put(TAG_STORY_IS_SEEN, "1");
            db.update(TABLE_STORIES, contentValues, TAG_STORY_ID + "=" + storyId, null);
        } catch (Exception ignore) {
        }
    }

    @SuppressLint("Range")
    public boolean isStoriesSeen(String userID) {
        try {
           Cursor cursor = db.rawQuery("SELECT CASE WHEN COUNT(*) = SUM("+ TAG_STORY_IS_SEEN +") THEN 1 ELSE 0 END AS all_seen FROM "+ TABLE_STORIES +" WHERE "+ TAG_STORY_USER_ID +" = ?", new String[]{String.valueOf(userID)});
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) == 1;
            }
            cursor.close();
            return false;
        } catch (Exception ignore) {
            return false;
        }
    }

    @SuppressLint("Range")
    public boolean isStorySeen(String storyID) {
        try {
            Cursor cursor = db.query(TABLE_STORIES, new String[]{TAG_STORY_ID}, TAG_STORY_ID + "=" + storyID + " AND " + TAG_STORY_IS_SEEN + "=1", null, null, null, null);
            if (cursor.moveToFirst()) {
                return cursor.getCount()>0;
            }
            cursor.close();
            return false;
        } catch (Exception ignore) {
            return false;
        }
    }

    @SuppressLint("Range")
    public void clearOldStories() {
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault());
            long twentyFourHoursAgo = System.currentTimeMillis() - (25 * 60 * 60 * 1000);

            Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_STORIES, null);
            while (cursor.moveToNext()) {
                String storyDate = cursor.getString(cursor.getColumnIndex("story_date"));
                try {
                    Date date = dbFormat.parse(storyDate);
                    if (date != null && date.getTime() <= twentyFourHoursAgo) {
                        int storyId = cursor.getInt(cursor.getColumnIndex("story_id"));
                        db.execSQL("DELETE FROM "+TABLE_STORIES+" WHERE "+TAG_STORY_ID+" = ?", new Object[]{storyId});
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addStoriesList(ItemStories.ItemStoryPost itemStoryPost, String userID) {
        if (!isStoryPresent(itemStoryPost.getStoryID())) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(TAG_STORY_ID, itemStoryPost.getStoryID());
            contentValues.put(TAG_STORY_DATE, itemStoryPost.getDate());
            contentValues.put(TAG_STORY_USER_ID, userID);
            contentValues.put(TAG_STORY_IS_SEEN, "0");

            db.insert(TABLE_STORIES, null, contentValues);
        }
    }

    @SuppressLint("Range")
    public ArrayList<ItemChatList> getStoryList() {
        ArrayList<ItemChatList> arrayList = new ArrayList<>();
        try {
            Cursor c = db.query(TABLE_STORIES, columns_stories, null, null, null, null, null);

            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                for (int i = 0; i < c.getCount(); i++) {
                    String id = c.getString(c.getColumnIndex(TAG_STORY_ID));
                    String name = c.getString(c.getColumnIndex(TAG_STORY_DATE));
                    String is_seen = c.getString(c.getColumnIndex(TAG_STORY_IS_SEEN));

                    c.moveToNext();
                }
                c.close();
            } else {
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    @SuppressLint("Range")
    public void clearNotifications() {
        try {
            db.delete(TABLE_NOTI, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addToAbout() {
        try {
            db.delete(TABLE_ABOUT, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(TAG_ABOUT_NAME, Constants.itemAbout.getAppName());
            contentValues.put(TAG_ABOUT_LOGO, Constants.itemAbout.getAppLogo());
            contentValues.put(TAG_ABOUT_VERSION, Constants.itemAbout.getAppVersion());
            contentValues.put(TAG_ABOUT_AUTHOR, Constants.itemAbout.getAuthor());
            contentValues.put(TAG_ABOUT_CONTACT, Constants.itemAbout.getContact());
            contentValues.put(TAG_ABOUT_EMAIL, Constants.itemAbout.getEmail());
            contentValues.put(TAG_ABOUT_WEBSITE, Constants.itemAbout.getWebsite());
            contentValues.put(TAG_ABOUT_DESC, Constants.itemAbout.getAppDesc());
            contentValues.put(TAG_ABOUT_PUB_ID, Constants.publisherAdID);
            contentValues.put(TAG_ABOUT_BANNER_ID, Constants.bannerAdID);
            contentValues.put(TAG_ABOUT_INTER_ID, Constants.interstitialAdID);
            contentValues.put(TAG_ABOUT_NATIVE_ID, Constants.nativeAdID);
            contentValues.put(TAG_ABOUT_IS_BANNER, Constants.isBannerAd.toString());
            contentValues.put(TAG_ABOUT_IS_INTER, Constants.isInterAd.toString());
            contentValues.put(TAG_ABOUT_IS_NATIVE, Constants.isNativeAd.toString());
            contentValues.put(TAG_ABOUT_INTER_CLICK, Constants.interstitialAdShow);
            contentValues.put(TAG_ABOUT_NATIVE_POS, Constants.nativeAdShow);
            contentValues.put(TAG_ABOUT_VIDEO_LIMIT, Constants.videoUploadDuration);

            db.insert(TABLE_ABOUT, null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("Range")
    public Boolean getAbout() {
        try {
            Cursor c = db.query(TABLE_ABOUT, columns_about, null, null, null, null, null);

            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                for (int i = 0; i < c.getCount(); i++) {
                    String appname = c.getString(c.getColumnIndex(TAG_ABOUT_NAME));
                    String applogo = c.getString(c.getColumnIndex(TAG_ABOUT_LOGO));
                    String desc = c.getString(c.getColumnIndex(TAG_ABOUT_DESC));
                    String appversion = c.getString(c.getColumnIndex(TAG_ABOUT_VERSION));
                    String appauthor = c.getString(c.getColumnIndex(TAG_ABOUT_AUTHOR));
                    String appcontact = c.getString(c.getColumnIndex(TAG_ABOUT_CONTACT));
                    String email = c.getString(c.getColumnIndex(TAG_ABOUT_EMAIL));
                    String website = c.getString(c.getColumnIndex(TAG_ABOUT_WEBSITE));

                    Constants.bannerAdID = c.getString(c.getColumnIndex(TAG_ABOUT_BANNER_ID));
                    Constants.interstitialAdID = c.getString(c.getColumnIndex(TAG_ABOUT_INTER_ID));
                    Constants.nativeAdID = c.getString(c.getColumnIndex(TAG_ABOUT_NATIVE_ID));
                    Constants.isBannerAd = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_ABOUT_IS_BANNER)));
                    Constants.isInterAd = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_ABOUT_IS_INTER)));
                    Constants.isNativeAd = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_ABOUT_IS_NATIVE)));
                    Constants.publisherAdID = c.getString(c.getColumnIndex(TAG_ABOUT_PUB_ID));
                    Constants.interstitialAdShow = Integer.parseInt(c.getString(c.getColumnIndex(TAG_ABOUT_INTER_CLICK)));
                    Constants.nativeAdShow = Integer.parseInt(c.getString(c.getColumnIndex(TAG_ABOUT_INTER_CLICK)));
                    Constants.videoUploadDuration = c.getInt(c.getColumnIndex(TAG_ABOUT_VIDEO_LIMIT));

                    Constants.itemAbout = new ItemAbout(appname, applogo, desc, appversion, appauthor, appcontact, email, website);
                }
                c.close();
                return true;
            } else {
                c.close();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            switch (oldVersion) {
                case 1: {
                    db.execSQL(CREATE_TABLE_NOTI);
                    db.execSQL(CREATE_TABLE_CHAT);
                }
                case 2: {
                    db.execSQL(CREATE_TABLE_CHAT);
                }
                case 3: {
                    db.execSQL(CREATE_TABLE_STORY);
                }
                case 4: {
                    db.execSQL("ALTER TABLE " + TABLE_STORIES + " ADD COLUMN " + TAG_CHAT_USER_VERIFIED + " TEXT DEFAULT false");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}