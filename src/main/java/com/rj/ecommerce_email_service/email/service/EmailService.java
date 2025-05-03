package com.rj.ecommerce_email_service.email.service;

import com.rj.ecommerce_email_service.contract.v1.EcommerceEmailRequest;
import com.rj.ecommerce_email_service.contract.v1.notification.EmailDeliveryStatusDTO;
import com.rj.ecommerce_email_service.email.template.TemplateRegistry;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * Service for processing and sending emails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final TemplateRegistry templateRegistry;
    
    /**
     * Process and send an email based on the request
     */
    public EmailDeliveryStatusDTO processEmail(EcommerceEmailRequest request) {
        try {
            log.info("Processing email request: {}", request.getMessageId());
            
            // Create Thymeleaf context with template data
            Context context = new Context();
            request.getTemplateData().forEach(context::setVariable);
            
            // Process template
            String templatePath = templateRegistry.getTemplatePath(request.getTemplate());
            String htmlBody = templateEngine.process(templatePath, context);
            
            // Send email
            sendEmail(request.getTo(), request.getSubject(), htmlBody);
            
            log.info("Email sent successfully: {}", request.getMessageId());
            return EmailDeliveryStatusDTO.success(request.getMessageId(), request.getTo());
            
        } catch (Exception e) {
            log.error("Failed to process email: {}", request.getMessageId(), e);
            return EmailDeliveryStatusDTO.failure(
                    request.getMessageId(), 
                    request.getTo(), 
                    e.getMessage()
            );
        }
    }
    
    private void sendEmail(String to, String subject, String htmlBody) throws MessagingException {
        var message = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(message, true);
        
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        
        mailSender.send(message);
    }
}
