package com.dgsw.javasuhangteam4.controller.subscribe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dgsw.javasuhangteam4.enitity.Member;
import com.dgsw.javasuhangteam4.enitity.Sub;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:subscription-read")
@AutoConfigureMockMvc
class SubscribeReadTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    // 오늘을 2026-11-19로 고정
    @TestBean
    private Clock clock;

    static Clock clock() {
        return Clock.fixed(LocalDate.of(2026, 11, 19).atStartOfDay(ZONE).toInstant(), ZONE);
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void returnsDetailWithTotalSpent() throws Exception {
        Long subId = transactionTemplate.execute(transaction -> {
            Member member = Member.builder().name("테스트 회원").email("member@example.com").build();
            entityManager.persist(member);
            Sub sub = Sub.builder()
                    .member(member)
                    .serviceName("유튜브 뮤직")
                    .monthlyPrice(new BigDecimal("14900"))
                    .billingDay(1)
                    .startDate(LocalDate.of(2026, 8, 1))
                    .build();
            entityManager.persist(sub);
            return sub.getId();
        });

        // 8/1, 9/1, 10/1, 11/1 → 4회 × 14,900원
        mvc.perform(get("/subscriptions/{id}", subId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceName").value("유튜브 뮤직"))
                .andExpect(jsonPath("$.paymentCount").value(4))
                .andExpect(jsonPath("$.totalSpent").value(59600));
    }

    @Test
    void returnsNotFoundForUnknownSub() throws Exception {
        mvc.perform(get("/subscriptions/{id}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("구독 없음"));
    }

    @Test
    void returnsMonthlySummary() throws Exception {
        mvc.perform(get("/subscriptions/summary").param("month", "2026-11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("2026-11"))
                .andExpect(jsonPath("$.subs").isArray());
    }

    @Test
    void rejectsInvalidMonth() throws Exception {
        mvc.perform(get("/subscriptions/summary").param("month", "2026-13"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("입력값 오류"));
    }

    @Test
    void excelMatchesMonthlySummary() throws Exception {
        transactionTemplate.executeWithoutResult(transaction -> {
            Member member = Member.builder().name("엑셀 회원").email("excel@example.com").build();
            entityManager.persist(member);
            // 11/5 결제일보다 늦게 시작 → 11월에는 결제되지 않음
            entityManager.persist(Sub.builder().member(member).serviceName("늦게 시작한 구독")
                    .monthlyPrice(new BigDecimal("5000")).billingDay(5)
                    .startDate(LocalDate.of(2026, 11, 15)).build());
            entityManager.persist(Sub.builder().member(member).serviceName("스포티파이")
                    .monthlyPrice(new BigDecimal("10900")).billingDay(20)
                    .startDate(LocalDate.of(2026, 9, 20)).build());
        });

        String summary = mvc.perform(get("/subscriptions/summary").param("month", "2026-11"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<String> summaryNames = JsonPath.read(summary, "$.subs[*].serviceName");
        double summaryTotal = ((Number) JsonPath.read(summary, "$.totalAmount")).doubleValue();

        byte[] file = mvc.perform(get("/api/subs/export").param("month", "2026-11"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(file))) {
            Sheet sheet = workbook.getSheetAt(0);
            List<String> excelNames = new ArrayList<>();
            for (int i = 1; i < sheet.getLastRowNum(); i++) {
                excelNames.add(sheet.getRow(i).getCell(0).getStringCellValue());
            }
            Row sum = sheet.getRow(sheet.getLastRowNum());

            assertThat(excelNames).containsExactlyElementsOf(summaryNames)
                    .contains("스포티파이")
                    .doesNotContain("늦게 시작한 구독");
            assertThat(sum.getCell(0).getStringCellValue()).isEqualTo("합계");
            assertThat(sum.getCell(2).getNumericCellValue()).isEqualTo(summaryTotal);
        }
    }
}
