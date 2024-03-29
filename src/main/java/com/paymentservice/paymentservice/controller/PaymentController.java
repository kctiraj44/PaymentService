package com.paymentservice.paymentservice.controller;


import com.paymentservice.paymentservice.dto.PaymentDetailDTO;
import com.paymentservice.paymentservice.dto.PaymentResponse;
import com.paymentservice.paymentservice.exception.ResourceNotFoundException;
import com.paymentservice.paymentservice.model.Payment;
import com.paymentservice.paymentservice.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);


    @Autowired
    private PaymentService paymentService;

    @PostMapping("/createPayemnt")
    public ResponseEntity<PaymentResponse<Payment>> acceptPayment(@RequestBody Payment payment) {
        log.info("Received request to accept payment for card number: {}", payment.getCardNumber());
        Payment processedPayment = paymentService.acceptPayment(payment);
        PaymentResponse<Payment> response = new PaymentResponse<>(processedPayment, "Payment processed successfully! Your transaction is now complete.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<PaymentResponse<Object>> stopPayment(@PathVariable Long id) {
        log.info("Received request to stop payment with ID: {}", id);
        boolean result = paymentService.stopPayment(id);
        if (result) {
            return ResponseEntity.ok(new PaymentResponse<>(null, "Payment has been successfully stopped."));
        } else {
            return ResponseEntity.badRequest().body(new PaymentResponse<>(null, "Unable to stop payment. Payment cannot be stopped after 15 minutes."));
        }
    }

    @GetMapping("/card/{cardNumber}")
    public ResponseEntity<PaymentResponse<List<Payment>>> getPaymentsByCardNumber(@PathVariable String cardNumber) {
        log.info("Received request to retrieve payments for card number: {}", cardNumber);
        List<Payment> payments = paymentService.getPaymentsByCardNumber(cardNumber);
        if (!payments.isEmpty()) {
            return ResponseEntity.ok(new PaymentResponse<>(payments, "Payments retrieved successfully."));
        } else {
            return ResponseEntity.ok(new PaymentResponse<>(payments, "No payments found for the provided card number."));
        }
    }

    @GetMapping("/active/{cardNumber}")
    public ResponseEntity<PaymentResponse<List<PaymentDetailDTO>>> getActivePaymentsByCardNumber(@PathVariable String cardNumber) {
        try {
            List<Payment> payments = paymentService.getActivePaymentsByCardNumber(cardNumber);
            List<PaymentDetailDTO> paymentDetailDTOs = payments.stream()
                    .map(payment -> new PaymentDetailDTO(payment.getId(), payment.getCardNumber(), payment.getAmount(), payment.getTimestamp()))
                    .collect(Collectors.toList());

            PaymentResponse<List<PaymentDetailDTO>> response = new PaymentResponse<>(paymentDetailDTOs, "Active payments retrieved successfully");

            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Error fetching active payments for card number {}: {}", cardNumber, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
