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
import java.util.List;
import lombok.RequiredArgsConstructor;
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

    private Sub findSub(Long id) {
        return subRepository.findById(id).orElseThrow(SubNotFoundException::new);
    }
}
