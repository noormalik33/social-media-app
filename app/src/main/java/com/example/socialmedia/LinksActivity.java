package blogtalk.com.socialmedia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import blogtalk.com.adapters.AdapterLinks;
import blogtalk.com.items.ItemLinks;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;

import java.util.ArrayList;

public class LinksActivity extends AppCompatActivity {

    Methods methods;
    SharedPref sharedPref;
    ImageView iv_back;
    RecyclerView rv_links;
    AdapterLinks adapterLinks;
    ArrayList<ItemLinks> arrayList = new ArrayList<>();
    ConstraintLayout cl_links;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_links);

        sharedPref = new SharedPref(this);

        methods = new Methods(this);
        methods.forceRTLIfSupported();

        rv_links = findViewById(R.id.rv_links);
        cl_links = findViewById(R.id.cl_add_links);
        iv_back = findViewById(R.id.iv_links_back);

        iv_back.setOnClickListener(view -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv_links.setLayoutManager(llm);

        if(!sharedPref.getLink1().isEmpty()) {
            arrayList.add(new ItemLinks(sharedPref.getLink1Title(), sharedPref.getLink1()));
        }
        if(!sharedPref.getLink2().isEmpty()) {
            arrayList.add(new ItemLinks(sharedPref.getLink2Title(), sharedPref.getLink2()));
        }
        if(!sharedPref.getLink3().isEmpty()) {
            arrayList.add(new ItemLinks(sharedPref.getLink3Title(), sharedPref.getLink3()));
        }
        if(!sharedPref.getLink4().isEmpty()) {
            arrayList.add(new ItemLinks(sharedPref.getLink4Title(), sharedPref.getLink4()));
        }
        if(!sharedPref.getLink5().isEmpty()) {
            arrayList.add(new ItemLinks(sharedPref.getLink5Title(), sharedPref.getLink5()));
        }

        Constants.arrayListLinks.clear();
        Constants.arrayListLinks.addAll(arrayList);

        setAdapter();

        cl_links.setOnClickListener(view -> {
            if(arrayList.size() < 5) {
                Intent intent = new Intent(LinksActivity.this, LinkAddActivity.class);
                intent.putExtra("isEdit", false);
                startActivity(intent);
            } else {
                methods.showToast(getString(R.string.err_max_5_links_added));
            }
        });

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);
    }

    private void setAdapter() {
        adapterLinks = new AdapterLinks(this, arrayList, true);
        rv_links.setAdapter(adapterLinks);
    }

    @Override
    protected void onResume() {
        if(Constants.isLinkChanged) {
            Constants.isLinkChanged = false;
            arrayList = Constants.arrayListLinks;
            setAdapter();
        }
        super.onResume();
    }
}