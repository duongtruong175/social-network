package vn.hust.socialnetwork.ui.authentication.register;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.github.razir.progressbutton.ButtonTextAnimatorExtensionsKt;
import com.github.razir.progressbutton.DrawableButton;
import com.github.razir.progressbutton.DrawableButtonExtensionsKt;
import com.github.razir.progressbutton.ProgressButtonHolderKt;
import com.github.razir.progressbutton.ProgressParams;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.authentication.RegisterRequest;
import vn.hust.socialnetwork.models.user.User;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.AuthenticationService;
import vn.hust.socialnetwork.utils.ContextExtension;

public class RegisterFragment extends Fragment {

    private AuthenticationService authenticationService;

    private TextInputEditText etEmail, etPassword, etConfirmPassword, etName;
    private RadioGroup rgGender;
    private AppCompatTextView etBirthday, tvTermService, tvLogin, tvErrorEmail, tvErrorPassword, tvErrorConfirmPassword, tvErrorBirthday, tvErrorName;
    private AppCompatButton btnRegister;

    private Calendar c;
    private DatePickerDialog dpd;

    public RegisterFragment() {
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
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // api
        authenticationService = ApiClient.getClient().create(AuthenticationService.class);

        // binding
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        etName = view.findViewById(R.id.et_name);
        etBirthday = view.findViewById(R.id.et_birthday);
        rgGender = view.findViewById(R.id.rg_gender);
        tvTermService = view.findViewById(R.id.tv_term_service);
        tvLogin = view.findViewById(R.id.tv_login);
        btnRegister = view.findViewById(R.id.btn_register);
        tvErrorEmail = view.findViewById(R.id.tv_error_email);
        tvErrorPassword = view.findViewById(R.id.tv_error_password);
        tvErrorConfirmPassword = view.findViewById(R.id.tv_error_confirm_password);
        tvErrorName = view.findViewById(R.id.tv_error_name);
        tvErrorBirthday = view.findViewById(R.id.tv_error_birthday);

        // set animation for button login
        ProgressButtonHolderKt.bindProgressButton(getViewLifecycleOwner(), btnRegister);
        ButtonTextAnimatorExtensionsKt.attachTextChangeAnimator(btnRegister);

        tvTermService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickViewTermService();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickLogin();
            }
        });

        etBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBirthdayPicker();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRegister();
            }
        });

        return view;
    }

    private void onClickRegister() {
        ContextExtension.hideKeyboard(getActivity());
        tvErrorEmail.setVisibility(View.GONE);
        tvErrorEmail.setText("");
        tvErrorPassword.setVisibility(View.GONE);
        tvErrorPassword.setText("");
        tvErrorConfirmPassword.setVisibility(View.GONE);
        tvErrorConfirmPassword.setText("");
        tvErrorName.setVisibility(View.GONE);
        tvErrorName.setText("");
        tvErrorBirthday.setVisibility(View.GONE);
        tvErrorBirthday.setText("");

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String name = etName.getText().toString().trim();
        // convert birthday dd/MM/yyyy to sql date yyyy-MM-dd
        String birthday = etBirthday.getText().toString().trim();
        String[] s = birthday.split("/");
        birthday = s[2] + "-" + s[1] + "-" + s[0];
        // get gender
        int rbSelectedId = rgGender.getCheckedRadioButtonId();
        String gender = getString(R.string.male); // default "Nam"
        if (rbSelectedId == R.id.rb_male){
            gender = getString(R.string.male);
        } else if (rbSelectedId == R.id.rb_female) {
            gender = getString(R.string.female);
        } else if (rbSelectedId == R.id.rb_other) {
            gender = getString(R.string.other);
        }

        // validate
        if (email.isEmpty() || !validateEmail(email)) {
            tvErrorEmail.setVisibility(View.VISIBLE);
            tvErrorEmail.setText(R.string.error_email_validation);
            ContextExtension.showKeyboard(etEmail);
        } else if (password.length() < 8 || !validatePassword(password)) {
            tvErrorPassword.setVisibility(View.VISIBLE);
            tvErrorPassword.setText(R.string.error_password);
            ContextExtension.showKeyboard(etPassword);
        } else if (!confirmPassword.equals(password)) {
            tvErrorConfirmPassword.setVisibility(View.VISIBLE);
            tvErrorConfirmPassword.setText(R.string.error_confirm_password);
            ContextExtension.showKeyboard(etConfirmPassword);
        } else if (name.length() < 2) {
            tvErrorName.setVisibility(View.VISIBLE);
            tvErrorName.setText(R.string.error_name);
            ContextExtension.showKeyboard(etName);
        } else if (!validateBirthday(birthday)) {
            tvErrorBirthday.setVisibility(View.VISIBLE);
            tvErrorBirthday.setText(R.string.error_birthday);
            etBirthday.requestFocus();
        } else {
            enableLoadingRegister();
            RegisterRequest registerBody = new RegisterRequest(email, password, confirmPassword, name, birthday, gender);
            // call api register
            Call<BaseResponse<User>> call = authenticationService.register(registerBody);
            call.enqueue(new Callback<BaseResponse<User>>() {
                @Override
                public void onResponse(Call<BaseResponse<User>> call, Response<BaseResponse<User>> response) {
                    if (response.isSuccessful()) {
                        BaseResponse<User> res = response.body();
                        User user = res.getData();
                        // navigate to verify otp
                        Bundle bundle = new Bundle();
                        bundle.putInt("type", 1);
                        bundle.putParcelable("user", user);
                        NavController navController = NavHostFragment.findNavController(RegisterFragment.this);
                        navController.navigate(R.id.action_registerFragment_to_verifyOtpFragment, bundle);
                    } else if (response.code() == 500) {
                        disableLoadingRegister();
                        Toast.makeText(getContext(), R.string.error_email_not_exist, Toast.LENGTH_SHORT).show();
                    } else {
                        // response code is not 2xx
                        disableLoadingRegister();
                        Toast.makeText(getContext(), R.string.error_register, Toast.LENGTH_SHORT).show();
                        try {
                            JSONObject res = new JSONObject(response.errorBody().string());
                            JSONObject errors = res.getJSONObject("errors");
                            if (errors.has("email")) {
                                tvErrorEmail.setVisibility(View.VISIBLE);
                                tvErrorEmail.setText(R.string.error_email_exist);
                                etEmail.requestFocus();
                            }
                            if (errors.has("birthday")) {
                                tvErrorBirthday.setVisibility(View.VISIBLE);
                                tvErrorBirthday.setText(R.string.error_birthday);
                                etBirthday.requestFocus();
                            }
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse<User>> call, Throwable t) {
                    // error network (no internet connection, socket timeout, unknown host, ...)
                    // error serializing/deserializing the data
                    call.cancel();
                    disableLoadingRegister();
                    Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void onClickViewTermService() {
        ContextExtension.hideKeyboard(getActivity());
    }

    private void onClickBirthdayPicker() {
        ContextExtension.hideKeyboard(getActivity());
        String birthday = etBirthday.getText().toString().trim();
        int day, month, year;
        if (birthday.equals("dd/MM/yyyy")) {
            c = Calendar.getInstance();
            day = c.get(Calendar.DAY_OF_MONTH);
            month = c.get(Calendar.MONTH);
            year = c.get(Calendar.YEAR);
        } else {
            String[] s = birthday.split("/");
            day = Integer.parseInt(s[0]);
            month = Integer.parseInt(s[1]) - 1;
            year = Integer.parseInt(s[2]);
        }

        dpd = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int nYear, int nMonth, int nDay) {
                c = Calendar.getInstance();
                c.set(nYear, nMonth, nDay);
                Date date = c.getTime();
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                etBirthday.setText(formatter.format(date));
            }
        }, year, month, day);
        dpd.show();
    }

    private void onClickLogin() {
        ContextExtension.hideKeyboard(getActivity());

        NavController navController = NavHostFragment.findNavController(RegisterFragment.this);
        navController.navigateUp();
    }

    private void enableLoadingRegister() {
        btnRegister.setEnabled(false);
        // start animation button
        DrawableButtonExtensionsKt.showProgress(btnRegister, new Function1<ProgressParams, Unit>() {
            @Override
            public Unit invoke(ProgressParams progressParams) {
                progressParams.setProgressColor(Color.WHITE);
                progressParams.setGravity(DrawableButton.GRAVITY_CENTER);
                return Unit.INSTANCE;
            }
        });
    }

    private void disableLoadingRegister() {
        btnRegister.setEnabled(true);
        DrawableButtonExtensionsKt.hideProgress(btnRegister, R.string.authentication_register);
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

    private boolean validateBirthday(String birthday) {
        if (birthday.equals("yyyy-MM-dd")) {
            return false;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate dob = LocalDate.parse(birthday);
            LocalDate curDate = LocalDate.now();
            Period period = Period.between(dob, curDate);
            if (period.getYears() < 14) {
                return false;
            }
        } else {
            String[] s = birthday.split("-");
            Calendar birth = Calendar.getInstance();
            Calendar today = Calendar.getInstance();
            birth.set(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]));
            int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            if (age < 14) {
                return false;
            }
        }
        return true;
    }
}