package com.portone.payment.controller;

import com.portone.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@Controller
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping(value = "/payment")
    public String payment() {
        return "payment";
    }

    @PostMapping(value = "/checkPayment", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public ResponseEntity<Object> checkPayment(@RequestBody Map<String, String> payload) {
        String imp_uid = payload.get("imp_uid");

        ResponseEntity<String> response = paymentService.checkPayment(imp_uid);

        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok().body(response.getBody());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response.getBody());
        }
    }
}
