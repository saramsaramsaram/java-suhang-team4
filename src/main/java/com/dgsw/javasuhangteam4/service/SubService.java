package com.dgsw.javasuhangteam4.service;

import com.dgsw.javasuhangteam4.dto.request.SubCreateRequest;
import com.dgsw.javasuhangteam4.dto.request.SubUpdateRequest;
import com.dgsw.javasuhangteam4.dto.response.SubResponseDto;
import java.util.List;

public interface SubService {

    SubResponseDto createSub(SubCreateRequest request);

    List<SubResponseDto> getSubs();

    SubResponseDto getSub(Long id);

    SubResponseDto updateSub(Long id, SubUpdateRequest request);
}
