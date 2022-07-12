package vn.hust.socialnetwork.ui.main.userprofile.edit.phone;

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
import android.widget.Toast;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.user.User;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.UserProfileService;
import vn.hust.socialnetwork.ui.main.userprofile.edit.UserProfileViewModel;
import vn.hust.socialnetwork.utils.ContextExtension;
import vn.hust.socialnetwork.utils.StringExtension;

public class PhoneProfileFragment extends Fragment {

    private User user;
    private UserProfileViewModel userProfileViewModel;

    private UserProfileService userProfileService;

    private AppCompatImageView ivToolbarBack;
    private AppCompatTextView tvToolbarTitle, tvToolbarConfirm, tvErrorPhone;
    private LinearProgressIndicator pbLoading;
    private TextInputEditText etPhone;

    public PhoneProfileFragment() {
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
        View view = inflater.inflate(R.layout.fragment_phone_profile, container, false);

        // api
        userProfileService = ApiClient.getClient().create(UserProfileService.class);

        // binding
        ivToolbarBack = view.findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = view.findViewById(R.id.tv_toolbar_title);
        tvToolbarConfirm = view.findViewById(R.id.tv_toolbar_confirm);
        pbLoading = view.findViewById(R.id.pb_loading);
        etPhone = view.findViewById(R.id.et_phone);
        tvErrorPhone = view.findViewById(R.id.tv_error_phone);

        // init view
        tvToolbarTitle.setText(R.string.toolbar_title_edit_phone);
        tvToolbarConfirm.setText(R.string.toolbar_text_right_edit_confirm);

        // get value from view model
        userProfileViewModel = new ViewModelProvider(requireActivity()).get(UserProfileViewModel.class);
        user = userProfileViewModel.getUser().getValue();
        if (user != null && StringExtension.checkValidValueString(user.getPhone())) {
            etPhone.setText(user.getPhone());
        }

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContextExtension.hideKeyboard(getActivity());
                NavController navController = NavHostFragment.findNavController(PhoneProfileFragment.this);
                navController.navigateUp();
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

    private void updateUser() {
        ContextExtension.hideKeyboard(etPhone);
        tvErrorPhone.setVisibility(View.GONE);
        tvErrorPhone.setText("");
        String phone = etPhone.getText().toString().trim();
        if (phone.length() != 10 || !validatePhone(phone)) {
            tvErrorPhone.setVisibility(View.VISIBLE);
            tvErrorPhone.setText(R.string.error_phone);
            ContextExtension.showKeyboard(etPhone);
        } else {
            pbLoading.setVisibility(View.VISIBLE);
            // call api update user
            Map<String, Object> req = new HashMap<>();
            req.put("phone", phone);
            Call<BaseResponse<User>> call = userProfileService.updateProfile(user.getId(), req);
            call.enqueue(new Callback<BaseResponse<User>>() {
                @Override
                public void onResponse(Call<BaseResponse<User>> call, Response<BaseResponse<User>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), R.string.update_phone_success, Toast.LENGTH_SHORT).show();
                        // update user
                        user.setPhone(phone);
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

    private boolean validatePhone(String phone) {
        String phoneRegex = "^(0)[0-9]{9}$";

        Pattern pattern = Pattern.compile(phoneRegex);
        if (phone == null)
            return false;
        return pattern.matcher(phone).matches();
    }
}