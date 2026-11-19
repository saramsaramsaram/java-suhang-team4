package com.dgsw.javasuhangteam4.dto.response;

import com.dgsw.javasuhangteam4.enitity.Sub;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubDetailResponseDto {

    private Long id;
    private String serviceName;
    private BigDecimal monthlyPrice;
    private Integer billingDay;
    private LocalDate startDate;
    private boolean active;
    private LocalDate createdAt;
    private int paymentCount;
    private BigDecimal totalSpent;

    public static SubDetailResponseDto of(Sub sub, int paymentCount) {
        return SubDetailResponseDto.builder()
                .id(sub.getId())
                .serviceName(sub.getServiceName())
                .monthlyPrice(sub.getMonthlyPrice())
                .billingDay(sub.getBillingDay())
                .startDate(sub.getStartDate())
                .active(sub.isActive())
                .createdAt(sub.getCreatedAt().toLocalDate())
                .paymentCount(paymentCount)
                .totalSpent(sub.getMonthlyPrice().multiply(BigDecimal.valueOf(paymentCount)))
                .build();
    }
}
