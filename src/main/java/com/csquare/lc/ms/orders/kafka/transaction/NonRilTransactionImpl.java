package com.csquare.lc.ms.orders.kafka.transaction;

import com.csquare.lc.ms.orders.kafka.service.interfaces.NonRilService;
import com.csquare.lc.ms.orders.kafka.transaction.interfaces.NonRilTransaction;
import com.csquare.ms.lib.transactions.BaseTransactionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class NonRilTransactionImpl extends BaseTransactionImpl implements NonRilTransaction {

    @Autowired
    private NonRilService nonRilService;

    @Override
    public void saveLcStoreSyncDetails(String c2Code, String url){
        nonRilService.saveLcStoreSyncDetails(c2Code,url);
    }

    @Override
    public String getC2CodeTypeName(String c2code) {
        return nonRilService.getC2CodeTypeName(c2code);
    }

}
