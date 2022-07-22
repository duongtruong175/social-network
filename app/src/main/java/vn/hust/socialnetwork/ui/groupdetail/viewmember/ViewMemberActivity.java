package vn.hust.socialnetwork.ui.groupdetail.viewmember;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.group.MemberGroup;
import vn.hust.socialnetwork.models.group.UserGroup;
import vn.hust.socialnetwork.models.user.Relation;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.GroupService;
import vn.hust.socialnetwork.ui.friend.AllFriendActivity;
import vn.hust.socialnetwork.ui.groupdetail.viewmember.adapters.MemberGroupAdapter;
import vn.hust.socialnetwork.ui.groupdetail.viewmember.adapters.OnMemberGroupListener;
import vn.hust.socialnetwork.ui.userdetail.UserDetailActivity;

public class ViewMemberActivity extends AppCompatActivity {

    private GroupService groupService;

    private AppCompatImageView ivToolbarBack;
    private LinearProgressIndicator pbLoading;
    private SearchView svSearch;
    private SwipeRefreshLayout lSwipeRefresh;
    private AppCompatTextView tvToolbarTitle, tvNoData, tvAdminName;
    private ConstraintLayout lAdmin;
    private CircleImageView civAdminAvatar;
    private RecyclerView rvMember;

    private MemberGroupAdapter memberGroupAdapter;
    private List<MemberGroup> memberGroups;
    private int groupId;
    private UserGroup admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_member);

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            groupId = extras.getInt("group_id");
            admin = extras.getParcelable("admin");
        } else {
            groupId = 0;
            admin = null;
        }

        // api
        groupService = ApiClient.getClient().create(GroupService.class);

        // binding
        ivToolbarBack = findViewById(R.id.iv_toolbar_back);
        pbLoading = findViewById(R.id.pb_loading);
        svSearch = findViewById(R.id.sv_search);
        lSwipeRefresh = findViewById(R.id.l_swipe_refresh);
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        lAdmin = findViewById(R.id.l_admin);
        tvAdminName = findViewById(R.id.tv_admin_name);
        civAdminAvatar = findViewById(R.id.civ_admin_avatar);
        rvMember = findViewById(R.id.rv_member);
        tvNoData = findViewById(R.id.tv_no_data);

        memberGroups = new ArrayList<>();
        memberGroupAdapter = new MemberGroupAdapter(ViewMemberActivity.this, memberGroups, new OnMemberGroupListener() {
            @Override
            public void onItemClick(int position) {
                // open user detail activity
                Intent intent = new Intent(ViewMemberActivity.this, UserDetailActivity.class);
                intent.putExtra("user_id", memberGroups.get(position).getUser().getId());
                startActivity(intent);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(ViewMemberActivity.this);
        rvMember.setLayoutManager(layoutManager);
        rvMember.setAdapter(memberGroupAdapter);

        // init data
        tvToolbarTitle.setText(R.string.group_member);

        if (admin != null) {
            Glide.with(ViewMemberActivity.this)
                    .asBitmap()
                    .load(admin.getAvatar())
                    .error(R.drawable.default_avatar)
                    .into(civAdminAvatar);
            tvAdminName.setText(admin.getName());
        }

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        lSwipeRefresh.setColorSchemeResources(R.color.colorAccent);
        lSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });

        svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                memberGroupAdapter.getFilter().filter(newText);
                if (admin!= null) {
                    if (newText.isEmpty() || admin.getName().toLowerCase().contains(newText)) {
                        lAdmin.setVisibility(View.VISIBLE);
                    } else {
                        lAdmin.setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });

        lAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (admin != null) {
                    // open user detail activity
                    Intent intent = new Intent(ViewMemberActivity.this, UserDetailActivity.class);
                    intent.putExtra("user_id", admin.getId());
                    startActivity(intent);
                }
            }
        });

        // call api
        getData();
    }

    private void getData() {
        pbLoading.setVisibility(View.VISIBLE);
        Call<BaseResponse<List<MemberGroup>>> call = groupService.getMemberGroups(groupId);
        call.enqueue(new Callback<BaseResponse<List<MemberGroup>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<MemberGroup>>> call, Response<BaseResponse<List<MemberGroup>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<MemberGroup>> res = response.body();
                    memberGroups.clear();
                    memberGroups.addAll(res.getData());
                    memberGroupAdapter.notifyDataSetChanged();
                    if (memberGroups.size() == 0) {
                        tvNoData.setVisibility(View.VISIBLE);
                    } else {
                        tvNoData.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(ViewMemberActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
                pbLoading.setVisibility(View.GONE);
                lSwipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<BaseResponse<List<MemberGroup>>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(ViewMemberActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                lSwipeRefresh.setRefreshing(false);
            }
        });
    }
}