package com.example.locket.ui.avatar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.locket.R;
import com.example.locket.databinding.FragmentAvatarBinding;
import com.example.locket.data.api.ApiClient;
import com.example.locket.data.api.ApiService;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AvatarFragment extends Fragment {
    private FragmentAvatarBinding binding;

    private  EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonSelectAvatar;
    private Button buttonUpdate;
    private Uri selectedImageUri;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private Button buttonDeleteAccount;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAvatarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        editTextUsername = binding.editTextUsername;
        editTextEmail = binding.editTextEmail;
        editTextPassword = binding.editTextPassword;
        buttonSelectAvatar = binding.buttonSelectAvatar;
        buttonUpdate = binding.buttonUpdate;

        // Khởi tạo launcher để chọn ảnh
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                         binding.imageView.setImageURI(selectedImageUri);
                    }
                }
        );

        buttonSelectAvatar.setOnClickListener(v -> openGallery());
        buttonUpdate.setOnClickListener(v -> updateUser());

        buttonDeleteAccount = binding.buttonDeleteAccount;
        buttonDeleteAccount.setOnClickListener(v -> deleteAccount());


        return root;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void updateUser() {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        String username = editTextUsername.getText().toString();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody usernamePart = RequestBody.create(username, MediaType.get("text/plain"));
        RequestBody emailPart = RequestBody.create(email, MediaType.get("text/plain"));
        RequestBody passwordPart = RequestBody.create(password, MediaType.get("text/plain"));

        MultipartBody.Part imagePart = null;

        if (selectedImageUri != null) {
            String filePath = FileUtils.getPath(getContext(), selectedImageUri);
            if (filePath == null) {
                Toast.makeText(getContext(), "Không thể lấy đường dẫn ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            File file = new File(filePath);
            RequestBody requestFile = RequestBody.create(file, MediaType.get("image/*"));
            imagePart = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
        }

        ApiService apiService = ApiClient.getApiService();
        String accessToken = requireContext()
                .getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
                .getString("accessToken", null);

        Call<Void> call = apiService.updateUser(accessToken, imagePart, usernamePart, emailPart, passwordPart);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    SharedPreferences prefs = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE);
                    prefs.edit()
                            .putString("username", username)
                            .apply();
                    NavOptions navOptions = new NavOptions.Builder()
                            .setPopUpTo(R.id.nav_avatar, true) // Xóa nav_avatar khỏi back stack
                            .build();

                    NavHostFragment.findNavController(AvatarFragment.this)
                            .navigate(R.id.nav_gallery, null, navOptions);
                } else {
                    Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void deleteAccount() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận")
                .setMessage("Bạn muốn rời xa Locket hả? Ở lại một xíu nữa nha 🥺")
                .setPositiveButton("Xóa", (dialogInterface, which) -> {
                    String accessToken = requireContext()
                            .getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
                            .getString("accessToken", null);

                    ApiService apiService = ApiClient.getApiService();
                    Call<Void> call = apiService.deleteUser(accessToken);
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Tài khoản đã bị xóa", Toast.LENGTH_SHORT).show();

                                requireContext().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
                                        .edit().clear().apply();

                                NavHostFragment.findNavController(AvatarFragment.this)
                                        .navigate(R.id.nav_login);
                            } else {
                                Toast.makeText(getContext(), "Xóa tài khoản thất bại", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("OK", (dialogInterface, which) -> dialogInterface.dismiss())
                .setCancelable(true)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
