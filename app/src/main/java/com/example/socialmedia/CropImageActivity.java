package blogtalk.com.socialmedia;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import blogtalk.com.utils.Methods;
import com.google.android.material.button.MaterialButton;
import com.naver.android.helloyako.imagecrop.view.ImageCropView;

import java.io.File;

public class CropImageActivity extends AppCompatActivity {

    Methods methods;
    ImageCropView iv_crop_image;
    MaterialButton btn_done;
    Uri imageUri;
    int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        imageUri = Uri.parse(getIntent().getStringExtra("uri"));
        pos = getIntent().getIntExtra("pos",0);

        methods = new Methods(this);

        iv_crop_image = findViewById(R.id.iv_crop_image);
        btn_done = findViewById(R.id.btn_crop_done);

        iv_crop_image.setImageFilePath(methods.getPathImage(imageUri));

        findViewById(R.id.iv_back).setOnClickListener(view -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        btn_done.setOnClickListener(view -> {

            File root = getExternalCacheDir().getAbsoluteFile();
            String filePath = root.getPath() + File.separator + System.currentTimeMillis() + ".jpg";
            methods.saveBitMap(root, iv_crop_image.getCroppedImage(), filePath);

            Intent intent = new Intent();
            intent.putExtra("crop_image_path", filePath);
            intent.putExtra("isCrop", true);
            intent.putExtra("pos", pos);
            setResult(RESULT_OK, intent);
            finish();
        });
    }
}