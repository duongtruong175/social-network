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
import vn.hust.socialnetwork.models.notification.Notification;

public interface NotificationService {
    // Notification
    @GET("notification/v1.0/notifications")
    Call<BaseResponse<List<Notification>>> getNotifications();

    @FormUrlEncoded
    @POST("notification/v1.0/update/{id}")
    Call<BaseResponse<Notification>> updateNotification(@Path("id") int id, @Field("notification_status_code") int notificationStatusCode);

    @POST("notification/v1.0/read-all")
    Call<BaseResponse<String>> readAllNotifications();

    @DELETE("notification/v1.0/delete/{id}")
    Call<BaseResponse<String>> deleteNotification(@Path("id") int id);
}
