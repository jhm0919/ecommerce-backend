package com.shop.global.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * 이메일 발송 공통 서비스.
 *
 * <p>@Async 로 비동기 발송 — 이메일 실패가 메인 플로우에 영향 안 줌.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.from-name}")
    private String fromName;

    /**
     * HTML 이메일 비동기 발송.
     *
     * <p>실패 시 로그만 남김 (재시도 없음).
     */
    @Async
    public void sendHtml(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(from, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);   // true = HTML

            mailSender.send(message);
            log.info("[Email] 발송 성공: to={}, subject={}", to, subject);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("[Email] 발송 실패: to={}, subject={}, error={}",
                    to, subject, e.getMessage());
            // 이메일 실패해도 비즈니스 로직에 영향 없음 (@Async 로 분리됨)
        }
    }
}
