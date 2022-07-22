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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.UserProfileService;
import vn.hust.socialnetwork.ui.album.adapters.OnPhotoListener;
import vn.hust.socialnetwork.ui.album.adapters.PhotoAdapter;
import vn.hust.socialnetwork.ui.mediaviewer.MediaViewerActivity;
import vn.hust.socialnetwork.utils.FileExtension;

public class PhotoAlbumActivity extends AppCompatActivity {

    private UserProfileService userProfileService;

    private int userId;
    private List<String> photos;
    private PhotoAdapter photoAdapter;

    private RecyclerView rvPhoto;
    private AppCompatImageView ivToolbarBack;
    private AppCompatTextView tvToolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_album);

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
        rvPhoto = findViewById(R.id.rv_photo);

        // init
        tvToolbarTitle.setText(R.string.toolbar_title_photo_album);

        photos = new ArrayList<>();
        photoAdapter = new PhotoAdapter(PhotoAlbumActivity.this, photos, new OnPhotoListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(PhotoAlbumActivity.this, MediaViewerActivity.class);
                intent.putExtra("media_type", FileExtension.MEDIA_IMAGE_TYPE);
                intent.putExtra("media_url", photos.get(position));
                startActivity(intent);
            }
        });
        int numberOfColumns = 3;
        LinearLayoutManager layoutManager = new GridLayoutManager(PhotoAlbumActivity.this, numberOfColumns);
        rvPhoto.setLayoutManager(layoutManager);
        rvPhoto.setAdapter(photoAdapter);

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        // get data
        getPhotos();
    }

    private void getPhotos() {
        Call<BaseResponse<List<String>>> call = userProfileService.getPhotoAlbum(userId);
        call.enqueue(new Callback<BaseResponse<List<String>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<String>>> call, Response<BaseResponse<List<String>>> response) {
                if (response.isSuccessful()) {
                    photos.clear();
                    BaseResponse<List<String>> res = response.body();
                    // update new photos
                    photos.addAll(res.getData());
                    photoAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<String>>> call, Throwable t) {

            }
        });
    }
}