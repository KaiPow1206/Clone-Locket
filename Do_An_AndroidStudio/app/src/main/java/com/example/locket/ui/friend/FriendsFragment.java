package com.example.locket.ui.friend;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.locket.R;
import com.example.locket.data.api.ApiClient;
import com.example.locket.data.api.ApiService;
import com.example.locket.data.model.Users.FriendRequest;

import com.example.locket.data.model.Users.SearchUserResponse;
import com.example.locket.data.model.Users.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendsFragment extends Fragment {

    private EditText edtSearchFriend;
    private LinearLayout layoutFriendList;
    private TextView textNoFriends;

    private LinearLayout layoutFriendRequestAction;

    private TextView textRequestMessage;
    private Button btnAccept, btnReject;

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    public FriendsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend, container, false);

        edtSearchFriend = view.findViewById(R.id.edtSearchFriend);
        layoutFriendList = view.findViewById(R.id.layoutFriendList);
        textNoFriends = view.findViewById(R.id.textNoFriends);

        fetchFriends();

        edtSearchFriend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchRunnable = () -> {
                    if (getActivity() == null) {
                        return;
                    }
                    String name = s.toString().trim();
                    if (!TextUtils.isEmpty(name)) {
                        searchUsers(name);
                    } else {
                        fetchFriends();
                    }
                };
                searchHandler.postDelayed(searchRunnable, 500); // Delay 500ms
            }
        });

        layoutFriendRequestAction = view.findViewById(R.id.layoutFriendRequestAction);

        checkFriendRequests();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchFriends();
    }

    private void fetchFriends() {
        String accessToken = requireContext()
                .getSharedPreferences("auth", Context.MODE_PRIVATE)
                .getString("accessToken", null);

        ApiService apiService = ApiClient.getApiService();
        Call<User> call = apiService.getFriend(accessToken);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User currentUser = response.body();
                    List<SearchUserResponse> friends = currentUser.getFriends();

                    layoutFriendList.removeAllViews();
                    if (friends != null && !friends.isEmpty()) {
                        for (SearchUserResponse user : friends) {
                            addUserViewWithStatus(user, null);
                        }
                        textNoFriends.setVisibility(View.GONE);
                    } else {
                        textNoFriends.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(getContext(), "Không thể lấy danh sách bạn bè", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", "Lỗi khi gọi API: " + t.getMessage(), t);
            }
        });
    }

    private void searchUsers(String name) {
        String accessToken = requireContext()
                .getSharedPreferences("auth", Context.MODE_PRIVATE)
                .getString("accessToken", null);

        ApiService apiService = ApiClient.getApiService();
        Call<List<SearchUserResponse>> searchCall = apiService.searchUsers(accessToken, name);

        searchCall.enqueue(new Callback<List<SearchUserResponse>>() {
            @Override
            public void onResponse(Call<List<SearchUserResponse>> call, Response<List<SearchUserResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SearchUserResponse> results = response.body();

                    if (results.isEmpty()) {
                        layoutFriendList.removeAllViews();
                        Toast.makeText(getContext(), "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
                    } else {
                        Call<List<FriendRequest>> requestCall = apiService.getFriendRequests(accessToken);
                        requestCall.enqueue(new Callback<List<FriendRequest>>() {
                            @Override
                            public void onResponse(Call<List<FriendRequest>> call, Response<List<FriendRequest>> response) {
                                Map<String, String> requestStatusMap = new HashMap<>();

                                if (response.isSuccessful() && response.body() != null) {
                                    for (FriendRequest r : response.body()) {
                                        String uname = r.getUsername();
                                        String status = r.getStatus();
                                        requestStatusMap.put(uname.toLowerCase(), status);
                                    }
                                }

                                showSearchResults(results, requestStatusMap);
                            }

                            @Override
                            public void onFailure(Call<List<FriendRequest>> call, Throwable t) {
                                Toast.makeText(getContext(), "Lỗi kết nối kiểm tra lời mời", Toast.LENGTH_SHORT).show();
                                showSearchResults(results, new HashMap<>());
                            }
                        });
                    }

                } else {
                    Toast.makeText(getContext(), "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
                    layoutFriendList.removeAllViews();
                    textNoFriends.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<SearchUserResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSearchResults(List<SearchUserResponse> results, Map<String, String> requestStatusMap) {
        layoutFriendList.removeAllViews();

        if (results.isEmpty()) {
            textNoFriends.setVisibility(View.VISIBLE);
        } else {
            for (SearchUserResponse user : results) {
                String status = requestStatusMap.getOrDefault(user.getUsername().toLowerCase(), "");
                addUserViewWithStatus(user, status);
            }
            textNoFriends.setVisibility(View.GONE);
        }
    }

    private void addUserViewWithStatus(SearchUserResponse user, String status) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View userView = inflater.inflate(R.layout.item_user, layoutFriendList, false);
        ImageView imgAvatar = userView.findViewById(R.id.imgAvatar);
        TextView textUsername = userView.findViewById(R.id.textUsername);
        Button btnAddFriend = userView.findViewById(R.id.btnAddFriend);
        Button btnRemoveFriend = userView.findViewById(R.id.btnRemoveFriend);

        textUsername.setText(user.getUsername());

        Glide.with(this)
                .load(user.getAvatar_url())
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(imgAvatar);

        if (status == null) {
            btnRemoveFriend.setVisibility(View.VISIBLE);
            btnRemoveFriend.setOnClickListener(v -> unFriend(user.getUsername(), btnRemoveFriend));
            btnRemoveFriend.setEnabled(true);

            btnAddFriend.setVisibility(View.GONE);
            btnAddFriend.setEnabled(false);
        } else if ("pending".equals(status)) {
            btnAddFriend.setText("Đã gửi");
            btnAddFriend.setEnabled(false);
            btnAddFriend.setBackgroundColor(Color.GRAY);
            btnAddFriend.setVisibility(View.VISIBLE);

            btnRemoveFriend.setVisibility(View.GONE);
            btnRemoveFriend.setEnabled(false);
        } else {
            btnAddFriend.setText("Kết bạn");
            btnAddFriend.setEnabled(true);
            btnAddFriend.setOnClickListener(v -> sendFriendRequest(user.getUsername(), btnAddFriend));
            btnAddFriend.setVisibility(View.VISIBLE);
            btnRemoveFriend.setEnabled(false);
            btnRemoveFriend.setVisibility(View.GONE);
        }

        layoutFriendList.addView(userView);
    }

    private void sendFriendRequest(String friendUsername, Button btnAddFriend) {
        String accessToken = requireContext()
                .getSharedPreferences("auth", Context.MODE_PRIVATE)
                .getString("accessToken", null);

        ApiService apiService = ApiClient.getApiService();

        // Tạo JSON body đúng định dạng
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("friendUsername", friendUsername);

        Call<Void> call = apiService.addFriend(accessToken, requestBody);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã gửi lời mời kết bạn", Toast.LENGTH_SHORT).show();
                    btnAddFriend.setText("Đã gửi");
                    btnAddFriend.setEnabled(false);
                    btnAddFriend.setBackgroundColor(Color.GRAY);
                } else {
                    Log.e("API_ERROR", "Mã lỗi: " + response.code());
                    Log.e("API_ERROR", "Message: " + response.message());
                    Toast.makeText(getContext(), "Không thể gửi lời mời", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi gửi lời mời: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkFriendRequests() {
        String token = requireContext()
                .getSharedPreferences("auth", Context.MODE_PRIVATE)
                .getString("accessToken", null);

        ApiService apiService = ApiClient.getApiService();
        Call<List<FriendRequest>> call = apiService.getSentRequests(token);

        call.enqueue(new Callback<List<FriendRequest>>() {
            @Override
            public void onResponse(Call<List<FriendRequest>> call, Response<List<FriendRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FriendRequest> requests = response.body();
                    layoutFriendRequestAction.removeAllViews();

                    boolean hasRequest = false;

                    for (FriendRequest req : requests) {
                        if ("pending".equals(req.getStatus())) {
                            hasRequest = true;
                            addRequestView(req.getUsername());
                        }
                    }

                    layoutFriendRequestAction.setVisibility(hasRequest ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<FriendRequest>> call, Throwable t) {
                Log.e("FriendReq", "Lỗi lấy danh sách yêu cầu", t);
            }
        });
    }

    private void addRequestView(String username) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View requestView = inflater.inflate(R.layout.friend_request_action, layoutFriendRequestAction, false);

        TextView textMessage = requestView.findViewById(R.id.textRequestMessage);
        Button btnAccept = requestView.findViewById(R.id.btnAccept);
        Button btnReject = requestView.findViewById(R.id.btnReject);

        textMessage.setText(username + " đã gửi lời mời kết bạn");

        btnAccept.setOnClickListener(v -> acceptFriend(username, requestView));

        btnReject.setOnClickListener(v -> rejectFriend(username, requestView));

        layoutFriendRequestAction.addView(requestView);
    }


    private void acceptFriend(String username, View requestView) {
        String accessToken = requireContext()
                .getSharedPreferences("auth", Context.MODE_PRIVATE)
                .getString("accessToken", null);
        ApiService apiService = ApiClient.getApiService();
        FriendRequest request = new FriendRequest(username);

        apiService.acceptFriend(accessToken, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isAdded() && response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã chấp nhận lời mời của " + username, Toast.LENGTH_SHORT).show();
                    layoutFriendRequestAction.removeView(requestView);
                    if (layoutFriendRequestAction.getChildCount() == 0) {
                        layoutFriendRequestAction.setVisibility(View.GONE);
                    }
                    fetchFriends();
                } else if (isAdded()) {
                    Log.e("API_ERROR", "Mã lỗi: " + response.code());
                    Log.e("API_ERROR", "Message: " + response.message());
                    Toast.makeText(getContext(), "Không thể chấp nhận lời mời", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if(isAdded()) {
                    Toast.makeText(getContext(), "Lỗi kết nối khi chấp nhận", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void rejectFriend(String username, View requestView) {
        String accessToken = requireContext()
                .getSharedPreferences("auth", Context.MODE_PRIVATE)
                .getString("accessToken", null);
        ApiService apiService = ApiClient.getApiService();
        FriendRequest request = new FriendRequest(username);

        apiService.rejectFriend(accessToken, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isAdded() && response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã từ chối lời mời của " + username, Toast.LENGTH_SHORT).show();
                    layoutFriendRequestAction.removeView(requestView);
                    if (layoutFriendRequestAction.getChildCount() == 0) {
                        layoutFriendRequestAction.setVisibility(View.GONE);
                    }
                    fetchFriends();
                } else if (isAdded()) {
                    Log.e("API_ERROR", "Mã lỗi: " + response.code());
                    Log.e("API_ERROR", "Message: " + response.message());
                    Toast.makeText(getContext(), "Không thể từ chối lời mời", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if(isAdded()) {
                    Toast.makeText(getContext(), "Lỗi kết nối khi từ chối", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void unFriend(String friendUsername, Button btnRemoveFriend) {
        String accessToken = requireContext()
                .getSharedPreferences("auth", Context.MODE_PRIVATE)
                .getString("accessToken", null);

        ApiService apiService = ApiClient.getApiService();
        FriendRequest request = new FriendRequest(friendUsername);

        Call<Void> call = apiService.unFriend(accessToken, request);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã hủy kết bạn thành công", Toast.LENGTH_SHORT).show();
                    fetchFriends();
                } else {
                    Log.e("API_ERROR", "Mã lỗi: " + response.code());
                    Log.e("API_ERROR", "Message: " + response.message());
                    Toast.makeText(getContext(), "Không thể hủy kết bạn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
