package blogtalk.compackage blogtalk.com.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import blogtalk.com.items.ItemPage;
import blogtalk.com.socialmedia.AboutActivity;
import blogtalk.com.socialmedia.R;
import blogtalk.com.socialmedia.WebviewActivity;

import java.util.ArrayList;

public class AdapterPages extends RecyclerView.Adapter<AdapterPages.MyViewHolder> {

    Context context;
    ArrayList<ItemPage> arrayList;

    public AdapterPages(Context context, ArrayList<ItemPage> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;
        View view_pages;

        MyViewHolder(View view) {
            super(view);
            tv_title = view.findViewById(R.id.tv_pages);
            view_pages = view.findViewById(R.id.view_pages);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_pages, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.tv_title.setText(arrayList.get(position).getTitle());
        holder.itemView.setOnClickListener(v -> {
            if(arrayList.get(holder.getAbsoluteAdapterPosition()).getId().equals("1")) {
                Intent intent = new Intent(context, AboutActivity.class);
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, WebviewActivity.class);
                intent.putExtra("item", arrayList.get(holder.getAbsoluteAdapterPosition()));
                context.startActivity(intent);
            }
        });
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