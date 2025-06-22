package com.example.locket.ui.gallery;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.locket.R;
import com.example.locket.data.api.ApiClient;
import com.example.locket.data.api.ApiService;
import com.example.locket.data.model.Photo_Reaction;
import com.example.locket.data.model.Photos;
import com.example.locket.databinding.FragmentGalleryBinding;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private Uri imageUri;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private View noPhotoView;
    private View photoFullView;
    private int currentPhotoIndex = 0;
    private List<Photos.Photo> currentPhotoList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        if (imageUri != null) {
                            loadImageAndShowCaption(imageUri);
                        }
                    }
                });

        binding.btnUpload.setOnClickListener(v -> {
            if (!hasImagePermission()) {
                requestImagePermission();
                return;
            }
            if (imageUri == null) {
                Toast.makeText(getContext(), "Vui lòng chọn ảnh trước", Toast.LENGTH_SHORT).show();
                return;
            }
            String captionText = binding.editCaption.getText().toString().trim();
            uploadPhotoToServer(captionText);
        });

        binding.btnPickImage.setOnClickListener(v -> {
            if (!hasImagePermission()) {
                requestImagePermission();
                return;
            }
            openGallery();
        });

        binding.editCaption.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String caption = s.toString().trim();
                if (!caption.isEmpty()) {
                    binding.textCaptionOverlay.setText(caption);
                    binding.textCaptionOverlay.setVisibility(View.VISIBLE);
                } else {
                    binding.textCaptionOverlay.setVisibility(View.GONE);
                }
            }
        });

        binding.btnScrollDown.setOnClickListener(v -> fetchPhotos());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }



        return binding.getRoot();
    }

    private void uploadPhotoToServer(String caption) {
        try {
            String filePath = RealPathUtil.getRealPath(requireContext(), imageUri);
            java.io.File file = new java.io.File(filePath);
            RequestBody requestFile = RequestBody.create(file, okhttp3.MediaType.parse("image/*"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
            RequestBody captionPart = RequestBody.create(caption, okhttp3.MultipartBody.FORM);
            String accessToken = requireContext().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE).getString("accessToken", null);
            ApiService apiService = ApiClient.getApiService();
            apiService.postPhoto(accessToken, body, captionPart).enqueue(new Callback<Photos.Photo>() {
                @Override
                public void onResponse(Call<Photos.Photo> call, Response<Photos.Photo> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Tải ảnh lên thành công", Toast.LENGTH_SHORT).show();
                        imageUri = null;
                        binding.imageDisplay.setImageDrawable(null);
                        binding.editCaption.setText("");
                        binding.textCaptionOverlay.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(getContext(), "Tải ảnh thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Photos.Photo> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void loadImageAndShowCaption(Uri uri) {
        try {
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ImageDecoder.Source source = ImageDecoder.createSource(requireActivity().getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
            }
            binding.imageDisplay.setImageBitmap(bitmap);
            String caption = binding.editCaption.getText().toString().trim();
            if (!caption.isEmpty()) {
                binding.textCaptionOverlay.setText(caption);
                binding.textCaptionOverlay.setVisibility(View.VISIBLE);
            } else {
                binding.textCaptionOverlay.setVisibility(View.GONE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showNoPhotoView() {
        binding.cardImageContainer.setVisibility(View.GONE);
        binding.captionLayout.setVisibility(View.GONE);
        binding.btnScrollDown.setVisibility(View.GONE);

        if (noPhotoView == null) {
            noPhotoView = getLayoutInflater().inflate(R.layout.item_no_photo, (ViewGroup) binding.getRoot(), false);
            noPhotoView.setClickable(true);
            noPhotoView.setFocusable(true);
            ((ViewGroup) binding.getRoot()).addView(noPhotoView);
        } else {
            noPhotoView.setVisibility(View.VISIBLE);
        }

        final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffY = e2.getY() - e1.getY();
                if (diffY > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    hidePhotoFullView();
                    if (noPhotoView != null) noPhotoView.setVisibility(View.GONE);
                    return true;
                }
                return false;
            }
        });
        noPhotoView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

    }

    private void showPhotoFullView(int index) {
        binding.cardImageContainer.setVisibility(View.GONE);
        binding.captionLayout.setVisibility(View.GONE);
        binding.btnScrollDown.setVisibility(View.GONE);

        if (photoFullView == null) {
            photoFullView = getLayoutInflater().inflate(R.layout.item_photo_full, (ViewGroup) binding.getRoot(), false);
            ((ViewGroup) binding.getRoot()).addView(photoFullView);
            photoFullView.setClickable(true);
            photoFullView.setFocusable(true);

        } else {
            photoFullView.setVisibility(View.VISIBLE);
        }

        ImageView imageMain = photoFullView.findViewById(R.id.imageMain);
        Glide.with(this).load(currentPhotoList.get(index).getImage_url()).into(imageMain);
        int photoId = currentPhotoList.get(index).getId();
        getReactionsForPhoto(photoId);

        TextView textCaptionFull = photoFullView.findViewById(R.id.textCaptionFull);
        String caption = currentPhotoList.get(index).getCaption();
        if (caption != null && !caption.isEmpty()) {
            textCaptionFull.setText(caption);
            textCaptionFull.setVisibility(View.VISIBLE);
        } else {
            textCaptionFull.setVisibility(View.GONE);
        }

        TextView textUploaderFull = photoFullView.findViewById(R.id.textUploaderFull);
        ImageButton btnDeletePhoto = photoFullView.findViewById(R.id.btnDeletePhoto);
        String uploaderName = null;
        if (currentPhotoList.get(index).getUser() != null) {
            uploaderName = currentPhotoList.get(index).getUser().getUsername();
        }
        String myUsername = requireContext().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
                .getString("username", null);
        if (myUsername != null && myUsername.equals(uploaderName)) {
            textUploaderFull.setText("Bạn");
            btnDeletePhoto.setVisibility(View.VISIBLE);
            btnDeletePhoto.setOnClickListener(v -> {
                AlertDialog dialog = new AlertDialog.Builder(requireContext())
                        .setTitle("Xóa ảnh")
                        .setMessage("Bạn có chắc muốn xóa ảnh này?")
                        .setPositiveButton("Xóa", (dialogInterface, which) -> {
                            String accessToken = requireContext().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
                                    .getString("accessToken", null);

                            ApiService apiService = ApiClient.getApiService();
                            apiService.deletePhoto(accessToken, photoId).enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(getContext(), "Xóa ảnh thành công", Toast.LENGTH_SHORT).show();
                                        currentPhotoList.remove(index);

                                        if (!currentPhotoList.isEmpty()) {
                                            if (index >= currentPhotoList.size()) {
                                                currentPhotoIndex = currentPhotoList.size() - 1;
                                            } else {
                                                currentPhotoIndex = index;
                                            }
                                            showPhotoFullView(currentPhotoIndex);
                                        } else {
                                            hidePhotoFullView();
                                            showNoPhotoView();
                                        }
                                    } else {
                                        Toast.makeText(getContext(), "Xóa ảnh thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(getContext(), "Lỗi khi xóa ảnh: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Hủy", null)
                        .create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            });
        } else {
            textUploaderFull.setText(uploaderName);
            btnDeletePhoto.setVisibility(View.GONE);
        }

        ImageButton btnSave = photoFullView.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            Drawable drawable = imageMain.getDrawable();
            if (drawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                saveImageToGallery(bitmap);
            } else {
                Toast.makeText(getContext(), "Không tìm thấy ảnh để lưu", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton btnLove = photoFullView.findViewById(R.id.btnLove);
        ImageButton btnWow = photoFullView.findViewById(R.id.btnWow);
        ImageButton btnSad = photoFullView.findViewById(R.id.btnSad);
        ImageButton btnHaha = photoFullView.findViewById(R.id.btnHaha);

        btnLove.setTag("Love");
        btnWow.setTag("Wow");
        btnSad.setTag("Sad");
        btnHaha.setTag("Haha");

        View.OnClickListener reactionClickListener = v -> {
            String reactionType = (String) v.getTag(); // lấy type từ tag
            sendReaction(photoId, reactionType);
        };

        btnLove.setOnClickListener(reactionClickListener);
        btnWow.setOnClickListener(reactionClickListener);
        btnSad.setOnClickListener(reactionClickListener);
        btnHaha.setOnClickListener(reactionClickListener);

        final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY < 0) {
                        if (currentPhotoIndex < currentPhotoList.size() - 1) {
                            currentPhotoIndex++;
                            showPhotoFullView(currentPhotoIndex);
                        }
                    } else {
                        if (currentPhotoIndex == 0) {
                            hidePhotoFullView();
                        } else {
                            currentPhotoIndex--;
                            showPhotoFullView(currentPhotoIndex);
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        photoFullView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

    }

    private void saveImageToGallery(Bitmap bitmap) {
        String fileName = "photo_" + System.currentTimeMillis() + ".jpg";
        OutputStream fos;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LocketApp");

                ContentResolver resolver = requireContext().getContentResolver();
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                fos = resolver.openOutputStream(imageUri);
            } else {
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "LocketApp");
                if (!dir.exists()) dir.mkdirs();
                File imageFile = new File(dir, fileName);
                fos = new FileOutputStream(imageFile);
            }

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            if (fos != null) fos.close();

            Toast.makeText(getContext(), "Đã lưu ảnh vào thư viện", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lưu ảnh thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void hidePhotoFullView() {
        if (photoFullView != null) {
            photoFullView.setVisibility(View.GONE);
        }
        binding.cardImageContainer.setVisibility(View.VISIBLE);
        binding.captionLayout.setVisibility(View.VISIBLE);
        binding.btnScrollDown.setVisibility(View.VISIBLE);
    }

    private void fetchPhotos() {
        ApiService apiService = ApiClient.getApiService();
        String accessToken = requireContext().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE).getString("accessToken", null);

        apiService.getPhotos(accessToken).enqueue(new Callback<List<Photos.Photo>>() {
            @Override
            public void onResponse(Call<List<Photos.Photo>> call, Response<List<Photos.Photo>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    currentPhotoList = response.body();
                    currentPhotoIndex = 0;
                    showPhotoFullView(currentPhotoIndex);
                } else {
                    showNoPhotoView();
                }
            }

            @Override
            public void onFailure(Call<List<Photos.Photo>> call, Throwable t) {
                showNoPhotoView();
            }
        });
    }

    private void sendReaction(int photoId, String reactionType) {
        String accessToken = requireContext().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
                .getString("accessToken", null);
        Photo_Reaction.ReactionRequest request = new Photo_Reaction.ReactionRequest(photoId, reactionType);
        ApiService apiService = ApiClient.getApiService();
        apiService.addReaction(accessToken, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    getReactionsForPhoto(photoId);
                } else {
                    deleteReaction(photoId,reactionType);
                    getReactionsForPhoto(photoId);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteReaction(int photoId, String reactionType) {
        String accessToken = requireContext().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
                .getString("accessToken", null);
        Photo_Reaction.DeleteReactionRequest request = new Photo_Reaction.DeleteReactionRequest(photoId, reactionType);
        ApiService apiService = ApiClient.getApiService();

        apiService.deleteReaction(accessToken, request)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            getReactionsForPhoto(photoId);
                        } else {
                            Toast.makeText(getContext(), "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi xoá: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getReactionsForPhoto(int photoId) {
        String accessToken = requireContext().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
                .getString("accessToken", null);
        ApiService apiService = ApiClient.getApiService();
        apiService.getReactions(accessToken, String.valueOf(photoId))
                .enqueue(new Callback<Photo_Reaction.ReactionSummary>() {
                    @Override
                    public void onResponse(Call<Photo_Reaction.ReactionSummary> call, Response<Photo_Reaction.ReactionSummary> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Photo_Reaction.ReactionSummary summary = response.body();

                            TextView textLoveCount = photoFullView.findViewById(R.id.textLoveCount);
                            TextView textHahaCount = photoFullView.findViewById(R.id.textHahaCount);
                            TextView textSadCount = photoFullView.findViewById(R.id.textSadCount);
                            TextView textWowCount = photoFullView.findViewById(R.id.textWowCount);

                            textLoveCount.setText(String.valueOf(summary.getLove()));
                            textHahaCount.setText(String.valueOf(summary.getHaha()));
                            textSadCount.setText(String.valueOf(summary.getSad()));
                            textWowCount.setText(String.valueOf(summary.getWow()));
                        } else {
                            Log.e("Reactions", "Phản hồi không hợp lệ: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Photo_Reaction.ReactionSummary> call, Throwable t) {
                        Log.e("Reactions", "Không thể tải reaction: " + t.getMessage());
                    }
                });
    }



    private boolean hasImagePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestImagePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1001);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
