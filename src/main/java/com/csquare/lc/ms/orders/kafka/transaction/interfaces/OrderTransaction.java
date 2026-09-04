package com.csquare.lc.ms.orders.kafka.transaction.interfaces;

import com.csquare.lc.ms.orders.lib.model.OrderMst;
import com.csquare.lc.ms.orders.lib.model.mongo.order.LcOrder;
import com.csquare.ms.lib.api.ApiResponse;
import com.csquare.ms.lib.bo.LcHeaderBO;
import com.csquare.ms.lib.exceptions.InputPayloadException;
import com.csquare.ms.lib.exceptions.RecordNotFoundException;

import java.time.LocalDateTime;
import java.util.Map;

public interface OrderTransaction {

    OrderMst getOrderMst(Long id) throws RecordNotFoundException;

    OrderMst fetchByOrderRefNoAndTime(String orderRefNo, LocalDateTime time);

    OrderMst createOrder(Long userId, OrderMst mst);

    void distributeOrder(OrderMst bo, Map<String, String> headers, ApiResponse apiResponse, String uri);

    void distributeOrderWithoutUCode(OrderMst bo, Map<String, String> headers, ApiResponse apiResponse, String url);

    LcOrder getLcOrder(Long userId, OrderMst orderMst) throws RecordNotFoundException;

    void saveLcOrder(LcOrder lcOrder) throws InputPayloadException;

    void notifyFailedProcessOrder(String payload);

    void updateOrderStatus(OrderMst orderMst);

    void sendSellerCancellationOnError(String payload) throws RecordNotFoundException;

    OrderMst fetchByRefNo(String orderId) throws RecordNotFoundException;

    void toLc1Order(LcOrder lcOrder);

    int orderOP(Map<String, String> headers, ApiResponse apiResponse, String c2Code, String status, String uri);

    OrderMst updateOrder(Long userId, OrderMst bo);

    OrderMst createTSOrder(Long userId, OrderMst bo);

    void createLoOrder(Long userId, OrderMst bo, LcHeaderBO lcHeaderBO) throws RecordNotFoundException, InputPayloadException;
}
