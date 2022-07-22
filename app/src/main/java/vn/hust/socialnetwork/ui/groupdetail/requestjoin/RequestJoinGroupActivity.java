package vn.hust.socialnetwork.ui.groupdetail.requestjoin;

import static vn.hust.socialnetwork.utils.StringExtension.checkValidValueString;

import androidx.annotation.NonNull;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.fcm.Data;
import vn.hust.socialnetwork.models.fcm.DataMessageSender;
import vn.hust.socialnetwork.models.fcm.FCMResponse;
import vn.hust.socialnetwork.models.fcm.Token;
import vn.hust.socialnetwork.models.group.Group;
import vn.hust.socialnetwork.models.group.MemberGroup;
import vn.hust.socialnetwork.models.notification.Notification;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.GroupService;
import vn.hust.socialnetwork.network.NotificationService;
import vn.hust.socialnetwork.ui.groupdetail.GroupDetailActivity;
import vn.hust.socialnetwork.ui.groupdetail.requestjoin.adapters.OnRequestJoinGroupListener;
import vn.hust.socialnetwork.ui.groupdetail.requestjoin.adapters.RequestJoinGroupAdapter;
import vn.hust.socialnetwork.ui.groupdetail.viewmember.ViewMemberActivity;
import vn.hust.socialnetwork.ui.userdetail.UserDetailActivity;
import vn.hust.socialnetwork.utils.NotificationExtension;

public class RequestJoinGroupActivity extends AppCompatActivity {

    private GroupService groupService;
    private NotificationService notificationService;

    private AppCompatImageView ivToolbarBack;
    private LinearProgressIndicator pbLoading;
    private SearchView svSearch;
    private SwipeRefreshLayout lSwipeRefresh;
    private AppCompatTextView tvToolbarTitle, tvNoData, tvLabel;
    private RecyclerView rvRequestJoin;

