package vn.hust.socialnetwork.ui.menuprofile.changepassword;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.orhanobut.hawk.Hawk;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.authentication.ChangePasswordRequest;
import vn.hust.socialnetwork.models.authentication.TokenResponse;
import vn.hust.socialnetwork.models.user.User;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.AuthenticationService;
import vn.hust.socialnetwork.ui.main.MainActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.ContextExtension;

public class ChangePasswordActivity extends AppCompatActivity {

    private AppCompatImageView ivToolbarBack;
    private ProgressBar pbLoading;
    private TextInputEditText etPassword, etConfirmPassword, etCurrentPassword;
    private AppCompatTextView tvToolbarTitle, tvErrorPassword, tvErrorConfirmPassword;
    private AppCompatButton btnVerify;

    private User user;

    private AuthenticationService authenticationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user = extras.getParcelable("user");
        }

        // api
        authenticationService = ApiClient.getClient().create(AuthenticationService.class);

        // binding
        pbLoading = findViewById(R.id.pb_loading);
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        ivToolbarBack = findViewById(R.id.iv_toolbar_back);
        etCurrentPassword = findViewById(R.id.et_current_password);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        tvErrorPassword = findViewById(R.id.tv_error_password);
        tvErrorConfirmPassword = findViewById(R.id.tv_error_confirm_password);
        btnVerify = findViewById(R.id.btn_verify);

        tvToolbarTitle.setText(R.string.toolbar_title_change_password);

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePassword();
            }
        });
    }

    private void updatePassword() {
        btnVerify.setEnabled(false);
        pbLoading.setVisibility(View.VISIBLE);
        ContextExtension.hideKeyboard(ChangePasswordActivity.this);
        tvErrorPassword.setVisibility(View.GONE);
        tvErrorPassword.setText("");
        tvErrorConfirmPassword.setVisibility(View.GONE);
        tvErrorConfirmPassword.setText("");
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (currentPassword.isEmpty()) {
            ContextExtension.showKeyboard(etCurrentPassword);
        } else if (password.length() < 8 || !validatePassword(password)) {
            tvErrorPassword.setVisibility(View.VISIBLE);
            tvErrorPassword.setText(R.string.error_password);
            ContextExtension.showKeyboard(etPassword);
        } else if (!confirmPassword.equals(password)) {
            tvErrorConfirmPassword.setVisibility(View.VISIBLE);
            tvErrorConfirmPassword.setText(R.string.error_confirm_password);
            ContextExtension.showKeyboard(etConfirmPassword);
        } else {
            pbLoading.setVisibility(View.VISIBLE);
            // call api change password
            Call<BaseResponse<String>> call = authenticationService.updatePassword(currentPassword, password, confirmPassword);
            call.enqueue(new Callback<BaseResponse<String>>() {
                @Override
                public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ChangePasswordActivity.this, R.string.change_password_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, R.string.error_change_password, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                    // error network (no internet connection, socket timeout, unknown host, ...)
                    // error serializing/deserializing the data
                    call.cancel();
                    Toast.makeText(ChangePasswordActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            });
        }
        pbLoading.setVisibility(View.GONE);
        btnVerify.setEnabled(true);
    }

    private boolean validatePassword(String password) {
        String passwordRegex = "^(?=.*?[A-Za-z])" +   // one character
                "(?=.*?[0-9])" +                      // one digit
                "(?=.*?[#?!@$%^&*-])" +                 // one special character
                ".{8,}$";                             // length > 8

        Pattern pattern = Pattern.compile(passwordRegex);
        if (password == null)
            return false;
        return pattern.matcher(password).matches();
    }
}