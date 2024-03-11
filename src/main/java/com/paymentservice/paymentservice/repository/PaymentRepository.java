package com.paymentservice.paymentservice.repository;

import com.paymentservice.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByCardNumber(String cardNumber);
}
