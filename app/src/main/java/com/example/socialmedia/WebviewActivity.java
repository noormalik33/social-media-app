package blogtalk.com.socialmedia;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import blogtalk.com.items.ItemPage;
import blogtalk.com.utils.Methods;

public class WebviewActivity extends AppCompatActivity {

    Methods methods;
    WebView webView;
    ImageView iv_back;
    TextView tv_toolbar;
    ItemPage itemPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        methods = new Methods(this);
        methods.forceRTLIfSupported();

        itemPage = (ItemPage) getIntent().getSerializableExtra("item");

        webView = findViewById(R.id.webView_pages);
        tv_toolbar = findViewById(R.id.tv_wb_toolbar);
        iv_back = findViewById(R.id.iv_wb_back);
        webView.getSettings().setJavaScriptEnabled(true);

        tv_toolbar.setText(itemPage.getTitle());

        iv_back.setOnClickListener(view -> {
            onBackPressed();
        });

        if (!itemPage.getId().equals("0")) {
            String mimeType = "text/html;charset=UTF-8";
            String encoding = "utf-8";

            String text;
            if (methods.isDarkMode()) {
                text = "<html><head>"
                        + "<style> @font-face { font-family: 'custom'; src: url(\"file:///android_res/font/outfit_medium.ttf\")"
                        + "} body {color:#fff !important;text-align:left; font-family: 'custom'; font-size:15px;}"
                        + "</style></head>"
                        + "<body>"
                        + itemPage.getContent()
                        + "</body></html>";
            } else {
                text = "<html><head>"
                        + "<style> @font-face { font-family: 'custom'; src: url(\"file:///android_res/font/outfit_medium.ttf\")"
                        + "} body {color:#65637B !important;text-align:left; font-family: 'custom'; font-size:15px;}"
                        + "</style></head>"
                        + "<body>"
                        + itemPage.getContent()
                        + "</body></html>";
            }

            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.loadDataWithBaseURL("blarg://ignored", text, mimeType, encoding, "");
        } else {
            webView.loadUrl(itemPage.getContent());
            webView.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView viewx, String urlx) {
                    viewx.loadUrl(urlx);
                    return false;
                }
            });
        }

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);
    }
}