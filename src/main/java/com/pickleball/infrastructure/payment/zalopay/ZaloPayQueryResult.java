package com.pickleball.infrastructure.payment.zalopay;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ZaloPayQueryResult {
    private String appTransId;
    private Integer returnCode;
    private String returnMessage;
    private Integer subReturnCode;
    private String subReturnMessage;
    private boolean isProcessing;
    private Long amount;
    private Long zpTransId;
    private String rawResponse;

    public boolean isPaid() {
        return returnCode != null && returnCode == 1;
    }
}
