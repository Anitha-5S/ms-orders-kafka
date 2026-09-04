package com.csquare.lc.ms.orders.kafka.service;

import com.csquare.lc.ms.orders.kafka.service.interfaces.LcOneOrderService;
import com.csquare.lc.ms.orders.lib.bo.LcOrderItemBO;
import com.csquare.lc.ms.orders.lib.model.mongo.order.LcOrder;
import com.csquare.ms.lib.services.BaseDBServiceImpl;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Slf4j
@Service
public class LcOneOrderServiceImpl extends BaseDBServiceImpl implements LcOneOrderService {

    @PersistenceContext(unitName = "mysql")
    @Autowired
    private EntityManager mysqlEntityManager;
    @Value("${order.lo.lc}")
    private String loToLc;

    @Override
    //@Transactional(rollbackOn = Exception.class)
    public JsonObject toLc1Order(LcOrder lcOrder)  {
        JsonObject data = null;
        String order = "";
        JsonObject jsonObject = helper.toJsonObjectTree(lcOrder, LcOrder.class);
        String response = this.callWebClientPostSyncApi(loToLc,jsonObject.toString());
        log.info(response);
        JsonObject responseObject = helper.getJsonObject(response);
        if (responseObject.has("appStatusCode") && responseObject.get("appStatusCode").getAsInt() == 0) {
            JsonObject payload = responseObject.get("payloadJson").getAsJsonObject();
            data = payload.getAsJsonObject("data");
        }
        return data;
       /* try{

        String inQ = "INSERT INTO cust_order_mst ( c_c2code, c_year, c_prefix," +
                " c_userid, d_upload_datetime, c_device_id, n_delivery_pickup_flag," +
                " n_order_status, n_device_type, n_amount, n_discount, c_modified_user," +
                " c_cust_code, c_created_user, c_order_no, n_ref_srno ) " +
                "VALUES (:c_c2code,:c_year, :c_prefix, :c_userid," +
                ":d_upload_datetime, :c_device_id, :n_delivery_pickup_flag, :n_order_status, " +
                ":n_device_type, :n_amount, :n_discount, :c_modified_user," +
                ":c_cust_code, :c_created_user, :c_order_no, :n_ref_srno)";
        System.out.println(inQ);
        Query query = mysqlEntityManager.createNativeQuery(inQ);
            query.setParameter("c_c2code",lcOrder.getOrderSummary().getSellerCode());
            query.setParameter("c_year",helper.getCurrentYear());
            query.setParameter("c_prefix","LO");
            // query.setParameter("c_userid",lcOrder.getUserId());
            query.setParameter("d_upload_datetime", helper.getCurrentTime());
            query.setParameter("c_device_id", "Live Order");
            query.setParameter("n_delivery_pickup_flag", 1);
            query.setParameter("n_order_status", 4);
            query.setParameter("n_device_type", 21);
            query.setParameter("n_amount", lcOrder.getOrderDetails().getAmountPaid());
            query.setParameter("n_discount", lcOrder.getOrderDetails().getCashDiscount());
            query.setParameter("c_modified_user",lcOrder.getUserId());
            query.setParameter("c_cust_code",lcOrder.getCustCode());
            query.setParameter("c_created_user",lcOrder.getUserId());
            query.setParameter("c_order_no",lcOrder.getOrderId());
            query.setParameter("c_userid",lcOrder.getOrderId());
            query.setParameter("n_ref_srno",lcOrder.getOrderId());
        query.executeUpdate();
        Query custTrans = mysqlEntityManager.createNativeQuery(this.getInsertQuery("cust_transaction_mst",getCustTransMst(lcOrder)));
        custTrans.setParameter("c_c2code", lcOrder.getOrderSummary().getSellerCode());
            custTrans.setParameter("n_bill_amount",lcOrder.getOrderDetails().getAmountPaid() );
            custTrans.setParameter("n_discount_amount",lcOrder.getOrderDetails().getCashDiscount() );
            custTrans.setParameter("n_order_or_invoice_flag", 1);
            custTrans.setParameter("n_order_status", 4);
            custTrans.setParameter("n_cancel_flag", 0);
            custTrans.setParameter("n_user_type_flag", 1);
            custTrans.setParameter("c_created_user", lcOrder.getUserId());
            custTrans.setParameter("c_modify_user", lcOrder.getUserId());
            custTrans.setParameter("d_created_datetime", helper.getCurrentTime());
            custTrans.setParameter("d_modified_datetime",helper.getCurrentTime() );
            custTrans.setParameter("n_ord_srno",lcOrder.getOrderId() );
            custTrans.setParameter("c_device_type",21 );
            custTrans.setParameter("n_type",1 );
            custTrans.setParameter("c_item_hold_flag",0 );
            custTrans.setParameter("c_cust_code",lcOrder.getCustCode() );
            custTrans.setParameter("n_promo_discount", 0);
            custTrans.setParameter("n_delivery_charge",0 );
            custTrans.executeUpdate();
        for(int i =0; i < lcOrder.getOrderDetails().getOrderItems().size(); i++){
            Query custDet = mysqlEntityManager.createNativeQuery(this.getInsertQuery("cust_transaction_det",getCustTransDet(lcOrder, i)));
            custDet.setParameter("c_c2code",lcOrder.getOrderSummary().getSellerCode());
            custDet.setParameter("n_tran_mst_srno",getTransId(getLcOneOrderId(lcOrder.getOrderId())));
            custDet.setParameter("n_prec_det_rowid",lcOrder.getOrderSummary().getNoOfLineItems());
            LcOrderItemBO itemBO = lcOrder.getOrderDetails().getOrderItems().get(i);
            custDet.setParameter("c_item_code",itemBO.getSellerItemCode());
            custDet.setParameter("n_qty",itemBO.getQuantity());
            custDet.setParameter("n_disc_per",itemBO.getDiscountPercentage());
            custDet.setParameter("n_rate",itemBO.getSaleRate());
            custDet.setParameter("n_value",itemBO.getNetAmount());
            custDet.setParameter("c_deviceid","Live order");
            custDet.setParameter("n_device_type",21);
            custDet.setParameter("n_rate_consider_flag",0);
            custDet.setParameter("n_scheme_qty",itemBO.getSchemeQuantity());
            custDet.setParameter("n_actual_rate",itemBO.getMrp());
            custDet.setParameter("c_buyer_itemcode",itemBO.getItemCode());
            custDet.setParameter("c_buyer_item_name",itemBO.getItemName());
        }}
        catch (Exception e){
            System.out.println(e);
            e.printStackTrace();
        }*/
    }

