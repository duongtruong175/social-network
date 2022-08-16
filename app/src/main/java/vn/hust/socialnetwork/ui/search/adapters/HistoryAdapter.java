package vn.hust.socialnetwork.ui.search.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.search.History;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private Context context;
    private List<History> histories;
    private OnHistoryListener onHistoryListener;

    public HistoryAdapter(Context context, List<History> histories, OnHistoryListener onHistoryListener) {
        this.context = context;
        this.histories = histories;
        this.onHistoryListener = onHistoryListener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        History history = histories.get(position);
        holder.tvSearchHistory.setText(history.getQuery());
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {

        AppCompatTextView tvSearchHistory;
        AppCompatImageView ivDelete;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // binding
            tvSearchHistory = itemView.findViewById(R.id.tv_search_history);
            ivDelete = itemView.findViewById(R.id.iv_delete);

            if (onHistoryListener != null) {
                ivDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onHistoryListener.onDeleteItemClick(getBindingAdapterPosition());
                    }
                });

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onHistoryListener.onItemClick(getBindingAdapterPosition());
                    }
                });
            }
        }
    }
}
