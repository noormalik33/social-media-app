package blogtalk.compackage blogtalk.com.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import blogtalk.com.items.ItemWithdraw;
import blogtalk.com.socialmedia.R;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;


public class AdapterWithdraw extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    Methods methods;
    ArrayList<ItemWithdraw> arrayList;
    final int VIEW_PROGRESS = -1;

    public AdapterWithdraw(Context context, ArrayList<ItemWithdraw> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        methods = new Methods(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title, tv_amount, tv_points, tv_date,tv_status;

        public ViewHolder(View view) {
            super(view);
            tv_title = view.findViewById(R.id.tv_title);
            tv_amount = view.findViewById(R.id.tvAmount);
            tv_points = view.findViewById(R.id.tvPoints);
            tv_date = view.findViewById(R.id.tv_date);
            tv_status = view.findViewById(R.id.tv_withdraw_status);
        }
    }

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {
        private static ProgressBar progressBar;

        private ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_PROGRESS) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_progressbar, parent, false);
            return new ProgressViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_withdraw_history, parent, false);
            return new ViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolder) {
            ItemWithdraw itemWithdraw = arrayList.get(holder.getAbsoluteAdapterPosition());

            ((ViewHolder) holder).tv_title.setText(context.getString(R.string.withdraw));
            ((ViewHolder) holder).tv_date.setText(itemWithdraw.getRequestDate());
            ((ViewHolder) holder).tv_status.setText(itemWithdraw.isStatusApproved() ? context.getString(R.string.approved) : context.getString(R.string.pending));
            ((ViewHolder) holder).tv_points.setText("-".concat(String.valueOf(itemWithdraw.getPoints())));
            ((ViewHolder) holder).tv_amount.setText("(".concat(methods.getCurrencySymbol(new SharedPref(context).getCurrencyCode())).concat(itemWithdraw.getAmount()).concat(")"));
            ((ViewHolder) holder).tv_status.setTextColor(ContextCompat.getColor(context, arrayList.get(holder.getAbsoluteAdapterPosition()).isStatusApproved() ? R.color.points : R.color.pending));
        } else {
            if (getItemCount() < 9) {
                ProgressViewHolder.progressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == arrayList.size()) {
            return VIEW_PROGRESS;
        } else {
            return position;
        }
    }

    public void hideProgressBar() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }

    public boolean isHeader(int pos) {
        return pos == arrayList.size();
    }
}