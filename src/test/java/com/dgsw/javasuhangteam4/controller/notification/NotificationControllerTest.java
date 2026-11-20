package com.dgsw.javasuhangteam4.controller.notification;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dgsw.javasuhangteam4.service.NotificationMailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = NotificationController.class, properties = {
        "app.notifications.from=sender@example.com",
        "app.notifications.test-recipient="
})
@Import(NotificationMailService.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JavaMailSender mailSender;

    @Test
    void rejectsMissingRecipient() throws Exception {
        mvc.perform(post("/api/notifications/test"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("메일 발송 실패"));

        verifyNoInteractions(mailSender);
    }
}
