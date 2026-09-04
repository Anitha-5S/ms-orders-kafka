package com.csquare.lc.ms.orders.kafka.bo;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DownloadLink {
    @Field("c_excel_link")
    @SerializedName("c_excel_link")
    public String excelLink;
    @Field("c_pdf_link")
    @SerializedName("c_pdf_link")
    public String pdfLink;
    @Field("c_csv_link")
    @SerializedName("c_csv_link")
    public String csvLink;
}
