package com.csquare.lc.ms.orders.kafka.service;

import com.csquare.lc.ms.orders.kafka.service.interfaces.FileUploadService;
import com.csquare.lc.ms.orders.lib.model.mongo.OffersByDistributors;
import com.csquare.lc.ms.orders.lib.model.mongo.PreferredSellers;
import com.csquare.lc.ms.orders.lib.repos.mongo.OffersByDistributorsImpl;
import com.csquare.lc.ms.orders.lib.repos.mongo.PreferredSellersImpl;
import com.google.gson.JsonObject;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

@Service
@Slf4j
public class FileUploadServiceImpl extends BlobBaseServicesImpl implements FileUploadService {

    @Autowired private CloudBlobContainer cloudBlobContainer;

    @Autowired
    PreferredSellersImpl preferredSellers;

    @Autowired
    OffersByDistributorsImpl offersImpl;
    @Override
    public String upload(JsonObject data)
            throws URISyntaxException, StorageException, IOException {
        String fTPFolderName = helper.getNullableString(data.get("fTPFolderName"));
        String fTPFileName = helper.getString(data.get("fTPFileName"));
        String fTPFileData = helper.getString(data.get("fTPFileData"));

        String path = "";
           if (helper.isEmpty(fTPFolderName)) {
            path = createFolderNameIfNotExist(fTPFolderName) + "/" + fTPFileName;
        } else {
             path = fTPFolderName + "/" +  fTPFileName;
        }

        byte[] imageData = Base64.getMimeDecoder().decode(fTPFileData);
        CloudBlockBlob blob = cloudBlobContainer.getBlockBlobReference(path);
        System.out.println(blob.getContainer().getName());
        blob.uploadFromByteArray(imageData, 0, imageData.length);
        URI uri = blob.getUri();

        return uri.toString();
    }

    @Override
    public PreferredSellers save(PreferredSellers sellers) {
        return preferredSellers.save(sellers);
    }

    @Override
    public PreferredSellers getByC2Code(String c2Code) {
        return preferredSellers.getByC2Code(c2Code);
    }

    @Override
    public OffersByDistributors saveOffers(OffersByDistributors distributors) {
        return offersImpl.save(distributors);
    }

    @Override
    public OffersByDistributors getOffersByC2Code(String c2Code) {
        return offersImpl.getByC2Code(c2Code);
    }

}
