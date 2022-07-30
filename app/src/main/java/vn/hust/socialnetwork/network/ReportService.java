package vn.hust.socialnetwork.network;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import vn.hust.socialnetwork.models.BaseResponse;

public interface ReportService {
    // report
    @FormUrlEncoded
    @POST("report/v1.0/create")
    Call<BaseResponse<String>> createReport(@FieldMap Map<String, Object> map);
}
