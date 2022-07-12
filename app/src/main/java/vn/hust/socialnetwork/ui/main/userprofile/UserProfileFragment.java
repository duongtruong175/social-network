package vn.hust.socialnetwork.ui.main.userprofile;

import static vn.hust.socialnetwork.utils.ContextExtension.getImageFromLayout;
import static vn.hust.socialnetwork.utils.StringExtension.checkValidValueString;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.orhanobut.hawk.Hawk;
import com.tbruyelle.rxpermissions3.RxPermissions;
import com.zhihu.matisse.Matisse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.common.style.CustomTypefaceSpan;
import vn.hust.socialnetwork.common.view.reactuser.ReactUserFragment;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.media.Media;
import vn.hust.socialnetwork.models.post.Post;
import vn.hust.socialnetwork.models.post.ReactUser;
import vn.hust.socialnetwork.models.user.CountsUser;
import vn.hust.socialnetwork.models.user.Relation;
import vn.hust.socialnetwork.models.user.User;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.PostService;
import vn.hust.socialnetwork.network.UserProfileService;
import vn.hust.socialnetwork.ui.album.PhotoAlbumActivity;
import vn.hust.socialnetwork.ui.album.VideoAlbumActivity;
import vn.hust.socialnetwork.ui.authentication.login.LoginFragment;
import vn.hust.socialnetwork.ui.main.group.GroupFragment;
import vn.hust.socialnetwork.ui.main.userprofile.adapters.FriendAdapter;
import vn.hust.socialnetwork.ui.main.userprofile.adapters.OnFriendListener;
import vn.hust.socialnetwork.ui.main.userprofile.adapters.OnPostListener;
import vn.hust.socialnetwork.ui.main.userprofile.adapters.PostAdapter;
import vn.hust.socialnetwork.ui.main.userprofile.crop.CropUserAvatarActivity;
import vn.hust.socialnetwork.ui.main.userprofile.crop.CropUserCoverActivity;
import vn.hust.socialnetwork.ui.main.userprofile.edit.EditUserProfileActivity;
import vn.hust.socialnetwork.ui.mediaviewer.MediaViewerActivity;
import vn.hust.socialnetwork.ui.menuprofile.MenuProfileActivity;
import vn.hust.socialnetwork.ui.postcreator.PostCreatorActivity;
import vn.hust.socialnetwork.ui.postdetail.PostDetailActivity;
import vn.hust.socialnetwork.ui.relation.RelationActivity;
import vn.hust.socialnetwork.ui.userdetail.UserDetailActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.ContextExtension;
import vn.hust.socialnetwork.utils.FileExtension;
import vn.hust.socialnetwork.utils.MediaPicker;
import vn.hust.socialnetwork.utils.TimeExtension;

public class UserProfileFragment extends Fragment {

    private static final int REQUEST_CODE_CHOOSE_IMAGE_AVATAR = 104;
    private static final int REQUEST_CODE_CHOOSE_IMAGE_COVER = 105;
    private static final int REQUEST_CODE_CAPTURE_PHOTO_AVATAR = 106;
    private static final int REQUEST_CODE_CAPTURE_PHOTO_COVER = 107;

    private String capturePhotoAvatarPath, capturePhotoCoverPath; // file path for capturing photo

    private UserProfileService userProfileService;
    private PostService postService;

    private User user;
    private List<Post> posts;
    private PostAdapter postAdapter;
    private List<Relation> friends;
    private FriendAdapter friendAdapter;

