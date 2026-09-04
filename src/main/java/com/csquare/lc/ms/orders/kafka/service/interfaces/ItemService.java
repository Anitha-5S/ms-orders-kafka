package com.csquare.lc.ms.orders.kafka.service.interfaces;

import com.csquare.lc.ms.orders.lib.model.mongo.master.LcItem;
import com.csquare.ms.lib.exceptions.RecordNotFoundException;
import com.google.gson.JsonObject;

import java.util.List;

public interface ItemService {

    LcItem create(LcItem item);

    LcItem getByItemId(String itemId) throws RecordNotFoundException;

    List<LcItem> searchByName(String itemName);

    JsonObject getSellerLogo(String sellerCode);

    String getUitemCode(String sellerCode, String itemCode);

    String getSellerItemGst(String sellerCode, String sellerItemCode);

}
