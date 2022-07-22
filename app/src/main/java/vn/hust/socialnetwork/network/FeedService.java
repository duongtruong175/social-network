package vn.hust.socialnetwork.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.post.Post;
import vn.hust.socialnetwork.models.story.Story;

public interface FeedService {
    // Feed
    @GET("feed/v1.0/main/stories")
    Call<BaseResponse<List<Story>>> getStories();

    @GET("feed/v1.0/main/posts")
    Call<BaseResponse<List<Post>>> getFeedPosts();
}
