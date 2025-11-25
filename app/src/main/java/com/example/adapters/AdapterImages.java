package blogtalk.compackage blogtalk.com.adapters;

import android.content.Context;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import blogtalk.com.interfaces.ClickListener;
import blogtalk.com.items.ItemMedia;
import blogtalk.com.socialmedia.R;
import blogtalk.com.utils.Methods;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterImages extends RecyclerView.Adapter<AdapterImages.MyViewHolder> {

    Context context;
    Methods methods;
    ArrayList<ItemMedia> arrayList;
    ArrayList<ItemMedia> selectedItems = new ArrayList<>();
    private int columnWidth, columnHeight;
    boolean isStories;
    private boolean isMultiSelectMode = false;
    ClickListener clickListener;

    public AdapterImages(Context context, ArrayList<ItemMedia> arrayList, boolean isStories, ClickListener clickListener) {
        this.context = context;
        this.arrayList = arrayList;
        this.isStories = isStories;
        this.clickListener = clickListener;

        methods = new Methods(context);
        columnWidth = methods.getColumnWidth(3, 2);
        if (!isStories) {
            columnHeight = columnWidth;
        } else {
            columnHeight = (int) (columnWidth / 0.60);
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, iv_type;
        TextView tv_number;

        MyViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.iv_images);
            iv_type = view.findViewById(R.id.iv_post_type);
            tv_number = view.findViewById(R.id.tv_number);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_images, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.imageView.setLayoutParams(new ConstraintLayout.LayoutParams(columnWidth, columnHeight));

        if (!isStories) {
            Picasso.get()
                    .load(arrayList.get(position).getMediaUrl())
                    .resize(150, 100)
                    .centerCrop()
                    .into(holder.imageView);
        } else {
            Picasso.get()
                    .load(arrayList.get(position).getMediaUrl())
                    .resize(150, 250)
                    .centerCrop()
                    .into(holder.imageView);
        }

        if (selectedItems.contains(arrayList.get(holder.getAbsoluteAdapterPosition()))) {
            holder.tv_number.setText(String.valueOf(selectedItems.indexOf(arrayList.get(holder.getAbsoluteAdapterPosition()))+1));
        } else {
            holder.tv_number.setText("");
        }

        if (arrayList.get(holder.getAbsoluteAdapterPosition()).getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
            holder.iv_type.setImageResource(R.drawable.ic_image);
        } else {
            holder.iv_type.setImageResource(R.drawable.ic_video);
        }

        if (selectedItems.contains(arrayList.get(holder.getAbsoluteAdapterPosition()))) {
            holder.imageView.setAlpha(0.5f); // Example of highlighting (dim the image)
        } else {
            holder.imageView.setAlpha(1.0f); // Reset highlight
        }

        if(!isStories) {
            holder.imageView.setOnLongClickListener(view -> {
                if (arrayList.get(holder.getAbsoluteAdapterPosition()).getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                    if (!isMultiSelectMode) {
                        isMultiSelectMode = true;
                    }
                    toggleSelection(position);
                    return true;
                } else {
                    methods.showToast("Cannot select multiple videos");
                    return false;
                }
            });
        }

        holder.imageView.setOnClickListener(view -> {
            if (isMultiSelectMode) {
                if (arrayList.get(holder.getAbsoluteAdapterPosition()).getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                    toggleSelection(position);
                } else {
                    methods.showToast("Cannot select video");
                }
            }
        });
    }

    private void toggleSelection(int position) {
        if (selectedItems.contains(arrayList.get(position))) {
            if(arrayList.get(position) == selectedItems.get(selectedItems.size()-1)) {
                selectedItems.remove(arrayList.get(position));
                if(!selectedItems.isEmpty()) {
                    clickListener.onClick(arrayList.indexOf(selectedItems.get(selectedItems.size() - 1)));
                }
            } else {
                selectedItems.remove(arrayList.get(position));
            }

            for (int i = 0; i < selectedItems.size(); i++) {
                notifyItemChanged(arrayList.indexOf(selectedItems.get(i)));
            }

        } else {
            selectedItems.add(arrayList.get(position));
            clickListener.onClick(position);
        }
        notifyItemChanged(position);
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public ArrayList<ItemMedia> getSelectedItems() {
        return selectedItems;
    }

    public int getSelectedItemsCount() {
        return selectedItems.size();
    }

    public boolean isMultiSelectMode() {
        return isMultiSelectMode;
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}