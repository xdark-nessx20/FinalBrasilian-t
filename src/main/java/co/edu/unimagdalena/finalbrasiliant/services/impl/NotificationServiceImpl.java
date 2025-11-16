package co.edu.unimagdalena.finalbrasiliant.services.impl;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.NotificationType;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.PaymentMethod;
import co.edu.unimagdalena.finalbrasiliant.services.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@Profile({"dev", "test"})
public class NotificationServiceImpl implements NotificationService {
    @Override
    public void sendTicketConfirmation(String phoneNumber, String passengerName, Long ticketId, String seatNumber, String qrCode) {
        var message = """
            TICKET BUYING CONFIRMATION
            Hello %s, your ticket #%d has been confirmed!
            Seat: %s
            QR Code: %s
            Show this QR code on boarding.
        """.formatted(passengerName, ticketId, seatNumber, qrCode);

        sendNotif(phoneNumber, NotificationType.TICKET_CONFIRMATION, message);
    }

    @Override
    public void sendTicketCancellation(String phoneNumber, String passengerName, Long ticketId, BigDecimal refundAmount, PaymentMethod paymentMethod) {
        var message = """
            TICKET CANCELLATION
            Hello %s, your ticket #%d has been cancelled!
            Refund amount: %f
            Original payment method: %s
        """.formatted(passengerName, ticketId, refundAmount, paymentMethod);
        sendNotif(phoneNumber, NotificationType.TICKET_CANCELLED, message);
    }

    @Override
    public void sendParcelCreated(String phoneNumber, String senderName, String parcelCode, String receiverName) {
        var message = """
            PARCEL CONFIRMED
            Hello %s, your parcel with code '%s' has been confirmed!
            Sent by %s
        """.formatted(receiverName, parcelCode, senderName);
        sendNotif(phoneNumber, NotificationType.PARCEL_CONFIRMATION, message);
    }

    @Override
    public void sendParcelInTransit(String phoneNumber, String receiverName, String parcelCode) {
        var message = """
            PARCEL STATUS UPDATED
            Hello %s, your parcel with code '%s' is now in transit!
        """.formatted(receiverName, parcelCode);
        sendNotif(phoneNumber, NotificationType.PARCEL_IN_TRANSIT, message);
    }

    @Override
    public void sendParcelReadyForPickup(String phoneNumber, String receiverName, String parcelCode, String deliveryOTP, String pickupLocation) {
        var message = """
            PARCEL READY FOR PICKUP
            Hello %s, your parcel with code '%s' is ready to pickup!
            You can pick it up in %s with the OTP: %s.
        """.formatted(receiverName, parcelCode, pickupLocation, deliveryOTP);
        sendNotif(phoneNumber, NotificationType.PARCEL_READY_FOR_PICKUP, message);
    }

    @Override
    public void sendParcelDelivered(String phoneNumber, String receiverName, String parcelCode) {
        var message = """
            PARCEL DELIVERED
            Hello %s, your parcel with code '%s' has been delivered!
        """.formatted(receiverName, parcelCode);
        sendNotif(phoneNumber, NotificationType.PARCEL_DELIVERED, message);
    }

    @Override
    public void sendParcelDeliveryFailed(String phoneNumber, String receiverName, String parcelCode, String reason) {
        var message = """
            PARCEL DELIVERY FAILED
            Hello %s, your parcel with code '%s' couldn't be delivered
            because %s
        """.formatted(receiverName, parcelCode, reason);
    }

    private void sendNotif(String phone, NotificationType type, String message){
        log.info("Sending Whatsapp Notification:");
        log.info("""
        ─────────────────────────────────────────────────
        To: {}
        Type: {}
        Message:
        {}
        ─────────────────────────────────────────────────
        """, phone, type, message);
    }
}
