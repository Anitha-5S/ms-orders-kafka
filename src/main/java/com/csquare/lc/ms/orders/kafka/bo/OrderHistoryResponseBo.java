package com.csquare.lc.ms.orders.kafka.bo;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderHistoryResponseBo {

    @Field("c_seller_logo_image")
    @SerializedName("c_seller_logo_image")
    public String sellerLogo;
    @Field("c_seller_Firm_name")
    @SerializedName("c_seller_Firm_name")
    public String sellerName;
    @Field("c_order_id")
    @SerializedName("c_order_id")
    public String orderId;
    @Field("c_ordered_date")
    @SerializedName("c_ordered_date")
    public String orderedDate;
    @Field("c_no_of_items_ordered")
    @SerializedName("c_no_of_items_ordered")
    public String noOfItems;
    @Field("c_outstanding_amount")
    @SerializedName("c_outstanding_amount")
    public String outstandingAmount;
    @Field("c_order_status")
    @SerializedName("c_order_status")
    public String orderStatus;
    @Field("c_delivery_location")
    @SerializedName("c_delivery_location")
    public String deliveryLocation;
    @Field("c_total_order_amount")
    @SerializedName("c_total_order_amount")
    public String totalOrderAmount;
    @Field("c_payment_status")
    @SerializedName("c_payment_status")
    public String paymentStatus;
    @Field("j_download_link")
    @SerializedName("j_download_link")
    public List<DownloadLink> downloadLink;

}