    private String custMstQuery(LcOrder lcOrder){
        return "INSERT INTO cust_order_mst ( c_c2code, c_year, c_prefix," +
                " c_userid, d_upload_datetime, c_device_id, n_delivery_pickup_flag," +
                " n_order_status, n_device_type, n_amount, n_discount, c_modified_user," +
                " c_cust_code, c_created_user, c_order_no, n_ref_srno ) " +
                "VALUES ( "+lcOrder.getOrderSummary().getSellerCode()+", "+
                helper.getCurrentYear()+", LO, "+
                lcOrder.getUserId()+", "+
                String.valueOf(lcOrder.getOrderSummary().getOrderDate()) +", "+
                "Live Order,1 , 4, 21," + lcOrder.getOrderDetails().getAmountPaid()+ ", "+
                lcOrder.getOrderDetails().getCashDiscount()+", "+
                lcOrder.getUserId()+ ", "+
                lcOrder.getCustCode()+", "+
                lcOrder.getUserId()+", "+
                lcOrder.getOrderId()+", "+
                Integer.parseInt(lcOrder.getOrderId()) +")";
    }
    private JsonObject getCustOrderMst(LcOrder lcOrder){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("c_c2code",lcOrder.getOrderSummary().getSellerCode());
        jsonObject.addProperty("c_year",helper.getCurrentYear());
        jsonObject.addProperty("c_prefix","LO");
        jsonObject.addProperty("c_userid",lcOrder.getUserId());
        jsonObject.addProperty("d_upload_datetime", String.valueOf(lcOrder.getOrderSummary().getOrderDate()));
        jsonObject.addProperty("c_device_id","Live Order");
        jsonObject.addProperty("n_delivery_pickup_flag",1);
        jsonObject.addProperty("n_order_status",4);
        jsonObject.addProperty("n_device_type",21);
        jsonObject.addProperty("n_amount",lcOrder.getOrderDetails().getAmountPaid());
        jsonObject.addProperty("n_discount",lcOrder.getOrderDetails().getCashDiscount());
        jsonObject.addProperty("c_modified_user",lcOrder.getUserId());
        jsonObject.addProperty("c_cust_code",lcOrder.getCustCode());
        jsonObject.addProperty("c_created_user",lcOrder.getUserId());
        jsonObject.addProperty("c_order_no",lcOrder.getOrderId());
        jsonObject.addProperty("n_ref_srno",Integer.parseInt(lcOrder.getOrderId()));
        return jsonObject;
    }
    private int getLcOneOrderId(String loOrderId){
        int orderId =0;
        Query query = mysqlEntityManager.createNativeQuery(
                "SELECT com.n_srno FROM cust_order_mst com WHERE com.c_order_no = :orderId");
        query.setParameter("orderId",loOrderId);
        Object obj = null;
                //this.getSingleResult(query);
        if (obj != null){
            orderId = (Integer) obj;
        }
        return orderId;
    }

