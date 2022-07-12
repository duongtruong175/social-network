package vn.hust.socialnetwork.ui.main.userprofile.edit.job;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.user.Job;
import vn.hust.socialnetwork.models.user.User;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.UserProfileService;
import vn.hust.socialnetwork.ui.main.userprofile.edit.UserProfileViewModel;
import vn.hust.socialnetwork.ui.main.userprofile.edit.name.NameProfileFragment;
import vn.hust.socialnetwork.utils.ContextExtension;

public class JobProfileFragment extends Fragment {

    private User user;
    private UserProfileViewModel userProfileViewModel;

    private UserProfileService userProfileService;

    private AppCompatImageView ivToolbarBack;
    private AppCompatTextView tvToolbarTitle, tvToolbarConfirm;
    private LinearProgressIndicator pbLoading;
    private TextInputEditText etCompany, etPosition;

    public JobProfileFragment() {
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
        View view = inflater.inflate(R.layout.fragment_job_profile, container, false);

        // api
        userProfileService = ApiClient.getClient().create(UserProfileService.class);

        // binding
        ivToolbarBack = view.findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = view.findViewById(R.id.tv_toolbar_title);
        tvToolbarConfirm = view.findViewById(R.id.tv_toolbar_confirm);
        pbLoading = view.findViewById(R.id.pb_loading);
        etCompany = view.findViewById(R.id.et_company);
        etPosition = view.findViewById(R.id.et_position);

        // init view
        tvToolbarTitle.setText(R.string.toolbar_title_edit_job);
        tvToolbarConfirm.setText(R.string.toolbar_text_right_edit_confirm);

        // get value from view model
        userProfileViewModel = new ViewModelProvider(requireActivity()).get(UserProfileViewModel.class);
        user = userProfileViewModel.getUser().getValue();
        if (user != null && user.getJob() != null) {
            etCompany.setText(user.getJob().getCompany());
            etPosition.setText(user.getJob().getPosition());
        }

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContextExtension.hideKeyboard(getActivity());
                NavController navController = NavHostFragment.findNavController(JobProfileFragment.this);
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
        String company = etCompany.getText().toString().trim();
        String position = etPosition.getText().toString().trim();
        if (company.isEmpty() && !position.isEmpty()) {
            ContextExtension.showKeyboard(etCompany);
        } else if (!company.isEmpty() && position.isEmpty()) {
            ContextExtension.showKeyboard(etCompany);
        } else { // all is null or has value
            pbLoading.setVisibility(View.VISIBLE);
            // call api update user
            Job job = new Job(company, position);
            JSONObject jobJson = new JSONObject();
            jobJson.put("company", company);
            jobJson.put("position", position);
            Map<String, Object> req = new HashMap<>();
            req.put("job", jobJson);
            Call<BaseResponse<User>> call = userProfileService.updateProfile(user.getId(), req);
            call.enqueue(new Callback<BaseResponse<User>>() {
                @Override
                public void onResponse(Call<BaseResponse<User>> call, Response<BaseResponse<User>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), R.string.update_job_success, Toast.LENGTH_SHORT).show();
                        // update user
                        if (company.isEmpty() && position.isEmpty()) {
                            user.setJob(null);
                        } else {
                            user.setJob(job);
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