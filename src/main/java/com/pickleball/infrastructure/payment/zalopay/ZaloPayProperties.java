package com.pickleball.infrastructure.payment.zalopay;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "zalopay")
public class ZaloPayProperties {
    private Integer appId;
    private String key1;
    private String key2;
    private String createEndpoint = "https://sb-openapi.zalopay.vn/v2/create";
    private String queryEndpoint = "https://sb-openapi.zalopay.vn/v2/query";
    private String refundEndpoint = "https://sb-openapi.zalopay.vn/v2/refund";
    private String callbackUrl;
    private String redirectUrl;
}
