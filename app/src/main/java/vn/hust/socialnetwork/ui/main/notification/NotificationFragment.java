package vn.hust.socialnetwork.ui.main.notification;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.notification.Notification;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.NotificationService;
import vn.hust.socialnetwork.ui.groupdetail.GroupDetailActivity;
import vn.hust.socialnetwork.ui.groupdetail.requestjoin.RequestJoinGroupActivity;
import vn.hust.socialnetwork.ui.main.notification.adapters.NotificationAdapter;
import vn.hust.socialnetwork.ui.main.notification.adapters.OnNotificationListener;
import vn.hust.socialnetwork.ui.postdetail.PostDetailActivity;
import vn.hust.socialnetwork.ui.relation.RelationActivity;
import vn.hust.socialnetwork.ui.userdetail.UserDetailActivity;

public class NotificationFragment extends Fragment {

    private NotificationService notificationService;

    private LinearProgressIndicator pbLoading;
    private AppCompatImageView ivReadAll;
    private AppCompatTextView tvEmptyTextData;
    private SwipeRefreshLayout lSwipeRefresh;
    private RecyclerView rvNotification;

    private NotificationAdapter notificationAdapter;
    private List<Notification> notifications;

    private int limitNotification = 5;
    private int pageNotification = 1;
    private boolean isLoadingMoreNotification = false;
    private boolean canLoadMoreNotification = true;

