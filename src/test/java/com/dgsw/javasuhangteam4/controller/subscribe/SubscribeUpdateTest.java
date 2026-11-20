package com.dgsw.javasuhangteam4.controller.subscribe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dgsw.javasuhangteam4.enitity.Member;
import com.dgsw.javasuhangteam4.enitity.Sub;
import com.dgsw.javasuhangteam4.repository.SubRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:subscription-update")
@AutoConfigureMockMvc
class SubscribeUpdateTest {

    private static final String VALID_UPDATE = """
            {"serviceName":"유튜브 프리미엄","monthlyPrice":14900,"billingDay":3}
            """;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private SubRepository subRepository;


    @Test
    void savesUpdate() throws Exception {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        Long id = transactionTemplate.execute(tx -> {
            Member member = Member.builder().name("테스트 회원").email("member@example.com").build();
            entityManager.persist(member);

            Sub sub = Sub.builder()
                    .member(member)
                    .serviceName("유튜브 뮤직")
                    .monthlyPrice(new BigDecimal("10900"))
                    .billingDay(1)
                    .startDate(startDate)
                    .active(false)
                    .build();
            entityManager.persist(sub);
            return sub.getId();
        });

        update(id, VALID_UPDATE)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.serviceName").value("유튜브 프리미엄"))
                .andExpect(jsonPath("$.monthlyPrice").value(14900))
                .andExpect(jsonPath("$.billingDay").value(3));

        Sub saved = subRepository.findById(id).orElseThrow();
        assertThat(saved.getServiceName()).isEqualTo("유튜브 프리미엄");
        assertThat(saved.getMonthlyPrice()).isEqualByComparingTo("14900");
        assertThat(saved.getBillingDay()).isEqualTo(3);
        assertThat(saved.getStartDate()).isEqualTo(startDate);
        assertThat(saved.isActive()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"serviceName\":\" \", \"monthlyPrice\":14900, \"billingDay\":3}",
            "{\"serviceName\":\"유튜브\", \"monthlyPrice\":-100, \"billingDay\":3}",
            "{\"serviceName\":\"유튜브\", \"monthlyPrice\":14900, \"billingDay\":32}",
            "{\"serviceName\":\"유튜브\", \"monthlyPrice\":14900}"
    })
    void rejectsInvalidInput(String body) throws Exception {
        update(1, body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 오류"));
    }

    @Test
    void rejectsBrokenJson() throws Exception {
        update(1, "{").andExpect(status().isBadRequest());
    }

    @Test
    void rejectsUnknownSub() throws Exception {
        update(99999, VALID_UPDATE)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("구독 없음"));
    }


    private ResultActions update(long id, String body) throws Exception {
        return mvc.perform(put("/api/subs/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
