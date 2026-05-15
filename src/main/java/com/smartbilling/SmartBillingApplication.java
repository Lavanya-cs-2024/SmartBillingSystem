package com.smartbilling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.annotation.SessionScope;
import com.smartbilling.service.CartService;

@SpringBootApplication
public class SmartBillingApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartBillingApplication.class, args);
        System.out.println("\n✅ Smart Billing System Started!");
        System.out.println("📍 Customer UI: http://localhost:8080/customer.html");
        System.out.println("📍 Admin UI: http://localhost:8080/admin.html\n");
    }

    @Bean
    @SessionScope
    public CartService cartService() {
        return new CartService();
    }
}