package com.example.tutorapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    private static final String PREFS_NAME = "TutorPalThemePrefs";
    private static final String KEY_THEME = "app_theme";
    
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2;
    
    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static void saveTheme(Context context, int theme) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(KEY_THEME, theme);
        editor.apply();
        applyTheme(theme);
    }
    
    public static int getSavedTheme(Context context) {
        return getPreferences(context).getInt(KEY_THEME, THEME_SYSTEM);
    }
    
    public static void applyTheme(int theme) {
        switch (theme) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
    
    public static void applyStoredTheme(Context context) {
        int theme = getSavedTheme(context);
        applyTheme(theme);
    }
    
    public static String getThemeName(int theme) {
        switch (theme) {
            case THEME_LIGHT:
                return "Light";
            case THEME_DARK:
                return "Dark";
            case THEME_SYSTEM:
            default:
                return "System Default";
        }
    }
}
