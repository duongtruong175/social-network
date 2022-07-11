package vn.hust.socialnetwork.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.authentication.ChangePasswordRequest;
import vn.hust.socialnetwork.models.authentication.LoginRequest;
import vn.hust.socialnetwork.models.authentication.TokenResponse;
import vn.hust.socialnetwork.models.authentication.RegisterRequest;
import vn.hust.socialnetwork.models.user.User;

public interface AuthenticationService {
    // Auth
    @POST("auth/v1.0/login")
    Call<BaseResponse<TokenResponse>> login(@Body LoginRequest loginRequest);

    @FormUrlEncoded
    @POST("auth/v1.0/forgot-password")
    Call<BaseResponse<User>> forgotPassword(@Field("email") String email);

    @FormUrlEncoded
    @POST("auth/v1.0/resent-otp")
    Call<BaseResponse<String>> resentOtp(@Field("user_id") int userId, @Field("type") int type);

    @FormUrlEncoded
    @POST("auth/v1.0/forgot-password-verify-otp")
    Call<BaseResponse<String>> forgotPasswordVerifyOtp(@Field("code") String code, @Field("user_id") int userId);

    @POST("auth/v1.0/forgot-password-complete")
    Call<BaseResponse<TokenResponse>> forgotPasswordComplete(@Body ChangePasswordRequest changePasswordRequest);

    @POST("auth/v1.0/register")
    Call<BaseResponse<User>> register(@Body RegisterRequest registerRequest);

    @FormUrlEncoded
    @POST("auth/v1.0/register-verify-otp")
    Call<BaseResponse<TokenResponse>> registerVerifyOtp(@Field("code") String code, @Field("user_id") int userId);

    @POST("auth/v1.0/logout")
    Call<BaseResponse<String>> logout();
}
