package vn.hust.socialnetwork.ui.menuprofile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.orhanobut.hawk.Hawk;
import com.tbruyelle.rxpermissions3.RxPermissions;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.user.User;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.AuthenticationService;
import vn.hust.socialnetwork.ui.authentication.AuthenticationActivity;
import vn.hust.socialnetwork.ui.qrcodescanner.QrCodeScannerActivity;
import vn.hust.socialnetwork.ui.relation.RelationActivity;
import vn.hust.socialnetwork.ui.search.SearchActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;

public class MenuProfileActivity extends AppCompatActivity {

    private AppCompatImageView ivToolbarBack, ivToolbarQrCode, ivToolbarSearch;
    private CircleImageView civAvatar;
    private AppCompatTextView tvName, tvLogout;
    private LinearLayoutCompat lName;
    private ConstraintLayout lFriendMenuProfile, lGroupMenuProfile, lChangePasswordMenuProfile, lSecurityMenuProfile, lSettingMenuProfile;

    private User user;
    private AuthenticationService authenticationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_profile);

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user = extras.getParcelable("user");
        }

        // api
        authenticationService = ApiClient.getClient().create(AuthenticationService.class);

        // binding
        ivToolbarBack = findViewById(R.id.iv_toolbar_back);
        ivToolbarQrCode = findViewById(R.id.iv_toolbar_qr_code);
        ivToolbarSearch = findViewById(R.id.iv_toolbar_search);
        civAvatar = findViewById(R.id.civ_avatar);
        lName = findViewById(R.id.l_name);
        tvName = findViewById(R.id.tv_name);
        lFriendMenuProfile = findViewById(R.id.l_friend_menu_profile);
        lGroupMenuProfile = findViewById(R.id.l_group_menu_profile);
        lChangePasswordMenuProfile = findViewById(R.id.l_change_password_menu_profile);
        lSecurityMenuProfile = findViewById(R.id.l_security_menu_profile);
        lSettingMenuProfile = findViewById(R.id.l_setting_menu_profile);
        tvLogout = findViewById(R.id.tv_logout);

        // init data
        Glide.with(MenuProfileActivity.this)
                .asBitmap()
                .load(user.getAvatar())
                .error(R.drawable.default_avatar)
                .into(civAvatar);
        tvName.setText(user.getName());

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ivToolbarQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open qr code activity
                RxPermissions rxPermissions = new RxPermissions(MenuProfileActivity.this);
                rxPermissions.request(Manifest.permission.CAMERA)
                        .subscribe(granted -> {
                            if (granted) {
                                Intent intent = new Intent(MenuProfileActivity.this, QrCodeScannerActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(MenuProfileActivity.this, R.string.permission_request_denied, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        ivToolbarSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuProfileActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        lName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open fragment user profile
                finish();
            }
        });

        lFriendMenuProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuProfileActivity.this, RelationActivity.class);
                startActivity(intent);
            }
        });

        lGroupMenuProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("navigation_to", "group");
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        lChangePasswordMenuProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open activity change password
            }
        });

        lSecurityMenuProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open activity security
            }
        });

        lSettingMenuProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open activity setting
            }
        });

        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void logout() {
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
                    Intent intent = new Intent(MenuProfileActivity.this, AuthenticationActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(MenuProfileActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                // error
                call.cancel();
                Toast.makeText(MenuProfileActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }
}