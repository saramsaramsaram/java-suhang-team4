package com.dgsw.javasuhangteam4.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SubCreateRequest(
        @NotNull Long memberId,
        @NotBlank @Size(max = 255) String serviceName,
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal monthlyPrice,
        @NotNull @Min(1) @Max(31) Integer billingDay,
        @NotNull LocalDate startDate
) {
}
