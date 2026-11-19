package com.dgsw.javasuhangteam4.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.dgsw.javasuhangteam4.dto.response.MonthlyBillingDto;
import com.dgsw.javasuhangteam4.dto.response.MonthlySummaryResponseDto;
import com.dgsw.javasuhangteam4.enitity.Sub;
import com.dgsw.javasuhangteam4.exception.SubNotFoundException;
import com.dgsw.javasuhangteam4.repository.MemberRepository;
import com.dgsw.javasuhangteam4.repository.SubRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SubServiceImplTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private SubRepository subRepository;
    private SubServiceImpl subService;

    @BeforeEach
    void setUp() {
        subRepository = mock(SubRepository.class);
        // 오늘을 2026-11-19로 고정
        Clock clock = Clock.fixed(LocalDate.of(2026, 11, 19).atStartOfDay(ZONE).toInstant(), ZONE);
        subService = new SubServiceImpl(subRepository, mock(MemberRepository.class), clock);
    }

    @Test
    void throwsWhenSubDoesNotExist() {
        given(subRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> subService.getSub(999L))
                .isInstanceOf(SubNotFoundException.class);
    }

    @Test
    void summarizesThisMonthBillings() {
        Sub netflix = sub("넷플릭스", "17000", 5, LocalDate.of(2026, 7, 5));
        Sub spotify = sub("스포티파이", "10900", 20, LocalDate.of(2026, 9, 20));
        Sub startedLate = sub("이번 달 늦게 시작", "5000", 10, LocalDate.of(2026, 11, 15));
        given(subRepository.findActiveSubsStartedBy(LocalDate.of(2026, 11, 30)))
                .willReturn(List.of(netflix, startedLate, spotify));

        MonthlySummaryResponseDto summary = subService.getMonthlySummary(null);

        assertThat(summary.getMonth()).isEqualTo("2026-11");
        assertThat(summary.getCount()).isEqualTo(2);
        assertThat(summary.getTotalAmount()).isEqualByComparingTo("27900");
        assertThat(summary.getPaidAmount()).isEqualByComparingTo("17000");
        assertThat(summary.getRemainingAmount()).isEqualByComparingTo("10900");
        assertThat(summary.getSubs())
                .extracting(MonthlyBillingDto::getServiceName, MonthlyBillingDto::getBillingDate, MonthlyBillingDto::isPaid)
                .containsExactly(
                        tuple("넷플릭스", LocalDate.of(2026, 11, 5), true),
                        tuple("스포티파이", LocalDate.of(2026, 11, 20), false));
    }

    @Test
    void summarizesRequestedMonth() {
        given(subRepository.findActiveSubsStartedBy(LocalDate.of(2026, 10, 31))).willReturn(List.of());

        MonthlySummaryResponseDto summary = subService.getMonthlySummary(YearMonth.of(2026, 10));

        assertThat(summary.getMonth()).isEqualTo("2026-10");
        assertThat(summary.getCount()).isZero();
        assertThat(summary.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private Sub sub(String name, String price, int billingDay, LocalDate startDate) {
        return Sub.builder()
                .serviceName(name)
                .monthlyPrice(new BigDecimal(price))
                .billingDay(billingDay)
                .startDate(startDate)
                .build();
    }
}
