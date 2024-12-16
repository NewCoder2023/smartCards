package com.example.smartcards;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.smartcards.utils.NavigationUtils;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppSettings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply the saved dark mode preference before setting the content view
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Bottom Navigation View
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_settings);
        NavigationUtils.setupBottomNavigation(this, bottomNavigationView);

        // Setup Dark Mode Switch
        setupDarkModeSwitch();

        // Setup Tilt Sensor Switch
        setupSwitch(R.id.switch_tilt_sensor, "tilt_sensor");

        // Setup Auto Mode Switch
        setupSwitch(R.id.switch_modeswitch, "auto_modeswitch");

        // Setup Push Notifications Switch
        setupSwitch(R.id.switch_notifications, "push_notifications");
    }


    private void setupDarkModeSwitch() {
        SwitchMaterial darkModeSwitch = findViewById(R.id.switch_dark_mode);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        darkModeSwitch.setChecked(isDarkMode);

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            // No need to call recreate()
        });
    }


    private void setupSwitch(int switchId, String preferenceKey) {
        SwitchMaterial switchMaterial = findViewById(switchId);
        boolean isChecked = sharedPreferences.getBoolean(preferenceKey, false);
        switchMaterial.setChecked(isChecked);

        switchMaterial.setOnCheckedChangeListener((buttonView, isChecked1) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(preferenceKey, isChecked1);
            editor.apply();
        });
    }
}
