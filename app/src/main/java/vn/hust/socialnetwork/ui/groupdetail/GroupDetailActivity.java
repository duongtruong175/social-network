package vn.hust.socialnetwork.ui.groupdetail;

import static vn.hust.socialnetwork.utils.ContextExtension.getImageFromLayout;
import static vn.hust.socialnetwork.utils.StringExtension.checkValidValueString;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.orhanobut.hawk.Hawk;
import com.tbruyelle.rxpermissions3.RxPermissions;
import com.zhihu.matisse.Matisse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import kr.co.prnd.readmore.ReadMoreTextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.common.view.reactuser.ReactUserFragment;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.fcm.Data;
import vn.hust.socialnetwork.models.fcm.DataMessageSender;
import vn.hust.socialnetwork.models.fcm.FCMResponse;
import vn.hust.socialnetwork.models.fcm.Token;
import vn.hust.socialnetwork.models.group.Group;
import vn.hust.socialnetwork.models.group.MemberGroup;
import vn.hust.socialnetwork.models.media.Media;
import vn.hust.socialnetwork.models.notification.Notification;
import vn.hust.socialnetwork.models.post.Post;
import vn.hust.socialnetwork.models.post.ReactUser;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.GroupService;
import vn.hust.socialnetwork.network.NotificationService;
import vn.hust.socialnetwork.network.PostService;
import vn.hust.socialnetwork.ui.groupdetail.adapters.OnPostListener;
import vn.hust.socialnetwork.ui.groupdetail.adapters.PostAdapter;
import vn.hust.socialnetwork.ui.groupdetail.crop.CropGroupCoverActivity;
import vn.hust.socialnetwork.ui.groupdetail.menu.MenuGroupDetailActivity;
import vn.hust.socialnetwork.ui.groupdetail.viewmember.ViewMemberActivity;
import vn.hust.socialnetwork.ui.mediaviewer.MediaViewerActivity;
import vn.hust.socialnetwork.ui.postcreator.PostCreatorActivity;
import vn.hust.socialnetwork.ui.postdetail.PostDetailActivity;
import vn.hust.socialnetwork.ui.userdetail.UserDetailActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.ContextExtension;
import vn.hust.socialnetwork.utils.FileExtension;
import vn.hust.socialnetwork.utils.MediaPicker;
import vn.hust.socialnetwork.utils.NotificationExtension;
import vn.hust.socialnetwork.utils.TimeExtension;

