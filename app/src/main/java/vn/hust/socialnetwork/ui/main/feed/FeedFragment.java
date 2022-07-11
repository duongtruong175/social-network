package vn.hust.socialnetwork.ui.main.feed;

import static vn.hust.socialnetwork.utils.ContextExtension.getImageFromLayout;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.orhanobut.hawk.Hawk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.common.view.reactuser.ReactUserFragment;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.media.Media;
import vn.hust.socialnetwork.models.post.Post;
import vn.hust.socialnetwork.models.post.ReactUser;
import vn.hust.socialnetwork.models.story.Story;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.PostService;
import vn.hust.socialnetwork.network.UserProfileService;
import vn.hust.socialnetwork.ui.main.feed.adapters.OnPostListener;
import vn.hust.socialnetwork.ui.main.feed.adapters.OnStoryListener;
import vn.hust.socialnetwork.ui.main.feed.adapters.StoryAdapter;
import vn.hust.socialnetwork.ui.main.feed.adapters.PostAdapter;
import vn.hust.socialnetwork.ui.mediaviewer.MediaViewerActivity;
import vn.hust.socialnetwork.ui.postcreator.PostCreatorActivity;
import vn.hust.socialnetwork.ui.postdetail.PostDetailActivity;
import vn.hust.socialnetwork.ui.search.SearchActivity;
import vn.hust.socialnetwork.ui.userdetail.UserDetailActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.ContextExtension;
import vn.hust.socialnetwork.utils.FileExtension;

public class FeedFragment extends Fragment {

    private AppCompatImageView ivSearch, ivNewPostWithPhoto;
    private LinearLayoutCompat lNewPost;
    private CircleImageView civMyAvatar;

    private RecyclerView rvStory, rvPost;
    private StoryAdapter storyAdapter;
    private List<Story> stories;
    private List<Post> posts;
    private PostAdapter postAdapter;

    private UserProfileService userProfileService;
    private PostService postService;

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
        userProfileService = ApiClient.getClient().create(UserProfileService.class);
        postService = ApiClient.getClient().create(PostService.class);

        // binding
        ivSearch = view.findViewById(R.id.iv_search);
        ivNewPostWithPhoto = view.findViewById(R.id.iv_new_post_with_photo);
        lNewPost = view.findViewById(R.id.l_new_post);
        civMyAvatar = view.findViewById(R.id.civ_my_avatar);
        rvStory = view.findViewById(R.id.rv_story);
        rvPost = view.findViewById(R.id.rv_post);

        // init
        String avatar = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, "");
        Glide.with(FeedFragment.this)
                .asBitmap()
                .load(avatar)
                .error(R.drawable.default_avatar)
                .into(civMyAvatar);
        stories = new ArrayList<>();
        storyAdapter = new StoryAdapter(getContext(), stories, new OnStoryListener() {
            @Override
            public void onAddStory() {

            }

            @Override
            public void onItemClick(int position) {

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
                startActivity(intent);
            }
        });

        ivNewPostWithPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PostCreatorActivity.class);
                intent.putExtra("open_photo", true);
                startActivity(intent);
            }
        });

        // call api to get all data
        getData();

        return view;
    }

    private void getData() {
        // call api
        int myUserId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY);
        getPosts(myUserId);
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
                        //lPostEmpty.setVisibility(View.VISIBLE);
                    } else {
                        //lPostEmpty.setVisibility(View.GONE);
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