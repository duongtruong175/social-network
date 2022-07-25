package vn.hust.socialnetwork.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.search.SearchResult;

public interface SearchService {
    // search
    @GET("search/v1.0/search-all")
    Call<BaseResponse<SearchResult>> searchAll(@Query("q") String keyword);
}
