package vn.hust.socialnetwork.ui.story;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.shts.android.storiesprogressview.StoriesProgressView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.common.view.viewerstory.OnBottomSheetDismiss;
import vn.hust.socialnetwork.common.view.viewerstory.ViewerStoryFragment;
import vn.hust.socialnetwork.event.StoryChangeEvent;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.story.Story;
import vn.hust.socialnetwork.models.story.UserStory;
import vn.hust.socialnetwork.models.story.ViewerStory;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.StoryService;
import vn.hust.socialnetwork.ui.userdetail.UserDetailActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.TimeExtension;

public class StoryActivity extends AppCompatActivity {

    private StoryService storyService;

    private ProgressBar pbLoading;
    private AppCompatImageView ivStory, ivClose, ivViewerStory, ivDeleteStory;
    private View vPrevious, vNext;
    private StoriesProgressView spvStory;
    private ConstraintLayout lUserStory;
    private CircleImageView civAvatar;
    private AppCompatTextView tvName, tvStoryTimeCreated, tvViewerStory;

    private List<Story> stories;
    private UserStory userStory;
    private int positionList;

    int currentStory = 0; // current story
    long pressTime = 0L;
    long limit = 500L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_up, R.anim.no_anim);
        setContentView(R.layout.activity_story);
        // api
        storyService = ApiClient.getClient().create(StoryService.class);

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            stories = extras.getParcelableArrayList("stories");
            if (stories.size() > 0) {
                userStory = stories.get(0).getUser();
            }
            positionList = extras.getInt("position_list", -1);
        } else {
            stories = new ArrayList<>();
            userStory = null;
            positionList = -1;
        }

        // check value
        if (stories.size() == 0 || userStory == null) {
            Toast.makeText(StoryActivity.this, R.string.story_load_error, Toast.LENGTH_SHORT).show();
            finish();
        }

        // binding
        pbLoading = findViewById(R.id.pb_loading);
        ivStory = findViewById(R.id.iv_story);
        ivClose = findViewById(R.id.iv_close);
        ivViewerStory = findViewById(R.id.iv_viewer_story);
        ivDeleteStory = findViewById(R.id.iv_delete_story);
        vPrevious = findViewById(R.id.v_previous);
        vNext = findViewById(R.id.v_next);
        spvStory = findViewById(R.id.spv_story);
        lUserStory = findViewById(R.id.l_user_story);
        civAvatar = findViewById(R.id.civ_avatar);
        tvName = findViewById(R.id.tv_name);
        tvStoryTimeCreated = findViewById(R.id.tv_story_time_created);
        tvViewerStory = findViewById(R.id.tv_viewer_story);

        // init view
        if (userStory.getId() != Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0)) {
            ivViewerStory.setVisibility(View.GONE);
            ivDeleteStory.setVisibility(View.GONE);
            tvViewerStory.setVisibility(View.GONE);
        }
        Glide.with(StoryActivity.this)
                .asBitmap()
                .load(userStory.getAvatar())
                .error(R.drawable.default_avatar)
                .into(civAvatar);
        tvName.setText(userStory.getName());

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        lUserStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open user detail
                Intent intent = new Intent(StoryActivity.this, UserDetailActivity.class);
                intent.putExtra("user_id", userStory.getId());
                startActivity(intent);
            }
        });

        ivViewerStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spvStory.pause();
                // open bottom sheet show viewer story
                BottomSheetDialogFragment bottomSheetDialogFragment = new ViewerStoryFragment(stories.get(currentStory).getId(), new OnBottomSheetDismiss() {
                    @Override
                    public void onDialogDismiss() {
                        spvStory.resume();
                    }
                });
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
            }
        });

        tvViewerStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spvStory.pause();
                // open bottom sheet show viewer story
                BottomSheetDialogFragment bottomSheetDialogFragment = new ViewerStoryFragment(stories.get(currentStory).getId(), new OnBottomSheetDismiss() {
                    @Override
                    public void onDialogDismiss() {
                        spvStory.resume();
                    }
                });
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
            }
        });

        ivDeleteStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open dialog confirm delete
                AlertDialog.Builder builder = new AlertDialog.Builder(StoryActivity.this, R.style.AlertDialogTheme);
                builder.setTitle(R.string.delete_story);
                builder.setMessage(R.string.do_you_realy_want_to_delete_this_story);
                builder.setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteStory(currentStory);
                    }
                });
                builder.setNegativeButton(R.string.not_agree, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        spvStory.pause();
                    }
                });
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        spvStory.resume();
                    }
                });
                dialog.show();
            }
        });

        // previous and next story
        vPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spvStory.reverse();
            }
        });
        vPrevious.setOnTouchListener(onTouchPreviousOrNextListener);
        vNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spvStory.skip();
            }
        });
        vNext.setOnTouchListener(onTouchPreviousOrNextListener);

        spvStory.setStoriesCount(stories.size()); // <- set stories
        spvStory.setStoryDuration(5000L); // <- set a story duration
        // set listener
        spvStory.setStoriesListener(new StoriesProgressView.StoriesListener() {
            @Override
            public void onNext() {
                // handle view a story
                currentStory++;
                handleStory(currentStory);
            }

            @Override
            public void onPrev() {
                if (currentStory == 0)
                    return;
                currentStory--;
                handleStory(currentStory);
            }

            @Override
            public void onComplete() {
                finish();
            }
        });

        // start
        handleStory(currentStory);
    }

    private View.OnTouchListener onTouchPreviousOrNextListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    spvStory.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    spvStory.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };

    @Override
    protected void onResume() {
        if (stories.size() != 0) {
            spvStory.resume();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (stories.size() != 0) {
            spvStory.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        spvStory.destroy();
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }

    private void handleStory(int position) {
        Story story = stories.get(position);
        tvStoryTimeCreated.setText(TimeExtension.formatTimeStory(story.getCreatedAt()));
        tvViewerStory.setText(story.getViewerCount() + " người xem");
        pbLoading.setVisibility(View.VISIBLE);
        Glide.with(StoryActivity.this)
                .asBitmap()
                .load(story.getMedia().getSrc())
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        pbLoading.setVisibility(View.GONE);
                        Toast.makeText(StoryActivity.this, R.string.story_load_error, Toast.LENGTH_SHORT).show();
                        spvStory.startStories(position); // <- start progress
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        pbLoading.setVisibility(View.GONE);
                        spvStory.startStories(position); // <- start progress
                        return false;
                    }
                })
                .into(ivStory);
        if (!story.isViewed()) {
            story.setViewed(true);
            viewStory(story.getId());
        }
    }

    private void viewStory(int storyId) {
        Call<BaseResponse<ViewerStory>> call = storyService.viewStory(storyId);
        call.enqueue(new Callback<BaseResponse<ViewerStory>>() {
            @Override
            public void onResponse(Call<BaseResponse<ViewerStory>> call, Response<BaseResponse<ViewerStory>> response) {
                // no action
            }

            @Override
            public void onFailure(Call<BaseResponse<ViewerStory>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private void deleteStory(int position) {
        Call<BaseResponse<String>> call = storyService.deleteStory(stories.get(position).getId());
        call.enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(StoryActivity.this, R.string.delete_story_success, Toast.LENGTH_SHORT).show();
                    stories.remove(position);

                    // send update to feed
                    EventBus.getDefault().post(new StoryChangeEvent(positionList, stories));

                    spvStory.setStoriesCount(stories.size());
                    if (stories.size() == 0) {
                        finish();
                    } else {
                        spvStory.startStories(position);
                    }
                } else {
                    Toast.makeText(StoryActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                // error
                call.cancel();
                Toast.makeText(StoryActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

