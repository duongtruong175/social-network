package vn.hust.socialnetwork.network;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.group.Group;
import vn.hust.socialnetwork.models.post.Post;
import vn.hust.socialnetwork.models.search.SearchResult;
import vn.hust.socialnetwork.models.user.User;

public interface SearchService {
    // search
    @GET("search/v1.0/search-all")
    Call<BaseResponse<SearchResult>> searchAll(@QueryMap Map<String, Object> options);

    @GET("search/v1.0/search-user")
    Call<BaseResponse<List<User>>> searchUser(@QueryMap Map<String, Object> options);

    @GET("search/v1.0/search-group")
    Call<BaseResponse<List<Group>>> searchGroup(@QueryMap Map<String, Object> options);

    @GET("search/v1.0/search-post")
    Call<BaseResponse<List<Post>>> searchPost(@QueryMap Map<String, Object> options);
}
