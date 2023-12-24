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
    private static long expired_at;
    private final String portOneUrl = "https://api.iamport.kr";

    public ResponseEntity<Object> getAccessToken() {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> error = new HashMap<>();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("imp_key", imp_key);
        map.add("imp_secret", imp_secret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<String> response = null;
            try {
                response = restTemplate.postForEntity(portOneUrl + "/users/getToken", request, String.class);
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
        try {
            if (accessToken == null && isAccessTokenValid(expired_at)) {
                getAccessToken();
                System.out.println("accessToken 발급");
            }else {
                System.out.println("accessToken 이 유효합니다.");
            }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", accessToken);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);
            String url = portOneUrl + "/payments/" + imp_uid;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

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
        return isExpired < 0;
    }

}
