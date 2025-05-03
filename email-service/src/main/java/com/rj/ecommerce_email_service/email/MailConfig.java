package com.rj.ecommerce_email_service.email;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@Profile("local")
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost"); // Get from properties if you prefer
        mailSender.setPort(1025);        // Get from properties if you prefer
        // mailSender.setUsername(...); // If needed
        // mailSender.setPassword(...); // If needed

        // Properties props = mailSender.getJavaMailProperties();
        // props.put("mail.smtp.auth", "false");
        // props.put("mail.smtp.starttls.enable", "false");

        return mailSender;
    }
}
