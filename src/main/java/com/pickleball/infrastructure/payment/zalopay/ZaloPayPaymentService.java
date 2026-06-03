package com.pickleball.infrastructure.payment.zalopay;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickleball.domain.valueobjects.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ZaloPayPaymentService {
    private static final String HMAC_SHA256 = "HmacSHA256";

    private final ZaloPayProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public ZaloPayCreateOrderResult createTopUpOrder(
            String appTransId,
            Long userId,
            Money amount,
            String description) {
        validateConfigured();

        long amountVnd = amount.getAmount().setScale(0, RoundingMode.HALF_UP).longValueExact();
        long appTime = Instant.now().toEpochMilli();
        String appUser = "user_" + userId;
        String item = "[]";
        String embedData = buildEmbedData();
        String macInput = properties.getAppId() + "|" + appTransId + "|" + appUser + "|"
                + amountVnd + "|" + appTime + "|" + embedData + "|" + item;
        String mac = hmacSha256(properties.getKey1(), macInput);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("app_id", properties.getAppId());
        request.put("app_user", appUser);
        request.put("app_trans_id", appTransId);
        request.put("app_time", appTime);
        request.put("amount", amountVnd);
        request.put("item", item);
        request.put("embed_data", embedData);
        request.put("description", trimDescription(description));
        request.put("callback_url", properties.getCallbackUrl());
        request.put("bank_code", "");
        request.put("mac", mac);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> response = restTemplate.postForObject(
                properties.getCreateEndpoint(),
                new HttpEntity<>(request, headers),
                Map.class);

        if (response == null) {
            throw new IllegalStateException("ZaloPay did not return a response");
        }

        return ZaloPayCreateOrderResult.builder()
                .appTransId(appTransId)
                .returnCode(asInt(response.get("return_code")))
                .returnMessage(asString(response.get("return_message")))
                .subReturnCode(asInt(response.get("sub_return_code")))
                .subReturnMessage(asString(response.get("sub_return_message")))
                .orderUrl(asString(response.get("order_url")))
                .orderToken(asString(response.get("order_token")))
                .zpTransToken(asString(response.get("zp_trans_token")))
                .qrCode(asString(response.get("qr_code")))
                .rawResponse(toJson(response))
                .build();
    }

    public ZaloPayQueryResult queryOrder(String appTransId) {
        validateConfigured();

        String macInput = properties.getAppId() + "|" + appTransId + "|" + properties.getKey1();
        String mac = hmacSha256(properties.getKey1(), macInput);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("app_id", properties.getAppId());
        request.put("app_trans_id", appTransId);
        request.put("mac", mac);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> response = restTemplate.postForObject(
                properties.getQueryEndpoint(),
                new HttpEntity<>(request, headers),
                Map.class);

        if (response == null) {
            throw new IllegalStateException("ZaloPay query did not return a response");
        }

        return ZaloPayQueryResult.builder()
                .appTransId(appTransId)
                .returnCode(asInt(response.get("return_code")))
                .returnMessage(asString(response.get("return_message")))
                .subReturnCode(asInt(response.get("sub_return_code")))
                .subReturnMessage(asString(response.get("sub_return_message")))
                .isProcessing(Boolean.TRUE.equals(response.get("is_processing")))
                .amount(asLong(response.get("amount")))
                .zpTransId(asLong(response.get("zp_trans_id")))
                .rawResponse(toJson(response))
                .build();
    }

    public boolean isValidCallback(String data, String requestMac) {
        validateConfigured();
        if (!StringUtils.hasText(data) || !StringUtils.hasText(requestMac)) {
            return false;
        }
        return hmacSha256(properties.getKey2(), data).equalsIgnoreCase(requestMac);
    }

    public Map<String, Object> parseCallbackData(String data) {
        try {
            return objectMapper.readValue(data, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ZaloPay callback data", e);
        }
    }

    private String buildEmbedData() {
        Map<String, String> embedData = new LinkedHashMap<>();
        if (StringUtils.hasText(properties.getRedirectUrl())) {
            embedData.put("redirecturl", properties.getRedirectUrl());
        }
        return toJson(embedData);
    }

    private void validateConfigured() {
        if (properties.getAppId() == null
                || !StringUtils.hasText(properties.getKey1())
                || !StringUtils.hasText(properties.getKey2())
                || !StringUtils.hasText(properties.getCreateEndpoint())
                || !StringUtils.hasText(properties.getCallbackUrl())) {
            throw new IllegalStateException("ZaloPay is not configured. Set ZALOPAY_APP_ID, ZALOPAY_KEY1, ZALOPAY_KEY2, ZALOPAY_ENDPOINT, and ZALOPAY_CALLBACK_URL.");
        }
    }

    private String hmacSha256(String key, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Could not calculate ZaloPay HMAC", e);
        }
    }

    private String trimDescription(String description) {
        String value = StringUtils.hasText(description) ? description : "Pickleball wallet top-up";
        return value.length() <= 256 ? value : value.substring(0, 256);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Could not serialize JSON", e);
        }
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer asInt(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private Long asLong(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }
}
