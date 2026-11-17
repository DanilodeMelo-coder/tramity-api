package br.com.TramityRH.TramityRH.service;

public interface EmailService {

    /**
     * Envia um e-mail.
     * @return true se o envio deu certo, false se falhou
     */
    boolean enviar(String para, String assunto, String html);
}