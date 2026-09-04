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
public class OrderHistoryFilterBo {

    @Field("c_from_date")
    @SerializedName("c_from_date")
    private String fromDate;

    @Field("c_to_date")
    @SerializedName("c_to_date")
    private String toDate;

    @Field("c_payment_status")
    @SerializedName("c_payment_status")
    private String paymentStatus;

    @Field("j_seller_code")
    @SerializedName("j_seller_code")
    private List<String> sellerCodes;

}
