package com.example.a7506_project.ui.management;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a7506_project.R;
import com.example.a7506_project.model.ParticipationSummary;
import com.example.a7506_project.util.MoneyFormatter;

import java.util.ArrayList;
import java.util.List;

public class ParticipationAdapter extends RecyclerView.Adapter<ParticipationAdapter.ViewHolder> {

    private final List<ParticipationSummary> items = new ArrayList<>();

    public void setItems(List<ParticipationSummary> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_participation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParticipationSummary item = items.get(position);
        holder.itemName.setText(item.getItemName());
        holder.amount.setText(MoneyFormatter.centsToHkd(item.getOfferAmountCents()));
        holder.status.setText(item.getOfferStatus());

        // Only reveal WhatsApp for CONFIRMED deals
        if (item.getCounterpartyWhatsapp() != null && !item.getCounterpartyWhatsapp().isEmpty()) {
            holder.whatsapp.setText("WhatsApp: " + item.getCounterpartyWhatsapp());
            holder.whatsapp.setVisibility(View.VISIBLE);
        } else {
            holder.whatsapp.setText(R.string.sample_whatsapp);
            holder.whatsapp.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, amount, status, whatsapp;

        ViewHolder(View view) {
            super(view);
            itemName = view.findViewById(R.id.textParticipationItem);
            amount = view.findViewById(R.id.textParticipationAmount);
            status = view.findViewById(R.id.textParticipationStatus);
            whatsapp = view.findViewById(R.id.textCounterpartyWhatsapp);
        }
    }
}
