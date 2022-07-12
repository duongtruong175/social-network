package vn.hust.socialnetwork.ui.main.userprofile.edit.gender;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

public class GenderProfileFragment extends Fragment {

    private User user;
    private UserProfileViewModel userProfileViewModel;

    private UserProfileService userProfileService;

    List<String> genders;

    private AppCompatImageView ivToolbarBack;
    private AppCompatTextView tvToolbarTitle, tvToolbarConfirm;
    private LinearProgressIndicator pbLoading;
    private AppCompatAutoCompleteTextView etGender;

    public GenderProfileFragment() {
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
        View view = inflater.inflate(R.layout.fragment_gender_profile, container, false);

        // api
        userProfileService = ApiClient.getClient().create(UserProfileService.class);

        // binding
        ivToolbarBack = view.findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = view.findViewById(R.id.tv_toolbar_title);
        tvToolbarConfirm = view.findViewById(R.id.tv_toolbar_confirm);
        pbLoading = view.findViewById(R.id.pb_loading);
        etGender = view.findViewById(R.id.et_gender);

        // init view
        tvToolbarTitle.setText(R.string.toolbar_title_edit_gender);
        tvToolbarConfirm.setText(R.string.toolbar_text_right_edit_confirm);

        genders = new ArrayList<>();
        genders.addAll(Arrays.asList(getResources().getStringArray(R.array.gender)));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.item_simple_spinner, genders);
        etGender.setAdapter(adapter);
        etGender.setInputType(InputType.TYPE_NULL);
        etGender.setDropDownHeight(getResources().getDimensionPixelOffset(R.dimen.drop_down_height));
        etGender.setDropDownVerticalOffset(getResources().getDimensionPixelOffset(R.dimen.drop_down_vertical_offset));

        // get value from view model
        userProfileViewModel = new ViewModelProvider(requireActivity()).get(UserProfileViewModel.class);
        user = userProfileViewModel.getUser().getValue();
        if (user != null) {
            etGender.setText(user.getGender(), false);
        }

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = NavHostFragment.findNavController(GenderProfileFragment.this);
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
        String gender = etGender.getText().toString().trim();
        if (!gender.equals(user.getGender())) {
            pbLoading.setVisibility(View.VISIBLE);
            // call api update user
            Map<String, Object> req = new HashMap<>();
            req.put("gender", gender);
            Call<BaseResponse<User>> call = userProfileService.updateProfile(user.getId(), req);
            call.enqueue(new Callback<BaseResponse<User>>() {
                @Override
                public void onResponse(Call<BaseResponse<User>> call, Response<BaseResponse<User>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), R.string.update_gender_success, Toast.LENGTH_SHORT).show();
                        // update user
                        user.setGender(gender);
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
}