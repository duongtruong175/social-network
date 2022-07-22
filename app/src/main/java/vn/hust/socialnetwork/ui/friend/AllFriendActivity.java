package vn.hust.socialnetwork.ui.friend;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import vn.hust.socialnetwork.models.user.Relation;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.UserProfileService;
import vn.hust.socialnetwork.ui.friend.adapters.FriendAdapter;
import vn.hust.socialnetwork.ui.friend.adapters.OnFriendListener;
import vn.hust.socialnetwork.ui.userdetail.UserDetailActivity;

public class AllFriendActivity extends AppCompatActivity {

    private UserProfileService userProfileService;

    private AppCompatImageView ivToolbarBack;
    private LinearProgressIndicator pbLoading;
    private SearchView svSearch;
    private SwipeRefreshLayout lSwipeRefresh;
    private AppCompatTextView tvToolbarTitle, tvLabel, tvNoData;
    private RecyclerView rvFriend;

    private FriendAdapter friendAdapter;
    private List<Relation> friends;
    private int userId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_friend);

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getInt("user_id");
            userName = extras.getString("user_name");
        } else {
            userId = 0;
            userName = "";
        }

        // api
        userProfileService = ApiClient.getClient().create(UserProfileService.class);

        // binding
        ivToolbarBack = findViewById(R.id.iv_toolbar_back);
        pbLoading = findViewById(R.id.pb_loading);
        svSearch = findViewById(R.id.sv_search);
        lSwipeRefresh = findViewById(R.id.l_swipe_refresh);
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        tvLabel = findViewById(R.id.tv_label);
        tvNoData = findViewById(R.id.tv_no_data);
        rvFriend = findViewById(R.id.rv_friend);

        friends = new ArrayList<>();
        friendAdapter = new FriendAdapter(AllFriendActivity.this, friends, new OnFriendListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(AllFriendActivity.this, UserDetailActivity.class);
                intent.putExtra("user_id", friends.get(position).getId());
                startActivity(intent);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(AllFriendActivity.this);
        rvFriend.setLayoutManager(layoutManager);
        rvFriend.setAdapter(friendAdapter);

        // init data
        tvToolbarTitle.setText(getString(R.string.toolbar_title_friend_list) + " " + userName);
        tvLabel.setText(friends.size() + " người bạn");

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
                friendAdapter.getFilter().filter(newText);
                return false;
            }
        });

        // get data
        getData();
    }

    private void getData() {
        pbLoading.setVisibility(View.VISIBLE);
        Call<BaseResponse<List<Relation>>> call = userProfileService.getAllFriends(userId);
        call.enqueue(new Callback<BaseResponse<List<Relation>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Relation>>> call, Response<BaseResponse<List<Relation>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<Relation>> res = response.body();
                    friends.clear();
                    friends.addAll(res.getData());
                    friendAdapter.notifyDataSetChanged();
                    if (friends.size() == 0) {
                        tvNoData.setVisibility(View.VISIBLE);
                    } else {
                        tvNoData.setVisibility(View.GONE);
                    }
                    tvLabel.setText(friends.size() + " người bạn");
                } else {
                    Toast.makeText(AllFriendActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
                pbLoading.setVisibility(View.GONE);
                lSwipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Relation>>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(AllFriendActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                lSwipeRefresh.setRefreshing(false);
            }
        });
    }
}