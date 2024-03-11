package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentservice.paymentservice.controller.PaymentController;
import com.paymentservice.paymentservice.dto.PaymentDetailDTO;
import com.paymentservice.paymentservice.dto.PaymentResponse;
import com.paymentservice.paymentservice.model.Payment;
import com.paymentservice.paymentservice.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private Payment payment;
    private List<Payment> paymentList;
    private PaymentDetailDTO paymentDetailDTO;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setId(1L);
        payment.setCardNumber("1234567890123456");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setTimestamp(LocalDateTime.now());

        paymentDetailDTO = new PaymentDetailDTO(payment.getId(), payment.getCardNumber(), payment.getAmount(), payment.getTimestamp());
        paymentList = Arrays.asList(payment);
    }

    @Test
    void acceptPayment_ShouldReturnPaymentResponse() throws Exception {
        given(paymentService.acceptPayment(any(Payment.class))).willReturn(payment);

        mockMvc.perform(post("/payments/createPayemnt")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(payment.getId().intValue())))
                .andExpect(jsonPath("$.data.cardNumber", is(payment.getCardNumber())))
                .andExpect(jsonPath("$.message", is("Payment processed successfully! Your transaction is now complete.")));

        verify(paymentService, times(1)).acceptPayment(any(Payment.class));
    }

    @Test
    void stopPayment_ShouldReturnSuccessOrBadRequest() throws Exception {
        given(paymentService.stopPayment(payment.getId())).willReturn(true);

        mockMvc.perform(delete("/payments/delete/{id}", payment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Payment has been successfully stopped.")));

        given(paymentService.stopPayment(payment.getId())).willReturn(false);

        mockMvc.perform(delete("/payments/delete/{id}", payment.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Unable to stop payment. Payment cannot be stopped after 15 minutes.")));

        verify(paymentService, times(2)).stopPayment(payment.getId());
    }

    @Test
    void getPaymentsByCardNumber_ShouldReturnPaymentsList() throws Exception {
        given(paymentService.getPaymentsByCardNumber(payment.getCardNumber())).willReturn(paymentList);

        mockMvc.perform(get("/payments/card/{cardNumber}", payment.getCardNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(payment.getId().intValue())))
                .andExpect(jsonPath("$.message", is("Payments retrieved successfully.")));

        verify(paymentService, times(1)).getPaymentsByCardNumber(payment.getCardNumber());
    }

    @Test
    void getActivePaymentsByCardNumber_ShouldReturnActivePaymentsList() throws Exception {
        given(paymentService.getActivePaymentsByCardNumber(payment.getCardNumber())).willReturn(paymentList);

        mockMvc.perform(get("/payments/active/{cardNumber}", payment.getCardNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(payment.getId().intValue())))
                .andExpect(jsonPath("$.message", is("Active payments retrieved successfully")));

        verify(paymentService, times(1)).getActivePaymentsByCardNumber(payment.getCardNumber());
    }
}
