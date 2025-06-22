package com.example.locket;

import com.example.locket.adapter.NotificationAdapter;
import com.example.locket.data.model.Users.*;
import com.example.locket.data.model.Notifications.*;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.locket.data.api.ApiClient;
import com.example.locket.data.api.ApiService;
import com.example.locket.data.model.Auth;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locket.databinding.ActivityMainBinding;

import android.app.AlertDialog;
import android.graphics.Color;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    private BottomSheetDialog bottomSheetDialog;
    private NotificationAdapter notificationAdapter;

    private boolean checkLogin() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);
        return accessToken != null && !accessToken.isEmpty();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;



        // Khởi tạo ApiClient context cho Interceptor
        com.example.locket.data.api.ApiClient.init(this);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_login, R.id.nav_gallery, R.id.nav_info, R.id.nav_avatar, R.id.nav_friend, R.id.nav_logout)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        if (!checkLogin()) {
            navController.navigate(R.id.nav_login);
            Menu menu = navigationView.getMenu();
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                if (item.getItemId() != R.id.nav_login) {
                    item.setVisible(false);
                }
            }
        }

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean isLoginOrSignup = destination.getId() == R.id.nav_login || destination.getId() == R.id.nav_signup;
            binding.appBarMain.toolbar.setVisibility(isLoginOrSignup ? View.GONE : View.VISIBLE);
            binding.drawerLayout.setDrawerLockMode(
                    isLoginOrSignup ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED
            );
            if (destination.getId() == R.id.nav_gallery) {
                loadNotificationsOnStart();
            }
        });

        binding.drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                loadUserProfile();
            }
        });

        // Thêm xử lý click logout
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_logout) {
                androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Đăng xuất")
                        .setMessage("Bạn có muốn đăng xuất tài khoản không?")
                        .setPositiveButton("Đồng ý", (dialogInterface, which) -> {
                            SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                            String refreshToken = prefs.getString("refreshToken", null);
                            ApiService apiService = ApiClient.getApiService();
                            Auth.TokenRequest request = new Auth().new TokenRequest(refreshToken);
                            apiService.logout(request).enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    prefs.edit().clear().apply();
                                    navController.navigate(R.id.nav_login);
                                    Toast.makeText(MainActivity.this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(MainActivity.this, "Lỗi khi đăng xuất", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Hủy bỏ", (dialogInterface, which) -> dialogInterface.dismiss())
                        .create();

                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                drawer.closeDrawers();
                return true;
            } else {
                boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
                if (handled) drawer.closeDrawers();
                return handled;
            }
        });
    }

    private void loadUserProfile() {
        String accessToken = getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
                .getString("accessToken", null);
        ApiService apiService = ApiClient.getApiService();
        apiService.getProfile(accessToken).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();

                    NavigationView navigationView = binding.navView;
                    View headerView = navigationView.getHeaderView(0);

                    TextView textViewUsername = headerView.findViewById(R.id.textUserName);
                    TextView textViewEmail = headerView.findViewById(R.id.textMail);
                    ImageView imageView = headerView.findViewById(R.id.imageView);

                    textViewUsername.setText(user.getUsername());
                    textViewEmail.setText(user.getEmail());

                    Glide.with(MainActivity.this)
                            .load(user.getAvatar_url())
                            .placeholder(R.drawable.default_avatar)
                            .into(imageView);
                } else {
                    Log.e("GET_PROFILE", "Không thể lấy thông tin user: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("GET_PROFILE", "Lỗi mạng khi lấy profile", t);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_notifications) {
            handleNotificationClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleNotificationClick() {
        // Nếu dialog đang mở, chỉ cần đóng nó lại
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
            return;
        }

        String accessToken = getSharedPreferences("auth", MODE_PRIVATE).getString("accessToken", null);
        ApiService apiService = ApiClient.getApiService();

        // 1. Luôn lấy thông báo mới nhất khi nhấn nút
        apiService.getNotifications(accessToken).enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Notification> notiList = response.body();
                    if (notiList.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Không có thông báo mới nào", Toast.LENGTH_SHORT).show();
                    } else {
                        // 2. Hiển thị thông báo
                        showBottomSheet(notiList);

                        // 3. Tự động đánh dấu đã đọc trên server
                        apiService.markNotificationsAsRead(accessToken).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Log.d("Notifications", "Đã đánh dấu tất cả thông báo là đã đọc.");
                                } else {
                                    Log.e("Notifications", "Lỗi khi đánh dấu đã đọc.");
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Log.e("Notifications", "Lỗi mạng khi đánh dấu đã đọc.", t);
                            }
                        });
                    }
                } else {
                    Log.e("API_ERROR", "Mã lỗi: " + response.code());
                    Log.e("API_ERROR", "Message: " + response.message());
                    Toast.makeText(MainActivity.this, "Không thể lấy danh sách thông báo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi kết nối khi lấy thông báo", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showBottomSheet(List<Notification> notificationList) {
        bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_notifications, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        RecyclerView recyclerView = bottomSheetView.findViewById(R.id.rvNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationAdapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(notificationAdapter);
        bottomSheetDialog.show();
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void loadNotificationsOnStart() {
        String accessToken = getSharedPreferences("auth", MODE_PRIVATE)
                .getString("accessToken", null);
        ApiService apiService = ApiClient.getApiService();
        apiService.getNotifications(accessToken).enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Notification> notiList = response.body();
                    if (!notiList.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Bạn có thông báo mới", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("Thông báo", "Không thể tải: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                Log.e("Thông báo", "Lỗi kết nối khi tải: " + t.getMessage());
            }
        });
    }

}