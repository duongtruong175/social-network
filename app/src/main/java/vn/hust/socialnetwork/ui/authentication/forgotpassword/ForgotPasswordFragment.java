package vn.hust.socialnetwork.ui.authentication.forgotpassword;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.user.User;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.AuthenticationService;
import vn.hust.socialnetwork.utils.ContextExtension;

public class ForgotPasswordFragment extends Fragment {

    private AuthenticationService authenticationService;

    private AppCompatTextView tvToolbarTitle, tvErrorEmail;
    private AppCompatImageView ivToolbarBack;
    private TextInputEditText etEmail;
    private AppCompatButton btnSearchAccount;
    private ProgressBar pbLoading;

    public ForgotPasswordFragment() {
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
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        authenticationService = ApiClient.getClient().create(AuthenticationService.class);

        // binding
        ivToolbarBack = view.findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = view.findViewById(R.id.tv_toolbar_title);
        etEmail = view.findViewById(R.id.et_email);
        tvErrorEmail = view.findViewById(R.id.tv_error_email);
        btnSearchAccount = view.findViewById(R.id.btn_search_account);
        pbLoading = view.findViewById(R.id.pb_loading);

        tvToolbarTitle.setText(R.string.search_account);

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContextExtension.hideKeyboard(getActivity());
                NavController navController = NavHostFragment.findNavController(ForgotPasswordFragment.this);
                navController.navigateUp();
            }
        });

        btnSearchAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContextExtension.hideKeyboard(getActivity());
                tvErrorEmail.setText("");
                String email = etEmail.getText().toString().trim();

                if (email.isEmpty() || !validateEmail(email)) {
                    tvErrorEmail.setText(R.string.error_email_validation);
                    ContextExtension.showKeyboard(etEmail);
                } else {
                    pbLoading.setVisibility(View.VISIBLE);
                    // get user api
                    Call<BaseResponse<User>> call = authenticationService.forgotPassword(email);
                    call.enqueue(new Callback<BaseResponse<User>>() {
                        @Override
                        public void onResponse(Call<BaseResponse<User>> call, Response<BaseResponse<User>> response) {
                            if (response.isSuccessful()) {
                                BaseResponse<User> res = response.body();
                                User user = res.getData();
                                // navigate to verify otp
                                Bundle bundle = new Bundle();
                                bundle.putInt("type", 2);
                                bundle.putParcelable("user", user);
                                NavController navController = NavHostFragment.findNavController(ForgotPasswordFragment.this);
                                navController.navigate(R.id.action_forgotPasswordFragment_to_verifyOtpFragment, bundle);
                            } else {
                                pbLoading.setVisibility(View.GONE);
                                tvErrorEmail.setText(R.string.error_no_email_match);
                            }
                        }

                        @Override
                        public void onFailure(Call<BaseResponse<User>> call, Throwable t) {
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

        ContextExtension.showKeyboard(etEmail);

        return view;
    }

    private boolean validateEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pattern = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pattern.matcher(email).matches();
    }
}