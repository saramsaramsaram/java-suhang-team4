package com.dgsw.javasuhangteam4.service;

import com.dgsw.javasuhangteam4.enitity.Sub;
import com.dgsw.javasuhangteam4.repository.SubRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingReminderScheduler {

    private static final String ZONE = "Asia/Seoul";

    private final SubRepository subRepository;
    private final NotificationMailService mailService;


    @Scheduled(cron = "${app.notifications.reminder-cron:-}", zone = ZONE)
    public void sendTodayReminders() {
        sendReminders(LocalDate.now(ZoneId.of(ZONE)));
    }

    void sendReminders(LocalDate today) {
        int day = today.getDayOfMonth();
        int lastBillingDay = day == today.lengthOfMonth() ? 31 : day;
        List<Sub> dueSubs = subRepository.findDueSubs(today, day, lastBillingDay);

        for (Sub sub : dueSubs) {
            try {
                mailService.sendBillingReminder(sub, today);
            } catch (MailException ignored) {
                log.warn("메일 발송 실패 (구독 {})", sub.getId());
            }
        }
    }
}
