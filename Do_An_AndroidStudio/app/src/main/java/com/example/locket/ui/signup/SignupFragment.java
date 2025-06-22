package com.example.locket.ui.signup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.locket.R;
import com.example.locket.data.api.ApiClient;
import com.example.locket.data.api.ApiService;
import com.example.locket.data.model.Auth;
import com.example.locket.data.model.Auth.AuthResponse;
import com.example.locket.databinding.FragmentSignupBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupFragment extends Fragment {
    private FragmentSignupBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonSignUp.setOnClickListener(v -> handleSignUp());
        binding.textLoginNow.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_login);
        });
    }

    private void handleSignUp() {
        String email = binding.editTextMail.getText().toString().trim();
        String username = binding.editTextUsername.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();

        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Auth.SignUpRequest request = new Auth().new SignUpRequest(username, email, password);
        ApiService apiService = ApiClient.getApiService();
        apiService.signUp(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Chuyển về màn hình đăng nhập và truyền bundle báo hiệu đăng ký thành công
                    androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(requireActivity(), com.example.locket.R.id.nav_host_fragment_content_main);
                    android.os.Bundle bundle = new android.os.Bundle();
                    bundle.putBoolean("signup_success", true);
                    navController.navigate(com.example.locket.R.id.nav_login, bundle);
                } else {
                    Toast.makeText(getContext(), "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
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
