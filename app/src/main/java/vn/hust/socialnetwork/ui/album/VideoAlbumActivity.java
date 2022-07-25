package vn.hust.socialnetwork.ui.album;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.UserProfileService;
import vn.hust.socialnetwork.ui.album.adapters.OnVideoListener;
import vn.hust.socialnetwork.ui.album.adapters.VideoAdapter;
import vn.hust.socialnetwork.ui.mediaviewer.MediaViewerActivity;
import vn.hust.socialnetwork.utils.FileExtension;

public class VideoAlbumActivity extends AppCompatActivity {

    private UserProfileService userProfileService;

    private int userId;
    private List<String> videos;
    private VideoAdapter videoAdapter;

    private RecyclerView rvVideo;
    private AppCompatImageView ivToolbarBack;
    private AppCompatTextView tvToolbarTitle, tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_album);

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getInt("user_id");
        } else {
            userId = 0;
        }

        // api
        userProfileService = ApiClient.getClient().create(UserProfileService.class);

        // binding
        ivToolbarBack = findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        rvVideo = findViewById(R.id.rv_video);
        tvEmpty = findViewById(R.id.tv_empty_text_data);

        // init
        tvToolbarTitle.setText(R.string.toolbar_title_video_album);

        videos = new ArrayList<>();
        videoAdapter = new VideoAdapter(VideoAlbumActivity.this, videos, new OnVideoListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(VideoAlbumActivity.this, MediaViewerActivity.class);
                intent.putExtra("media_type", FileExtension.MEDIA_VIDEO_TYPE);
                intent.putExtra("media_url", videos.get(position));
                startActivity(intent);
            }
        });
        int numberOfColumns = 3;
        LinearLayoutManager layoutManager = new GridLayoutManager(VideoAlbumActivity.this, numberOfColumns);
        rvVideo.setLayoutManager(layoutManager);
        rvVideo.setAdapter(videoAdapter);

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // get data
        getVideos();
    }

    private void getVideos() {
        Call<BaseResponse<List<String>>> call = userProfileService.getVideoAlbum(userId);
        call.enqueue(new Callback<BaseResponse<List<String>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<String>>> call, Response<BaseResponse<List<String>>> response) {
                if (response.isSuccessful()) {
                    videos.clear();
                    BaseResponse<List<String>> res = response.body();
                    // update new videos
                    videos.addAll(res.getData());
                    videoAdapter.notifyDataSetChanged();
                    if (videos.size() == 0) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<String>>> call, Throwable t) {
                Toast.makeText(VideoAlbumActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }
}