    private LinearProgressIndicator pbLoading;
    private ConstraintLayout lError, lPostEmpty, lZodiacContainer;
    private CoordinatorLayout lMain;
    private ShimmerFrameLayout lShimmer;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AppBarLayout appBarLayout;
    private AppCompatTextView tvUserNameToolbar, tvUserName, tvZodiac, tvDescription, tvFollowerCount, tvPostCount, tvReactCount, tvFriendCount, tvViewAllFriend;
    private LinearLayoutCompat lEditUserProfile, lPhotoUserProfile, lVideoUserProfile, lNewPost;
    private AppCompatButton btnRefresh;
    private RelativeLayout lJobContainer, lEducationContainer, lPhoneContainer, lCurrentResidenceContainer, lHometownContainer, lRelationshipContainer, lWebsiteContainer;
    private AppCompatTextView tvJob, tvEducation, tvPhone, tvCurrentResidence, tvHometown, tvGender, tvRelationship, tvWebsite, tvJoinIn;
    private CircleImageView civImageAvatarToolbar, civImageAvatar, civMyAvatar;
    private AppCompatImageView ivMenuToolbar, ivImageCover, ivImageAvatarCamera, ivImageCoverCamera, ivNewPostWithPhoto, ivZodiac, ivGender;
    private RecyclerView rvFriend, rvPost;

