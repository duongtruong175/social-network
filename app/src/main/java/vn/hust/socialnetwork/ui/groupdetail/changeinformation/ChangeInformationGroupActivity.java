package vn.hust.socialnetwork.ui.groupdetail.changeinformation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.group.Group;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.GroupService;
import vn.hust.socialnetwork.utils.ContextExtension;

public class ChangeInformationGroupActivity extends AppCompatActivity {

    private GroupService groupService;

    private TextInputEditText etNameGroup, etIntroductionGroup;
    private AppCompatButton btnUpdateGroup;
    private AppCompatTextView tvToolbarTitle,tvErrorNameGroup;
    private AppCompatImageView ivToolbarBack;
    private Dialog progressDialog;

    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_information_group);
        // api
        groupService = ApiClient.getClient().create(GroupService.class);

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            group = extras.getParcelable("group");
        } else {
            group = null;
        }

        // binding
        ivToolbarBack = findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        etNameGroup = findViewById(R.id.et_name_group);
        etIntroductionGroup = findViewById(R.id.et_introduction_group);
        btnUpdateGroup = findViewById(R.id.btn_update_group);
        tvErrorNameGroup = findViewById(R.id.tv_error_name_group);

        // init
        progressDialog = ContextExtension.createProgressDialog(ChangeInformationGroupActivity.this);
        tvToolbarTitle.setText(R.string.toolbar_title_update_information_group);
        if (group != null) {
            etNameGroup.setText(group.getName());
            if (group.getIntroduction() != null) {
                etIntroductionGroup.setText(group.getIntroduction());
            }
        }

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnUpdateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateInformationGroup();
            }
        });
    }

    private void updateInformationGroup() {
        ContextExtension.hideKeyboard(ChangeInformationGroupActivity.this);
        tvErrorNameGroup.setVisibility(View.GONE);
        tvErrorNameGroup.setText("");

        String name = etNameGroup.getText().toString().trim();
        String introduction = etIntroductionGroup.getText().toString().trim();
        if (name.isEmpty()) {
            tvErrorNameGroup.setVisibility(View.VISIBLE);
            tvErrorNameGroup.setText(R.string.error_name_group_validation);
            ContextExtension.showKeyboard(etNameGroup);
        } else {
            Map<String, Object> req = new HashMap<>();
            req.put("name", name);
            if (!introduction.isEmpty()) {
                req.put("introduction", introduction);
            }
            progressDialog.show();
            // call api
            Call<BaseResponse<Group>> call = groupService.updateGroup(group.getId(), req);
            call.enqueue(new Callback<BaseResponse<Group>>() {
                @Override
                public void onResponse(Call<BaseResponse<Group>> call, Response<BaseResponse<Group>> response) {
                    if (response.isSuccessful()) {
                        BaseResponse<Group> res = response.body();
                        Group newGroup = res.getData();
                        group.setName(newGroup.getName());
                        group.setIntroduction(newGroup.getIntroduction());

                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("updated_group", group);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(ChangeInformationGroupActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse<Group>> call, Throwable t) {
                    // error network (no internet connection, socket timeout, unknown host, ...)
                    // error serializing/deserializing the data
                    call.cancel();
                    progressDialog.dismiss();
                    Toast.makeText(ChangeInformationGroupActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}