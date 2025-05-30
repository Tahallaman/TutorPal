package com.example.tutorapp.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.tutorapp.ProfileActivity;

/**
 * Utility class for handling authentication checks and redirects
 */
public class AuthUtils {
    
    private static final String PREFS_NAME = "TutorPalAuthPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
      /**
     * Check if user is currently logged in
     * @param context Application context
     * @return true if user is logged in, false otherwise
     */
    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        String userId = prefs.getString(KEY_USER_ID, null);
        return isLoggedIn && userId != null && !userId.isEmpty();
    }
      /**
     * Get the current logged in username
     * @param context Application context
     * @return username if logged in, empty string otherwise
     */
    public static String getLoggedInUsername(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USERNAME, "");
    }
    
    /**
     * Check if user is logged in, if not show toast and redirect to profile page
     * @param context Application context
     * @param action Description of the action that requires authentication (e.g., "book a session", "send messages")
     * @return true if user is logged in, false if redirected to login
     */
    public static boolean requireAuthentication(Context context, String action) {
        if (isLoggedIn(context)) {
            return true;
        }
        
        // Show toast message
        String message = action != null && !action.isEmpty() 
            ? "Please sign in to " + action
            : "Please sign in to continue";
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        
        // Redirect to profile page for login
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        
        return false;
    }
    
    /**
     * Check if user is logged in, if not show toast and redirect to profile page
     * Uses default message "Please sign in to continue"
     * @param context Application context
     * @return true if user is logged in, false if redirected to login
     */
    public static boolean requireAuthentication(Context context) {
        return requireAuthentication(context, null);
    }
}
