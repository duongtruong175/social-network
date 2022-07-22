package vn.hust.socialnetwork.ui.userdetail;

import static vn.hust.socialnetwork.utils.ContextExtension.getImageFromLayout;
import static vn.hust.socialnetwork.utils.StringExtension.checkValidValueString;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.orhanobut.hawk.Hawk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.common.style.CustomTypefaceSpan;
import vn.hust.socialnetwork.common.view.reactuser.ReactUserFragment;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.fcm.Data;
import vn.hust.socialnetwork.models.fcm.DataMessageSender;
import vn.hust.socialnetwork.models.fcm.FCMResponse;
import vn.hust.socialnetwork.models.fcm.Token;
import vn.hust.socialnetwork.models.media.Media;
import vn.hust.socialnetwork.models.notification.Notification;
import vn.hust.socialnetwork.models.post.Post;
import vn.hust.socialnetwork.models.post.ReactUser;
import vn.hust.socialnetwork.models.relation.RelationUser;
import vn.hust.socialnetwork.models.user.CountsUser;
import vn.hust.socialnetwork.models.user.Relation;
import vn.hust.socialnetwork.models.user.User;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.NotificationService;
import vn.hust.socialnetwork.network.PostService;
import vn.hust.socialnetwork.network.RelationService;
import vn.hust.socialnetwork.network.UserProfileService;
import vn.hust.socialnetwork.ui.album.PhotoAlbumActivity;
import vn.hust.socialnetwork.ui.album.VideoAlbumActivity;
import vn.hust.socialnetwork.ui.friend.AllFriendActivity;
import vn.hust.socialnetwork.ui.groupdetail.GroupDetailActivity;
import vn.hust.socialnetwork.ui.mediaviewer.MediaViewerActivity;
import vn.hust.socialnetwork.ui.message.MessageActivity;
import vn.hust.socialnetwork.ui.postcreator.PostCreatorActivity;
import vn.hust.socialnetwork.ui.postdetail.PostDetailActivity;
import vn.hust.socialnetwork.ui.relation.RelationActivity;
import vn.hust.socialnetwork.ui.userdetail.adapters.FriendAdapter;
import vn.hust.socialnetwork.ui.userdetail.adapters.OnFriendListener;
import vn.hust.socialnetwork.ui.userdetail.adapters.OnPostListener;
import vn.hust.socialnetwork.ui.userdetail.adapters.PostAdapter;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.ContextExtension;
import vn.hust.socialnetwork.utils.FileExtension;
import vn.hust.socialnetwork.utils.NotificationExtension;
import vn.hust.socialnetwork.utils.TimeExtension;

public class UserDetailActivity extends AppCompatActivity {

    private UserProfileService userProfileService;
    private PostService postService;
    private RelationService relationService;
    private NotificationService notificationService;

