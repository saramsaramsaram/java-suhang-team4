package com.dgsw.javasuhangteam4.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dgsw.javasuhangteam4.enitity.Member;
import com.dgsw.javasuhangteam4.enitity.Sub;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:notification-mail",
        "app.notifications.from=sender@example.com",
        "app.notifications.test-recipient=test@example.com",
        "app.notifications.reminder-cron=-"
})
@AutoConfigureMockMvc
class NotificationMailTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BillingReminderScheduler scheduler;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private JavaMailSender mailSender;


    @Test
    void sendsTestMailToConfiguredRecipient() throws Exception {
        mvc.perform(post("/api/notifications/test")).andExpect(status().isCreated());

        var captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage mail = captor.getValue();
        assertThat(mail.getTo()).containsExactly("test@example.com");
        assertThat(mail.getFrom()).isEqualTo("sender@example.com");
    }

    @Test
    void returnsBadGatewayWhenSmtpFails() throws Exception {
        doThrow(new MailSendException("SMTP 오류"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        mvc.perform(post("/api/notifications/test"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502));
    }

    @Test
    @Transactional
    void sendsMonthEndReminderForActiveSubscription() {
        LocalDate today = LocalDate.of(2026, 2, 28);
        Member member = Member.builder().name("테스트 회원").email("member@example.com").build();
        entityManager.persist(member);

        saveSub(member, "31일 결제", 31, today, true);
        saveSub(member, "지난 결제", 27, today.minusDays(2), true);
        saveSub(member, "비활성 구독", 28, today, false);
        saveSub(member, "다음 달 시작", 28, today.plusDays(1), true);

        entityManager.flush();
        entityManager.clear();

        scheduler.sendReminders(today);

        var captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage mail = captor.getValue();
        assertThat(mail.getTo()).containsExactly("member@example.com");
        assertThat(mail.getText()).contains("31일 결제", "2026-02-28", "14900원");
        verifyNoMoreInteractions(mailSender);
    }


    private void saveSub(Member member, String name, int billingDay, LocalDate startDate, boolean active) {
        entityManager.persist(Sub.builder()
                .member(member)
                .serviceName(name)
                .monthlyPrice(new BigDecimal("14900"))
                .billingDay(billingDay)
                .startDate(startDate)
                .active(active)
                .build());
    }
}
