package com.dgsw.javasuhangteam4.service;

import com.dgsw.javasuhangteam4.dto.request.SubCreateRequest;
import com.dgsw.javasuhangteam4.dto.request.SubUpdateRequest;
import com.dgsw.javasuhangteam4.dto.response.MonthlySummaryResponseDto;
import com.dgsw.javasuhangteam4.dto.response.SubDetailResponseDto;
import com.dgsw.javasuhangteam4.dto.response.SubResponseDto;
import java.time.YearMonth;
import java.util.List;

public interface SubService {

    SubResponseDto createSub(SubCreateRequest request);

    List<SubResponseDto> getSubs();

    SubDetailResponseDto getSub(Long id);

    // month가 null이면 이번 달을 조회한다.
    MonthlySummaryResponseDto getMonthlySummary(YearMonth month);

    SubResponseDto updateSub(Long id, SubUpdateRequest request);

    void deleteSub(Long id);

    byte[] exportExcel(YearMonth month);
}
