package com.csquare.lc.ms.orders.kafka.controller;

import com.csquare.lc.ms.orders.kafka.service.interfaces.ItemService;

import com.csquare.lc.ms.orders.lib.model.mongo.master.LcItem;
import com.csquare.ms.lib.api.ApiResponse;
import com.csquare.ms.lib.controller.BaseController;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(path = {"/po/k/it", "/lc/ms/it"})
public class ItemController extends BaseController {

    @Autowired
    private ItemService service;
    @PostMapping(path = "/createItem", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ApiResponse> createItem(@RequestHeader Map<String, String> headers, @RequestBody String payload) {
        ApiResponse apiResponse = this.initializeResponse("/lc/ms/it/createItem->" + headers.toString() + ":" + payload);
        try {
            Long userId = this.getUserId(headers);
            LcItem bo = helper.fromJSON(payload, LcItem.class);
            service.create(bo);
            JsonObject ret = helper.toJsonObjectTree(bo, LcItem.class);

            this.setDataJsonObjectPayload(apiResponse, ret);
        } catch (Exception e) {
            this.handleAppExceptions(e, apiResponse);
        }
        return this.getResponseEntity(apiResponse);
    }
    @GetMapping(path = "/getByItemId/{itemId}")
    public ResponseEntity<ApiResponse> getByItemId(@RequestHeader Map<String, String> headers, @PathVariable String itemId) {
        ApiResponse apiResponse = this.initializeResponse("/lc/ms/it/getByItemId->");
        try {
            Long userId = this.getUserId(headers);
            System.out.println(itemId);
            LcItem item=service.getByItemId(itemId);
            System.out.println("hifs"+item);
            JsonObject ret = helper.toJsonObjectTree(item, LcItem.class);

            this.setDataJsonObjectPayload(apiResponse, ret);
        } catch (Exception e) {
            this.handleAppExceptions(e, apiResponse);
        }
        return this.getResponseEntity(apiResponse);
    }
    @GetMapping(path = "/searchByName/{name}")
    public ResponseEntity<ApiResponse> searchByName(@RequestHeader Map<String, String> headers, @PathVariable String name) {
        ApiResponse apiResponse = this.initializeResponse("/lc/ms/it/searchByName->" + headers.toString());
        try {
            Long userId = this.getUserId(headers);
            List<LcItem> item=service.searchByName(name);

            JsonArray ret = helper.toJsonArrayTree(item, new TypeToken<List<LcItem>>() {}.getType());
            this.setDataJsonArrayPayload(apiResponse, ret);

           // this.setDataJsonObjectPayload(apiResponse, helper.toJsonArrayTree());
        } catch (Exception e) {
            this.handleAppExceptions(e, apiResponse);
        }
        return this.getResponseEntity(apiResponse);
    }
}
