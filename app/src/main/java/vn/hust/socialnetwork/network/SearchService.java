package vn.hust.socialnetwork.network;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.group.Group;
import vn.hust.socialnetwork.models.post.Post;
import vn.hust.socialnetwork.models.search.History;
import vn.hust.socialnetwork.models.search.SearchResult;
import vn.hust.socialnetwork.models.user.User;

public interface SearchService {
    // search
    @GET("search/v1.0/histories")
    Call<BaseResponse<List<History>>> getHistories();

    @FormUrlEncoded
    @POST("search/v1.0/histories")
    Call<BaseResponse<History>> addHistory(@Field("query") String query);

    @DELETE("search/v1.0/histories/{id}")
    Call<BaseResponse<String>> deleteHistory(@Path("id") int id);

    @GET("search/v1.0/search-all")
    Call<BaseResponse<SearchResult>> searchAll(@QueryMap Map<String, Object> options);

    @GET("search/v1.0/search-user")
    Call<BaseResponse<List<User>>> searchUser(@QueryMap Map<String, Object> options);

    @GET("search/v1.0/search-group")
    Call<BaseResponse<List<Group>>> searchGroup(@QueryMap Map<String, Object> options);

    @GET("search/v1.0/search-post")
    Call<BaseResponse<List<Post>>> searchPost(@QueryMap Map<String, Object> options);
}
