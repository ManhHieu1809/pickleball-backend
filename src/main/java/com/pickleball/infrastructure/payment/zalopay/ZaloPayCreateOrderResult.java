package com.pickleball.infrastructure.payment.zalopay;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ZaloPayCreateOrderResult {
    private String appTransId;
    private Integer returnCode;
    private String returnMessage;
    private Integer subReturnCode;
    private String subReturnMessage;
    private String orderUrl;
    private String orderToken;
    private String zpTransToken;
    private String qrCode;
    private String rawResponse;

    public boolean isAccepted() {
        return returnCode != null && returnCode == 1;
    }
}
