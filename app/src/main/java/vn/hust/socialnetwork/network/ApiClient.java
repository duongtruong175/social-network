package vn.hust.socialnetwork.network;

import androidx.annotation.NonNull;

import com.orhanobut.hawk.Hawk;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.hust.socialnetwork.utils.AppSharedPreferences;

public class ApiClient {
    private static final String BASE_URL = "http://192.168.43.2:8000/api/";
    private static String token = "";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        token = Hawk.get(AppSharedPreferences.TOKEN_KEY, "");

        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @NonNull
            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {
                Request request = chain.request().newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .build();
                return chain.proceed(request);
            }
        }).build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit;
    }
}
