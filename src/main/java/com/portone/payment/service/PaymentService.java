package com.portone.payment.service;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
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


    public ResponseEntity<String> getAccessToken() {
        String url = portOneUrl + "/users/getToken";
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());


        try {
            long code;
            JSONObject Json = null;
            JSONObject JsonResponse = null;


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("imp_key", imp_key);
            body.add("imp_secret", imp_secret);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            try {
                JSONParser parser = new JSONParser();
                Json = (JSONObject) parser.parse(String.valueOf(response.getBody()));
                JsonResponse = (JSONObject) Json.get("response");
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (response.getStatusCode().is2xxSuccessful()) {
                code = (long) Json.get("code");
                if(code == 0){
                    System.out.println("Access token issue successful");
                    accessToken = (String) JsonResponse.get("access_token");
                    expired_at = (long) JsonResponse.get("expired_at");
                }
            }
            return response;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.out.println("Access token issue failed");
            return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        }catch (Exception e) {
            System.out.println("Access token issue failed");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public ResponseEntity<String> checkPayment(String imp_uid) {
        String url = portOneUrl + "/payments/" + imp_uid;
        ResponseEntity<String> response = null;

        try {
            if (accessToken == null) {
                System.out.println("There is no access token. Issue the access token.");
                response = getAccessToken();
                if(!response.getStatusCode().is2xxSuccessful()){
                    return response;
                }
            }
            else if(!isAccessTokenValid(expired_at)) {
                System.out.println("Access token expired, reissue the access token.");
                response = getAccessToken();
                if(!response.getStatusCode().is2xxSuccessful()){
                    return response;
                }
            }else {
                System.out.println("Access token is valid.");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", accessToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

            response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

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
            if(response.getStatusCode().is2xxSuccessful()) {
                JSONObject Json = null;
                JSONObject JsonResponse = null;
                long code;
                try {
                    JSONParser parser = new JSONParser();
                    Json = (JSONObject) parser.parse(String.valueOf(response.getBody()));
                    code = (long) Json.get("code");

                    if(code == 0){
                        System.out.println("Payment cancellation was successful.");
                        return response;
                    }else {
                        System.out.println("Payment cancellation failed. Please try again.");
                        return response;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            return response;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public boolean isAccessTokenValid(long expired_at) {
        long now = System.currentTimeMillis() / 1000L;
        boolean isExpired = expired_at > now;

        if (expired_at == 0) {
            System.out.println("There is no access token.");
        } else if (!isExpired) {
            System.out.println("Access token  is Expired");
        }
        return isExpired;
    }
}
