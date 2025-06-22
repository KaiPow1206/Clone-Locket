package com.example.locket.data.api;


import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;
import com.example.locket.data.model.Auth.*;
import com.example.locket.data.model.Users.*;
import com.example.locket.data.model.Photos.*;
import com.example.locket.data.model.Photo_Reaction.*;
import com.example.locket.data.model.Notifications.*;




import java.util.List;
import java.util.Map;

public interface ApiService {

    // ---------- AUTH ----------
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/sign-up")
    Call<AuthResponse> signUp(@Body SignUpRequest request);

    @POST("auth/refresh-token")
    Call<AccessTokenResponse> refreshToken(@Body TokenRequest request);

    @POST("auth/logout")
    Call<Void> logout(@Body TokenRequest request);


    // ---------- USER ----------
    @GET("users/get-profile")
    Call<User> getProfile(@Header("token") String token);
    @GET("users/get-users")
    Call<User> getFriend(@Header("token") String token);

    @DELETE("users/delete-user")
    Call<Void> deleteUser(@Header("token") String token);

    @Multipart
    @PUT("users/update-user")
    Call<Void> updateUser(
            @Header("token") String token,
            @Part MultipartBody.Part avatar,
            @Part("username") RequestBody username,
            @Part("email") RequestBody email,
            @Part("password") RequestBody password
    );


    @GET("users/get-sentrequests")
    Call<List<FriendRequest>> getSentRequests(@Header("token") String accessToken);

    @GET("users/get-allrequests")
    Call<List<FriendRequest>> getFriendRequests(@Header("token") String accessToken);

    @POST("users/add-friend")
    Call<Void> addFriend(@Header("token") String token, @Body Map<String, String> request);

    @POST("users/un-friend")
    Call<Void> unFriend(@Header("token") String token, @Body FriendRequest request);


    @PATCH("users/accept-friend")
    Call<Void> acceptFriend(@Header("token") String token, @Body FriendRequest request);

    @PATCH("users/reject-friend")
    Call<Void> rejectFriend(@Header("token") String token, @Body FriendRequest request);

    @GET("users/search-users")
    Call<List<SearchUserResponse>> searchUsers(@Header("token") String token, @Query("name") String name);


    // ---------- PHOTOS ----------
    @GET("photo/get-photos")
    Call<List<Photo>> getPhotos(@Header("token") String token);

    @Multipart
    @POST("photo/post-photos")
    Call<Photo> postPhoto(
            @Header("token") String token,
            @Part MultipartBody.Part image,
            @Part("caption") RequestBody caption
    );

    @DELETE("photo/delete-photo/{photoId}")
    Call<Void> deletePhoto(@Header("token") String token, @Path("photoId") int photoId);


    // ---------- REACTIONS ----------
    @GET("photo-reaction/all-reactions")
    Call<ReactionSummary> getReactions(@Header("token") String token, @Query("photoId") String photoId);

    @POST("photo-reaction/add-reaction")
    Call<Void> addReaction(@Header("token") String token, @Body ReactionRequest request);

    @HTTP(method = "DELETE", path = "photo-reaction/delete-reaction", hasBody = true)
    Call<Void> deleteReaction(@Header("token") String token, @Body DeleteReactionRequest request);


    // ---------- NOTIFICATIONS ----------
    @GET("noti/all-noti")
    Call<List<Notification>> getNotifications(@Header("token") String token);

    @PUT("noti/read-noti")
    Call<Void> markNotificationsAsRead(@Header("token") String token);
}