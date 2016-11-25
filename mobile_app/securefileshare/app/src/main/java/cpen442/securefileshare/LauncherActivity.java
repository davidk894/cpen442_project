package cpen442.securefileshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userId = preferences.getString(Constants.SHARED_PREF_USER_ID, null);
        Intent intent;
        if(userId != null) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, CreateAccountActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
