package com.csquare.lc.ms.orders.kafka.service.interfaces;

import com.csquare.lc.ms.orders.lib.model.mongo.OffersByDistributors;
import com.csquare.lc.ms.orders.lib.model.mongo.PreferredSellers;
import com.google.gson.JsonObject;
import com.microsoft.azure.storage.StorageException;

import java.io.IOException;
import java.net.URISyntaxException;

public interface FileUploadService {

    String upload(JsonObject data) throws URISyntaxException, StorageException, IOException;

    PreferredSellers save(PreferredSellers sellers);

    PreferredSellers getByC2Code(String c2Code);

    OffersByDistributors saveOffers(OffersByDistributors distributors);

    OffersByDistributors getOffersByC2Code(String c2Code);

}
