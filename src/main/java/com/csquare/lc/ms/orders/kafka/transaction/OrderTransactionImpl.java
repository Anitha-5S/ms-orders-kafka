package com.csquare.lc.ms.orders.kafka.transaction;
import com.csquare.lc.ms.orders.kafka.service.interfaces.*;
import com.csquare.lc.ms.orders.kafka.transaction.interfaces.OrderTransaction;
import com.csquare.lc.ms.orders.lib.bo.*;
import com.csquare.lc.ms.orders.lib.model.BillingDetail;
import com.csquare.lc.ms.orders.lib.model.ContactDetail;
import com.csquare.lc.ms.orders.lib.model.CustomerDetail;
import com.csquare.lc.ms.orders.lib.model.DeliveryDetail;
import com.csquare.lc.ms.orders.lib.model.OrderDet;
import com.csquare.lc.ms.orders.lib.model.OrderMst;
import com.csquare.lc.ms.orders.lib.model.PaymentDetail;
import com.csquare.lc.ms.orders.lib.model.Prescription;
import com.csquare.lc.ms.orders.lib.model.PrescriptionDoc;
import com.csquare.lc.ms.orders.lib.model.ShippingDetail;
import com.csquare.lc.ms.orders.lib.model.*;
import com.csquare.lc.ms.orders.lib.model.mongo.master.LcItem;
import com.csquare.lc.ms.orders.lib.model.mongo.order.LcOrder;
import com.csquare.lc.ms.orders.lib.repos.OrderByPrescriptionRepository;
import com.csquare.lc.ms.orders.lib.services.interfaces.*;
import com.csquare.lc.ms.orders.lib.utils.LoConstants;
import com.csquare.ms.lib.api.ApiResponse;
import com.csquare.ms.lib.bo.LcHeaderBO;
import com.csquare.ms.lib.exceptions.InputPayloadException;
import com.csquare.ms.lib.exceptions.RecordNotFoundException;
import com.csquare.ms.lib.topics.MsApiTopic;
import com.csquare.ms.lib.transactions.BaseTransactionImpl;
import com.csquare.ms.lib.utils.Constants;
import com.google.gson.JsonObject;
import lombok.extern.log4j.Log4j2;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Log4j2
@Component
public class OrderTransactionImpl extends BaseTransactionImpl implements OrderTransaction {

    @Value("${one.pharma.send.email}") private String apiUrl;
    @Value("${order.failed.notification.email.recipients}") private String recipients;
    @Value("${egCloud.order.status.update.url.endpoint}") private String statusUpdateUrl;


    @Autowired private ApiCallService apiCallService;
    @Autowired private LcOrderService lcOrderService;
    @Autowired private OrderMstService orderMstService;
    @Autowired private OrderDetService orderDetService;
    @Autowired private OrderMappingService orderMappingService;
    @Autowired private PrescriptionService prescriptionService;
    @Autowired private ContactDetailService contactDetailService;
    @Autowired private PaymentDetailService paymentDetailService;
    @Autowired private ShippingDetailService shippingDetailService;
    @Autowired private DeliveryDetailService deliveryDetailService;
    @Autowired private PrescriptionDocService prescriptionDocService;
    @Autowired private OrderStatusMappingService orderStatusMappingService;
    @Autowired private OrderTransactionLedgerService orderTransactionLedgerService;
    @Autowired private OrdersDistributorProducerService ordersDistributorProducerService;
    @Autowired private ItemService itemService;
    @Autowired private NonRilService nonRilService;
    @Autowired private LcOneOrderService lcOneOrderService;
    @Autowired private OrderByPrescriptionRepository orderByPrescriptionRepository;
    @Override
    public OrderMst getOrderMst(Long id) throws RecordNotFoundException {
        return orderMstService.fetchById(id);
    }

