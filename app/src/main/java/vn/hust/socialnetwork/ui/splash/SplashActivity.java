package vn.hust.socialnetwork.ui.splash;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.database.FirebaseDatabase;
import com.orhanobut.hawk.Hawk;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.user.User;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.UserProfileService;
import vn.hust.socialnetwork.ui.authentication.AuthenticationActivity;
import vn.hust.socialnetwork.ui.locked.LockedActivity;
import vn.hust.socialnetwork.ui.main.MainActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private UserProfileService userProfileService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // api
        userProfileService = ApiClient.getClient().create(UserProfileService.class);

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
        int userId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0);
        String userName = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_NAME_KEY, "");

        if (!token.isEmpty() && isLogin && userId != 0 && !userName.isEmpty()) {
            // check locked
            Call<BaseResponse<User>> call = userProfileService.getUserProfileById(userId);
            call.enqueue(new Callback<BaseResponse<User>>() {
                @Override
                public void onResponse(Call<BaseResponse<User>> call, Response<BaseResponse<User>> response) {
                    if (response.isSuccessful()) {
                        BaseResponse<User> res = response.body();
                        User user = res.getData();
                        Hawk.put(AppSharedPreferences.LOGGED_IN_USER_NAME_KEY, user.getName());
                        Hawk.put(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, user.getAvatar());
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                    } else {
                        // clear FCM token
                        FirebaseDatabase.getInstance().getReference("fcmTokens").child(String.valueOf(userId)).setValue(null);
                        // error because user_status_code = 2
                        Intent intent = new Intent(SplashActivity.this, LockedActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse<User>> call, Throwable t) {
                    // error network
                    call.cancel();
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                }
            });
        } else {
            Intent intent = new Intent(SplashActivity.this, AuthenticationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }
    }
}