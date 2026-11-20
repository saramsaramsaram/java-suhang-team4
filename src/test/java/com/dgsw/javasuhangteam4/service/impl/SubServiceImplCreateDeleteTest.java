package com.dgsw.javasuhangteam4.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.dgsw.javasuhangteam4.dto.request.SubCreateRequest;
import com.dgsw.javasuhangteam4.dto.response.SubResponseDto;
import com.dgsw.javasuhangteam4.enitity.Member;
import com.dgsw.javasuhangteam4.enitity.Sub;
import com.dgsw.javasuhangteam4.exception.MemberNotFoundException;
import com.dgsw.javasuhangteam4.exception.SubNotFoundException;
import com.dgsw.javasuhangteam4.repository.MemberRepository;
import com.dgsw.javasuhangteam4.repository.SubRepository;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SubServiceImplCreateDeleteTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private SubRepository subRepository;
    private MemberRepository memberRepository;
    private SubServiceImpl subService;

    @BeforeEach
    void setUp() {
        subRepository = mock(SubRepository.class);
        memberRepository = mock(MemberRepository.class);
        Clock clock = Clock.fixed(LocalDate.of(2026, 11, 19).atStartOfDay(ZONE).toInstant(), ZONE);
        subService = new SubServiceImpl(subRepository, memberRepository, clock);
    }

    @Test
    void createsSubForMember() {
        Member member = Member.builder().name("테스트 회원").email("member@example.com").build();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(subRepository.save(any(Sub.class))).willAnswer(invocation -> invocation.getArgument(0));

        SubResponseDto response = subService.createSub(
                new SubCreateRequest(1L, "  유튜브 뮤직  ", new BigDecimal("14900"), 1, LocalDate.of(2026, 11, 1)));

        ArgumentCaptor<Sub> saved = ArgumentCaptor.forClass(Sub.class);
        verify(subRepository).save(saved.capture());
        assertThat(saved.getValue().getMember()).isSameAs(member);
        assertThat(saved.getValue().isActive()).isTrue();
        assertThat(response.getServiceName()).isEqualTo("유튜브 뮤직");
        assertThat(response.getMonthlyPrice()).isEqualByComparingTo("14900");
        assertThat(response.getBillingDay()).isEqualTo(1);
    }

    @Test
    void createThrowsWhenMemberDoesNotExist() {
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> subService.createSub(
                new SubCreateRequest(999L, "넷플릭스", new BigDecimal("13500"), 5, LocalDate.of(2026, 11, 1))))
                .isInstanceOf(MemberNotFoundException.class);
        verify(subRepository, never()).save(any());
    }

    @Test
    void deletesExistingSub() {
        Sub sub = sub("넷플릭스", "13500", 5, LocalDate.of(2026, 11, 1));
        given(subRepository.findById(1L)).willReturn(Optional.of(sub));

        subService.deleteSub(1L);

        verify(subRepository).delete(sub);
    }

    @Test
    void deleteThrowsWhenSubDoesNotExist() {
        given(subRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> subService.deleteSub(999L)).isInstanceOf(SubNotFoundException.class);
        verify(subRepository, never()).delete(any());
    }

    @Test
    void exportsMonthlyExcelWithTotalRow() throws Exception {
        given(subRepository.findActiveSubsStartedBy(LocalDate.of(2026, 2, 28)))
                .willReturn(List.of(
                        sub("유튜브 뮤직", "14900", 1, LocalDate.of(2026, 1, 1)),
                        sub("넷플릭스", "13500", 31, LocalDate.of(2026, 1, 15))));

        byte[] file = subService.exportExcel(YearMonth.of(2026, 2));

        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(file))) {
            Sheet sheet = workbook.getSheet("2026-02");
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("서비스명");
            assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("2026-02-01");
            // 31일 결제는 2월 말일(28일)로 들어간다
            assertThat(sheet.getRow(2).getCell(1).getStringCellValue()).isEqualTo("2026-02-28");
            assertThat(sheet.getRow(3).getCell(0).getStringCellValue()).isEqualTo("합계");
            assertThat(sheet.getRow(3).getCell(2).getNumericCellValue()).isEqualTo(28400);
        }
    }

    private Sub sub(String name, String price, int billingDay, LocalDate startDate) {
        return Sub.builder()
                .serviceName(name)
                .monthlyPrice(new BigDecimal(price))
                .billingDay(billingDay)
                .startDate(startDate)
                .build();
    }
}
