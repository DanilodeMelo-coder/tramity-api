package br.com.TramityRH.TramityRH.service;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ResendEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);

    private final Resend resend;
    private final String remetente;

    public ResendEmailService(@Value("${resend.api-key}") String apiKey,
                              @Value("${resend.from}") String remetente) {
        this.resend = (apiKey == null || apiKey.isBlank()) ? null : new Resend(apiKey);
        this.remetente = remetente;
    }

    @Override
    public boolean enviar(String para, String assunto, String html) {
        if (resend == null) {
            log.info("[E-MAIL SIMULADO - sem RESEND_API_KEY] Assunto: {} | Conteúdo: {}", assunto, html);
            return false;
        }

        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(remetente)
                    .to(para)
                    .subject(assunto)
                    .html(html)
                    .build();

            resend.emails().send(params);
            return true;
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail. Assunto: {} | Erro: {}", assunto, e.getMessage());
            return false;
        }
    }
}