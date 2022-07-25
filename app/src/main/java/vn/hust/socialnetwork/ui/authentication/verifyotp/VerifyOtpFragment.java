package vn.hust.socialnetwork.ui.authentication.verifyotp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.orhanobut.hawk.Hawk;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.authentication.TokenResponse;
import vn.hust.socialnetwork.models.user.User;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.AuthenticationService;
import vn.hust.socialnetwork.ui.main.MainActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.ContextExtension;

public class VerifyOtpFragment extends Fragment {

    private AuthenticationService authenticationService;

    private static final long OTP_COUNT_DOWN = 600000;
    private static final long OTP_COUNT_DOWN_INTERVAL = 1000;
    private CountDownTimer countDown;

    private int type;
    private User user;

    private ProgressBar pbLoading;
    private AppCompatTextView tvVerifyOtpLabel, tvVerifyOtpRetry, tvVerifyOtpRetryTime, tvVerifyOtpNotice;
    private AppCompatButton btnVerify;
    private TextInputEditText etOtp;

    public VerifyOtpFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getInt("type");
            user = getArguments().getParcelable("user");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_verify_otp, container, false);

        authenticationService = ApiClient.getClient().create(AuthenticationService.class);

        // binding
        pbLoading = view.findViewById(R.id.pb_loading);
        tvVerifyOtpLabel = view.findViewById(R.id.tv_verify_otp_label);
        tvVerifyOtpRetry = view.findViewById(R.id.tv_verify_otp_retry);
        tvVerifyOtpRetryTime = view.findViewById(R.id.tv_verify_otp_retry_time);
        btnVerify = view.findViewById(R.id.btn_verify);
        etOtp = view.findViewById(R.id.et_otp);
        tvVerifyOtpNotice = view.findViewById(R.id.tv_verify_otp_notice);

        // init data and view
        if (type == 1) {
            tvVerifyOtpLabel.setText(R.string.verify_register);
        } else if (type == 2) {
            tvVerifyOtpLabel.setText(R.string.change_password);
        }
        // text notice
        SpannableStringBuilder textNotice = new SpannableStringBuilder();
        textNotice.append(getString(R.string.verify_otp_notice_1));
        textNotice.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.color_otp_notice)), 0, textNotice.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textNotice.append(" ");
        textNotice.append(getString(R.string.verify_otp_notice_2));
        tvVerifyOtpNotice.setText(textNotice);

        tvVerifyOtpRetry.setEnabled(false);
        countDownResentOtp();
        ContextExtension.showKeyboard(etOtp);

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContextExtension.hideKeyboard(getActivity());
                String otp_code = etOtp.getText().toString().trim();
                if (otp_code.length() != 6) {
                    ContextExtension.showKeyboard(etOtp);
                } else {
                    if (type == 1) {
                        pbLoading.setVisibility(View.VISIBLE);
                        // call api register verify otp
                        Call<BaseResponse<TokenResponse>> call = authenticationService.registerVerifyOtp(otp_code, user.getId());
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
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                } else {
                                    pbLoading.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), R.string.error_no_otp_match, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<BaseResponse<TokenResponse>> call, Throwable t) {
                                // error network (no internet connection, socket timeout, unknown host, ...)
                                // error serializing/deserializing the data
                                call.cancel();
                                Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                                pbLoading.setVisibility(View.GONE);
                            }                        });
                    }
                    else if (type == 2) {
                        pbLoading.setVisibility(View.VISIBLE);
                        // call api forgot password verify otp
                        Call<BaseResponse<String>> call = authenticationService.forgotPasswordVerifyOtp(otp_code, user.getId());
                        call.enqueue(new Callback<BaseResponse<String>>() {
                            @Override
                            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                                if (response.isSuccessful()) {
                                    // navigate to verify otp
                                    Bundle bundle = new Bundle();
                                    bundle.putParcelable("user", user);
                                    NavController navController = NavHostFragment.findNavController(VerifyOtpFragment.this);
                                    navController.navigate(R.id.action_verifyOtpFragment_to_changePasswordFragment, bundle);
                                } else {
                                    pbLoading.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), R.string.error_no_otp_match, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                                // error network (no internet connection, socket timeout, unknown host, ...)
                                // error serializing/deserializing the data
                                call.cancel();
                                Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                                pbLoading.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            }
        });

        tvVerifyOtpRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (countDown != null) {
                    countDown.cancel();
                }
                // call api resent otp
                pbLoading.setVisibility(View.VISIBLE);
                Call<BaseResponse<String>> call = authenticationService.resentOtp(user.getId(), type);
                call.enqueue(new Callback<BaseResponse<String>>() {
                    @Override
                    public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                        if (response.isSuccessful()) {
                            tvVerifyOtpRetry.setEnabled(false);
                            countDownResentOtp();
                        } else {
                            Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                        }
                        pbLoading.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                        // error network (no internet connection, socket timeout, unknown host, ...)
                        // error serializing/deserializing the data
                        call.cancel();
                        Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                        pbLoading.setVisibility(View.GONE);
                    }
                });
            }
        });

        return view;
    }

    private void countDownResentOtp() {
        countDown = new CountDownTimer(OTP_COUNT_DOWN, OTP_COUNT_DOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                long timeRemaining = millisUntilFinished / OTP_COUNT_DOWN_INTERVAL;
                tvVerifyOtpRetryTime.setText(timeRemaining + "s");
            }

            @Override
            public void onFinish() {
                tvVerifyOtpRetry.setEnabled(true);
                tvVerifyOtpRetryTime.setText("");
            }
        };
        countDown.start();
    }
}