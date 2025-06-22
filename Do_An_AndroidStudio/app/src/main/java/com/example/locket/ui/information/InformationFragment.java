package com.example.locket.ui.information;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.locket.databinding.FragmentInformationBinding;

public class InformationFragment extends Fragment {

    private FragmentInformationBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentInformationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textInfo;
        textView.setText("Đây là ứng dụng clone Locket được dev bởi KaiPow mọi thứ trên app đều an toàn để sử dụng, " +
                "cam kết là những thông tin sẽ không được công khai cho ai, và đây là ứng dụng mã nguồn mở bạn có thể check github của KaiPow." +
                "Ứng dụng được build Back-end là NodeJS ExpressJS kết hợp với Java trên Android Studio. Khuyến khích vì đây là một social media nên" +
                "hãy sử dụng tên giả hoặc một tên dth. Cảm ơn các bạn !!.\n KaiPow");
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}