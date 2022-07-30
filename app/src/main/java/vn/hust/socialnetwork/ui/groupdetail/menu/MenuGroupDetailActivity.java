package vn.hust.socialnetwork.ui.groupdetail.menu;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.orhanobut.hawk.Hawk;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.group.Group;
import vn.hust.socialnetwork.models.group.MemberGroup;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.GroupService;
import vn.hust.socialnetwork.ui.groupdetail.GroupDetailActivity;
import vn.hust.socialnetwork.ui.groupdetail.changeinformation.ChangeInformationGroupActivity;
import vn.hust.socialnetwork.ui.groupdetail.requestjoin.RequestJoinGroupActivity;
import vn.hust.socialnetwork.ui.groupdetail.viewmember.ViewMemberActivity;
import vn.hust.socialnetwork.ui.postdetail.PostDetailActivity;
import vn.hust.socialnetwork.ui.report.ReportActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.StringExtension;

public class MenuGroupDetailActivity extends AppCompatActivity {

    private GroupService groupService;

    private int memberGroupRequestId;
    private Group group;

    private AppCompatImageView ivToolbarBack, ivGroupCover;
    private AppCompatTextView tvToolbarTitle, tvGroupName, tvGroupMemberCount, tvLinkGroup, tvTypeGroup;
    private ConstraintLayout lRequestJoinGroup, lMemberGroup, lBlockMemberGroup, lChangeInformationGroup, lLinkGroup, lChangeTypeGroup, lReportGroup, lLeaveGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_group_detail);

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            group = extras.getParcelable("group");
        }

        // api
        groupService = ApiClient.getClient().create(GroupService.class);

        // binding
        ivToolbarBack = findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        ivGroupCover = findViewById(R.id.iv_group_cover);
        tvGroupName = findViewById(R.id.tv_group_name);
        tvGroupMemberCount = findViewById(R.id.tv_group_member_count);
        tvLinkGroup = findViewById(R.id.tv_link_group);
        tvTypeGroup = findViewById(R.id.tv_type_group);
        lRequestJoinGroup = findViewById(R.id.l_request_join_group);
        lMemberGroup = findViewById(R.id.l_member_group);
        lBlockMemberGroup = findViewById(R.id.l_block_member_group);
        lChangeInformationGroup = findViewById(R.id.l_change_information_group);
        lLinkGroup = findViewById(R.id.l_link_group);
        lChangeTypeGroup = findViewById(R.id.l_change_type_group);
        lReportGroup = findViewById(R.id.l_report_group);
        lLeaveGroup = findViewById(R.id.l_leave_group);

        // init data
        if (group != null) {
            Glide.with(MenuGroupDetailActivity.this)
                    .asBitmap()
                    .load(group.getCoverImage())
                    .error(R.drawable.default_group_cover)
                    .into(ivGroupCover);
            tvGroupName.setText(group.getName());
            tvGroupMemberCount.setText(StringExtension.formatMemberGroupCount(group.getCounts().getMemberCount()) + " thành viên");
            tvLinkGroup.setText("group/" + group.getId());
            if (group.getType() == 1) {
                tvTypeGroup.setText(R.string.group_type_public);
            } else {
                tvTypeGroup.setText(R.string.group_type_private);
            }
        }
        tvToolbarTitle.setText(R.string.menu);

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("updated_group", group);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        lRequestJoinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open activity request join group
                Intent intent = new Intent(MenuGroupDetailActivity.this, RequestJoinGroupActivity.class);
                intent.putExtra("group_id", group.getId());
                startActivity(intent);
            }
        });

        lMemberGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuGroupDetailActivity.this, ViewMemberActivity.class);
                intent.putExtra("admin", group.getAdmin());
                intent.putExtra("group_id", group.getId());
                startActivity(intent);
            }
        });

        lBlockMemberGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open activity block member group
            }
        });

        lChangeInformationGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open activity change information group
                Intent intent = new Intent(MenuGroupDetailActivity.this, ChangeInformationGroupActivity.class);
                intent.putExtra("group", group);
                openUpdateInformationActivityResultLauncher.launch(intent);
            }
        });

        lChangeTypeGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeTypeGroupBottomSheetDialog();
            }
        });

        lReportGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open activity report group
                Intent intent = new Intent(MenuGroupDetailActivity.this, ReportActivity.class);
                intent.putExtra("type", 3);
                intent.putExtra("url", "group/" + group.getId());
                startActivity(intent);
            }
        });

        lLeaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveGroup();
            }
        });

        handleMenu();

        // call api to get all data
        getMemberGroupRequest();
    }

    // ActivityResultLauncher for open update information group
    ActivityResultLauncher<Intent> openUpdateInformationActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Get data from result
                        Intent data = result.getData();
                        if (data != null) {
                            group = data.getParcelableExtra("updated_group");
                            tvGroupName.setText(group.getName());
                        }
                    }
                }
            });

    private void getMemberGroupRequest() {
        int userId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0);
        Call<BaseResponse<MemberGroup>> call = groupService.getMemberGroupRequest(group.getId(), userId);
        call.enqueue(new Callback<BaseResponse<MemberGroup>>() {
            @Override
            public void onResponse(Call<BaseResponse<MemberGroup>> call, Response<BaseResponse<MemberGroup>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<MemberGroup> res = response.body();
                    memberGroupRequestId = res.getData().getId();
                } else {
                    memberGroupRequestId = -1;
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<MemberGroup>> call, Throwable t) {
                call.cancel();
                memberGroupRequestId = -1;
            }
        });
    }

    private void handleMenu() {
        int adminId = group.getAdmin().getId();
        int userId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0);
        if (userId != adminId) {
            lRequestJoinGroup.setVisibility(View.GONE);
            lBlockMemberGroup.setVisibility(View.GONE);
            lChangeInformationGroup.setVisibility(View.GONE);
            lLinkGroup.setClickable(false);
            lChangeTypeGroup.setClickable(false);
        } else {
            lReportGroup.setVisibility(View.GONE);
        }
    }

    private void leaveGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MenuGroupDetailActivity.this, R.style.AlertDialogTheme);
        builder.setTitle(R.string.leave_group);
        builder.setMessage(getString(R.string.do_you_realy_want_to_leave_group));
        builder.setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                sendRequestLeaveGroup();
            }
        });
        builder.setNegativeButton("Đóng", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void sendRequestLeaveGroup() {
        int adminId = group.getAdmin().getId();
        int userId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0);
        if (userId != adminId) {
            Call<BaseResponse<String>> call = groupService.deleteMemberGroupRequest(memberGroupRequestId);
            call.enqueue(new Callback<BaseResponse<String>>() {
                @Override
                public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                    if (response.isSuccessful()) {
                        memberGroupRequestId = -1;
                        group.setCurrentUserStatus("guest");
                        int temp = group.getCounts().getMemberCount() - 1;
                        group.getCounts().setMemberCount(temp);
                        Toast.makeText(MenuGroupDetailActivity.this, R.string.leave_group_success, Toast.LENGTH_SHORT).show();

                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("updated_group", group);
                        returnIntent.putExtra("member_group_request_id", memberGroupRequestId);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    } else {
                        Toast.makeText(MenuGroupDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                    // error network (no internet connection, socket timeout, unknown host, ...)
                    // error serializing/deserializing the data
                    call.cancel();
                    Toast.makeText(MenuGroupDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(MenuGroupDetailActivity.this, R.string.error_leave_group, Toast.LENGTH_SHORT).show();
        }
    }

    private void showChangeTypeGroupBottomSheetDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MenuGroupDetailActivity.this, R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_type_group_change);

        RadioGroup rgTypeGroup = bottomSheetDialog.findViewById(R.id.rg_type_group);
        AppCompatRadioButton rbTypeGroupPublic = bottomSheetDialog.findViewById(R.id.rb_type_group_public);
        AppCompatRadioButton rbTypeGroupPrivate = bottomSheetDialog.findViewById(R.id.rb_type_group_private);
        AppCompatTextView tvConfirm = bottomSheetDialog.findViewById(R.id.tv_confirm);

        if (rgTypeGroup != null && rbTypeGroupPublic != null && rbTypeGroupPrivate != null && tvConfirm != null) {
            // init data
            if (group.getType() == 1) {
                rbTypeGroupPublic.setChecked(true);
            } else {
                rbTypeGroupPrivate.setChecked(true);
            }

            tvConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    int type;
                    int selectedId = rgTypeGroup.getCheckedRadioButtonId();
                    if (selectedId == R.id.rb_type_group_public) {
                        type = 1;
                    } else {
                        type = 2;
                    }
                    if (type != group.getType()) {
                        sendRequestChangeTypeGroup(type);
                    }
                }
            });
        }

        bottomSheetDialog.show();
    }

    private void sendRequestChangeTypeGroup(int type) {
        Call<BaseResponse<Group>> call = groupService.updateTypeGroup(group.getId(), type);
        call.enqueue(new Callback<BaseResponse<Group>>() {
            @Override
            public void onResponse(Call<BaseResponse<Group>> call, Response<BaseResponse<Group>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<Group> res = response.body();
                    group.setType(res.getData().getType());
                    // update view
                    if (group.getType() == 1) {
                        tvTypeGroup.setText(R.string.group_type_public);
                    } else {
                        tvTypeGroup.setText(R.string.group_type_private);
                    }
                } else {
                    Toast.makeText(MenuGroupDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Group>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                Toast.makeText(MenuGroupDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }
}