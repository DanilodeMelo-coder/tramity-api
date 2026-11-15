package br.com.TramityRH.TramityRH.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.host:}")
    private String host;

    @Value("${tramity.mail.remetente:no-reply@tramity.local}")
    private String remetente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCodigoRecuperacao(String destinatario, String codigo) {
        if (host == null || host.isBlank()) {
            log.warn("MAIL_HOST não configurado. Código de recuperação para {}: {}", destinatario, codigo);
            return;
        }

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject("TramityRH - Código de recuperação de senha");
        mensagem.setText("""
                Olá!

                Recebemos um pedido para redefinir a senha da sua conta no TramityRH.

                Seu código de recuperação é: %s

                Ele expira em 15 minutos e só pode ser usado uma vez.

                Se você não solicitou essa alteração, ignore este e-mail.
                """.formatted(codigo));

        try {
            mailSender.send(mensagem);
            log.info("Código de recuperação enviado para {}", destinatario);
        } catch (Exception e) {
            log.error("Falha ao enviar o e-mail de recuperação para {}", destinatario, e);
        }
    }

    public void enviarCodigoConvite(String destinatario, String codigo, LocalDateTime expiraEm) {
        if (host == null || host.isBlank()) {
            log.warn("MAIL_HOST não configurado. Convite para {}: {}", destinatario, codigo);
            return;
        }

        String validade = expiraEm.format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", Locale.of("pt", "BR")));

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject("TramityRH - Convite para participar de um processo");
        mensagem.setText("""
                Olá!

                Você foi convidado(a) a participar de um processo seletivo no TramityRH.

                Seu código de convite é: %s

                Ele expira em %s.

                Acesse o sistema, crie sua conta com este mesmo e-mail e informe o código
                para aceitar o convite.
                """.formatted(codigo, validade));

        try {
            mailSender.send(mensagem);
            log.info("Código de convite enviado para {}", destinatario);
        } catch (Exception e) {
            log.error("Falha ao enviar o e-mail de convite para {}", destinatario, e);
        }
    }
}
