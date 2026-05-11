package com.example.demo.service;

import com.example.demo.model.Payment;
import com.example.demo.model.Tenant;
import com.example.demo.model.Notification;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public Payment generateMonthlyRent(Tenant tenant, double amount, String month) {
        Payment payment = new Payment();
        payment.setTenant(tenant);
        payment.setAmount(amount);
        payment.setMonth(month);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setPaymentDate(null);
        
        Payment saved = paymentRepository.save(payment);
        
        // Notify Tenant
        Notification notification = new Notification();
        notification.setRecipient(tenant.getUser());
        notification.setTitle("Rent Generated");
        notification.setMessage("Rent for " + month + " has been generated. Please pay ₹" + amount);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notificationRepository.save(notification);
        
        return saved;
    }

    public Payment verifyPayment(Long paymentId, String transactionId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setTransactionId(transactionId);
        
        return paymentRepository.save(payment);
    }
}
