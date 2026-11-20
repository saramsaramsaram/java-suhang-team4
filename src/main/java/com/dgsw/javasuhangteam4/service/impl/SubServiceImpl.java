package com.dgsw.javasuhangteam4.service.impl;

import com.dgsw.javasuhangteam4.dto.request.SubUpdateRequest;
import com.dgsw.javasuhangteam4.dto.response.MonthlyBillingDto;
import com.dgsw.javasuhangteam4.dto.response.MonthlySummaryResponseDto;
import com.dgsw.javasuhangteam4.dto.response.SubDetailResponseDto;
import com.dgsw.javasuhangteam4.dto.response.SubResponseDto;
import com.dgsw.javasuhangteam4.enitity.Sub;
import com.dgsw.javasuhangteam4.exception.SubNotFoundException;
import com.dgsw.javasuhangteam4.repository.SubRepository;
import com.dgsw.javasuhangteam4.service.BillingCalculator;
import com.dgsw.javasuhangteam4.service.SubService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubServiceImpl implements SubService {

    private final SubRepository subRepository;
    private final Clock clock;

    @Override
    public List<SubResponseDto> getSubs() {
        return subRepository.findAll().stream()
                .map(SubResponseDto::from)
                .toList();
    }

    @Override
    public SubDetailResponseDto getSub(Long id) {
        Sub sub = findSub(id);
        int paymentCount = BillingCalculator.countPayments(
                sub.getStartDate(), sub.getBillingDay(), LocalDate.now(clock));
        return SubDetailResponseDto.of(sub, paymentCount);
    }

    @Override
    public MonthlySummaryResponseDto getMonthlySummary(YearMonth month) {
        LocalDate today = LocalDate.now(clock);
        YearMonth target = month != null ? month : YearMonth.from(today);

        List<MonthlyBillingDto> billings = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal paid = BigDecimal.ZERO;

        for (Sub sub : subRepository.findActiveSubsStartedBy(target.atEndOfMonth())) {
            LocalDate billingDate = BillingCalculator.billingDate(target, sub.getBillingDay());
            if (billingDate.isBefore(sub.getStartDate())) {
                continue; // 이번 달 결제일 이후에 시작한 구독은 다음 달부터 결제된다.
            }

            boolean isPaid = !billingDate.isAfter(today);
            billings.add(MonthlyBillingDto.of(sub, billingDate, isPaid));
            total = total.add(sub.getMonthlyPrice());
            if (isPaid) {
                paid = paid.add(sub.getMonthlyPrice());
            }
        }

        return MonthlySummaryResponseDto.builder()
                .month(target.toString())
                .count(billings.size())
                .totalAmount(total)
                .paidAmount(paid)
                .remainingAmount(total.subtract(paid))
                .subs(billings)
                .build();
    }

    @Override
    @Transactional
    public SubResponseDto updateSub(Long id, SubUpdateRequest request) {
        Sub sub = findSub(id);
        sub.setServiceName(request.serviceName().strip());
        sub.setMonthlyPrice(request.monthlyPrice());
        sub.setBillingDay(request.billingDay());
        return SubResponseDto.from(sub);
    }

    private Sub findSub(Long id) {
        return subRepository.findById(id).orElseThrow(SubNotFoundException::new);
    }
}
