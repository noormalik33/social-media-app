package blogtalk.compackage blogtalk.com.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import blogtalk.com.interfaces.DoubleClickListener;
import blogtalk.com.items.ItemImageGallery;
import blogtalk.com.items.ItemPost;
import blogtalk.com.items.ItemStories;
import blogtalk.com.socialmedia.R;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.DoubleClick;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterImagePager extends RecyclerView.Adapter<AdapterImagePager.ViewPagerViewHolder> {

    private final ArrayList<ItemImageGallery> arrayList;
    DoubleClickListener doubleClickListener;

    public AdapterImagePager(ArrayList<ItemImageGallery> arrayList, DoubleClickListener doubleClickListener) {
        this.arrayList = arrayList;
        this.doubleClickListener = doubleClickListener;
    }

    public static class ViewPagerViewHolder extends RecyclerView.ViewHolder {
        private final RoundedImageView iv_posts;

        ViewPagerViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_posts = itemView.findViewById(R.id.iv_posts);
        }
    }

    @NonNull
    @Override
    public ViewPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_image_pager, parent, false);
        return new ViewPagerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewPagerViewHolder holder, int position) {
        ItemImageGallery itemImageGallery = arrayList.get(holder.getAbsoluteAdapterPosition());

        holder.iv_posts.setMaxHeight(Constants.photoHeight);
        Picasso.get().load(!itemImageGallery.getImage().isEmpty() ? itemImageGallery.getImage() : "null")
                .placeholder(R.drawable.placeholder)
                .into(holder.iv_posts);

        holder.iv_posts.setOnClickListener(new DoubleClick(new DoubleClickListener() {
            @Override
            public void onSingleClick(View view) {
                doubleClickListener.onSingleClick(view);
            }

            @Override
            public void onDoubleClick(View view) {
                doubleClickListener.onDoubleClick(view);
            }
        }));
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}