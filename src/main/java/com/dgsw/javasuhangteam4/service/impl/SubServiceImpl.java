package com.dgsw.javasuhangteam4.service.impl;

import com.dgsw.javasuhangteam4.dto.request.SubCreateRequest;
import com.dgsw.javasuhangteam4.dto.request.SubUpdateRequest;
import com.dgsw.javasuhangteam4.dto.response.MonthlyBillingDto;
import com.dgsw.javasuhangteam4.dto.response.MonthlySummaryResponseDto;
import com.dgsw.javasuhangteam4.dto.response.SubDetailResponseDto;
import com.dgsw.javasuhangteam4.dto.response.SubResponseDto;
import com.dgsw.javasuhangteam4.enitity.Member;
import com.dgsw.javasuhangteam4.enitity.Sub;
import com.dgsw.javasuhangteam4.exception.MemberNotFoundException;
import com.dgsw.javasuhangteam4.exception.SubNotFoundException;
import com.dgsw.javasuhangteam4.repository.MemberRepository;
import com.dgsw.javasuhangteam4.repository.SubRepository;
import com.dgsw.javasuhangteam4.service.BillingCalculator;
import com.dgsw.javasuhangteam4.service.SubService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubServiceImpl implements SubService {

    private final SubRepository subRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    @Override
    @Transactional
    public SubResponseDto createSub(SubCreateRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(MemberNotFoundException::new);
        Sub sub = Sub.builder()
                .member(member)
                .serviceName(request.serviceName().strip())
                .monthlyPrice(request.monthlyPrice())
                .billingDay(request.billingDay())
                .startDate(request.startDate())
                .build();
        return SubResponseDto.from(subRepository.save(sub));
    }

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

    @Override
    @Transactional
    public void deleteSub(Long id) {
        subRepository.delete(findSub(id));
    }

    // 이번 달 지출 조회(getMonthlySummary)와 같은 기준으로 그 달 결제 내역을 만든다.
    @Override
    public byte[] exportExcel(YearMonth month) {
        MonthlySummaryResponseDto summary = getMonthlySummary(month);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(month.toString());
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("서비스명");
            header.createCell(1).setCellValue("결제일");
            header.createCell(2).setCellValue("금액");

            int rowIndex = 1;
            for (MonthlyBillingDto billing : summary.getSubs()) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(billing.getServiceName());
                row.createCell(1).setCellValue(billing.getBillingDate().toString());
                row.createCell(2).setCellValue(billing.getMonthlyPrice().doubleValue());
            }

            Row sum = sheet.createRow(rowIndex);
            sum.createCell(0).setCellValue("합계");
            sum.createCell(2).setCellValue(summary.getTotalAmount().doubleValue());

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Sub findSub(Long id) {
        return subRepository.findById(id).orElseThrow(SubNotFoundException::new);
    }
}
