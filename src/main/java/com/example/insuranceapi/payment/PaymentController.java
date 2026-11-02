package com.example.insuranceapi.payment;

import com.example.insuranceapi.policy.PolicyResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{orderNo}/pay")
    public ResponseEntity<PolicyResponse> pay(
            @PathVariable String orderNo
    ) {
        PaymentResult result = paymentService.pay(orderNo);

        return ResponseEntity
                .status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(result.policy());
    }
}
