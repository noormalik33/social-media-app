package blogtalk.com.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import blogtalk.com.socialmedia.R;
import blogtalk.com.socialmedia.UploadActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.Objects;

public class FragmentUploadText extends Fragment {

    TextInputEditText et_text;
    RoundedImageView iv_close;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_upload_text, container, false);

        et_text = rootView.findViewById(R.id.et_upload_text);
        iv_close = rootView.findViewById(R.id.iv_upload_text_close);

        iv_close.setOnClickListener(view -> {
            requireActivity().finish();
        });

        et_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ((UploadActivity)requireActivity()).fab_upload_text.setVisibility(charSequence.length()>0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return rootView;
    }

    public String getText() {
        return et_text.getText().toString();
    }

    @Override
    public void onResume() {
        if(!Objects.requireNonNull(et_text.getText()).toString().trim().isEmpty()) {
            ((UploadActivity)requireActivity()).fab_upload_text.setVisibility(View.VISIBLE);
        } else {
            ((UploadActivity)requireActivity()).fab_upload_text.setVisibility(View.GONE);
        }
        super.onResume();
    }
}