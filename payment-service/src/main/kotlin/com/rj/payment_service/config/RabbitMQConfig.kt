package config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.amqp.core.*
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig(private val props: RabbitMQProperties) { // Inject properties

    // --- Queues, Exchanges, Bindings using properties ---
    @Bean fun checkoutSessionExchange(): TopicExchange = TopicExchange(props.checkoutSessionExchange)
    @Bean fun checkoutSessionQueue(): Queue = Queue(props.checkoutSessionQueue)
    @Bean fun checkoutSessionBinding(): Binding = BindingBuilder.bind(checkoutSessionQueue())
        .to(checkoutSessionExchange()).with(props.checkoutSessionRoutingKey)

    // ... other bindings for DLQ etc. ...

    // --- CRITICAL FIX: Decoupled Message Converter ---
    @Bean
    fun jsonMessageConverter(): MessageConverter {
        val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
        // DO NOT configure default typing or class mappers.
        // The contract is the JSON structure, not the Java class.
        return Jackson2JsonMessageConverter(objectMapper)
    }
}