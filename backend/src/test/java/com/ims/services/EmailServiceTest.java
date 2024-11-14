package com.ims.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender emailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private static final String TEST_TO_ADDRESS = "test@example.com";
    private static final String TEST_SUBJECT = "Test Subject";
    private static final String TEST_CONTENT = "Test Content";

    @BeforeEach
    void setUp() {
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendSimpleMessage_ValidInputs_Success() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendSimpleMessage(TEST_TO_ADDRESS, TEST_SUBJECT, TEST_CONTENT);

        // Assert
        verify(emailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage capturedMessage = messageCaptor.getValue();

        assertNotNull(capturedMessage);
        assertEquals(TEST_TO_ADDRESS, capturedMessage.getTo()[0]);
        assertEquals(TEST_SUBJECT, capturedMessage.getSubject());
        assertEquals(TEST_CONTENT, capturedMessage.getText());
    }

    @Test
    void sendSimpleMessage_NullInputs_ThrowsException() {
        // Assert
        assertThrows(IllegalArgumentException.class, () ->
                emailService.sendSimpleMessage(null, TEST_SUBJECT, TEST_CONTENT));

        assertThrows(IllegalArgumentException.class, () ->
                emailService.sendSimpleMessage(TEST_TO_ADDRESS, null, TEST_CONTENT));

        assertThrows(IllegalArgumentException.class, () ->
                emailService.sendSimpleMessage(TEST_TO_ADDRESS, TEST_SUBJECT, null));
    }

    @Test
    void sendSimpleMessage_EmptyInputs_ThrowsException() {
        // Assert
        assertThrows(IllegalArgumentException.class, () ->
                emailService.sendSimpleMessage("", TEST_SUBJECT, TEST_CONTENT));

        assertThrows(IllegalArgumentException.class, () ->
                emailService.sendSimpleMessage(TEST_TO_ADDRESS, "", TEST_CONTENT));

        assertThrows(IllegalArgumentException.class, () ->
                emailService.sendSimpleMessage(TEST_TO_ADDRESS, TEST_SUBJECT, ""));
    }

    @Test
    void sendSimpleMessage_EmailSenderThrowsException_PropagatesException() {
        // Arrange
        doThrow(new RuntimeException("Mail server error"))
                .when(emailSender).send(any(SimpleMailMessage.class));

        // Assert
        assertThrows(RuntimeException.class, () ->
                emailService.sendSimpleMessage(TEST_TO_ADDRESS, TEST_SUBJECT, TEST_CONTENT));
    }

    @Test
    void sendHtmlMessage_ValidInputs_Success() throws MessagingException {
        // Arrange
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);

        // Act
        emailService.sendHtmlMessage(TEST_TO_ADDRESS, TEST_SUBJECT, "<html><body>" + TEST_CONTENT + "</body></html>");

        // Assert
        verify(emailSender, times(1)).send(messageCaptor.capture());
        assertEquals(mimeMessage, messageCaptor.getValue());
    }

    @Test
    void sendHtmlMessage_NullInputs_ThrowsException() {
        // Assert
        assertThrows(IllegalArgumentException.class, () ->
                emailService.sendHtmlMessage(null, TEST_SUBJECT, TEST_CONTENT));

        assertThrows(IllegalArgumentException.class, () ->
                emailService.sendHtmlMessage(TEST_TO_ADDRESS, null, TEST_CONTENT));

        assertThrows(IllegalArgumentException.class, () ->
                emailService.sendHtmlMessage(TEST_TO_ADDRESS, TEST_SUBJECT, null));
    }

    @Test
    void sendHtmlMessage_EmptyInputs_ThrowsException() {
        // Assert
        assertThrows(IllegalArgumentException.class, () ->
                emailService.sendHtmlMessage("", TEST_SUBJECT, TEST_CONTENT));

        assertThrows(IllegalArgumentException.class, () ->
                emailService.sendHtmlMessage(TEST_TO_ADDRESS, "", TEST_CONTENT));

        assertThrows(IllegalArgumentException.class, () ->
                emailService.sendHtmlMessage(TEST_TO_ADDRESS, TEST_SUBJECT, ""));
    }

    @Test
    void sendHtmlMessage_InvalidHtmlContent_ThrowsException() {
        // Assert
        assertThrows(IllegalArgumentException.class, () ->
                emailService.sendHtmlMessage(TEST_TO_ADDRESS, TEST_SUBJECT, "Invalid HTML Content < >"));
    }

    @Test
    void sendHtmlMessage_EmailSenderThrowsException_PropagatesException() throws MessagingException {
        // Arrange
        doThrow(new MessagingException("Mail server error"))
                .when(emailSender).send(any(MimeMessage.class));

        // Assert
        assertThrows(MessagingException.class, () ->
                emailService.sendHtmlMessage(TEST_TO_ADDRESS, TEST_SUBJECT, "<html><body>" + TEST_CONTENT + "</body></html>"));
    }
}