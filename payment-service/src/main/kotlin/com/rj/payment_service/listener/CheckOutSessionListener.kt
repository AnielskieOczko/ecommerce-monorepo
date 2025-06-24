package listener

import com.rj.payment_service.service.PaymentService
import org.springframework.stereotype.Component

@Component
class CheckOutSessionListener(
    private val paymentService: PaymentService,
    private val messageProducer: MessageProducer
) {
}