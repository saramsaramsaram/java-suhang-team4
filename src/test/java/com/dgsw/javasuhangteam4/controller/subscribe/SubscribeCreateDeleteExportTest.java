package com.dgsw.javasuhangteam4.controller.subscribe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dgsw.javasuhangteam4.enitity.Member;
import com.dgsw.javasuhangteam4.repository.MemberRepository;
import com.jayway.jsonpath.JsonPath;
import java.io.ByteArrayInputStream;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:subscription-create-delete-export")
@AutoConfigureMockMvc
class SubscribeCreateDeleteExportTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void createsThenDeletesSubscription() throws Exception {
        Long memberId = saveMember();

        String body = mvc.perform(create(memberId, "유튜브 뮤직", 14900, 1, "2026-11-01"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serviceName").value("유튜브 뮤직"))
                .andReturn().getResponse().getContentAsString();
        long subId = ((Number) JsonPath.read(body, "$.id")).longValue();

        mvc.perform(delete("/api/subs/{id}", subId)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/subs/{id}", subId)).andExpect(status().isNotFound());
    }

    @Test
    void rejectsUnknownMember() throws Exception {
        mvc.perform(create(99999L, "넷플릭스", 13500, 5, "2026-11-01"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("회원 없음"));
    }

    @Test
    void exportsMonthlyExcel() throws Exception {
        Long memberId = saveMember();
        mvc.perform(create(memberId, "월말 결제", 10000, 31, "2026-01-01")).andExpect(status().isCreated());
        mvc.perform(create(memberId, "3월 시작", 5000, 1, "2026-03-01")).andExpect(status().isCreated());

        byte[] file = mvc.perform(get("/api/subs/export").param("month", "2026-02"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(file))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("월말 결제");
            assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("2026-02-28");
            assertThat(sheet.getRow(2).getCell(0).getStringCellValue()).isEqualTo("합계");
            assertThat(sheet.getRow(2).getCell(2).getNumericCellValue()).isEqualTo(10000);
        }
    }

    private Long saveMember() {
        return memberRepository.save(Member.builder().name("테스트 회원").email("member@example.com").build()).getId();
    }

    private static org.springframework.test.web.servlet.RequestBuilder create(
            Long memberId, String name, int price, int billingDay, String startDate) {
        String json = """
                {"memberId":%d,"serviceName":"%s","monthlyPrice":%d,"billingDay":%d,"startDate":"%s"}
                """.formatted(memberId, name, price, billingDay, startDate);
        return post("/api/subs").contentType(MediaType.APPLICATION_JSON).content(json);
    }
}
