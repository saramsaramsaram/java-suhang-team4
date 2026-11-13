package com.dgsw.javasuhangteam4.service.impl;

import com.dgsw.javasuhangteam4.dto.response.SubResponseDto;
import com.dgsw.javasuhangteam4.exception.SubNotFoundException;
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

    @Override
    public List<SubResponseDto> getSubs() {
        return subRepository.findAll().stream()
                .map(SubResponseDto::from)
                .toList();
    }

    @Override
    public SubResponseDto getSub(Long id) {
        return subRepository.findById(id)
                .map(SubResponseDto::from)
                .orElseThrow(() -> new SubNotFoundException(id));
    }
}