    public NotificationFragment() {
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
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        // api
        notificationService = ApiClient.getClient().create(NotificationService.class);

        // binding
        pbLoading = view.findViewById(R.id.pb_loading);
        ivReadAll = view.findViewById(R.id.iv_read_all);
        lSwipeRefresh = view.findViewById(R.id.l_swipe_refresh);
        rvNotification = view.findViewById(R.id.rv_notification);
        tvEmptyTextData = view.findViewById(R.id.tv_empty_text_data);

        // init
        notifications = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(getContext(), notifications, new OnNotificationListener() {
            @Override
            public void onMenuItemClick(int position) {
                onMenuItemNotificationClick(position);
            }

            @Override
            public void onItemClick(int position) {
                onItemNotificationClick(position);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvNotification.setLayoutManager(layoutManager);
        rvNotification.setAdapter(notificationAdapter);
        rvNotification.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // end list
                if (!recyclerView.canScrollVertically(1)) {
                    if (canLoadMoreNotification && !isLoadingMoreNotification) {
                        loadMoreNotification();
                    }
                }
            }
        });

        ivReadAll.setEnabled(false);
        ivReadAll.setColorFilter(ContextCompat.getColor(getActivity(), R.color.color_text_secondary));

        lSwipeRefresh.setColorSchemeResources(R.color.colorAccent);
        lSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                lSwipeRefresh.setRefreshing(false);
            }
        });

        ivReadAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReadAllClick();
            }
        });

        getAllNotifications();

        return view;
    }

    private void onItemNotificationClick(int position) {
        Notification notification = notifications.get(position);
        if (notification.getNotificationStatusCode() == 0) {
            updateReadStatusNotification(position, 1);
        }
        String url = notification.getUrl();
        // url: {name}/{id}
        // type: new request friend 1 => request_friend
        //       new accept friend  2 => user/{user_id}
        //       new action post    3 => post/{post_id}
        //       new action group   4 => group/{group_id} or request_join_group/{group_id}
        String[] s = url.split("/");
        if (s.length == 1) {
            if (notification.getType() == 1 && s[0].equals("request_friend")) {
                openActivityMyFriend();
            }
        } else if (s.length == 2) {
            try {
                int id = Integer.parseInt(s[1]);
                if (notification.getType() == 2 && s[0].equals("user")) {
                    openActivityUserDetail(id);
                } else if (notification.getType() == 3 && s[0].equals("post")) {
                    openActivityPost(id);
                } else if (notification.getType() == 4 && s[0].equals("group")) {
                    openActivityGroupDetail(id);
                } else if (notification.getType() == 4 && s[0].equals("request_join_group")) {
                    openActivityGroupRequest(id);
                }
            } catch (NumberFormatException ignored) {

            }
        }
    }

    private void openActivityGroupRequest(int groupId) {
        // open group request
        Intent intent = new Intent(getActivity(), RequestJoinGroupActivity.class);
        intent.putExtra("group_id", groupId);
        startActivity(intent);
    }

    private void openActivityGroupDetail(int groupId) {
        // open group detail
        Intent intent = new Intent(getActivity(), GroupDetailActivity.class);
        intent.putExtra("group_id", groupId);
        startActivity(intent);
    }

    private void openActivityPost(int postId) {
        // open post detail
        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
        intent.putExtra("post_id", postId);
        startActivity(intent);
    }

    private void openActivityUserDetail(int userId) {
        // open user detail
        Intent intent = new Intent(getActivity(), UserDetailActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }

    private void openActivityMyFriend() {
        // open relation add friend
        Intent intent = new Intent(getActivity(), RelationActivity.class);
        intent.putExtra("navigation_to", "add_friend");
        startActivity(intent);
    }

    private void onMenuItemNotificationClick(int position) {
        Notification notification = notifications.get(position);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_menu_notification);

        CircleImageView civImageNotification = bottomSheetDialog.findViewById(R.id.civ_image_notification);
        AppCompatTextView tvNotificationContent = bottomSheetDialog.findViewById(R.id.tv_notification_content);
        ConstraintLayout lReadNotification = bottomSheetDialog.findViewById(R.id.l_read_notification);
        ConstraintLayout lUnreadNotification = bottomSheetDialog.findViewById(R.id.l_unread_notification);
        ConstraintLayout lDeleteNotification = bottomSheetDialog.findViewById(R.id.l_delete_notification);

        if (notification.getType() == 4) {
            Glide.with(getActivity())
                    .asBitmap()
                    .load(notification.getImageUrl())
                    .error(R.drawable.default_group_cover)
                    .into(civImageNotification);
        } else {
            Glide.with(getActivity())
                    .asBitmap()
                    .load(notification.getImageUrl())
                    .error(R.drawable.default_avatar)
                    .into(civImageNotification);
        }
        tvNotificationContent.setText(notification.getContent());

        if (notification.getNotificationStatusCode() == 0) {
            lUnreadNotification.setVisibility(View.GONE);
            lReadNotification.setVisibility(View.VISIBLE);
            lReadNotification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    updateReadStatusNotification(position, 1);
                }
            });
        } else {
            lReadNotification.setVisibility(View.GONE);
            lUnreadNotification.setVisibility(View.VISIBLE);
            lUnreadNotification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    updateReadStatusNotification(position, 0);
                }
            });
        }

        lDeleteNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                deleteNotification(position);
            }
        });

        bottomSheetDialog.show();
    }

    // status: 0 => unread, 1 => read
    private void updateReadStatusNotification(int position, int notificationStatusCode) {
        Notification notification = notifications.get(position);
        Call<BaseResponse<Notification>> call = notificationService.updateNotification(notification.getId(), notificationStatusCode);
        call.enqueue(new Callback<BaseResponse<Notification>>() {
            @Override
            public void onResponse(Call<BaseResponse<Notification>> call, Response<BaseResponse<Notification>> response) {
                if (response.isSuccessful()) {
                    notification.setNotificationStatusCode(notificationStatusCode);
                    notificationAdapter.notifyItemChanged(position);
                    handleUnreadNotifications();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Notification>> call, Throwable t) {

            }
        });
    }

    private void deleteNotification(int position) {
        Notification notification = notifications.get(position);
        Call<BaseResponse<String>> call = notificationService.deleteNotification(notification.getId());
        call.enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), R.string.delete_notification_success, Toast.LENGTH_SHORT).show();
                    notifications.remove(position);
                    notificationAdapter.notifyItemRemoved(position);
                    handleUnreadNotifications();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {

            }
        });
    }

    private void handleUnreadNotifications() {
        boolean isUnread = false;
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).getNotificationStatusCode() == 0) {
                isUnread = true;
                break;
            }
        }
        ivReadAll.setEnabled(isUnread);
        ivReadAll.setColorFilter(isUnread ? ContextCompat.getColor(getActivity(), R.color.color_text_primary) : ContextCompat.getColor(getActivity(), R.color.color_text_secondary));
    }

    private void onReadAllClick() {
        ivReadAll.setEnabled(false);
        ivReadAll.setColorFilter(ContextCompat.getColor(getActivity(), R.color.color_text_secondary));
        // call api read more
        pbLoading.setVisibility(View.VISIBLE);
        Call<BaseResponse<String>> call = notificationService.readAllNotifications();
        call.enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful()) {
                    // update list notifications
                    Toast.makeText(getContext(), R.string.read_all_notification, Toast.LENGTH_SHORT).show();

                    for (int i = 0; i < notifications.size(); i++) {
                        Notification notification = notifications.get(i);
                        if (notification.getNotificationStatusCode() == 0) {
                            notification.setNotificationStatusCode(1);
                            notificationAdapter.notifyItemChanged(i);
                        }
                    }
                }
                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                ivReadAll.setEnabled(true);
                ivReadAll.setColorFilter(ContextCompat.getColor(getActivity(), R.color.color_text_primary));
                pbLoading.setVisibility(View.GONE);
            }
        });
    }

    private void refresh() {
        getAllNotifications();
    }

    private void getAllNotifications() {
        // call api get notifications
        pbLoading.setVisibility(View.VISIBLE);
        Map<String, Object> options = new HashMap<>();
        options.put("limit", limitNotification * pageNotification);
        options.put("page", 1);
        Call<BaseResponse<List<Notification>>> call = notificationService.getNotifications(options);
        call.enqueue(new Callback<BaseResponse<List<Notification>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Notification>>> call, Response<BaseResponse<List<Notification>>> response) {
                if (response.isSuccessful()) {
                    notifications.clear();
                    BaseResponse<List<Notification>> res = response.body();
                    // update new notifications
                    notifications.addAll(res.getData());
                    notificationAdapter.notifyDataSetChanged();
                    handleUnreadNotifications();
                    if (notifications.size() == 0) {
                        tvEmptyTextData.setVisibility(View.VISIBLE);
                        canLoadMoreNotification = false;
                    } else {
                        tvEmptyTextData.setVisibility(View.GONE);
                        canLoadMoreNotification = true;
                    }
                }
                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Notification>>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                pbLoading.setVisibility(View.GONE);
            }
        });
    }

    private void loadMoreNotification() {
        isLoadingMoreNotification = true;
        // call api get more notifications
        pbLoading.setVisibility(View.VISIBLE);
        Map<String, Object> options = new HashMap<>();
        options.put("limit", limitNotification);
        options.put("page", pageNotification + 1);
        Call<BaseResponse<List<Notification>>> call = notificationService.getNotifications(options);
        call.enqueue(new Callback<BaseResponse<List<Notification>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Notification>>> call, Response<BaseResponse<List<Notification>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<Notification>> res = response.body();
                    List<Notification> moreData = res.getData();
                    if (moreData.size() > 0) {
                        int oldSize = notifications.size();
                        // update new notifications
                        notifications.addAll(moreData);
                        notificationAdapter.notifyItemRangeInserted(oldSize, moreData.size());
                        handleUnreadNotifications();
                    } else {
                        canLoadMoreNotification = false;
                    }
                    pageNotification++;
                }
                pbLoading.setVisibility(View.GONE);
                isLoadingMoreNotification = false;
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Notification>>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                pbLoading.setVisibility(View.GONE);
                isLoadingMoreNotification = false;
            }
        });
    }
}