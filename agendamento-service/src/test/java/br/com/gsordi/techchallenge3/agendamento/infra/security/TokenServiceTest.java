package br.com.gsordi.techchallenge3.agendamento.infra.security;

import br.com.gsordi.techchallenge3.agendamento.infra.exception.TokenInvalidoException;
import br.com.gsordi.techchallenge3.agendamento.usuario.Perfil;
import br.com.gsordi.techchallenge3.agendamento.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TokenServiceTest {

    private TokenService tokenService;
    private final String secret = "minha-chave-secreta-de-testes-123456";

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", secret);
    }

    @Test
    @DisplayName("Deve gerar token JWT com sucesso contendo subject, issuer e claim de perfil")
    void deveGerarTokenComSucesso() {
        var usuario = mock(Usuario.class);
        when(usuario.getEmail()).thenReturn("medico@hospital.com");
        when(usuario.getPerfil()).thenReturn(Perfil.MEDICO);

        var token = tokenService.gerarToken(usuario);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("Deve recuperar o e-mail do subject a partir de um token JWT válido")
    void deveRecuperarEmailComSucesso() {
        var emailEsperado = "paciente@hospital.com";
        var usuario = mock(Usuario.class);
        when(usuario.getEmail()).thenReturn(emailEsperado);
        when(usuario.getPerfil()).thenReturn(Perfil.PACIENTE);

        var token = tokenService.gerarToken(usuario);
        var emailRecuperado = tokenService.recuperarEmail(token);

        assertEquals(emailEsperado, emailRecuperado);
    }

    @Test
    @DisplayName("Deve lançar TokenInvalidoException ao tentar recuperar e-mail de token malformado ou corrompido")
    void deveLancarExcecaoQuandoTokenEstiverInvalido() {
        var tokenInvalido = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.token.malformado";

        var ex = assertThrows(
                TokenInvalidoException.class,
                () -> tokenService.recuperarEmail(tokenInvalido)
        );

        assertEquals("Token JWT inválido ou expirado", ex.getMessage());
    }

    @Test
    @DisplayName("Deve lançar TokenInvalidoException quando o token tiver sido assinado com outro secret")
    void deveLancarExcecaoQuandoAssinaturaForInvalida() {
        var outroTokenService = new TokenService();
        ReflectionTestUtils.setField(outroTokenService, "secret", "outro-secret-diferente-987654");

        var usuario = mock(Usuario.class);
        when(usuario.getEmail()).thenReturn("enfermeiro@hospital.com");
        when(usuario.getPerfil()).thenReturn(Perfil.ENFERMEIRO);

        var tokenOutroSecret = outroTokenService.gerarToken(usuario);

        var ex = assertThrows(
                TokenInvalidoException.class,
                () -> tokenService.recuperarEmail(tokenOutroSecret)
        );

        assertEquals("Token JWT inválido ou expirado", ex.getMessage());
    }
}