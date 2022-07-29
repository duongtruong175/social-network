package vn.hust.socialnetwork.network;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.fcm.DataMessageSender;
import vn.hust.socialnetwork.models.fcm.FCMResponse;
import vn.hust.socialnetwork.models.notification.Notification;

public interface NotificationService {
    // Notification
    @GET("notification/v1.0/notifications")
    Call<BaseResponse<List<Notification>>> getNotifications(@QueryMap Map<String, Object> options);

    @FormUrlEncoded
    @POST("notification/v1.0/create")
    Call<BaseResponse<Notification>> createNotification(@FieldMap Map<String, Object> map);

    @FormUrlEncoded
    @POST("notification/v1.0/update/{id}")
    Call<BaseResponse<Notification>> updateNotification(@Path("id") int id, @Field("notification_status_code") int notificationStatusCode);

    @POST("notification/v1.0/read-all")
    Call<BaseResponse<String>> readAllNotifications();

    @DELETE("notification/v1.0/delete/{id}")
    Call<BaseResponse<String>> deleteNotification(@Path("id") int id);

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA97Re2sI:APA91bE8dg1jgYy5_XP6TMUK3gwSagckH5TVvABHP9tvO2irsTpUSXwkbvVOJ0ggTU5xS4kPE2hikRrKnQsMompo1WNfBaKenmD3oDHLV2oGYqIy4lgCnyjjetGiKcbNzw3TW2cy4fgL"
            }
    )
    @POST("fcm/send")
    Call<FCMResponse> sendFCMNotification(@Body DataMessageSender message);
}
