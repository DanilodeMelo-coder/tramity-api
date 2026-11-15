package br.com.TramityRH.TramityRH.service;

import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailServiceTest {

    @Test
    void enviaCodigoDeRecuperacaoParaDestinatario() throws Exception {
        CapturingMailSender sender = new CapturingMailSender();
        EmailService service = new EmailService(sender);
        setField(service, "host", "smtp.exemplo.com");
        setField(service, "remetente", "nao-responda@tramity.com");

        service.enviarCodigoRecuperacao("pessoa@exemplo.com", "123456");

        assertNotNull(sender.sent);
        assertEquals("pessoa@exemplo.com", sender.sent.getTo()[0]);
        assertEquals("nao-responda@tramity.com", sender.sent.getFrom());
        assertEquals("TramityRH - Código de recuperação de senha", sender.sent.getSubject());
        assertTrue(sender.sent.getText().contains("123456"));
    }

    @Test
    void enviaCodigoDeConviteComValidade() throws Exception {
        CapturingMailSender sender = new CapturingMailSender();
        EmailService service = new EmailService(sender);
        setField(service, "host", "smtp.exemplo.com");
        setField(service, "remetente", "nao-responda@tramity.com");

        LocalDateTime expiraEm = LocalDateTime.of(2026, 11, 20, 14, 30);
        service.enviarCodigoConvite("colaborador@exemplo.com", "654321", expiraEm);

        assertNotNull(sender.sent);
        assertEquals("colaborador@exemplo.com", sender.sent.getTo()[0]);
        assertTrue(sender.sent.getText().contains("654321"));
        assertTrue(sender.sent.getText().contains("20/11/2026 às 14:30"));
    }

    @Test
    void naoEnviaQuandoHostNaoEstaConfigurado() throws Exception {
        CapturingMailSender sender = new CapturingMailSender();
        EmailService service = new EmailService(sender);
        setField(service, "host", "");
        setField(service, "remetente", "nao-responda@tramity.com");

        service.enviarCodigoRecuperacao("pessoa@exemplo.com", "123456");

        assertNull(sender.sent);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static class CapturingMailSender extends JavaMailSenderImpl {

        private SimpleMailMessage sent;

        @Override
        public void send(SimpleMailMessage... simpleMessages) {
            this.sent = simpleMessages[0];
        }
    }
}
