package blogtalk.compackage blogtalk.com.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import blogtalk.com.socialmedia.PostByTagActivity;
import blogtalk.com.socialmedia.R;

import java.util.ArrayList;

public class AdapterTags extends RecyclerView.Adapter<AdapterTags.MyViewHolder> {

    Context context;
    ArrayList<String> arrayList;

    public AdapterTags(Context context, ArrayList<String> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_tags;

        MyViewHolder(View view) {
            super(view);
            tv_tags = view.findViewById(R.id.tv_tags);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_tags, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.tv_tags.setText("#".concat(arrayList.get(position)));
        holder.tv_tags.setOnClickListener(view -> {
            Intent intent = new Intent(context, PostByTagActivity.class);
            intent.putExtra("tag", arrayList.get(holder.getAbsoluteAdapterPosition()));
            context.startActivity(intent);
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