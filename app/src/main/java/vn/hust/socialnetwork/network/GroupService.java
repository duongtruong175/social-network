package vn.hust.socialnetwork.network;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.group.Group;
import vn.hust.socialnetwork.models.group.MemberGroup;
import vn.hust.socialnetwork.models.post.Post;

public interface GroupService {
    // Group
    @GET("group/v1.0/groups")
    Call<BaseResponse<List<Group>>> getMyGroups();

    @GET("group/v1.0/suggest-groups")
    Call<BaseResponse<List<Group>>> getSuggestGroups();

    @GET("group/v1.0/groups/feed")
    Call<BaseResponse<List<Post>>> getFeedGroup(@QueryMap Map<String, Object> options);

    @GET("group/v1.0/view/{id}")
    Call<BaseResponse<Group>> getGroupDetail(@Path("id") int id);

    @GET("group/v1.0/{id}/posts")
    Call<BaseResponse<List<Post>>> getGroupPosts(@Path("id") int id, @QueryMap Map<String, Object> options);

    @GET("group/v1.0/{id}/members")
    Call<BaseResponse<List<MemberGroup>>> getMemberGroups(@Path("id") int id);

    @GET("group/v1.0/{id}/requests")
    Call<BaseResponse<List<MemberGroup>>> getGroupRequests(@Path("id") int id);

    @FormUrlEncoded
    @POST("group/v1.0/get-request")
    Call<BaseResponse<MemberGroup>> getMemberGroupRequest(@Field("group_id") int groupId, @Field("user_id") int userId);

    @POST("group/v1.0/accept-request/{id}")
    Call<BaseResponse<MemberGroup>> acceptMemberGroupRequest(@Path("id") int id);

    @DELETE("group/v1.0/delete-request/{id}")
    Call<BaseResponse<String>> deleteMemberGroupRequest(@Path("id") int id);

    @FormUrlEncoded
    @POST("group/v1.0/send-request")
    Call<BaseResponse<MemberGroup>> sendRequestJoinGroup(@Field("group_id") int groupId, @Field("user_id") int userId);

    @FormUrlEncoded
    @POST("group/v1.0/create")
    Call<BaseResponse<Group>> createGroup(@FieldMap Map<String, Object> map);

    @Multipart
    @POST("group/v1.0/{id}/update-cover-image")
    @Headers({"multipart: true"})
    Call<BaseResponse<String>> updateCoverImageGroup(@Part MultipartBody.Part coverImage, @Path("id") int id);

    @FormUrlEncoded
    @POST("group/v1.0/update/{id}")
    Call<BaseResponse<Group>> updateGroup(@Path("id") int id, @FieldMap Map<String, Object> map);

    @FormUrlEncoded
    @POST("group/v1.0/update-type/{id}")
    Call<BaseResponse<Group>> updateTypeGroup(@Path("id") int id, @Field("type") int type);

    @DELETE("group/v1.0/delete/{id}")
    Call<BaseResponse<String>> deleteGroup(@Path("id") int id);
}
