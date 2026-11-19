package com.dgsw.javasuhangteam4.controller.subscribe;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dgsw.javasuhangteam4.enitity.Member;
import com.dgsw.javasuhangteam4.enitity.Sub;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
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
}
