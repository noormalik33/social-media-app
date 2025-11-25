package blogtalk.com.fragments;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import blogtalk.com.adapters.AdapterDownloads;
import blogtalk.com.socialmedia.R;
import blogtalk.com.utils.BackgroundTask;
import blogtalk.com.utils.Methods;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class FragmentDownloads extends Fragment {

    private Methods methods;
    RecyclerView rv_post;
    AdapterDownloads adapterDownloads;
    ArrayList<Uri> arrayList = new ArrayList<>();
    CircularProgressBar pb;
    int tabPosition = 0;
    LinearLayout ll_empty;
    String errorMsg = "";

    public static FragmentDownloads newInstance(int position) {
        FragmentDownloads fragment = new FragmentDownloads();
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_downloads, container, false);

        tabPosition = getArguments().getInt("pos",0);

        methods = new Methods(getActivity());

        rv_post = rootView.findViewById(R.id.rv_downloads);
        pb = rootView.findViewById(R.id.pb);
        ll_empty = rootView.findViewById(R.id.ll_empty);

        rv_post.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        new BackgroundTask() {
            @Override
            public void onPreExecute() {
            }
            @Override
            public boolean doInBackground() {
                if (tabPosition == 0) {
                    getImages();
                } else {
                    getVideos();
                }
                return false;
            }
            @Override
            public void onPostExecute(Boolean isExecutionSuccess) {
                setAdapter();
            }
        }.execute();

        return rootView;
    }

    @SuppressLint("Range")
    public void getImages() {
        arrayList.clear();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            String selection;
            String[] selectionArgs;

            selection = MediaStore.Files.FileColumns.RELATIVE_PATH + " like? ";
            selectionArgs = new String[]{"%" + getString(R.string.app_name) + "%"};

            Cursor cursor = getActivity().getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME},
                    selection,
                    selectionArgs,
                    null);

            if (cursor != null && cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    String id = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                    arrayList.add(Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id));

                    cursor.moveToNext();
                }

                cursor.close();
            }
        } else {
            File root = new File(Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_PICTURES + File.separator + getResources().getString(R.string.app_name));
            if (root.exists()) {
                String[] okFileExtensions = new String[]{"jpg", "jpeg", "png"};
                Collection<File> images = FileUtils.listFiles(root, okFileExtensions, true);
                for (File image : images) {
                    arrayList.add(getImageContentUri(image));
                }
            }
        }
    }

    @SuppressLint("Range")
    public void getVideos() {
        arrayList.clear();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            String selection;
            String[] selectionArgs;

                selection = MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " like? ";
                selectionArgs = new String[]{getString(R.string.app_name)};

            Cursor cursor = getActivity().getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME},
                    selection,
                    selectionArgs,
                    null);

            if (cursor != null && cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    String id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                    arrayList.add(Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id));

                    cursor.moveToNext();
                }

                cursor.close();
            }
        } else {
            File root = new File(Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_MOVIES + File.separator + getResources().getString(R.string.app_name));
            if (root.exists()) {
                String[] okFileExtensions = new String[]{"mp4"};
                Collection<File> video = FileUtils.listFiles(root, okFileExtensions, true);
                for (File videos : video) {
                    arrayList.add(getVideoImageUri(videos));
                }
            }
        }
    }

    private void setAdapter() {
        if(adapterDownloads == null) {
            adapterDownloads = new AdapterDownloads(getActivity(), arrayList, tabPosition == 0);
            rv_post.setAdapter(adapterDownloads);
        } else {
            adapterDownloads.notifyDataSetChanged();
        }

        setEmpty();
    }

    private void setEmpty() {
        pb.setVisibility(View.GONE);
        if (arrayList.size() == 0) {
            ll_empty.setVisibility(View.VISIBLE);
            rv_post.setVisibility(View.GONE);
        } else {
            rv_post.setVisibility(View.VISIBLE);
            ll_empty.setVisibility(View.GONE);
        }
    }

    @SuppressLint("Range")
    public Uri getImageContentUri(File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = getActivity().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        }
        return null;
    }

    @SuppressLint("Range")
    public Uri getVideoImageUri(File videoFile) {
        String filePath = videoFile.getAbsolutePath();
        Cursor cursor = getActivity().getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID},
                MediaStore.Video.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "" + id);
        }
        return null;
    }
}