package com.dgsw.javasuhangteam4.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.dgsw.javasuhangteam4.enitity.Member;
import com.dgsw.javasuhangteam4.enitity.Sub;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class SubRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private SubRepository subRepository;

    @Test
    void findsActiveSubsStartedByMonthEndOrderedByBillingDay() {
        Member member = Member.builder().name("테스트 회원").email("member@example.com").build();
        entityManager.persist(member);
        persistSub(member, "스포티파이", 20, LocalDate.of(2026, 9, 1), true);
        persistSub(member, "넷플릭스", 5, LocalDate.of(2026, 7, 5), true);
        persistSub(member, "해지한 구독", 1, LocalDate.of(2026, 7, 1), false);
        persistSub(member, "다음 달 시작", 3, LocalDate.of(2026, 12, 1), true);
        entityManager.flush();
        entityManager.clear();

        List<Sub> subs = subRepository.findActiveSubsStartedBy(LocalDate.of(2026, 11, 30));

        assertThat(subs).extracting(Sub::getServiceName)
                .containsExactly("넷플릭스", "스포티파이");
    }

    private void persistSub(Member member, String name, int billingDay, LocalDate startDate, boolean active) {
        entityManager.persist(Sub.builder()
                .member(member)
                .serviceName(name)
                .monthlyPrice(new BigDecimal("10000"))
                .billingDay(billingDay)
                .startDate(startDate)
                .active(active)
                .build());
    }
}
