package com.example.a7506_project.ui.management;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a7506_project.R;
import com.example.a7506_project.model.ItemCard;
import com.example.a7506_project.util.ImageUriLoader;
import com.example.a7506_project.util.MoneyFormatter;
import com.example.a7506_project.util.TradeDisplayFormatter;

import java.util.ArrayList;
import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {

    private final List<ItemCard> items = new ArrayList<>();
    private OnListingClickListener listener;

    public interface OnListingClickListener {
        void onListingClick(ItemCard item);
    }

    public void setOnListingClickListener(OnListingClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ItemCard> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_listing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemCard item = items.get(position);
        holder.name.setText(item.getName());
        holder.price.setText(MoneyFormatter.centsToHkd(item.getPriceCents()));
        holder.status.setText(TradeDisplayFormatter.statusLabel(holder.itemView.getContext(), item.getStatus()));
        holder.status.setTextColor(ContextCompat.getColor(
                holder.itemView.getContext(), TradeDisplayFormatter.statusColor(item.getStatus())));
        int count = item.getOfferCount();
        holder.offerCount.setText(holder.itemView.getResources().getQuantityString(
                R.plurals.offer_count, count, count));
        ImageUriLoader.loadOrShowPlaceholder(
                holder.itemView.getContext(), holder.image, item.getImageUri(), R.drawable.ic_item_placeholder);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onListingClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, price, status, offerCount;

        ViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.imageListing);
            name = view.findViewById(R.id.textListingName);
            price = view.findViewById(R.id.textListingPrice);
            status = view.findViewById(R.id.textListingStatus);
            offerCount = view.findViewById(R.id.textOfferCount);
        }
    }
}
