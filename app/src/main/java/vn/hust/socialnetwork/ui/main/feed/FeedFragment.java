package vn.hust.socialnetwork.ui.main.feed;

import static vn.hust.socialnetwork.utils.ContextExtension.getImageFromLayout;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.orhanobut.hawk.Hawk;
import com.tbruyelle.rxpermissions3.RxPermissions;
import com.zhihu.matisse.Matisse;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.common.view.commentpost.CommentPostFragment;
import vn.hust.socialnetwork.common.view.commentpost.OnBottomSheetDismiss;
import vn.hust.socialnetwork.common.view.reactuser.ReactUserFragment;
import vn.hust.socialnetwork.event.StoryChangeEvent;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.media.Media;
import vn.hust.socialnetwork.models.post.Post;
import vn.hust.socialnetwork.models.post.ReactUser;
import vn.hust.socialnetwork.models.story.Story;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.FeedService;
import vn.hust.socialnetwork.network.PostService;
import vn.hust.socialnetwork.network.StoryService;
import vn.hust.socialnetwork.network.UploadMediaService;
import vn.hust.socialnetwork.network.UserProfileService;
import vn.hust.socialnetwork.ui.groupdetail.GroupDetailActivity;
import vn.hust.socialnetwork.ui.main.feed.adapters.OnPostListener;
import vn.hust.socialnetwork.ui.main.feed.adapters.OnStoryListener;
import vn.hust.socialnetwork.ui.main.feed.adapters.StoryAdapter;
import vn.hust.socialnetwork.ui.main.feed.adapters.PostAdapter;
import vn.hust.socialnetwork.ui.main.userprofile.crop.CropUserAvatarActivity;
import vn.hust.socialnetwork.ui.mediaviewer.MediaViewerActivity;
import vn.hust.socialnetwork.ui.postcreator.PostCreatorActivity;
import vn.hust.socialnetwork.ui.postdetail.PostDetailActivity;
import vn.hust.socialnetwork.ui.search.SearchActivity;
import vn.hust.socialnetwork.ui.story.StoryActivity;
import vn.hust.socialnetwork.ui.userdetail.UserDetailActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.ContextExtension;
import vn.hust.socialnetwork.utils.FileExtension;
import vn.hust.socialnetwork.utils.MediaPicker;
import vn.hust.socialnetwork.utils.NotificationExtension;
import vn.hust.socialnetwork.utils.RequestCodeResultActivity;

public class FeedFragment extends Fragment {

    private static final int REQUEST_CODE_CHOOSE_IMAGE = 104;

    private FeedService feedService;
    private PostService postService;
    private StoryService storyService;

    private AppCompatImageView ivSearch, ivNewPostWithPhoto;
    private LinearLayoutCompat lNewPost;
    private CircleImageView civMyAvatar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutCompat lMain;
    private ShimmerFrameLayout lShimmer;
    private LinearProgressIndicator pbLoading;
    private ConstraintLayout lPostEmpty;
    private View viewItemAddStory;

    private RecyclerView rvStory, rvPost;
    private StoryAdapter storyAdapter;
    private List<Story> stories;
    private List<Post> posts;
    private PostAdapter postAdapter;

