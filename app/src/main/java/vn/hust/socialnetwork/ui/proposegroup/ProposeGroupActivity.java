package vn.hust.socialnetwork.ui.proposegroup;

import static vn.hust.socialnetwork.utils.StringExtension.checkValidValueString;

import androidx.annotation.NonNull;
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
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.orhanobut.hawk.Hawk;

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
import vn.hust.socialnetwork.ui.proposegroup.adapters.GroupAdapter;
import vn.hust.socialnetwork.ui.proposegroup.adapters.OnGroupListener;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.NotificationExtension;

public class ProposeGroupActivity extends AppCompatActivity {

    private GroupService groupService;
    private NotificationService notificationService;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout lMain, lError;
    private ShimmerFrameLayout lShimmer;
    private AppCompatImageView ivToolbarBack;
    private AppCompatTextView tvToolbarTitle, tvNoData;
    private RecyclerView rvProposeGroup;

    private List<Group> groups;
    private GroupAdapter groupAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_propose_group);

        // api
        groupService = ApiClient.getClient().create(GroupService.class);
        notificationService = ApiClient.getClient().create(NotificationService.class);

        // binding
        lMain = findViewById(R.id.l_main);
        lShimmer = findViewById(R.id.l_shimmer);
        lError = findViewById(R.id.l_error);
        swipeRefreshLayout = findViewById(R.id.l_swipe_refresh);
        ivToolbarBack = findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        rvProposeGroup = findViewById(R.id.rv_propose_group);
        tvNoData = findViewById(R.id.tv_no_data);

        // init
        tvToolbarTitle.setText(R.string.propose_group);

        handleRefreshView();

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        groups = new ArrayList<>();
        groupAdapter = new GroupAdapter(ProposeGroupActivity.this, groups, new OnGroupListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(ProposeGroupActivity.this, GroupDetailActivity.class);
                intent.putExtra("group_id", groups.get(position).getId());
                startActivity(intent);
            }

            @Override
            public void onJoinGroupClick(int position) {
                sendRequestJoinGroup(position);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(ProposeGroupActivity.this);
        rvProposeGroup.setLayoutManager(layoutManager);
        rvProposeGroup.setAdapter(groupAdapter);

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
        Call<BaseResponse<List<Group>>> call = groupService.getSuggestGroups();
        call.enqueue(new Callback<BaseResponse<List<Group>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Group>>> call, Response<BaseResponse<List<Group>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<Group>> res = response.body();
                    groups.clear();
                    groups.addAll(res.getData());
                    groupAdapter.notifyDataSetChanged();
                    lMain.setVisibility(View.VISIBLE);
                    lError.setVisibility(View.GONE);
                    if (groups.size() == 0) {
                        tvNoData.setVisibility(View.VISIBLE);
                    } else {
                        tvNoData.setVisibility(View.GONE);
                    }
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

    private void sendRequestJoinGroup(int position) {
        // create a form request join group and send to server
        Group group = groups.get(position);
        int userId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0);
        Call<BaseResponse<MemberGroup>> call = groupService.sendRequestJoinGroup(group.getId(), userId);
        call.enqueue(new Callback<BaseResponse<MemberGroup>>() {
            @Override
            public void onResponse(Call<BaseResponse<MemberGroup>> call, Response<BaseResponse<MemberGroup>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProposeGroupActivity.this, R.string.join_group_success, Toast.LENGTH_SHORT).show();
                    groups.remove(position);
                    groupAdapter.notifyItemRemoved(position);
                    if (groups.size() == 0) {
                        tvNoData.setVisibility(View.VISIBLE);
                    } else {
                        tvNoData.setVisibility(View.GONE);
                    }

                    // send a notification
                    int receiverId = userId;
                    String imageUrl = "";
                    if (checkValidValueString(group.getCoverImage())) {
                        imageUrl = group.getCoverImage();
                    }
                    int type = 4;
                    String content = "Yêu cầu tham gia nhóm " + group.getName() + " đã được chấp nhận";
                    String url = "group/" + group.getId();
                    sendNotification(receiverId, imageUrl, type, content, url);
                } else {
                    Toast.makeText(ProposeGroupActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<MemberGroup>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                Toast.makeText(ProposeGroupActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
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