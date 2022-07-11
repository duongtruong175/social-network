package vn.hust.socialnetwork.ui.authentication.login;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.razir.progressbutton.ButtonTextAnimatorExtensionsKt;
import com.github.razir.progressbutton.DrawableButton;
import com.github.razir.progressbutton.DrawableButtonExtensionsKt;
import com.github.razir.progressbutton.ProgressButtonHolderKt;
import com.github.razir.progressbutton.ProgressParams;
import com.google.android.material.textfield.TextInputEditText;
import com.orhanobut.hawk.Hawk;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.authentication.LoginRequest;
import vn.hust.socialnetwork.models.authentication.TokenResponse;
import vn.hust.socialnetwork.models.user.User;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.AuthenticationService;
import vn.hust.socialnetwork.ui.main.MainActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.ContextExtension;

public class LoginFragment extends Fragment {

    private AuthenticationService authenticationService;

    private TextInputEditText etEmail, etPassword;
    private AppCompatButton btnLogin;
    private AppCompatTextView tvRegister, tvForgotPassword;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // api
        authenticationService = ApiClient.getClient().create(AuthenticationService.class);

        // binding
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        tvForgotPassword = view.findViewById(R.id.tv_forgot_password);
        btnLogin = view.findViewById(R.id.btn_login);
        tvRegister = view.findViewById(R.id.tv_register);

        // set animation for button login
        ProgressButtonHolderKt.bindProgressButton(getViewLifecycleOwner(), btnLogin);
        ButtonTextAnimatorExtensionsKt.attachTextChangeAnimator(btnLogin);

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRegister();
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickForgotPassword();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickLogin();
            }
        });

        return view;
    }

    private void onClickLogin() {
        ContextExtension.hideKeyboard(getActivity());

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (email.isEmpty()) {
            ContextExtension.showKeyboard(etEmail);
        } else if (password.isEmpty()) {
            ContextExtension.showKeyboard(etPassword);
        } else {
            enableLoadingLogin();
            LoginRequest loginBody = new LoginRequest(email, password);
            // call api login
            Call<BaseResponse<TokenResponse>> call = authenticationService.login(loginBody);
            call.enqueue(new Callback<BaseResponse<TokenResponse>>() {
                @Override
                public void onResponse(Call<BaseResponse<TokenResponse>> call, Response<BaseResponse<TokenResponse>> response) {
                    if (response.isSuccessful()) {
                        BaseResponse<TokenResponse> res = response.body();
                        TokenResponse data = res.getData();
                        User user = data.getUser();
                        if (data.getToken().isEmpty() || user.getEmailVerifiedAt() == null || user.getEmailVerifiedAt().isEmpty()) {
                            // call api resent otp
                            Call<BaseResponse<String>> call1 = authenticationService.resentOtp(user.getId(), 1);
                            call1.enqueue(new Callback<BaseResponse<String>>() {
                                @Override
                                public void onResponse(Call<BaseResponse<String>> call1, Response<BaseResponse<String>> response) {
                                    if (response.isSuccessful()) {
                                        // navigate to verify otp
                                        Bundle bundle = new Bundle();
                                        bundle.putInt("type", 1);
                                        bundle.putParcelable("user", user);
                                        NavController navController = NavHostFragment.findNavController(LoginFragment.this);
                                        navController.navigate(R.id.action_loginFragment_to_verifyOtpFragment, bundle);
                                    } else {
                                        Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<BaseResponse<String>> call1, Throwable t) {
                                    // error network (no internet connection, socket timeout, unknown host, ...)
                                    // error serializing/deserializing the data
                                    call1.cancel();
                                    Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Hawk.put(AppSharedPreferences.TOKEN_KEY, data.getToken());
                            Hawk.put(AppSharedPreferences.IS_LOGIN_KEY, true);
                            Hawk.put(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, user.getId());
                            Hawk.put(AppSharedPreferences.LOGGED_IN_USER_NAME_KEY, user.getName());
                            Hawk.put(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, user.getAvatar());
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    } else {
                        // response code is not 2xx
                        disableLoadingLogin();
                        Toast.makeText(getContext(), R.string.error_login, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse<TokenResponse>> call, Throwable t) {
                    // error network (no internet connection, socket timeout, unknown host, ...)
                    // error serializing/deserializing the data
                    call.cancel();
                    disableLoadingLogin();
                    Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void onClickRegister() {
        ContextExtension.hideKeyboard(getActivity());

        NavController navController = NavHostFragment.findNavController(LoginFragment.this);
        navController.navigate(R.id.action_loginFragment_to_registerFragment);
    }

    private void onClickForgotPassword() {
        ContextExtension.hideKeyboard(getActivity());

        NavController navController = NavHostFragment.findNavController(LoginFragment.this);
        navController.navigate(R.id.action_loginFragment_to_forgotPasswordFragment);
    }

    private void enableLoadingLogin() {
        btnLogin.setEnabled(false);
        // start animation button
        DrawableButtonExtensionsKt.showProgress(btnLogin, new Function1<ProgressParams, Unit>() {
            @Override
            public Unit invoke(ProgressParams progressParams) {
                progressParams.setProgressColor(Color.WHITE);
                progressParams.setGravity(DrawableButton.GRAVITY_CENTER);
                return Unit.INSTANCE;
            }
        });
    }

    private void disableLoadingLogin() {
        btnLogin.setEnabled(true);
        DrawableButtonExtensionsKt.hideProgress(btnLogin, R.string.authentication_login);
    }
}