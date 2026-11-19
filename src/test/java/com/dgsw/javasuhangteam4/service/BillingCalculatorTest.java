package com.dgsw.javasuhangteam4.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class BillingCalculatorTest {

    @Test
    void countsPaymentsFromStartDateUntilToday() {
        // 7/1, 8/1, 9/1, 10/1, 11/1 → 5회
        int count = BillingCalculator.countPayments(
                LocalDate.of(2026, 7, 1), 1, LocalDate.of(2026, 11, 19));

        assertThat(count).isEqualTo(5);
    }

    @Test
    void excludesThisMonthWhenBillingDayHasNotPassed() {
        // 7/20, 8/20, 9/20, 10/20 → 11/20은 아직 지나지 않음
        int count = BillingCalculator.countPayments(
                LocalDate.of(2026, 7, 20), 20, LocalDate.of(2026, 11, 19));

        assertThat(count).isEqualTo(4);
    }

    @Test
    void excludesStartMonthWhenStartedAfterBillingDay() {
        // 7/15 시작, 결제일 5일 → 8/5부터 결제
        int count = BillingCalculator.countPayments(
                LocalDate.of(2026, 7, 15), 5, LocalDate.of(2026, 9, 5));

        assertThat(count).isEqualTo(2);
    }

    @Test
    void returnsZeroBeforeStartDate() {
        int count = BillingCalculator.countPayments(
                LocalDate.of(2026, 12, 1), 1, LocalDate.of(2026, 11, 19));

        assertThat(count).isZero();
    }

    @Test
    void movesBillingDayToLastDayOfShortMonth() {
        assertThat(BillingCalculator.billingDate(YearMonth.of(2026, 2), 31))
                .isEqualTo(LocalDate.of(2026, 2, 28));
        assertThat(BillingCalculator.billingDate(YearMonth.of(2026, 11), 31))
                .isEqualTo(LocalDate.of(2026, 11, 30));
    }
}
