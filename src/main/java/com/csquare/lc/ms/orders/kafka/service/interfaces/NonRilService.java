package com.csquare.lc.ms.orders.kafka.service.interfaces;

public interface NonRilService {

    void saveLcStoreSyncDetails(String c2Code, String url);

    String getC2CodeTypeName(String c2code);
}
