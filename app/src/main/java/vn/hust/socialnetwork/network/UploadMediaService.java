package vn.hust.socialnetwork.network;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.media.Media;

public interface UploadMediaService {
    // Upload media
    @Multipart
    @POST("media/v1.0/upload")
    @Headers({"multipart: true"})
    Call<BaseResponse<Media>> uploadMedia(@Part MultipartBody.Part media, @Part("type") RequestBody type);
}
