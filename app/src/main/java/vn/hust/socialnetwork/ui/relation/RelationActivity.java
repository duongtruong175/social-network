package vn.hust.socialnetwork.ui.relation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.relation.DestinationUser;
import vn.hust.socialnetwork.models.relation.RelationUser;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.RelationService;
import vn.hust.socialnetwork.ui.relation.adapters.OnAddFriendListener;
import vn.hust.socialnetwork.ui.relation.adapters.OnFriendListener;
import vn.hust.socialnetwork.ui.relation.adapters.OnRequestFriendListener;
import vn.hust.socialnetwork.ui.relation.adapters.OnSuggestFriendListener;
import vn.hust.socialnetwork.ui.relation.adapters.RelationAdapter;
import vn.hust.socialnetwork.ui.userdetail.UserDetailActivity;

public class RelationActivity extends AppCompatActivity {

    private static final int ITEM_ADD_FRIEND = 1;
    private static final int ITEM_FRIEND = 2;
    private static final int ITEM_REQUEST_FRIEND = 3;
    private static final int ITEM_SUGGEST_FRIEND = 4;

    private RelationService relationService;

    private AppCompatImageView ivToolbarBack;
    private SearchView svToolbarSearch;
    private TabLayout tbRelation;
    private LinearProgressIndicator pbLoading;
    private SwipeRefreshLayout lSwipeRefresh;
    private AppCompatTextView tvLabel, tvNoData;
    private RecyclerView rvRelation;

