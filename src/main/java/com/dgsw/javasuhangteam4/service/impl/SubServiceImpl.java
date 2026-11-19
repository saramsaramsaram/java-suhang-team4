package com.dgsw.javasuhangteam4.service.impl;

import com.dgsw.javasuhangteam4.dto.request.SubUpdateRequest;
import com.dgsw.javasuhangteam4.dto.response.SubDetailResponseDto;
import com.dgsw.javasuhangteam4.dto.response.SubResponseDto;
import com.dgsw.javasuhangteam4.enitity.Sub;
import com.dgsw.javasuhangteam4.exception.SubNotFoundException;
import com.dgsw.javasuhangteam4.repository.SubRepository;
import com.dgsw.javasuhangteam4.service.BillingCalculator;
import com.dgsw.javasuhangteam4.service.SubService;
import java.time.Clock;
import java.time.LocalDate;
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
