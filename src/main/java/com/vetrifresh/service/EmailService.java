package com.vetrifresh.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vetrifresh.model.Order;
import lombok.RequiredArgsConstructor;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;


@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOrderConfirmation(String toEmail, Order order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Order Confirmed! #" + order.getOrderNumber());
        message.setText(
            "Hi " + order.getShippingName() + ",\n\n" +
            "Your order has been placed successfully!\n\n" +
            "Order Number: " + order.getOrderNumber() + "\n" +
            "Total Amount: ₹" + order.getTotalAmount() + "\n" +
            "Status: " + order.getStatus() + "\n\n" +
            "Thank you for shopping with VetriFresh!\n" +
            "Team VetriFresh"
        );
        mailSender.send(message);
    }
}
