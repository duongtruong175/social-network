package vn.hust.socialnetwork.ui.mygroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.group.Group;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.GroupService;
import vn.hust.socialnetwork.ui.groupdetail.GroupDetailActivity;
import vn.hust.socialnetwork.ui.mygroup.adapters.GroupAdapter;
import vn.hust.socialnetwork.ui.mygroup.adapters.OnGroupListener;
import vn.hust.socialnetwork.utils.AppSharedPreferences;

public class MyGroupActivity extends AppCompatActivity {

    private GroupService groupService;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout lMain, lError;
    private ShimmerFrameLayout lShimmer;
    private AppCompatImageView ivToolbarBack;
    private AppCompatTextView tvToolbarTitle;
    private RecyclerView rvMyGroupAdmin, rvMyGroupJoin;

    private List<Group> listGroupAdmin, listGroupJoin;
    private GroupAdapter groupAdminAdapter, groupJoinAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_group);

        // api
        groupService = ApiClient.getClient().create(GroupService.class);

        // binding
        lMain = findViewById(R.id.l_main);
        lShimmer = findViewById(R.id.l_shimmer);
        lError = findViewById(R.id.l_error);
        swipeRefreshLayout = findViewById(R.id.l_swipe_refresh);
        ivToolbarBack = findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        rvMyGroupAdmin = findViewById(R.id.rv_my_group_admin);
        rvMyGroupJoin = findViewById(R.id.rv_my_group_join);

        // init
        tvToolbarTitle.setText(R.string.my_group);

        handleRefreshView();

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        listGroupAdmin = new ArrayList<>();
        groupAdminAdapter = new GroupAdapter(MyGroupActivity.this, listGroupAdmin, new OnGroupListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(MyGroupActivity.this, GroupDetailActivity.class);
                intent.putExtra("group_id", listGroupAdmin.get(position).getId());
                startActivity(intent);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(MyGroupActivity.this);
        rvMyGroupAdmin.setLayoutManager(layoutManager);
        rvMyGroupAdmin.setAdapter(groupAdminAdapter);

        listGroupJoin = new ArrayList<>();
        groupJoinAdapter = new GroupAdapter(MyGroupActivity.this, listGroupJoin, new OnGroupListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(MyGroupActivity.this, GroupDetailActivity.class);
                intent.putExtra("group_id", listGroupJoin.get(position).getId());
                startActivity(intent);
            }
        });
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(MyGroupActivity.this);
        rvMyGroupJoin.setLayoutManager(layoutManager2);
        rvMyGroupJoin.setAdapter(groupJoinAdapter);

        // call api to get all data
        getData();
    }

    private void handleRefreshView() {
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    private void getData() {
        lMain.setVisibility(View.GONE);
        lError.setVisibility(View.GONE);
        lShimmer.setVisibility(View.VISIBLE);
        lShimmer.startShimmer();
        // call api
        getGroups();
    }

    private void refresh() {
        // call api
        getGroups();
    }

    private void getGroups() {
        Call<BaseResponse<List<Group>>> call = groupService.getMyGroups();
        call.enqueue(new Callback<BaseResponse<List<Group>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Group>>> call, Response<BaseResponse<List<Group>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<Group>> res = response.body();
                    List<Group> groups = res.getData();
                    listGroupAdmin.clear();
                    listGroupJoin.clear();
                    int userId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0);
                    for (Group group : groups) {
                        if (group.getAdmin().getId() == userId) {
                            listGroupAdmin.add(group);
                        } else {
                            listGroupJoin.add(group);
                        }
                    }
                    groupAdminAdapter.notifyDataSetChanged();
                    groupJoinAdapter.notifyDataSetChanged();
                    lMain.setVisibility(View.VISIBLE);
                    lError.setVisibility(View.GONE);
                } else {
                    lMain.setVisibility(View.GONE);
                    lError.setVisibility(View.VISIBLE);
                }
                lShimmer.stopShimmer();
                lShimmer.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Group>>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                lMain.setVisibility(View.GONE);
                lShimmer.stopShimmer();
                lShimmer.setVisibility(View.GONE);
                lError.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}