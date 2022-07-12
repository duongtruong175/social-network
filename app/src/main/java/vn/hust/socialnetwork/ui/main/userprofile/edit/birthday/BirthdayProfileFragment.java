package vn.hust.socialnetwork.ui.main.userprofile.edit.birthday;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.user.User;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.UserProfileService;
import vn.hust.socialnetwork.ui.main.userprofile.edit.UserProfileViewModel;
import vn.hust.socialnetwork.utils.TimeExtension;

public class BirthdayProfileFragment extends Fragment {

    private User user;
    private UserProfileViewModel userProfileViewModel;

    private UserProfileService userProfileService;

    private Calendar c;
    private DatePickerDialog dpd;

    private AppCompatImageView ivToolbarBack;
    private AppCompatTextView tvToolbarTitle, tvToolbarConfirm, tvErrorBirthday;
    private LinearProgressIndicator pbLoading;
    private AppCompatTextView etBirthday;

    public BirthdayProfileFragment() {
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
        View view = inflater.inflate(R.layout.fragment_birthday_profile, container, false);

        // api
        userProfileService = ApiClient.getClient().create(UserProfileService.class);

        // binding
        ivToolbarBack = view.findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = view.findViewById(R.id.tv_toolbar_title);
        tvToolbarConfirm = view.findViewById(R.id.tv_toolbar_confirm);
        pbLoading = view.findViewById(R.id.pb_loading);
        etBirthday = view.findViewById(R.id.et_birthday);
        tvErrorBirthday = view.findViewById(R.id.tv_error_birthday);

        // init view
        tvToolbarTitle.setText(R.string.toolbar_title_edit_birthday);
        tvToolbarConfirm.setText(R.string.toolbar_text_right_edit_confirm);

        // get value from view model
        userProfileViewModel = new ViewModelProvider(requireActivity()).get(UserProfileViewModel.class);
        user = userProfileViewModel.getUser().getValue();
        if (user != null) {
            etBirthday.setText(TimeExtension.formatBirthday(user.getBirthday()));
        };

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = NavHostFragment.findNavController(BirthdayProfileFragment.this);
                navController.navigateUp();
            }
        });

        etBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBirthdayPicker();
            }
        });

        tvToolbarConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUser();
            }
        });

        return view;
    }

    private void openBirthdayPicker() {
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

    private void updateUser() {
        tvErrorBirthday.setVisibility(View.GONE);
        tvErrorBirthday.setText("");
        // convert birthday dd/MM/yyyy to sql date yyyy-MM-dd
        String birthday = etBirthday.getText().toString().trim();
        String[] s = birthday.split("/");
        birthday = s[2] + "-" + s[1] + "-" + s[0];
        if (!validateBirthday(birthday)) {
            tvErrorBirthday.setVisibility(View.VISIBLE);
            tvErrorBirthday.setText(R.string.error_birthday);
        } else if (!birthday.equals(user.getBirthday())){
            pbLoading.setVisibility(View.VISIBLE);
            // call api update user
            Map<String, Object> req = new HashMap<>();
            req.put("birthday", birthday);
            Call<BaseResponse<User>> call = userProfileService.updateProfile(user.getId(), req);
            String finalBirthday = birthday;
            call.enqueue(new Callback<BaseResponse<User>>() {
                @Override
                public void onResponse(Call<BaseResponse<User>> call, Response<BaseResponse<User>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), R.string.update_birthday_success, Toast.LENGTH_SHORT).show();
                        // update user
                        user.setBirthday(finalBirthday);
                        userProfileViewModel.setUser(user);
                    } else {
                        Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                    }
                    pbLoading.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<BaseResponse<User>> call, Throwable t) {
                    // error
                    call.cancel();
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            });
        }
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