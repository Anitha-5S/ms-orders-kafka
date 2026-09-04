package com.csquare.lc.ms.orders.kafka.controller;

import com.csquare.lc.ms.orders.kafka.service.interfaces.FileUploadService;
import com.csquare.lc.ms.orders.lib.bo.ImageUrlBo;
import com.csquare.lc.ms.orders.lib.bo.SolutionImageBo;
import com.csquare.lc.ms.orders.lib.model.mongo.OffersByDistributors;
import com.csquare.lc.ms.orders.lib.model.mongo.PreferredSellers;
import com.csquare.lc.ms.orders.lib.model.mongo.Solution;
import com.csquare.lc.ms.orders.lib.model.mongo.SolutionHelpful;
import com.csquare.ms.lib.api.ApiResponse;
import com.csquare.ms.lib.bo.KeyValue;
import com.csquare.ms.lib.controller.BaseController;
import com.csquare.ms.lib.utils.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.microsoft.azure.storage.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(path = {"/po/k/im", "/lc/ms/im"})
public class ImageResizeController extends BaseController {

    @Autowired
    FileUploadService fileUploadService;

    public String resize(int width,int height,ImageUrlBo req) {
        String result=null;
        try {

            File convFile = new File(req.getUrl());
            String fileName = convFile.getName().replaceFirst("[.][^.]+$", "");
            Image  img = ImageIO.read(convFile);
            BufferedImage  tempJPG = resizeImage(img, width, height);
            JsonObject upload=new JsonObject();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(tempJPG, "jpg", baos);
            byte[] bytes = baos.toByteArray();
            upload.addProperty("fTPFolderName",req.getFolderName());
            upload.addProperty("fTPFileName",fileName+width+"_"+height+".JPG");
            String s = Base64.getMimeEncoder().encodeToString(bytes);
            upload.addProperty("fTPFileData",s);
             result = fileUploadService.upload(upload);
             System.out.println(result);

        } catch (IOException | URISyntaxException | StorageException ioException) {
            ioException.printStackTrace();
        }
        assert result != null;
        return result;
    }

    public static BufferedImage resizeImage(final Image image, int width, int height) {
        final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setComposite(AlphaComposite.Src);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(image, 0, 0, width, height, null);
        graphics2D.dispose();
        return bufferedImage;
    }
    @PostMapping(value = "/preferredSellers", produces = Constants.APPLICATION_JSON_CHARSET_UTF_8)
    public ResponseEntity<?> preferredSellers(@RequestHeader Map<String, String> headers, @RequestBody Object payload) {
        ApiResponse apiResponse = this.initializeResponse("/lc/ms/mst/l/preferredSellers");
        try {
            String c2Code = headers.get("x-csquare-c2code");
            PreferredSellers sellers = helper.fromJson(helper.toJson(payload), PreferredSellers.class);
            ImageUrlBo imageUrlBo=new ImageUrlBo();
            PreferredSellers sellers1=new PreferredSellers();
            List<String> urls = new ArrayList<>();
            for (int i=0;i<sellers.getImageList().size();i++) {
                imageUrlBo.setUrl(sellers.getImageList().get(i));
                imageUrlBo.setFolderName("preferred");
                String url=resize(200,200,imageUrlBo);
                System.out.println("url=======> "+url);
                urls.add(url);
            }
            sellers1.setC2code(c2Code);
            sellers1.setImageList(urls);
            PreferredSellers sellers2=fileUploadService.save(sellers1);

            JsonObject ret = helper.toJsonObjectTree(sellers2, new TypeToken<PreferredSellers>() {}.getType());
            this.setDataJsonObjectPayload(apiResponse, ret);
        } catch (Exception e) {
            this.handleAppExceptions(e, apiResponse);
        }
        return this.getResponseEntity(apiResponse);
    }
    @GetMapping(value = "/preferredSellers", produces = Constants.APPLICATION_JSON_CHARSET_UTF_8)
    public ResponseEntity<?> getpreferredSellers(@RequestHeader Map<String, String> headers) {
        ApiResponse apiResponse = this.initializeResponse("/lc/ms/im/preferredSellers");
        try {
            String c2Code = headers.get("x-csquare-c2code");
            PreferredSellers sellers= fileUploadService.getByC2Code(c2Code);
            JsonObject ret = helper.toJsonObjectTree(sellers, new TypeToken<PreferredSellers>() {}.getType());
            this.setDataJsonObjectPayload(apiResponse, ret);
        } catch (Exception e) {
            this.handleAppExceptions(e, apiResponse);
        }
        return this.getResponseEntity(apiResponse);
    }
    @PostMapping(value = "/offersDistributors", produces = Constants.APPLICATION_JSON_CHARSET_UTF_8)
    public ResponseEntity<?> offersDistributors(@RequestHeader Map<String, String> headers, @RequestBody Object payload) {
        ApiResponse apiResponse = this.initializeResponse("/lc/ms/im/offersDistributors");
        try {
            String c2Code = headers.get("x-csquare-c2code");
            OffersByDistributors distributors = helper.fromJson(helper.toJson(payload), OffersByDistributors.class);
            ImageUrlBo imageUrlBo=new ImageUrlBo();
            OffersByDistributors distributors1=new OffersByDistributors();
            List<String> urls = new ArrayList<>();
            for (int i=0;i<distributors.getImageList().size();i++) {
                imageUrlBo.setUrl(distributors.getImageList().get(i));
                imageUrlBo.setFolderName("offersDistributors");
                String url=resize(200,200,imageUrlBo);
                System.out.println("url=======> "+url);
                urls.add(url);
            }
            distributors1.setC2code(c2Code);
            distributors1.setImageList(urls);
            OffersByDistributors distributors2=fileUploadService.saveOffers(distributors1);

            JsonObject ret = helper.toJsonObjectTree(distributors2, new TypeToken<OffersByDistributors>() {}.getType());
            this.setDataJsonObjectPayload(apiResponse, ret);
        } catch (Exception e) {
            this.handleAppExceptions(e, apiResponse);
        }
        return this.getResponseEntity(apiResponse);
    }
    @GetMapping(value = "/offersDistributors", produces = Constants.APPLICATION_JSON_CHARSET_UTF_8)
    public ResponseEntity<?> getOffersDistributors(@RequestHeader Map<String, String> headers) {
        ApiResponse apiResponse = this.initializeResponse("/lc/ms/im/offersDistributors");
        try {
            String c2Code = headers.get("x-csquare-c2code");
            OffersByDistributors distributors= fileUploadService.getOffersByC2Code(c2Code);
            JsonObject ret = helper.toJsonObjectTree(distributors, new TypeToken<OffersByDistributors>() {}.getType());
            this.setDataJsonObjectPayload(apiResponse, ret);
        } catch (Exception e) {
            this.handleAppExceptions(e, apiResponse);
        }
        return this.getResponseEntity(apiResponse);
    }
}