    private RelationAdapter relationAdapter;
    private List<RelationUser> relations, viewRelations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relation);
        // api
        relationService = ApiClient.getClient().create(RelationService.class);

        // binding
        ivToolbarBack = findViewById(R.id.iv_toolbar_back);
        svToolbarSearch = findViewById(R.id.sv_toolbar_search);
        tbRelation = findViewById(R.id.tb_relation);
        pbLoading = findViewById(R.id.pb_loading);
        lSwipeRefresh = findViewById(R.id.l_swipe_refresh);
        tvLabel = findViewById(R.id.tv_label);
        tvNoData = findViewById(R.id.tv_no_data);
        rvRelation = findViewById(R.id.rv_relation);

        relations = new ArrayList<>();
        viewRelations = new ArrayList<>();
        relationAdapter = new RelationAdapter(RelationActivity.this, viewRelations, new OnFriendListener() {
            @Override
            public void onUserClick(int position) {
                DestinationUser user = viewRelations.get(position).getUser();
                if (user != null) {
                    openUserDetailActivity(user.getId());
                }
            }

            @Override
            public void onChatClick(int position) {
                DestinationUser user = viewRelations.get(position).getUser();
                if (user != null) {
                    // open chat activity

                }
            }
        }, new OnAddFriendListener() {
            @Override
            public void onUserClick(int position) {
                DestinationUser user = viewRelations.get(position).getUser();
                if (user != null) {
                    openUserDetailActivity(user.getId());
                }
            }

            @Override
            public void onAcceptFriendClick(int position) {
                acceptFriend(position);
            }

            @Override
            public void onDeleteFriendClick(int position) {
                deleteRelation(position);
            }
        }, new OnRequestFriendListener() {
            @Override
            public void onUserClick(int position) {
                DestinationUser user = viewRelations.get(position).getUser();
                if (user != null) {
                    openUserDetailActivity(user.getId());
                }
            }

            @Override
            public void onCancelFriendClick(int position) {
                deleteRelation(position);
            }
        }, new OnSuggestFriendListener() {
            @Override
            public void onUserClick(int position) {
                DestinationUser user = viewRelations.get(position).getUser();
                if (user != null) {
                    openUserDetailActivity(user.getId());
                }
            }

            @Override
            public void onRequestFriendClick(int position) {
                // create a request friend
                sendRequestFriend(position);
            }

            @Override
            public void onDeleteFriendClick(int position) {
                // delete suggest friend
            }
        });
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(RelationActivity.this);
        rvRelation.setLayoutManager(layoutManager);
        rvRelation.setAdapter(relationAdapter);

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

        svToolbarSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                relationAdapter.getFilter().filter(newText);
                return false;
            }
        });

        // init tab layout
        initRelationTabs();
        tbRelation.getTabAt(2).select();

        // get data
        getData();
    }

    private void getData() {
        pbLoading.setVisibility(View.VISIBLE);
        Call<BaseResponse<List<RelationUser>>> call = relationService.getAllRelations();
        call.enqueue(new Callback<BaseResponse<List<RelationUser>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<RelationUser>>> call, Response<BaseResponse<List<RelationUser>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<RelationUser>> res = response.body();
                    relations.clear();
                    relations.addAll(res.getData());
                    TabLayout.Tab tab = tbRelation.getTabAt(tbRelation.getSelectedTabPosition());
                    updateDataRecycleView((int) tab.getTag());
                } else {
                    Toast.makeText(RelationActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
                pbLoading.setVisibility(View.GONE);
                lSwipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<BaseResponse<List<RelationUser>>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(RelationActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                lSwipeRefresh.setRefreshing(false);
            }
        });
    }

    private void updateDataRecycleView(int type) {
        if (type == ITEM_ADD_FRIEND) {
            viewRelations.clear();
            for (int i = 0; i < relations.size(); i++) {
                RelationUser relationUser = relations.get(i);
                if (relationUser.getRelation().equals("receiver")) {
                    viewRelations.add(relationUser);
                }
            }
            tvLabel.setText(viewRelations.size() + " lời mời kết bạn");
        } else if (type == ITEM_FRIEND) {
            viewRelations.clear();
            for (int i = 0; i < relations.size(); i++) {
                RelationUser relationUser = relations.get(i);
                if (relationUser.getRelation().equals("friend")) {
                    viewRelations.add(relationUser);
                }
            }
            tvLabel.setText(viewRelations.size() + " người bạn");
        } else if (type == ITEM_REQUEST_FRIEND) {
            viewRelations.clear();
            for (int i = 0; i < relations.size(); i++) {
                RelationUser relationUser = relations.get(i);
                if (relationUser.getRelation().equals("sender")) {
                    viewRelations.add(relationUser);
                }
            }
            tvLabel.setText(viewRelations.size() + " lời mời đã gửi");
        } else {
            viewRelations.clear();
            for (int i = 0; i < relations.size(); i++) {
                RelationUser relationUser = relations.get(i);
                if (relationUser.getRelation().equals("")) {
                    viewRelations.add(relationUser);
                }
            }
            tvLabel.setText("Gợi ý kết bạn");
        }
        if (viewRelations.size() == 0) {
            tvNoData.setVisibility(View.VISIBLE);
        } else {
            tvNoData.setVisibility(View.GONE);
        }
        relationAdapter.notifyDataSetChanged();
        relationAdapter.getFilter().filter(svToolbarSearch.getQuery());
    }

    private void initRelationTabs() {
        tbRelation.addTab(tbRelation.newTab().setTag(ITEM_ADD_FRIEND).setCustomView(initTabView(ITEM_ADD_FRIEND)));
        tbRelation.addTab(tbRelation.newTab().setTag(ITEM_SUGGEST_FRIEND).setCustomView(initTabView(ITEM_SUGGEST_FRIEND)));
        tbRelation.addTab(tbRelation.newTab().setTag(ITEM_FRIEND).setCustomView(initTabView(ITEM_FRIEND)));
        tbRelation.addTab(tbRelation.newTab().setTag(ITEM_REQUEST_FRIEND).setCustomView(initTabView(ITEM_REQUEST_FRIEND)));

        tbRelation.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int type = (int) tab.getTag();
                // update view
                View view = tab.getCustomView();
                ConstraintLayout lRoot = view.findViewById(R.id.l_root);
                AppCompatTextView tvRelation = view.findViewById(R.id.tv_relation);
                lRoot.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(RelationActivity.this, R.color.color_tab_relation_selected)));
                tvRelation.setTextColor(ContextCompat.getColor(RelationActivity.this, R.color.color_text_highlight));
                // update data
                updateDataRecycleView(type);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int type = (int) tab.getTag();
                // update view
                View view = tab.getCustomView();
                ConstraintLayout lRoot = view.findViewById(R.id.l_root);
                AppCompatTextView tvRelation = view.findViewById(R.id.tv_relation);
                lRoot.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(RelationActivity.this, R.color.color_tab_relation_primary)));
                tvRelation.setTextColor(ContextCompat.getColor(RelationActivity.this, R.color.color_primary_80));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private View initTabView(int type) {
        View view = LayoutInflater.from(RelationActivity.this).inflate(R.layout.item_tab_relation, null);
        ConstraintLayout lRoot = view.findViewById(R.id.l_root);
        AppCompatTextView tvRelation = view.findViewById(R.id.tv_relation);
        lRoot.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(RelationActivity.this, R.color.color_tab_relation_primary)));
        tvRelation.setTextColor(ContextCompat.getColor(RelationActivity.this, R.color.color_primary_80));
        if (type == ITEM_ADD_FRIEND) {
            tvRelation.setText(R.string.tab_relation_add_friend);
        } else if (type == ITEM_REQUEST_FRIEND) {
            tvRelation.setText(R.string.tab_relation_request_friend);
        } else if (type == ITEM_FRIEND) {
            tvRelation.setText(R.string.tab_relation_friend);
        } else {
            tvRelation.setText(R.string.tab_relation_suggest);
        }
        return view;
    }

    private void openUserDetailActivity(int userId) {
        Intent intent = new Intent(RelationActivity.this, UserDetailActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }

    private void acceptFriend(int position) {
        pbLoading.setVisibility(View.VISIBLE);
        RelationUser relation = viewRelations.get(position);
        int id = relation.getId();
        Call<BaseResponse<RelationUser>> call = relationService.updateRelation(id, 1);
        call.enqueue(new Callback<BaseResponse<RelationUser>>() {
            @Override
            public void onResponse(Call<BaseResponse<RelationUser>> call, Response<BaseResponse<RelationUser>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<RelationUser> res = response.body();
                    // update list
                    RelationUser relationUser = res.getData();
                    for (int i = 0; i < relations.size(); i++) {
                        if (relations.get(i).getId() == id) {
                            relations.set(i, relationUser);
                            break;
                        }
                    }
                    viewRelations.remove(position);
                    relationAdapter.notifyItemRemoved(position);
                    Toast.makeText(RelationActivity.this, R.string.accept_friend_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RelationActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<BaseResponse<RelationUser>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(RelationActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteRelation(int position) {
        pbLoading.setVisibility(View.VISIBLE);
        RelationUser relation = viewRelations.get(position);
        int id = relation.getId();
        Call<BaseResponse<String>> call = relationService.deleteRelation(id);
        call.enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful()) {
                    // update list
                    for (int i = 0; i < relations.size(); i++) {
                        if (relations.get(i).getId() == id) {
                            relations.remove(i);
                            break;
                        }
                    }
                    viewRelations.remove(position);
                    relationAdapter.notifyItemRemoved(position);
                } else {
                    Toast.makeText(RelationActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(RelationActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendRequestFriend(int position) {
        pbLoading.setVisibility(View.VISIBLE);
        DestinationUser destinationUser = viewRelations.get(position).getUser();
        // create a form request friend and send to server
        Call<BaseResponse<RelationUser>> call = relationService.createRequestFriend(destinationUser.getId());
        call.enqueue(new Callback<BaseResponse<RelationUser>>() {
            @Override
            public void onResponse(Call<BaseResponse<RelationUser>> call, Response<BaseResponse<RelationUser>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<RelationUser> res = response.body();
                    // update list
                    relations.add(res.getData());
                    Toast.makeText(RelationActivity.this, R.string.relation_create_request_friend_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RelationActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<BaseResponse<RelationUser>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(RelationActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }
}