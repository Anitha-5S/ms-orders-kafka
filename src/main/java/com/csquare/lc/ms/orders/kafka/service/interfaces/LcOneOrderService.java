package com.csquare.lc.ms.orders.kafka.service.interfaces;

import com.csquare.lc.ms.orders.lib.model.mongo.order.LcOrder;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;

public interface LcOneOrderService {

    JsonObject toLc1Order(LcOrder lcOrder);

    String getTransId (String id);
}
