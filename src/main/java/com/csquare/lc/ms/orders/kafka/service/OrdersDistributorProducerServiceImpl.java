package com.csquare.lc.ms.orders.kafka.service;

import com.csquare.lc.ms.orders.kafka.service.interfaces.OrdersDistributorProducerService;
import com.csquare.lc.ms.orders.lib.bo.NmOrderStatusPushBO;
import com.csquare.lc.ms.orders.lib.model.OrderStatusMapping;
import com.csquare.ms.lib.services.BaseServicesImpl;
import com.csquare.ms.lib.topics.MsApiTopic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service

public class OrdersDistributorProducerServiceImpl extends BaseServicesImpl implements OrdersDistributorProducerService {

    @Value("${kafka.producer.TOPIC.NAME}") private String TOPIC_NAME;
    @Autowired private KafkaTemplate<String, MsApiTopic> kafkaTemplate;

    public void sendAsyncMessage(MsApiTopic message) {
        ListenableFuture<SendResult<String, MsApiTopic>> listenableFuture = kafkaTemplate.send(TOPIC_NAME, message);
        listenableFuture.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("Failed  - {}", ex.toString());
            }
            @Override
            public void onSuccess(SendResult<String, MsApiTopic> result) {
                log.debug("Success - {}", result.getProducerRecord().value().getId());
            }
        });
    }

    @Override
    public String pushOrderDetails(NmOrderStatusPushBO nmOrderStatusPushBO, OrderStatusMapping orderStatusMapping) {
        Map<String, String> headers = new HashMap<>();
        if (!helper.isEmpty(orderStatusMapping.getJHeaders())) {
            headers = helper.fromJson(orderStatusMapping.getJHeaders(), Map.class);
        }
        String json = helper.toJson(nmOrderStatusPushBO);
        log.debug("Netmeds api call -> {},{},{}", orderStatusMapping.getCUrl(), json, headers);
        String response = this.callWebClientPostSyncApiWithHeader(orderStatusMapping.getCUrl(),
                json, headers);
        log.debug(response); // check the response
        return response + " -> " + json;
    }
}