    public FeedFragment() {
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
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        // api
        feedService = ApiClient.getClient().create(FeedService.class);
        postService = ApiClient.getClient().create(PostService.class);
        storyService = ApiClient.getClient().create(StoryService.class);

        // binding
        ivSearch = view.findViewById(R.id.iv_search);
        ivNewPostWithPhoto = view.findViewById(R.id.iv_new_post_with_photo);
        lNewPost = view.findViewById(R.id.l_new_post);
        civMyAvatar = view.findViewById(R.id.civ_my_avatar);
        swipeRefreshLayout = view.findViewById(R.id.l_swipe_refresh);
        lMain = view.findViewById(R.id.l_main);
        lShimmer = view.findViewById(R.id.l_shimmer);
        lPostEmpty = view.findViewById(R.id.l_post_empty);
        pbLoading = view.findViewById(R.id.pb_loading);
        rvStory = view.findViewById(R.id.rv_story);
        rvPost = view.findViewById(R.id.rv_post);

        // init
        stories = new ArrayList<>();
        storyAdapter = new StoryAdapter(getContext(), stories, new OnStoryListener() {
            @Override
            public void onAddStory(View viewItem) {
                viewItemAddStory = viewItem;
                openPickerImage();
            }

            @Override
            public void onItemClick(int position) {
                // open activity view story
                List<Story> view = new ArrayList<>();
                view.add(stories.get(position));
                Intent intent = new Intent(getActivity(), StoryActivity.class);
                intent.putParcelableArrayListExtra("stories", (ArrayList<? extends Parcelable>) view);
                intent.putExtra("position_list", position);
                startActivity(intent);
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvStory.setLayoutManager(linearLayoutManager);
        rvStory.setAdapter(storyAdapter);

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
                Intent intent = new Intent(getActivity(), GroupDetailActivity.class);
                intent.putExtra("group_id", posts.get(position).getGroup().getId());
                startActivity(intent);
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
            public void onShowCommentDialogClick(AppCompatTextView tvCommentCount, int position) {
                Post post = posts.get(position);
                BottomSheetDialogFragment bottomSheetDialogFragment = new CommentPostFragment(post, new OnBottomSheetDismiss() {
                    @Override
                    public void onDialogDismiss(int totalComment) {
                        posts.get(position).getCounts().setCommentCount(totalComment);
                        tvCommentCount.setText(totalComment + " " + getString(R.string.comment_count));
                    }
                });
                bottomSheetDialogFragment.show(getParentFragmentManager(), bottomSheetDialogFragment.getTag());
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
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getContext());
        rvPost.setLayoutManager(layoutManager2);
        rvPost.setAdapter(postAdapter);

        handleRefreshView();

        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
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

        // call api to get all data
        getData();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            // upload image to story
            String imagePath = Matisse.obtainPathResult(data).get(0);
            // call api upload story
            uploadStory(imagePath);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            postAdapter.pauseAllPlayers();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(StoryChangeEvent event) {
        // update story
        int positionList = event.getPositionList();
        List<Story> newListStory = event.getNewListStory();
        if (positionList != -1) {
            if (newListStory.size() == 0) {
                stories.remove(positionList);
                storyAdapter.notifyItemRemoved(positionList);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
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
                        }
                    }
                }
            });

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
        // init view user
        Glide.with(FeedFragment.this)
                .asBitmap()
                .load(Hawk.get(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, ""))
                .error(R.drawable.default_avatar)
                .into(civMyAvatar);

        lMain.setVisibility(View.GONE);
        lShimmer.setVisibility(View.VISIBLE);
        lShimmer.startShimmer();
        // call api
        getStories();
        getPosts();
    }

    private void refresh() {
        // init view user
        Glide.with(FeedFragment.this)
                .asBitmap()
                .load(Hawk.get(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, ""))
                .error(R.drawable.default_avatar)
                .into(civMyAvatar);

        // call api
        getStories();
        getPosts();
    }

    private void getStories() {
        Call<BaseResponse<List<Story>>> call = feedService.getStories();
        call.enqueue(new Callback<BaseResponse<List<Story>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Story>>> call, Response<BaseResponse<List<Story>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<Story>> res = response.body();
                    stories.clear();
                    stories.addAll(res.getData());
                    storyAdapter.notifyDataSetChanged();
                }
                lMain.setVisibility(View.VISIBLE);
                lShimmer.stopShimmer();
                lShimmer.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Story>>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                lMain.setVisibility(View.VISIBLE);
                lShimmer.stopShimmer();
                lShimmer.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void getPosts() {
        Call<BaseResponse<List<Post>>> call = feedService.getFeedPosts();
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

    private void uploadStory(String imagePath) {
        if (viewItemAddStory != null) {
            viewItemAddStory.setClickable(false);
            ProgressBar pbLoad = viewItemAddStory.findViewById(R.id.pb_loading_add_story);
            AppCompatTextView tvAddStory = viewItemAddStory.findViewById(R.id.tv_add_story);
            if (pbLoad != null && tvAddStory != null) {
                pbLoad.setVisibility(View.VISIBLE);
                tvAddStory.setText(R.string.add_story_load);

                File file = new File(imagePath);
                RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
                MultipartBody.Part mediaUpload = MultipartBody.Part.createFormData("media", file.getName(), requestFile);
                RequestBody type = RequestBody.create(FileExtension.MEDIA_IMAGE_TYPE, okhttp3.MultipartBody.FORM);
                Call<BaseResponse<Story>> call = storyService.createStory(mediaUpload, type);
                call.enqueue(new Callback<BaseResponse<Story>>() {
                    @Override
                    public void onResponse(Call<BaseResponse<Story>> call, Response<BaseResponse<Story>> response) {
                        if (response.isSuccessful()) {
                            BaseResponse<Story> res = response.body();
                            stories.add(0, res.getData());
                            storyAdapter.notifyItemInserted(0);

                            // send a notification
                            NotificationExtension.showCreateStorySuccess(requireContext());

                        } else {
                            Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                        }
                        pbLoad.setVisibility(View.INVISIBLE);
                        tvAddStory.setText(R.string.add_new_story);
                        viewItemAddStory.setClickable(true);
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<Story>> call, Throwable t) {
                        // error network (no internet connection, socket timeout, unknown host, ...)
                        // error serializing/deserializing the data
                        call.cancel();
                        Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                        pbLoad.setVisibility(View.INVISIBLE);
                        tvAddStory.setText(R.string.add_new_story);
                        viewItemAddStory.setClickable(true);
                    }
                });
            } else {
                viewItemAddStory.setClickable(true);
            }
        }
    }

    private void openPickerImage() {
        RxPermissions rxPermissions = new RxPermissions(FeedFragment.this);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .subscribe(granted -> {
                    if (granted) {
                        MediaPicker.openImagePicker(FeedFragment.this, REQUEST_CODE_CHOOSE_IMAGE, 1, true);
                    } else {
                        Toast.makeText(getContext(), R.string.permission_request_denied, Toast.LENGTH_SHORT).show();
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