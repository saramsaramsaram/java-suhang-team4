package com.dgsw.javasuhangteam4.dto.response;

import com.dgsw.javasuhangteam4.enitity.Sub;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonthlyBillingDto {

    private Long id;
    private String serviceName;
    private BigDecimal monthlyPrice;
    private LocalDate billingDate;
    private boolean paid;

    public static MonthlyBillingDto of(Sub sub, LocalDate billingDate, boolean paid) {
        return MonthlyBillingDto.builder()
                .id(sub.getId())
                .serviceName(sub.getServiceName())
                .monthlyPrice(sub.getMonthlyPrice())
                .billingDate(billingDate)
                .paid(paid)
                .build();
    }
}