    @Override
    public OrderMst fetchByOrderRefNoAndTime(String orderRefNo, LocalDateTime time) {
        return orderMstService.fetchByOrderRefNoAndTime(orderRefNo, time);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public OrderMst createOrder(Long userId, OrderMst mst) {

        LocalDateTime time = helper.getCurrentTime();
        if (helper.isEmpty(mst.getTOrderCreatedTimestamp())) { // order create timestamp is set in the producer
            mst.setTOrderCreatedTimestamp(time);
        } else {
            time = mst.getTOrderCreatedTimestamp();
        }

        mst.setIdTime(userId, time);

        if (mst.getDeliveryDetail() != null) {
            createDeliveryDetail(userId, time, mst);
        }

        if (mst.getShippingDetail() != null) {
            createShippingDetail(userId, time, mst);
        }

        if (mst.getPaymentDetails() != null) {
            createPaymentDetail(userId, time, mst);
        }

        if (mst.getPrescription() != null) {
            createPrescription(userId, time, mst);
        }

        if (mst.getBillingDetail() != null) {
            createBillingDetails(userId, time, mst);
        }

        if (mst.getCustomerDetail() != null) {
            createCustomerDetail(userId, time, mst);
        }

        if(mst.getCouponDetail() != null){
            createCouponDetail(time, mst);
        } else {
            mst.setnAdditionalDisc(new BigDecimal(0));
        }

        createOrderDet(userId, time, mst);
        orderTransactionLedgerService.save(mst.getNOrderNo(), mst.getCOrderStatus());
        return orderMstService.save(mst);
    }

    private void createCouponDetail(LocalDateTime time, OrderMst mst) {
        BigDecimal additionalDisAmt = new BigDecimal(0);
        List<CouponDetail> couponDetailList = new ArrayList<>();

        Map<String, BigDecimal> coupons = new HashMap<>();
        for (CouponDetail couponDetail : mst.getCouponDetail()) {
            coupons.put(couponDetail.getCCouponCode(), couponDetail.getNCouponAmount());
        }

        for (Map.Entry<String, BigDecimal> entry : coupons.entrySet()) {
            CouponDetail detail = new CouponDetail();
            detail.setNOrderNo(mst.getNOrderNo());
            detail.setCCouponCode(entry.getKey());
            detail.setNCouponAmount(entry.getValue());
            detail.setTCreatedTimestamp(time);
            detail.setOrderMst(mst);
            couponDetailList.add(detail);

            additionalDisAmt = additionalDisAmt.add(entry.getValue());
        }

        mst.setnAdditionalDisc(additionalDisAmt);
        mst.setCouponDetail(couponDetailList);
    }

    private void createBillingDetails(Long userId, LocalDateTime time, OrderMst mst) {
        BillingDetail billingDetail = mst.getBillingDetail();
        billingDetail.setIdTime(userId, time);
        billingDetail.getContactDetail().setCAddress2(helper.isEmpty(billingDetail.getContactDetail().getCAddress2())?"-":billingDetail.getContactDetail().getCAddress2());
        billingDetail.setContactDetail(createContactDetail(userId, time, billingDetail.getContactDetail()));
        mst.setBillingDetail(billingDetail);
    }

    private void createCustomerDetail(Long userId, LocalDateTime time, OrderMst mst) {
        CustomerDetail customerDetail = mst.getCustomerDetail();
        customerDetail.setIdTime(userId, time);
        customerDetail.setContactDetail(createContactDetail(userId, time, customerDetail.getContactDetail()));
        mst.setCustomerDetail(customerDetail);
    }

    private void createOrderDet(Long userId, LocalDateTime time, OrderMst mst) {
        List<OrderDet> list = new ArrayList<>();
        int seq = 0;
        for (OrderDet det:mst.getOrderDets()) {
            det.setnOrderNo(mst.getNOrderNo());
            det.setnSeq(++seq);
            det.setIdTime(userId, time);
            det.setOrderMst(mst);
            list.add(det);
        }
        mst.setOrderDets(list);
    }

    private ContactDetail createContactDetail(Long userId, LocalDateTime time, ContactDetail contactDetail){
        contactDetail.setIdTime(userId, time);
        contactDetailService.save(contactDetail);
        return contactDetail;
    }

    private void createDeliveryDetail(Long userId, LocalDateTime time, OrderMst mst) {
        DeliveryDetail deliveryDetail = mst.getDeliveryDetail();
        deliveryDetail.setIdTime(userId, time);
        deliveryDetail.getContactDetail().setCAddress2(helper.isEmpty(deliveryDetail.getContactDetail().getCAddress2())?"-":deliveryDetail.getContactDetail().getCAddress2());
        deliveryDetail.setContactDetail(createContactDetail(userId, time, deliveryDetail.getContactDetail()));
//        deliveryDetailService.save(deliveryDetail);
        mst.setDeliveryDetail(deliveryDetail);
    }

    private void createShippingDetail(Long userId, LocalDateTime time, OrderMst mst) {
        ShippingDetail shippingDetail = mst.getShippingDetail();
        shippingDetail.setIdTime(userId, time);
        shippingDetail.getContactDetail().setCAddress2(helper.isEmpty(shippingDetail.getContactDetail().getCAddress2())?"-":shippingDetail.getContactDetail().getCAddress2());
        shippingDetail.setContactDetail(createContactDetail(userId, time, shippingDetail.getContactDetail()));
//        shippingDetailService.save(shippingDetail);
        mst.setShippingDetail(shippingDetail);
    }

    private void createPrescription(Long userId, LocalDateTime time, OrderMst mst) {
        Prescription prescription = mst.getPrescription();
        prescription.setIdTime(userId, time);
        if (prescription.getContactDetail() != null) {
            prescription.setContactDetail(createContactDetail(userId, time, prescription.getContactDetail()));
        }
        if (prescription.getPrescriptionDocs() != null) {
            createPrescriptionDocs(userId, time, prescription);
        }
        mst.setPrescription(prescription);
    }

    private void createPrescriptionDocs(Long userId, LocalDateTime time, Prescription prescription) {
        List<PrescriptionDoc> prescriptionDocs = new ArrayList<>();
        for (PrescriptionDoc doc : prescription.getPrescriptionDocs()){
            doc.setIdTime(userId, time);
            doc.setPrescription(prescription);
//            prescriptionDocService.save(doc);
            prescriptionDocs.add(doc);
        }
        prescription.setPrescriptionDocs(prescriptionDocs);
    }

    private void createPaymentDetail(Long userId, LocalDateTime time, OrderMst mst) {
        List<PaymentDetail> paymentDetailList = new ArrayList<>();
        for (PaymentDetail paymentDetail:mst.getPaymentDetails()){
            paymentDetail.setIdTime(userId, time);
            paymentDetail.setOrderMst(mst);
            paymentDetailList.add(paymentDetail);
        }
        mst.setPaymentDetails(paymentDetailList);
    }

    private void saveDeliveryDetail(Long userId, DeliveryDetail detail) {
        detail.setLastUpdated(userId, helper.getCurrentTime());
        deliveryDetailService.save(detail);
    }

    @Override
    public void distributeOrder(OrderMst bo, Map<String, String> headers, ApiResponse apiResponse, String uri) {
        OrderMapping orderMapping = orderMappingService.findById(bo.getCC2code(), bo.getCCustCode());
        if (orderMapping != null) {
            OrderDistributorBO orderDistributorBO = new OrderDistributorBO(orderMapping, bo);
            log.debug("push order to order distribution");
            ordersDistributorProducerService.sendAsyncMessage(new MsApiTopic(apiResponse.getRequestId(), uri, helper.toJson(orderDistributorBO), helper.getCurrentTime(), headers));
        }
    }

    @Override
    public void distributeOrderWithoutUCode(OrderMst bo, Map<String, String> headers, ApiResponse apiResponse,String url) {
        OrderMapping orderMapping = orderMappingService.findById(bo.getCC2code(), bo.getCCustCode());
        if (orderMapping != null) {
            OrderDistributorBO orderDistributorBO = new OrderDistributorBO(orderMapping, bo);

            ordersDistributorProducerService.sendAsyncMessage(new MsApiTopic(apiResponse.getRequestId(), url, helper.toJson(orderDistributorBO), helper.getCurrentTime(), headers));
        }
    }

    @Override
    public LcOrder getLcOrder(Long userId, OrderMst orderMst) throws RecordNotFoundException {
        //TODO - generate LcOrder from OrderMst
        LcOrder lcOrder = new LcOrder();
        lcOrder.setOrderId(helper.toString(orderMst.getNOrderNo()));
        if (orderMst.getNOrderNo() == null) {
            ObjectId objectId = new ObjectId();
            lcOrder.setOrderId(objectId.toString());
        }
        lcOrder.setUserId(String.valueOf(userId));
        lcOrder.setCustCode(orderMst.getCCustCode());
        lcOrder.setPaymentStatus(Constants.STATUS_NO);
        lcOrder.setSourceRef(orderMst.getCSourceRefNo());
        lcOrder.setCombineCode(orderMst.getCCustCode() +"|"+ orderMst.getCC2code());
        if (orderMst.getCDeliveryBranchId()!=null)
        lcOrder.setDeliveryBranchId(Long.parseLong(orderMst.getCDeliveryBranchId()));
        lcOrder.setBranchId(orderMst.getOrderBranchId());
        lcOrder.setOrderSummary(new LcOrderSummaryBO());
        lcOrder.getOrderSummary().setOrderId(helper.toString(orderMst.getNOrderNo()));
        lcOrder.getOrderSummary().setSellerCode(orderMst.getCC2code());
        if(orderMst.getCSourceRefNo().startsWith("LO_")){
           // lcOrder.setCustCode(orderMst.getCC2code());
            lcOrder.setOrderPrefix(LoConstants.LO_ORDER_PREFIX);
            lcOrder.getOrderSummary().setSellerCode(orderMst.getCC2code());
        }
        lcOrder.getOrderSummary().setSellerName(orderMst.getCSellerName());
        lcOrder.getOrderSummary().setOrderDate(orderMst.getTSourceTimestamp());
        lcOrder.getOrderSummary().setOrderStatus(orderMst.getCOrderStatus());
        lcOrder.getOrderSummary().setNoOfLineItems(orderMst.getOrderDets().size());
        lcOrder.setMobileNo(orderMst.getCNote());
        /*  if(orderMst.getBillingDetail()!= null){
            if (orderMst.getBillingDetail().getContactDetail() != null) {
                if (!helper.isEmpty(orderMst.getBillingDetail().getContactDetail().getCMobileNo()))
                    lcOrder.setMobileNo(orderMst.getBillingDetail().getContactDetail().getCMobileNo());
            }

        }*/
        if (helper.isEmpty(lcOrder.getOrderSummary().getSellerLogoUrl()) &&
        !helper.isEmpty(lcOrder.getOrderSummary().getSellerCode())) {

            JsonObject data = itemService.getSellerLogo(
                    lcOrder.getOrderSummary().getSellerCode());
            if (data != null) {
                if (data.has("c_seller_logo") && !helper.isEmpty(data.get("c_seller_logo").getAsString())) {
                    lcOrder.getOrderSummary().setSellerLogoUrl(data.get("c_seller_logo").getAsString());
                }
                if (helper.isEmpty(lcOrder.getOrderSummary().getSellerName()) &&
                        data.has("c_seller_name") && !helper.isEmpty(data.get("c_seller_name").getAsString()))
                    lcOrder.getOrderSummary().setSellerName(data.get("c_seller_name").getAsString());
            }
        }
        if (orderMst.getCustomerDetail() != null) {
            lcOrder.getOrderSummary().setCustomerName(orderMst.getCustomerDetail().getCCustomerName());
            lcOrder.getOrderSummary().setBuyerArea(orderMst.getCustomerDetail().getContactDetail().getCCity());
        }

        LcOrderDetailsBO lcOrderDetailsBO = new LcOrderDetailsBO();
        lcOrderDetailsBO.setCashDiscount(orderMst.getNDiscountAmount());
        lcOrderDetailsBO.setSellerName(orderMst.getCSellerName());
        lcOrderDetailsBO.setSubTotal(orderMst.getNItemTotal());
       // lcOrderDetailsBO.setInvoiceNumber(orderMst.getCInvoiceRefNo());
        lcOrderDetailsBO.setOrderId(helper.toString(orderMst.getNOrderNo()));
        lcOrderDetailsBO.setAmountPaid(orderMst.getNNetPayableAmount());
        BigDecimal gst = orderMst.getNNetPayableAmount().subtract(orderMst.getNItemTotal());
        lcOrderDetailsBO.setGstAmount(gst.add(orderMst.getNDiscountAmount()));
        lcOrder.setOrderDetails(lcOrderDetailsBO);

        List<OrderDet> orderDets = orderMst.getOrderDets();
        List<LcOrderItemBO> lcOrderItemBOS = new ArrayList<>();
        for (OrderDet orderDet : orderDets) {
            LcOrderItemBO itemBO = new LcOrderItemBO();
            //TODO  relation \"lc2_c2code_type\" does not exist
           /* String c2CodeTypeName = nonRilService.getC2CodeTypeName(orderMst.getCC2code());
            if (!CustomerNameEnum.RELIANCE.name().equals(c2CodeTypeName)) {*/
            itemBO.setItemName(orderDet.getCBuyerItemName());
                if (!helper.isEmpty(orderDet.getCBuyerItemcode())) {
                    LcItem lcItem = itemService.getByItemId(orderDet.getCBuyerItemcode());
                    if (lcItem!=null) {
                        itemBO.setGstPercentage(lcItem.getGstCode());
                        itemBO.setItemName(lcItem.getItemName());
                    }
                }
                else {
                   /* String itemCode = itemService.getUitemCode(orderMst.getCC2code(),orderDet.getCItemCode());
                    if(!helper.isEmpty(itemCode)) {
                        LcItem lcItem = itemService.getByItemId(itemCode);
                        if (lcItem!=null) {
                            itemBO.setGstPercentage(lcItem.getGstCode());
                            itemBO.setItemName(lcItem.getItemName());
                        }
                    }*/
                   itemBO.setGstPercentage(itemService.getSellerItemGst(orderMst.getCC2code(), orderDet.getCItemCode()));
                }
          //  }
            itemBO.setItemCode(orderDet.getCBuyerItemcode());
            itemBO.setScheme(orderDet.getcScheme());
            itemBO.setSellerItemCode(orderDet.getCItemCode());
            itemBO.setMrp(orderDet.getNMrp());
            itemBO.setQuantity(orderDet.getNQty());
            itemBO.setSchemeQuantity(orderDet.getNSchemeQty());
            itemBO.setDiscountPercentage(orderDet.getNDiscPercentage());
            itemBO.setSaleRate(orderDet.getNSaleRate());
            itemBO.setNetAmount(orderDet.getNItemTotal());
            itemBO.setReason(orderDet.getcCancellationReason());
            lcOrderItemBOS.add(itemBO);
        }
        lcOrder.getOrderDetails().setOrderItems(lcOrderItemBOS);
        //TS related fields
        if(orderMst.getPrescription()!=null){
            lcOrder.getPrescriptionBo().setPrescriptionId(orderMst.getPrescription().getNPrescriptionId().toString());
            lcOrder.getPrescriptionBo().setDoctorName(orderMst.getPrescription().getCDoctorName().toString());
            lcOrder.getPrescriptionBo().setPatientName(orderMst.getPrescription().getCPatientName().toString());
            lcOrder.getPrescriptionBo().setDoctorMobile(orderMst.getPrescription().getcDoctorMobile());
            lcOrder.getPrescriptionBo().setPrescriptionExpiryDate(orderMst.getPrescription().getDPrescriptionExpiryDate());
            List<PrescriptionDoc> prescriptionDocList =  orderMst.getPrescription().getPrescriptionDocs();
            PrescriptionDocsBO prescriptionDocsBO = new PrescriptionDocsBO();
            List<PrescriptionDocsBO> prescriptionDocsBOArrayList = new ArrayList<>();
            if (!prescriptionDocList.isEmpty()){
                for (PrescriptionDoc prescriptionDoc:prescriptionDocList) {
                    prescriptionDocsBO.setPrescriptionDocsUrl(prescriptionDoc.getCUrl());
                    prescriptionDocsBOArrayList.add(prescriptionDocsBO);
                }
            }
            lcOrder.getPrescriptionBo().setPrescriptionDocsBOList(prescriptionDocsBOArrayList);
            List<OrderByPrescriptionEntity> orderByPrescriptionEntityList = orderByPrescriptionRepository.getByOrderId(orderMst.getNOrderNo());
            if(!orderByPrescriptionEntityList.isEmpty()){
                for (OrderByPrescriptionEntity orderByPrescriptionEntity:orderByPrescriptionEntityList) {
                    lcOrder.getTsCustomerDetailsBo().setCustomerId(helper.getString(orderByPrescriptionEntity.getUserId()));
                   // lcOrder.getPrescriptionBo().setPrescriptionValidStatus(orderByPrescriptionEntity.getValidStatus());
                }
            }
        }else{
            lcOrder.getTsCustomerDetailsBo().setCustomerId(helper.getString(orderMst.getNCreatedBy()));
        }
        if (orderMst.getCustomerDetail() != null){
            lcOrder.getTsCustomerDetailsBo().setCustomerName(orderMst.getCustomerDetail().getCCustomerName());
            lcOrder.getTsCustomerDetailsBo().setCustMobile(orderMst.getCustomerDetail().getContactDetail().getCMobileNo());
            lcOrder.getTsCustomerDetailsBo().setPincode(orderMst.getCustomerDetail().getContactDetail().getCPin());
            lcOrder.getTsCustomerDetailsBo().setStateCode(orderMst.getCustomerDetail().getContactDetail().getCState());
        }
        if(orderMst.getDeliveryDetail()!=null){
            lcOrder.getTsCustomerDetailsBo().setDeliveryAddress(orderMst.getDeliveryDetail().getContactDetail().getCAddress1());
            lcOrder.getTsCustomerDetailsBo().setDeliveryId(helper.getString(orderMst.getDeliveryDetail().getNDeliveryId()));
            //lcOrder.getTsCustomerDetailsBo().setDeliveryAddressId(helper.getString(orderMst.getDeliveryDetail().getContactDetail().getNContactId()));
        }
        lcOrder.setDiscountAmount(orderMst.getNDiscountAmount());

        return lcOrder;
    }

    @Transactional(rollbackOn = Exception.class)
    @Override
    public void saveLcOrder(LcOrder lcOrder) throws InputPayloadException {
        if (lcOrder.getSourceRef().startsWith("LO_")) {
            log.debug("From Live Order Application " + lcOrder.getOrderId());
            JsonObject jsonObject = lcOneOrderService.toLc1Order(lcOrder);
            if (jsonObject != null) {
                lcOrder.setSourceRef(jsonObject.get("c_order_id").getAsString());
                lcOrder.setTransNo(jsonObject.get("c_trans_no").getAsString());
                orderMstService.updateLc1Order(jsonObject.get("c_order_id").getAsString(), Long.parseLong(lcOrder.getOrderId()));
            } else {
                throw new InputPayloadException("Order is not populated into MySQL!");
            }
        }  else {
            String tId = lcOneOrderService.getTransId(lcOrder.getSourceRef());
            if (!helper.isEmpty(tId))
                lcOrder.setTransNo(tId);
        }
        lcOrderService.saveLoOrder(lcOrder);
    }
    @Override
    public void notifyFailedProcessOrder(String payload) {

        OrderMst bo = helper.fromJson(payload, OrderMst.class);
        EmailBO emailBO = new EmailBO();
        ToListBO toListBO = new ToListBO();
        toListBO.setTo(Arrays.asList(recipients.split(",")));

        String subject = "Unable to process this order, csquare order no : " + bo.getNOrderNo() +
                ", reference order number : "+bo.getCSourceRefNo() +
                " source timestamp : " + bo.getTSourceTimestamp().toString();
        emailBO.setSubject(subject);

        subject +="<br> <br>" + payload;
        emailBO.setContent(subject);

        String requestBody = helper.toJson(emailBO);
        Map<String, String> headers = new HashMap<>();
        headers.put("x-csquare-user-id", "1");

        String response = apiCallService.postApiCall(apiUrl, requestBody, headers);
        log.error("Unable to process the order sending failed notification via email for :"+bo.getNOrderNo());
    }

    @Override
    public void updateOrderStatus(OrderMst orderMst) {
        JsonObject payload=new JsonObject();
        JsonObject data=new JsonObject();
        data.addProperty("c_br_code","0000");
        data.addProperty("c_year","00");
        data.addProperty("c_prefix","0");
        data.addProperty("n_srno",0);
        data.addProperty("c_order_id", orderMst.getCSourceRefNo());
        data.addProperty("c_order_status","OP");
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");
        data.addProperty("d_date_time", orderMst.getTOrderCreatedTimestamp().format(format));
        data.addProperty("c_return_message","From LC 2.0");
        payload.addProperty("c2_code",orderMst.getCC2code());
        payload.addProperty("br_code",orderMst.getCBrCode());
        payload.add("data",data);
        apiCallService.postApiCall(statusUpdateUrl, helper.toJson(payload));
    }

    @Override
    public void sendSellerCancellationOnError(String payload) throws RecordNotFoundException {
        OrderMst bo = helper.fromJson(payload, OrderMst.class);
        NmOrderStatusPushBO nmOrderStatusStatusBO = getNmOrderStatusPushBO(bo);
        OrderStatusMapping orderStatusMapping = getOrderStatusMapping(bo);
        String details = ordersDistributorProducerService.pushOrderDetails(nmOrderStatusStatusBO, orderStatusMapping);
        orderTransactionLedgerService.save(bo.getNOrderNo(), OrderStatusEnum.CS.name(),
                "Error in application order processing!", details);
    }

    @Override
    public OrderMst fetchByRefNo(String orderId) throws RecordNotFoundException {
        return orderMstService.fetchByRefNo(orderId);
    }

    @Override
    public void toLc1Order(LcOrder lcOrder) {
            lcOneOrderService.toLc1Order(lcOrder);
    }

    private OrderStatusMapping getOrderStatusMapping(OrderMst bo) throws RecordNotFoundException {
        return orderStatusMappingService.fetchByC2codeAndCustCode(bo.getCC2code(), bo.getCCustCode(), "S");
    }

    private NmOrderStatusPushBO getNmOrderStatusPushBO(OrderMst bo) {
        NmOrderStatusPushBO nm = new NmOrderStatusPushBO();
        nm.setReason("Error processing the order!");
        nm.setOrderStatus("CANCELLED");
        nm.setSubOrderId(bo.getCSourceRefNo());
        nm.setLastUpdatedTime(helper.getCurrentTimeString());
        return nm;
    }

    @Override
    public int orderOP(Map<String, String> headers, ApiResponse apiResponse, String c2Code, String status, String uri) {
        int size = 100;
        int page = 0;
        int count = 0;
        int total = 0;
        while (size > count) {
            List<OrderMst> list = orderMstService.getOrdersByStatus(c2Code, status, page++, size);
            count = list.size();
            for (OrderMst orderMst : list) {
                distributeOrder(orderMst, headers, apiResponse, uri);
            }
            total += count;
        }
        log.debug("Total records distributed : {}", total);
        return total;
    }

    @Override
    public OrderMst updateOrder(Long userId, OrderMst mst) {
        orderTransactionLedgerService.save(mst.getNOrderNo(), mst.getCOrderStatus());
        return orderMstService.save(mst);
    }

    @Override
    public OrderMst createTSOrder(Long userId, OrderMst mst) {
        createOrderDet(userId, helper.getCurrentTime(), mst);
        orderTransactionLedgerService.save(mst.getNOrderNo(), mst.getCOrderStatus());
        return orderMstService.save(mst);
    }

    @Transactional(rollbackOn = Exception.class)
    @Override
    public void createLoOrder(Long userId, OrderMst bo, LcHeaderBO lcHeaderBO) throws RecordNotFoundException, InputPayloadException {
        OrderMst orderMst = createOrder(userId, bo);
        LcOrder lcOrder = getLcOrder(userId,orderMst);

        if (lcHeaderBO.getType().equals("C")) {
            lcOrder.setOrderFrom("TS");
        }

        saveLcOrder(lcOrder);

        if("700000".equals(orderMst.getCC2code()))
            updateOrderStatus(bo);
    }
}