    private RequestJoinGroupAdapter requestJoinGroupAdapter;
    private List<MemberGroup> requestJoins;
    private int groupId;
    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_join_group);

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            groupId = extras.getInt("group_id");
        } else {
            groupId = 0;
        }

        // api
        groupService = ApiClient.getClient().create(GroupService.class);
        notificationService = ApiClient.getClient().create(NotificationService.class);

        // binding
        ivToolbarBack = findViewById(R.id.iv_toolbar_back);
        pbLoading = findViewById(R.id.pb_loading);
        svSearch = findViewById(R.id.sv_search);
        lSwipeRefresh = findViewById(R.id.l_swipe_refresh);
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        tvLabel = findViewById(R.id.tv_label);
        rvRequestJoin = findViewById(R.id.rv_request_join);
        tvNoData = findViewById(R.id.tv_no_data);

        // init data
        tvToolbarTitle.setText(R.string.toolbar_title_request_join_group);

        requestJoins = new ArrayList<>();
        requestJoinGroupAdapter = new RequestJoinGroupAdapter(RequestJoinGroupActivity.this, requestJoins, new OnRequestJoinGroupListener() {
            @Override
            public void onUserClick(int position) {
                // open user detail
                Intent intent = new Intent(RequestJoinGroupActivity.this, UserDetailActivity.class);
                intent.putExtra("user_id", requestJoins.get(position).getUser().getId());
                startActivity(intent);
            }

            @Override
            public void onAcceptRequestClick(int position) {
                if (group != null) {
                    acceptRequest(position);
                } else {
                    Toast.makeText(RequestJoinGroupActivity.this, R.string.error_request_join_group, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRefuseRequestClick(int position) {
                if (group != null) {
                    refuseRequest(position);
                } else {
                    Toast.makeText(RequestJoinGroupActivity.this, R.string.error_request_join_group, Toast.LENGTH_SHORT).show();
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(RequestJoinGroupActivity.this);
        rvRequestJoin.setLayoutManager(layoutManager);
        rvRequestJoin.setAdapter(requestJoinGroupAdapter);

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
                requestJoinGroupAdapter.getFilter().filter(newText);
                return false;
            }
        });

        // call api
        getGroupDetail(groupId);
        getData();
    }

    private void getGroupDetail(int groupId) {
        Call<BaseResponse<Group>> call = groupService.getGroupDetail(groupId);
        call.enqueue(new Callback<BaseResponse<Group>>() {
            @Override
            public void onResponse(Call<BaseResponse<Group>> call, Response<BaseResponse<Group>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<Group> res = response.body();
                    group = res.getData();
                } else {
                    Toast.makeText(RequestJoinGroupActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Group>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                Toast.makeText(RequestJoinGroupActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getData() {
        pbLoading.setVisibility(View.VISIBLE);
        Call<BaseResponse<List<MemberGroup>>> call = groupService.getGroupRequests(groupId);
        call.enqueue(new Callback<BaseResponse<List<MemberGroup>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<MemberGroup>>> call, Response<BaseResponse<List<MemberGroup>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<MemberGroup>> res = response.body();
                    requestJoins.clear();
                    requestJoins.addAll(res.getData());
                    requestJoinGroupAdapter.notifyDataSetChanged();
                    tvLabel.setText(requestJoins.size() + " yêu cầu");
                    if (requestJoins.size() == 0) {
                        tvNoData.setVisibility(View.VISIBLE);
                    } else {
                        tvNoData.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(RequestJoinGroupActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(RequestJoinGroupActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                lSwipeRefresh.setRefreshing(false);
            }
        });
    }

    private void refuseRequest(int position) {
        MemberGroup requestJoin = requestJoins.get(position);
        Call<BaseResponse<String>> call = groupService.deleteMemberGroupRequest(requestJoin.getId());
        call.enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful()) {
                    requestJoins.remove(position);
                    requestJoinGroupAdapter.notifyItemRemoved(position);
                    tvLabel.setText(requestJoins.size() + " yêu cầu");
                } else {
                    Toast.makeText(RequestJoinGroupActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                Toast.makeText(RequestJoinGroupActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void acceptRequest(int position) {
        MemberGroup requestJoin = requestJoins.get(position);
        Call<BaseResponse<MemberGroup>> call = groupService.acceptMemberGroupRequest(requestJoin.getId());
        call.enqueue(new Callback<BaseResponse<MemberGroup>>() {
            @Override
            public void onResponse(Call<BaseResponse<MemberGroup>> call, Response<BaseResponse<MemberGroup>> response) {
                if (response.isSuccessful()) {
                    requestJoins.remove(position);
                    requestJoinGroupAdapter.notifyItemRemoved(position);
                    tvLabel.setText(requestJoins.size() + " yêu cầu");

                    // send a notification
                    int receiverId = requestJoin.getUser().getId();
                    String imageUrl = "";
                    if (checkValidValueString(group.getCoverImage())) {
                        imageUrl = group.getCoverImage();
                    }
                    int type = 4;
                    String content = "Yêu cầu tham gia nhóm " + group.getName() + " đã được chấp nhận";
                    String url = "group/" + groupId;
                    sendNotification(receiverId, imageUrl, type, content, url);
                } else {
                    Toast.makeText(RequestJoinGroupActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<MemberGroup>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                Toast.makeText(RequestJoinGroupActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(int receiverId, String imageUrl, int type, String content, String url) {
        Map<String, Object> req = new HashMap<>();
        req.put("receiver_id", receiverId);
        req.put("type", type);
        req.put("content", content);
        req.put("url", url);
        if (!imageUrl.isEmpty()) {
            req.put("image_url", imageUrl);
        }
        Call<BaseResponse<Notification>> call = notificationService.createNotification(req);
        call.enqueue(new Callback<BaseResponse<Notification>>() {
            @Override
            public void onResponse(Call<BaseResponse<Notification>> call, Response<BaseResponse<Notification>> response) {
                if (response.isSuccessful()) {
                    // send a notification to FCM
                    Data data = new Data("Social Network", content, "notification", url);
                    NotificationExtension.sendFCMNotification(receiverId, data);
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Notification>> call, Throwable t) {
                // error
                call.cancel();
            }
        });
    }
}