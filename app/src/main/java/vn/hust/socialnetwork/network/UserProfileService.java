package vn.hust.socialnetwork.network;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.post.Post;
import vn.hust.socialnetwork.models.user.Relation;
import vn.hust.socialnetwork.models.user.User;
import vn.hust.socialnetwork.models.userprofile.ChangeAvatarResponse;
import vn.hust.socialnetwork.models.userprofile.ChangeCoverImageResponse;

public interface UserProfileService {
    // User Profile
    @GET("user-profile/v1.0/profile/{id}")
    Call<BaseResponse<User>> getUserProfileById(@Path("id") int id);

    @Multipart
    @POST("user-profile/v1.0/profile/{id}/update-avatar")
    @Headers({"multipart: true"})
    Call<BaseResponse<ChangeAvatarResponse>> updateAvatar(@Part MultipartBody.Part avatar, @Path("id") int id);

    @Multipart
    @POST("user-profile/v1.0/profile/{id}/update-cover-image")
    @Headers({"multipart: true"})
    Call<BaseResponse<ChangeCoverImageResponse>> updateCoverImage(@Part MultipartBody.Part coverImage, @Path("id") int id);

    @GET("user-profile/v1.0/profile/{id}/photo-album")
    Call<BaseResponse<List<String>>> getPhotoAlbum(@Path("id") int id);

    @GET("user-profile/v1.0/profile/{id}/video-album")
    Call<BaseResponse<List<String>>> getVideoAlbum(@Path("id") int id);

    @GET("user-profile/v1.0/profile/{id}/top-friend")
    Call<BaseResponse<List<Relation>>> getTopFriends(@Path("id") int id);

    @GET("user-profile/v1.0/profile/{id}/all-friend")
    Call<BaseResponse<List<Relation>>> getAllFriends(@Path("id") int id);

    @GET("user-profile/v1.0/profile/{id}/posts")
    Call<BaseResponse<List<Post>>> getPosts(@Path("id") int id);
}
