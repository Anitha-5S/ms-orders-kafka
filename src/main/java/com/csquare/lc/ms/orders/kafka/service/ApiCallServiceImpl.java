package com.csquare.lc.ms.orders.kafka.service;
import com.csquare.lc.ms.orders.kafka.service.interfaces.ApiCallService;
import com.csquare.ms.lib.services.BaseServicesImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class ApiCallServiceImpl extends BaseServicesImpl implements ApiCallService {

    @Override
    public String postApiCall(String apiUrl, String requestBody, Map<String, String> headers) {
        log.debug("{},{},{}", apiUrl, requestBody, headers);
        String response = callWebClientPostSyncApiWithHeader(apiUrl, requestBody, headers);
        log.debug("{}", response);
        return response;
    }

    @Override
    public String postApiCall(String apiUrl, String requestBody) {
        log.debug("{},{},{}", apiUrl, requestBody);
        String response = callWebClientPostSyncApi(apiUrl, requestBody);
        log.debug("response {}", response);
        return response;
    }


}
