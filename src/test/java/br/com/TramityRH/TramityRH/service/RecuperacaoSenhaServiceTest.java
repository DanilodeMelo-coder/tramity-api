package br.com.TramityRH.TramityRH.service;

import br.com.TramityRH.TramityRH.dto.RecuperarSenhaRequest;
import br.com.TramityRH.TramityRH.model.RecuperacaoSenha;
import br.com.TramityRH.TramityRH.repository.ColaboradorRepository;
import br.com.TramityRH.TramityRH.repository.RecuperacaoSenhaRepository;
import br.com.TramityRH.TramityRH.repository.UsuarioRhEmpresaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecuperacaoSenhaServiceTest {

    @Mock
    private RecuperacaoSenhaRepository recuperacaoRepository;

    @Mock
    private ColaboradorRepository colaboradorRepository;

    @Mock
    private UsuarioRhEmpresaRepository usuarioRhRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private RecuperacaoSenhaService service;

    @Test
    void solicitarGeraCodigoDeSeisDigitosEEnviaPorEmail() {
        String email = "pessoa@exemplo.com";
        when(colaboradorRepository.existsByEmail(email)).thenReturn(true);
        when(recuperacaoRepository.save(any(RecuperacaoSenha.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.solicitar(new RecuperarSenhaRequest(email));

        ArgumentCaptor<RecuperacaoSenha> captor = ArgumentCaptor.forClass(RecuperacaoSenha.class);
        verify(recuperacaoRepository).save(captor.capture());
        RecuperacaoSenha salva = captor.getValue();

        assertEquals(email, salva.getEmail());
        assertNotNull(salva.getCodigo());
        assertTrue(salva.getCodigo().matches("\\d{6}"));
        assertNotNull(salva.getDataExpiracao());
        assertTrue(salva.getDataExpiracao().isAfter(LocalDateTime.now()));
        assertTrue(salva.getDataExpiracao().isBefore(LocalDateTime.now().plusMinutes(16)));
        verify(emailService).enviarCodigoRecuperacao(email, salva.getCodigo());
    }

    @Test
    void solicitarNaoEnviaEmailQuandoUsuarioNaoExiste() {
        String email = "ninguem@exemplo.com";
        when(colaboradorRepository.existsByEmail(email)).thenReturn(false);
        when(usuarioRhRepository.existsByEmail(email)).thenReturn(false);

        service.solicitar(new RecuperarSenhaRequest(email));

        verify(recuperacaoRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }
}
