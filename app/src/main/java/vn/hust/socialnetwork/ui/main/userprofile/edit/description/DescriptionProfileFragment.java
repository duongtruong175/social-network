package vn.hust.socialnetwork.ui.main.userprofile.edit.description;

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
import com.google.android.material.textfield.TextInputLayout;

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
import vn.hust.socialnetwork.utils.ContextExtension;

public class DescriptionProfileFragment extends Fragment {

    private User user;
    private UserProfileViewModel userProfileViewModel;

    private UserProfileService userProfileService;

    private AppCompatImageView ivToolbarBack;
    private AppCompatTextView tvToolbarTitle, tvToolbarConfirm;
    private LinearProgressIndicator pbLoading;
    private TextInputLayout lEdit;
    private TextInputEditText etDescription;

    public DescriptionProfileFragment() {
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
        View view = inflater.inflate(R.layout.fragment_description_profile, container, false);

        // api
        userProfileService = ApiClient.getClient().create(UserProfileService.class);

        // binding
        ivToolbarBack = view.findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = view.findViewById(R.id.tv_toolbar_title);
        tvToolbarConfirm = view.findViewById(R.id.tv_toolbar_confirm);
        pbLoading = view.findViewById(R.id.pb_loading);
        lEdit = view.findViewById(R.id.l_edit);
        etDescription = view.findViewById(R.id.et_description);

        // init view
        tvToolbarTitle.setText(R.string.toolbar_title_edit_description);
        tvToolbarConfirm.setText(R.string.toolbar_text_right_edit_confirm);

        // get value from view model
        userProfileViewModel = new ViewModelProvider(requireActivity()).get(UserProfileViewModel.class);
        user = userProfileViewModel.getUser().getValue();
        if (user != null && user.getShortDescription() != null) {
            etDescription.setText(user.getShortDescription());
        }

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContextExtension.hideKeyboard(getActivity());
                NavController navController = NavHostFragment.findNavController(DescriptionProfileFragment.this);
                navController.navigateUp();
            }
        });

        lEdit.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etDescription.setText("");
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
        ContextExtension.hideKeyboard(etDescription);
        String description = etDescription.getText().toString().trim();
        pbLoading.setVisibility(View.VISIBLE);
        // call api update user
        Map<String, Object> req = new HashMap<>();
        req.put("short_description", description);
        Call<BaseResponse<User>> call = userProfileService.updateProfile(user.getId(), req);
        call.enqueue(new Callback<BaseResponse<User>>() {
            @Override
            public void onResponse(Call<BaseResponse<User>> call, Response<BaseResponse<User>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), R.string.update_desciption_success, Toast.LENGTH_SHORT).show();
                    // update user
                    if (description.isEmpty()) {
                        user.setShortDescription(null);
                    } else {
                        user.setShortDescription(description);
                    }
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