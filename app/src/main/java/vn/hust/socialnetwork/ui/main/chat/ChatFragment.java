package vn.hust.socialnetwork.ui.main.chat;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.chat.Conversation;
import vn.hust.socialnetwork.models.user.Relation;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.ChatService;
import vn.hust.socialnetwork.network.UserProfileService;
import vn.hust.socialnetwork.ui.main.chat.adapters.ConversationAdapter;
import vn.hust.socialnetwork.ui.main.chat.adapters.FriendAdapter;
import vn.hust.socialnetwork.ui.main.chat.adapters.OnConversationListener;
import vn.hust.socialnetwork.ui.main.chat.adapters.OnFriendListener;
import vn.hust.socialnetwork.ui.message.MessageActivity;
import vn.hust.socialnetwork.ui.relation.RelationActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;

public class ChatFragment extends Fragment {

    private UserProfileService userProfileService;
    private ChatService chatService;

    private List<Relation> friends;
    private FriendAdapter friendAdapter;
    private List<Conversation> conversations;
    private ConversationAdapter conversationAdapter;

    private SwipeRefreshLayout lSwipeRefresh;
    private CircleImageView civMyAvatar;
    private AppCompatTextView tvUserName;
    private AppCompatImageView ivStrangeChat, ivNewChat;
    private ConstraintLayout lSearchFriend;
    private RecyclerView rvFriend, rvConversation;

    public ChatFragment() {
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
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // api
        userProfileService = ApiClient.getClient().create(UserProfileService.class);
        chatService = ApiClient.getClient().create(ChatService.class);

        // binding
        lSwipeRefresh = view.findViewById(R.id.l_swipe_refresh);
        civMyAvatar = view.findViewById(R.id.civ_my_avatar);
        tvUserName = view.findViewById(R.id.tv_user_name);
        ivStrangeChat = view.findViewById(R.id.iv_strange_chat);
        ivNewChat = view.findViewById(R.id.iv_new_chat);
        lSearchFriend = view.findViewById(R.id.l_search_friend);
        rvFriend = view.findViewById(R.id.rv_friend);
        rvConversation = view.findViewById(R.id.rv_conversation);

        handleRefreshView();

        friends = new ArrayList<>();
        friendAdapter = new FriendAdapter(getContext(), friends, new OnFriendListener() {
            @Override
            public void onItemClick(int position) {
                // open chat activity
                Intent intent = new Intent(getActivity(), MessageActivity.class);
                intent.putExtra("user_id", friends.get(position).getId());
                startActivity(intent);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvFriend.setLayoutManager(layoutManager);
        rvFriend.setAdapter(friendAdapter);

        conversations = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(getContext(), conversations, new OnConversationListener() {
            @Override
            public void onItemClick(int position) {
                // open chat activity
                Intent intent = new Intent(getActivity(), MessageActivity.class);
                intent.putExtra("user_id", conversations.get(position).getReceiver().getId());
                startActivity(intent);
            }
        });
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getContext());
        rvConversation.setLayoutManager(layoutManager2);
        rvConversation.setAdapter(conversationAdapter);

        // call api to get all data
        getData();

        return view;
    }

    private void handleRefreshView() {
        lSwipeRefresh.setColorSchemeResources(R.color.colorAccent);
        lSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    private void getData() {
        // init data
        Glide.with(ChatFragment.this)
                .asBitmap()
                .load(Hawk.get(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, ""))
                .into(civMyAvatar);
        tvUserName.setText(Hawk.get(AppSharedPreferences.LOGGED_IN_USER_NAME_KEY, ""));

        // call api
        int myUserId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY);
        getFriends(myUserId);
        getConversations();
    }

    private void refresh() {
        // call api
        int myUserId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY);
        getFriends(myUserId);
        getConversations();
    }

    private void getFriends(int userId) {
        Call<BaseResponse<List<Relation>>> call = userProfileService.getAllFriends(userId);
        call.enqueue(new Callback<BaseResponse<List<Relation>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Relation>>> call, Response<BaseResponse<List<Relation>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<Relation>> res = response.body();
                    friends.clear();
                    friends.addAll(res.getData());
                    friendAdapter.notifyDataSetChanged();
                }
                lSwipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Relation>>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                lSwipeRefresh.setRefreshing(false);
            }
        });
    }

    private void getConversations() {
        Call<BaseResponse<List<Conversation>>> call = chatService.getConversations();
        call.enqueue(new Callback<BaseResponse<List<Conversation>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Conversation>>> call, Response<BaseResponse<List<Conversation>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<Conversation>> res = response.body();
                    conversations.clear();
                    conversations.addAll(res.getData());
                    conversationAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Conversation>>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
            }
        });
    }
}