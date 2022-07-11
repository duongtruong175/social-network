package vn.hust.socialnetwork.network;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.post.Post;
import vn.hust.socialnetwork.models.post.ReactCount;
import vn.hust.socialnetwork.models.post.ReactUser;

public interface PostService {
    // Post
    @GET("post/v1.0/view/{id}")
    Call<BaseResponse<Post>> getPostDetail(@Path("id") int id);

    @FormUrlEncoded
    @POST("post/v1.0/create")
    Call<BaseResponse<Post>> createPost(@FieldMap Map<String, Object> map);

    @FormUrlEncoded
    @POST("post/v1.0/update/{id}")
    Call<BaseResponse<Post>> updatePost(@Path("id") int id, @FieldMap Map<String, Object> map);

    @DELETE("post/v1.0/delete/{id}")
    Call<BaseResponse<String>> deletePost(@Path("id") int id);

    @GET("post/v1.0/total-react/{id}")
    Call<BaseResponse<ReactCount>> getTotalReacts(@Path("id") int id);

    @GET("post/v1.0/list-react-user/{id}")
    Call<BaseResponse<List<ReactUser>>> getListReactUsers(@Path("id") int id);

    @FormUrlEncoded
    @POST("post/v1.0/react/{id}")
    Call<BaseResponse<ReactUser>> reactPost(@Path("id") int postId, @Field("user_id") int userId, @Field("type") int type);
}
