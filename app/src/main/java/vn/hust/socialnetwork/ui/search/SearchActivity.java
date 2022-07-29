package vn.hust.socialnetwork.ui.search;

import static vn.hust.socialnetwork.utils.ContextExtension.getImageFromLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.orhanobut.hawk.Hawk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.common.view.commentpost.CommentPostFragment;
import vn.hust.socialnetwork.common.view.commentpost.OnBottomSheetDismiss;
import vn.hust.socialnetwork.common.view.reactuser.ReactUserFragment;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.group.Group;
import vn.hust.socialnetwork.models.media.Media;
import vn.hust.socialnetwork.models.post.Post;
import vn.hust.socialnetwork.models.post.ReactUser;
import vn.hust.socialnetwork.models.search.SearchResult;
import vn.hust.socialnetwork.models.user.User;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.PostService;
import vn.hust.socialnetwork.network.SearchService;
import vn.hust.socialnetwork.ui.groupdetail.GroupDetailActivity;
import vn.hust.socialnetwork.ui.mediaviewer.MediaViewerActivity;
import vn.hust.socialnetwork.ui.postcreator.PostCreatorActivity;
import vn.hust.socialnetwork.ui.postdetail.PostDetailActivity;
import vn.hust.socialnetwork.ui.search.adapters.GroupAdapter;
import vn.hust.socialnetwork.ui.search.adapters.OnGroupListener;
import vn.hust.socialnetwork.ui.search.adapters.OnPostListener;
import vn.hust.socialnetwork.ui.search.adapters.OnUserListener;
import vn.hust.socialnetwork.ui.search.adapters.PostAdapter;
import vn.hust.socialnetwork.ui.search.adapters.UserAdapter;
import vn.hust.socialnetwork.ui.userdetail.UserDetailActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.ContextExtension;
import vn.hust.socialnetwork.utils.FileExtension;

public class SearchActivity extends AppCompatActivity {

    private static final int ITEM_SEARCH_USER = 1;
    private static final int ITEM_SEARCH_GROUP = 2;
    private static final int ITEM_SEARCH_POST = 3;

    private SearchService searchService;
    private PostService postService;

    private AppCompatImageView ivToolbarBack;
    private SearchView svToolbarSearch;
    private ConstraintLayout lHistorySearch, lResultSearch, lLoading;
    private LinearLayoutCompat lSearchEmpty;
    private LinearProgressIndicator pbLoading;
    private RecyclerView rvUser, rvGroup, rvPost;
    private TabLayout tbSearch;

    private String keyword;
    private List<User> users;
    private UserAdapter userAdapter;
    private List<Group> groups;
    private GroupAdapter groupAdapter;
    private List<Post> posts;
    private PostAdapter postAdapter;

    private int limitUser, limitGroup, limitPost;
    private int pageUser, pageGroup, pagePost;
    private boolean isLoadingMoreUser, isLoadingMoreGroup, isLoadingMorePost;
    private boolean canLoadMoreUser, canLoadMoreGroup, canLoadMorePost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // api
        searchService = ApiClient.getClient().create(SearchService.class);
        postService = ApiClient.getClient().create(PostService.class);

        // binding
        ivToolbarBack = findViewById(R.id.iv_toolbar_back);
        svToolbarSearch = findViewById(R.id.sv_toolbar_search);
        lHistorySearch = findViewById(R.id.l_history_search);
        lResultSearch = findViewById(R.id.l_result_search);
        lLoading = findViewById(R.id.l_loading);
        pbLoading = findViewById(R.id.pb_loading);
        tbSearch = findViewById(R.id.tb_search);
        lSearchEmpty = findViewById(R.id.l_search_empty);
        rvUser = findViewById(R.id.rv_user);
        rvGroup = findViewById(R.id.rv_group);
        rvPost = findViewById(R.id.rv_post);

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // init data
        keyword = "";
        lHistorySearch.setVisibility(View.VISIBLE);
        lResultSearch.setVisibility(View.GONE);
        rvUser.setVisibility(View.GONE);
        rvGroup.setVisibility(View.GONE);
        rvPost.setVisibility(View.GONE);
        initPaginateSearch();

