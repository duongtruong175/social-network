package vn.hust.socialnetwork.ui.main.userprofile.edit.relationship;

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
import vn.hust.socialnetwork.ui.main.userprofile.edit.gender.GenderProfileFragment;

public class RelationshipProfileFragment extends Fragment {

    private User user;
    private UserProfileViewModel userProfileViewModel;

    private UserProfileService userProfileService;

    List<String> relationships;

    private AppCompatImageView ivToolbarBack;
    private AppCompatTextView tvToolbarTitle, tvToolbarConfirm;
    private LinearProgressIndicator pbLoading;
    private AppCompatAutoCompleteTextView etRelationship;

    public RelationshipProfileFragment() {
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
        View view = inflater.inflate(R.layout.fragment_relationship_profile, container, false);

        // api
        userProfileService = ApiClient.getClient().create(UserProfileService.class);

        // binding
        ivToolbarBack = view.findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = view.findViewById(R.id.tv_toolbar_title);
        tvToolbarConfirm = view.findViewById(R.id.tv_toolbar_confirm);
        pbLoading = view.findViewById(R.id.pb_loading);
        etRelationship = view.findViewById(R.id.et_relationship);

        // init view
        tvToolbarTitle.setText(R.string.toolbar_title_edit_relationship);
        tvToolbarConfirm.setText(R.string.toolbar_text_right_edit_confirm);

        relationships = new ArrayList<>();
        relationships.addAll(Arrays.asList(getResources().getStringArray(R.array.relationship)));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.item_simple_spinner, relationships);
        etRelationship.setAdapter(adapter);
        etRelationship.setInputType(InputType.TYPE_NULL);
        etRelationship.setDropDownHeight(getResources().getDimensionPixelOffset(R.dimen.drop_down_height));
        etRelationship.setDropDownVerticalOffset(getResources().getDimensionPixelOffset(R.dimen.drop_down_vertical_offset));

        // get value from view model
        userProfileViewModel = new ViewModelProvider(requireActivity()).get(UserProfileViewModel.class);
        user = userProfileViewModel.getUser().getValue();
        if (user != null && user.getRelationshipStatus() != null) {
            etRelationship.setText(user.getRelationshipStatus(), false);
        }

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = NavHostFragment.findNavController(RelationshipProfileFragment.this);
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
        String relationship = etRelationship.getText().toString().trim();
        pbLoading.setVisibility(View.VISIBLE);
        // call api update user
        Map<String, Object> req = new HashMap<>();
        req.put("relationship_status", relationship);
        Call<BaseResponse<User>> call = userProfileService.updateProfile(user.getId(), req);
        call.enqueue(new Callback<BaseResponse<User>>() {
            @Override
            public void onResponse(Call<BaseResponse<User>> call, Response<BaseResponse<User>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), R.string.update_relationship_success, Toast.LENGTH_SHORT).show();
                    // update user
                    if (relationship.isEmpty()) {
                        user.setRelationshipStatus(null);
                    } else {
                        user.setRelationshipStatus(relationship);
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