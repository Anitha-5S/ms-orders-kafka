package com.csquare.lc.ms.orders.kafka.service;

import com.csquare.lc.ms.orders.kafka.service.interfaces.ItemService;
import com.csquare.lc.ms.orders.lib.model.mongo.master.LcItem;
import com.csquare.lc.ms.orders.lib.repos.mongo.master.ItemImpl;
import com.csquare.ms.lib.exceptions.RecordNotFoundException;
import com.csquare.ms.lib.services.BaseDBServiceImpl;
import com.csquare.ms.lib.services.BaseServicesImpl;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Slf4j
@Service
public class ItemServiceImpl extends BaseDBServiceImpl implements ItemService {


    @Value("${seller.uitem.url}")
    private String sellerUitemUrl;
    
    @Value("${seller.logo.url}")
    private String sellerLogoUrl;

    @Autowired
    private ItemImpl itemImpl;

    @PersistenceContext(unitName = "mysql")
    @Autowired
    private EntityManager mysqlEntityManager;

    @Override
    public LcItem create(LcItem item) {
        return itemImpl.save(item);
    }

    @Override
    public LcItem getByItemId(String itemId) throws RecordNotFoundException {
        return itemImpl.getById(itemId);
    }

    @Override
    public List<LcItem> searchByName(String itemName) {
        return itemImpl.searchByName(itemName);
    }

    @Override
    public JsonObject getSellerLogo(String sellerCode) {
        JsonObject data = null;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("c_seller_code",sellerCode);
        String response =  this.callWebClientPostSyncApi(sellerLogoUrl,jsonObject.toString());
        log.info(response);
        if(response!= null){
        JsonObject responseObject = helper.getJsonObject(response);
        if (responseObject.has("appStatusCode") && responseObject.get("appStatusCode").getAsInt() == 0) {
            JsonObject payload = responseObject.get("payloadJson").getAsJsonObject();
            data = payload.getAsJsonObject("data");
        }}
        return data;
    }

    @Override
    public String getUitemCode(String sellerCode, String itemCode) {
        JsonObject data = null;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("c_seller_code",sellerCode);
        jsonObject.addProperty("c_seller_item_code",sellerCode);
        String response =  this.callWebClientPostSyncApi(sellerUitemUrl,jsonObject.toString());
        if(response!= null){
            JsonObject responseObject = helper.getJsonObject(response);
            if (responseObject.has("appStatusCode") && responseObject.get("appStatusCode").getAsInt() == 0) {
                JsonObject payload = responseObject.get("payloadJson").getAsJsonObject();
                data = payload.getAsJsonObject("data");
                if (data.has("c_item_code"))
                    return data.get("c_item_code").getAsString();
            }}
        return "";
    }


    @Override
    public String getSellerItemGst(String sellerCode, String sellerItemCode) {

        String sql = " SELECT  " +
                "  cim.c_gst  " +
                "from  " +
                "  cust_item_mst cim  " +
                "where  " +
                "  cim.c_c2code = : sellerCode  " +
                "  and cim.c_code = : sellerItemCode  ";
        Query query = mysqlEntityManager.createNativeQuery(sql);
        return this.getSingleResultNull(query) ;
    }

}