    private int userId;
    private int relationId;
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
    private LinearLayoutCompat lPhotoUserProfile, lVideoUserProfile;
    private AppCompatButton btnRefresh;
    private RelativeLayout lJobContainer, lEducationContainer, lPhoneContainer, lCurrentResidenceContainer, lHometownContainer, lRelationshipContainer, lWebsiteContainer;
    private AppCompatTextView tvJob, tvEducation, tvPhone, tvCurrentResidence, tvHometown, tvGender, tvRelationship, tvWebsite, tvJoinIn;
    private CircleImageView civImageAvatarToolbar, civImageAvatar;
    private AppCompatImageView ivBackToolbar, ivImageCover, ivZodiac, ivGender;
    private RecyclerView rvFriend, rvPost;
    private ConstraintLayout lRelationUserProfile, lChatRelation;
    private AppCompatButton btnChangeRelation;
    private AppCompatImageView ivMenuRelation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getInt("user_id");
        } else {
            userId = 0;
        }

        // api
        userProfileService = ApiClient.getClient().create(UserProfileService.class);
        postService = ApiClient.getClient().create(PostService.class);
        relationService = ApiClient.getClient().create(RelationService.class);
        notificationService = ApiClient.getClient().create(NotificationService.class);

        // binding
        lMain = findViewById(R.id.l_main);
        lShimmer = findViewById(R.id.l_shimmer);
        lError = findViewById(R.id.l_error);
        lPostEmpty = findViewById(R.id.l_post_empty);
        pbLoading = findViewById(R.id.pb_loading);
        swipeRefreshLayout = findViewById(R.id.l_swipe_refresh);
        appBarLayout = findViewById(R.id.ab_main_user_profile);
        ivBackToolbar = findViewById(R.id.iv_back_toolbar);
        tvUserNameToolbar = findViewById(R.id.tv_user_name_toolbar);
        civImageAvatarToolbar = findViewById(R.id.civ_image_avatar_toolbar);
        civImageAvatar = findViewById(R.id.civ_image_avatar);
        ivImageCover = findViewById(R.id.iv_image_cover);
        tvUserName = findViewById(R.id.tv_user_name);
        lZodiacContainer = findViewById(R.id.l_zodiac_container);
        tvZodiac = findViewById(R.id.tv_zodiac);
        ivZodiac = findViewById(R.id.iv_zodiac);
        tvDescription = findViewById(R.id.tv_description);
        tvFollowerCount = findViewById(R.id.tv_follower_count);
        tvPostCount = findViewById(R.id.tv_post_count);
        tvReactCount = findViewById(R.id.tv_react_count);
        tvFriendCount = findViewById(R.id.tv_friend_count);
        tvViewAllFriend = findViewById(R.id.tv_view_all_friend);
        lPhotoUserProfile = findViewById(R.id.l_photo_user_profile);
        lVideoUserProfile = findViewById(R.id.l_video_user_profile);
        lJobContainer = findViewById(R.id.l_job_container);
        lEducationContainer = findViewById(R.id.l_education_container);
        lPhoneContainer = findViewById(R.id.l_phone_container);
        lCurrentResidenceContainer = findViewById(R.id.l_current_residence_container);
        lHometownContainer = findViewById(R.id.l_hometown_container);
        lRelationshipContainer = findViewById(R.id.l_relationship_container);
        lWebsiteContainer = findViewById(R.id.l_website_container);
        tvJob = findViewById(R.id.tv_job);
        tvEducation = findViewById(R.id.tv_education);
        tvPhone = findViewById(R.id.tv_phone);
        tvCurrentResidence = findViewById(R.id.tv_current_residence);
        tvHometown = findViewById(R.id.tv_hometown);
        tvGender = findViewById(R.id.tv_gender);
        ivGender = findViewById(R.id.iv_gender);
        tvRelationship = findViewById(R.id.tv_relationship);
        tvWebsite = findViewById(R.id.tv_website);
        tvJoinIn = findViewById(R.id.tv_join_in);
        rvFriend = findViewById(R.id.rv_friend);
        rvPost = findViewById(R.id.rv_post);
        btnRefresh = findViewById(R.id.btn_refresh);
        lRelationUserProfile = findViewById(R.id.l_relation_user_profile);
        lChatRelation = findViewById(R.id.l_chat_relation);
        btnChangeRelation = findViewById(R.id.btn_change_relation);
        ivMenuRelation = findViewById(R.id.iv_menu_relation);

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

        // view avatar
        civImageAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null && checkValidValueString(user.getAvatar())) {
                    Intent viewPhotoIntent = new Intent(UserDetailActivity.this, MediaViewerActivity.class);
                    viewPhotoIntent.putExtra("media_url", user.getAvatar());
                    viewPhotoIntent.putExtra("media_type", FileExtension.MEDIA_IMAGE_TYPE);
                    startActivity(viewPhotoIntent);
                }
            }
        });

        ivImageCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null && checkValidValueString(user.getCoverImage())) {
                    Intent viewPhotoIntent = new Intent(UserDetailActivity.this, MediaViewerActivity.class);
                    viewPhotoIntent.putExtra("media_url", user.getCoverImage());
                    viewPhotoIntent.putExtra("media_type", FileExtension.MEDIA_IMAGE_TYPE);
                    startActivity(viewPhotoIntent);
                }
            }
        });

        // view all photos
        lPhotoUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDetailActivity.this, PhotoAlbumActivity.class);
                intent.putExtra("user_id", user.getId());
                startActivity(intent);
            }
        });

        // view all videos
        lVideoUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDetailActivity.this, VideoAlbumActivity.class);
                intent.putExtra("user_id", user.getId());
                startActivity(intent);
            }
        });

        // view all relation
        tvViewAllFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open activity friend
                Intent intent = new Intent(UserDetailActivity.this, AllFriendActivity.class);
                intent.putExtra("user_id", user.getId());
                intent.putExtra("user_name", user.getName());
                startActivity(intent);
            }
        });

        lChatRelation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open chat activity
                Intent intent = new Intent(UserDetailActivity.this, MessageActivity.class);
                intent.putExtra("user_id", user.getId());
                startActivity(intent);
            }
        });

        // update relation
        btnChangeRelation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionChangeRelation();
            }
        });
        ivMenuRelation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuUserDetail();
            }
        });

        friends = new ArrayList<>();
        friendAdapter = new FriendAdapter(UserDetailActivity.this, friends, new OnFriendListener() {
            @Override
            public void onItemClick(int position) {
                onItemFriendClick(position);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(UserDetailActivity.this, LinearLayoutManager.HORIZONTAL, false);
        rvFriend.setLayoutManager(layoutManager);
        rvFriend.setAdapter(friendAdapter);

        posts = new ArrayList<>();
        postAdapter = new PostAdapter(UserDetailActivity.this, posts, new OnPostListener() {
            @Override
            public void onUserPostClick(int position) {
                // open user detail
                Intent intent = new Intent(UserDetailActivity.this, UserDetailActivity.class);
                intent.putExtra("user_id", posts.get(position).getUser().getId());
                startActivity(intent);
            }

            @Override
            public void onGroupPostClick(int position) {
                // open group detail
                Intent intent = new Intent(UserDetailActivity.this, GroupDetailActivity.class);
                intent.putExtra("group_id", posts.get(position).getGroup().getId());
                startActivity(intent);
            }

            @Override
            public void onMenuItemClick(int position) {
                showMenuPost(position);
            }

            @Override
            public void onContentLongClick(int position) {
                Dialog dialog = new Dialog(UserDetailActivity.this);
                WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
                layoutParams.gravity = Gravity.CENTER | Gravity.START;
                layoutParams.x = 100;
                dialog.setContentView(R.layout.dialog_copy_content_post);
                dialog.setCancelable(true);
                dialog.findViewById(R.id.tv_copy_content).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        ContextExtension.copyToClipboard(UserDetailActivity.this, posts.get(position).getCaption());
                        Toast.makeText(UserDetailActivity.this, R.string.content_copied, Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show();
            }

            @Override
            public void onMediaItemClick(int position) {
                Media media = posts.get(position).getMedia();
                if (media != null) {
                    Intent viewPhotoIntent = new Intent(UserDetailActivity.this, MediaViewerActivity.class);
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
                Intent intent = new Intent(UserDetailActivity.this, PostDetailActivity.class);
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
                Intent intent = new Intent(UserDetailActivity.this, PostDetailActivity.class);
                intent.putExtra("post_id", post.getId());
                intent.putExtra("is_show_comment_input", false);
                startActivity(intent);
            }
        });
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(UserDetailActivity.this);
        rvPost.setLayoutManager(layoutManager2);
        rvPost.setAdapter(postAdapter);

        // call api to get all data
        getData();
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
                    civImageAvatarToolbar.setVisibility(View.VISIBLE);
                    tvUserNameToolbar.setVisibility(View.VISIBLE);
                } else {
                    if (isShowToolbar) { // use variable isShowToolbar to avoid run function hide toolbar too much
                        // hide toolbar
                        isShowToolbar = false;
                        ivBackToolbar.setColorFilter(Color.WHITE);
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
        getUserDetail(userId);
        getFriends(userId);
        getPosts(userId);
        getRelation(userId);
    }

    private void refresh() {
        // call api
        getUserDetail(userId);
        getFriends(userId);
        getPosts(userId);
        getRelation(userId);
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
        Glide.with(UserDetailActivity.this)
                .asBitmap()
                .load(user.getCoverImage())
                .error(R.drawable.default_cover)
                .into(ivImageCover);
        // avatar
        Glide.with(UserDetailActivity.this)
                .asBitmap()
                .load(user.getAvatar())
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(civImageAvatar);
        // avatar in toolbar
        Glide.with(UserDetailActivity.this)
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
        Typeface typeface = ResourcesCompat.getFont(UserDetailActivity.this, R.font.f_roboto_medium);
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

        // update relation
        if (userId == Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0)) {
            lRelationUserProfile.setVisibility(View.GONE);
        } else {
            handleRelation();
        }
    }

    private void handleRelation() {
        switch (user.getRelation()) {
            case "friend":
                btnChangeRelation.setText(R.string.friend);
                btnChangeRelation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_edit, 0, 0, 0);
                break;
            case "receiver":
                btnChangeRelation.setText(R.string.relation_sender_user_profile);
                btnChangeRelation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_friend_request_cancel, 0, 0, 0);
                break;
            case "sender":
                btnChangeRelation.setText(R.string.relation_receiver_user_profile);
                btnChangeRelation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_friend_receiver, 0, 0, 0);
                break;
            default:
                btnChangeRelation.setText(R.string.relation_default_user_profile);
                btnChangeRelation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_friend_add, 0, 0, 0);
        }
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
        Intent intent = new Intent(UserDetailActivity.this, UserDetailActivity.class);
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

    private void getRelation(int userId) {
        Call<BaseResponse<RelationUser>> call = relationService.getRelation(userId);
        call.enqueue(new Callback<BaseResponse<RelationUser>>() {
            @Override
            public void onResponse(Call<BaseResponse<RelationUser>> call, Response<BaseResponse<RelationUser>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<RelationUser> res = response.body();
                    relationId = res.getData().getId();
                } else {
                    relationId = -1;
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<RelationUser>> call, Throwable t) {
                call.cancel();
                relationId = -1;
            }
        });
    }

    private void showMenuUserDetail() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(UserDetailActivity.this, R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_menu_user_detail);

        ConstraintLayout lCopyLinkUser = bottomSheetDialog.findViewById(R.id.l_copy_link_user);
        ConstraintLayout lReportUser = bottomSheetDialog.findViewById(R.id.l_report_user);
        ConstraintLayout lBlockUser = bottomSheetDialog.findViewById(R.id.l_block_user);

        if (lCopyLinkUser != null && lReportUser != null && lBlockUser != null) {
            lCopyLinkUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    String link = getString(R.string.share_link_base) + "/user/" + userId;
                    ContextExtension.copyToClipboard(UserDetailActivity.this, link);
                    Toast.makeText(UserDetailActivity.this, R.string.link_copied, Toast.LENGTH_SHORT).show();
                }
            });
            lReportUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    // open activity report user
                    Toast.makeText(UserDetailActivity.this, R.string.report_user_success, Toast.LENGTH_SHORT).show();
                }
            });
            lBlockUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    // open dialog confirm block user
                    Toast.makeText(UserDetailActivity.this, R.string.block_user_success, Toast.LENGTH_SHORT).show();
                }
            });
        }

        bottomSheetDialog.show();
    }

    private void showOptionChangeRelation() {
        AlertDialog.Builder builder;
        switch (user.getRelation()) {
            case "friend":
                // open dialog confirm
                builder = new AlertDialog.Builder(UserDetailActivity.this, R.style.AlertDialogTheme);
                builder.setTitle(R.string.delete_friend);
                builder.setMessage(getString(R.string.do_you_realy_want_to_delete_friend) + " " + user.getName() + "?");
                builder.setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteRelation();
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
            case "receiver":
                // open dialog confirm
                builder = new AlertDialog.Builder(UserDetailActivity.this, R.style.AlertDialogTheme);
                builder.setTitle(R.string.delete_request_friend);
                builder.setMessage(getString(R.string.do_you_realy_want_to_delete_request_friend) + " " + user.getName() + "?");
                builder.setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteRelation();
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
            case "sender":
                // open dialog confirm
                builder = new AlertDialog.Builder(UserDetailActivity.this, R.style.AlertDialogTheme);
                builder.setTitle(R.string.relation_receiver_user_profile);
                builder.setMessage(getString(R.string.do_you_realy_want_to_accept_friend) + " " + user.getName() + "?");
                builder.setPositiveButton("Chấp nhận", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        acceptFriend();
                    }
                });
                builder.setNegativeButton("Từ chối", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteRelation();
                    }
                });
                builder.create().show();
                break;
            default:
                // send a request friend
                sendRequestFriend();
        }
    }

    private void acceptFriend() {
        Call<BaseResponse<RelationUser>> call = relationService.updateRelation(relationId, 1);
        call.enqueue(new Callback<BaseResponse<RelationUser>>() {
            @Override
            public void onResponse(Call<BaseResponse<RelationUser>> call, Response<BaseResponse<RelationUser>> response) {
                if (response.isSuccessful()) {
                    user.setRelation("friend");
                    handleRelation();
                    Toast.makeText(UserDetailActivity.this, R.string.accept_friend_success, Toast.LENGTH_SHORT).show();

                    // send a notification
                    int receiverId = userId;
                    String imageUrl = "";
                    if (!Hawk.get(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, "").isEmpty()) {
                        imageUrl = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, "");
                    }
                    int type = 2;
                    String content = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_NAME_KEY, "") + " đã chấp nhận lời mời kết bạn của bạn";
                    String url = "user/" + Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0);
                    sendNotification(receiverId, imageUrl, type, content, url);
                } else {
                    Toast.makeText(UserDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<RelationUser>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                Toast.makeText(UserDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteRelation() {
        Call<BaseResponse<String>> call = relationService.deleteRelation(relationId);
        call.enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful()) {
                    relationId = -1;
                    user.setRelation("");
                    handleRelation();
                    Toast.makeText(UserDetailActivity.this, R.string.delete_relation_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UserDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                Toast.makeText(UserDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendRequestFriend() {
        // create a form request friend and send to server
        Call<BaseResponse<RelationUser>> call = relationService.createRequestFriend(userId);
        call.enqueue(new Callback<BaseResponse<RelationUser>>() {
            @Override
            public void onResponse(Call<BaseResponse<RelationUser>> call, Response<BaseResponse<RelationUser>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<RelationUser> res = response.body();
                    relationId = res.getData().getId();
                    user.setRelation("receiver");
                    handleRelation();
                    Toast.makeText(UserDetailActivity.this, R.string.relation_create_request_friend_success, Toast.LENGTH_SHORT).show();

                    // send a notification
                    int receiverId = userId;
                    String imageUrl = "";
                    if (!Hawk.get(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, "").isEmpty()) {
                        imageUrl = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, "");
                    }
                    int type = 1;
                    String content = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_NAME_KEY, "") + " đã gửi cho bạn một lời mời kết bạn";
                    String url = "request_friend";
                    sendNotification(receiverId, imageUrl, type, content, url);
                } else {
                    Toast.makeText(UserDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<RelationUser>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                Toast.makeText(UserDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
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
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(UserDetailActivity.this, R.style.BottomSheetDialogTheme);
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
                        Intent shareIntent = new Intent(UserDetailActivity.this, PostCreatorActivity.class);
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
                    ContextExtension.copyToClipboard(UserDetailActivity.this, link);
                    Toast.makeText(UserDetailActivity.this, R.string.link_copied, Toast.LENGTH_SHORT).show();
                }
            });
            lShareWithFacebookPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    String applicationPackage = "com.facebook.katana"; // Facebook App package
                    Intent intent = UserDetailActivity.this.getPackageManager().getLaunchIntentForPackage(applicationPackage);
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
                        Toast.makeText(UserDetailActivity.this, R.string.no_facebook_install, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            lShareWithMessengerPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    String applicationPackage = "com.facebook.orca"; // Message App package
                    Intent intent = UserDetailActivity.this.getPackageManager().getLaunchIntentForPackage(applicationPackage);
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
                        Toast.makeText(UserDetailActivity.this, R.string.no_messager_install, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(UserDetailActivity.this, R.string.error_generate_image_from_layout, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private void showMenuPost(int position) {
        Post post = posts.get(position);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(UserDetailActivity.this, R.style.BottomSheetDialogTheme);
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
                    ContextExtension.copyToClipboard(UserDetailActivity.this, post.getCaption());
                    Toast.makeText(UserDetailActivity.this, R.string.content_copied, Toast.LENGTH_SHORT).show();
                }
            });
            lCopyLinkPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    String link = getString(R.string.share_link_base) + "/post/" + post.getId();
                    ContextExtension.copyToClipboard(UserDetailActivity.this, link);
                    Toast.makeText(UserDetailActivity.this, R.string.link_copied, Toast.LENGTH_SHORT).show();
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(UserDetailActivity.this, R.style.AlertDialogTheme);
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
                    Toast.makeText(UserDetailActivity.this, R.string.report_post_success, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(UserDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                // error
                call.cancel();
                Toast.makeText(UserDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
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