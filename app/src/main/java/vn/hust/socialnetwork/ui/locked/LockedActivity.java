package vn.hust.socialnetwork.ui.locked;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.orhanobut.hawk.Hawk;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.AuthenticationService;
import vn.hust.socialnetwork.ui.authentication.AuthenticationActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.ContextExtension;

public class LockedActivity extends AppCompatActivity {

    private AuthenticationService authenticationService;

    private AppCompatTextView tvLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locked);

        // api
        authenticationService = ApiClient.getClient().create(AuthenticationService.class);

        // binding
        tvLogout = findViewById(R.id.tv_logout);

        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void logout() {
        Dialog processDialog = ContextExtension.createProgressDialog(LockedActivity.this);
        processDialog.show();
        int userId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0);
        Call<BaseResponse<String>> call = authenticationService.logout();
        call.enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful()) {
                    // reset value
                    Hawk.put(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0);
                    Hawk.put(AppSharedPreferences.LOGGED_IN_USER_NAME_KEY, "");
                    Hawk.put(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, "");
                    Hawk.put(AppSharedPreferences.IS_LOGIN_KEY, false);
                    Hawk.put(AppSharedPreferences.TOKEN_KEY, "");
                    // clear FCM token
                    FirebaseDatabase.getInstance().getReference("fcmTokens").child(String.valueOf(userId)).setValue(null);

                    Intent intent = new Intent(LockedActivity.this, AuthenticationActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(LockedActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
                processDialog.dismiss();
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                // error
                call.cancel();
                processDialog.dismiss();
                Toast.makeText(LockedActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }
}