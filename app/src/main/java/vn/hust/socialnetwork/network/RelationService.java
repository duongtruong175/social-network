package vn.hust.socialnetwork.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.relation.RelationUser;

public interface RelationService {
    // Relation
    @GET("relation/v1.0/relation/{id}")
    Call<BaseResponse<RelationUser>> getRelation(@Path("id") int userId);

    @GET("relation/v1.0/all-relation")
    Call<BaseResponse<List<RelationUser>>> getAllRelations();

    @FormUrlEncoded
    @POST("relation/v1.0/create")
    Call<BaseResponse<RelationUser>> createRequestFriend(@Field("receiver_id") int receiverId);

    @FormUrlEncoded
    @POST("relation/v1.0/{id}/update")
    Call<BaseResponse<RelationUser>> updateRelation(@Path("id") int id, @Field("friend_status_code") int friendStatusCode);

    @DELETE("relation/v1.0/{id}/delete")
    Call<BaseResponse<String>> deleteRelation(@Path("id") int id);
}
