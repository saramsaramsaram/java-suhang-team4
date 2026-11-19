package com.dgsw.javasuhangteam4.enitity;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class SubPersistenceTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void storesSubscriptionsWithOrWithoutCategory() {
        var member = Member.builder()
                .name("테스트 회원")
                .email("member@example.com")
                .build();
        var category = Category.builder().name("음악").build();
        entityManager.persist(member);
        entityManager.persist(category);

        var categorized = Sub.builder()
                .member(member)
                .category(category)
                .serviceName("유튜브 뮤직")
                .monthlyPrice(new BigDecimal("14900"))
                .billingDay(3)
                .startDate(LocalDate.of(2026, 11, 1))
                .build();
        var uncategorized = Sub.builder()
                .member(member)
                .serviceName("예시 구독")
                .monthlyPrice(new BigDecimal("5000"))
                .billingDay(10)
                .startDate(LocalDate.of(2026, 11, 1))
                .active(false)
                .build();
        entityManager.persist(categorized);
        entityManager.persist(uncategorized);
        entityManager.flush();
        entityManager.clear();

        var saved = entityManager.find(Sub.class, categorized.getId());
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getMember().getId()).isEqualTo(member.getId());
        assertThat(saved.getCategory().getName()).isEqualTo("음악");
        assertThat(saved.getCreatedAt()).isNotNull();

        var savedWithoutCategory = entityManager.find(Sub.class, uncategorized.getId());
        assertThat(savedWithoutCategory.getCategory()).isNull();
        assertThat(savedWithoutCategory.isActive()).isFalse();
    }
}
