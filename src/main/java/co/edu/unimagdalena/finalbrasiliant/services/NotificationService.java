package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.PaymentMethod;

import java.math.BigDecimal;

public interface NotificationService {
    void sendTicketConfirmation(String phoneNumber, String passengerName, Long ticketId, String seatNumber, String qrCode);

    void sendTicketUsed(String phoneNumber, String passengerName, Long ticketId, String route);

    void sendTicketCancellation(String phoneNumber, String passengerName, Long ticketId, BigDecimal refundAmount, PaymentMethod paymentMethod);

    void sendParcelCreated(String phoneNumber, String senderName, String parcelCode, String receiverName);

    void sendParcelInTransit(String phoneNumber, String receiverName, String parcelCode);

    void sendParcelReadyForPickup(String phoneNumber, String receiverName, String parcelCode, String deliveryOTP, String pickupLocation);

    void sendParcelDelivered(String phoneNumber, String receiverName, String parcelCode);

    void sendParcelDeliveryFailed(String phoneNumber, String receiverName, String parcelCode, Long parcelId);
}
