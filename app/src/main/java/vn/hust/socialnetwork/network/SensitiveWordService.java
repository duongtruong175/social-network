package vn.hust.socialnetwork.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import vn.hust.socialnetwork.models.BaseResponse;

public interface SensitiveWordService {
    // sensitive word
    @GET("sensitive-word/v1.0/words")
    Call<BaseResponse<List<String>>> getSensitiveWords();
}
