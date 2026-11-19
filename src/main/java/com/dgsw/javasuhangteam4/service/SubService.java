package com.dgsw.javasuhangteam4.service;

import com.dgsw.javasuhangteam4.dto.response.SubResponseDto;
import java.util.List;

public interface SubService {

    List<SubResponseDto> getSubs();

    SubResponseDto getSub(Long id);
}
