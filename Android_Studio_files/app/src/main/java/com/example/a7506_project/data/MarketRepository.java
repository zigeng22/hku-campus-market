package com.example.a7506_project.data;

import com.example.a7506_project.model.Item;
import com.example.a7506_project.model.ItemCard;
import com.example.a7506_project.model.ItemDraft;
import com.example.a7506_project.model.OfferSummary;
import com.example.a7506_project.model.ParticipationSummary;
import com.example.a7506_project.model.SortOrder;
import com.example.a7506_project.model.TradeTransaction;
import com.example.a7506_project.model.User;
import com.example.a7506_project.model.result.AcceptOfferResult;
import com.example.a7506_project.model.result.PlaceOfferResult;
import com.example.a7506_project.model.result.RegistrationResult;
import com.example.a7506_project.model.result.RepositoryResultCode;

import java.util.List;

public interface MarketRepository {
    RegistrationResult registerUser(String nickname, String password, String whatsapp);
    User authenticate(String nickname, String password);
    User getUserById(long userId);
    RepositoryResultCode updateUserProfile(long userId, String nickname, String whatsapp);
    RepositoryResultCode changePassword(long userId, String currentPassword, String newPassword);

    long createItem(long sellerId, ItemDraft draft);
    boolean updateItem(long itemId, long sellerId, ItemDraft draft);
    boolean updateItemImage(long itemId, long sellerId, String imageUri);
    boolean softDeleteItem(long itemId, long sellerId);
    Item getItemById(long itemId);
    List<ItemCard> searchActiveItems(String keyword, String category, SortOrder sortOrder);
    List<ItemCard> getListingsBySeller(long sellerId);

    PlaceOfferResult placeOffer(long itemId, long buyerId, long amountCents, String offerType);
    List<OfferSummary> getOffersForSellerItem(long itemId, long sellerId);
    RepositoryResultCode rejectOffer(long offerId, long sellerId);
    AcceptOfferResult acceptOffer(long offerId, long sellerId);
    List<ParticipationSummary> getBuyerActivity(long buyerId);
    TradeTransaction getTransactionForItem(long itemId);
}
