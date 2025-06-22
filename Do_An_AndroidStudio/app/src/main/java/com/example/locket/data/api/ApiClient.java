package com.example.locket.data.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.example.locket.data.model.Auth;
import com.example.locket.data.model.Auth.AccessTokenResponse;
import com.example.locket.data.model.Auth.TokenRequest;

public class ApiClient {
    private static final String baseUrl = "http://10.0.2.2:3000/";
    private static Retrofit retrofit = null;
    private static OkHttpClient okHttpClient = null;
    private static Context appContext;
    private static final Object lock = new Object(); // đồng bộ refresh token

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor())
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getRetrofit().create(ApiService.class);
    }

    public static class AuthInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();

            SharedPreferences prefs = appContext.getSharedPreferences("auth", Context.MODE_PRIVATE);
            String accessToken = prefs.getString("accessToken", null);
            String refreshToken = prefs.getString("refreshToken", null);


            // Thêm access token vào header nếu có
            Request.Builder builder = originalRequest.newBuilder();
            if (accessToken != null && !accessToken.isEmpty()) {
                builder.header("Authorization", "Bearer " + accessToken);
            }

            Response response = chain.proceed(builder.build());

            if (response.code() == 401 && refreshToken != null) {
                synchronized (lock) {
                    // Check lại nếu đã được thread khác làm mới token
                    String newAccessToken = prefs.getString("accessToken", null);
                    if (newAccessToken == null || newAccessToken.equals(accessToken)) {
                        newAccessToken = refreshAccessToken(refreshToken);
                    }

                    if (newAccessToken != null) {
                        prefs.edit().putString("accessToken", newAccessToken).apply();

                        // Retry lại request cũ với token mới
                        Request newRequest = originalRequest.newBuilder()
                                .header("Authorization", "Bearer " + newAccessToken)
                                .build();
                        response.close();
                        return chain.proceed(newRequest);
                    } else {
                        // Nếu không refresh được → xóa token, có thể chuyển về login
                        prefs.edit().clear().apply();
                    }
                }
            }

            return response;
        }

        private String refreshAccessToken(String refreshToken) {
            try {
                // Tạo Retrofit riêng không có Interceptor
                OkHttpClient client = new OkHttpClient.Builder().build();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                ApiService apiService = retrofit.create(ApiService.class);
                TokenRequest request = new Auth().new TokenRequest(refreshToken);
                retrofit2.Response<AccessTokenResponse> response = apiService.refreshToken(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    return response.body().getData();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
