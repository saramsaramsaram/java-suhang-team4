package com.dgsw.javasuhangteam4.service;

import java.time.LocalDate;
import java.time.YearMonth;

public final class BillingCalculator {

    private BillingCalculator() {
    }

    // 결제일이 그 달의 마지막 날보다 크면 마지막 날에 결제된다. (예: 31일 결제 → 2월은 28일)
    public static LocalDate billingDate(YearMonth month, int billingDay) {
        return month.atDay(Math.min(billingDay, month.lengthOfMonth()));
    }

    // 시작일부터 오늘까지 실제로 지나간 결제일의 수
    public static int countPayments(LocalDate startDate, int billingDay, LocalDate today) {
        if (today.isBefore(startDate)) {
            return 0;
        }

        int count = 0;
        YearMonth last = YearMonth.from(today);
        for (YearMonth month = YearMonth.from(startDate); !month.isAfter(last); month = month.plusMonths(1)) {
            LocalDate billingDate = billingDate(month, billingDay);
            if (!billingDate.isBefore(startDate) && !billingDate.isAfter(today)) {
                count++;
            }
        }
        return count;
    }
}
