package vn.hust.socialnetwork.ui.main.userprofile.edit.education;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.user.Education;
import vn.hust.socialnetwork.models.user.Job;
import vn.hust.socialnetwork.models.user.User;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.UserProfileService;
import vn.hust.socialnetwork.ui.main.userprofile.edit.UserProfileViewModel;
import vn.hust.socialnetwork.ui.main.userprofile.edit.job.JobProfileFragment;
import vn.hust.socialnetwork.utils.ContextExtension;

public class EducationProfileFragment extends Fragment {

    private User user;
    private UserProfileViewModel userProfileViewModel;

    private UserProfileService userProfileService;

    private AppCompatImageView ivToolbarBack;
    private AppCompatTextView tvToolbarTitle, tvToolbarConfirm;
    private LinearProgressIndicator pbLoading;
    private TextInputEditText etSchool, etMajors;

    public EducationProfileFragment() {
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
        View view = inflater.inflate(R.layout.fragment_education_profile, container, false);

        // api
        userProfileService = ApiClient.getClient().create(UserProfileService.class);

        // binding
        ivToolbarBack = view.findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = view.findViewById(R.id.tv_toolbar_title);
        tvToolbarConfirm = view.findViewById(R.id.tv_toolbar_confirm);
        pbLoading = view.findViewById(R.id.pb_loading);
        etSchool = view.findViewById(R.id.et_school);
        etMajors = view.findViewById(R.id.et_majors);

        // init view
        tvToolbarTitle.setText(R.string.toolbar_title_edit_education);
        tvToolbarConfirm.setText(R.string.toolbar_text_right_edit_confirm);

        // get value from view model
        userProfileViewModel = new ViewModelProvider(requireActivity()).get(UserProfileViewModel.class);
        user = userProfileViewModel.getUser().getValue();
        if (user != null && user.getEducation() != null) {
            etSchool.setText(user.getEducation().getSchool());
            etMajors.setText(user.getEducation().getMajors());
        }

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContextExtension.hideKeyboard(getActivity());
                NavController navController = NavHostFragment.findNavController(EducationProfileFragment.this);
                navController.navigateUp();
            }
        });

        tvToolbarConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    updateUser();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void updateUser() throws JSONException {
        ContextExtension.hideKeyboard(getActivity());
        String school = etSchool.getText().toString().trim();
        String majors = etMajors.getText().toString().trim();
        if (school.isEmpty() && !majors.isEmpty()) {
            ContextExtension.showKeyboard(etSchool);
        } else if (!school.isEmpty() && majors.isEmpty()) {
            ContextExtension.showKeyboard(etMajors);
        } else { // all is null or has value
            pbLoading.setVisibility(View.VISIBLE);
            // call api update user
            Education education = new Education(school, majors);
            JSONObject educationJson = new JSONObject();
            educationJson.put("school", school);
            educationJson.put("majors", majors);
            Map<String, Object> req = new HashMap<>();
            req.put("education", educationJson);
            Call<BaseResponse<User>> call = userProfileService.updateProfile(user.getId(), req);
            call.enqueue(new Callback<BaseResponse<User>>() {
                @Override
                public void onResponse(Call<BaseResponse<User>> call, Response<BaseResponse<User>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), R.string.update_education_success, Toast.LENGTH_SHORT).show();
                        // update user
                        if (school.isEmpty() && majors.isEmpty()) {
                            user.setEducation(null);
                        } else {
                            user.setEducation(education);
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
}