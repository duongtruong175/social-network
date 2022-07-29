package vn.hust.socialnetwork.network;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.post.Post;
import vn.hust.socialnetwork.models.story.Story;

public interface FeedService {
    // Feed
    @GET("feed/v1.0/main/stories")
    Call<BaseResponse<List<Story>>> getStories(@QueryMap Map<String, Object> options);

    @GET("feed/v1.0/main/posts")
    Call<BaseResponse<List<Post>>> getFeedPosts(@QueryMap Map<String, Object> options);
}
