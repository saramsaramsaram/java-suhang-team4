package com.dgsw.javasuhangteam4.service.impl;

import com.dgsw.javasuhangteam4.dto.request.SubCreateRequest;
import com.dgsw.javasuhangteam4.dto.request.SubUpdateRequest;
import com.dgsw.javasuhangteam4.dto.response.SubResponseDto;
import com.dgsw.javasuhangteam4.enitity.Member;
import com.dgsw.javasuhangteam4.enitity.Sub;
import com.dgsw.javasuhangteam4.exception.MemberNotFoundException;
import com.dgsw.javasuhangteam4.exception.SubNotFoundException;
import com.dgsw.javasuhangteam4.repository.MemberRepository;
import com.dgsw.javasuhangteam4.repository.SubRepository;
import com.dgsw.javasuhangteam4.service.SubService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.YearMonth;
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
    public SubResponseDto getSub(Long id) {
        return SubResponseDto.from(findSub(id));
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

    // ponytail: 결제 이력 테이블이 없어서 "지금 활성 + 그 달 말일 이전 시작" 구독을 그 달 지출로 봄
    @Override
    public byte[] exportExcel(YearMonth month) {
        List<Sub> subs = subRepository.findByActiveTrueAndStartDateLessThanEqualOrderByBillingDay(month.atEndOfMonth());

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(month.toString());
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("서비스명");
            header.createCell(1).setCellValue("결제일");
            header.createCell(2).setCellValue("금액");

            BigDecimal total = BigDecimal.ZERO;
            int rowIndex = 1;
            for (Sub sub : subs) {
                // 31일 결제인데 그 달이 30일까지면 말일에 결제
                int day = Math.min(sub.getBillingDay(), month.lengthOfMonth());
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(sub.getServiceName());
                row.createCell(1).setCellValue(month.atDay(day).toString());
                row.createCell(2).setCellValue(sub.getMonthlyPrice().doubleValue());
                total = total.add(sub.getMonthlyPrice());
            }

            Row sum = sheet.createRow(rowIndex);
            sum.createCell(0).setCellValue("합계");
            sum.createCell(2).setCellValue(total.doubleValue());

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
