package vn.hust.socialnetwork.ui.authentication.changepassword;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class ChangePasswordFragment extends Fragment {

    private AuthenticationService authenticationService;

    private User user;

    private ProgressBar pbLoading;
    private TextInputEditText etPassword, etConfirmPassword;
    private AppCompatTextView tvErrorPassword, tvErrorConfirmPassword;
    private AppCompatButton btnVerify;

    public ChangePasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = getArguments().getParcelable("user");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        authenticationService = ApiClient.getClient().create(AuthenticationService.class);

        // binding
        pbLoading = view.findViewById(R.id.pb_loading);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        tvErrorPassword = view.findViewById(R.id.tv_error_password);
        tvErrorConfirmPassword = view.findViewById(R.id.tv_error_confirm_password);
        btnVerify = view.findViewById(R.id.btn_verify);

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContextExtension.hideKeyboard(getActivity());
                tvErrorPassword.setText("");
                tvErrorConfirmPassword.setText("");
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                if (password.length() < 8 || !validatePassword(password)) {
                    tvErrorPassword.setText(R.string.error_password);
                    ContextExtension.showKeyboard(etPassword);
                } else if (!confirmPassword.equals(password)) {
                    tvErrorConfirmPassword.setText(R.string.error_confirm_password);
                    ContextExtension.showKeyboard(etConfirmPassword);
                } else {
                    pbLoading.setVisibility(View.VISIBLE);
                    // call api change password
                    ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(user.getId(), password, confirmPassword);
                    Call<BaseResponse<TokenResponse>> call = authenticationService.forgotPasswordComplete(changePasswordRequest);
                    call.enqueue(new Callback<BaseResponse<TokenResponse>>() {
                        @Override
                        public void onResponse(Call<BaseResponse<TokenResponse>> call, Response<BaseResponse<TokenResponse>> response) {
                            if (response.isSuccessful()) {
                                BaseResponse<TokenResponse> res = response.body();
                                TokenResponse data = res.getData();
                                Hawk.put(AppSharedPreferences.TOKEN_KEY, data.getToken());
                                Hawk.put(AppSharedPreferences.IS_LOGIN_KEY, true);
                                Hawk.put(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, user.getId());
                                Hawk.put(AppSharedPreferences.LOGGED_IN_USER_NAME_KEY, user.getName());
                                Hawk.put(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, user.getAvatar());
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            } else {
                                pbLoading.setVisibility(View.GONE);
                                Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<BaseResponse<TokenResponse>> call, Throwable t) {
                            // error network (no internet connection, socket timeout, unknown host, ...)
                            // error serializing/deserializing the data
                            call.cancel();
                            Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                            pbLoading.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });

        return view;
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