package com.example.locket.ui.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.ViewGroup;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.locket.R;
import com.example.locket.data.api.ApiClient;
import com.example.locket.data.api.ApiService;
import com.example.locket.data.model.Auth.AuthResponse;
import com.example.locket.data.model.Auth.LoginRequest;
import com.example.locket.databinding.FragmentLoginBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null && getArguments().getBoolean("signup_success", false)) {
            Toast.makeText(getContext(), "Đăng ký thành công!", Toast.LENGTH_LONG).show();
        }

        binding.buttonLogin.setOnClickListener(v -> handleLogin());
        binding.textSignUpNow.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_signup);
        });

    }

    private void handleLogin() {
        String username = binding.editTextUsername.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest request = new LoginRequest(username, password);
        ApiService apiService = ApiClient.getApiService();

        apiService.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String accessToken = response.body().getData();
                    String refreshToken = response.body().getRefreshToken();
                    String username = binding.editTextUsername.getText().toString().trim();

                    // Log token để debug
                    Log.d("TOKEN", "accessToken: " + accessToken);
                    Log.d("TOKEN", "refreshToken: " + refreshToken);

                    // Lưu token
                    SharedPreferences prefs = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE);
                    prefs.edit()
                            .putString("accessToken", accessToken)
                            .putString("refreshToken", refreshToken)
                            .putString("username", username)
                            .apply();

                    // Mở lại menu
                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                    navController.navigate(R.id.nav_gallery);
                } else {
                    Toast.makeText(getContext(), "\t \t \t \t \t Đăng nhập thất bại\n Vui lòng kiểm tra tên và password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                android.util.Log.e("LoginFragment", "Lỗi kết nối:", t);
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
