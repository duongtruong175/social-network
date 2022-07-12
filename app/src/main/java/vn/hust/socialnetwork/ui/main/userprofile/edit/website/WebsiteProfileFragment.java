package vn.hust.socialnetwork.ui.main.userprofile.edit.website;

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
import vn.hust.socialnetwork.ui.main.userprofile.edit.name.NameProfileFragment;
import vn.hust.socialnetwork.utils.ContextExtension;

public class WebsiteProfileFragment extends Fragment {

    private User user;
    private UserProfileViewModel userProfileViewModel;

    private UserProfileService userProfileService;

    private AppCompatImageView ivToolbarBack;
    private AppCompatTextView tvToolbarTitle, tvToolbarConfirm;
    private LinearProgressIndicator pbLoading;
    private TextInputLayout lEdit;
    private TextInputEditText etWebsite;

    public WebsiteProfileFragment() {
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
        View view = inflater.inflate(R.layout.fragment_website_profile, container, false);

        // api
        userProfileService = ApiClient.getClient().create(UserProfileService.class);

        // binding
        ivToolbarBack = view.findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = view.findViewById(R.id.tv_toolbar_title);
        tvToolbarConfirm = view.findViewById(R.id.tv_toolbar_confirm);
        pbLoading = view.findViewById(R.id.pb_loading);
        lEdit = view.findViewById(R.id.l_edit);
        etWebsite = view.findViewById(R.id.et_website);

        // init view
        tvToolbarTitle.setText(R.string.toolbar_title_edit_website);
        tvToolbarConfirm.setText(R.string.toolbar_text_right_edit_confirm);

        // get value from view model
        userProfileViewModel = new ViewModelProvider(requireActivity()).get(UserProfileViewModel.class);
        user = userProfileViewModel.getUser().getValue();
        if (user != null && user.getWebsite() != null) {
            etWebsite.setText(user.getWebsite());
        }

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContextExtension.hideKeyboard(getActivity());
                NavController navController = NavHostFragment.findNavController(WebsiteProfileFragment.this);
                navController.navigateUp();
            }
        });

        lEdit.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etWebsite.setText("");
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
        ContextExtension.hideKeyboard(etWebsite);
        String website = etWebsite.getText().toString().trim();
        pbLoading.setVisibility(View.VISIBLE);
        // call api update user
        Map<String, Object> req = new HashMap<>();
        req.put("website", website);
        Call<BaseResponse<User>> call = userProfileService.updateProfile(user.getId(), req);
        call.enqueue(new Callback<BaseResponse<User>>() {
            @Override
            public void onResponse(Call<BaseResponse<User>> call, Response<BaseResponse<User>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), R.string.update_website_success, Toast.LENGTH_SHORT).show();
                    // update user
                    if (website.isEmpty()) {
                        user.setWebsite(null);
                    } else {
                        user.setWebsite(website);
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