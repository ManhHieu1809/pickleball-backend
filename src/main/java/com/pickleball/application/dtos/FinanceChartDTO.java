package com.pickleball.application.dtos;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
@Data
@Builder
public class FinanceChartDTO {
    private List<String> labels;
    private List<BigDecimal> data;
}