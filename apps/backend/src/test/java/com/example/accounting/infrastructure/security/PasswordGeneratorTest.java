package com.example.accounting.infrastructure.security;

import org.junit.jupiter.api.Test;

public class PasswordGeneratorTest {
    
    @Test
    void generateDemoPasswords() {
        PasswordEncoder encoder = new PasswordEncoder();
        
        System.out.println("-- Admin password: admin123");
        System.out.println("'" + encoder.encode("admin123") + "',");
        
        System.out.println("-- Manager password: manager123");
        System.out.println("'" + encoder.encode("manager123") + "',");
        
        System.out.println("-- Staff password: staff123");
        System.out.println("'" + encoder.encode("staff123") + "',");
        
        System.out.println("-- Viewer password: viewer123");
        System.out.println("'" + encoder.encode("viewer123") + "'");
    }
}
