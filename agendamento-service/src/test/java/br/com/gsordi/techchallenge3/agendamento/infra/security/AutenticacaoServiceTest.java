package br.com.gsordi.techchallenge3.agendamento.infra.security;

import br.com.gsordi.techchallenge3.agendamento.usuario.Usuario;
import br.com.gsordi.techchallenge3.agendamento.usuario.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AutenticacaoService autenticacaoService;

    @Test
    @DisplayName("Deve carregar usuário por username/email com sucesso")
    void deveCarregarUsuarioComSucesso() {
        var email = "usuario@clinica.com";
        var usuarioMock = mock(Usuario.class);

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuarioMock));

        var resultado = autenticacaoService.loadUserByUsername(email);

        assertNotNull(resultado);
        assertEquals(usuarioMock, resultado);
        verify(usuarioRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando usuário não for encontrado")
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        var emailInexistente = "inexistente@clinica.com";

        when(usuarioRepository.findByEmail(emailInexistente)).thenReturn(Optional.empty());

        var ex = assertThrows(
                UsernameNotFoundException.class,
                () -> autenticacaoService.loadUserByUsername(emailInexistente)
        );

        assertEquals("Usuário não encontrado", ex.getMessage());
        verify(usuarioRepository, times(1)).findByEmail(emailInexistente);
    }
}