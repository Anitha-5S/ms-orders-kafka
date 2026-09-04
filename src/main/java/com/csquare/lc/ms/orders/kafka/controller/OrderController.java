package com.csquare.lc.ms.orders.kafka.controller;

import com.csquare.lc.ms.orders.kafka.transaction.interfaces.NonRilTransaction;
import com.csquare.lc.ms.orders.kafka.transaction.interfaces.OrderTransaction;
import com.csquare.lc.ms.orders.lib.model.OrderMst;
import com.csquare.lc.ms.orders.lib.model.OrderStatusEnum;
import com.csquare.lc.ms.orders.lib.model.mongo.order.LcOrder;
import com.csquare.ms.lib.api.ApiResponse;
import com.csquare.ms.lib.bo.LcHeaderBO;
import com.csquare.ms.lib.controller.LoBaseController;
import com.csquare.ms.lib.exceptions.DuplicateRecordException;
import com.csquare.ms.lib.exceptions.InvalidRequestException;
import com.csquare.ms.lib.exceptions.RecordNotFoundException;
import com.csquare.ms.lib.utils.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(path = {"/po/k/lo", "/lc/ms/ord/k"})
public class OrderController extends LoBaseController {

    @Autowired private OrderTransaction orderTransaction;

    @Autowired private NonRilTransaction nonRilTransaction;

