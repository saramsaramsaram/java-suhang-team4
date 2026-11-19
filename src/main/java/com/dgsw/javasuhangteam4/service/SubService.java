package com.dgsw.javasuhangteam4.service;

import com.dgsw.javasuhangteam4.dto.request.SubUpdateRequest;
import com.dgsw.javasuhangteam4.dto.response.SubDetailResponseDto;
import com.dgsw.javasuhangteam4.dto.response.SubResponseDto;
import java.util.List;

public interface SubService {

    List<SubResponseDto> getSubs();

    SubDetailResponseDto getSub(Long id);

    SubResponseDto updateSub(Long id, SubUpdateRequest request);
}
