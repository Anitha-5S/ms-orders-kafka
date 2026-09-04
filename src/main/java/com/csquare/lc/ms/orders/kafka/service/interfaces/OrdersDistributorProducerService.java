package com.csquare.lc.ms.orders.kafka.service.interfaces;

import com.csquare.lc.ms.orders.lib.bo.NmOrderStatusPushBO;
import com.csquare.lc.ms.orders.lib.model.OrderStatusMapping;
import com.csquare.ms.lib.services.interfaces.BaseService;
import com.csquare.ms.lib.topics.MsApiTopic;

public interface OrdersDistributorProducerService extends BaseService {
    void sendAsyncMessage(MsApiTopic message);
    String pushOrderDetails(NmOrderStatusPushBO nmOrderStatusPushBO, OrderStatusMapping orderStatusMapping);
}