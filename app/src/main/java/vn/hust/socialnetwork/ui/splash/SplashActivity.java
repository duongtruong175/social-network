package vn.hust.socialnetwork.ui.splash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.orhanobut.hawk.Hawk;

import vn.hust.socialnetwork.ui.authentication.AuthenticationActivity;
import vn.hust.socialnetwork.ui.main.MainActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(1);
        Window window = getWindow();
        if (window != null) {
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        // check login
        checkLogin();
    }

    private void checkLogin() {
        String token = Hawk.get(AppSharedPreferences.TOKEN_KEY, "");
        boolean isLogin = Hawk.get(AppSharedPreferences.IS_LOGIN_KEY, false);

        Intent intent;
        if (!token.isEmpty() && isLogin) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, AuthenticationActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}