        // init tab layout
        initSearchTabs();

        // init recycle view
        users = new ArrayList<>();
        userAdapter = new UserAdapter(SearchActivity.this, users, new OnUserListener() {
            @Override
            public void onItemClick(int position) {
                // open user detail
                Intent intent = new Intent(SearchActivity.this, UserDetailActivity.class);
                intent.putExtra("user_id", users.get(position).getId());
                startActivity(intent);
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SearchActivity.this);
        rvUser.setLayoutManager(linearLayoutManager);
        rvUser.setAdapter(userAdapter);
        rvUser.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // end list
                if (!recyclerView.canScrollVertically(1)) {
                    if (canLoadMoreUser && !isLoadingMoreUser) {
                        loadMoreUser();
                    }
                }
            }
        });

        groups = new ArrayList<>();
        groupAdapter = new GroupAdapter(SearchActivity.this, groups, new OnGroupListener() {
            @Override
            public void onItemClick(int position) {
                // open group detail
                Intent intent = new Intent(SearchActivity.this, GroupDetailActivity.class);
                intent.putExtra("group_id", groups.get(position).getId());
                startActivity(intent);
            }
        });
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(SearchActivity.this);
        rvGroup.setLayoutManager(linearLayoutManager2);
        rvGroup.setAdapter(groupAdapter);
        rvGroup.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // end list
                if (!recyclerView.canScrollVertically(1)) {
                    if (canLoadMoreGroup && !isLoadingMoreGroup) {
                        loadMoreGroup();
                    }
                }
            }
        });

        posts = new ArrayList<>();
        postAdapter = new PostAdapter(SearchActivity.this, posts, new OnPostListener() {
            @Override
            public void onUserPostClick(int position) {
                // open user detail
                Intent intent = new Intent(SearchActivity.this, UserDetailActivity.class);
                intent.putExtra("user_id", posts.get(position).getUser().getId());
                startActivity(intent);
            }

            @Override
            public void onGroupPostClick(int position) {
                // open group detail
                Intent intent = new Intent(SearchActivity.this, GroupDetailActivity.class);
                intent.putExtra("group_id", posts.get(position).getGroup().getId());
                startActivity(intent);
            }

            @Override
            public void onMenuItemClick(int position) {
                showMenuPost(position);
            }

            @Override
            public void onContentLongClick(int position) {
                Dialog dialog = new Dialog(SearchActivity.this);
                WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
                layoutParams.gravity = Gravity.CENTER | Gravity.START;
                layoutParams.x = 100;
                dialog.setContentView(R.layout.dialog_copy_content_post);
                dialog.setCancelable(true);
                dialog.findViewById(R.id.tv_copy_content).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        ContextExtension.copyToClipboard(SearchActivity.this, posts.get(position).getCaption());
                        Toast.makeText(SearchActivity.this, R.string.content_copied, Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show();
            }

            @Override
            public void onMediaItemClick(int position) {
                Media media = posts.get(position).getMedia();
                if (media != null) {
                    Intent viewPhotoIntent = new Intent(SearchActivity.this, MediaViewerActivity.class);
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
                Intent intent = new Intent(SearchActivity.this, PostDetailActivity.class);
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
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
            }

            @Override
            public void onItemClick(int position) {
                Post post = posts.get(position);
                Intent intent = new Intent(SearchActivity.this, PostDetailActivity.class);
                intent.putExtra("post_id", post.getId());
                intent.putExtra("is_show_comment_input", false);
                startActivity(intent);
            }
        });
        LinearLayoutManager linearLayoutManager3 = new LinearLayoutManager(SearchActivity.this);
        rvPost.setLayoutManager(linearLayoutManager3);
        rvPost.setAdapter(postAdapter);
        rvPost.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // end list
                if (!recyclerView.canScrollVertically(1)) {
                    if (canLoadMorePost && !isLoadingMorePost) {
                        loadMorePost();
                    }
                }
            }
        });

        svToolbarSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String temp = query.trim();
                if (temp.isEmpty()) {
                    lHistorySearch.setVisibility(View.VISIBLE);
                    lResultSearch.setVisibility(View.GONE);
                } else {
                    lHistorySearch.setVisibility(View.GONE);
                    lResultSearch.setVisibility(View.VISIBLE);
                    if (!temp.equals(keyword)) {
                        keyword = temp;
                        initPaginateSearch();
                        // search
                        TabLayout.Tab tab = tbSearch.getTabAt(tbSearch.getSelectedTabPosition());
                        int type = (int) tab.getTag();
                        if (type == ITEM_SEARCH_USER) {
                            searchUserByKeyword();
                        } else if (type == ITEM_SEARCH_GROUP) {
                            searchGroupByKeyword();
                        } else if (type == ITEM_SEARCH_POST) {
                            // release old video
                            postAdapter.releaseAllPlayers();
                            searchPostByKeyword();
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                lHistorySearch.setVisibility(View.VISIBLE);
                lResultSearch.setVisibility(View.GONE);
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        postAdapter.releaseAllPlayers();
    }

    private void initSearchTabs() {
        tbSearch.addTab(tbSearch.newTab().setTag(ITEM_SEARCH_USER).setText(R.string.tab_search_user));
        tbSearch.addTab(tbSearch.newTab().setTag(ITEM_SEARCH_GROUP).setText(R.string.tab_search_group));
        tbSearch.addTab(tbSearch.newTab().setTag(ITEM_SEARCH_POST).setText(R.string.tab_search_post));

        tbSearch.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int type = (int) tab.getTag();
                if (type == ITEM_SEARCH_USER) {
                    rvGroup.setVisibility(View.GONE);
                    rvPost.setVisibility(View.GONE);
                    // release old video
                    postAdapter.releaseAllPlayers();
                    searchUserByKeyword();
                } else if (type == ITEM_SEARCH_GROUP) {
                    rvUser.setVisibility(View.GONE);
                    rvPost.setVisibility(View.GONE);
                    // release old video
                    postAdapter.releaseAllPlayers();
                    searchGroupByKeyword();
                } else if (type == ITEM_SEARCH_POST) {
                    rvUser.setVisibility(View.GONE);
                    rvGroup.setVisibility(View.GONE);
                    searchPostByKeyword();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void initPaginateSearch() {
        limitUser = 5;
        limitGroup = 5;
        limitPost = 5;
        pageUser = 1;
        pageGroup = 1;
        pagePost = 1;
        isLoadingMoreUser = false;
        isLoadingMoreGroup = false;
        isLoadingMorePost = false;
        canLoadMoreUser = true;
        canLoadMoreGroup = true;
        canLoadMorePost = true;
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
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(SearchActivity.this, R.style.BottomSheetDialogTheme);
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
                        Intent shareIntent = new Intent(SearchActivity.this, PostCreatorActivity.class);
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
                    ContextExtension.copyToClipboard(SearchActivity.this, link);
                    Toast.makeText(SearchActivity.this, R.string.link_copied, Toast.LENGTH_SHORT).show();
                }
            });
            lShareWithFacebookPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    String applicationPackage = "com.facebook.katana"; // Facebook App package
                    Intent intent = SearchActivity.this.getPackageManager().getLaunchIntentForPackage(applicationPackage);
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
                        Toast.makeText(SearchActivity.this, R.string.no_facebook_install, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            lShareWithMessengerPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    String applicationPackage = "com.facebook.orca"; // Message App package
                    Intent intent = SearchActivity.this.getPackageManager().getLaunchIntentForPackage(applicationPackage);
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
                        Toast.makeText(SearchActivity.this, R.string.no_messager_install, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SearchActivity.this, R.string.error_generate_image_from_layout, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private void showMenuPost(int position) {
        Post post = posts.get(position);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(SearchActivity.this, R.style.BottomSheetDialogTheme);
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
                    ContextExtension.copyToClipboard(SearchActivity.this, post.getCaption());
                    Toast.makeText(SearchActivity.this, R.string.content_copied, Toast.LENGTH_SHORT).show();
                }
            });
            lCopyLinkPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    String link = getString(R.string.share_link_base) + "/post/" + post.getId();
                    ContextExtension.copyToClipboard(SearchActivity.this, link);
                    Toast.makeText(SearchActivity.this, R.string.link_copied, Toast.LENGTH_SHORT).show();
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this, R.style.AlertDialogTheme);
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
                    Toast.makeText(SearchActivity.this, R.string.report_post_success, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SearchActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                // error
                call.cancel();
                Toast.makeText(SearchActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchUserByKeyword() {
        lLoading.setVisibility(View.VISIBLE);
        Map<String, Object> options = new HashMap<>();
        options.put("q", keyword);
        options.put("limit", limitUser);
        options.put("page", 1);
        Call<BaseResponse<List<User>>> call = searchService.searchUser(options);
        call.enqueue(new Callback<BaseResponse<List<User>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<User>>> call, Response<BaseResponse<List<User>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<User>> res = response.body();
                    users.clear();
                    users.addAll(res.getData());
                    userAdapter.notifyDataSetChanged();
                    if (users.size() == 0) {
                        lSearchEmpty.setVisibility(View.VISIBLE);
                        canLoadMoreUser = false;
                    } else {
                        lSearchEmpty.setVisibility(View.GONE);
                        canLoadMoreUser = true;
                    }
                }
                rvUser.setVisibility(View.VISIBLE);
                lLoading.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<BaseResponse<List<User>>> call, Throwable t) {
                call.cancel();
                rvUser.setVisibility(View.VISIBLE);
                lLoading.setVisibility(View.GONE);
            }
        });
    }

    private void loadMoreUser() {
        isLoadingMoreUser = true;
        // call api to get more user
        pbLoading.setVisibility(View.VISIBLE);
        Map<String, Object> options = new HashMap<>();
        options.put("q", keyword);
        options.put("limit", limitUser);
        options.put("page", pageUser + 1);
        Call<BaseResponse<List<User>>> call = searchService.searchUser(options);
        call.enqueue(new Callback<BaseResponse<List<User>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<User>>> call, Response<BaseResponse<List<User>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<User>> res = response.body();
                    List<User> moreData = res.getData();
                    if (moreData.size() > 0) {
                        int oldSize = users.size();
                        users.addAll(moreData);
                        userAdapter.notifyItemRangeInserted(oldSize, moreData.size());
                    } else {
                        canLoadMoreUser = false;
                    }
                    pageUser++;
                }
                pbLoading.setVisibility(View.GONE);
                isLoadingMoreUser = false;
            }

            @Override
            public void onFailure(Call<BaseResponse<List<User>>> call, Throwable t) {
                call.cancel();
                pbLoading.setVisibility(View.GONE);
                isLoadingMoreUser = false;
            }
        });
    }

    private void searchGroupByKeyword() {
        lLoading.setVisibility(View.VISIBLE);
        Map<String, Object> options = new HashMap<>();
        options.put("q", keyword);
        options.put("limit", limitGroup);
        options.put("page", 1);
        Call<BaseResponse<List<Group>>> call = searchService.searchGroup(options);
        call.enqueue(new Callback<BaseResponse<List<Group>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Group>>> call, Response<BaseResponse<List<Group>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<Group>> res = response.body();
                    groups.clear();
                    groups.addAll(res.getData());
                    groupAdapter.notifyDataSetChanged();
                    if (groups.size() == 0) {
                        lSearchEmpty.setVisibility(View.VISIBLE);
                        canLoadMoreGroup = false;
                    } else {
                        lSearchEmpty.setVisibility(View.GONE);
                        canLoadMoreGroup = true;
                    }
                }
                rvGroup.setVisibility(View.VISIBLE);
                lLoading.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Group>>> call, Throwable t) {
                call.cancel();
                rvGroup.setVisibility(View.VISIBLE);
                lLoading.setVisibility(View.GONE);
            }
        });
    }

    private void loadMoreGroup() {
        isLoadingMoreGroup = true;
        // call api to get more group
        pbLoading.setVisibility(View.VISIBLE);
        Map<String, Object> options = new HashMap<>();
        options.put("q", keyword);
        options.put("limit", limitGroup);
        options.put("page", pageGroup + 1);
        Call<BaseResponse<List<Group>>> call = searchService.searchGroup(options);
        call.enqueue(new Callback<BaseResponse<List<Group>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Group>>> call, Response<BaseResponse<List<Group>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<Group>> res = response.body();
                    List<Group> moreData = res.getData();
                    if (moreData.size() > 0) {
                        int oldSize = groups.size();
                        groups.addAll(moreData);
                        groupAdapter.notifyItemRangeInserted(oldSize, moreData.size());
                    } else {
                        canLoadMoreGroup = false;
                    }
                    pageGroup++;
                }
                pbLoading.setVisibility(View.GONE);
                isLoadingMoreGroup = false;
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Group>>> call, Throwable t) {
                call.cancel();
                pbLoading.setVisibility(View.GONE);
                isLoadingMoreGroup = false;
            }
        });
    }

    private void searchPostByKeyword() {
        lLoading.setVisibility(View.VISIBLE);
        Map<String, Object> options = new HashMap<>();
        options.put("q", keyword);
        options.put("limit", limitPost);
        options.put("page", 1);
        Call<BaseResponse<List<Post>>> call = searchService.searchPost(options);
        call.enqueue(new Callback<BaseResponse<List<Post>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Post>>> call, Response<BaseResponse<List<Post>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<Post>> res = response.body();
                    posts.clear();
                    posts.addAll(res.getData());
                    postAdapter.notifyDataSetChanged();
                    if (posts.size() == 0) {
                        lSearchEmpty.setVisibility(View.VISIBLE);
                        canLoadMorePost = false;
                    } else {
                        lSearchEmpty.setVisibility(View.GONE);
                        canLoadMorePost = true;
                    }
                }
                rvPost.setVisibility(View.VISIBLE);
                lLoading.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Post>>> call, Throwable t) {
                call.cancel();
                rvPost.setVisibility(View.VISIBLE);
                lLoading.setVisibility(View.GONE);
            }
        });
    }

    private void loadMorePost() {
        isLoadingMorePost = true;
        // call api to get more user
        pbLoading.setVisibility(View.VISIBLE);
        Map<String, Object> options = new HashMap<>();
        options.put("q", keyword);
        options.put("limit", limitPost);
        options.put("page", pagePost + 1);
        Call<BaseResponse<List<Post>>> call = searchService.searchPost(options);
        call.enqueue(new Callback<BaseResponse<List<Post>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Post>>> call, Response<BaseResponse<List<Post>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<Post>> res = response.body();
                    List<Post> moreData = res.getData();
                    if (moreData.size() > 0) {
                        int oldSize = posts.size();
                        posts.addAll(moreData);
                        postAdapter.notifyItemRangeInserted(oldSize, moreData.size());
                    } else {
                        canLoadMorePost = false;
                    }
                    pagePost++;
                }
                pbLoading.setVisibility(View.GONE);
                isLoadingMorePost = false;
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Post>>> call, Throwable t) {
                call.cancel();
                pbLoading.setVisibility(View.GONE);
                isLoadingMorePost = false;
            }
        });
    }
}