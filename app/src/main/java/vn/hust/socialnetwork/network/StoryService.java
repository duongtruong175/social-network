package vn.hust.socialnetwork.network;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.story.Story;
import vn.hust.socialnetwork.models.story.ViewerStory;

public interface StoryService {
    // Story
    @Multipart
    @POST("story/v1.0/create")
    @Headers({"multipart: true"})
    Call<BaseResponse<Story>> createStory(@Part MultipartBody.Part media, @Part("type") RequestBody type);

    @POST("story/v1.0/view-story/{id}")
    Call<BaseResponse<ViewerStory>> viewStory(@Path("id") int storyId);

    @GET("story/v1.0/viewer-story/{id}")
    Call<BaseResponse<List<ViewerStory>>> getViewerStory(@Path("id") int storyId);

    @DELETE("story/v1.0/delete/{id}")
    Call<BaseResponse<String>> deleteStory(@Path("id") int storyId);
}
