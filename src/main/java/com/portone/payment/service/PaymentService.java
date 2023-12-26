package com.portone.payment.service;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {
    @Value("${portone.impkey}")
    private String imp_key;
    @Value("${portone.impSecret}")
    private String imp_secret;
    private static String accessToken;
    private static long expired_at = 0;
    private final String portOneUrl = "https://api.iamport.kr";

    RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<Object> getAccessToken() {
        String url = portOneUrl + "/users/getToken";
        Map<String, String> error = new HashMap<>();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("imp_key", imp_key);
        body.add("imp_secret", imp_secret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = null;
            try {
                response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                return ResponseEntity.status(e.getStatusCode()).body(e.getStackTrace());
            }

        JSONObject Json = null;
        JSONObject JsonResponse = null;
            try {
                JSONParser parser = new JSONParser();
                Json = (JSONObject) parser.parse(String.valueOf(response.getBody()));
                JsonResponse = (JSONObject) Json.get("response");
            } catch (ParseException e) {
                e.printStackTrace();
            }

        if (response.getStatusCode().is2xxSuccessful()) {
            accessToken = (String) JsonResponse.get("access_token");
            expired_at = (long) JsonResponse.get("expired_at");
            return ResponseEntity.ok().body(accessToken);
        } else if(response.getStatusCode().is4xxClientError()){
            error.put("code", (String) Json.get("code"));
            error.put("message", (String) Json.get("message"));
            return ResponseEntity.status(response.getStatusCode()).body(error);
        } else {
            error.put("code", "500");
            error.put("message", "Internal Server Error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    public ResponseEntity<String> checkPayment(String imp_uid) {
        String url = portOneUrl + "/payments/" + imp_uid;
        System.out.println(isAccessTokenValid(expired_at));
        try {
            if (accessToken == null || !isAccessTokenValid(expired_at)) {
                getAccessToken();
                System.out.println("Issue accessToken.");
            }else {
                System.out.println("accessToken is valid.");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", accessToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            return response;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<String> cancelPayment(String imp_uid) {
        String url = portOneUrl + "/payments/cancel";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("imp_uid", imp_uid);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            return response;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public boolean isAccessTokenValid(long expired_at) {
        long now = System.currentTimeMillis() / 1000L;
        long isExpired = expired_at - now;
        System.out.println("isExpired : " + isExpired);
        return expired_at > now;
    }

}
