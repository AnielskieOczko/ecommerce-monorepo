package com.rj.notification_service.messaging.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.rj.notification_service.config.AppProperties
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AppProperties::class)
class RabbitMQConfig {

    // --- Email Request Queue, DLX, and DLQ ---

    @Bean
    fun notificationRequestDlx(props: AppProperties): DirectExchange {
        // The DLX is a simple Direct Exchange
        return DirectExchange(props.rabbitmq.notificationRequest.dlq!!.exchange)
    }

    @Bean
    fun notificationRequestDlq(props: AppProperties): Queue {
        // The DLQ name is derived from the main queue name for consistency
        return Queue("${props.rabbitmq.notificationRequest.queue}.dlq")
    }

    @Bean
    fun notificationRequestDlqBinding(props: AppProperties): Binding {
        // Bind the DLQ to the DLX with the DLQ routing key
        return BindingBuilder.bind(notificationRequestDlq(props))
            .to(notificationRequestDlx(props))
            .with(props.rabbitmq.notificationRequest.dlq!!.routingKey)
    }

    @Bean
    fun notificationRequestExchange(props: AppProperties): TopicExchange =
        TopicExchange(props.rabbitmq.notificationRequest.exchange)

    @Bean
    fun notificationRequestQueue(props: AppProperties): Queue {
        // CORRECT: The main queue is now defined in one place, with its DLQ arguments attached.
        return QueueBuilder.durable(props.rabbitmq.notificationRequest.queue)
            .withArgument("x-dead-letter-exchange", props.rabbitmq.notificationRequest.dlq!!.exchange)
            .withArgument("x-dead-letter-routing-key", props.rabbitmq.notificationRequest.dlq!!.routingKey)
            .build()
    }

    @Bean
    fun notificationRequestBinding(props: AppProperties): Binding =
        BindingBuilder.bind(notificationRequestQueue(props))
            .to(notificationRequestExchange(props))
            .with(props.rabbitmq.notificationRequest.routingKey)

    // --- Email Receipt Queue (no DLQ needed for this service) ---

    @Bean
    fun notificationReceiptExchange(props: AppProperties): TopicExchange =
        TopicExchange(props.rabbitmq.notificationReceipt.exchange)

    @Bean
    fun notificationReceiptQueue(props: AppProperties): Queue =
        Queue(props.rabbitmq.notificationReceipt.queue)

    @Bean
    fun notificationReceiptBinding(props: AppProperties): Binding =
        BindingBuilder.bind(notificationReceiptQueue(props))
            .to(notificationReceiptExchange(props))
            .with(props.rabbitmq.notificationReceipt.routingKey)

    // --- Serialization Beans (Simplified) ---

    @Bean
    fun rabbitObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(JavaTimeModule())
            registerModule(KotlinModule.Builder().build()) // IMPORTANT for Kotlin data classes
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

            // This is still needed to deserialize the generic EmailRequest<T> correctly.
            activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
            )
        }
    }

    @Bean
    fun jsonMessageConverter(objectMapper: ObjectMapper): MessageConverter {
        // 1. Create the converter and pass it our custom ObjectMapper
        val converter = Jackson2JsonMessageConverter(objectMapper)

        // 2. Create a custom Class Mapper to control deserialization
        val typeMapper = DefaultJackson2JavaTypeMapper()

        // 3. Configure the trusted packages. This is the crucial security step.
        // Whitelisting specific packages is more secure than trusting all ("*").
        typeMapper.setTrustedPackages(
            "com.rj.ecommerce.api.shared", // Trust the root of our shared DTOs
            "java.util",                   // Trust standard Java collections
            "java.time"                    // Trust standard Java time objects
        )

        // 4. Set the custom, configured Class Mapper on the converter.
        converter.setClassMapper(typeMapper)

        return converter
    }

    // --- Admin Bean (Consolidated) ---
    @Bean
    fun rabbitAdmin(connectionFactory: ConnectionFactory): RabbitAdmin {
        return RabbitAdmin(connectionFactory)
    }
}