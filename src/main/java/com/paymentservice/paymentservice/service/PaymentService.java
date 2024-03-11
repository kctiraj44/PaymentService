package com.paymentservice.paymentservice.service;


import com.paymentservice.paymentservice.exception.PaymentValidationException;
import com.paymentservice.paymentservice.exception.ResourceNotFoundException;
import com.paymentservice.paymentservice.model.Payment;
import com.paymentservice.paymentservice.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);


    @Autowired
    private PaymentRepository paymentRepository;

    public Payment acceptPayment(Payment payment) {
        log.debug("Accepting payment: {}", payment.getCardNumber());
        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentValidationException("Payment amount must be positive.");
        }

        payment.setTimestamp(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public boolean stopPayment(Long paymentId) {
        log.debug("Stopping payment with ID: {}", paymentId);
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (!paymentOpt.isPresent()) {
            throw new ResourceNotFoundException("Payment with ID " + paymentId + " not found.");
        }

        Payment payment = paymentOpt.get();
        if (payment.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            throw new PaymentValidationException("Payments over $10,000 cannot be stopped automatically. Please contact customer service.");
        }

        if (LocalDateTime.now().minusMinutes(15).isBefore(payment.getTimestamp())) {
            paymentRepository.deleteById(paymentId);
            return true;
        } else {
            throw new PaymentValidationException("Payment cannot be stopped after 15 minutes.");
        }
    }

    public List<Payment> getPaymentsByCardNumber(String cardNumber) {
        log.debug("Retrieving payments for card number: {}", cardNumber);
        List<Payment> payments = paymentRepository.findByCardNumber(cardNumber);
        if (payments.isEmpty()) {
            throw new ResourceNotFoundException("No payments found for card number: " + cardNumber);
        }
        return payments;
    }
}
