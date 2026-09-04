package com.csquare.lc.ms.orders.kafka.transaction.interfaces;

public interface NonRilTransaction {

    void saveLcStoreSyncDetails(String c2Code, String url);

    String getC2CodeTypeName(String c2code);
}