    @PostMapping(path = "/order", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createPurchaseOrder(HttpServletRequest servletRequest, @RequestHeader Map<String, String> headers, @RequestBody String payload) throws RecordNotFoundException {
        ApiResponse apiResponse = this.initializeResponse("/po/k/lo/order->" + headers.toString() + ":" + payload);
        try {
            Long userId = this.getUserId(headers);
            OrderMst bo = helper.fromJson(payload, OrderMst.class);
            this.validateInputPayload(bo);

            if (orderTransaction.fetchByOrderRefNoAndTime(bo.getCSourceRefNo(), bo.getTSourceTimestamp()) != null)
                throw new DuplicateRecordException(bo.getCSourceRefNo() + Constants.HYPHEN + bo.getTSourceTimestamp(), "Order already processed!");

            OrderMst orderMst = orderTransaction.createOrder(userId, bo);
                if (!orderMst.getCC2code().equals("700000")) {
                    LcOrder lcOrder = orderTransaction.getLcOrder(userId, orderMst);
                    orderTransaction.saveLcOrder(lcOrder);
                }

            if (OrderStatusEnum.OP.toString().equals(bo.getCOrderStatus())) {
                String uri = servletRequest.getRequestURI();
                orderTransaction.distributeOrder(bo, headers, apiResponse, uri);
            }

//            Eg Neo changes
//            String c2CodeTypeName = nonRilTransaction.getC2CodeTypeName(orderMst.getCC2code());
//            if (CustomerNameEnum.RELIANCE.name().equals(c2CodeTypeName))
                orderTransaction.updateOrderStatus(bo);
//            else
//                nonRilTransaction.saveLcStoreSyncDetails(orderMst.getCC2code(), String.valueOf(servletRequest.getRequestURL()));

        } catch (DuplicateRecordException e) {
            this.handleAppExceptions(e, apiResponse);
        } catch (Exception e) {
            this.handleAppExceptions(e, apiResponse);
            try {
                orderTransaction.sendSellerCancellationOnError(payload); //TODO handle exception handling
                orderTransaction.notifyFailedProcessOrder(payload);
            } catch (Exception p) {
                log.error(p.getMessage());
            }
        }
        return this.getResponseEntity(apiResponse);
    }

    @PostMapping(path = "/push", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> pushPurchaseOrder(@RequestHeader Map<String, String> headers, @RequestBody String payload) {
        ApiResponse apiResponse = this.initializeResponse("/lc/ms/ord/k/push");
        try {
            JsonObject data = helper.getJsonObject(payload);
            OrderMst orderMst = orderTransaction.fetchByRefNo(data.get("sourceRefNo").getAsString());

            if (OrderStatusEnum.OP.toString().equals(orderMst.getCOrderStatus())) {
                orderTransaction.distributeOrder(orderMst, headers, apiResponse, "");

                if("700000".equals(orderMst.getCC2code()))
                    orderTransaction.updateOrderStatus(orderMst);
            }

        } catch (Exception e) {
            this.handleAppExceptions(e, apiResponse);
        }
        return this.getResponseEntity(apiResponse);
    }

    //TODO - check who is using this
    @PostMapping(path = "/order/multi", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createPurchaseOrders(@RequestHeader Map<String, String> headers, @RequestBody String payload) {
        ApiResponse apiResponse = this.initializeResponse("/po/k/lo/order->" + headers.toString() + ":" + payload);
        try {
            Long userId = this.getUserId(headers);
            JsonArray bos = helper.toJsonArrayTree(payload, OrderMst.class);

            for (JsonElement bo : bos) {
                OrderMst orderMst1 = helper.fromJson(bo.getAsJsonObject(), OrderMst.class);
                this.validateInputPayload(bo);
                OrderMst orderMst = orderTransaction.createOrder(userId, orderMst1);

                LcOrder lcOrder = orderTransaction.getLcOrder(userId, orderMst);

                orderTransaction.saveLcOrder(lcOrder);

                if (OrderStatusEnum.OP.toString().equals(orderMst1.getCOrderStatus())) {
                    orderTransaction.distributeOrder(orderMst1, headers, apiResponse, "");
                }
            }
        } catch (Exception e) {
            this.handleAppExceptions(e, apiResponse);
        }
        return this.getResponseEntity(apiResponse);
    }

    // internal api to re-push OP status orders
    @PostMapping(path = "/order/OP", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> orderOP(HttpServletRequest servletRequest, @RequestHeader Map<String, String> headers, @RequestBody String payload) {
        ApiResponse apiResponse = this.initializeResponse("/po/k/lo/order/OP->" + headers.toString() + ":" + payload);
        try {
            JsonObject data = helper.getJsonObject(payload);
            String c2Code = data.get("c_c2code").getAsString();
            String uri = servletRequest.getRequestURI();

            orderTransaction.orderOP(headers, apiResponse, c2Code, OrderStatusEnum.OP.toString(), uri);

        } catch (Exception e) {
            this.handleAppExceptions(e, apiResponse);
        }
        return this.getResponseEntity(apiResponse);
    }

    // internal api to test LcOrder conversion from OrdMst
    @PostMapping(path = "/order/convert", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> orderAll(@RequestHeader Map<String, String> headers, @RequestBody String payload) {
        ApiResponse apiResponse = this.initializeResponse("/po/k/lo/order->" + headers.toString() + ":" + payload);
        try {
            Long userId = this.getUserId(headers);
            List<String> ids = helper.fromJson(payload, List.class);

            for (String id : ids) {
                OrderMst mst = orderTransaction.getOrderMst(helper.getLong(id));
                LcOrder lcOrder = orderTransaction.getLcOrder(userId, mst);

                orderTransaction.saveLcOrder(lcOrder);
            }

        } catch (Exception e) {
            this.handleAppExceptions(e, apiResponse);
        }
        return this.getResponseEntity(apiResponse);
    }

    // internal api for testing LcOrder save in mongo from LcOrder payload
    @PostMapping(path = "/order/arch", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> saveLcOrder(@RequestHeader Map<String, String> headers, @RequestBody String payload) {
        ApiResponse apiResponse = this.initializeResponse("/po/k/lo/order/lc->" + headers.toString() + ":" + payload);
        try {
            Long userId = this.getUserId(headers);
            LcOrder lcOrder = helper.fromJson(payload, LcOrder.class);

            orderTransaction.saveLcOrder(lcOrder);

        } catch (Exception e) {
            this.handleAppExceptions(e, apiResponse);
        }
        return this.getResponseEntity(apiResponse);
    }

    @PostMapping(path = "/order/nm-code", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createPurchaseOrderWithoutUCode(HttpServletRequest servletRequest, @RequestHeader Map<String, String> headers, @RequestBody String payload) throws RecordNotFoundException {
        ApiResponse apiResponse = this.initializeResponse("/order/nm-code->" + headers.toString() + ":" + payload);
        try {
            Long userId = this.getUserId(headers);
            OrderMst bo = helper.fromJson(payload, OrderMst.class);
            this.validateInputPayload(bo);

            if (orderTransaction.fetchByOrderRefNoAndTime(bo.getCSourceRefNo(), bo.getTSourceTimestamp()) != null)
                throw new DuplicateRecordException(bo.getCSourceRefNo() + Constants.HYPHEN + bo.getTSourceTimestamp(), "Order already processed!");

            OrderMst orderMst = orderTransaction.createOrder(userId, bo);

            LcOrder lcOrder = orderTransaction.getLcOrder(userId, orderMst);

            orderTransaction.saveLcOrder(lcOrder);

            if (OrderStatusEnum.OP.toString().equals(bo.getCOrderStatus())) {
                String uri = servletRequest.getRequestURI();
                orderTransaction.distributeOrderWithoutUCode(bo, headers, apiResponse,uri);
            }

            if("700000".equals(orderMst.getCC2code()));
//                orderTransaction.updateOrderStatus(bo);

        } catch (DuplicateRecordException e) {
            this.handleAppExceptions(e, apiResponse);
        } catch (Exception e) {
            this.handleAppExceptions(e, apiResponse);
            try {
                orderTransaction.sendSellerCancellationOnError(payload);
                orderTransaction.notifyFailedProcessOrder(payload);
            } catch (Exception p) {
                log.error(p.getMessage());
            }
        }
        return this.getResponseEntity(apiResponse);
    }


    @PostMapping(path = "/cartToOrder", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> cartToOrder(HttpServletRequest servletRequest,@RequestHeader Map<String, String> headers, @RequestBody String payload) throws RecordNotFoundException {
        ApiResponse apiResponse = this.initializeResponse("/po/k/lo/cartToOrder->" + headers.toString() + ":" + payload);
        try {
            Long userId = getLOUserId(headers);
            LcHeaderBO lcHeaderBO = this.getLcHeader(headers);
            OrderMst bo = helper.fromJson(payload, OrderMst.class);
            this.validateInputPayload(bo);

            if (orderTransaction.fetchByOrderRefNoAndTime(bo.getCSourceRefNo(), bo.getTSourceTimestamp()) != null) {
                log.debug(bo.getCSourceRefNo() + Constants.HYPHEN + bo.getTSourceTimestamp(), "Order already processed!");
                throw new DuplicateRecordException(bo.getCSourceRefNo() + Constants.HYPHEN + bo.getTSourceTimestamp(), "Order already processed!");
            }

            orderTransaction.createLoOrder(userId, bo, lcHeaderBO);
//            OrderMst orderMst = orderTransaction.createOrder(userId, bo);

//            if (lcHeaderBO.getType().equals("C")) {
//                orderMst = orderTransaction.createTSOrder(userId, bo);
//            } else {
//                orderMst = orderTransaction.createOrder(userId, bo);
//            }

//            LcOrder lcOrder = orderTransaction.getLcOrder(userId,orderMst);

//            if (lcHeaderBO.getType().equals("C")) {
//                lcOrder.setOrderFrom("TS");
//            }

//            orderTransaction.saveLcOrder(lcOrder);

            if (OrderStatusEnum.OP.toString().equals(bo.getCOrderStatus())) {
                String uri = servletRequest.getRequestURI();
                orderTransaction.distributeOrder(bo, headers, apiResponse,uri);
            }

//            if("700000".equals(orderMst.getCC2code()))
//                orderTransaction.updateOrderStatus(bo);

        } catch (DuplicateRecordException e) {
            this.handleAppExceptions(e, apiResponse);
        } catch (Exception e) {
            this.handleAppExceptions(e, apiResponse);
            try {
                orderTransaction.sendSellerCancellationOnError(payload); //TODO handle exception handling
                orderTransaction.notifyFailedProcessOrder(payload);
            } catch (Exception p) {
                log.error(p.getMessage());
            }
        }
        return this.getResponseEntity(apiResponse);
    }

    @PostMapping(path = "/b2c/web/order/confirm", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> orderConfirm(HttpServletRequest servletRequest,@RequestHeader Map<String, String> headers, @RequestBody String payload) throws RecordNotFoundException {
        ApiResponse apiResponse = this.initializeResponse("/po/k/lo/order/confirm->" + headers.toString() + ":" + payload);
        try {
            Long userId = getLOUserId(headers);
            JsonObject request = helper.fromJson(payload, JsonObject.class);
            OrderMst bo = helper.fromJson(payload, OrderMst.class);
            this.validateInputPayload(bo);

            OrderMst orderMst;
            LcOrder lcOrder;
            if (orderTransaction.fetchByOrderRefNoAndTime(bo.getCSourceRefNo(), bo.getTSourceTimestamp()) != null) {
                log.debug(bo.getCSourceRefNo() + Constants.HYPHEN + bo.getTSourceTimestamp(), "Updating already processed Order!");
                orderMst = orderTransaction.createOrder(bo.getCustomerDetail().getNCustomerId(), bo);

            } else {
                log.debug("Creating new Order!");
                orderMst = orderTransaction.createOrder(bo.getCustomerDetail().getNCustomerId(), bo);
            }

            lcOrder = orderTransaction.getLcOrder(userId, orderMst);
            lcOrder.setOrderFrom("TS");
            if (request.has("c_discount_type") && request.get("c_discount_type").getAsString() != null) {
                lcOrder.setDiscountType(request.get("c_discount_type").getAsString());
                if (request.has("n_discount_percentage") && !helper.isEmpty(request.get("n_discount_percentage").getAsString())) {
                    lcOrder.setDiscountPercentage(request.get("n_discount_percentage").getAsBigDecimal());
                    lcOrder.setDiscountAmount(request.get("n_discount_amount").getAsBigDecimal());
                }
                if (request.has("n_discount_amount") && !helper.isEmpty(request.get("n_discount_amount").getAsString())) {
                    lcOrder.setDiscountAmount(request.get("n_discount_amount").getAsBigDecimal());
                }
            }
            orderTransaction.saveLcOrder(lcOrder);

            if ("700000".equals(orderMst.getCC2code()))
                orderTransaction.updateOrderStatus(bo);

        } catch (Exception e) {
            this.handleAppExceptions(e, apiResponse);
            try {
                orderTransaction.sendSellerCancellationOnError(payload); //TODO handle exception handling
                orderTransaction.notifyFailedProcessOrder(payload);
            } catch (Exception p) {
                log.error(p.getMessage());
            }
        }
        return this.getResponseEntity(apiResponse);
    }

    private static final String USER_ID = "x-csquare-terminal-id";
    protected Long getLOUserId(Map<String, String> headers) throws InvalidRequestException {
        String id = headers.get(USER_ID);
        if (helper.isEmpty(id)) {
            throw new InvalidRequestException("", "User id is not set!");
        }
        return Long.parseLong(id);
    }
}