public class GroupDetailActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHOOSE_IMAGE_COVER = 104;
    private static final int REQUEST_CODE_CAPTURE_PHOTO_COVER = 105;

    private String capturePhotoCoverPath; // file path for capturing photo

    private GroupService groupService;
    private PostService postService;
    private NotificationService notificationService;

    private int groupId;
    private int memberGroupRequestId;
    private Group group;
    private List<Post> posts;
    private PostAdapter postAdapter;

    private LinearProgressIndicator pbLoading;
    private ConstraintLayout lError, lPostEmpty;
    private CoordinatorLayout lMain;
    private ShimmerFrameLayout lShimmer;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AppBarLayout appBarLayout;
    private AppCompatTextView tvGroupNameToolbar, tvGroupName, tvJoinGroup, tvInviteFriend, tvGroupType, tvGroupMemberCount, tvGroupPostCount, tvCreateGroupTime;
    private LinearLayoutCompat lMemberGroup;
    private ConstraintLayout lViewMemberGroup;
    private CircleImageView civPreviewMemberAvatar, civAdminAvatar;
    private AppCompatImageView ivViewMoreMember, ivGroupType;
    private ReadMoreTextView tvIntroduction;
    private AppCompatImageView ivImageCover, ivImageCoverCamera, ivBackToolbar, ivMenuToolbar, ivShareToolbar, ivSearchToolbar;
    private AppCompatButton btnRefresh;
    private RecyclerView rvPost;
    private FloatingActionButton btnAddPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            groupId = extras.getInt("group_id");
        } else {
            groupId = 0;
        }

        // api
        groupService = ApiClient.getClient().create(GroupService.class);
        postService = ApiClient.getClient().create(PostService.class);
        notificationService = ApiClient.getClient().create(NotificationService.class);

        // binding
        lMain = findViewById(R.id.l_main);
        lShimmer = findViewById(R.id.l_shimmer);
        lError = findViewById(R.id.l_error);
        lPostEmpty = findViewById(R.id.l_post_empty);
        pbLoading = findViewById(R.id.pb_loading);
        btnRefresh = findViewById(R.id.btn_refresh);
        swipeRefreshLayout = findViewById(R.id.l_swipe_refresh);
        appBarLayout = findViewById(R.id.ab_main_group_detail);
        ivBackToolbar = findViewById(R.id.iv_back_toolbar);
        ivMenuToolbar = findViewById(R.id.iv_menu_toolbar);
        ivShareToolbar = findViewById(R.id.iv_share_toolbar);
        ivSearchToolbar = findViewById(R.id.iv_search_toolbar);
        tvGroupNameToolbar = findViewById(R.id.tv_group_name_toolbar);
        ivImageCover = findViewById(R.id.iv_image_cover);
        ivImageCoverCamera = findViewById(R.id.iv_image_cover_camera);
        tvGroupName = findViewById(R.id.tv_group_name);
        tvJoinGroup = findViewById(R.id.tv_join_group);
        lMemberGroup = findViewById(R.id.l_member_group);
        lViewMemberGroup = findViewById(R.id.l_view_member_group);
        ivViewMoreMember = findViewById(R.id.iv_view_more_member);
        civPreviewMemberAvatar = findViewById(R.id.civ_preview_member_avatar);
        civAdminAvatar = findViewById(R.id.civ_admin_avatar);
        tvInviteFriend = findViewById(R.id.tv_invite_friend);
        tvIntroduction = findViewById(R.id.tv_introduction);
        ivGroupType = findViewById(R.id.iv_group_type);
        tvGroupType = findViewById(R.id.tv_group_type);
        tvGroupMemberCount = findViewById(R.id.tv_group_member_count);
        tvGroupPostCount = findViewById(R.id.tv_group_post_count);
        tvCreateGroupTime = findViewById(R.id.tv_create_group_time);
        rvPost = findViewById(R.id.rv_post);
        btnAddPost = findViewById(R.id.btn_add_post);

        handleRefreshView();
        handleToolbarView();

        // reload when view error
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeRefreshLayout.setRefreshing(true);
                refresh();
            }
        });

        ivBackToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ivMenuToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupDetailActivity.this, MenuGroupDetailActivity.class);
                intent.putExtra("group", group);
                openMenuActivityResultLauncher.launch(intent);
            }
        });

        ivSearchToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open activity search post in group
            }
        });

        ivShareToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShareGroupBottomSheetDialog();
            }
        });

        ivImageCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (group != null && checkValidValueString(group.getCoverImage())) {
                    Intent viewPhotoIntent = new Intent(GroupDetailActivity.this, MediaViewerActivity.class);
                    viewPhotoIntent.putExtra("media_url", group.getCoverImage());
                    viewPhotoIntent.putExtra("media_type", FileExtension.MEDIA_IMAGE_TYPE);
                    startActivity(viewPhotoIntent);
                }
            }
        });

        ivImageCoverCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeCoverBottomSheetDialog();
            }
        });

        //  send or cancel request join group
        tvJoinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOrCancelRequestJoinGroup();
            }
        });

        // view member group
        lViewMemberGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open activity view member group
                Intent intent = new Intent(GroupDetailActivity.this, ViewMemberActivity.class);
                intent.putExtra("admin", group.getAdmin());
                intent.putExtra("group_id", groupId);
                startActivity(intent);
            }
        });

        // invite friend to join group
        tvInviteFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open activity invite friend to join group
            }
        });

        // create a post
        btnAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupDetailActivity.this, PostCreatorActivity.class);
                intent.putExtra("group_id", groupId);
                intent.putExtra("type", 2);
                openPostCreatorActivityResultLauncher.launch(intent);
            }
        });

        // init data
        capturePhotoCoverPath = "";

        posts = new ArrayList<>();
        postAdapter = new PostAdapter(GroupDetailActivity.this, posts, new OnPostListener() {
            @Override
            public void onUserPostClick(int position) {
                // open user detail
                Intent intent = new Intent(GroupDetailActivity.this, UserDetailActivity.class);
                intent.putExtra("user_id", posts.get(position).getUser().getId());
                startActivity(intent);
            }

            @Override
            public void onMenuItemClick(int position) {
                showMenuPost(position);
            }

            @Override
            public void onContentLongClick(int position) {
                Dialog dialog = new Dialog(GroupDetailActivity.this);
                WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
                layoutParams.gravity = Gravity.CENTER | Gravity.START;
                layoutParams.x = 100;
                dialog.setContentView(R.layout.dialog_copy_content_post);
                dialog.setCancelable(true);
                dialog.findViewById(R.id.tv_copy_content).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        ContextExtension.copyToClipboard(GroupDetailActivity.this, posts.get(position).getCaption());
                        Toast.makeText(GroupDetailActivity.this, R.string.content_copied, Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show();
            }

            @Override
            public void onMediaItemClick(int position) {
                Media media = posts.get(position).getMedia();
                if (media != null) {
                    Intent viewPhotoIntent = new Intent(GroupDetailActivity.this, MediaViewerActivity.class);
                    viewPhotoIntent.putExtra("media_url", media.getSrc());
                    viewPhotoIntent.putExtra("media_type", media.getType());
                    startActivity(viewPhotoIntent);
                }
            }

            @Override
            public void onReactCountClick(int position) {
                Post post = posts.get(position);
                BottomSheetDialogFragment bottomSheetDialogFragment = ReactUserFragment.newInstance(post.getId(), "post");
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
            }

            @Override
            public void onReactActionClick(int reactType, int position) {
                createOrUpdateReactPost(reactType, position);
            }

            @Override
            public void onReactActionLongClick(int reactType, int position) {
                createOrUpdateReactPost(reactType, position);
            }

            @Override
            public void onCommentActionClick(int position) {
                Post post = posts.get(position);
                Intent intent = new Intent(GroupDetailActivity.this, PostDetailActivity.class);
                intent.putExtra("post_id", post.getId());
                intent.putExtra("is_show_comment_input", true);
                startActivity(intent);
            }

            @Override
            public void onShareActionClick(View lContentToShare, int position) {
                showSharePost(lContentToShare, position);
            }

            @Override
            public void onItemClick(int position) {
                Post post = posts.get(position);
                Intent intent = new Intent(GroupDetailActivity.this, PostDetailActivity.class);
                intent.putExtra("post_id", post.getId());
                intent.putExtra("is_show_comment_input", false);
                startActivity(intent);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(GroupDetailActivity.this);
        rvPost.setLayoutManager(layoutManager);
        rvPost.setAdapter(postAdapter);

        // call api to get all data
        getData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE_IMAGE_COVER && resultCode == Activity.RESULT_OK && data != null) {
            // Put selected image to Crop Cover Activity
            Intent intent = new Intent(GroupDetailActivity.this, CropGroupCoverActivity.class);
            intent.putExtra("image_path", Matisse.obtainPathResult(data).get(0));
            intent.putExtra("group_id", groupId);
            cropCoverActivityResultLauncher.launch(intent);
        }
        if (requestCode == REQUEST_CODE_CAPTURE_PHOTO_COVER && resultCode == Activity.RESULT_OK && !capturePhotoCoverPath.isEmpty()) {
            // Put captured image to Crop Cover Activity
            Intent intent = new Intent(GroupDetailActivity.this, CropGroupCoverActivity.class);
            intent.putExtra("image_path", capturePhotoCoverPath);
            intent.putExtra("group_id", groupId);
            cropCoverActivityResultLauncher.launch(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        postAdapter.continueCurrentPlayingVideo();
    }

    @Override
    public void onPause() {
        super.onPause();
        postAdapter.pauseAllPlayers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        postAdapter.releaseAllPlayers();
    }

    // ActivityResultLauncher for crop cover activity
    ActivityResultLauncher<Intent> cropCoverActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Get image from result
                        Intent data = result.getData();
                        if (data != null) {
                            // update value
                            String newCoverImage = data.getStringExtra("new_cover_image");
                            group.setCoverImage(newCoverImage);
                            Glide.with(GroupDetailActivity.this)
                                    .clear(ivImageCover);
                            Glide.with(GroupDetailActivity.this)
                                    .asBitmap()
                                    .load(newCoverImage)
                                    .error(R.drawable.default_group_cover)
                                    .into(ivImageCover);
                        }
                    }
                }
            });

    // ActivityResultLauncher for open menu user profile
    ActivityResultLauncher<Intent> openMenuActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Get data from result
                        Intent data = result.getData();
                        if (data != null) {
                            if (data.hasExtra("member_group_request_id")) {
                                memberGroupRequestId = data.getIntExtra("member_group_request_id", -1);
                            }
                            group = data.getParcelableExtra("updated_group");
                            handleGroupToView();
                        }
                    }
                }
            });

    // ActivityResultLauncher for post creator
    ActivityResultLauncher<Intent> openPostCreatorActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Get data from result
                        Intent data = result.getData();
                        if (data != null) {
                            // update value
                            int temp = group.getCounts().getPostCount() + 1;
                            group.getCounts().setPostCount(temp);
                            Post newPost = data.getParcelableExtra("new_post");
                            posts.add(0, newPost);
                            postAdapter.notifyItemInserted(0);
                            lPostEmpty.setVisibility(View.GONE);
                        }
                    }
                }
            });

    private void handleToolbarView() {
        // show/hide toolbar when scroll
        // only refresh when at top screen (when appbar full)
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            int scrollRange = -1;
            boolean isShowToolbar = false;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    // when show toolbar
                    isShowToolbar = true;
                    ivBackToolbar.setColorFilter(Color.BLACK);
                    ivBackToolbar.setBackgroundTintList(ContextCompat.getColorStateList(GroupDetailActivity.this, R.color.white));
                    tvGroupNameToolbar.setVisibility(View.VISIBLE);
                    ivSearchToolbar.setColorFilter(Color.BLACK);
                    ivSearchToolbar.setBackgroundTintList(ContextCompat.getColorStateList(GroupDetailActivity.this, R.color.white));
                    ivShareToolbar.setColorFilter(Color.BLACK);
                    ivShareToolbar.setBackgroundTintList(ContextCompat.getColorStateList(GroupDetailActivity.this, R.color.white));
                    ivMenuToolbar.setColorFilter(Color.BLACK);
                    ivMenuToolbar.setBackgroundTintList(ContextCompat.getColorStateList(GroupDetailActivity.this, R.color.white));
                } else {
                    if (isShowToolbar) { // use variable isShowToolbar to avoid run function hide toolbar too much
                        // hide toolbar
                        isShowToolbar = false;
                        ivBackToolbar.setColorFilter(Color.WHITE);
                        ivBackToolbar.setBackgroundTintList(ContextCompat.getColorStateList(GroupDetailActivity.this, R.color.color_primary_60));
                        tvGroupNameToolbar.setVisibility(View.GONE);
                        ivSearchToolbar.setColorFilter(Color.WHITE);
                        ivSearchToolbar.setBackgroundTintList(ContextCompat.getColorStateList(GroupDetailActivity.this, R.color.color_primary_60));
                        ivShareToolbar.setColorFilter(Color.WHITE);
                        ivShareToolbar.setBackgroundTintList(ContextCompat.getColorStateList(GroupDetailActivity.this, R.color.color_primary_60));
                        ivMenuToolbar.setColorFilter(Color.WHITE);
                        ivMenuToolbar.setBackgroundTintList(ContextCompat.getColorStateList(GroupDetailActivity.this, R.color.color_primary_60));
                    }

                }
                if (!swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setEnabled(verticalOffset == 0);
                }
            }
        });
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
        getGroupDetail(groupId);
        getMemberGroupRequest(groupId);
        getPosts(groupId);
    }

    private void refresh() {
        // call api
        getGroupDetail(groupId);
        getMemberGroupRequest(groupId);
        getPosts(groupId);
    }

    private void getGroupDetail(int groupId) {
        Call<BaseResponse<Group>> call = groupService.getGroupDetail(groupId);
        call.enqueue(new Callback<BaseResponse<Group>>() {
            @Override
            public void onResponse(Call<BaseResponse<Group>> call, Response<BaseResponse<Group>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<Group> res = response.body();
                    group = res.getData();
                    handleGroupToView();
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
            public void onFailure(Call<BaseResponse<Group>> call, Throwable t) {
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

    private void handleGroupToView() {
        // cover image
        Glide.with(GroupDetailActivity.this)
                .asBitmap()
                .load(group.getCoverImage())
                .error(R.drawable.default_group_cover)
                .into(ivImageCover);
        // name in toolbar
        tvGroupNameToolbar.setText(group.getName());
        // name
        tvGroupName.setText(group.getName());
        // introduction
        if (checkValidValueString(group.getIntroduction())) {
            tvIntroduction.setText(group.getIntroduction());
        }
        // member preview
        Glide.with(GroupDetailActivity.this)
                .asBitmap()
                .load(group.getAdmin().getAvatar())
                .error(R.drawable.default_avatar)
                .into(civAdminAvatar);
        if (group.getPreviewMember() != null) {
            civPreviewMemberAvatar.setVisibility(View.VISIBLE);
            Glide.with(GroupDetailActivity.this)
                    .asBitmap()
                    .load(group.getPreviewMember().getAvatar())
                    .error(R.drawable.default_avatar)
                    .into(civPreviewMemberAvatar);
        } else {
            civPreviewMemberAvatar.setVisibility(View.GONE);
        }
        if (group.getCounts().getMemberCount() > 2) {
            ivViewMoreMember.setVisibility(View.VISIBLE);
        } else {
            ivViewMoreMember.setVisibility(View.GONE);
        }

        // counts
        switch (group.getType()) {
            case 1:
                ivGroupType.setImageResource(R.drawable.ic_public);
                tvGroupType.setText(R.string.group_type_public);
                break;
            case 2:
                ivGroupType.setImageResource(R.drawable.ic_private);
                tvGroupType.setText(R.string.group_type_private);
                break;
        }
        tvGroupMemberCount.setText(String.valueOf(group.getCounts().getMemberCount()));
        tvGroupPostCount.setText(String.valueOf(group.getCounts().getPostCount()));
        // create group time
        tvCreateGroupTime.setText(TimeExtension.formatTimeCreateGroup(group.getCreatedAt()));

        if (Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0) == group.getAdmin().getId()) {
            ivImageCoverCamera.setVisibility(View.VISIBLE);
        } else {
            ivImageCoverCamera.setVisibility(View.GONE);
        }

        // handler member relation with group
        handleMemberRelation();
    }

    private void handleMemberRelation() {
        switch (group.getCurrentUserStatus()) {
            case "joined":
                ivSearchToolbar.setVisibility(View.VISIBLE);
                ivShareToolbar.setVisibility(View.VISIBLE);
                ivMenuToolbar.setVisibility(View.VISIBLE);
                rvPost.setVisibility(View.VISIBLE);
                btnAddPost.setVisibility(View.VISIBLE);
                tvJoinGroup.setVisibility(View.GONE);
                lMemberGroup.setVisibility(View.VISIBLE);
                break;
            case "guest":
                ivSearchToolbar.setVisibility(View.GONE);
                ivShareToolbar.setVisibility(View.GONE);
                ivMenuToolbar.setVisibility(View.GONE);
                rvPost.setVisibility(View.GONE);
                lPostEmpty.setVisibility(View.GONE);
                btnAddPost.setVisibility(View.GONE);
                lMemberGroup.setVisibility(View.GONE);
                tvJoinGroup.setVisibility(View.VISIBLE);
                tvJoinGroup.setText(R.string.join_group);
                tvJoinGroup.setTextColor(ContextCompat.getColorStateList(GroupDetailActivity.this, R.color.white));
                tvJoinGroup.setBackgroundTintList(ContextCompat.getColorStateList(GroupDetailActivity.this, R.color.color_text_highlight));
                break;
            case "requested":
                ivSearchToolbar.setVisibility(View.GONE);
                ivShareToolbar.setVisibility(View.GONE);
                ivMenuToolbar.setVisibility(View.GONE);
                rvPost.setVisibility(View.GONE);
                lPostEmpty.setVisibility(View.GONE);
                btnAddPost.setVisibility(View.GONE);
                lMemberGroup.setVisibility(View.GONE);
                tvJoinGroup.setVisibility(View.VISIBLE);
                tvJoinGroup.setText(R.string.cancel_join_group);
                tvJoinGroup.setTextColor(ContextCompat.getColorStateList(GroupDetailActivity.this, R.color.color_primary_80));
                tvJoinGroup.setBackgroundTintList(ContextCompat.getColorStateList(GroupDetailActivity.this, R.color.color_background_icon_default));
                break;
        }

        if (group.getType() == 1) {
            rvPost.setVisibility(View.VISIBLE);
        }
    }

    private void getPosts(int groupId) {
        Call<BaseResponse<List<Post>>> call = groupService.getGroupPosts(groupId);
        call.enqueue(new Callback<BaseResponse<List<Post>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Post>>> call, Response<BaseResponse<List<Post>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<Post>> res = response.body();
                    posts.clear();
                    posts.addAll(res.getData());
                    postAdapter.notifyDataSetChanged();
                    if (posts.size() == 0) {
                        lPostEmpty.setVisibility(View.VISIBLE);
                    } else {
                        lPostEmpty.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Post>>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
            }
        });
    }

    private void getMemberGroupRequest(int groupId) {
        int userId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0);
        Call<BaseResponse<MemberGroup>> call = groupService.getMemberGroupRequest(groupId, userId);
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

    private void sendOrCancelRequestJoinGroup() {
        switch (group.getCurrentUserStatus()) {
            case "guest":
                sendRequestJoinGroup();
                break;
            case "requested":
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupDetailActivity.this, R.style.AlertDialogTheme);
                builder.setTitle(R.string.cancel_join_group);
                builder.setMessage(getString(R.string.do_you_realy_want_to_cancel_join_group));
                builder.setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        cancelRequestJoinGroup();
                    }
                });
                builder.setNegativeButton("Để sau", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                break;
            default:
                break;
        }
    }

    private void sendRequestJoinGroup() {
        // create a form request join group and send to server
        int userId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0);
        Call<BaseResponse<MemberGroup>> call = groupService.sendRequestJoinGroup(groupId, userId);
        call.enqueue(new Callback<BaseResponse<MemberGroup>>() {
            @Override
            public void onResponse(Call<BaseResponse<MemberGroup>> call, Response<BaseResponse<MemberGroup>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<MemberGroup> res = response.body();
                    MemberGroup memberGroup = res.getData();
                    memberGroupRequestId = memberGroup.getId();

                    if (memberGroup.getMemberGroupStatusCode() == 1) {
                        // = 1 -> group public -> join success
                        // update value
                        group.setCurrentUserStatus("joined");
                        int temp = group.getCounts().getMemberCount() + 1;
                        group.getCounts().setMemberCount(temp);
                        if (group.getPreviewMember() == null) {
                            group.setPreviewMember(memberGroup.getUser());
                        }
                        handleGroupToView();

                        // send a notification
                        int receiverId = userId;
                        String imageUrl = "";
                        if (checkValidValueString(group.getCoverImage())) {
                            imageUrl = group.getCoverImage();
                        }
                        int type = 4;
                        String content = "Yêu cầu tham gia nhóm " + group.getName() + " đã được chấp nhận";
                        String url = "group/" + groupId;
                        sendNotification(receiverId, imageUrl, type, content, url);
                    } else {
                        // = 0 -> group private -> wait admin accept
                        // update value
                        group.setCurrentUserStatus("requested");
                        handleMemberRelation();
                        Toast.makeText(GroupDetailActivity.this, R.string.create_request_join_group_success, Toast.LENGTH_SHORT).show();

                        // send a notification
                        int receiverId = group.getAdmin().getId();
                        String imageUrl = "";
                        if (!Hawk.get(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, "").isEmpty()) {
                            imageUrl = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, "");
                        }
                        int type = 4;
                        String content = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_NAME_KEY, "") + " đã gửi yêu cầu tham gia nhóm " + group.getName();
                        String url = "request_join_group/" + groupId;
                        sendNotification(receiverId, imageUrl, type, content, url);
                    }
                } else {
                    Toast.makeText(GroupDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<MemberGroup>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                Toast.makeText(GroupDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelRequestJoinGroup() {
        Call<BaseResponse<String>> call = groupService.deleteMemberGroupRequest(memberGroupRequestId);
        call.enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful()) {
                    memberGroupRequestId = -1;
                    group.setCurrentUserStatus("guest");
                    handleMemberRelation();
                    Toast.makeText(GroupDetailActivity.this, R.string.delete_request_join_group_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GroupDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                Toast.makeText(GroupDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showShareGroupBottomSheetDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(GroupDetailActivity.this, R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_share_group);

        ConstraintLayout lShareCopyLinkGroup = bottomSheetDialog.findViewById(R.id.l_share_copy_link_group);
        ConstraintLayout lShareWithFacebookGroup = bottomSheetDialog.findViewById(R.id.l_share_with_facebook_group);
        ConstraintLayout lShareWithMessengerGroup = bottomSheetDialog.findViewById(R.id.l_share_with_messenger_group);
        ConstraintLayout lShareOtherGroup = bottomSheetDialog.findViewById(R.id.l_share_other_group);

        if (lShareCopyLinkGroup != null && lShareWithFacebookGroup != null && lShareWithMessengerGroup != null && lShareOtherGroup != null) {
            lShareCopyLinkGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    String link = getString(R.string.share_link_base) + "/group/" + group.getId();
                    ContextExtension.copyToClipboard(GroupDetailActivity.this, link);
                    Toast.makeText(GroupDetailActivity.this, R.string.link_copied, Toast.LENGTH_SHORT).show();
                }
            });
            lShareWithFacebookGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    String link = getString(R.string.share_link_base) + "/group/" + group.getId();
                    String applicationPackage = "com.facebook.katana"; // Facebook App package
                    Intent intent = GroupDetailActivity.this.getPackageManager().getLaunchIntentForPackage(applicationPackage);
                    if (intent != null) {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, link);
                        shareIntent.setPackage(applicationPackage);
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with_facebook)));
                    } else {
                        Toast.makeText(GroupDetailActivity.this, R.string.no_facebook_install, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            lShareWithMessengerGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    String link = getString(R.string.share_link_base) + "/group/" + group.getId();
                    String applicationPackage = "com.facebook.orca"; // Message App package
                    Intent intent = GroupDetailActivity.this.getPackageManager().getLaunchIntentForPackage(applicationPackage);
                    if (intent != null) {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, link);
                        shareIntent.setPackage(applicationPackage);
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with_messenger)));
                    } else {
                        Toast.makeText(GroupDetailActivity.this, R.string.no_messager_install, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            lShareOtherGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    String link = getString(R.string.share_link_base) + "/group/" + group.getId();
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, link);
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.choose_application)));
                }
            });
        }

        bottomSheetDialog.show();
    }

    private void showChangeCoverBottomSheetDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(GroupDetailActivity.this, R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_cover_change);

        ConstraintLayout lViewPhoto = bottomSheetDialog.findViewById(R.id.l_view_photo);
        ConstraintLayout lTakePhoto = bottomSheetDialog.findViewById(R.id.l_take_photo);
        ConstraintLayout lChangePhoto = bottomSheetDialog.findViewById(R.id.l_change_photo);

        if (lViewPhoto != null && lTakePhoto != null && lChangePhoto != null) {
            if (checkValidValueString(group.getCoverImage())) {
                lViewPhoto.setVisibility(View.VISIBLE);
            } else {
                lViewPhoto.setVisibility(View.GONE);
            }

            lViewPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    if (!group.getCoverImage().isEmpty()) {
                        Intent viewPhotoIntent = new Intent(GroupDetailActivity.this, MediaViewerActivity.class);
                        viewPhotoIntent.putExtra("media_url", group.getCoverImage());
                        viewPhotoIntent.putExtra("media_type", FileExtension.MEDIA_IMAGE_TYPE);
                        startActivity(viewPhotoIntent);
                    }
                }
            });

            lTakePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    RxPermissions rxPermissions = new RxPermissions(GroupDetailActivity.this);
                    rxPermissions.request(Manifest.permission.CAMERA)
                            .subscribe(granted -> {
                                if (granted) {
                                    try {
                                        // create a temp file to save capture photo
                                        File capturePhotoFile = FileExtension.getPathCapturePhoto(GroupDetailActivity.this);
                                        capturePhotoCoverPath = capturePhotoFile.getAbsolutePath();
                                        MediaPicker.openCameraForCapturePhoto(GroupDetailActivity.this, REQUEST_CODE_CAPTURE_PHOTO_COVER, capturePhotoFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(GroupDetailActivity.this, R.string.permission_request_denied, Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });

            lChangePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    RxPermissions rxPermissions = new RxPermissions(GroupDetailActivity.this);
                    rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .subscribe(granted -> {
                                if (granted) {
                                    MediaPicker.openCoverPicker(GroupDetailActivity.this, REQUEST_CODE_CHOOSE_IMAGE_COVER, 1, false);
                                } else {
                                    Toast.makeText(GroupDetailActivity.this, R.string.permission_request_denied, Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        }
        bottomSheetDialog.show();
    }

    private void createOrUpdateReactPost(int reactType, int position) {
        int postId = posts.get(position).getId();
        int userId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0);
        Call<BaseResponse<ReactUser>> call = postService.reactPost(postId, userId, reactType);
        call.enqueue(new Callback<BaseResponse<ReactUser>>() {
            @Override
            public void onResponse(Call<BaseResponse<ReactUser>> call, Response<BaseResponse<ReactUser>> response) {
                // no action
            }

            @Override
            public void onFailure(Call<BaseResponse<ReactUser>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private void showSharePost(View lContentToShare, int position) {
        Post post = posts.get(position);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(GroupDetailActivity.this, R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_share_post);

        ConstraintLayout lShareWritePost = bottomSheetDialog.findViewById(R.id.l_share_write_post);
        ConstraintLayout lShareCopyLinkPost = bottomSheetDialog.findViewById(R.id.l_share_copy_link_post);
        ConstraintLayout lShareWithFacebookPost = bottomSheetDialog.findViewById(R.id.l_share_with_facebook_post);
        ConstraintLayout lShareWithMessengerPost = bottomSheetDialog.findViewById(R.id.l_share_with_messenger_post);
        ConstraintLayout lShareOtherPost = bottomSheetDialog.findViewById(R.id.l_share_other_post);

        if (lShareWritePost != null && lShareCopyLinkPost != null && lShareWithFacebookPost != null && lShareWithMessengerPost != null && lShareOtherPost != null) {
            lShareWritePost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    // open write post
                    File file = getShareViewFilePath(lContentToShare, post);
                    if (file != null) {
                        Intent shareIntent = new Intent(GroupDetailActivity.this, PostCreatorActivity.class);
                        shareIntent.putExtra("photo_share", file.getAbsoluteFile().toString());
                        startActivity(shareIntent);
                    }
                }
            });
            lShareCopyLinkPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    String link = getString(R.string.share_link_base) + "/post/" + post.getId();
                    ContextExtension.copyToClipboard(GroupDetailActivity.this, link);
                    Toast.makeText(GroupDetailActivity.this, R.string.link_copied, Toast.LENGTH_SHORT).show();
                }
            });
            lShareWithFacebookPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    String applicationPackage = "com.facebook.katana"; // Facebook App package
                    Intent intent = GroupDetailActivity.this.getPackageManager().getLaunchIntentForPackage(applicationPackage);
                    if (intent != null) {
                        File file = getShareViewFilePath(lContentToShare, post);
                        if (file != null) {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("image/*");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                            shareIntent.setPackage(applicationPackage);
                            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with_facebook)));
                        }
                    } else {
                        Toast.makeText(GroupDetailActivity.this, R.string.no_facebook_install, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            lShareWithMessengerPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    String applicationPackage = "com.facebook.orca"; // Message App package
                    Intent intent = GroupDetailActivity.this.getPackageManager().getLaunchIntentForPackage(applicationPackage);
                    if (intent != null) {
                        File file = getShareViewFilePath(lContentToShare, post);
                        if (file != null) {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("image/*");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                            shareIntent.setPackage(applicationPackage);
                            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with_messenger)));
                        }
                    } else {
                        Toast.makeText(GroupDetailActivity.this, R.string.no_messager_install, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            lShareOtherPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    File file = getShareViewFilePath(lContentToShare, post);
                    if (file != null) {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("image/*");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.choose_application)));
                    }
                }
            });
        }

        bottomSheetDialog.show();
    }

    private File getShareViewFilePath(View lContentToShare, Post post) {
        ConstraintLayout lContent = (ConstraintLayout) lContentToShare;
        if (lContent != null) {
            try {
                if (post.getMedia().getType().equals(FileExtension.MEDIA_VIDEO_TYPE)) {
                    lContent.findViewById(R.id.iv_video_thumbnail).setVisibility(View.VISIBLE);
                }
                File file = getImageFromLayout(lContent);
                if (post.getMedia().getType().equals(FileExtension.MEDIA_VIDEO_TYPE)) {
                    lContent.findViewById(R.id.iv_video_thumbnail).setVisibility(View.GONE);
                }
                return file;
            } catch (IOException e) {
                Toast.makeText(GroupDetailActivity.this, R.string.error_generate_image_from_layout, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private void showMenuPost(int position) {
        Post post = posts.get(position);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(GroupDetailActivity.this, R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_menu_post);

        ConstraintLayout lCopyContentPost = bottomSheetDialog.findViewById(R.id.l_copy_content_post);
        ConstraintLayout lCopyLinkPost = bottomSheetDialog.findViewById(R.id.l_copy_link_post);
        ConstraintLayout lEditPost = bottomSheetDialog.findViewById(R.id.l_edit_post);
        ConstraintLayout lDeletePost = bottomSheetDialog.findViewById(R.id.l_delete_post);
        ConstraintLayout lReportPost = bottomSheetDialog.findViewById(R.id.l_repost_post);

        if (lCopyContentPost != null && lCopyLinkPost != null && lEditPost != null && lDeletePost != null && lReportPost != null) {
            if (post.getUser().getId() != Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0)) {
                lEditPost.setVisibility(View.GONE);
                lDeletePost.setVisibility(View.GONE);
            } else {
                lReportPost.setVisibility(View.GONE);
            }

            lCopyContentPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    ContextExtension.copyToClipboard(GroupDetailActivity.this, post.getCaption());
                    Toast.makeText(GroupDetailActivity.this, R.string.content_copied, Toast.LENGTH_SHORT).show();
                }
            });
            lCopyLinkPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    String link = getString(R.string.share_link_base) + "/post/" + post.getId();
                    ContextExtension.copyToClipboard(GroupDetailActivity.this, link);
                    Toast.makeText(GroupDetailActivity.this, R.string.link_copied, Toast.LENGTH_SHORT).show();
                }
            });
            lEditPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    // open fragment edit post

                }
            });
            lDeletePost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    // open dialog confirm delete
                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupDetailActivity.this, R.style.AlertDialogTheme);
                    builder.setTitle(R.string.menu_delete_post);
                    builder.setMessage(R.string.do_you_realy_want_to_delete_this_post);
                    builder.setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            deletePost(position);
                        }
                    });
                    builder.setNegativeButton(R.string.not_agree, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
            });
            lReportPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    // open activity report post
                    Toast.makeText(GroupDetailActivity.this, R.string.report_post_success, Toast.LENGTH_SHORT).show();
                }
            });
        }

        bottomSheetDialog.show();
    }

    private void deletePost(int position) {
        int postId = posts.get(position).getId();
        Call<BaseResponse<String>> call = postService.deletePost(postId);
        call.enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful()) {
                    posts.remove(position);
                    postAdapter.notifyItemRemoved(position);
                } else {
                    Toast.makeText(GroupDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                // error
                call.cancel();
                Toast.makeText(GroupDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
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