package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.PaymentDTOs.*;

public interface PaymentService {
    PaymentResponse confirmPayment(PaymentRequest paymentRequest);
    //No more methods needed, I think...
}
