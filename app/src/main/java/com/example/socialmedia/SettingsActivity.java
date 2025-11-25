package blogtalk.com.socialmedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import blogtalk.com.adapters.AdapterPages;
import blogtalk.com.utils.BackgroundTask;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.onesignal.OneSignal;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.DecimalFormat;

public class SettingsActivity extends AppCompatActivity {

    SharedPref sharedPref;
    Methods methods;
    RecyclerView rv_pages;
    ImageView iv_back;
    ConstraintLayout cl_about, cl_theme, cl_clear_cache, cl_share_app, cl_rate_app, cl_more_app, cl_permission, cl_contact_us;
    SwitchMaterial switch_noti;
    AdapterPages adapterPages;
    TextView tv_theme, tv_cache_size;
    Boolean isNoti = true;
    String them_mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPref = new SharedPref(this);
        methods = new Methods(this);
        methods.forceRTLIfSupported();

        isNoti = sharedPref.getIsNotification();
        findViewById(R.id.tv_noti_permission).setVisibility(methods.getPerNotificationStatus() ? View.GONE : View.VISIBLE);
        them_mode = methods.getDarkMode();

        rv_pages = findViewById(R.id.rv_pages);
        iv_back = findViewById(R.id.iv_settings_back);
        cl_theme = findViewById(R.id.cl_settings_theme);
        cl_permission = findViewById(R.id.cl_permission);
        cl_about = findViewById(R.id.cl_settings_about);
        tv_theme = findViewById(R.id.tv_setting_theme);
        switch_noti = findViewById(R.id.switch_setting_noti);
        cl_clear_cache = findViewById(R.id.cl_settings_cache);
        cl_share_app = findViewById(R.id.cl_settings_share);
        cl_rate_app = findViewById(R.id.cl_settings_rate);
        cl_more_app = findViewById(R.id.cl_settings_more);
        cl_contact_us = findViewById(R.id.cl_settings_contact_us);
        tv_cache_size = findViewById(R.id.tv_setting_cache_size);

        switch_noti.setChecked(sharedPref.getIsNotification());
        switch (them_mode) {
            case Constants.DARK_MODE_SYSTEM:
                tv_theme.setText(getString(R.string.system_default));
                break;
            case Constants.DARK_MODE_OFF:
                tv_theme.setText(getString(R.string.light));
                break;
            case Constants.DARK_MODE_ON:
                tv_theme.setText(getString(R.string.dark));
                break;
        }

        iv_back.setOnClickListener(view -> onBackPressed());

        cl_theme.setOnClickListener(view -> {
            openThemeDialog();
        });

        cl_about.setOnClickListener(view -> {
            Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
            startActivity(intent);
        });

