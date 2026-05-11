package com.example.demo.controller;

import com.example.demo.dto.response.ReceiptDto;
import com.example.demo.security.CurrentUser;
import com.example.demo.service.PaymentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/receipts")
@PreAuthorize("isAuthenticated()")
public class ReceiptController {

    private final PaymentService paymentService;
    private final CurrentUser currentUser;

    public ReceiptController(PaymentService paymentService, CurrentUser currentUser) {
        this.paymentService = paymentService;
        this.currentUser = currentUser;
    }

    @GetMapping("/{receiptNumber}")
    public ReceiptDto get(@PathVariable String receiptNumber) {
        return paymentService.getReceipt(receiptNumber, currentUser.id(), currentUser.role());
    }
}