    @Override
    public String getTransId(String lcOneId) {

        Query query = mysqlEntityManager.createNativeQuery(
                "SELECT ctm.n_srno FROM cust_transaction_mst ctm, cust_order_mst com" +
                        " WHERE ctm.n_ord_srno = com.n_srno AND com.c_order_no = :orderId");
        query.setParameter("orderId", lcOneId);
        return this.getSingleResultNull(query);

    }
    private JsonObject getCustTransMst(LcOrder lcOrder){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("c_c2code",lcOrder.getOrderSummary().getSellerCode());
        jsonObject.addProperty("n_bill_amount",lcOrder.getOrderDetails().getAmountPaid());
        jsonObject.addProperty("n_discount_amount",lcOrder.getOrderDetails().getCashDiscount());
        jsonObject.addProperty("n_order_or_invoice_flag",1);
        jsonObject.addProperty("n_order_status",4);
        jsonObject.addProperty("n_cancel_flag",0);
        jsonObject.addProperty("n_user_type_flag",1);
        jsonObject.addProperty("c_created_user",lcOrder.getUserId());
        jsonObject.addProperty("c_modify_user",lcOrder.getUserId());
        jsonObject.addProperty("d_created_datetime", String.valueOf(lcOrder.getOrderSummary().getOrderDate()));
        jsonObject.addProperty("d_modified_datetime",String.valueOf(lcOrder.getOrderSummary().getOrderDate()));
        jsonObject.addProperty("n_ord_srno",getLcOneOrderId(lcOrder.getOrderId()));
        jsonObject.addProperty("c_device_type",21);
        jsonObject.addProperty("n_type",1);
        jsonObject.addProperty("c_item_hold_flag",0);
        jsonObject.addProperty("c_cust_code",lcOrder.getCustCode());
        jsonObject.addProperty("n_promo_discount",0);
        //n_delivery_charge
        jsonObject.addProperty("n_delivery_charge",0);


        return jsonObject;
    }
/*
    private JsonObject getCustTransDet(LcOrder lcOrder, int i){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("c_c2code",lcOrder.getOrderSummary().getSellerCode());
        jsonObject.addProperty("n_tran_mst_srno",getTransId(getLcOneOrderId(lcOrder.getOrderId())));
        jsonObject.addProperty("n_prec_det_rowid",lcOrder.getOrderSummary().getNoOfLineItems());
        LcOrderItemBO itemBO = lcOrder.getOrderDetails().getOrderItems().get(i);
        jsonObject.addProperty("c_item_code",itemBO.getSellerItemCode());
        jsonObject.addProperty("n_qty",itemBO.getQuantity());
        jsonObject.addProperty("n_disc_per",itemBO.getDiscountPercentage());
        jsonObject.addProperty("n_rate",itemBO.getSaleRate());
        jsonObject.addProperty("n_value",itemBO.getNetAmount());
        jsonObject.addProperty("c_deviceid","Live order");
        jsonObject.addProperty("n_device_type",21);
        jsonObject.addProperty("n_rate_consider_flag",0);
        jsonObject.addProperty("n_scheme_qty",itemBO.getSchemeQuantity());
        jsonObject.addProperty("n_actual_rate",itemBO.getMrp());
        jsonObject.addProperty("c_buyer_itemcode",itemBO.getItemCode());
        jsonObject.addProperty("c_buyer_item_name",itemBO.getItemName());
       // jsonObject.addProperty("n_delivery_charge",0);



        return jsonObject;
    }
*/

}
