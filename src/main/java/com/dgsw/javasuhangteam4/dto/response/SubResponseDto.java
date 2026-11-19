package com.dgsw.javasuhangteam4.dto.response;

import com.dgsw.javasuhangteam4.enitity.Sub;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubResponseDto {

    private Long id;
    private String serviceName;
    private BigDecimal monthlyPrice;
    private Integer billingDay;
    private LocalDate startDate;
    private boolean active;

    public static SubResponseDto from(Sub sub) {
        return SubResponseDto.builder()
                .id(sub.getId())
                .serviceName(sub.getServiceName())
                .monthlyPrice(sub.getMonthlyPrice())
                .billingDay(sub.getBillingDay())
                .startDate(sub.getStartDate())
                .active(sub.isActive())
                .build();
    }
}
