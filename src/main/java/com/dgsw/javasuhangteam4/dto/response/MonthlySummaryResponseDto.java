package com.dgsw.javasuhangteam4.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonthlySummaryResponseDto {

    private String month;
    private int count;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private List<MonthlyBillingDto> subs;
}
