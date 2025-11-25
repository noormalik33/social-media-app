package com.example.utils;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;

import com.example.socialmedia.R;

public class StatusBarUtil {
    public static void setStatusBar(Activity activity) {
        EdgeUtils.enable(activity);

        boolean isLightMode = (activity.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO;

        Window window = activity.getWindow();
        window.setNavigationBarColor(ContextCompat.getColor(activity, R.color.bg_bottom_nav));

        View decorView = window.getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController insetsController = decorView.getWindowInsetsController();

            if (insetsController != null) {
                if (isLightMode) {
                    // Light mode → black icons
                    insetsController.setSystemBarsAppearance(
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS |
                                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS |
                                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    );
                } else {
                    // Dark mode → white icons
                    insetsController.setSystemBarsAppearance(
                            0,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS |
                                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    );
                }
            }
        } else {
            int flags = decorView.getSystemUiVisibility();
            if (isLightMode) {
                // Light mode → dark icons (status + nav bar)
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
            } else {
                // Dark mode → light icons (status + nav bar)
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
            }
            decorView.setSystemUiVisibility(flags);
        }
    }

    public static void setStatusBarDarkActivity(Activity activity) {
        Window window = activity.getWindow();
        Drawable background = ContextCompat.getDrawable(activity, R.drawable.status_bar_black);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setBackgroundDrawable(background);
        EdgeUtils.enable(activity);

        window.setNavigationBarColor(ContextCompat.getColor(activity, R.color.black));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            View decorView = activity.getWindow().getDecorView();
            WindowInsetsController insetsController = decorView.getWindowInsetsController();

            if (insetsController != null) {
                // Dark mode → white icons
                insetsController.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                );
            }
        } else {
            View decorView = window.getDecorView();
            int flags = decorView.getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decorView.setSystemUiVisibility(flags);
        }
    }

    public static void setStatusBarMainActivity(Activity activity) {
        boolean isLightMode = (activity.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO;

        Window window = activity.getWindow();
        window.setNavigationBarColor(ContextCompat.getColor(activity, R.color.bg_bottom_nav));

        View decorView = window.getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            WindowInsetsController insetsController = decorView.getWindowInsetsController();

            if (insetsController != null) {
                if (isLightMode) {
                    // Light mode → black icons
                    insetsController.setSystemBarsAppearance(
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS |
                                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS |
                                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    );
                } else {
                    // Dark mode → white icons
                    insetsController.setSystemBarsAppearance(
                            0,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS |
                                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    );
                }
            }
        } else {
            int flags = decorView.getSystemUiVisibility();
            if (isLightMode) {
                // Light mode → dark icons (status + nav bar)
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
            } else {
                // Dark mode → light icons (status + nav bar)
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
            }
            decorView.setSystemUiVisibility(flags);
        }
    }
}