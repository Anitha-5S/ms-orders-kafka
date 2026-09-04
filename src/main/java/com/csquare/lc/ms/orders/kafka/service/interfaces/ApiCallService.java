package com.csquare.lc.ms.orders.kafka.service.interfaces;

import java.util.Map;

public interface ApiCallService {
    String postApiCall(String apiUrl, String requestBody, Map<String, String> headers);
    String postApiCall(String apiUrl, String requestBody);
}
