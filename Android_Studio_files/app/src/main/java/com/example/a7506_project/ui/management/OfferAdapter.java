package com.example.a7506_project.ui.management;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a7506_project.R;
import com.example.a7506_project.contract.AppContract;
import com.example.a7506_project.model.OfferSummary;
import com.example.a7506_project.util.MoneyFormatter;

import java.util.ArrayList;
import java.util.List;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.ViewHolder> {

    private final List<OfferSummary> offers = new ArrayList<>();
    private OnAcceptListener listener;

    public interface OnAcceptListener {
        void onAccept(OfferSummary offer);
    }

    public void setOnAcceptListener(OnAcceptListener listener) {
        this.listener = listener;
    }

    public void setOffers(List<OfferSummary> newOffers) {
        offers.clear();
        offers.addAll(newOffers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_offer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OfferSummary offer = offers.get(position);
        holder.buyerName.setText(offer.getBuyerNickname());
        holder.amount.setText(MoneyFormatter.centsToHkd(offer.getAmountCents()));
        holder.type.setText(offer.getType());
        holder.status.setText(offer.getStatus());

        boolean isPending = AppContract.OFFER_PENDING.equals(offer.getStatus());
        holder.acceptButton.setVisibility(isPending ? View.VISIBLE : View.GONE);
        holder.acceptButton.setOnClickListener(v -> {
            if (listener != null) listener.onAccept(offer);
        });
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView buyerName, amount, type, status;
        View acceptButton;

        ViewHolder(View view) {
            super(view);
            buyerName = view.findViewById(R.id.textBuyerName);
            amount = view.findViewById(R.id.textOfferAmount);
            type = view.findViewById(R.id.textOfferType);
            status = view.findViewById(R.id.textOfferStatus);
            acceptButton = view.findViewById(R.id.buttonAcceptOffer);
        }
    }
}
