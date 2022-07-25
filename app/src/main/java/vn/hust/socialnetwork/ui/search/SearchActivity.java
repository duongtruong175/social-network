package vn.hust.socialnetwork.ui.search;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.group.Group;
import vn.hust.socialnetwork.models.search.SearchResult;
import vn.hust.socialnetwork.models.user.User;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.SearchService;
import vn.hust.socialnetwork.ui.groupdetail.GroupDetailActivity;
import vn.hust.socialnetwork.ui.search.adapters.GroupAdapter;
import vn.hust.socialnetwork.ui.search.adapters.OnGroupListener;
import vn.hust.socialnetwork.ui.search.adapters.OnUserListener;
import vn.hust.socialnetwork.ui.search.adapters.UserAdapter;
import vn.hust.socialnetwork.ui.userdetail.UserDetailActivity;

public class SearchActivity extends AppCompatActivity {

    private SearchService searchService;

    private AppCompatImageView ivToolbarBack;
    private SearchView svToolbarSearch;
    private LinearProgressIndicator pbLoading;
    private LinearLayoutCompat lListUser, lListGroup, lSearchEmpty;
    private RecyclerView rvUser, rvGroup;

    private String keyword;
    private List<User> users;
    private UserAdapter userAdapter;
    private List<Group> groups;
    private GroupAdapter groupAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // api
        searchService = ApiClient.getClient().create(SearchService.class);

        // binding
        ivToolbarBack = findViewById(R.id.iv_toolbar_back);
        svToolbarSearch = findViewById(R.id.sv_toolbar_search);
        pbLoading = findViewById(R.id.pb_loading);
        lListUser = findViewById(R.id.l_list_user);
        lListGroup = findViewById(R.id.l_list_group);
        lSearchEmpty = findViewById(R.id.l_search_empty);
        rvUser = findViewById(R.id.rv_user);
        rvGroup = findViewById(R.id.rv_group);

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // init data
        keyword = "";
        lListUser.setVisibility(View.GONE);
        lListGroup.setVisibility(View.GONE);

        users = new ArrayList<>();
        userAdapter = new UserAdapter(SearchActivity.this, users, new OnUserListener() {
            @Override
            public void onItemClick(int position) {
                // open user detail
                Intent intent = new Intent(SearchActivity.this, UserDetailActivity.class);
                intent.putExtra("user_id", users.get(position).getId());
                startActivity(intent);
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SearchActivity.this, LinearLayoutManager.HORIZONTAL, false);
        rvUser.setLayoutManager(linearLayoutManager);
        rvUser.setAdapter(userAdapter);

        groups = new ArrayList<>();
        groupAdapter = new GroupAdapter(SearchActivity.this, groups, new OnGroupListener() {
            @Override
            public void onItemClick(int position) {
                // open group detail
                Intent intent = new Intent(SearchActivity.this, GroupDetailActivity.class);
                intent.putExtra("group_id", groups.get(position).getId());
                startActivity(intent);
            }
        });
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(SearchActivity.this, LinearLayoutManager.HORIZONTAL, false);
        rvGroup.setLayoutManager(linearLayoutManager2);
        rvGroup.setAdapter(groupAdapter);

        svToolbarSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String temp = query.trim();
                if (!temp.equals(keyword) && !temp.isEmpty()) {
                    keyword = temp;
                    searchKeyword();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void searchKeyword() {
        pbLoading.setVisibility(View.VISIBLE);
        Call<BaseResponse<SearchResult>> call = searchService.searchAll(keyword);
        call.enqueue(new Callback<BaseResponse<SearchResult>>() {
            @Override
            public void onResponse(Call<BaseResponse<SearchResult>> call, Response<BaseResponse<SearchResult>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<SearchResult> res = response.body();
                    SearchResult result = res.getData();
                    users.clear();
                    users.addAll(result.getUsers());
                    userAdapter.notifyDataSetChanged();
                    groups.clear();
                    groups.addAll(result.getGroups());
                    groupAdapter.notifyDataSetChanged();
                    if (users.size() == 0) {
                        lListUser.setVisibility(View.GONE);
                    } else {
                        lListUser.setVisibility(View.VISIBLE);
                    }
                    if (groups.size() == 0) {
                        lListGroup.setVisibility(View.GONE);
                    } else {
                        lListGroup.setVisibility(View.VISIBLE);
                    }
                    if (users.size() == 0 && groups.size() == 0) {
                        lSearchEmpty.setVisibility(View.VISIBLE);
                    } else {
                        lSearchEmpty.setVisibility(View.GONE);
                    }
                }
                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<BaseResponse<SearchResult>> call, Throwable t) {
                call.cancel();
                pbLoading.setVisibility(View.GONE);
            }
        });
    }
}