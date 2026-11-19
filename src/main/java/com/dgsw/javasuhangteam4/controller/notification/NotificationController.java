package com.dgsw.javasuhangteam4.controller.notification;

import com.dgsw.javasuhangteam4.service.NotificationMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationMailService mailService;

    @PostMapping("/api/notifications/test")
    @ResponseStatus(HttpStatus.CREATED)
    public void sendTestMail() {
        mailService.sendTestMail();
    }
}
