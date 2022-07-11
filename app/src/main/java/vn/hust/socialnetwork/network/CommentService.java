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
import retrofit2.http.Query;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.post.CommentPost;
import vn.hust.socialnetwork.models.post.ReactCount;
import vn.hust.socialnetwork.models.post.ReactUser;

public interface CommentService {
    // Comment
    @GET("comment/v1.0/comments")
    Call<BaseResponse<List<CommentPost>>> getCommentsByPostId(@Query("post_id") int postId);

    @FormUrlEncoded
    @POST("comment/v1.0/create")
    Call<BaseResponse<CommentPost>> createComment(@FieldMap Map<String, Object> map);

    @FormUrlEncoded
    @POST("comment/v1.0/update/{id}")
    Call<BaseResponse<CommentPost>> updateComment(@Path("id") int id, @FieldMap Map<String, Object> map);

    @DELETE("comment/v1.0/delete/{id}")
    Call<BaseResponse<String>> deleteComment(@Path("id") int id);

    @GET("comment/v1.0/total-react/{id}")
    Call<BaseResponse<ReactCount>> getTotalReacts(@Path("id") int id);

    @GET("comment/v1.0/list-react-user/{id}")
    Call<BaseResponse<List<ReactUser>>> getListReactUsers(@Path("id") int id);

    @FormUrlEncoded
    @POST("comment/v1.0/react/{id}")
    Call<BaseResponse<ReactUser>> reactComment(@Path("id") int commentId, @Field("user_id") int userId, @Field("type") int type);
}
