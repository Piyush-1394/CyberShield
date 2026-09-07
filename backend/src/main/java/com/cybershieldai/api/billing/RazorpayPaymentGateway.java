package com.cybershieldai.api.billing;

import com.cybershieldai.api.config.AppProperties;
import com.razorpay.RazorpayClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class RazorpayPaymentGateway implements PaymentGateway {
    private static final Logger log = LoggerFactory.getLogger(RazorpayPaymentGateway.class);
    private final AppProperties props;

    public RazorpayPaymentGateway(AppProperties props) {
        this.props = props;
    }

    @Override
    public TokenizedMethod tokenize(AddPaymentRequest request) {
        String token = request.gatewayToken();
        if (token == null || token.isBlank()) {
            token = "tok_mock_" + request.last4();
        }
        if (props.razorpay().keyId() != null && !props.razorpay().keyId().isBlank()) {
            try {
                new RazorpayClient(props.razorpay().keyId(), props.razorpay().keySecret());
                log.info("Razorpay client initialized in test/live mode for tokenization token={}", token);
            } catch (Exception e) {
                log.warn("Razorpay client unavailable, storing token locally: {}", e.getMessage());
            }
        } else {
            log.info("RAZORPAY_KEY_ID not set — using mock tokenization");
        }
        return new TokenizedMethod(
                request.brand() == null ? "card" : request.brand(),
                request.last4(),
                request.methodType() == null ? "card" : request.methodType(),
                token
        );
    }
}