        cl_permission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!methods.getPerNotificationStatus()) {
                    methods.checkPerNotification();
                }
            }
        });

        switch_noti.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    OneSignal.getUser().getPushSubscription().optIn();
                } else{
                    OneSignal.getUser().getPushSubscription().optOut();
                }
                sharedPref.setIsNotification(isChecked);
            }
        });

        cl_clear_cache.setOnClickListener(v -> {

            ProgressDialog progressDialog = new ProgressDialog(SettingsActivity.this);
            progressDialog.setMessage(getString(R.string.clearing_cache));

            new BackgroundTask() {
                @Override
                public void onPreExecute() {
                    progressDialog.show();
                }
                @Override
                public boolean doInBackground() {
                    try {
                        FileUtils.deleteQuietly(getCacheDir());
                        FileUtils.deleteQuietly(getExternalCacheDir());
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }
                @Override
                public void onPostExecute(Boolean isExecutionSuccess) {
                    progressDialog.dismiss();
                    if(isExecutionSuccess) {
                        Toast.makeText(SettingsActivity.this, getString(R.string.cache_cleared), Toast.LENGTH_SHORT).show();
                        tv_cache_size.setText("0 MB");
                    } else {
                        methods.showToast("error");
                    }
                }
            }.execute();
        });

        cl_share_app.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.app_name) + " - http://play.google.com/store/apps/details?id=" + getPackageName());
            startActivity(intent);
        });

        cl_rate_app.setOnClickListener(v -> {
            final String appName = getPackageName();//your application package name i.e play store application url
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id="
                                + appName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id="
                                + appName)));
            }
        });

        cl_more_app.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_apps))));
        });

        cl_contact_us.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ContactUsActivity.class);
            startActivity(intent);
        });

        if(Constants.arrayListPages.size() > 0) {
            rv_pages.setLayoutManager(new LinearLayoutManager(SettingsActivity.this) {
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            });
            adapterPages = new AdapterPages(SettingsActivity.this, Constants.arrayListPages);
            rv_pages.setAdapter(adapterPages);
        }

        initializeCache();

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);
    }

    private void openThemeDialog() {
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_theme, null);

        BottomSheetDialog dialog_theme = new BottomSheetDialog(SettingsActivity.this, R.style.BottomSheetDialogStyle);
        dialog_theme.setContentView(view);
        dialog_theme.show();

        RadioGroup radioGroup = dialog_theme.findViewById(R.id.rg_theme);
        MaterialButton btn_cancel = dialog_theme.findViewById(R.id.btn_theme_cancel);
        MaterialButton btn_save = dialog_theme.findViewById(R.id.btn_theme_save);

        switch (sharedPref.getDarkMode()) {
            case Constants.DARK_MODE_SYSTEM:
                radioGroup.check(radioGroup.getChildAt(0).getId());
                break;
            case Constants.DARK_MODE_OFF:
                radioGroup.check(radioGroup.getChildAt(1).getId());
                break;
            case Constants.DARK_MODE_ON:
                radioGroup.check(radioGroup.getChildAt(2).getId());
                break;
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                MaterialRadioButton rb = group.findViewById(checkedId);
                if (null != rb && checkedId > -1) {
                    if (checkedId == R.id.rb_system_them) {
                        them_mode = Constants.DARK_MODE_SYSTEM;
                    } else if (checkedId == R.id.rb_light_them) {
                        them_mode = Constants.DARK_MODE_OFF;
                    } else if (checkedId == R.id.rb_dark_them) {
                        them_mode = Constants.DARK_MODE_ON;
                    }
                }
            }
        });

        btn_cancel.setOnClickListener(v -> dialog_theme.dismiss());

        btn_save.setOnClickListener(v -> {
            sharedPref.setDarkMode(them_mode);
            switch (them_mode) {
                case Constants.DARK_MODE_SYSTEM:
                    tv_theme.setText(getResources().getString(R.string.system_default));
                    break;
                case Constants.DARK_MODE_OFF:
                    tv_theme.setText(getResources().getString(R.string.light));
                    break;
                case Constants.DARK_MODE_ON:
                    tv_theme.setText(getResources().getString(R.string.dark));
                    break;
                default:
                    break;
            }
            dialog_theme.dismiss();

            String mode = sharedPref.getDarkMode();
            switch (mode) {
                case Constants.DARK_MODE_SYSTEM:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    break;
                case Constants.DARK_MODE_OFF:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                case Constants.DARK_MODE_ON:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
            }
        });
    }

    private void initializeCache() {
        long size = 0;
        size += getDirSize(this.getCacheDir());
        size += getDirSize(this.getExternalCacheDir());
        tv_cache_size.setText(readableFileSize(size));
    }

    public long getDirSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file != null && file.isDirectory()) {
                size += getDirSize(file);
            } else if (file != null && file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

    public static String readableFileSize(long size) {
        if (size <= 0) return "0 Bytes";
        final String[] units = new String[]{"Bytes", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(permissions.length > 0 && permissions[0].equalsIgnoreCase("android.permission.post_notifications")) {
            if(grantResults.length >0 && grantResults[0] != -1) {
                findViewById(R.id.tv_noti_permission).setVisibility(View.GONE);
            }
        }
    }
}