    public UserProfileFragment() {
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
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        // api
        userProfileService = ApiClient.getClient().create(UserProfileService.class);
        postService = ApiClient.getClient().create(PostService.class);

        // binding
        lMain = view.findViewById(R.id.l_main);
        lShimmer = view.findViewById(R.id.l_shimmer);
        lError = view.findViewById(R.id.l_error);
        lPostEmpty = view.findViewById(R.id.l_post_empty);
        pbLoading = view.findViewById(R.id.pb_loading);
        swipeRefreshLayout = view.findViewById(R.id.l_swipe_refresh);
        appBarLayout = view.findViewById(R.id.ab_main_user_profile);
        tvUserNameToolbar = view.findViewById(R.id.tv_user_name_toolbar);
        civImageAvatarToolbar = view.findViewById(R.id.civ_image_avatar_toolbar);
        ivMenuToolbar = view.findViewById(R.id.iv_menu_toolbar);
        civImageAvatar = view.findViewById(R.id.civ_image_avatar);
        ivImageAvatarCamera = view.findViewById(R.id.iv_image_avatar_camera);
        civMyAvatar = view.findViewById(R.id.civ_my_avatar);
        ivImageCover = view.findViewById(R.id.iv_image_cover);
        ivImageCoverCamera = view.findViewById(R.id.iv_image_cover_camera);
        ivNewPostWithPhoto = view.findViewById(R.id.iv_new_post_with_photo);
        tvUserName = view.findViewById(R.id.tv_user_name);
        lZodiacContainer = view.findViewById(R.id.l_zodiac_container);
        tvZodiac = view.findViewById(R.id.tv_zodiac);
        ivZodiac = view.findViewById(R.id.iv_zodiac);
        tvDescription = view.findViewById(R.id.tv_description);
        tvFollowerCount = view.findViewById(R.id.tv_follower_count);
        tvPostCount = view.findViewById(R.id.tv_post_count);
        tvReactCount = view.findViewById(R.id.tv_react_count);
        tvFriendCount = view.findViewById(R.id.tv_friend_count);
        tvViewAllFriend = view.findViewById(R.id.tv_view_all_friend);
        lEditUserProfile = view.findViewById(R.id.l_edit_user_profile);
        lPhotoUserProfile = view.findViewById(R.id.l_photo_user_profile);
        lVideoUserProfile = view.findViewById(R.id.l_video_user_profile);
        lNewPost = view.findViewById(R.id.l_new_post);
        lJobContainer = view.findViewById(R.id.l_job_container);
        lEducationContainer = view.findViewById(R.id.l_education_container);
        lPhoneContainer = view.findViewById(R.id.l_phone_container);
        lCurrentResidenceContainer = view.findViewById(R.id.l_current_residence_container);
        lHometownContainer = view.findViewById(R.id.l_hometown_container);
        lRelationshipContainer = view.findViewById(R.id.l_relationship_container);
        lWebsiteContainer = view.findViewById(R.id.l_website_container);
        tvJob = view.findViewById(R.id.tv_job);
        tvEducation = view.findViewById(R.id.tv_education);
        tvPhone = view.findViewById(R.id.tv_phone);
        tvCurrentResidence = view.findViewById(R.id.tv_current_residence);
        tvHometown = view.findViewById(R.id.tv_hometown);
        tvGender = view.findViewById(R.id.tv_gender);
        ivGender = view.findViewById(R.id.iv_gender);
        tvRelationship = view.findViewById(R.id.tv_relationship);
        tvWebsite = view.findViewById(R.id.tv_website);
        tvJoinIn = view.findViewById(R.id.tv_join_in);
        rvFriend = view.findViewById(R.id.rv_friend);
        rvPost = view.findViewById(R.id.rv_post);
        btnRefresh = view.findViewById(R.id.btn_refresh);

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

        ivMenuToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MenuProfileActivity.class);
                intent.putExtra("user", user);
                openMenuActivityResultLauncher.launch(intent);
            }
        });

        // change avatar
        civImageAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeAvatarBottomSheetDialog();
            }
        });
        ivImageAvatarCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeAvatarBottomSheetDialog();
            }
        });

        // change image cover
        ivImageCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeCoverBottomSheetDialog();
            }
        });
        ivImageCoverCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeCoverBottomSheetDialog();
            }
        });

        // navigate to edit user profile
        lEditUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EditUserProfileActivity.class);
                intent.putExtra("user", user);
                openUpdateUserProfileActivityResultLauncher.launch(intent);
            }
        });

        // view all photos
        lPhotoUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PhotoAlbumActivity.class);
                intent.putExtra("user_id", user.getId());
                startActivity(intent);
            }
        });

        // view all videos
        lVideoUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), VideoAlbumActivity.class);
                intent.putExtra("user_id", user.getId());
                startActivity(intent);
            }
        });

        // view all relation
        tvViewAllFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RelationActivity.class);
                startActivity(intent);
            }
        });

        lNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PostCreatorActivity.class);
                openPostCreatorActivityResultLauncher.launch(intent);
            }
        });

        ivNewPostWithPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PostCreatorActivity.class);
                intent.putExtra("open_photo", true);
                openPostCreatorActivityResultLauncher.launch(intent);
            }
        });

        // init all
        capturePhotoAvatarPath = "";
        capturePhotoCoverPath = "";

        friends = new ArrayList<>();
        friendAdapter = new FriendAdapter(getContext(), friends, new OnFriendListener() {
            @Override
            public void onItemClick(int position) {
                onItemFriendClick(position);
            }
        });
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvFriend.setLayoutManager(layoutManager);
        rvFriend.setAdapter(friendAdapter);

        posts = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), posts, new OnPostListener() {
            @Override
            public void onUserPostClick(int position) {
                // open user detail
                Intent intent = new Intent(getActivity(), UserDetailActivity.class);
                intent.putExtra("user_id", posts.get(position).getUser().getId());
                startActivity(intent);
            }

            @Override
            public void onGroupPostClick(int position) {
                // open group detail
            }

            @Override
            public void onMenuItemClick(int position) {
                showMenuPost(position);
            }

            @Override
            public void onContentLongClick(int position) {
                Dialog dialog = new Dialog(getContext());
                WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
                layoutParams.gravity = Gravity.CENTER | Gravity.START;
                layoutParams.x = 100;
                dialog.setContentView(R.layout.dialog_copy_content_post);
                dialog.setCancelable(true);
                dialog.findViewById(R.id.tv_copy_content).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        ContextExtension.copyToClipboard(requireContext(), posts.get(position).getCaption());
                        Toast.makeText(getContext(), R.string.content_copied, Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show();
            }

            @Override
            public void onMediaItemClick(int position) {
                Media media = posts.get(position).getMedia();
                if (media != null) {
                    Intent viewPhotoIntent = new Intent(getActivity(), MediaViewerActivity.class);
                    viewPhotoIntent.putExtra("media_url", media.getSrc());
                    viewPhotoIntent.putExtra("media_type", media.getType());
                    startActivity(viewPhotoIntent);
                }
            }

            @Override
            public void onReactCountClick(int position) {
                Post post = posts.get(position);
                BottomSheetDialogFragment bottomSheetDialogFragment = ReactUserFragment.newInstance(post.getId(), "post");
                bottomSheetDialogFragment.show(getParentFragmentManager(), bottomSheetDialogFragment.getTag());
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
                Intent intent = new Intent(getActivity(), PostDetailActivity.class);
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
                Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                intent.putExtra("post_id", post.getId());
                intent.putExtra("is_show_comment_input", false);
                startActivity(intent);
            }
        });
        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(getContext());
        rvPost.setLayoutManager(layoutManager2);
        rvPost.setAdapter(postAdapter);
        /*
        // error because RecyclerView in NestedScrollView -> findFirstVisibleItemPosition not working
        rvPost.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int firstVisibleItem = manager.findFirstVisibleItemPosition();
                    if (firstVisibleItem != -1) {
                        postAdapter.playIndexThenPausePreviousPlayer(firstVisibleItem);
                    }
                }
            }
        });
         */

        // call api to get all data
        getData();

        return view;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE_IMAGE_AVATAR && resultCode == Activity.RESULT_OK && data != null) {
            // Put selected image to Crop Avatar Activity
            List<String> s = Matisse.obtainPathResult(data);
            String s1 = Matisse.obtainPathResult(data).get(0);
            Intent intent = new Intent(getActivity(), CropUserAvatarActivity.class);
            intent.putExtra("image_path", Matisse.obtainPathResult(data).get(0));
            cropAvatarActivityResultLauncher.launch(intent);
        }
        if (requestCode == REQUEST_CODE_CAPTURE_PHOTO_AVATAR && resultCode == Activity.RESULT_OK && !capturePhotoAvatarPath.isEmpty()) {
            // Put captured image to Crop Avatar Activity
            Intent intent = new Intent(getActivity(), CropUserAvatarActivity.class);
            intent.putExtra("image_path", capturePhotoAvatarPath);
            cropAvatarActivityResultLauncher.launch(intent);
        }
        if (requestCode == REQUEST_CODE_CHOOSE_IMAGE_COVER && resultCode == Activity.RESULT_OK && data != null) {
            // Put selected image to Crop Cover Activity
            List<String> s = Matisse.obtainPathResult(data);
            String s1 = Matisse.obtainPathResult(data).get(0);
            Intent intent = new Intent(getActivity(), CropUserCoverActivity.class);
            intent.putExtra("image_path", Matisse.obtainPathResult(data).get(0));
            intent.putExtra("image_avatar", user.getAvatar());
            cropCoverActivityResultLauncher.launch(intent);
        }
        if (requestCode == REQUEST_CODE_CAPTURE_PHOTO_COVER && resultCode == Activity.RESULT_OK && !capturePhotoCoverPath.isEmpty()) {
            // Put captured image to Crop Cover Activity
            Intent intent = new Intent(getActivity(), CropUserCoverActivity.class);
            intent.putExtra("image_path", capturePhotoCoverPath);
            intent.putExtra("image_avatar", user.getAvatar());
            cropCoverActivityResultLauncher.launch(intent);
        }
    }

    // ActivityResultLauncher for crop avatar activity
    ActivityResultLauncher<Intent> cropAvatarActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Get image from result
                        Intent data = result.getData();
                        if (data != null) {
                            // update value
                            String newAvatar = data.getStringExtra("new_avatar");
                            Post newPost = data.getParcelableExtra("new_post");
                            user.setAvatar(newAvatar);
                            Hawk.put(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, newAvatar);
                            Glide.with(UserProfileFragment.this)
                                    .clear(civImageAvatar);
                            Glide.with(UserProfileFragment.this)
                                    .asBitmap()
                                    .load(newAvatar)
                                    .into(civImageAvatar);
                            posts.add(0, newPost);
                            postAdapter.notifyItemInserted(0);
                            lPostEmpty.setVisibility(View.GONE);
                        }
                    }
                }
            });

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
                            Post newPost = data.getParcelableExtra("new_post");
                            user.setCoverImage(newCoverImage);
                            Glide.with(UserProfileFragment.this)
                                    .clear(ivImageCover);
                            Glide.with(UserProfileFragment.this)
                                    .asBitmap()
                                    .load(newCoverImage)
                                    .into(ivImageCover);
                            posts.add(0, newPost);
                            postAdapter.notifyItemInserted(0);
                            lPostEmpty.setVisibility(View.GONE);
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
                            String destination = data.getStringExtra("navigation_to");
                            if (destination != null && destination.equals("group")) {
                                // navigation to group fragment
                                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
                                if (bottomNavigationView != null) {
                                    bottomNavigationView.setSelectedItemId(R.id.groupFragment);
                                }
                            }
                        }
                    }
                }
            });

    // ActivityResultLauncher for update user profile
    ActivityResultLauncher<Intent> openUpdateUserProfileActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Get data from result
                        Intent data = result.getData();
                        if (data != null) {
                            // update value
                            user = data.getParcelableExtra("updated_user");
                            handleUserToView();
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
                    ivMenuToolbar.setColorFilter(Color.BLACK);
                    civImageAvatarToolbar.setVisibility(View.VISIBLE);
                    tvUserNameToolbar.setVisibility(View.VISIBLE);
                } else {
                    if (isShowToolbar) { // use variable isShowToolbar to avoid run function hide toolbar too much
                        // hide toolbar
                        isShowToolbar = false;
                        ivMenuToolbar.setColorFilter(Color.WHITE);
                        civImageAvatarToolbar.setVisibility(View.GONE);
                        tvUserNameToolbar.setVisibility(View.GONE);
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
        int myUserId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY);
        getUserDetail(myUserId);
        getFriends(myUserId);
        getPosts(myUserId);
    }

    private void refresh() {
        // call api
        int myUserId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY);
        getUserDetail(myUserId);
        getFriends(myUserId);
        getPosts(myUserId);
    }

    private void getUserDetail(int userId) {
        Call<BaseResponse<User>> call = userProfileService.getUserProfileById(userId);
        call.enqueue(new Callback<BaseResponse<User>>() {
            @Override
            public void onResponse(Call<BaseResponse<User>> call, Response<BaseResponse<User>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<User> res = response.body();
                    user = res.getData();
                    handleUserToView();
                    lMain.setVisibility(View.VISIBLE);
                    lShimmer.stopShimmer();
                    lShimmer.setVisibility(View.GONE);
                    lError.setVisibility(View.GONE);
                } else {
                    lMain.setVisibility(View.GONE);
                    lShimmer.stopShimmer();
                    lShimmer.setVisibility(View.GONE);
                    lError.setVisibility(View.VISIBLE);
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<BaseResponse<User>> call, Throwable t) {
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

    private void handleUserToView() {
        // cover image
        Glide.with(getActivity())
                .asBitmap()
                .load(user.getCoverImage())
                .error(R.drawable.default_cover)
                .into(ivImageCover);
        // avatar
        Glide.with(getActivity())
                .asBitmap()
                .load(user.getAvatar())
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(civImageAvatar);
        // avatar in add new post
        Glide.with(getActivity())
                .asBitmap()
                .load(Hawk.get(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, ""))
                .error(R.drawable.default_avatar)
                .into(civMyAvatar);
        // avatar in toolbar
        Glide.with(getActivity())
                .asBitmap()
                .load(user.getAvatar())
                .error(R.drawable.default_avatar)
                .into(civImageAvatarToolbar);
        // name in toolbar
        tvUserNameToolbar.setText(user.getName());
        // name
        tvUserName.setText(user.getName());
        // zodiac
        String zodiac = TimeExtension.getZodiac(user.getBirthday());
        switch (zodiac) {
            case "aquarius":
                ivZodiac.setImageResource(R.drawable.ic_aquarius);
                tvZodiac.setText(R.string.aquarius);
                break;
            case "pisces":
                ivZodiac.setImageResource(R.drawable.ic_pisces);
                tvZodiac.setText(R.string.pisces);
                break;
            case "aries":
                ivZodiac.setImageResource(R.drawable.ic_aries);
                tvZodiac.setText(R.string.aries);
                break;
            case "taurus":
                ivZodiac.setImageResource(R.drawable.ic_taurus);
                tvZodiac.setText(R.string.taurus);
                break;
            case "gemini":
                ivZodiac.setImageResource(R.drawable.ic_gemini);
                tvZodiac.setText(R.string.gemini);
                break;
            case "cancer":
                ivZodiac.setImageResource(R.drawable.ic_cancer);
                tvZodiac.setText(R.string.cancer);
                break;
            case "leo":
                ivZodiac.setImageResource(R.drawable.ic_leo);
                tvZodiac.setText(R.string.leo);
                break;
            case "virgo":
                ivZodiac.setImageResource(R.drawable.ic_virgo);
                tvZodiac.setText(R.string.virgo);
                break;
            case "libra":
                ivZodiac.setImageResource(R.drawable.ic_libra);
                tvZodiac.setText(R.string.libra);
                break;
            case "scorpio":
                ivZodiac.setImageResource(R.drawable.ic_scorpio);
                tvZodiac.setText(R.string.scorpio);
                break;
            case "sagittarius":
                ivZodiac.setImageResource(R.drawable.ic_sagittarius);
                tvZodiac.setText(R.string.sagittarius);
                break;
            case "capricorn":
                ivZodiac.setImageResource(R.drawable.ic_capricorn);
                tvZodiac.setText(R.string.capricorn);
                break;
            default:
                lZodiacContainer.setVisibility(View.GONE);
        }
        // introduction (description)
        hideOrShowData(tvDescription, tvDescription, user.getShortDescription());
        // counts (followers, posts, reacts, friends)
        CountsUser counts = user.getCounts();
        tvFollowerCount.setText(String.valueOf(counts.getFollowCount()));
        tvPostCount.setText(String.valueOf(counts.getPostCount()));
        tvReactCount.setText(String.valueOf(counts.getReactCount()));
        tvFriendCount.setText(counts.getFriendCount() + " " + getString(R.string.friend_profile));
        // job
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.f_roboto_medium);
        if (user.getJob() != null) {
            lJobContainer.setVisibility(View.VISIBLE);
            SpannableStringBuilder textJob = new SpannableStringBuilder();
            textJob.append(user.getJob().getPosition());
            textJob.append(" ");
            textJob.append(getString(R.string.at));
            textJob.append(" ");
            int i = textJob.length();
            textJob.append(user.getJob().getCompany());
            textJob.setSpan(new CustomTypefaceSpan(typeface), i, textJob.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvJob.setText(textJob);
        } else {
            lJobContainer.setVisibility(View.GONE);
        }
        // education
        if (user.getEducation() != null) {
            lEducationContainer.setVisibility(View.VISIBLE);
            SpannableStringBuilder textEducation = new SpannableStringBuilder();
            textEducation.append(getString(R.string.study));
            textEducation.append(" ");
            textEducation.append(user.getEducation().getMajors());
            textEducation.append(" ");
            textEducation.append(getString(R.string.at));
            textEducation.append(" ");
            int i = textEducation.length();
            textEducation.append(user.getEducation().getSchool());
            textEducation.setSpan(new CustomTypefaceSpan(typeface), i, textEducation.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvEducation.setText(textEducation);
        } else {
            lEducationContainer.setVisibility(View.GONE);
        }
        // phone
        hideOrShowData(lPhoneContainer, tvPhone, user.getPhone());
        // current residence
        hideOrShowData(lCurrentResidenceContainer, tvCurrentResidence, user.getCurrentResidence());
        // hometown
        hideOrShowData(lHometownContainer, tvHometown, user.getHometown());
        // gender
        String gender = user.getGender();
        tvGender.setText(gender);
        if (gender.equals(getString(R.string.male))) {
            ivGender.setImageResource(R.drawable.ic_male_gender);
        } else if (gender.equals(getString(R.string.female))) {
            ivGender.setImageResource(R.drawable.ic_female_gender);
        } else {
            ivGender.setImageResource(R.drawable.ic_other_gender);
        }
        // relationship
        hideOrShowData(lRelationshipContainer, tvRelationship, user.getRelationshipStatus());
        // website
        hideOrShowData(lWebsiteContainer, tvWebsite, user.getWebsite());
        // date join in
        tvJoinIn.setText(TimeExtension.formatTimeJoinIn(user.getCreatedAt()));
    }

    private void hideOrShowData(View lContainer, AppCompatTextView tv, String value) {
        if (value != null && !value.isEmpty()) {
            lContainer.setVisibility(View.VISIBLE);
            tv.setText(value);
        } else {
            lContainer.setVisibility(View.GONE);
        }
    }

    private void getFriends(int userId) {
        Call<BaseResponse<List<Relation>>> call = userProfileService.getTopFriends(userId);
        call.enqueue(new Callback<BaseResponse<List<Relation>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Relation>>> call, Response<BaseResponse<List<Relation>>> response) {
                if (response.isSuccessful()) {
                    if (response.isSuccessful()) {
                        BaseResponse<List<Relation>> res = response.body();
                        friends.clear();
                        friends.addAll(res.getData());
                        friendAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Relation>>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
            }
        });
    }

    private void onItemFriendClick(int position) {
        // navigate to activity user detail
        int user_id = friends.get(position).getId();
        Intent intent = new Intent(getActivity(), UserDetailActivity.class);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }

    private void getPosts(int userId) {
        Call<BaseResponse<List<Post>>> call = userProfileService.getPosts(userId);
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

    private void showChangeAvatarBottomSheetDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_avatar_change);

        ConstraintLayout lViewPhoto = bottomSheetDialog.findViewById(R.id.l_view_photo);
        ConstraintLayout lTakePhoto = bottomSheetDialog.findViewById(R.id.l_take_photo);
        ConstraintLayout lChangePhoto = bottomSheetDialog.findViewById(R.id.l_change_photo);

        if (lViewPhoto != null && lTakePhoto != null && lChangePhoto != null) {
            if (checkValidValueString(user.getAvatar())) {
                lViewPhoto.setVisibility(View.VISIBLE);
            } else {
                lViewPhoto.setVisibility(View.GONE);
            }

            lViewPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    if (!user.getAvatar().isEmpty()) {
                        Intent viewPhotoIntent = new Intent(getActivity(), MediaViewerActivity.class);
                        viewPhotoIntent.putExtra("media_url", user.getAvatar());
                        viewPhotoIntent.putExtra("media_type", FileExtension.MEDIA_IMAGE_TYPE);
                        startActivity(viewPhotoIntent);
                    }
                }
            });

            lTakePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    RxPermissions rxPermissions = new RxPermissions(UserProfileFragment.this);
                    rxPermissions.request(Manifest.permission.CAMERA)
                            .subscribe(granted -> {
                                if (granted) {
                                    try {
                                        // create a temp file to save capture photo
                                        File capturePhotoFile = FileExtension.getPathCapturePhoto(requireContext());
                                        capturePhotoAvatarPath = capturePhotoFile.getAbsolutePath();
                                        MediaPicker.openCameraForCapturePhoto(UserProfileFragment.this, REQUEST_CODE_CAPTURE_PHOTO_AVATAR, capturePhotoFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(getContext(), R.string.permission_request_denied, Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });

            lChangePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    RxPermissions rxPermissions = new RxPermissions(UserProfileFragment.this);
                    rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .subscribe(granted -> {
                                if (granted) {
                                    MediaPicker.openAvatarPicker(UserProfileFragment.this, REQUEST_CODE_CHOOSE_IMAGE_AVATAR, 1, false);
                                } else {
                                    Toast.makeText(getContext(), R.string.permission_request_denied, Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        }
        bottomSheetDialog.show();
    }

    private void showChangeCoverBottomSheetDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_cover_change);

        ConstraintLayout lViewPhoto = bottomSheetDialog.findViewById(R.id.l_view_photo);
        ConstraintLayout lTakePhoto = bottomSheetDialog.findViewById(R.id.l_take_photo);
        ConstraintLayout lChangePhoto = bottomSheetDialog.findViewById(R.id.l_change_photo);

        if (lViewPhoto != null && lTakePhoto != null && lChangePhoto != null) {
            if (checkValidValueString(user.getCoverImage())) {
                lViewPhoto.setVisibility(View.VISIBLE);
            } else {
                lViewPhoto.setVisibility(View.GONE);
            }

            lViewPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    if (!user.getCoverImage().isEmpty()) {
                        Intent viewPhotoIntent = new Intent(getActivity(), MediaViewerActivity.class);
                        viewPhotoIntent.putExtra("media_url", user.getCoverImage());
                        viewPhotoIntent.putExtra("media_type", FileExtension.MEDIA_IMAGE_TYPE);
                        startActivity(viewPhotoIntent);
                    }
                }
            });

            lTakePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    RxPermissions rxPermissions = new RxPermissions(UserProfileFragment.this);
                    rxPermissions.request(Manifest.permission.CAMERA)
                            .subscribe(granted -> {
                                if (granted) {
                                    try {
                                        // create a temp file to save capture photo
                                        File capturePhotoFile = FileExtension.getPathCapturePhoto(requireContext());
                                        capturePhotoCoverPath = capturePhotoFile.getAbsolutePath();
                                        MediaPicker.openCameraForCapturePhoto(UserProfileFragment.this, REQUEST_CODE_CAPTURE_PHOTO_COVER, capturePhotoFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(getContext(), R.string.permission_request_denied, Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });

            lChangePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    RxPermissions rxPermissions = new RxPermissions(UserProfileFragment.this);
                    rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .subscribe(granted -> {
                                if (granted) {
                                    MediaPicker.openCoverPicker(UserProfileFragment.this, REQUEST_CODE_CHOOSE_IMAGE_COVER, 1, false);
                                } else {
                                    Toast.makeText(getContext(), R.string.permission_request_denied, Toast.LENGTH_SHORT).show();
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
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);
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
                        Intent shareIntent = new Intent(getActivity(), PostCreatorActivity.class);
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
                    ContextExtension.copyToClipboard(getContext(), link);
                    Toast.makeText(getContext(), R.string.link_copied, Toast.LENGTH_SHORT).show();
                }
            });
            lShareWithFacebookPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    String applicationPackage = "com.facebook.katana"; // Facebook App package
                    Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(applicationPackage);
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
                        Toast.makeText(getContext(), R.string.no_facebook_install, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            lShareWithMessengerPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    String applicationPackage = "com.facebook.orca"; // Message App package
                    Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(applicationPackage);
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
                        Toast.makeText(getContext(), R.string.no_messager_install, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), R.string.error_generate_image_from_layout, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private void showMenuPost(int position) {
        Post post = posts.get(position);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);
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
                    ContextExtension.copyToClipboard(getContext(), post.getCaption());
                    Toast.makeText(getContext(), R.string.content_copied, Toast.LENGTH_SHORT).show();
                }
            });
            lCopyLinkPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    String link = getString(R.string.share_link_base) + "/post/" + post.getId();
                    ContextExtension.copyToClipboard(getContext(), link);
                    Toast.makeText(getContext(), R.string.link_copied, Toast.LENGTH_SHORT).show();
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);
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
                    Toast.makeText(getContext(), R.string.report_post_success, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                // error
                call.cancel();
                Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }
}