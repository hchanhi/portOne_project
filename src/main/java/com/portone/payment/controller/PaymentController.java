package com.portone.payment.controller;

import com.portone.payment.service.PaymentService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
        ResponseEntity<String> cancelResponse = null;

        if (response.getStatusCode().is2xxSuccessful()) {
                JSONObject Json = null;
                JSONObject JsonResponse = null;
                String status = null;
                try {
                    JSONParser parser = new JSONParser();
                    Json = (JSONObject) parser.parse(String.valueOf(response.getBody()));
                    JsonResponse = (JSONObject) Json.get("response");
                    status = (String) JsonResponse.get("status");
                    System.out.println("payment status : " + status);

                    if(status.equals("paid")){
                        System.out.println("cancel payments");
                        cancelResponse = paymentService.cancelPayment(imp_uid);
                        return ResponseEntity.ok().body(cancelResponse.getBody());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            return ResponseEntity.ok().body(response.getBody());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response.getBody());
        }
    }
}
