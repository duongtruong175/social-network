package vn.hust.socialnetwork.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.chat.Conversation;

public interface ChatService {
    // chat
    @GET("chat/v1.0/conversations")
    Call<BaseResponse<List<Conversation>>> getConversations();

    @FormUrlEncoded
    @POST("chat/v1.0/create-conversation")
    Call<BaseResponse<Conversation>> createConversation(@Field("receiver_id") int receiverId);
}
