package com.example.a7506_project.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a7506_project.R;
import com.example.a7506_project.model.ItemCard;
import com.example.a7506_project.util.ImageUriLoader;
import com.example.a7506_project.util.MoneyFormatter;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private final List<ItemCard> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ItemCard item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
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
                .inflate(R.layout.row_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemCard item = items.get(position);
        holder.name.setText(item.getName());
        holder.price.setText(MoneyFormatter.centsToHkd(item.getPriceCents()));
        holder.category.setText(item.getCategory());
        holder.seller.setText(holder.itemView.getContext().getString(R.string.seller_prefix, item.getSellerNickname()));

        ImageUriLoader.loadOrShowPlaceholder(
                holder.itemView.getContext(),
                holder.image,
                item.getImageUri(),
                R.drawable.ic_item_placeholder);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, price, category, seller;

        ViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.imageItemThumbnail);
            name = view.findViewById(R.id.textRowItemName);
            price = view.findViewById(R.id.textRowItemPrice);
            category = view.findViewById(R.id.textRowItemCategory);
            seller = view.findViewById(R.id.textRowSeller);
        }
    }
}
